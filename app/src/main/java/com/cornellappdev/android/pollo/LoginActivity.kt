package com.cornellappdev.android.pollo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val ssoLogo = ContextCompat.getDrawable(this, R.drawable.cornell_sso_logo)
        val ssoLogoColor = ContextCompat.getColor(this, R.color.loginGray)
        ssoLogo?.colorFilter = PorterDuffColorFilter(ssoLogoColor, PorterDuff.Mode.SRC_ATOP)
        sso_button.setCompoundDrawablesWithIntrinsicBounds(ssoLogo, null, null, null)

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        sso_button.setOnClickListener {
            webview.settings.javaScriptEnabled = true
            webview.addJavascriptInterface(WebAppInterface(this), "Mobile")
            webview.webViewClient = WebAppClient()
            val host = "https://" + BuildConfig.BACKEND_URI + "/api/v2/auth/saml/cornell/"
            webview.loadUrl(host)
            webview.visibility = View.VISIBLE
        }
    }

    inner class WebAppClient : android.webkit.WebViewClient() {
        // Ensures that redirects in webview still load in the webview, and not in other browsers
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return false
        }
    }

    // Interface that allows WebView to call Android functions using JavaScript
    inner class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        // sessionInfo is a stringified version of UserSession JSON
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
}
