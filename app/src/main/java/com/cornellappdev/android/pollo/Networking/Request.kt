package com.cornellappdev.android.pollo.Networking

import android.util.Log
import com.cornellappdev.android.pollo.Models.ApiResponse
import com.cornellappdev.android.pollo.Models.Nodes.UserSessionNode
import com.cornellappdev.android.pollo.Models.User
import com.cornellappdev.android.pollo.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object Request {
    val httpClient = OkHttpClient()

    suspend inline fun<reified T> makeRequest(request: okhttp3.Request, typeToken: Type): T {
        val response = httpClient.newCall(request).await()
        val responseBody = response.body()
        val responseBodyString = responseBody?.string() ?: ""
        Log.d("NETWORK RESPONSE", responseBodyString)

        //Invalid Session Token
        if(response.code() == 401) {
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