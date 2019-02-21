package com.cornellappdev.android.pollo.Networking

import android.util.Log
import com.cornellappdev.android.pollo.BuildConfig
import com.cornellappdev.android.pollo.Models.Nodes.GroupNodeResponse
import com.cornellappdev.android.pollo.Models.User
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.Request
import java.io.IOException

enum class EndpointMethod {
    GET, POST, DELETE, PUT
}

class Endpoint(private val path: String, private val headers: Map<String, String> = mapOf(), private val body: RequestBody? = null, private val method: EndpointMethod) {

    private val host = "https://" + BuildConfig.BACKEND_URI + "/api/v2"

    companion object {}

    fun okHttpRequest(): Request {
        val endpoint = host + path
        val headers = Headers.of(headers)

        when (method) {
            EndpointMethod.POST -> {
                return Request.Builder()
                        .url(endpoint)
                        .post(body ?: RequestBody.create(MediaType.get("application/json; charset=utf-8"), ""))
                        .headers(headers)
                        .build()
            }
            EndpointMethod.GET -> {
                return Request.Builder()
                        .url(endpoint)
                        .headers(headers)
                        .build()
            }
            else -> {
               throw IllegalArgumentException("NOT IMPLEMENTED")
            }
        }
    }

}
