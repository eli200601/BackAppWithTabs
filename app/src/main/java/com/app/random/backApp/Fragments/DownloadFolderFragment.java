package com.app.random.backApp.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Recycler.UpdateBottomBar;
import com.app.random.backApp.Utils.AppsDataUtils;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;

import java.util.ArrayList;

public class DownloadFolderFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "DownloadFolderFragment";
    private AppsDataUtils appsDataUtils;
    private FilesUtils filesUtils;

    private OnFinishDownloadReceiver onFinishDownloadReceiver;

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private Context context;
    private ArrayList<AppDataItem> appsListData = new ArrayList<>();

    private TextView listAmountTextField;
    private TextView selectedAmountTextField;

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
        setRecyclerLayoutType();

        appsListData = appsDataUtils.getFolderAppsList();
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();

        //Starting to load applications here
//        isStoragePermissionGranted();


        listAmountTextField = (TextView) view.findViewById(R.id.itemsInListValueText_folder);
        selectedAmountTextField = (TextView) view.findViewById(R.id.ItemsSelectedValueText_folder);
        updateBottomBar();
        return view;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) {
                Log.v(TAG,"Permission is granted");
                appsListData = appsDataUtils.getFolderAppsList();
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                String[] permissions = new String[]{
//                        android.Manifest.permission.INTERNET,
//                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//
                };
                requestPermissions(permissions, 1);
//                && getContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                requestPermissions(new String[]{ android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            appsListData = appsDataUtils.getFolderAppsList();
            mAdapter.setItems(appsListData);
            mAdapter.notifyDataSetChanged();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG,"onRequestPermissionsResult start grantResults.size = " + String.valueOf(grantResults.length));
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission: "+permissions[0]+ " was " + grantResults[0]);
            appsListData = appsDataUtils.getFolderAppsList();
            mAdapter.setItems(appsListData);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateBottomBar() {

        String appsListSize = String.valueOf(appsDataUtils.getAppsListFolderSize());
        String selectedAppsListSize = String.valueOf(mAdapter.getSelectedAppsListSize());

        listAmountTextField.setText(appsListSize);
        selectedAmountTextField.setText(selectedAppsListSize + "/" + appsListSize);

        getActivity().invalidateOptionsMenu();

    }

    public void setRecyclerLayoutType(){
        String prefViewType = SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_FOLDER);
        Log.d(TAG, "setRecyclerLayoutType(): " + SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_FOLDER));
        switch (prefViewType) {
            case Keys.PREF_VIEWTYPE_LIST: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(), appsListData, TAG);
                mRecyclerView.setAdapter(mAdapter);
                break;
            }
            case Keys.PREF_VIEWTYPE_CARD: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(), appsListData, TAG);
                mRecyclerView.setAdapter(mAdapter);
                break;
            }
            case Keys.PREF_VIEWTYPE_GRID: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), Keys.NUMBER_OF_COLUMNS));
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(), appsListData, TAG);
                mRecyclerView.setAdapter(mAdapter);
                break;
            }
        }
        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick() {
                updateBottomBar();
            }

            @Override
            public void onShareAPKButtonClick(AppDataItem app) {

            }
        });
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
//        updateBottomBar();
    }
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), Keys.NUMBER_OF_COLUMNS));
//        mRecyclerView.setHasFixedSize(true);
//        mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
//        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
//            @Override
//            public void onCheckBoxClick() {
//                updateBottomBar();
//            }
//        });
//    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case Keys.PREF_VIEWTYPE_FOLDER: {
                setRecyclerLayoutType();
                break;
            }
        }
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
                ArrayList<AppDataItem> listToDelete;
                listToDelete = mAdapter.getSelectedCustomArrayList();
                boolean flag = filesUtils.deleteFilesFromArray(listToDelete);
                new PostDeleteRefresh().execute();
                if (flag) {
                    Snackbar.make(getView(), "Files deleted.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else {
                    Snackbar.make(getView(), "An error occur", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }


                break;
            }
            case R.id.folder_action_install:{
                ArrayList<AppDataItem> listToInstall;
                listToInstall = mAdapter.getSelectedCustomArrayList();
                filesUtils.installFilesFromArray(listToInstall);
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();

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

    public class OnFinishDownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver finish download");
            if (intent.getBooleanExtra(Keys.SERVICE_DOWNLOAD_STATUS,false)) {
                Snackbar.make(getView(), "Download successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                appsListData = appsDataUtils.getFolderAppsList();
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
            else {
                Snackbar.make(getView(), "Download Failed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                appsListData = appsDataUtils.getFolderAppsList();
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
            }


        }
    }

    private class PostDeleteRefresh extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;

        }


        @Override
        protected void onPostExecute(Void result) {
            appsListData = appsDataUtils.getFolderAppsList();
            mAdapter.setItems(appsListData);
            mAdapter.clearSelectedList();
            mAdapter.notifyDataSetChanged();
            updateBottomBar();
            super.onPostExecute(result);
        }

    }


}
