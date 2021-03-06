package com.cornellappdev.android.pollo.networking

import android.os.Parcelable
import com.cornellappdev.android.pollo.models.Poll
import com.cornellappdev.android.pollo.models.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetSortedPollsResponse(val date: String, var polls: ArrayList<Poll>, var isLive: Boolean = false) : Parcelable

@Parcelize
data class PollResult(val text: String, val count: Int) : Parcelable

@Parcelize
data class PollsResponse(val id: String, val text: String, val results: Map<String, PollResult>, val shared: Boolean, val type: String, val correctAnswer: String) : Parcelable

fun Endpoint.Companion.getSortedPolls(id: String): Endpoint {
    val accessToken = User.currentSession.accessToken
    return Endpoint(path = "/sessions/$id/polls", headers = mapOf("Authorization" to "Bearer $accessToken"), body = null, method = EndpointMethod.GET)
}