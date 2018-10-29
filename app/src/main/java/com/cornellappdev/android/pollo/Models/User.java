package com.cornellappdev.android.pollo.Models;

public class User {

    public enum Role {
        ADMIN,
        MEMBER
    }

    public static User currentUser;
    public static UserSession currentSession;

    private String id;
    private String name;
    private String netId;
    private String givenName;
    private String familyName;
    private String email;

    public User(String id, String name, String givenName, String familyName, String email) {
        this.id = id;
        this.name = name;
        this.netId = email.split("@")[0];
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
    }
}
