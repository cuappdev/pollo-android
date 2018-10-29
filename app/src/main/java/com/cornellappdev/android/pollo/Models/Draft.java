package com.cornellappdev.android.pollo.Models;

import java.util.List;

public class Draft {

    private String id;
    private String text;
    private List<String> options;

    public Draft(String id, String text, List<String> options) {
        this.id = id;
        this.text = text;
        this.options = options;
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

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

}
