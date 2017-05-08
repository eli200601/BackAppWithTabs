package com.app.random.backapp.Activitys;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.random.backapp.R;
import com.app.random.backapp.Recycler.AppDataItem;
import com.app.random.backapp.Services.DropboxUploadIntentService;
import com.app.random.backapp.Utils.ConnectionDetector;
import com.app.random.backapp.Utils.FilesUtils;
import com.app.random.backapp.Utils.Keys;
import com.app.random.backapp.Utils.SharedPrefsUtils;

import java.util.ArrayList;


public class AppInfoDialogActivity extends AppCompatActivity {

    static final String TAG = "AppInfoDialogActiviTAG";

    FilesUtils filesUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature( Window.FEATURE_ACTIVITY_TRANSITIONS );
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_info_dialog);
//        Transition slide = TransitionInflater.from(this).inflateTransition(R.transition.slide_element);
//        getWindow().setExitTransition(slide);

        filesUtils = FilesUtils.getInstance(getApplicationContext());

        int x,y;
        Intent intent = getIntent();

        Bundle bundle = intent.getBundleExtra("bundleAppInfo");
        final AppDataItem app = (AppDataItem) bundle.getSerializable("AppInfo");

        x = intent.getIntExtra("x", 0);
        y = intent.getIntExtra("y", 0);

        Log.d(TAG, app.getPackageName());
        Log.d(TAG, app.getName());
        Log.d(TAG, "Original x,y = " + String.valueOf(x) + "," + String.valueOf(y));

        RelativeLayout screenOverlay = (RelativeLayout) findViewById(R.id.layout_screen);
        RelativeLayout dialog = (RelativeLayout) findViewById(R.id.dialog_container);

        ImageView appIcon = (ImageView) findViewById(R.id.app_icon);
        TextView appTitle = (TextView) findViewById(R.id.app_name);
        TextView appSize = (TextView) findViewById(R.id.item_size);
        TextView appVersion = (TextView) findViewById(R.id.item_version);

        Button shareAPK = (Button) findViewById((R.id.share_apk_card_action));
        Button downloadAPK = (Button) findViewById((R.id.download_apk_card_action));
        Button deleteAPK = (Button) findViewById((R.id.delete_apk_card_action));
        Button done = (Button) findViewById(R.id.done_share_apk_dialog);

        String verStr;
        if (!app.getAppVersion().contains("v")) {
            verStr = "Version: v" + app.getAppVersion();
        }
        else {
            verStr = "Version: " + app.getAppVersion();
        }
        appTitle.setText(app.getName());
        appSize.setText("APK Size: " + app.getApkSize());
        appVersion.setText(verStr);
        shareAPK.setText(R.string.device_info_card_action);
        downloadAPK.setText(R.string.device_upload_card_action);
        deleteAPK.setText(R.string.device_uninstall_card_action);

        try {
            appIcon.setImageDrawable(getApplicationContext().getPackageManager().getApplicationIcon(app.getPackageName()));

        }
        catch (PackageManager.NameNotFoundException error) {
            Log.e(TAG, error.getMessage());
            appIcon.setImageResource(R.mipmap.ic_main);
        }

        // Listeners
        shareAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        downloadAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Download Apk button clicked
                if (new ConnectionDetector(getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");

                    ArrayList<AppDataItem> itemsToUpload = new ArrayList<>();


                    itemsToUpload.add(app);
                    long totalUploadSize = filesUtils.getFileSizeFromListArray(itemsToUpload);
                    long cloudFreeSpace = SharedPrefsUtils.getLongPreference(getApplicationContext(), Keys.DROPBOX_FREE_SPACE_LONG, totalUploadSize);
                    if ((cloudFreeSpace - totalUploadSize) < 0) {
                        Log.e(TAG, "There is no free space on cloud...");
//                        Snackbar.make(getCurrentFocus(), "Upload failed. There is no free space on cloud...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else {
//                        Snackbar.make(getCurrentFocus(), "Starting to upload " + String.valueOf(itemsToUpload.size()) + " applications...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Keys.APPS_UPLOAD_ARRAYLIST, itemsToUpload);

                        Intent intent = new Intent(getApplicationContext(), DropboxUploadIntentService.class);
                        intent.putExtras(bundle);

                        Log.d(TAG, "Starting the intent....");

                        startService(intent);

//                        mAdapter.clearSelectedList();
//                        mAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    Log.d(TAG, "There is no connection...");
//                    Snackbar.make(getCurrentFocus(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        deleteAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Uninstall apk clicked
                ArrayList<AppDataItem> uninstallList = new ArrayList<>();
                uninstallList.add(app);
                filesUtils.uninstallAppFromList(uninstallList);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Done Clicked
                finishAfterTransition();
            }
        });

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
