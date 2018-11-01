package com.cornellappdev.android.pollo;

import com.cornellappdev.android.pollo.Models.Edges.GroupEdge;
import com.cornellappdev.android.pollo.Models.GoogleCredentials;
import com.cornellappdev.android.pollo.Models.Group;
import com.cornellappdev.android.pollo.Models.Nodes.GeneratedCodeNode;
import com.cornellappdev.android.pollo.Models.Nodes.GroupNode;
import com.cornellappdev.android.pollo.Models.Nodes.UserSessionNode;
import com.cornellappdev.android.pollo.Models.User;
import com.cornellappdev.android.pollo.Models.UserSession;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class NetworkUtils {

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";

    private static final String API_V2 = "/api/v2";
    private static final String GENERATE_CODE_ROUTE = "/generate/code";
    private static final String MOBILE_AUTH_ROUTE = "/auth/mobile";
    private static final String JOIN_GROUP_ROUTE = "/join/session";
    private static final String GET_GROUP_ROUTE = "/sessions/";
    private static final String GET_GROUPS_ROUTE = "/sessions/all";

    private static final String MEMBER_ENDPOINT = "/member";
    private static final String MEMBERS_ENDPOINT = MEMBER_ENDPOINT + "s";
    private static final String ADMIN_ENDPOINT = "/admin";
    private static final String ADMINS_ENDPOINT = ADMIN_ENDPOINT + "s";

    static UserSession userAuthenticate(final GoogleCredentials googleCredentials) throws IOException {
        final String googleCredentialsJSON = new Gson().toJson(googleCredentials, GoogleCredentials.class);
        final RequestBody requestBody = RequestBody.create(JSON, googleCredentialsJSON);
        final String endpoint = R.string.deployed_backed + API_V2 + MOBILE_AUTH_ROUTE;
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

    static String generateCode() throws IOException {
        final String endpoint = R.string.deployed_backed + API_V2 + GENERATE_CODE_ROUTE;
        final Request request = new Request.Builder()
                .url(endpoint)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();

            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                final GeneratedCodeNode generatedCodeNode = responseBodyJSON.fromJson(responseBodyString, GeneratedCodeNode.class);
                try {
                    return generatedCodeNode.getData().getString("code");
                } catch (JSONException e) {
                    return null;
                }
            }
        }

        return null;

    }

    static Group joinGroup(final String code) throws IOException {
        final JSONObject codeJSON = new JSONObject();
        try {
            codeJSON.put("code", code);
        } catch (JSONException e) {
            return null;
        }
        final String endpoint = R.string.deployed_backed + API_V2 + JOIN_GROUP_ROUTE;
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
                final GroupNode groupNode = responseBodyJSON.fromJson(responseBodyString, GroupNode.class);
                return groupNode.getData();
            }
        }

        return null;
    }

    static Group getGroup(final String id) throws IOException {
        final String endpoint = R.string.deployed_backed + API_V2 + GET_GROUP_ROUTE + id;
        final Request request = new Request.Builder()
                .url(endpoint)
                .addHeader(AUTHORIZATION, BEARER + " " + User.currentSession.getAccessToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            if (responseBody != null) {
                final String responseBodyString = responseBody.string();
                final Gson responseBodyJSON = new Gson();
                final GroupNode groupNode = responseBodyJSON.fromJson(responseBodyString, GroupNode.class);
                return groupNode.getData();
            }
        }

        return null;
    }

    static List<Group> getAllGroupsAsMember() throws IOException {
        return getAllGroups(MEMBER_ENDPOINT);
    }

    static List<Group> getAllGroupsAsAdmin() throws IOException {
        return getAllGroups(ADMIN_ENDPOINT);
    }

    private static List<Group> getAllGroups(final String roleEndpoint) throws IOException {
        final String endpoint = R.string.deployed_backed + API_V2 + GET_GROUPS_ROUTE + roleEndpoint;
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

    static boolean leaveGroup(final String id) throws IOException {
        final String endpoint = R.string.deployed_backed + API_V2 + GET_GROUP_ROUTE + id + MEMBERS_ENDPOINT;
        final Request request = new Request.Builder()
                .url(endpoint)
                .addHeader(AUTHORIZATION, BEARER + " " + User.currentSession.getAccessToken())
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            final ResponseBody responseBody = response.body();
            return response.isSuccessful();
        }
    }

}