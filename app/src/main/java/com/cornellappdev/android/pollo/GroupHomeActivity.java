package com.cornellappdev.android.pollo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cornellappdev.android.pollo.Models.Socket.CurrentState;
import com.cornellappdev.android.pollo.Models.Socket.Poll;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class GroupHomeActivity extends AppCompatActivity {

    private static final String SERVER_POLL_END = "server/poll/end";
    private static final String SERVER_POLL_START = "server/poll/start";
    private static final String SERVER_POLL_RESULTS = "server/poll/results";
    private static final String SERVER_POLL_TALLY = "server/poll/tally";
    private static final String SERVER_POLL_UPVOTE = "server/poll/upvote";

    private static final String USER_POLL_START = "user/poll/start";
    private static final String USER_POLL_END = "user/poll/end";
    private static final String USER_POLL_RESULTS = "user/poll/results";
    private static final String USER_POLL_RESULTS_LIVE = "user/poll/results/live";
    private static final String USER_COUNT = "user/count";

    private static final String ADMIN_POLL_START = "admin/poll/start";
    private static final String ADMIN_POLL_UPDATE_TALLY = "admin/poll/updateTally";
    private static final String ADMIN_POLL_ENDED = "admin/poll/ended";

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(getString(R.string.deployed_backend));
        } catch (URISyntaxException e) {}
    }

    private Poll poll;
    private Integer userCount;
    private CurrentState currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_home);
        mSocket.on(USER_POLL_START, onUserPollStart);
        mSocket.on(USER_POLL_END, onUserPollEnd);
        mSocket.on(USER_POLL_RESULTS, onUserPollResults);
        mSocket.on(USER_POLL_RESULTS_LIVE, onUserPollResultsLive);
        mSocket.on(USER_COUNT, onUserCount);
        mSocket.connect();
    }

    private Emitter.Listener onUserPollStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson dataJSON = new Gson();
                    poll = dataJSON.fromJson(data.toString(), Poll.class);
                }
            });
        }
    };

    private Emitter.Listener onUserPollEnd = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson dataJSON = new Gson();
                    poll = dataJSON.fromJson(data.toString(), Poll.class);
                }
            });
        }
    };

    private Emitter.Listener onUserPollResults = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson dataJSON = new Gson();
                    currentState = dataJSON.fromJson(data.toString(), CurrentState.class);
                }
            });
        }
    };

    private Emitter.Listener onUserPollResultsLive = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson dataJSON = new Gson();
                    currentState = dataJSON.fromJson(data.toString(), CurrentState.class);
                }
            });
        }
    };

    private Emitter.Listener onUserCount = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        userCount = data.getInt("count");
                    } catch (JSONException e) {}
                }
            });
        }
    };

}
