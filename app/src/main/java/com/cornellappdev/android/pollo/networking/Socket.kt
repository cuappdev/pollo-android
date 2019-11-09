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
import java.util.*

interface SocketDelegate {
    fun onPollStart(poll: Poll)
    fun onPollEnd(poll: Poll)
    fun onPollResult(poll: Poll)
    fun freeResponseSubmissionSuccessful()
    fun freeResponseSubmissionFailed(pollFilter: com.cornellappdev.android.pollo.networking.Socket.PollFilter)
    fun freeResponseUpdates(poll: Poll)
    fun onPollDelete(pollID: String)
    fun onPollDeleteLive()
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
        val json= args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollStart(poll) }
    }

    private val onPollEnd = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.onPollEnd(poll) }
    }

    private val onFreeResponseFilter = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val pollFilter = Gson().fromJson<PollFilter>(json.toString(), PollFilter::class.java)
        if (pollFilter.success) {
            delegates.forEach { it.freeResponseSubmissionSuccessful() }
        } else {
            delegates.forEach { it.freeResponseSubmissionFailed(pollFilter) }
        }
    }

    private val onFreeResponseLive = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val json = args[0] as JSONObject
        val poll = Gson().fromJson<Poll>(json.toString(), Poll::class.java)
        delegates.forEach { it.freeResponseUpdates(poll) }
    }


    private val onPollDelete = Emitter.Listener { args ->
        if (args.isEmpty()) return@Listener
        val pollID = args[0] as String
        delegates.forEach { it.onPollDelete(pollID) }
    }

    private val onPollDeleteLive = Emitter.Listener {
        delegates.forEach { it.onPollDeleteLive() }
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
        socket.on("user/poll/fr/live", onFreeResponseLive)
        socket.on("user/poll/delete", onPollDelete)
        socket.on("user/poll/delete/live", onPollDeleteLive)
        socket.on("user/poll/fr/filter", onFreeResponseFilter)

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

    fun sendMCAnswer(pollChoice: PollChoice) {
        socket.emit("server/poll/answer", JSONObject(Gson().toJson(pollChoice)))
    }

    fun sendUpvoteAnswer(pollChoice: PollChoice) {
        socket.emit("server/poll/upvote", JSONObject(Gson().toJson(pollChoice)))
    }

}