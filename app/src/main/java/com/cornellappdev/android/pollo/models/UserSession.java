package com.cornellappdev.android.pollo.models;

public class UserSession {
    private String uuid;
    private String sessionToken;
    private String expiresAt;
    private String updateToken;
    private boolean isActive;
    private User user;

    public UserSession(String accessToken, String refreshToken, String sessionExpiration, boolean isActive) {
        this.sessionToken = accessToken;
        this.updateToken = refreshToken;
        this.expiresAt = sessionExpiration;
        this.isActive = isActive;
    }

    public String getAccessToken() {
        return sessionToken;
    }

    public String getRefreshToken() {
        return updateToken;
    }

    public String getSessionExpiration() {
        return expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

}
