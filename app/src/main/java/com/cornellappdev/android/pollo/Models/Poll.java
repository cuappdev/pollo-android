package com.cornellappdev.android.pollo.models;

import org.json.JSONObject;

public class Poll {

    private Long id;
    private String text;
    private JSONObject results;
    private boolean shared;
    private String type;
    private String answer;

    public Poll(Long id, String text, JSONObject results, boolean shared, String type, String answer) {
        this.id = id;
        this.text = text;
        this.results = results;
        this.shared = shared;
        this.type = type;
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public JSONObject getResults() {
        return results;
    }

    public void setResults(JSONObject results) {
        this.results = results;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
