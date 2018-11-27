package com.cornellappdev.android.pollo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class GroupHomeActivity extends AppCompatActivity {

    private final String SERVER_POLL_END = "server/poll/end";
    private final String SERVER_POLL_START = "server/poll/start";
    private final String SERVER_POLL_RESULTS = "server/poll/results";
    private final String SERVER_POLL_TALLY = "server/poll/tally";
    private final String SERVER_POLL_UPVOTE = "server/poll/upvote";

    private final String USER_POLL_START = "user/poll/start";
    private final String USER_POLL_END = "user/poll/end";
    private final String USER_POLL_RESULTS = "user/poll/results";
    private final String USER_POLL_RESULTS_LIVE = "user/poll/results/live";
    private final String USER_COUNT = "user/count";

    private final String ADMIN_POLL_START = "admin/poll/start";
    private final String ADMIN_POLL_UPDATE_TALLY = "admin/poll/updateTally";
    private final String ADMIN_POLL_ENDED = "admin/poll/ended";

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(getString(R.string.deployed_backend));
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_home);
        mSocket.connect();
    }
}
