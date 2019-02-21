package com.cornellappdev.android.pollo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.cornellappdev.android.pollo.Models.ApiResponse
import com.cornellappdev.android.pollo.Models.Nodes.GroupNodeResponse
import com.cornellappdev.android.pollo.Models.Nodes.UserSessionNode
import com.cornellappdev.android.pollo.Models.User
import com.cornellappdev.android.pollo.Models.UserSession
import com.cornellappdev.android.pollo.Networking.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity(), GroupRecyclerView.ItemClickListener {

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

    internal var userSession: UserSession? = null

    private var socket: Socket? = null

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    private val joinPollTextWatcher = object: TextWatcher {

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            Log.d("Count", count.toString())
            //join_poll_group.isEnabled = count == 6
            //join_poll_group.isClickable = count == 6
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<EditText>(R.id.edit_text_join_poll)
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

        // Add listener for when join button is pressed
        join_poll_group.setOnClickListener { joinGroup(editText.text.toString()) }

        val account = GoogleSignIn.getLastSignedInAccount(this)
        // If account is null, attempt to sign in, if not, launch the normal activity. updateUI(account);

        if (account != null) {
            val expiresAt = preferencesHelper.expiresAt
            val dateAccessTokenExpires = Date(expiresAt)
            val currentDate = Date()
            val isAccessTokenExpired = currentDate <= dateAccessTokenExpires
            CoroutineScope(Dispatchers.Main).launch {
                if (isAccessTokenExpired) {
                    val refreshTokenEndpoint = Endpoint.userRefreshSession(preferencesHelper.refreshToken)
                    val userSession = withContext(Dispatchers.IO) { Request.makeRequest<UserSessionNode>(refreshTokenEndpoint.okHttpRequest()) }.data
                    User.currentSession = userSession
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

    fun showSettings(view: View) {
        val settings = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(settings)
    }

    fun joinGroup(code: String) {
        val endpoint = Endpoint.joinGroupWithCode(code)
        CoroutineScope(Dispatchers.IO).launch {
            val groupNodeResponse = Request.makeRequest<GroupNodeResponse>(endpoint.okHttpRequest())
            val allPollsEndpoint = Endpoint.getSortedPolls(groupNodeResponse.data.node.id)
            Request.makeRequest<ApiResponse<List<GetSortedPollsResponse>>>(allPollsEndpoint.okHttpRequest())
            // CURRENTLY BEING USED FOR TESTING SOCKETS
            // startSocket(id=groupNodeResponse.data.node.id)
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
        if (requestCode == LOGIN_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val idToken = data?.getStringExtra("idToken") ?: ""
            val userAuthenticateEndpoint = Endpoint.userAuthenticate(idToken)
            CoroutineScope(Dispatchers.Main).launch {
                val userSession = withContext(Dispatchers.IO) { Request.makeRequest<UserSessionNode>(userAuthenticateEndpoint.okHttpRequest()) }.data

                preferencesHelper.accessToken = userSession.accessToken
                preferencesHelper.refreshToken = userSession.refreshToken
                preferencesHelper.expiresAt = userSession.sessionExpiration

                User.currentSession = userSession

                 finishAuthFlow()
            }
        }
    }


    override fun onItemClick(view: View, position: Int) {
        val pollActivity = Intent(this, PollGroupActivity::class.java)
        startActivity(pollActivity)
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     *
     * getItem is called to instantiate the fragment for the given page.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager): FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return GroupFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            return 2
        }
    }

    companion object {
        private val LOGIN_REQ_CODE = 10031
    }
}
