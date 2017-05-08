package com.app.random.backapp.Services;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class IntentManager {

    private static IntentManager instance;
//    private Context context;
    Intent uploadIntent;

    public static synchronized IntentManager getInstance() {
        if (instance == null) {
            instance = new IntentManager();
        }
        return instance;
    }

    private IntentManager(){
//        this.context = context;
//        this.uploadIntent = new Intent(context, DropboxUploadIntentService.class);
    }

    public void startUploadIntent(Bundle bundle, Context context) {
        Intent intent = new Intent(context, DropboxUploadIntentService.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
//        context.startActivity();
    }

    public void stopUploadIntent(Context context) {
        context.stopService(uploadIntent);
    }


}
