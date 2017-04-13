package com.app.random.backApp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.MainActivity;
import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by eliran.alon on 13-Apr-17.
 */

public class DropboxDownloadService extends Service {
    private static String TAG = "DropboxDownloadService";

    private Context context;

    private DropBoxManager dropBoxManager;
    private FilesUtils filesUtils;

    private ArrayList<AppDataItem> listToDownload;
    private int listToDownloadSize = 0;
    private String downloadPath;

    private NotificationManager mNotifyManager;
    private Notification.Builder mBuilder;

    int id = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        // Your code hare
        context = getApplicationContext();
        dropBoxManager = DropBoxManager.getInstance(context);
        filesUtils = FilesUtils.getInstance(context);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        Log.d(TAG, "Path is: " + downloadPath);

        Bundle bundle = intent.getExtras();
        listToDownload = (ArrayList<AppDataItem>) bundle.getSerializable(Keys.APPS_DOWNLOAD_ARRAYLIST);
        listToDownloadSize = listToDownload.size();

        buildNotification();
        startForeground(id, mBuilder.build());
//        displayNotification();

        new DownloadFileFromCloud().execute();


        return START_STICKY;

    }

    public void buildNotification() {
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_main)
                .setOngoing(true)
                .setAutoCancel(true)
                .setTicker("Starting to Download")
                .setProgress(100, 0, false)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent.putExtra(Keys.STARTED_FROM, Keys.DROPBOX_DOWNLOAD_INTENT_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void setNotificationText(String title, String content) {
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);
    }

    public void setNotificationProgress(int progress) {
        mBuilder.setProgress(100, progress, false);
    }

    public void displayNotification() {
        mNotifyManager.notify(id, mBuilder.build());
    }

    public void onFinishDownloadFiles(int result) {
        if (result == listToDownloadSize) {
            Log.d(TAG, "Download finished successful");
            stopForeground(true);
            mBuilder.setProgress(0, 0, false);
            setNotificationText("Download finished", "Finished successful to download " + String.valueOf(result) + " apps");

            //TODO: Send receiver hare to update the UI

            displayNotification();
            stopSelf();
        }
        else {
            Log.d(TAG, "Download Not finished successful");
            stopForeground(true);
            mBuilder.setProgress(0, 0, false);
            setNotificationText("Download finished", "Download Failed!");

            //TODO: Send receiver hare to update the UI

            displayNotification();
            stopSelf();
        }
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return null;
    }

    private class DownloadFileFromCloud extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... string) {
            DropboxAPI.DropboxFileInfo info;
            Integer finishDownloadItems = 0;


            for (AppDataItem item: listToDownload) {
                info = null;
                String filePath = item.getSourceDir();
                Log.d(TAG, "File path is: " + filePath);

                File file = new File(downloadPath + filePath);
                //Create new file in download target
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                setNotificationText("Staring to download", item.getName());
                setNotificationProgress(0);
                displayNotification();

                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    info = dropBoxManager.mDBApi.getFile(filePath, null, outputStream, new ProgressListener() {
                        @Override
                        public void onProgress(long bytes, long total) {
                            Log.d(TAG, "Byte is: " + String.valueOf(bytes) + "Total is: " + String.valueOf(total));

                            int percentage = (int) (bytes * 100.0 / total + 0.5);

                            Log.d(TAG, "percentage is: " + String.valueOf(percentage));

                            setNotificationProgress(percentage);
                            displayNotification();

                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch (DropboxException e) {
                    e.printStackTrace();
                    break;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                // finished to download a file
                if (info != null) {
                    finishDownloadItems = finishDownloadItems + 1;
                }
                Log.d(TAG, "File downloaded :)");
            }
            Intent intent = new Intent(Keys.BC_ON_FINISH_DOWNLOAD);
            if (finishDownloadItems.equals(listToDownloadSize)) {
                intent.putExtra(Keys.SERVICE_DOWNLOAD_STATUS, true);
            }
            else {
                intent.putExtra(Keys.SERVICE_DOWNLOAD_STATUS, false);
            }
            sendBroadcast(intent);
            //Finished the download flow hare

            return finishDownloadItems;
        }


        @Override
        protected void onPostExecute(Integer result) {
            onFinishDownloadFiles(result);
            super.onPostExecute(result);
        }
    // End of AsyncTask Class
    }

//End of Service
}
