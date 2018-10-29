package com.cornellappdev.android.pollo;

import com.cornellappdev.android.pollo.Models.Edges.GroupEdge;
import com.cornellappdev.android.pollo.Models.GoogleCredentials;
import com.cornellappdev.android.pollo.Models.Group;
import com.cornellappdev.android.pollo.Models.Nodes.UserSessionNode;
import com.cornellappdev.android.pollo.Models.User;
import com.cornellappdev.android.pollo.Models.UserSession;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class NetworkUtils {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String DEPLOYED_BACKEND = "http://pollo-backend.cornellappdev.com";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";

    private static final String API_V2 = "/api/v2";
    private static final String MOBILE_AUTH_ROUTE = "/auth/mobile";
    private static final String GET_GROUPS_ROUTE = "/sessions/all";


    static UserSession userAuthenticate(GoogleCredentials googleCredentials) throws IOException {
        final String googleCredentialsJSON = new Gson().toJson(googleCredentials, GoogleCredentials.class);
        final RequestBody requestBody = RequestBody.create(JSON, googleCredentialsJSON);
        final String endpoint = DEPLOYED_BACKEND + API_V2 + MOBILE_AUTH_ROUTE;
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();

            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                final UserSessionNode userSessionNode = responseBodyJSON.fromJson(responseBodyString, UserSessionNode.class);
                return userSessionNode.getData();
            }
        }

        return null;
    }

    static List<Group> getAllGroups(final String role) throws IOException {
        final String endpoint = DEPLOYED_BACKEND + API_V2 + GET_GROUPS_ROUTE + "/" + role;
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(endpoint)
                .addHeader(AUTHORIZATION, BEARER + " " + User.currentSession.getAccessToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                final GroupEdge groupEdge = responseBodyJSON.fromJson(responseBodyString, GroupEdge.class);
                return groupEdge.edgesToModels();
            }
        }

        return null;
    }

}