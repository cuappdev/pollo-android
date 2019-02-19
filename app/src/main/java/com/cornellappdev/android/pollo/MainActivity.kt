package com.cornellappdev.android.pollo

import android.content.Intent
import android.os.AsyncTask
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View

import android.view.inputmethod.EditorInfo
import android.widget.EditText

import com.cornellappdev.android.pollo.Models.GoogleCredentials
import com.cornellappdev.android.pollo.Models.Nodes.GroupNodeResponse
import com.cornellappdev.android.pollo.Models.User
import com.cornellappdev.android.pollo.Models.UserSession
import com.cornellappdev.android.pollo.Networking.Endpoint
import com.cornellappdev.android.pollo.Networking.Request
import com.cornellappdev.android.pollo.Networking.joinGroupWithCode
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.IOException

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
    private var ViewPager: ViewPager? = null

    internal var userSession: UserSession? = null

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
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // If account is null, attempt to sign in, if not, launch the normal activity. updateUI(account);

        if (account != null) {
            RetrieveUserSessionTask().execute(account)
            return
        }
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
            //TODO: The next step after we join the group
        }
    }


    internal inner class RetrieveUserSessionTask : AsyncTask<GoogleSignInAccount, Void, UserSession>() {

        override fun doInBackground(vararg accounts: GoogleSignInAccount): UserSession? {
            val account = accounts[0]
            try {
                userSession = NetworkUtils.userAuthenticate(baseContext, GoogleCredentials(account.idToken))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return userSession
        }

        override fun onPostExecute(userSession: UserSession?) {
            if (userSession == null) return
            User.currentSession = userSession
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

            // Set up the ViewPager with the sections adapter.
            ViewPager = findViewById(R.id.container)
            ViewPager!!.adapter = mSectionsPagerAdapter

            val tabLayout = findViewById<TabLayout>(R.id.tabs)

            ViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(ViewPager))
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
