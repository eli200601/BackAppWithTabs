package com.app.random.backApp.Activitys;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;


public class AppInfoDialogActivity extends Activity {

    static final String TAG = "AppInfoDialogActiviTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature( Window.FEATURE_ACTIVITY_TRANSITIONS );
//        getWindow().setEnterTransition(new Explode());
//        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info_dialog);

        int x,y;
        Intent intent = getIntent();

        Bundle bundle = intent.getBundleExtra("bundleAppInfo");
        AppDataItem app = (AppDataItem) bundle.getSerializable("AppInfo");

        x = intent.getIntExtra("x", 0);
        y = intent.getIntExtra("y", 0);

        Log.d(TAG, app.getPackageName());
        Log.d(TAG, app.getName());
        Log.d(TAG, "Original x,y = " + String.valueOf(x) + "," + String.valueOf(y));

        RelativeLayout screenOverlay = (RelativeLayout) findViewById(R.id.layout_screen);
        RelativeLayout dialog = (RelativeLayout) findViewById(R.id.dialog_container);

        ImageView appIcon = (ImageView) findViewById(R.id.app_icon);

        try {
            appIcon.setImageDrawable(getApplicationContext().getPackageManager().getApplicationIcon(app.getPackageName()));

        }
        catch (PackageManager.NameNotFoundException error) {
            Log.e(TAG, error.getMessage());
            appIcon.setImageResource(R.mipmap.ic_main);
        }

//        screenOverlay.setVisibility(View.INVISIBLE);
//        dialog.setVisibility(View.INVISIBLE);
//        appIcon.setVisibility(View.INVISIBLE);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        this.supportFinishAfterTransition();
    }
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        Intent intent = getIntent();
//        int startX = intent.getIntExtra("x", 0);
//        int startY = intent.getIntExtra("y", 0);
//        int[] i = {startX,startY};
//        Log.d(TAG, " StartX = " + String.valueOf(startX) + " startY = " + String.valueOf(startY));
//
//        RelativeLayout screenOverlay = (RelativeLayout) findViewById(R.id.layout_screen);
//        RelativeLayout dialog = (RelativeLayout) findViewById(R.id.dialog_container);
//
//        ImageView appIcon = (ImageView) findViewById(R.id.app_icon_activity_dialog);
//        ImageView moving_appIcon = (ImageView) findViewById(R.id.app_icon_activity_dialog_moving);
//        appIcon.setImageResource(R.mipmap.ic_main);
//        screenOverlay.setVisibility(View.INVISIBLE);
//        dialog.setVisibility(View.INVISIBLE);
//        appIcon.setVisibility(View.INVISIBLE);
//        moving_appIcon.setVisibility(View.VISIBLE);
//        screenOverlay.setBackgroundColor(getResources().getColor(R.color.backgroundOverlayColor));
//
//        if (startX + startY == 0) {
//            //Cannot retrive x,y. no animation
//        } else {
//            //Start icon animation
//
//            int[] targetPos = new int[2];
//            appIcon.getLocationOnScreen(targetPos);
//            int x = targetPos[0];
//            int y = targetPos[1];
//            Log.d(TAG, "Coordinate X = " + String.valueOf(x) + " Y = " + String.valueOf(y));
//            Rect rectf = new Rect();
//            appIcon.getLocalVisibleRect(rectf);
//
//            Log.d(TAG, "WIDTH        :" + String.valueOf(rectf.width()));
//            Log.d(TAG, "HEIGHT       :" + String.valueOf(rectf.height()));
//            Log.d(TAG, "left         :" + String.valueOf(rectf.left));
//            Log.d(TAG, "right        :" + String.valueOf(rectf.right));
//            Log.d(TAG, "top          :" + String.valueOf(rectf.top));
//            Log.d(TAG, "bottom       :" + String.valueOf(rectf.bottom));
//
//            Log.d(TAG, "get Left " + String.valueOf(appIcon.getLeft()) + " getTop = " + String.valueOf(appIcon.getTop()));
//            Animation anim_fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.dialog_fade_in);
//            anim_fade.reset();
//        }
//            screenOverlay.setAnimation(anim_fade);
//            dialog.setAnimation(anim_fade);

//            final PathMeasure pm;
//            final float point[] = {Float.valueOf(startX), 0f};
//
//            ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener(){
//                        @Override
//                        public void onAnimationUpdate (ValueAnimator animation) {
//                            float val = animation.getAnimatedFraction();
//                            pm.getPosTan(pm.getLength() * val, point, null);
//                            appIcon.setTranslationX(point[0]);
//                            appIcon.setTranslationY(point[1]);
//                        }
//                    };
//
//// and then to animate
//            pm = new PathMeasure(path, false);
//            ValueAnimator a = ValueAnimator.ofFloat(0.0f 1.0f);
//            a.setDuration(/* your duration */);
//            a.setInterpolator(/* your interpolator */);
//            a.addUpdateListener(listener);
//            a.start();
//            RelativeLayout relativeLayout = new RelativeLayout(this);
//// ImageView
//            ImageView imageView = new ImageView(this);
//
//// Setting layout params to our RelativeLayout
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(70, 70);
//
//// Setting position of our ImageView
//            layoutParams.leftMargin = startX;
//            layoutParams.topMargin = startY;
//
//// Finally Adding the imageView to RelativeLayout and its position
//            relativeLayout.addView(appIcon, layoutParams);
//            moving_appIcon.setX(startX + 10);
//            moving_appIcon.setY(startY - 170);
//            moving_appIcon.animate()
//                    .translationX(x)
//                    .translationY(y)
//                    .setInterpolator(new LinearInterpolator())
//                    .setDuration(4000)
//                    .start();

//        }
//        screenOverlay.setVisibility(View.VISIBLE);
//        dialog.setVisibility(View.VISIBLE);
//        moving_appIcon.setVisibility(View.VISIBLE);
//    }
}
