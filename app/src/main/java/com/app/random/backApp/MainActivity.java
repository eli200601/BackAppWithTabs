package com.app.random.backApp;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.app.random.backApp.Activitys.DBAccountInfoActivity;
import com.app.random.backApp.Activitys.SettingsActivity;
import com.app.random.backApp.Activitys.SplashScreenActivity;
import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.Dropbox.DropboxCallBackListener;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Services.DropboxUploadIntentService;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DropboxCallBackListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static final String TAG = "MainActivity";
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DropBoxManager dropBoxManager = null;
    private FilesUtils filesUtils;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String origin = savedInstanceState.getString("origin");

        setContentView(R.layout.activity_main);

        filesUtils = FilesUtils.getInstance(getApplicationContext());

        if (SharedPrefsUtils.getStringPreference(getApplicationContext(),Keys.PREF_ACTIVATE) == null) {
            Intent myIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
//            myIntent.putExtra("key", value); //Optional parameters
            this.startActivity(myIntent);
            this.finish();
            return;
        }

        dropBoxManager = DropBoxManager.getInstance(getApplicationContext());
        dropBoxManager.loginDropbox();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        String startedFrom = getIntent().getStringExtra("started_from");
        if (startedFrom != null) {
            setTabPaging(1);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        checkIfUploadDisrupted();
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    public boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    public void checkIfUploadDisrupted() {
        String jsonNotFinishList = SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.NOT_FINISH_UPLOAD_LIST);
        if (!isServiceRunning("DropboxUploadIntentService")) {
            if (jsonNotFinishList != null) {
                ArrayList<AppDataItem> appsList = filesUtils.getArrayFromJSONString(jsonNotFinishList);
                if (appsList.size() > 0) {
                    long totalUploadSize = filesUtils.getFileSizeFromListArray(appsList);
                    long cloudFreeSpace = SharedPrefsUtils.getLongPreference(getApplicationContext(), Keys.DROPBOX_FREE_SPACE_LONG, totalUploadSize);
                    if (( cloudFreeSpace - totalUploadSize) < 0) {
                        Log.e(TAG, "There is no free space on cloud...");
                        Snackbar.make(getCurrentFocus(), "Upload failed. There is no free space on cloud...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Keys.DIR_TO_UPLOAD_LIST, appsList);
                        Intent intent = new Intent(getApplicationContext(), DropboxUploadIntentService.class);
                        intent.putExtras(bundle);
                        Log.d(TAG, "Found that there is unfinished upload, starting again");
                        startService(intent);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dropBoxManager.removeDropboxListener(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Adding " + TAG + " TO Listener List");
        dropBoxManager.addDropboxListener(this, TAG);
        dropBoxManager.onResumeManager();
        invalidateOptionsMenu();
        // More Actions...
    }

    public void setTabPaging(int page) {
        mViewPager.setCurrentItem(2);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem logoutButton = menu.findItem(R.id.action_logout);
        MenuItem loginButton = menu.findItem(R.id.action_login);
        MenuItem user_name_text = menu.findItem(R.id.user_name_text);
        MenuItem accountInfo = menu.findItem(R.id.action_account_info);

        boolean isLogIn = dropBoxManager.isLoginToDropbox();

        logoutButton.setVisible(isLogIn);
        loginButton.setVisible(!isLogIn);
        accountInfo.setVisible(isLogIn);

        String user_name = SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.DROPBOX_DISPLAY_NAME);
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
                dropBoxManager.unlinkDropbox();
                invalidateOptionsMenu();
                break;
            }
            case R.id.action_login: {
                dropBoxManager.loginDropbox();
                Log.d(TAG, "Login Clicked");
                invalidateOptionsMenu();
                break;
            }
            case R.id.action_account_info: {
                startActivity(new Intent(this, DBAccountInfoActivity.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUserNameReceived() {
        invalidateOptionsMenu();
    }

    @Override
    public void onFinishUploadFiles() {

    }

    @Override
    public void onFinishGeneratingCloudList(ArrayList<AppDataItem> arrayList) {

    }

    @Override
    public void onFinishDeletingFiles() {

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

}
