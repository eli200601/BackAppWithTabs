package com.app.random.backApp;


import android.app.Application;
import android.util.Log;

import com.app.random.backApp.Dropbox.DropBoxManager;

public class BackappApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    private void init() {
        DropBoxManager.getInstance(getApplicationContext());
        Log.d("EliranHere", "Yesssssssssssssss");
    }
}
