package com.cornellappdev.android.pollo

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.models.UserSession
import com.cornellappdev.android.pollo.networking.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), GroupFragment.GroupFragmentDelegate {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var viewPager: ViewPager? = null
    private var socket: Socket? = null
    private var joinedGroupFragment: GroupFragment? = null
    private var createdGroupFragment: GroupFragment? = null

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

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

        val account = GoogleSignIn.getLastSignedInAccount(this)
        // If account is null, attempt to sign in, if not, launch the normal activity. updateUI(account);

        if (account != null) {
            val expiresAt = preferencesHelper.expiresAt
            val dateAccessTokenExpires = Date(expiresAt * 1000)
            val currentDate = Date()
            val isAccessTokenExpired = currentDate >= dateAccessTokenExpires
            CoroutineScope(Dispatchers.Main).launch {
                if (isAccessTokenExpired) {
                    val refreshTokenEndpoint = Endpoint.userRefreshSession(preferencesHelper.refreshToken)
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

    private fun finishAuthFlow() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        if (mSectionsPagerAdapter != null) {
            createdGroupFragment?.refreshGroups()
            joinedGroupFragment?.refreshGroups()
            return
        }

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.container)
        viewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SETTINGS_CODE && resultCode == Activity.RESULT_OK) {
            val signInIntent = Intent(this, LoginActivity::class.java)
            startActivityForResult(signInIntent, LOGIN_REQ_CODE)
        }

        if (requestCode == LOGIN_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val idToken = data?.getStringExtra("idToken") ?: ""
            val userAuthenticateEndpoint = Endpoint.userAuthenticate(idToken)
            CoroutineScope(Dispatchers.Main).launch {
                val typeToken = object : TypeToken<ApiResponse<UserSession>>() {}.type
                val userSession = withContext(Dispatchers.IO) { Request.makeRequest<ApiResponse<UserSession>>(userAuthenticateEndpoint.okHttpRequest(), typeToken) }!!.data

                preferencesHelper.accessToken = userSession.accessToken
                preferencesHelper.refreshToken = userSession.refreshToken
                preferencesHelper.expiresAt = userSession.sessionExpiration.toLong()

                println(userSession.refreshToken)
                println(userSession.sessionExpiration)

                User.currentSession = userSession

                finishAuthFlow()
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     *
     * getItem is called to instantiate the fragment for the given page.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            if (position == 0) {
                joinedGroupFragment = joinedGroupFragment ?: GroupFragment.newInstance(position + 1, userRole = User.Role.MEMBER)
                return joinedGroupFragment!!
            }

            createdGroupFragment = createdGroupFragment ?: GroupFragment.newInstance(position + 1, userRole= User.Role.ADMIN)
            return createdGroupFragment!!
        }

        override fun getCount(): Int {
            return 2
        }
    }

    companion object {
        private const val LOGIN_REQ_CODE = 10031
        private const val SETTINGS_CODE = 10032
    }
}
