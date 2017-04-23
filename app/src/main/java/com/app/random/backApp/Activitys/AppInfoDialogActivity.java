package com.app.random.backApp.Activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.app.random.backApp.R;


public class AppInfoDialogActivity extends Activity {

    static final String TAG = "AppInfoDialogActiviTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info_dialog);
        RelativeLayout screenOverlay = (RelativeLayout) findViewById(R.id.layout_screen);
        RelativeLayout dialog = (RelativeLayout) findViewById(R.id.dialog_container);

        ImageView appIcon = (ImageView) findViewById(R.id.app_icon_activity_dialog);
//        ImageView moving_appIcon = (ImageView) findViewById(R.id.app_icon_activity_dialog_moving);
        appIcon.setImageResource(R.mipmap.ic_main);
        screenOverlay.setVisibility(View.INVISIBLE);
        dialog.setVisibility(View.INVISIBLE);
        appIcon.setVisibility(View.INVISIBLE);

//        moving_appIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Intent intent = getIntent();
        int startX = intent.getIntExtra("x", 0);
        int startY = intent.getIntExtra("y", 0);
        Log.d(TAG, " StartX = " + String.valueOf(startX) + " startY = " + String.valueOf(startY));

        RelativeLayout screenOverlay = (RelativeLayout) findViewById(R.id.layout_screen);
        RelativeLayout dialog = (RelativeLayout) findViewById(R.id.dialog_container);

        ImageView appIcon = (ImageView) findViewById(R.id.app_icon_activity_dialog);
//        ImageView moving_appIcon = (ImageView) findViewById(R.id.app_icon_activity_dialog_moving);
        appIcon.setImageResource(R.mipmap.ic_main);
        screenOverlay.setVisibility(View.INVISIBLE);
        dialog.setVisibility(View.INVISIBLE);
        appIcon.setVisibility(View.VISIBLE);
//        moving_appIcon.setVisibility(View.VISIBLE);
        screenOverlay.setBackgroundColor(getResources().getColor(R.color.backgroundOverlayColor));

        if (startX + startY == 0) {
            //Cannot retrive x,y. no animation
        } else {
            //Start icon animation

            int[] targetPos = new int[2];
            appIcon.getLocationOnScreen(targetPos);
            int x = targetPos[0];
            int y = targetPos[1];
            Log.d(TAG, "Coordinate X = " + String.valueOf(x) + " Y = " + String.valueOf(y));
            Rect rectf = new Rect();
            appIcon.getLocalVisibleRect(rectf);

            Log.d(TAG,"WIDTH        :"+ String.valueOf(rectf.width()));
            Log.d(TAG,"HEIGHT       :"+ String.valueOf(rectf.height()));
            Log.d(TAG,"left         :"+ String.valueOf(rectf.left));
            Log.d(TAG,"right        :"+ String.valueOf(rectf.right));
            Log.d(TAG,"top          :"+ String.valueOf(rectf.top));
            Log.d(TAG,"bottom       :"+ String.valueOf(rectf.bottom));

            Log.d(TAG, "get Left " + String.valueOf(appIcon.getLeft()) + " getTop = " + String.valueOf(appIcon.getTop()));
            Animation anim_fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_fade_in);
            anim_fade.reset();

            screenOverlay.setAnimation(anim_fade);
            dialog.setAnimation(anim_fade);

            Animation lTraslateAnimation = new TranlateAnimation()0 , 0, 0, 500);
            lTraslateAnimation.setDuration(2000);
            lTraslateAnimation.setFillAfter(true);
            lTraslateAnimation.setRepeatCount(-1);
            lTraslateAnimation.setRepeatMode(Animation.REVERSE);
            imgView .startAnimation(lTraslateAnimation);
//            appIcon.animate().scaleX(startX)
//                    .scaleY(startY)
//                    .x(x)
//                    .y(y)
//                    .setDuration(4000)
//                    .start();

        }
        screenOverlay.setVisibility(View.VISIBLE);
        dialog.setVisibility(View.VISIBLE);
        appIcon.setVisibility(View.VISIBLE);
    }
}
