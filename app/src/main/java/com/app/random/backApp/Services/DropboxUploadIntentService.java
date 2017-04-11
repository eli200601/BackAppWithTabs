package com.app.random.backApp.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.Formatter;
import android.util.Log;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.MainActivity;
import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DropboxUploadIntentService extends IntentService {

    private static String TAG = "UploadIntentService";

    private DropBoxManager dropBoxManager;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    int id = 1;
    private FilesUtils filesUtils;


    public DropboxUploadIntentService() {
        super(TAG);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() started");

    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent() started");
        Context context;
        context = getApplicationContext();
        filesUtils = FilesUtils.getInstance(context);
        dropBoxManager = DropBoxManager.getInstance(context);


        int icon = R.mipmap.ic_main;
        // Getting from Bundle the app list to upload
        Bundle bundle = intent.getExtras();
        ArrayList<AppDataItem> dirList = (ArrayList<AppDataItem>) bundle.getSerializable(Keys.DIR_TO_UPLOAD_LIST);
        ArrayList<AppDataItem> doneList = dirList;
        int listSize = dirList.size();
        //Saving the list in prefs to save state
        String jsonNotFinishList = filesUtils.getJSONStringFromArray(doneList);
        SharedPrefsUtils.setStringPreference(context,Keys.NOT_FINISH_UPLOAD_LIST,jsonNotFinishList);

        Log.d(TAG, "dirList size =  " + dirList.size());
        Log.d(TAG, "Starting to Upload....");

        int i=0;
        //Setting up the notification
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new Builder(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class).putExtra("started_from","notification"), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        // Starting to upload each app in the list
        for (AppDataItem item: new ArrayList<>(dirList)) {
            String fileName = createFileNameToUpload(item);
            Log.d(TAG, fileName);

            FileInputStream inputStream = null;
            try {
                File file = new File(item.getSourceDir());
                inputStream = new FileInputStream(file);

                Log.d(TAG, "Starting to upload: " + fileName);
                Log.d(TAG, "APK size is: " + file.length());

                if (dirList.size()==1) {
                    mBuilder.setContentTitle("Uploading...");
                    mBuilder.setContentText(item.getName() + " in progress");
                    mBuilder.setSmallIcon(icon);
                }
                else {
                    mBuilder.setContentTitle("Uploading " + item.getName());
                    mBuilder.setContentText("Uploading: " + String.valueOf(i) + "/" + String.valueOf(dirList.size()));
                }

                mBuilder.setSmallIcon(icon);
                mBuilder.setProgress(100, 0, false);
                mNotifyManager.notify(id, mBuilder.build());

                DropboxAPI.Entry newEntry = dropBoxManager.mDBApi.putFileOverwrite("/" + fileName, inputStream,
                        file.length(), new ProgressListener() {
                            @Override
                            public void onProgress(long bytes, long total) {
                                Log.d(TAG, "Byte is: " + String.valueOf(bytes) + "Total is: " + String.valueOf(total));

                                int percentage = (int) (bytes * 100.0 / total + 0.5);

                                Log.d(TAG, "percentage is: " + String.valueOf(percentage));

                                mBuilder.setProgress(100, percentage, false);
                                mNotifyManager.notify(id, mBuilder.build());

                            }
                        });

                Log.d(TAG, "New file is: " + newEntry.fileName() + " #### " + newEntry.path);
            }
            catch (Exception e) {
                Log.e(TAG, "Unable to upload, " + e.getMessage());
                break;
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) { Log.e(TAG, "Unable to upload, " + e.getMessage()); }
                }
            }
            mBuilder.setContentTitle("Upload completed");
            mBuilder.setContentText(item.getName() + " successfully uploaded.");
            // Finished to upload app, removing the app from doneList list
            assert doneList != null;
            doneList.remove(item);
            if (doneList.size() == 0) {
                SharedPrefsUtils.setStringPreference(context, Keys.NOT_FINISH_UPLOAD_LIST, null);
            }
            else {
                jsonNotFinishList = filesUtils.getJSONStringFromArray(doneList);
                SharedPrefsUtils.setStringPreference(context, Keys.NOT_FINISH_UPLOAD_LIST, jsonNotFinishList);
            }

            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
            i++;
            Log.d(TAG, "Done upload app: " + fileName);


        }
        Intent sendUpdateList = new Intent("com.app.random.backApp.OnFinishUploadReceiver");

        if (i != listSize) {
            mBuilder.setContentTitle("Upload Failed!");
            mBuilder.setContentText("Upload Failed! ");
            mNotifyManager.notify(id, mBuilder.build());
            sendUpdateList.putExtra(Keys.SERVICE_UPLOAD_STATUS, false);
        }
        else {
            mBuilder.setContentTitle("Upload completed!");
            mBuilder.setContentText("successfully uploaded " + String.valueOf(i) + " apps to cloud");
            mNotifyManager.notify(id, mBuilder.build());
            sendUpdateList.putExtra(Keys.SERVICE_UPLOAD_STATUS, true);
        }
        SharedPrefsUtils.setStringPreference(context, Keys.NOT_FINISH_UPLOAD_LIST, null);

        // Send broadcast action
//        intent.setAction("com.app.random.backApp.OnFinishUploadReceiver");
        sendBroadcast(sendUpdateList);
    }

    private String createFileNameToUpload(AppDataItem item) {
        String name;
        String separator = "_";
        String ending = ".apk";

        name = item.getName().replace(" ", "") + separator + item.getPackageName().trim() + separator +
                item.getAppVersion() + ending;

        return name;
    }



}
