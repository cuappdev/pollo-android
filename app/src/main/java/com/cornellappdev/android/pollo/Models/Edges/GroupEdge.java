package com.cornellappdev.android.pollo.Models.Edges;

import com.cornellappdev.android.pollo.Models.ApiResponse;
import com.cornellappdev.android.pollo.Models.Group;
import com.cornellappdev.android.pollo.Models.Nodes.GroupNode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupEdge extends ApiResponse {

    private JSONObject pageInfo;
    private List<GroupNode> data;

    public JSONObject getPageInfo() {
        return pageInfo;
    }

    List<GroupNode> getData() {
        return data;
    }

    public List<Group> edgesToModels() {
        List<Group> models = new ArrayList<>();
        for (GroupNode g : data) {
            models.add(g.getData());
        }
        return models;
    }
}
