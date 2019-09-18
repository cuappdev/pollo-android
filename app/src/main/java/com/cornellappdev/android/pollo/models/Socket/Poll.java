package com.cornellappdev.android.pollo.models.Socket;

public class Poll {

    private String id;
    private String text;
    private String type;
    private String[] options;
    private boolean shared;
    private String correctAnswer;

    public Poll(String id, String text, String type, String[] options, boolean shared, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.shared = shared;
        this.correctAnswer = correctAnswer;
    }

    public Poll(String id, String text, String type, boolean shared, String correctAnswer) {
        this(id, text, type, null, shared, correctAnswer);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
