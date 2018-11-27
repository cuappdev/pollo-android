package com.cornellappdev.android.pollo.Models.Socket;

import org.json.JSONObject;

public class CurrentState {

    private Integer poll;
    private JSONObject results;
    private JSONObject answers;
    private JSONObject upvotes;

    public CurrentState(Integer poll, JSONObject results, JSONObject answers, JSONObject upvotes) {
        this.poll = poll;
        this.results = results;
        this.answers = answers;
        this.upvotes = upvotes;
    }

    public Integer getPoll() {
        return poll;
    }

    public void setPoll(Integer poll) {
        this.poll = poll;
    }

    public JSONObject getResults() {
        return results;
    }

    public void setResults(JSONObject results) {
        this.results = results;
    }

    public JSONObject getAnswers() {
        return answers;
    }

    public void setAnswers(JSONObject answers) {
        this.answers = answers;
    }

    public JSONObject getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(JSONObject upvotes) {
        this.upvotes = upvotes;
    }
}
