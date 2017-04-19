package com.app.random.backApp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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
import com.app.random.backApp.Utils.ConnectionDetector;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

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
    private Toolbar toolbar;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        String origin = savedInstanceState.getString("origin");

        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.container);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setSupportActionBar(toolbar);

        filesUtils = FilesUtils.getInstance(getApplicationContext());
        dropBoxManager = DropBoxManager.getInstance(getApplicationContext());
        if (SharedPrefsUtils.getStringPreference(getApplicationContext(),Keys.PREF_ACTIVATE) == null) {
            Intent myIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
//            myIntent.putExtra("key", value); //Optional parameters
            this.startActivity(myIntent);
            this.finish();
            return;
        }
        else {
            Log.d(TAG, "Checking if there is permission");
            isStoragePermissionGranted();
        }



//        setSectionsPagerAdapter();



        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        // Set up the ViewPager with the sections adapter.



        String startedFrom = getIntent().getStringExtra(Keys.STARTED_FROM);
        if (startedFrom != null) {
            switch (startedFrom) {
                case Keys.DROPBOX_UPLOAD_INTENT_SERVICE: {
                    setTabPaging(1);
                    break;
                }
                case Keys.DROPBOX_DOWNLOAD_INTENT_SERVICE: {
                    setTabPaging(1);
                    break;
                }
            }

        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    public void setViewPager(){
        setSupportActionBar(toolbar);


        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            tabLayout.setupWithViewPager(mViewPager);
        }

//        tabLayout.setupWithViewPager(mViewPager);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) {
                Log.v(TAG,"Android N Permission is granted");
                setSectionsPagerAdapter();
                setViewPager();
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                String[] permissions = new String[]{
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);

                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Not N device, Permission is granted");
            setSectionsPagerAdapter();
            setViewPager();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = true;
        int index = 0;
        Log.v(TAG,"onRequestPermissionsResult start grantResults.size = " + String.valueOf(grantResults.length));
        if (grantResults.length > 0) {
            for (int result: grantResults) {
                if(result == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG,"Permission: "+permissions[index]+ " was " + grantResults[0]);
                }
                else {
                        Log.d(TAG, "Permission Denied, You cannot use local drive .");
                    flag = false;
                }
                index++;
            }
        }
        if (flag) {
            setSectionsPagerAdapter();
            setViewPager();
        }
        else {
            isStoragePermissionGranted();
        }


    }


    public void setSectionsPagerAdapter() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
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
                if (new ConnectionDetector(getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection to internet");
                    Log.d(TAG, "Logout Clicked");
                    dropBoxManager.unlinkDropbox();
                    invalidateOptionsMenu();
                }
                else {
                    Log.d(TAG, "There is no connection to internet");
                    Snackbar.make(getCurrentFocus(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }

                break;
            }
            case R.id.action_login: {
                if (new ConnectionDetector(getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection to internet");
                    dropBoxManager.loginDropbox();
                    Log.d(TAG, "Login Clicked");
                    invalidateOptionsMenu();
                }
                else {
                    Log.d(TAG, "There is no connection to internet");
                    Snackbar.make(getCurrentFocus(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }

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
        Crashlytics.setUserName(SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.DROPBOX_USER_NAME + " " + Keys.DROPBOX_LAST_NAME));
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
