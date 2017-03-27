package com.app.random.backApp.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.random.backApp.R;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

import java.util.ArrayList;
import com.app.random.backApp.Utils.Constants;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CloudMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CloudMainFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    private static final String TAG = "CloudMainFragment";

    private DropboxAPI<AndroidAuthSession> mDBApi;
    private boolean mIsLoggedIn = false;
    public boolean flag;

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

        AppKeyPair appKeys = new AppKeyPair(Constants.DROPBOX_APP_KEY, Constants.DROPBOX_APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String accessToken;
        accessToken = preferences.getString("accessToken", null);
        if (accessToken == null) {
            Log.d(TAG,"New User as arrived");
            mDBApi.getSession().startOAuth2Authentication(this.getActivity());
        }
        else{
            mDBApi.getSession().setOAuth2AccessToken(accessToken);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_main, container, false);

        Button button = (Button) view.findViewById(R.id.button);

        button.setOnClickListener(this);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                Log.d(TAG, "Access Token: " + accessToken);

                SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("accessToken", accessToken);

                editor.apply();

            } catch (IllegalStateException e) {
                Log.i("DbAuthLog","Error authenticating",e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button:

                new LoadDataDropbox().execute();

                break;
        }
    }

    private class LoadDataDropbox extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.d(TAG, mDBApi.accountInfo().displayName);
                Log.d(TAG, mDBApi.accountInfo().country);
                Log.d(TAG, mDBApi.accountInfo().referralLink);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();


            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null,
                    "Loading data from dropbox...");

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
