package com.app.random.backApp.Dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.util.HashMap;

public class DropBoxManager {

    public enum State {
        CREATED, IN_PROGRESS, DONE
    }
    private static String TAG = "DropBoxManager";

    private static DropBoxManager instance;
    private static Context context;

    private HashMap<String, DropboxCallBackListener> dropboxCallBackListenerHashMap;

    public DropboxAPI<AndroidAuthSession> mDBApi;
    public boolean isLogIn;
    public State accountInfoState;

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
        dropboxCallBackListenerHashMap = new HashMap();
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

