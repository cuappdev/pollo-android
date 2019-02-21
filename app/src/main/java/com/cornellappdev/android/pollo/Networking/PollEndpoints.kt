package com.cornellappdev.android.pollo.Networking

import com.cornellappdev.android.pollo.Models.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject

data class GetSortedPollsResponse(val date: String, val polls: List<PollsResponse>)
data class PollResult(val text: String, val count: Int)
data class PollsResponse(val id: Int, val text: String, val results: Map<String, PollResult>, val shared: Boolean, val type: String, val correctAnswer: String?)

fun Endpoint.Companion.getSortedPolls(id: String): Endpoint {
    val accessToken = User.currentSession.accessToken
    return Endpoint(path="/sessions/$id/polls", headers=mapOf("Authorization" to "Bearer $accessToken"), body=null, method=EndpointMethod.GET)
}