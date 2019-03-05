package com.cornellappdev.android.pollo.Networking

import com.cornellappdev.android.pollo.Models.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject

fun Endpoint.Companion.joinGroupWithCode(code: String): Endpoint {
    val codeJSON = JSONObject()
    try {
        codeJSON.put("code", code)
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), codeJSON.toString())
    return Endpoint(path = "/join/session", headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken), body = requestBody, method = EndpointMethod.POST)
}

fun Endpoint.Companion.getAllGroups(role: String): Endpoint {
    return Endpoint("/sessions/all/$role", headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken), method = EndpointMethod.GET)
}

fun Endpoint.Companion.leaveGroup(id: String): Endpoint {
    return Endpoint("/sessions/$id/members", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), method = EndpointMethod.DELETE)
}

