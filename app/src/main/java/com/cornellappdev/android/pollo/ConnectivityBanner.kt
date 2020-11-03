package com.cornellappdev.android.pollo

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

object ConnectivityBanner {
    private var currContext: Context? = null;
    private var currView: View? = null
    private var isConnected = true

    // Registers a new NetworkCallback if one does not currently exist or updates existing network callback
    fun setUpConnectivityBanners(context: Context, view: View) {
        if (currContext == null) {
            currContext = context
            currView = view
            val cm = currContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    isConnected = true
                    displaySnackbar()
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    isConnected = false
                    displaySnackbar()
                }
            }
            cm.registerNetworkCallback(builder.build(), callback)
        } else {
            currContext = context
            currView = view
            if (!isConnected) {
                displaySnackbar()
            }
        }
    }
    
    // Displays appropriate connectivity banner based on current connectivity status
    private fun displaySnackbar() {
        val color = ContextCompat.getColor(currContext!!, if (isConnected) R.color.polloGreen else R.color.red)
        val text = if (isConnected) "Connected" else "Not Connected"
        val duration = if (isConnected) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_INDEFINITE
        val snackbar = Snackbar.make(currView!!, text, duration).setBackgroundTint(color)
        snackbar.show()
    }
}