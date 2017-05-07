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
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.app.random.backApp.Activitys.AboutActivity;
import com.app.random.backApp.Activitys.DBAccountInfoActivity;
import com.app.random.backApp.Activitys.SettingsActivity;
import com.app.random.backApp.Activitys.SplashScreenActivity;
import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.Dropbox.DropboxCallBackListener;
import com.app.random.backApp.Receiver.MessageEvent;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Utils.ConnectionDetector;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

//import com.google.android.gms.ads.AdListener;
//// [SNIPPET load_banner_ad]
//// Load an ad into the AdView.
//// [START load_banner_ad]
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//// [START_EXCLUDE]
//import com.google.android.gms.ads.InterstitialAd;

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

    private EventBus eventBus = EventBus.getDefault();

    private DropBoxManager dropBoxManager = null;
    private FilesUtils filesUtils;

    private FirebaseAnalytics mFirebaseAnalytics;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    AdView mAdView;
    AdRequest adRequest;
    AdListener adListener;
    InterstitialAd mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature( Window.FEATURE_ACTIVITY_TRANSITIONS );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-9908355189846572~1764017445");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        adListener = new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded");
                Animation anim_slide = AnimationUtils.loadAnimation(getApplicationContext() ,R.anim.banner_anim);
                anim_slide.reset();
                mAdView.startAnimation(anim_slide);
                mAdView.setVisibility(View.VISIBLE);
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("banner_ad_main_screen", params);
            }
        };


        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "Starting to display interstitial");
                mInterstitial.show();

            }
        });
        mInterstitial.setAdUnitId("ca-app-pub-9908355189846572/3660736242");

        mAdView = (AdView) findViewById(R.id.adView);
//        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdListener(adListener);
        mAdView.setVisibility(View.INVISIBLE);
//        mAdView.setAdUnitId("ca-app-pub-9908355189846572/3240750649");
        adRequest = new AdRequest.Builder()
                .addTestDevice("TEST_DEVICE_ID")
//                .addTestDevice("3EBD101ECF40235E879DEC9A5791380A") // Galaxy S6
//                .addTestDevice("CBB0D5315C3B905007D97BC471EA1351") // Galaxy Note 4
                .build();
        mAdView.loadAd(adRequest);
        mInterstitial.loadAd(adRequest);

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
            Bundle params = new Bundle();
            mFirebaseAnalytics.logEvent("open_app", params);
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

    private Transition enterTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setDuration(10000);

        return bounds;
    }

    private Transition returnTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new DecelerateInterpolator());
        bounds.setDuration(10000);

        return bounds;
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
        eventBus.unregister(this);
        mAdView.pause();

    }

    @Override
    public void onResume() {
        super.onResume();



        Log.d(TAG, "Adding " + TAG + " TO Listener List");
        eventBus.register(this);
        dropBoxManager.addDropboxListener(this, TAG);
        dropBoxManager.onResumeManager();
        invalidateOptionsMenu();
        // More Actions...
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    public void setTabPaging(int page) {
        mViewPager.setCurrentItem(2);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
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
                break;
            }
            case R.id.action_about: {
                startActivity(new Intent(this, AboutActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUserNameReceived() {
        String user_name = SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.DROPBOX_USER_NAME + " " + Keys.DROPBOX_LAST_NAME);
        Crashlytics.setUserName(user_name);
        mFirebaseAnalytics.setUserProperty("account_name", user_name);
        invalidateOptionsMenu();
    }

    @Override
    public void onFinishUploadFiles() {

    }



    @Override
    public void onFinishGeneratingCloudList(ArrayList<AppDataItem> arrayList) {
        Log.d(TAG, "Starting to display ad");

    }

    @Override
    public void onFinishDeletingFiles() {

    }

    @Override
    public void onFileUploadProgress(int percentage, long bytes, long total, AppDataItem app) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent messageEvent) {
        if (messageEvent instanceof MessageEvent.OnChargingDischargingEvent) {
            String message = ((MessageEvent.OnChargingDischargingEvent) messageEvent).getStatus();
            Log.d(TAG, "On Event occur, " + message);
        }
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
