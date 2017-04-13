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

import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Recycler.UpdateBottomBar;
import com.app.random.backApp.Utils.AppsDataUtils;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;

import java.util.ArrayList;

public class DownloadFolderFragment extends Fragment {

    private static final String TAG = "DownloadFolderFragment";
    private AppsDataUtils appsDataUtils;
    private FilesUtils filesUtils;

    private OnFinishDownloadReceiver onFinishDownloadReceiver;

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private Context context;
    private ArrayList<AppDataItem> appsListData = new ArrayList<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        appsDataUtils = AppsDataUtils.getInstance(context);
        filesUtils = FilesUtils.getInstance(context);
        mAdapter = new MyRecyclerAdapter(context,appsListData, TAG);
        onFinishDownloadReceiver = new OnFinishDownloadReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //TODO: xml file for this
        View view = inflater.inflate(R.layout.fragment_download_folder, container, false);
        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_folder);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        //Starting to load applications here
        appsListData = appsDataUtils.getFolderAppsList();
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();

        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick() {
                //When checkbox is clicked
                getActivity().invalidateOptionsMenu();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(onFinishDownloadReceiver, new IntentFilter(Keys.BC_ON_FINISH_DOWNLOAD));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(onFinishDownloadReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.folder_frag_menu, menu);
        MenuItem deleteItem = menu.findItem(R.id.folder_action_delete);
        MenuItem installItem = menu.findItem(R.id.folder_action_install);

        if (mAdapter.getSelectedAppsListSize() > 0) {
            deleteItem.setVisible(true);
            installItem.setVisible(true);
        }
        else {
            deleteItem.setVisible(false);
            installItem.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean result = false;
        switch (id) {
            case R.id.folder_action_delete:{
                break;
            }
            case R.id.folder_action_install:{
                break;
            }
            case R.id.folder_action_refresh:{
                appsListData = appsDataUtils.getFolderAppsList();
                mAdapter.setItems(appsListData);
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }
            default:
                result = super.onOptionsItemSelected(item);
                break;

        }
        return result;
    }

    public class OnFinishDownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver finish download");
            if (intent.getBooleanExtra(Keys.SERVICE_DOWNLOAD_STATUS,false)) {
                Snackbar.make(getView(), "Download successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                appsListData = appsDataUtils.getFolderAppsList();
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
            }
            else {
                Snackbar.make(getView(), "Download Failed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                appsListData = appsDataUtils.getFolderAppsList();
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
            }


        }
    }

}
