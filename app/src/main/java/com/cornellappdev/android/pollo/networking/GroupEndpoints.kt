package com.cornellappdev.android.pollo.networking

import com.cornellappdev.android.pollo.models.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun Endpoint.Companion.joinGroupWithCode(code: String): Endpoint {
    val codeJSON = JSONObject()
    codeJSON.put("code", code)
    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), codeJSON.toString())
    return Endpoint(path = "/join/session", headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken), body = requestBody, method = EndpointMethod.POST)
}

fun Endpoint.Companion.getAllGroups(role: String): Endpoint {
    return Endpoint("/sessions/all/$role", headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken), method = EndpointMethod.GET)
}

fun Endpoint.Companion.leaveGroup(id: String): Endpoint {
    return Endpoint("/sessions/$id/members", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), method = EndpointMethod.DELETE)
}

fun Endpoint.Companion.deleteGroup(id: String): Endpoint {
    return Endpoint("/sessions/$id", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), method = EndpointMethod.DELETE)
}

fun Endpoint.Companion.renameGroup(id: Int, name: String): Endpoint {
    val nameJSON = JSONObject()
    nameJSON.put("name", name)
    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), nameJSON.toString())
    return Endpoint("/sessions/$id", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), body = requestBody, method = EndpointMethod.PUT)
}

fun Endpoint.Companion.startSession(code: String, name: String) : Endpoint {
    val codeJSON = JSONObject()
    codeJSON.put("code", code)
    codeJSON.put("name", name)
    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), codeJSON.toString())

    return Endpoint("/start/session/", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), body = requestBody, method = EndpointMethod.POST)
}

fun Endpoint.Companion.generateCode() : Endpoint {
    return Endpoint("/generate/code/", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), method = EndpointMethod.GET)
}