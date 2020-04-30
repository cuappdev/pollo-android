package com.cornellappdev.android.pollo.networking

import com.cornellappdev.android.pollo.models.Draft
import com.cornellappdev.android.pollo.models.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

fun Endpoint.Companion.getAllDrafts(): Endpoint {
    return Endpoint(
            "/drafts",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            method = EndpointMethod.GET
    )
}

fun Endpoint.Companion.createDraft(draft: Draft): Endpoint {
    val draftJSON = JSONObject()
    draftJSON.put("text", draft.text)
    val optionsJSON = JSONArray()
    for (option in draft.options) optionsJSON.put(option)
    draftJSON.put("options", optionsJSON)

    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), draftJSON.toString())

    return Endpoint(
            "/drafts",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            body = requestBody,
            method = EndpointMethod.POST
    )
}

fun Endpoint.Companion.updateDraft(draft: Draft): Endpoint {
    val draftJSON = JSONObject()
    draftJSON.put("text", draft.text)
    val optionsJSON = JSONArray()
    for (option in draft.options) optionsJSON.put(option)
    draftJSON.put("options", optionsJSON)

    val requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), draftJSON.toString())

    return Endpoint(
            "/drafts/${draft.id}",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            body = requestBody,
            method = EndpointMethod.PUT
    )
}

fun Endpoint.Companion.deleteDraft(draftId: String): Endpoint {
    return Endpoint(
            "/drafts/{$draftId}",
            headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken),
            method = EndpointMethod.DELETE
    )
}
