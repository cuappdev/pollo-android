package com.cornellappdev.android.pollo;

import android.net.Uri;
import android.util.Log;

import com.cornellappdev.android.pollo.Models.GoogleCredentials;
import com.cornellappdev.android.pollo.Models.Nodes.UserSessionNode;
import com.cornellappdev.android.pollo.Models.UserSession;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class NetworkUtils {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String MOBILE_AUTH_ROUTE = "/api/v2/auth/mobile";
    static final String DEPLOYED_BACKEND = "http://pollo-backend.cornellappdev.com";

    public static UserSession userAuthenticate(GoogleCredentials googleCredentials) throws IOException {

        final String googleCredentialsJSON = new Gson().toJson(googleCredentials, GoogleCredentials.class);
        final RequestBody requestBody = RequestBody.create(JSON, googleCredentialsJSON);
        final String endpoint = DEPLOYED_BACKEND + MOBILE_AUTH_ROUTE;
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                Gson responseBodyJSON = new Gson();
                UserSessionNode userSessionNode = responseBodyJSON.fromJson(responseBody.string(), UserSessionNode.class);
                return userSessionNode.getNode();
            }
        }

        return null;
    }

}