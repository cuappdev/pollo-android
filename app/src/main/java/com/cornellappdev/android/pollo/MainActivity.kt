package com.cornellappdev.android.pollo

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.models.UserSession
import com.cornellappdev.android.pollo.networking.*
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), GroupFragment.GroupFragmentDelegate {

    /**
     * [FragmentStateAdapter] that will provide
     * fragments for each of the sections.
     */
    private var pagerAdapter: FragmentStateAdapter? = null

    /**
     * Fragments for each section.
     */
    private var joinedGroupFragment: GroupFragment? = null
    private var createdGroupFragment: GroupFragment? = null

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // If there is no accessToken in preferences, attempt to sign in, otherwise launch the normal activity
        if (preferencesHelper.accessToken!!.isNotEmpty()) {
            val expiresAt = preferencesHelper.expiresAt
            val dateAccessTokenExpires = Date(expiresAt * 1000)
            val currentDate = Date()
            val isAccessTokenExpired = currentDate >= dateAccessTokenExpires
            CoroutineScope(Dispatchers.Main).launch {
                if (isAccessTokenExpired) {
                    val refreshTokenEndpoint = Endpoint.userRefreshSession(preferencesHelper.refreshToken as String)
                    val typeToken = object : TypeToken<ApiResponse<UserSession>>() {}.type
                    val userSession = withContext(Dispatchers.IO) {
                        Request.makeRequest<ApiResponse<UserSession>>(refreshTokenEndpoint.okHttpRequest(), typeToken)
                    }!!.data
                    User.currentSession = userSession

                    if (userSession.sessionExpiration == null) {
                        val signInIntent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivityForResult(signInIntent, LOGIN_REQ_CODE)
                        return@launch
                    }

                    preferencesHelper.refreshToken = userSession.refreshToken
                    preferencesHelper.accessToken = userSession.accessToken
                    preferencesHelper.expiresAt = userSession.sessionExpiration.toLong()
                } else {
                    User.currentSession = UserSession(preferencesHelper.accessToken, preferencesHelper.refreshToken, expiresAt.toString(), true)
                }

                finishAuthFlow()
            }
            return
        }

        // If account is null, we need to prompt them to login
        val signInIntent = Intent(this, LoginActivity::class.java)
        startActivityForResult(signInIntent, LOGIN_REQ_CODE)
    }

    override fun onRestart() {
        super.onRestart()
        if (pagerAdapter != null) {
            createdGroupFragment?.refreshGroups()
            joinedGroupFragment?.refreshGroups()
            return
        }
    }

    override fun onBackPressed() {
        if (appbar.alpha < 1.0f) {
            joinedGroupFragment?.dismissPopup()
            createdGroupFragment?.dismissPopup()
        } else {
            super.onBackPressed()
        }
    }

    override fun setDim(shouldDim: Boolean, groupFragment: GroupFragment) {
        when (groupFragment) {
            joinedGroupFragment -> createdGroupFragment?.setSelfDim(shouldDim)
            createdGroupFragment -> joinedGroupFragment?.setSelfDim(shouldDim)
        }

        settingsImageView.isEnabled = !shouldDim

        val alphaValue = if (shouldDim) 0.5f else 1.0f
        val dimAnimation = ObjectAnimator.ofFloat(appbar, "alpha", alphaValue)
        dimAnimation.duration = 500
        dimAnimation.start()
    }

    override fun startGroupActivity(role: User.Role, group: Group, polls: ArrayList<GetSortedPollsResponse>) {
        val pollsDateActivity = Intent(this@MainActivity, PollsDateActivity::class.java)
        pollsDateActivity.putExtra("SORTED_POLLS", polls)
        pollsDateActivity.putExtra("GROUP_NODE", group)
        pollsDateActivity.putExtra("USER_ROLE", role)
        startActivity(pollsDateActivity)
    }

    fun showSettings(view: View) {
        val settings = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivityForResult(settings, SETTINGS_CODE)
    }

    // Saves current user information
    private fun getUser() {
        CoroutineScope(Dispatchers.Main).launch {
            val getUserInfoEndpoint = Endpoint.getUserInfo()
            val typeToken = object : TypeToken<ApiResponse<User>>() {}.type
            val userInfo = withContext(Dispatchers.IO) {
                Request.makeRequest<ApiResponse<User>>(getUserInfoEndpoint.okHttpRequest(), typeToken)
            }!!.data

            User.currentUser = userInfo
        }
    }

    private fun finishAuthFlow() {
        getUser()

        if (pagerAdapter != null) {
            createdGroupFragment?.refreshGroups()
            joinedGroupFragment?.refreshGroups()
            return
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        pagerAdapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                if (position == 0) {
                    joinedGroupFragment = joinedGroupFragment
                            ?: GroupFragment.newInstance(position + 1, userRole = User.Role.MEMBER)
                    return joinedGroupFragment!!
                }

                createdGroupFragment = createdGroupFragment
                        ?: GroupFragment.newInstance(position + 1, userRole = User.Role.ADMIN)
                return createdGroupFragment!!
            }

            override fun getItemCount(): Int {
                return 2
            }
        }

        viewPager.adapter = pagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        createdGroupFragment?.dismissPopup()
                    }
                    1 -> {
                        joinedGroupFragment?.dismissPopup()
                    }
                }
            }
        })

        TabLayoutMediator(tabs, viewPager) { tab, position -> tab.text = if (position == 0) getString(R.string.joined) else getString(R.string.created) }.attach()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            val signInIntent = Intent(this, LoginActivity::class.java)
            startActivityForResult(signInIntent, LOGIN_REQ_CODE)
        }

        if (requestCode == LOGIN_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val accessToken = data?.getStringExtra("accessToken")
            val refreshToken = data?.getStringExtra("refreshToken")
            val expiresAt = data?.getStringExtra("sessionExpiration")

            preferencesHelper.accessToken = accessToken
            preferencesHelper.refreshToken = refreshToken
            preferencesHelper.expiresAt = expiresAt!!.toLong()

            val session = UserSession(accessToken, refreshToken, expiresAt, true)
            User.currentSession = session
            finishAuthFlow()
        }
    }

    companion object {
        private const val LOGIN_REQ_CODE = 10031
        private const val SETTINGS_CODE = 10032
    }
}
