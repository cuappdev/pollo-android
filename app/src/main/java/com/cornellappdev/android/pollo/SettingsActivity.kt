package com.cornellappdev.android.pollo

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn

import kotlinx.android.synthetic.main.activity_settings.*
import android.content.Intent
import android.net.Uri
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        emailText.text = googleAccount?.email ?: "Unknown"

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
        val data = Intent()
        setResult(Activity.RESULT_OK, data)
        finish()
    }
}
