package com.cornellappdev.android.pollo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class ConnectivityBanner(val context: Context) {
    companion object {
        private var callbackRegistered = false
        private var isConnected = true
    }

    // Registers a new NetworkCallback if one does not currently exist or updates existing network callback
    fun setUpConnectivityBanners(view: View) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (callbackRegistered && !isConnected) {
                    isConnected = true
                    displaySnackbar(view)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isConnected = false
                displaySnackbar(view)
            }
        }
        cm.registerNetworkCallback(builder.build(), callback)

        if (!callbackRegistered || !isConnected) {
            displaySnackbar(view)
            callbackRegistered = true
        }
    }

    // Displays appropriate connectivity banner based on current connectivity status
    private fun displaySnackbar(view: View) {
        val color = ContextCompat.getColor(context, if (isConnected) R.color.polloGreen else R.color.red)
        val text = if (isConnected) "Connected" else "Not Connected"
        val duration = if (isConnected) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_INDEFINITE
        val snackbar = Snackbar.make(view, text, duration).setBackgroundTint(color)
        snackbar.show()
    }
}