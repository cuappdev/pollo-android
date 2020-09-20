package com.cornellappdev.android.pollo.networking

import com.cornellappdev.android.pollo.BuildConfig
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

enum class EndpointMethod {
    GET, POST, DELETE, PUT
}

class Endpoint(private val path: String, private val headers: Map<String, String> = mapOf(), private val body: RequestBody? = null, private val method: EndpointMethod) {

//    private val host = "https://" + BuildConfig.BACKEND_URI + "/api/v2"
    private val host = "https://" + BuildConfig.TEMP_BACKEND_URI + "/api/v2"

    companion object

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
                        .get()
                        .build()
            }

            EndpointMethod.DELETE -> {
                return Request.Builder()
                        .url(endpoint)
                        .headers(headers)
                        .delete()
                        .build()
            }

            EndpointMethod.PUT -> {
                return Request.Builder()
                        .url(endpoint)
                        .headers(headers)
                        .put(body ?: RequestBody.create(MediaType.get("application/json; charset=utf-8"), ""))
                        .build()
            }

            else -> {
                throw IllegalArgumentException("NOT IMPLEMENTED")
            }
        }
    }

}
