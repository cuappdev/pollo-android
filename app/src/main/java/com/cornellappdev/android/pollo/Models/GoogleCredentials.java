package com.cornellappdev.android.pollo.Models;

public class GoogleCredentials {

    private String userId;
    private String givenName;
    private String familyName;
    private String email;

    public GoogleCredentials(String userId, String givenName, String familyName, String email) {
        this.userId = userId;
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }

}
