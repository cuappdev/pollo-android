package com.cornellappdev.android.pollo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class GroupHomeActivity extends AppCompatActivity {

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
