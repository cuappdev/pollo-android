package com.cornellappdev.android.pollo.models;

public class UserSession {

    private String accessToken;
    private String refreshToken;
    private String sessionExpiration;
    private boolean isActive;

    public UserSession(String accessToken, String refreshToken, String sessionExpiration, boolean isActive) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sessionExpiration = sessionExpiration;
        this.isActive = isActive;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getSessionExpiration() {
        return sessionExpiration;
    }

    public boolean isActive() {
        return isActive;
    }

}