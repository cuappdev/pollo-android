package com.cornellappdev.android.pollo.networking

import com.cornellappdev.android.pollo.models.User

fun Endpoint.Companion.getUserInfo(): Endpoint {
    return Endpoint(path = "/users", headers = mapOf("Authorization" to "Bearer " + User.currentSession.accessToken), method = EndpointMethod.GET)
}

fun Endpoint.Companion.userRefreshSession(refreshToken: String): Endpoint {
    return Endpoint(path = "/auth/refresh", headers = mapOf("Authorization" to "Bearer $refreshToken"), method = EndpointMethod.POST)
}

fun Endpoint.Companion.dummyUserLogin(): Endpoint {
    return Endpoint(path = "/auth/fake/android/616647266964", method = EndpointMethod.POST)
}