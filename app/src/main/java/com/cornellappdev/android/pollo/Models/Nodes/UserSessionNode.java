package com.cornellappdev.android.pollo.Models.Nodes;

import com.cornellappdev.android.pollo.Models.ApiResponse;
import com.cornellappdev.android.pollo.Models.UserSession;

public class UserSessionNode extends ApiResponse {

    private UserSession data;

    public UserSession getData() {
        return data;
    }

    @Override
    public String toString() {
        return "accessToken: " + getData().getAccessToken() + "\n"
                + "isActive: " + getData().isActive();
    }
}
