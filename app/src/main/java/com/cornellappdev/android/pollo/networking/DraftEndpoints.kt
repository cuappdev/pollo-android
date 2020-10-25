package com.cornellappdev.android.pollo.networking

import com.cornellappdev.android.pollo.models.SavedPoll
import com.cornellappdev.android.pollo.models.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

fun Endpoint.Companion.getAllSavedPolls(): Endpoint {
    return Endpoint(
            "/drafts",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            method = EndpointMethod.GET
    )
}

fun Endpoint.Companion.createSavedPoll(savedPoll: SavedPoll): Endpoint {
    val savedPollJSON = JSONObject()
    savedPollJSON.put("text", savedPoll.text)
    val optionsJSON = JSONArray()
    for (option in savedPoll.options) optionsJSON.put(option)
    savedPollJSON.put("options", optionsJSON)

    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), savedPollJSON.toString())

    return Endpoint(
            "/drafts",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            body = requestBody,
            method = EndpointMethod.POST
    )
}

fun Endpoint.Companion.updateSavedPoll(savedPoll: SavedPoll): Endpoint {
    val savePollJSON = JSONObject()
    savePollJSON.put("text", savedPoll.text)
    val optionsJSON = JSONArray()
    for (option in savedPoll.options) optionsJSON.put(option)
    savePollJSON.put("options", optionsJSON)

    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), savePollJSON.toString())

    return Endpoint(
            "/drafts/${savedPoll.id}",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            body = requestBody,
            method = EndpointMethod.PUT
    )
}

fun Endpoint.Companion.deleteSavedPoll(savedPollId: String): Endpoint {
    return Endpoint(
            "/drafts/{$savedPollId}",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            method = EndpointMethod.DELETE
    )
}
