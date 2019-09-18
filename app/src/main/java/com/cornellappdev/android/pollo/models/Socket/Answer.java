package com.cornellappdev.android.pollo.models.Socket;

public class Answer {

    private String id;
    private String googleId;
    private String pollId;
    private String choice;
    private String text;

    public Answer(String id, String googleId, String pollId, String choice, String text) {
        this.id = id;
        this.googleId = googleId;
        this.pollId = pollId;
        this.choice = choice;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
