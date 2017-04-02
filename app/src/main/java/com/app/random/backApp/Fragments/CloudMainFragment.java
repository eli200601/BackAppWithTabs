package com.app.random.backApp.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.Dropbox.DropboxCallBackListener;
import com.app.random.backApp.R;
import com.dropbox.client2.session.Session.AccessType;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CloudMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CloudMainFragment extends Fragment implements View.OnClickListener, DropboxCallBackListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    private static final String TAG = "CloudMainFragment";

    private DropBoxManager dropBoxManager = null;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public CloudMainFragment() {
        // Required empty public constructor
    }


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

        dropBoxManager = DropBoxManager.getInstance(getActivity().getApplicationContext());


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPause() {
        super.onPause();
        dropBoxManager.removeDropboxListener(TAG);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Adding " + TAG + " TO Listener List");
        dropBoxManager.addDropboxListener(this, TAG);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button: {

                break;
            }
        }
    }

    @Override
    public void onUserNameReceived() {

    }

    @Override
    public void onFinishUploadFiles() {

    }
}
