package com.app.random.backApp.Dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DropBoxManager {

    public enum State {
        CREATED, IN_PROGRESS, DONE
    }

    private static String TAG = "DropBoxManager";

    private static DropBoxManager instance;
    private Context context;

    private HashMap<String, DropboxCallBackListener> dropboxCallBackListenerHashMap;

    public DropboxAPI<AndroidAuthSession> mDBApi;
    public boolean isLogIn;
    public State accountInfoState;

    ArrayList<AppDataItem> cloudAppsList;



    public static DropBoxManager getInstance(Context context) {
        if (instance == null) {
            instance = new DropBoxManager(context);
        }
        return instance;
    }

    private DropBoxManager(Context context) {
        this.mDBApi = initSession();
        this.context = context;
        this.isLogIn = false;
        this.dropboxCallBackListenerHashMap = new HashMap();
        this.cloudAppsList = new ArrayList<>();
        this.accountInfoState = State.CREATED;
    }

    private DropBoxManager() { }

    public void addDropboxListener(DropboxCallBackListener dropboxCallBackListener, String origin) {
        dropboxCallBackListenerHashMap.put(origin, dropboxCallBackListener);
    }

    public void removeDropboxListener (String origin) {
        Log.d(TAG, "Trying to remove " + origin + " From list");
        dropboxCallBackListenerHashMap.remove(origin);
    }


    public State getAccountInfoState() {
        return accountInfoState;
    }

    public DropboxAPI<AndroidAuthSession> initSession() {
        AppKeyPair appKeys = new AppKeyPair(Keys.DROPBOX_APP_KEY, Keys.DROPBOX_APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        return new DropboxAPI<AndroidAuthSession>(session);

    }

    public void loginDropbox() {
        String accessToken = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_ACCESS_TOKEN);
        if (accessToken == null) {
            Log.d(TAG, "New User as arrived");
            mDBApi.getSession().startOAuth2Authentication(context);
        }
        else {
            isLogIn = true;
            mDBApi.getSession().setOAuth2AccessToken(accessToken);
            getDropBoxFileListMethod();
//            mDBApi.getSession().finishAuthentication();
        }
    }

    public void unlinkDropbox() {
        mDBApi.getSession().unlink();
        SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_ACCESS_TOKEN, null);
        SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_USER_NAME, null);
        accountInfoState = State.CREATED;
        isLogIn = false;
    }

    public void onResumeManager() {

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.d(TAG, "Access Token: " + accessToken);

                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_ACCESS_TOKEN, accessToken);
                new LoadDataDropbox().execute();
                getDropBoxFileListMethod();
                isLogIn = true;

            }
            catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }

    }

    public boolean isLoginToDropbox() {
        return isLogIn;
    }

    public void getDropBoxFileListMethod() {
        Log.i("CloudMainFragment", "Starting to gets file list!");
        new GetDropBoxFileList().execute();

    }

    public static String deCamelCasealize(String camelCasedString) {
        if (camelCasedString == null || camelCasedString.isEmpty())
            return camelCasedString;

        StringBuilder result = new StringBuilder();
        result.append(camelCasedString.charAt(0));
        for (int i = 1; i < camelCasedString.length(); i++) {
            if (Character.isUpperCase(camelCasedString.charAt(i))) result.append(" ");
            result.append(camelCasedString.charAt(i));
        }
        return result.toString();
    }

    public void generateCloudAppsList(List<DropboxAPI.Entry> cloudFilesList) {
        // apk file contain appName_appPackageName_version.apk
        String separator = "_";
        cloudAppsList.clear();

        if (cloudFilesList == null) {
            // TODO: generate empty state list
        }
        else {

            for (DropboxAPI.Entry entry: cloudFilesList) {
                if (!entry.isDeleted) {
                    if (!entry.isDir) {
                        if (entry.fileName().contains(".apk")) {
                            String appName;
                            String appPackageName;
                            String appVersion;
                            String[] fileNameOutput;

                            fileNameOutput = entry.fileName().split(separator);
                            appName = deCamelCasealize(fileNameOutput[0].trim());
                            appPackageName = fileNameOutput[1].trim();
                            appVersion = fileNameOutput[2].trim().replace(".apk", "");

                            Log.d(TAG, "appName = " + appName);
                            Log.d(TAG, "appPackageName = " + appPackageName);
                            Log.d(TAG, "appVersion = " + appVersion);

                            AppDataItem appItem = new AppDataItem(appName, appPackageName, "/");
                            cloudAppsList.add(appItem);
                        }
                    }
                }
            }

//            for (AppDataItem appItem: cloudAppsList) {
//                Log.d(TAG, "appName = " + appItem.getName());
//                Log.d(TAG, "appPackageName = " + appItem.getPackageName());
////                Log.d(TAG, "appVersion = " + appVersion);
//            }

        }
        String listenTo = "CloudMainFragment";
        dropboxCallBackListenerHashMap.get(listenTo).onFinishGeneratingCloudList(cloudAppsList);


    }

    private class GetDropBoxFileList extends AsyncTask<Void, Void, List<DropboxAPI.Entry>> {

        @Override
        protected List<DropboxAPI.Entry> doInBackground(Void... param) {
            DropboxAPI.Entry entries = null;
            Log.i("CloudMainFragment", "New Tread started...");
            try {
                entries = mDBApi.metadata("/", 100, null, true, null);
            } catch (DropboxException e) {
                e.printStackTrace();

            }

            return entries.contents;
        }

        @Override
        protected void onPostExecute(List<DropboxAPI.Entry> cloudFilesList) {

            generateCloudAppsList(cloudFilesList);

            super.onPostExecute(cloudFilesList);
        }
    }

    private class LoadDataDropbox extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            accountInfoState = State.IN_PROGRESS;
            String name = null;

            try {

                name = mDBApi.accountInfo().displayName;
                Log.d(TAG, "Trying to fetch name = " + name);
                Log.d(TAG, name);
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return name;
        }

        @Override
        protected void onPostExecute(String result) {
            String listenTo = "MainActivity";
            if (result != null) {
                String arr[] = result.split(" ", 2);
                String username = "hi, " + arr[0];
                Log.d(TAG, "Finished background, name is: " + username);
                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_USER_NAME, username);
                accountInfoState = State.DONE;
//                dropboxCallBackListener.get().onUserNameReceived();
                if (dropboxCallBackListenerHashMap.containsKey(listenTo)) {
                    dropboxCallBackListenerHashMap.get(listenTo).onUserNameReceived();
                }
//                dropboxCallBackListenerArrayList.get(0).onUserNameReceived();

            }
            super.onPostExecute(result);
        }
    }

}

