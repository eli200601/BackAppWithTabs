package com.app.random.backApp.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.Dropbox.DropboxCallBackListener;
import com.app.random.backApp.MainActivity;
import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Recycler.UpdateBottomBar;
import com.app.random.backApp.Utils.AppsDataUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CloudMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CloudMainFragment extends Fragment implements View.OnClickListener, DropboxCallBackListener {

    private OnFinishUploadReceiver onFinishUploadReceiver;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private static final String TAG = "CloudMainFragment";

    private DropBoxManager dropBoxManager = null;

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private AppsDataUtils appsDataUtils;

    private ArrayList<AppDataItem> appsListData = new ArrayList<>();

    public int sortType = 0; // 1 = Dsc | 0 = Asc


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
        onFinishUploadReceiver = new OnFinishUploadReceiver();
        setHasOptionsMenu(true);

        dropBoxManager = DropBoxManager.getInstance(getActivity().getApplicationContext());

        appsDataUtils = AppsDataUtils.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_main, container, false);

        sortType = SharedPrefsUtils.getIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE, 0);

        //RecyclerView - Apps list
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_cloud);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick() {
                // Do stuff when clicking on checkbox
            }
        });

//        dropBoxManager.getDropBoxFileListMethod();
//        Button button = (Button) view.findViewById(R.id.button);
//
//        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cloud_frag_menu, menu);

        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
//        MenuItem sort_a_zItem = menu.findItem(R.id.action_sort_a_z_cloud);
//        MenuItem sort_z_aItem = menu.findItem(R.id.action_sort_z_a_cloud);
        MenuItem selectAllItem = menu.findItem(R.id.menuSelectAll_cloud);
        MenuItem unSelectAllItem = menu.findItem(R.id.menuUnSelectAll_cloud);


        Log.d(TAG, "dropbox is login? " + String.valueOf(dropBoxManager.isLoginToDropbox()));

        refreshItem.setVisible(dropBoxManager.isLoginToDropbox());
        deleteItem.setVisible(dropBoxManager.isLoginToDropbox());
//        sort_a_zItem.setVisible(dropBoxManager.isLoginToDropbox());
//        sort_z_aItem.setVisible(dropBoxManager.isLoginToDropbox());
        selectAllItem.setVisible(dropBoxManager.isLoginToDropbox());
        unSelectAllItem.setVisible(dropBoxManager.isLoginToDropbox());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh: {
                dropBoxManager.getDropBoxFileListMethod();
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }
//            case R.id.action_sort_a_z_cloud: {
//
//                break;
//            }
//            case R.id.action_sort_z_a: {
//
//                break;
//            }
            case R.id.menuSelectAll_cloud: {
                mAdapter.setAllListSelected();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuUnSelectAll_cloud: {
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }


            case R.id.action_delete: {
                if (mAdapter.getSelectedAppsListCloud().size() > 0) {
                    dropBoxManager.deleteFileListFromCloud(mAdapter.getSelectedAppsListCloud());
                    mAdapter.clearSelectedList();
                }

                //Continue here !!!!!!!!!!!!!!!!!!!!!!!!!!
            }

            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
        dropBoxManager.removeDropboxListener(TAG);
        getActivity().unregisterReceiver(onFinishUploadReceiver);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Adding " + TAG + " TO Listener List");
        dropBoxManager.addDropboxListener(this, TAG);
        getActivity().registerReceiver(onFinishUploadReceiver,new IntentFilter("com.app.random.backApp.OnFinishUploadReceiver"));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
//        switch (id) {
//            case R.id.button: {
//
//                dropBoxManager.getDropBoxFileListMethod();
//
//                break;
//            }
//        }
    }

    @Override
    public void onUserNameReceived() {

    }

    @Override
    public void onFinishUploadFiles() {

    }

    @Override
    public void onFinishGeneratingCloudList(ArrayList<AppDataItem> cloudList) {
        appsListData = cloudList;
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onFinishDeletingFiles() {
        View view = getView();
        assert view != null;
        Snackbar.make(view, "Files deleted...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        dropBoxManager.getDropBoxFileListMethod();
    }

    public void updateUIList(Intent intent) {
        dropBoxManager.getDropBoxFileListMethod();
        mAdapter.clearSelectedList();
        mAdapter.notifyDataSetChanged();
        View view = getView();
        assert view != null;
        Snackbar.make(view, "Uploaded successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }


    public class OnFinishUploadReceiver extends BroadcastReceiver {

//        public OnFinishUploadReceiver() { super();}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver finish upload");
            updateUIList(intent);

        }
    }

}
