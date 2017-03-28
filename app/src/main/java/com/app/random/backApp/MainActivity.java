package com.app.random.backApp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static final String TAG = "Main Activity";
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DropboxAPI<AndroidAuthSession> mDBApi;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginDropBox();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    public DropboxAPI<AndroidAuthSession> createSession() {
        AppKeyPair appKeys = new AppKeyPair(Keys.DROPBOX_APP_KEY, Keys.DROPBOX_APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        return new DropboxAPI<AndroidAuthSession>(session);
    }

    public void loginDropBox() {
        mDBApi = createSession();
        String accessToken = SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.DROPBOX_ACCESS_TOKEN);
        if (accessToken == null) {
            Log.d(TAG, "New User as arrived");
            mDBApi.getSession().startOAuth2Authentication(this);
        } else {
            mDBApi.getSession().setOAuth2AccessToken(accessToken);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.d(TAG, "Access Token: " + accessToken);

                SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.DROPBOX_ACCESS_TOKEN, accessToken);
                new LoadDataDropbox().execute();

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }

        invalidateOptionsMenu();
        // More Actions...
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem logoutButton = menu.findItem(R.id.action_logout);
        MenuItem loginButton = menu.findItem(R.id.action_login);
        MenuItem user_name_text = menu.findItem((R.id.user_name_text));
        boolean isLogIn = mDBApi.getSession().isLinked();

        logoutButton.setVisible(isLogIn);
        loginButton.setVisible(!isLogIn);

        String user_name = SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.DROPBOX_USER_NAME);
        if (user_name != null && isLogIn) {
            user_name_text.setVisible(true);
            user_name_text.setTitle(user_name);
        } else {
            user_name_text.setVisible(false);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings: {

                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case R.id.action_logout: {
                Log.d(TAG, "Logout Clicked");

                mDBApi.getSession().unlink();
                SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.DROPBOX_ACCESS_TOKEN, null);
                SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.DROPBOX_USER_NAME, null);
                invalidateOptionsMenu();
                break;
            }
            case R.id.action_login: {
                loginDropBox();
                Log.d(TAG, "Login Clicked");
                invalidateOptionsMenu();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }
    private class LoadDataDropbox extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String name = null;

            try {

                name = mDBApi.accountInfo().displayName;
                Log.d(TAG, "Trying to fetch name = " + name);
                Log.d(TAG, name);
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return name;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String arr[] = result.split(" ", 2);
                String username = "hi, " + arr[0];
                Log.d(TAG, "Finished background, name is: " + username);
                SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.DROPBOX_USER_NAME, username);
                invalidateOptionsMenu();
            }
            super.onPostExecute(result);
        }
    }
}
