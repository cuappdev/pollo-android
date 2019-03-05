package com.cornellappdev.android.pollo.Networking

import android.util.Log
import com.cornellappdev.android.pollo.BuildConfig
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class Socket(val id: String, val googleUserID: String) {

    private val client = OkHttpClient.Builder().build()
    private val manager: Manager
    private val socket: Socket

    private val onConnect = Emitter.Listener {
        println("CONNECTED!!!")
    }

    private val onMessage = Emitter.Listener { args ->
        println(args)
    }

    private val onFailure = Emitter.Listener {
        println("FAILED TO CONNECT")
    }

    private val onDisconnect = Emitter.Listener {
        println("Disconnected!")
    }

    private val onResults = Emitter.Listener { args ->
        println(args.toString())
    }

    init {
        val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host(BuildConfig.BACKEND_URI)
                .build()

        Log.d("Socket URL", urlBuilder.url().toString())
        val socketOptions = IO.Options()
        socketOptions.query = "userType=member&googleID=$googleUserID"
        manager = Manager(urlBuilder.uri(), socketOptions)
        socket = manager.socket("/$id")
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_MESSAGE, onMessage)
        socket.on(Socket.EVENT_CONNECT_ERROR, onFailure)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.on("user/poll/results", onResults)

        socket.connect()
    }

}