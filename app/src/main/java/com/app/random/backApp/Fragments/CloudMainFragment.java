package com.app.random.backApp.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.random.backApp.R;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CloudMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CloudMainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private final static String DROPBOX_FILE_DIR = "/Apps/BackAppFolder/";
    private final static String DROPBOX_NAME = "BackAppFolder";
    private final static String DROPBOX_APP_KEY = "r1qzs8cbwnhlnlb";
    private final static String DROPBOX_APP_SECRET = "abfz67yh01qz456";
    private final static AccessType ACCESS_TYPE = AccessType.DROPBOX;

    private DropboxAPI<AndroidAuthSession> mDBApi;
    public boolean flag;

    ArrayList files;
    ArrayList dir;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public CloudMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CloudMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CloudMainFragment newInstance(String param1, String param2) {
        CloudMainFragment fragment = new CloudMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        flag = false;
        AppKeyPair appKeys = new AppKeyPair(DROPBOX_APP_KEY, DROPBOX_APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cloud_main, container, false);
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(DROPBOX_APP_KEY,DROPBOX_APP_SECRET);
        AndroidAuthSession session;

//        String[] stored = getKeys();
        SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.name_sharedPref), Context.MODE_PRIVATE);
        String key;
        String secret;
        key = preferences.getString("key", null);
        secret = preferences.getString("secret", null);

        if (key != null) {
            flag = true;
            AccessTokenPair accessToken = new AccessTokenPair(key,secret);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }
        return session;
    }


    @Override
    public void onResume() {

        AndroidAuthSession session = (AndroidAuthSession) mApi.getSession();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {

                mDBApi.getSession().finishAuthentication();
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                session.finishAuthentication();

                TokenPair tokens = mDBApi.getSession().getAccessTokenPair();
//                storeKeys(tokens.key, tokens.secret);
                SharedPreferences preferences = getActivity().getSharedPreferences(getString(R.string.name_sharedPref), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("key", tokens.key);
                editor.putString("secret", tokens.secret);
                editor.apply();

                flag = true;
            } catch (IllegalStateException e) {
                Log.e("CloudMain", e.getMessage());
            }
        }
        DropboxAPI.Entry direct = null;
        try {
            direct = mApi.metadata("/", 1000, null, true, null);
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        files = new ArrayList<DropboxAPI.Entry>();
        dir = new ArrayList<String>();
        for (com.dropbox.client2.DropboxAPI.Entry ent : direct.contents) {
            files.add(ent);
            dir.add(new String(ent.path));
        }
        super.onResume();
    }



}
