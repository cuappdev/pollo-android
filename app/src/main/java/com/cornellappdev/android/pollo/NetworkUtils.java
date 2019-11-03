package com.cornellappdev.android.pollo;

import android.content.Context;
import android.util.Log;
import com.cornellappdev.android.pollo.models.Edges.GroupEdge;
import com.cornellappdev.android.pollo.models.GoogleCredentials;
import com.cornellappdev.android.pollo.models.Group;
import com.cornellappdev.android.pollo.models.Nodes.GroupNodeResponse;
import com.cornellappdev.android.pollo.models.Nodes.UserSessionNode;
import com.cornellappdev.android.pollo.models.User;
import com.cornellappdev.android.pollo.models.UserSession;
import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

final class NetworkUtils {

    static final String AUTHORIZATION = "Authorization";
    static final String BEARER = "Bearer";
    static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    static final String JOIN_GROUP_ROUTE = "/join/session";
    private static final OkHttpClient client = new OkHttpClient();
    private static final String GENERATE_CODE_ROUTE = "/generate/code";
    private static final String MOBILE_AUTH_ROUTE = "/auth/mobile";
    private static final String GET_GROUP_ROUTE = "/sessions/";
    private static final String GET_GROUPS_ROUTE = "/sessions/all";

    private static final String ADMIN_ENDPOINT = "/admin";
    private static final String ADMINS_ENDPOINT = ADMIN_ENDPOINT + "s";
    private static final String MEMBER_ENDPOINT = "/member";
    private static final String MEMBERS_ENDPOINT = MEMBER_ENDPOINT + "s";

    static UserSession userAuthenticate(final Context context, final GoogleCredentials googleCredentials) throws IOException {
        final String googleCredentialsJSON = new Gson().toJson(googleCredentials, GoogleCredentials.class);
        final RequestBody requestBody = RequestBody.create(JSON, googleCredentialsJSON);
        final String endpoint = BuildConfig.BACKEND_URI + MOBILE_AUTH_ROUTE;
        final Request request = new Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();

            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                Log.d("Response from userAuth", responseBodyString);
                final UserSessionNode userSessionNode = responseBodyJSON.fromJson(responseBodyString, UserSessionNode.class);
                return userSessionNode.getData();
            }
        }

        return null;
    }

    static Group joinGroup(final Context context, final String code) throws IOException {
        final JSONObject codeJSON = new JSONObject();
        try {
            codeJSON.put("code", code);
        } catch (JSONException e) {
            return null;
        }
        final String endpoint = BuildConfig.BACKEND_URI + JOIN_GROUP_ROUTE;
        final RequestBody requestBody = RequestBody.create(JSON, codeJSON.toString());
        final Request request = new Request.Builder()
                .url(endpoint)
                .addHeader(AUTHORIZATION, BEARER + " " + User.currentSession.getAccessToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                final GroupNodeResponse groupNodeResponse = responseBodyJSON.fromJson(responseBodyString, GroupNodeResponse.class);
                return groupNodeResponse.getData().getNode();
            }
        }

        return null;
    }

    static Group getGroup(final Context context, final String id) throws IOException {
        final String endpoint = BuildConfig.BACKEND_URI + GET_GROUP_ROUTE + id;
        final Request request = new Request.Builder()
                .url(endpoint)
                .addHeader(AUTHORIZATION, BEARER + " " + User.currentSession.getAccessToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                final GroupNodeResponse groupNodeResponse = responseBodyJSON.fromJson(responseBodyString, GroupNodeResponse.class);
                return groupNodeResponse.getData().getNode();
            }
        }

        return null;
    }

    static List<Group> getAllGroupsAsMember(final Context context) throws IOException {
        return getAllGroups(context, MEMBER_ENDPOINT);
    }

    static List<Group> getAllGroupsAsAdmin(final Context context) throws IOException {
        return getAllGroups(context, ADMIN_ENDPOINT);
    }

    private static List<Group> getAllGroups(final Context context, final String roleEndpoint) throws IOException {
        final String endpoint = BuildConfig.BACKEND_URI + GET_GROUPS_ROUTE + roleEndpoint;
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

    static boolean leaveGroup(final Context context, final String id) throws IOException {
        final String endpoint = BuildConfig.BACKEND_URI + GET_GROUP_ROUTE + id + MEMBERS_ENDPOINT;
        final Request request = new Request.Builder()
                .url(endpoint)
                .addHeader(AUTHORIZATION, BEARER + " " + User.currentSession.getAccessToken())
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

}