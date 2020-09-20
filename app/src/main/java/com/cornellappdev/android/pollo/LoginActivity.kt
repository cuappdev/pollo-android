package com.cornellappdev.android.pollo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 10032

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .requestIdToken(BuildConfig.web_client_id)
                .build()

        val loginButton = findViewById<View>(R.id.sign_in_button) as SignInButton
        loginButton.setOnClickListener(this)
        setGoogleSignInButtonText(loginButton, "Continue with Google")

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        sso_button.setOnClickListener {
            webview.settings.javaScriptEnabled = true
            webview.addJavascriptInterface(WebAppInterface(this), "Mobile")
            webview.webViewClient = WebAppClient()
            // localhost for testing
            // val host = "http://10.0.2.2:8000/"
            val host = "https://" + BuildConfig.TEMP_BACKEND_URI + "/api/v2/auth/saml/cornell/"
            webview.loadUrl(host)
            webview.visibility = View.VISIBLE
        }
    }

    inner class WebAppClient : android.webkit.WebViewClient() {
        /**
         * Ensures that redirects in webview still load in the webview, and not in other browsers
         */
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return false
        }
    }

    /**
     * Interface that allows WebView to call Android functions using JavaScript
     */
    inner class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        // tokens is a stringified version of UserSession JSON
        fun handleToken(sessionInfo: String) {
            runOnUiThread { webview.visibility = View.GONE }
            val data = Intent()
            data.putExtra("sessionInfo", sessionInfo)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onBackPressed() {
        // Pressing the back button closes the webview if it's open, else goes to the home screen
        if (webview.visibility == View.VISIBLE) {
            runOnUiThread { webview.visibility = View.GONE }
        } else {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            super.onBackPressed()
        }
    }

    private fun setGoogleSignInButtonText(signInButton: SignInButton, buttonText: String) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (i in 0 until signInButton.childCount) {
            val v = signInButton.getChildAt(i)

            if (v is TextView) {
                v.text = buttonText
                return
            }
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.sign_in_button) {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            val data = Intent()
            data.putExtra("idToken", account?.idToken)
            setResult(Activity.RESULT_OK, data)
            finish()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //updateUI(null);
            Log.e("ApiException", "signInResult:failed code=" + e.statusCode)
            e.printStackTrace()
        }

    }
}
