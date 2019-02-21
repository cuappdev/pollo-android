package com.cornellappdev.android.pollo.Networking

import com.cornellappdev.android.pollo.Models.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject

fun Endpoint.Companion.userAuthenticate(idToken: String): Endpoint {
    val codeJSON = JSONObject()
    try {
        codeJSON.put("idToken", idToken)
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), codeJSON.toString())
    return Endpoint(path="/auth/mobile", body=requestBody, method=EndpointMethod.POST)
}

fun Endpoint.Companion.userRefreshSession(refreshToken: String): Endpoint {
    return Endpoint(path="/auth/mobile", headers=mapOf("Authorization" to "Bearer $refreshToken"), method=EndpointMethod.POST)
}