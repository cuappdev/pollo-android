package com.cornellappdev.android.pollo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.cornellappdev.android.pollo.Models.ApiResponse
import com.cornellappdev.android.pollo.Models.Group
import com.cornellappdev.android.pollo.Models.Nodes.GroupNodeResponse
import com.cornellappdev.android.pollo.Models.Nodes.UserSessionNode
import com.cornellappdev.android.pollo.Models.User
import com.cornellappdev.android.pollo.Models.UserSession
import com.cornellappdev.android.pollo.Networking.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.manage_group_view.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

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

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    private val joinPollTextWatcher = object: TextWatcher {

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val hasSixCharacters = s?.length == 6
            join_poll_group.isEnabled = hasSixCharacters
            val correctBackgroundColor = if (hasSixCharacters) R.color.colorPrimary else R.color.settings_detail
            join_poll_group.setBackgroundColor(resources.getColor(correctBackgroundColor))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<EditText>(R.id.edit_text_join_poll)
        editText.filters = editText.filters + InputFilter.AllCaps()
        editText.addTextChangedListener(joinPollTextWatcher)
        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> editText.isCursorVisible = hasFocus }
        editText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_SEARCH -> {
                    editText.isCursorVisible = false
                    true
                }
                else -> {
                    editText.isCursorVisible = true
                    false
                }
            }
        }

        groupMenuOptionsView.closeButton.setOnClickListener {
            val animate = TranslateAnimation(0f, 0f, 0f, groupMenuOptionsView.height.toFloat())
            animate.duration = 300
            animate.fillAfter = true
            groupMenuOptionsView.startAnimation(animate)
            groupMenuOptionsView.visibility = View.INVISIBLE
        }

        // Add listener for when join button is pressed
        join_poll_group.setOnClickListener { joinGroup(editText.text.toString()) }

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
                    val typeToken = object : TypeToken<UserSessionNode>() {}.type
                    val userSession = withContext(Dispatchers.IO) {
                        Request.makeRequest<UserSessionNode>(refreshTokenEndpoint.okHttpRequest(), typeToken)
                    }!!.data
                    User.currentSession = userSession
                    preferencesHelper.refreshToken = userSession.refreshToken
                    preferencesHelper.accessToken = userSession.accessToken
                    preferencesHelper.expiresAt = userSession.sessionExpiration
                } else {
                    User.currentSession = UserSession(preferencesHelper.accessToken, preferencesHelper.refreshToken, expiresAt, true)
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
        groupMenuOptionsView.visibility = View.VISIBLE
        val animate = TranslateAnimation(0f, 0f, groupMenuOptionsView.height.toFloat(), 0f)
        animate.duration = 300
        animate.fillAfter = true
        groupMenuOptionsView.startAnimation(animate)
    }

    fun showSettings(view: View) {
        val settings = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivityForResult(settings, SETTINGS_CODE)
    }

    fun joinGroup(code: String) {
        val endpoint = Endpoint.joinGroupWithCode(code)
        CoroutineScope(Dispatchers.IO).launch {
            val typeTokenGroupNode = object : TypeToken<GroupNodeResponse>() {}.type
            val typeTokenSortedPolls = object : TypeToken<ApiResponse<ArrayList<GetSortedPollsResponse>>>() {}.type
            val groupNodeResponse = Request.makeRequest<GroupNodeResponse>(endpoint.okHttpRequest(), typeTokenGroupNode)

            if(groupNodeResponse?.success == false || groupNodeResponse?.data == null) {
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Code Not Valid")
                        .setMessage("Failed to join session with code $code.\nTry again!")
                        .setNeutralButton(android.R.string.ok, null)
                        .show()
                }
                return@launch
            }

            val allPollsEndpoint = Endpoint.getSortedPolls(groupNodeResponse.data.node.id)
            val sortedPolls = Request.makeRequest<ApiResponse<ArrayList<GetSortedPollsResponse>>>(allPollsEndpoint.okHttpRequest(), typeTokenSortedPolls)

            if(sortedPolls?.success == false || sortedPolls?.data == null) return@launch

            val pollsDateActivity = Intent(this@MainActivity, PollsDateActivity::class.java)
            pollsDateActivity.putExtra("SORTED_POLLS", sortedPolls.data)
            pollsDateActivity.putExtra("GROUP_NODE", groupNodeResponse.data.node)
            startActivity(pollsDateActivity)
        }
    }

    // CURRENTLY USED FOR TESTING SOCKETS
    private fun startSocket(id: String) {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        socket = Socket(id=id, googleUserID=account?.id ?: "")
    }

    private fun finishAuthFlow() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
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
                val typeToken = object : TypeToken<UserSessionNode>() {}.type
                val userSession = withContext(Dispatchers.IO) { Request.makeRequest<UserSessionNode>(userAuthenticateEndpoint.okHttpRequest(), typeToken) }!!.data

                preferencesHelper.accessToken = userSession.accessToken
                preferencesHelper.refreshToken = userSession.refreshToken
                preferencesHelper.expiresAt = userSession.sessionExpiration

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
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager): FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return GroupFragment.newInstance(position + 1, this@MainActivity)
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
