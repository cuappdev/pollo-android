package com.cornellappdev.android.pollo.networking

import com.cornellappdev.android.pollo.models.User
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

fun Endpoint.Companion.leaveGroup(id: Int): Endpoint {
    return Endpoint("/sessions/$id/members", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), method = EndpointMethod.DELETE)
}

fun Endpoint.Companion.startSession(code: String, name: String) : Endpoint {
    val codeJSON = JSONObject()
    try {
        codeJSON.put("code", code)
        codeJSON.put("name", name)
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), codeJSON.toString())

    return Endpoint("/start/session/", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), body = requestBody, method = EndpointMethod.POST)
}

fun Endpoint.Companion.startPoll(text: String, answerChoices: JSONObject, correctAnswer: String, type: String) : Endpoint {
    val codeJSON = JSONObject()
    try {
        codeJSON.put("text", text)
        codeJSON.put("answerChoices", answerChoices)
        codeJSON.put("state", "live")
        codeJSON.put("correctAnswer", correctAnswer)
        codeJSON.put("userAnswers", {})
        codeJSON.put("type", type)
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), codeJSON.toString())
    return Endpoint("/server/poll/start/", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), body = requestBody, method = EndpointMethod.GET)
}

fun Endpoint.Companion.generateCode() : Endpoint {
    return Endpoint("/generate/code/", headers = mapOf("Authorization" to "Bearer ${User.currentSession.accessToken}"), method = EndpointMethod.GET)
}