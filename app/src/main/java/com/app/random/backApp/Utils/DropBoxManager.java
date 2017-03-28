package com.app.random.backApp.Utils;

import android.content.Context;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

public class DropBoxManager {

    public enum State {
        CREATED, INPROGRESS, DONE
    }
    private static String TAG = "DropBoxManager";

    private static DropBoxManager instance;
    private Context context;

    public DropboxAPI<AndroidAuthSession> mDBApi;
    public boolean isLogedIn;
    public State state;

    public static DropBoxManager getInstance(Context context) {
        if (instance == null) {
            instance = new DropBoxManager(context);
        }
        return instance;
    }

    private DropBoxManager(Context context) {
        this.context = context;
        this.isLogedIn = false;
        this.state = State.CREATED;
    }

    private DropBoxManager() { }

    public State getState() {
        return state;
    }

    public void createSession() {

        AppKeyPair appKeys = new AppKeyPair(Keys.DROPBOX_APP_KEY, Keys.DROPBOX_APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        String accessToken = SharedPrefsUtils.getStringPreference(context, Keys.DROPBOX_ACCESS_TOKEN);

        if (accessToken == null) {
            Log.d(TAG, "New User as arrived");
            mDBApi.getSession().startOAuth2Authentication(context);
        } else {
            mDBApi.getSession().setOAuth2AccessToken(accessToken);
        }
    }

    public void onResumeManager() {

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.d(TAG, "Access Token: " + accessToken);

                SharedPrefsUtils.setStringPreference(context, Keys.DROPBOX_ACCESS_TOKEN, accessToken);
//                new MainActivity.LoadDataDropbox().execute();
                isLogedIn = true;

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    public void unlike

}

