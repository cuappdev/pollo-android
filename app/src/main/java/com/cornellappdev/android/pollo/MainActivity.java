package com.cornellappdev.android.pollo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cornellappdev.android.pollo.Models.GoogleCredentials;
import com.cornellappdev.android.pollo.Models.Group;
import com.cornellappdev.android.pollo.Models.User;
import com.cornellappdev.android.pollo.Models.UserSession;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private static final int LOGIN_REQ_CODE = 10031;

    UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = findViewById(R.id.editText_joinPoll);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editText.setCursorVisible(hasFocus);
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                    case EditorInfo.IME_ACTION_NEXT:
                    case EditorInfo.IME_ACTION_SEARCH:
                        editText.setCursorVisible(false);
                        return true;
                    default:
                        editText.setCursorVisible(true);
                        return false;
                }
            }
        });

        Button button = findViewById(R.id.join_poll_group);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(false);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Force sign out every app launch, for debugging purposes only
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();
        GoogleSignIn.getClient(this, gso).signOut();

        Intent signInIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(signInIntent, LOGIN_REQ_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // If account is null, attempt to sign in, if not, launch the normal activity. updateUI(account);
        if (account == null) {
            final Intent signInIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(signInIntent, LOGIN_REQ_CODE);
        } else {
            new RetrieveUserSessionTask().execute(account);
        }
    }

    class RetrieveUserSessionTask extends AsyncTask<GoogleSignInAccount, Void, UserSession> {

        protected UserSession doInBackground(GoogleSignInAccount... accounts) {
            final GoogleSignInAccount account = accounts[0];
            try {
                userSession = NetworkUtils.userAuthenticate(new GoogleCredentials(
                        account.getId(), account.getGivenName(),
                        account.getFamilyName(), account.getEmail()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return userSession;
        }

        protected void onPostExecute(UserSession userSession) {
            if (userSession == null) return;
            User.currentSession = userSession;
            // TODO(kevin): Remove the literal, but also find out a better place for this call.
            new RetrieveGroupsTask().execute("member");
        }
    }

    class RetrieveGroupsTask extends AsyncTask<String, Void, List<Group>> {

        @Override
        protected List<Group> doInBackground(String... strings) {
            List<Group> groups = new ArrayList<>();
            try {
                groups = NetworkUtils.getAllGroups(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return groups;
        }

        @Override
        protected void onPostExecute(List<Group> groups) {
            super.onPostExecute(groups);

            // TODO(kevin): We can remove this. I just used it for testing purposes
            String foundGroups = "Found " + groups.size() + " groups";
            Toast.makeText(MainActivity.this, foundGroups, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }
}
