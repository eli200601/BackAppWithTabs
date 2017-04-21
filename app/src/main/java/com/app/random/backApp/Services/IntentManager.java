package com.app.random.backApp.Services;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.app.random.backApp.Dropbox.DropBoxManager;

public class IntentManager {

    private static IntentManager instance;
    private Context context;
    Intent uploadIntent;

    public static synchronized IntentManager getInstance(Context context) {
        if (instance == null) {
            instance = new IntentManager(context);
        }
        return instance;
    }

    private IntentManager(Context context){
        this.context = context;
        this.uploadIntent = new Intent(context, DropboxUploadIntentService.class);
    }

    public void startUploadIntent(Bundle bundle) {
        uploadIntent.putExtras(bundle);
        context.startActivity(uploadIntent);
    }

    public void stopUploadIntent() {
        context.stopService(uploadIntent);
    }


}
