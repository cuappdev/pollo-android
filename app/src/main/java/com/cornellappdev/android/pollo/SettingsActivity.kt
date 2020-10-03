package com.cornellappdev.android.pollo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.models.UserSession
import com.cornellappdev.android.pollo.networking.Endpoint
import com.cornellappdev.android.pollo.networking.Request
import com.cornellappdev.android.pollo.networking.getUserInfo
import com.cornellappdev.android.pollo.networking.userRefreshSession
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsActivity : AppCompatActivity() {

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        emailText.text = getString(R.string.user_email, User.currentUser.netID)

    }

    fun moreAppsButtonClicked(view: View) {
        val eateryPlayStoreId = "com.cornellappdev.android.eatery"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$eateryPlayStoreId")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$eateryPlayStoreId")))
        }
    }

    fun visitOurWebsiteButtonClicked(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cornellappdev.com")))
    }

    fun sendFeedbackButtonClicked(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSegAMf2UlxxuuoG27SjkWRr4Gc65GGk7mGSRq2eIhjSrL_00w/viewform")))
    }

    fun privacyPolicyButtonClicked(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cornellappdev.com/privacy/policies/pollo")))
    }

    fun logoutButtonClicked(view: View) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build()
        GoogleSignIn.getClient(this, gso).signOut()
        preferencesHelper.accessToken = ""
        preferencesHelper.refreshToken = ""
        preferencesHelper.expiresAt = 0L
        val data = Intent()
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}
