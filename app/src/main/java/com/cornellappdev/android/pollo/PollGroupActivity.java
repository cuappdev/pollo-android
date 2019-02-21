package com.cornellappdev.android.pollo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cornellappdev.android.pollo.Models.GoogleCredentials;
import com.cornellappdev.android.pollo.Models.Socket.CurrentState;
import com.cornellappdev.android.pollo.Models.Socket.Poll;
import com.cornellappdev.android.pollo.Models.User;
import com.cornellappdev.android.pollo.Models.UserSession;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

public class PollGroupActivity extends AppCompatActivity implements PollRecyclerView.ItemClickListener {

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

    // TODO(Kevin): write proper documentation for components
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // TODO(Kevin): write proper documentation for components
    private ViewPager mViewPager;

    private Poll poll;
    private Integer userCount;
    private CurrentState currentState;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mSocket = IO.socket(BuildConfig.BACKEND_URI);
        } catch (URISyntaxException e) {}
        setContentView(R.layout.activity_poll_group);
        mSocket.on(USER_POLL_START, onUserPollStart);
        mSocket.on(USER_POLL_END, onUserPollEnd);
        mSocket.on(USER_POLL_RESULTS, onUserPollResults);
        mSocket.on(USER_POLL_RESULTS_LIVE, onUserPollResultsLive);
        mSocket.on(USER_COUNT, onUserCount);
        mSocket.connect();
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    // TODO(Kevin): write relevant documentation for components
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PollFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
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
