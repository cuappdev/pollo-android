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
    private var currView: View? = null
    private var isConnected = true
    private var currSnackbar: Snackbar? = null

    // Registers a new NetworkCallback if one does not currently exist or updates existing network callback
    fun setUpConnectivityBanners(context: Context, view: View) {
        if (currView == null) {
            currView = view
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val builder = NetworkRequest.Builder()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    isConnected = true
                    displaySnackbar(context)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    isConnected = false
                    displaySnackbar(context)
                }
            }
            cm.registerNetworkCallback(builder.build(), callback)
        } else {
            currView = view
            if (!isConnected) {
                displaySnackbar(context)
            }
        }
    }

    // Displays appropriate connectivity banner based on current connectivity status
    private fun displaySnackbar(context: Context) {
        currSnackbar?.dismiss()
        val color = ContextCompat.getColor(context, if (isConnected) R.color.polloGreen else R.color.red)
        val text = if (isConnected) "Connected" else "Not Connected"
        val duration = if (isConnected) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_INDEFINITE
        val snackbar = Snackbar.make(currView!!, text, duration).setBackgroundTint(color)
        snackbar.show()
        currSnackbar = snackbar
    }
}