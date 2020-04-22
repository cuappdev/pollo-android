package com.cornellappdev.android.pollo.networking

import android.util.Log
import com.cornellappdev.android.pollo.BuildConfig
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.PollChoice
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.HttpUrl
import org.json.JSONObject

interface SocketDelegate {
    fun onPollStart(poll: Poll)
    fun onPollEnd(poll: Poll)
    fun onPollResult(poll: Poll)
    fun onPollDelete(pollID: String)
    fun onPollDeleteLive()
    fun onPollStartAdmin(poll: Poll)
    fun onPollEndAdmin(poll: Poll)
    fun onPollUpdateAdmin(poll: Poll)
}

object Socket {

    data class PollFilter(val success: Boolean, val text: String?, val filter: ArrayList<String>?)

    private var delegates = ArrayList<SocketDelegate>()
    private lateinit var manager: Manager
    private lateinit var socket: Socket


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
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollResult(poll) }
    }

    private val onPollStart = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollStart(poll) }
    }

    private val onPollEnd = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollEnd(poll) }
    }

    private val onPollDelete = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val pollID = args[0] as String
        delegates.forEach { it.onPollDelete(pollID) }
    }

    private val onPollDeleteLive = Emitter.Listener {
        delegates.forEach { it.onPollDeleteLive() }
    }

    private val onPollStartAdmin = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollStart(poll) }
    }

    private val onPollUpdateAdmin = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollResult(poll) }
    }

    private val onPollEndAdmin = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollEnd(poll) }
    }

    fun connect(id: String, accessToken: String) {
        val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host(BuildConfig.BACKEND_URI)
                .build()

        Log.d("Socket URL", urlBuilder.url().toString())
        val socketOptions = IO.Options()
        socketOptions.query = "userType=member&accessToken=$accessToken"
        manager = Manager(urlBuilder.uri(), socketOptions)
        socket = manager.socket("/$id")
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_MESSAGE, onMessage)
        socket.on(Socket.EVENT_CONNECT_ERROR, onFailure)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)

        socket.on("user/poll/start", onPollStart)
        socket.on("user/poll/end", onPollEnd)
        socket.on("user/poll/results", onResults)
        socket.on("user/poll/delete", onPollDelete)
        socket.on("user/poll/delete/live", onPollDeleteLive)

        socket.on("admin/poll/start", onPollStartAdmin)
        socket.on("admin/poll/updates", onPollUpdateAdmin)
        socket.on("admin/poll/ended", onPollEndAdmin)

        socket.connect()
    }

    fun disconnect() {
        socket.disconnect()
    }

    fun add(socketDelegate: SocketDelegate) {
        delegates.add(socketDelegate)
    }

    fun delete(socketDelegate: SocketDelegate) {
        delegates = ArrayList(delegates.filter { it != socketDelegate })
    }

    fun serverStart(newPoll: Poll) {
        socket.emit("server/poll/start", JSONObject(Gson().toJson(newPoll)))
    }

    fun serverEnd() {
        socket.emit("server/poll/end")
    }

    fun shareResults(poll: Poll) {
        socket.emit("server/poll/results", poll.id ?: "")
    }

    fun deleteSavedPoll(poll: Poll) {
        socket.emit("server/poll/delete", poll.id ?: "")
    }

    fun deleteLivePoll() {
        socket.emit("server/poll/delete/live")
    }

    fun sendMCAnswer(pollChoice: PollChoice) {
        socket.emit("server/poll/answer", JSONObject(Gson().toJson(pollChoice)))
    }
}