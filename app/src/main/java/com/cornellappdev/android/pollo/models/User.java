package com.cornellappdev.android.pollo.models;

public class User {

    public static User currentUser;
    public static UserSession currentSession;
    private String id;
    private String createdAt;
    private String updatedAt;
    private String name;
    private String netID;

    public User(String id, String createdAt, String updatedAt, String name, String netID) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.name = name;
        this.netID = netID;
    }

    public String getNetID() { return netID; }

    public enum Role {
        ADMIN,
        MEMBER
    }
}
