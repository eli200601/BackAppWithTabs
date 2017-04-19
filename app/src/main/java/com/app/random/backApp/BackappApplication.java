package com.app.random.backApp;


import android.app.Application;
import android.util.Log;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class BackappApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        init();

    }

    private void init() {
        DropBoxManager.getInstance(getApplicationContext());
        Log.d("EliranHere", "Yesssssssssssssss");
    }
}
