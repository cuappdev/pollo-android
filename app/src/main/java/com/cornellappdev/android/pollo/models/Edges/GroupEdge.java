package com.cornellappdev.android.pollo.models.Edges;

import com.cornellappdev.android.pollo.models.Group;
import com.cornellappdev.android.pollo.models.Nodes.GroupNode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupEdge {

    private JSONObject pageInfo;
    private List<GroupNode> data;
    private Boolean success;

    public JSONObject getPageInfo() {
        return pageInfo;
    }

    List<GroupNode> getData() {
        return data;
    }

    public List<Group> edgesToModels() {
        List<Group> models = new ArrayList<>();
        for (GroupNode g : data) {
            models.add(g.getNode());
        }
        return models;
    }
}
