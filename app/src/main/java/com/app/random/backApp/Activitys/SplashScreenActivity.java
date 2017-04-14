package com.app.random.backApp.Activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
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

public class SplashScreenActivity extends Activity {


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
        SharedPrefsUtils.setStringPreference(getApplicationContext(), Keys.PREF_VIEWTYPE, Keys.PREF_VIEWTYPE_LIST);
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        this.startActivity(myIntent);
        this.finish();
    }
}