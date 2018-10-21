package com.cornellappdev.android.pollo.Models;

public class UserSession {

    private String accessToken;
    private String refreshToken;
    private Long sessionExpiration;
    private boolean isActive;

    public UserSession(String accessToken, String refreshToken, Long sessionExpiration, boolean isActive) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sessionExpiration = sessionExpiration;
        this.isActive = isActive;
    }
}
