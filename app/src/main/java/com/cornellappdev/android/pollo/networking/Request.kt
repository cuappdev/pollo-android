package com.cornellappdev.android.pollo.networking

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object Request {
    val httpClient = OkHttpClient()

    suspend inline fun <reified T> makeRequest(request: okhttp3.Request, typeToken: Type): T? {
        val response = httpClient.newCall(request).await()
        val responseBody = response.body()
        val responseBodyString = responseBody?.string() ?: ""
        Log.d("NETWORK RESPONSE", responseBodyString)

        // Invalid Session Token, should automatically refresh and then retry the request
        if (response.code() == 401) {
        }
        val responseBodyJSON = Gson()

        return responseBodyJSON.fromJson<T>(responseBodyString, typeToken)
    }
}

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return Result of request or throw exception
 */
suspend fun Call.await(recordStackTrace: Boolean = true): Response {
    val recordStackTrace = if (recordStackTrace) IOException("Exception occured while awaiting Call.") else null
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (recordStackTrace != null) {
                    recordStackTrace.initCause(e)
                    continuation.resumeWithException(recordStackTrace)
                } else {
                    continuation.resumeWithException(e)
                }
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
    }
}