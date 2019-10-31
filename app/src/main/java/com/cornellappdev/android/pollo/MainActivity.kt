package com.cornellappdev.android.pollo

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.Group
import com.cornellappdev.android.pollo.models.GroupCode
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.models.UserSession
import com.cornellappdev.android.pollo.networking.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.manage_group_view.*
import kotlinx.android.synthetic.main.manage_group_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity(), GroupFragment.OnMoreButtonPressedListener {

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
    private var groupSelected: Group? = null
    private var socket: Socket? = null
    private var joinedGroupFragment: GroupFragment? = null
    private var createdGroupFragment: GroupFragment? = null

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    private val joinPollTextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val hasSixCharacters = s?.length == 6
            joinGroupButton.isEnabled = hasSixCharacters
            val correctBackground = if (hasSixCharacters) R.drawable.rounded_join_button_filled else R.drawable.rounded_join_button
            joinGroupButton.setBackgroundResource(correctBackground)
        }
    }

    private val createPollTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val hasText = s?.length != 0
            createGroupButton.isEnabled = hasText
            val correctBackground = if (hasText) R.drawable.rounded_join_button_filled else R.drawable.rounded_join_button
            createGroupButton.setBackgroundResource(correctBackground)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Joining group bottom bar setup
        editTextJoinGroup.filters = editTextJoinGroup.filters + InputFilter.AllCaps()
        editTextJoinGroup.addTextChangedListener(joinPollTextWatcher)
        editTextJoinGroup.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> editTextJoinGroup.isCursorVisible = hasFocus }
        editTextJoinGroup.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH -> {
                    editTextJoinGroup.isCursorVisible = false
                    true
                }
                else -> {
                    editTextJoinGroup.isCursorVisible = true
                    false
                }
            }
        }

        // Creating group bottom bar setup
        editTextCreateGroup.addTextChangedListener(createPollTextWatcher)
        editTextCreateGroup.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> editTextCreateGroup.isCursorVisible = hasFocus }
        editTextCreateGroup.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH -> {
                    editTextCreateGroup.isCursorVisible = false
                    true
                }
                else -> {
                    editTextCreateGroup.isCursorVisible = true
                    false
                }
            }
        }

        editTextCreateGroup.visibility = View.GONE
        createGroupButton.visibility = View.GONE
        editGroupName.visibility = View.GONE
        endPoll.visibility = View.GONE
        deleteGroup.visibility = View.GONE
        leaveGroup.visibility = View.VISIBLE

        joinGroupButton.isEnabled = false
        createGroupButton.isEnabled = false

        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        editTextCreateGroup.visibility = View.GONE
                        createGroupButton.visibility = View.GONE
                        editTextJoinGroup.visibility = View.VISIBLE
                        joinGroupButton.visibility = View.VISIBLE

                        editGroupName.visibility = View.GONE
                        endPoll.visibility = View.GONE
                        deleteGroup.visibility = View.GONE
                        leaveGroup.visibility = View.VISIBLE
                    }
                    1 -> {
                        editTextCreateGroup.visibility = View.VISIBLE
                        createGroupButton.visibility = View.VISIBLE
                        editTextJoinGroup.visibility = View.GONE
                        joinGroupButton.visibility = View.GONE

                        editGroupName.visibility = View.VISIBLE
                        endPoll.visibility = View.VISIBLE
                        deleteGroup.visibility = View.VISIBLE
                        leaveGroup.visibility = View.GONE

//                        groupMenuOptionsView.y = groupMenuOptionsView.y + (589 - 325)
                    }
                }
            }

        })

        groupMenuOptionsView.closeButton.setOnClickListener {
            dismissPopup()
        }

        groupMenuOptionsView.leaveGroup.setOnClickListener {
            val groupId = groupSelected?.id ?: return@setOnClickListener
            val leaveGroupEndpoint = Endpoint.leaveGroup(groupId)

            CoroutineScope(Dispatchers.IO).launch {
                val typeToken = object : TypeToken<ApiResponse<String>>() {}.type
                Request.makeRequest<ApiResponse<String>>(leaveGroupEndpoint.okHttpRequest(), typeToken)
            }
            joinedGroupFragment?.removeGroup(groupId)
            dismissPopup()
        }

        // Add listener for when join and create buttons are pressed
        joinGroupButton.setOnClickListener {
            joinGroup(editTextJoinGroup.text.toString())
            editTextJoinGroup.setText("")
        }
        createGroupButton.setOnClickListener {
            createGroup(editTextCreateGroup.text.toString())
            editTextCreateGroup.setText("")
        }

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

    override fun onMoreButtonPressed(group: Group?) {
        manageDim(true)
        groupSelected = group
        groupMenuOptionsView.groupNameTextView.text = group?.name ?: "Pollo Group"
        groupMenuOptionsView.visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, groupMenuOptionsView.height.toFloat(), 0f)
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
    }

    private fun dismissPopup() {
        manageDim(false)
        dimView.isClickable = false
        dimView.isFocusable = false
        val animate = TranslateAnimation(0f, 0f, 0f, groupMenuOptionsView.height.toFloat())
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
        groupMenuOptionsView.visibility = View.INVISIBLE
        groupSelected = null
    }

    private fun manageDim(shouldDim: Boolean) {
        val alphaValue = if (shouldDim) 0.5f else 1.0f
        val dimAnimation = ObjectAnimator.ofFloat(dimView, "alpha", alphaValue)
        dimAnimation.duration = 500
        dimAnimation.start()
    }

    fun showSettings(view: View) {
        val settings = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivityForResult(settings, SETTINGS_CODE)
    }

    private fun joinGroup(code: String) {
        val endpoint = Endpoint.joinGroupWithCode(code)
        CoroutineScope(Dispatchers.IO).launch {
            val typeTokenGroupNode = object : TypeToken<ApiResponse<Group>>() {}.type
            val typeTokenSortedPolls = object : TypeToken<ApiResponse<ArrayList<GetSortedPollsResponse>>>() {}.type
            val groupResponse = Request.makeRequest<ApiResponse<Group>>(endpoint.okHttpRequest(), typeTokenGroupNode)

            if (groupResponse?.success == false || groupResponse?.data == null) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("Code Not Valid")
                            .setMessage("Failed to join session with code $code.\nTry again!")
                            .setNeutralButton(android.R.string.ok, null)
                            .show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                joinedGroupFragment?.addGroup(groupResponse.data)
            }

            val allPollsEndpoint = Endpoint.getSortedPolls(groupResponse.data.id)
            val sortedPolls = Request.makeRequest<ApiResponse<ArrayList<GetSortedPollsResponse>>>(allPollsEndpoint.okHttpRequest(), typeTokenSortedPolls)

            if (sortedPolls?.success == false || sortedPolls?.data == null) return@launch

            val pollsDateActivity = Intent(this@MainActivity, PollsDateActivity::class.java)
            pollsDateActivity.putExtra("SORTED_POLLS", sortedPolls.data)
            pollsDateActivity.putExtra("GROUP_NODE", groupResponse.data)
            pollsDateActivity.putExtra("USER_ROLE", User.Role.MEMBER)
            startActivity(pollsDateActivity)
        }
    }

    private fun createGroup(name: String) {
        val typeTokenGroupCode = object : TypeToken<ApiResponse<GroupCode>>() {}.type
        val generateCodeEndpoint = Endpoint.generateCode()

        CoroutineScope(Dispatchers.IO).launch {
            val result = Request.makeRequest<ApiResponse<GroupCode>>(generateCodeEndpoint.okHttpRequest(),typeTokenGroupCode)

            if (result?.success == true) {
                val code = result.data.code

                val joinSessionEndpoint = Endpoint.startSession(code, name)
                val typeTokenGroupNode = object : TypeToken<ApiResponse<Group>>() {}.type
                val typeTokenSortedPolls = object : TypeToken<ApiResponse<ArrayList<GetSortedPollsResponse>>>() {}.type
                val groupResponse = Request.makeRequest<ApiResponse<Group>>(joinSessionEndpoint.okHttpRequest(), typeTokenGroupNode)

                if (groupResponse?.success == false || groupResponse?.data == null) {
                    return@launch
                }


                withContext(Dispatchers.Main) {
                    createdGroupFragment?.addGroup(groupResponse.data)
                }

                val allPollsEndpoint = Endpoint.getSortedPolls(groupResponse.data.id)
                val sortedPolls = Request.makeRequest<ApiResponse<ArrayList<GetSortedPollsResponse>>>(allPollsEndpoint.okHttpRequest(), typeTokenSortedPolls)

                if (sortedPolls?.success == false || sortedPolls?.data == null) return@launch

                val pollsDateActivity = Intent(this@MainActivity, PollsDateActivity::class.java)
                pollsDateActivity.putExtra("SORTED_POLLS", sortedPolls.data)
                pollsDateActivity.putExtra("GROUP_NODE", groupResponse.data)
                pollsDateActivity.putExtra("USER_ROLE", User.Role.ADMIN)
                startActivity(pollsDateActivity)

            } else {
                Log.e("failure","backend response failed to generate code")
                return@launch
            }
        }


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
                joinedGroupFragment = joinedGroupFragment ?: GroupFragment.newInstance(position + 1, this@MainActivity, userRole = User.Role.MEMBER)
                return joinedGroupFragment!!
            }

            createdGroupFragment = createdGroupFragment ?: GroupFragment.newInstance(position + 1, this@MainActivity, userRole= User.Role.ADMIN)
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
