package com.app.random.backApp.Activitys;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.random.backApp.MainActivity;
import com.app.random.backApp.R;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.crashlytics.android.Crashlytics;

import java.util.regex.Pattern;

public class SplashScreenActivity extends Activity {
    private final static String TAG = "SplashScreenActivity";

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
//    Thread splashTread;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        StartAnimations();
    }

    private void StartAnimations() {
        RelativeLayout title_layout=(RelativeLayout) findViewById(R.id.splash_title_container);
        final RelativeLayout bottom = (RelativeLayout) findViewById(R.id.splash_main_container);
        TextView description = (TextView) findViewById(R.id.splash_desc);
        TextView subtitle = (TextView) findViewById(R.id.splash_policy_desc);
        Button getStarted = (Button) findViewById(R.id.button);

        bottom.setVisibility(View.INVISIBLE);
        final Animation container_fade = AnimationUtils.loadAnimation(this, R.anim.alpha);
        container_fade.reset();

        Animation anim_slide = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim_slide.reset();
        anim_slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottom.startAnimation(container_fade);
                bottom.setVisibility(View.VISIBLE);
             }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        title_layout.startAnimation(anim_slide);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void getStartedClicked(View view) {
//        SharedPrefsUtils.getStringPreference(getApplicationContext(), Keys.PREF_ACTIVATE)
        SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.PREF_ACTIVATE, Keys.PREF_ACTIVATE);
        SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.PREF_VIEWTYPE_DEVICE, Keys.PREF_VIEWTYPE_LIST);
        SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.PREF_VIEWTYPE_CLOUD, Keys.PREF_VIEWTYPE_CARD);
        SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.PREF_VIEWTYPE_FOLDER, Keys.PREF_VIEWTYPE_GRID);

        // Crashlytics create a user session
        logUser();


        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        this.startActivity(myIntent);
        this.finish();
    }

    private void logUser() {
        // TODO: Use the current user's information
        // Crashlytics create a user session
        String email = getUserEmail();
        String id = getDeviceName();
//        String name = getDeviceUserName();

        Crashlytics.setUserIdentifier(id);
        Crashlytics.setUserEmail(email);
//        Crashlytics.setUserName(name);

    }

    private String getUserEmail() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        String possibleEmail = null;
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
                Log.d(TAG, "user email is : " + possibleEmail);
            }
        }
        return possibleEmail;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

//    public String getDeviceUserName() {
//        String name = null;
//        ContentResolver cr = getContentResolver();
//        Cursor curser = cr.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
//        if(curser.getCount()>0){
//            curser.moveToFirst();
//            name = curser.getString(curser.getColumnIndex(
//                    ContactsContract.Profile.DISPLAY_NAME));
//            Toast.makeText(getApplicationContext(), "name: "+name, Toast.LENGTH_SHORT).show();
//        }
//        curser.close();
//        return name;
//    }

}