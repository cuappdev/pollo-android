package com.cornellappdev.android.pollo.Networking

import android.os.Parcelable
import com.cornellappdev.android.pollo.Models.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetSortedPollsResponse(val date: String, val polls: ArrayList<PollsResponse>) : Parcelable

@Parcelize
data class PollResult(val text: String, val count: Int) : Parcelable

@Parcelize
data class PollsResponse(val id: Int, val text: String, val results: Map<String, PollResult>, val shared: Boolean, val type: String, val correctAnswer: String?) : Parcelable

fun Endpoint.Companion.getSortedPolls(id: String): Endpoint {
    val accessToken = User.currentSession.accessToken
    return Endpoint(path = "/sessions/$id/polls", headers = mapOf("Authorization" to "Bearer $accessToken"), body = null, method = EndpointMethod.GET)
}