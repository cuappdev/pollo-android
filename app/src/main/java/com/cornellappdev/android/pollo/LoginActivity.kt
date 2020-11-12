package com.cornellappdev.android.pollo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cornellappdev.android.pollo.models.ApiResponse
import com.cornellappdev.android.pollo.models.User
import com.cornellappdev.android.pollo.models.UserSession
import com.cornellappdev.android.pollo.networking.Endpoint
import com.cornellappdev.android.pollo.networking.Request
import com.cornellappdev.android.pollo.networking.dummyUserLogin
import com.cornellappdev.android.pollo.networking.userRefreshSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val ssoLogo = ContextCompat.getDrawable(this, R.drawable.cornell_logo)
        val ssoLogoColor = ContextCompat.getColor(this, R.color.loginGray)
        ssoLogo?.colorFilter = PorterDuffColorFilter(ssoLogoColor, PorterDuff.Mode.SRC_ATOP)
        sso_button.setCompoundDrawablesWithIntrinsicBounds(ssoLogo, null, null, null)

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        sso_button.setOnClickListener {
            WebView.setWebContentsDebuggingEnabled(false)
            webview.settings.javaScriptEnabled = true
            webview.addJavascriptInterface(WebAppInterface(this), "Mobile")
            webview.webViewClient = WebAppClient()
            val host = "https://" + BuildConfig.BACKEND_URI + "/api/v2/auth/saml/cornell/"
            webview.loadUrl(host)
            webview.visibility = View.VISIBLE
        }

        // Dummy login, to be removed before pushing to production
        if (BuildConfig.DUMMY_LOGIN_ENABLED) {
            val buttons = arrayOf(dummy_login_button, dummy_login_button_2)
            for ((i, button) in buttons.withIndex()) {
                button.visibility = View.VISIBLE
                button.text = getString(R.string.dummy_login, i)
                button.setOnClickListener {
                    val userId = if (i == 0) BuildConfig.DUMMY_USER_ID1 else BuildConfig.DUMMY_USER_ID2
                    dummyLogin(userId)
                }
            }
        }
    }

    private fun dummyLogin(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val dummyLoginEndpoint = Endpoint.dummyUserLogin(userId)
            val typeToken = object : TypeToken<ApiResponse<UserSession>>() {}.type
            val userSession = withContext(Dispatchers.IO) {
                Request.makeRequest<ApiResponse<UserSession>>(dummyLoginEndpoint.okHttpRequest(), typeToken)
            }!!.data
            sendSessionInfo(userSession)
        }
    }

    private fun sendSessionInfo(session: UserSession) {
        val data = Intent()
        data.putExtra("accessToken", session.accessToken)
        data.putExtra("refreshToken", session.refreshToken)
        data.putExtra("sessionExpiration", session.sessionExpiration)
        setResult(Activity.RESULT_OK, data)
        finish()
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
            sendSessionInfo(Gson().fromJson(sessionInfo, UserSession::class.java))
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
