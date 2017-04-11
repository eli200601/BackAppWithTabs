package com.app.random.backApp.Dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.Formatter;
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

    // Hold the cloud file list
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
        SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_DISPLAY_NAME, null);
        accountInfoState = State.CREATED;
        isLogIn = false;
    }

    public void loadUserInfo() {
        new LoadDataDropbox().execute();
    }

    public void onResumeManager() {

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.d(TAG, "Access Token: " + accessToken);

                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_ACCESS_TOKEN, accessToken);
                loadUserInfo();
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
        // This method starting the flow of getting the cloud list
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
                            Log.d(TAG, "path = " + entry.path);

                            AppDataItem appItem = new AppDataItem(appName, appPackageName, entry.path, appVersion, true);
                            appItem.setApkSize(entry.size);
                            cloudAppsList.add(appItem);
                        }
                    }
                }
            }

        }
        if (cloudAppsList.size() == 0) {
            AppDataItem appItem = new AppDataItem("There is no applications", "Please backup your apps", "/");
            cloudAppsList.add(appItem);
        }

        String listenTo = "CloudMainFragment";
        if (dropboxCallBackListenerHashMap.get(listenTo) != null ) {
            dropboxCallBackListenerHashMap.get(listenTo).onFinishGeneratingCloudList(cloudAppsList);
        }

    }



    public void deleteFileListFromCloud(List<AppDataItem> appDataItems) {
        ArrayList<String> dirList = new ArrayList<>();
//        String[] dirList;
        for (AppDataItem item: appDataItems) {
            String dir = item.getSourceDir();
            dirList.add(dir);
        }
        new DeleteFileFromCloud(dirList).execute();

    }

    private class DeleteFileFromCloud extends AsyncTask<Void, Void, Void> {

        private ArrayList<String> dirList;

        public DeleteFileFromCloud (ArrayList<String> dirList) {
            this.dirList = dirList;
        }

        @Override
        protected Void doInBackground(Void... strings) {
            for (String dir: dirList) {
                try {
                    mDBApi.delete(dir);
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String cloudFragListener = "CloudMainFragment";
            if (dropboxCallBackListenerHashMap.containsKey(cloudFragListener)) {
                dropboxCallBackListenerHashMap.get(cloudFragListener).onFinishDeletingFiles();
            }
            super.onPostExecute(result);
        }

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

    private class LoadDataDropbox extends AsyncTask<Void, Void, DropboxAPI.Account> {

        @Override
        protected DropboxAPI.Account doInBackground(Void... params) {
            accountInfoState = State.IN_PROGRESS;

            DropboxAPI.Account userAccount = null;

            try {
                userAccount = mDBApi.accountInfo();
            } catch (DropboxException e) {
                e.printStackTrace();
            }


            return userAccount;
        }

        @Override
        protected void onPostExecute(DropboxAPI.Account result) {
            String mainActivityListener = "MainActivity";
            String cloudFragListener = "CloudMainFragment";


            if (result != null) {
                String userName = null;
                String displayName = null;
                String lastName = null;
                String userEmail = null;
                String uid = null;
                String referral = null;
                long quota_long,quotaUsed_long;
                String quota;
                String quotaUsed;

                String fullName[] = result.displayName.split(" ", 2);

                userName = fullName[0];
                lastName = fullName[1];
                displayName = "hi, " + userName;
                quota_long = result.quota;
                quotaUsed_long = result.quotaNormal;
                quota = Formatter.formatFileSize(context, quota_long);
                quotaUsed = Formatter.formatFileSize(context, quotaUsed_long);
                uid = String.valueOf(result.uid);
                referral = result.referralLink;

                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_USER_NAME, userName);
                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_LAST_NAME, lastName);
                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_DISPLAY_NAME, displayName);

                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_TOTAL_SPACE, quota);
                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_USED_SPACE, quotaUsed);

                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_UID, uid);
                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_REFERRAL_URL, referral);

                SharedPrefsUtils.setLongPreference(context, Keys.DROPBOX_USED_SPACE_LONG, quotaUsed_long);
                SharedPrefsUtils.setLongPreference(context, Keys.DROPBOX_TOTAL_SPACE_LONG, quota_long);
                SharedPrefsUtils.setLongPreference(context, Keys.DROPBOX_FREE_SPACE_LONG, quota_long-quotaUsed_long);

                Log.d(TAG, "Finished background, name is: " + userName);

                accountInfoState = State.DONE;
//                dropboxCallBackListener.get().onUserNameReceived();
                if (dropboxCallBackListenerHashMap.containsKey(mainActivityListener)) {
                    dropboxCallBackListenerHashMap.get(mainActivityListener).onUserNameReceived();
                }
                if (dropboxCallBackListenerHashMap.containsKey(cloudFragListener)) {
                    dropboxCallBackListenerHashMap.get(cloudFragListener).onUserNameReceived();
                }
//

            }
            super.onPostExecute(result);
        }
    }

}

