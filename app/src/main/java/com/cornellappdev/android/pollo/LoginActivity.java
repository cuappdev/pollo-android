package com.cornellappdev.android.pollo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    GoogleSignInClient mGoogleSignInClient;
    final int RC_SIGN_IN = 10032;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();

        SignInButton loginButton = (SignInButton)findViewById(R.id.sign_in_button);
        loginButton.setOnClickListener(this);
        setGoogleSignInButtonText(loginButton,"Continue with Google");

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //If account is null, attempt to sign in, if not, launch the normal activity. updateUI(account);
        if(account != null){
            Intent data = new Intent();
            data.putExtra("accountData_name", account.getDisplayName());
            data.putExtra("accountData_givenName", account.getGivenName());
            data.putExtra("accountData_familyName", account.getFamilyName());
            data.putExtra("accountData_email", account.getEmail());
            data.putExtra("accountData_id", account.getId());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    protected void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d("Testing","Logged In");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            Intent data = new Intent();
            data.putExtra("accountData_name", account.getDisplayName());
            data.putExtra("accountData_givenName", account.getGivenName());
            data.putExtra("accountData_familyName", account.getFamilyName());
            data.putExtra("accountData_email", account.getEmail());
            data.putExtra("accountData_id", account.getId());
            setResult(RESULT_OK, data);
            finish();
            Log.d("Testing","Logged In: Done");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
            Log.d("Testing","Logged In Failed: "  +e.getStatusCode());
        }
    }
}
