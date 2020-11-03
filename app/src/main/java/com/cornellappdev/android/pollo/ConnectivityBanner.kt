package com.cornellappdev.android.pollo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

object ConnectivityBanner {
    private var currContext: Context? = null;
    private var currView: View? = null

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
                    val polloGreen = ContextCompat.getColor(currContext!!, R.color.polloGreen)
                    val snackbar = Snackbar.make(currView!!, "Connected", Snackbar.LENGTH_SHORT).setBackgroundTint(polloGreen)
                    snackbar.show()
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    val errorRed = ContextCompat.getColor(currContext!!, R.color.red)
                    val snackbar = Snackbar.make(currView!!, "Not Connected", Snackbar.LENGTH_LONG).setBackgroundTint(errorRed)
                    snackbar.show()
                }
            }
            cm.registerNetworkCallback(builder.build(), callback)
        } else {
            currContext = context
            currView = view
        }
    }
}