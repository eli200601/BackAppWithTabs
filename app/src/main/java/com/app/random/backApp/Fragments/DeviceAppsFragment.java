package com.app.random.backApp.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.Dropbox.DropboxCallBackListener;
import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Recycler.UpdateBottomBar;
import com.app.random.backApp.Services.DropboxUploadIntentService;
import com.app.random.backApp.Utils.AppsDataUtils;
import com.app.random.backApp.Utils.ConnectionDetector;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.HashSet;


public class DeviceAppsFragment  extends Fragment implements SearchView.OnQueryTextListener, DropboxCallBackListener, SharedPreferences.OnSharedPreferenceChangeListener{

    private ArrayList<ApplicationInfo> appsListInfo =  new ArrayList<>();
    private ArrayList<AppDataItem> appsListData;

    HashSet<String> cloudAppsList;

    private static final String TAG = "DeviceAppsFragment";
    private FilesUtils filesUtils;
    private AppsDataUtils appsDataUtils;
    public int sortType = 0; // 1 = Dsc | 0 = Asc

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private TextView listAmountTextField;
    private TextView selectedAmountTextField;

    private DropBoxManager dropBoxManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dropBoxManager = DropBoxManager.getInstance(getActivity().getApplicationContext());
        filesUtils = FilesUtils.getInstance(getActivity().getApplicationContext());
        Log.d(TAG, "Building this Fragment!!!!");
        appsListData = new ArrayList<>();
        cloudAppsList = new HashSet<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps_list, null);

        setHasOptionsMenu(true);

        appsDataUtils = AppsDataUtils.getInstance(getActivity().getApplicationContext());

        //Bottom Bar init
        listAmountTextField = (TextView) view.findViewById(R.id.itemsInListValueText);
        selectedAmountTextField = (TextView) view.findViewById(R.id.ItemsSelectedValueText);

        //RecyclerView - Apps list
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setRecyclerLayoutType();

        new LoadApplications().execute();

        return view;
    }

    public void setRecyclerLayoutType(){
//        mRecyclerView.setLayoutManager(null);
        String prefViewType = SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_DEVICE);
        Log.d(TAG, "setRecyclerLayoutType(): " + SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_DEVICE));

        switch (prefViewType) {
            case Keys.PREF_VIEWTYPE_LIST: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
                break;
            }
            case Keys.PREF_VIEWTYPE_CARD: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
                break;
            }
            case Keys.PREF_VIEWTYPE_GRID: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), Keys.NUMBER_OF_COLUMNS));
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
                break;
            }
        }

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick() {
                updateBottomBar();
            }
        });
        mAdapter.setItems(appsListData);

        mAdapter.setCloudSavedList(cloudAppsList);
        Log.d(TAG, "Setting up cloud list = " + String.valueOf(mAdapter.getSelectedAppsListSize()));
        mAdapter.notifyDataSetChanged();
//        updateBottomBar();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.d(TAG, "Getting nuts");
        switch (key) {
            case Keys.PREF_VIEWTYPE_DEVICE: {
                if (mAdapter.getSelectedAppsListSize() > 0) {
                    cloudAppsList = mAdapter.getCloudSavedList();
                }
                setRecyclerLayoutType();
                break;
            }
        }
    }

    public void updateBottomBar() {
        Log.d(TAG, "updateBottomBar called");
        String appsListSize = String.valueOf(appsDataUtils.getAppsListInfoListSize());
        String selectedAppsListSize = String.valueOf(mAdapter.getSelectedAppsListSize());

        listAmountTextField.setText(appsListSize);
        selectedAmountTextField.setText(selectedAppsListSize + "/" + appsListSize);

        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apps_frag_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem uploadItem = menu.findItem(R.id.action_upload);
        MenuItem uninstallItem = menu.findItem(R.id.menuUninstallAPK);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);


        searchView.setOnQueryTextListener(this);

        if (mAdapter.getSelectedAppsListSize() > 0) {
            uninstallItem.setVisible(true);
        }
        else {
            uninstallItem.setVisible(false);
        }

        if (mAdapter.getSelectedAppsListSize() > 0 && dropBoxManager.isLogIn) {
            uploadItem.setVisible(true);
        }
        else {
            uploadItem.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_a_z: {
//               1 = Dsc | 0 = Asc
                SharedPrefsUtils.setIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE_INSTALLED_APPS, 0);
                appsListData = appsDataUtils.updateSort(0);
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_sort_z_a: {
//              1 = Dsc | 0 = Asc
                SharedPrefsUtils.setIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE_INSTALLED_APPS, 1);
                appsListData = appsDataUtils.updateSort(1);
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuSelectAll: {
                mAdapter.setAllListSelected();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuUnSelectAll: {
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_upload: {
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
                    HashSet<String> selectedPackageNameList;
                    ArrayList<AppDataItem> itemsToUpload;

                    selectedPackageNameList = mAdapter.getSelectedPackageNamesList();
                    ArrayList<String> dirList = new ArrayList<>();

                    Log.d(TAG, "Path ArrayList:::::::::::::::::::");
                    Log.d(TAG, appsDataUtils.getAPKArrayListFromPackageNames(selectedPackageNameList).toString());

                    itemsToUpload = appsDataUtils.getAPKArrayListFromPackageNames(selectedPackageNameList);
                    long totalUploadSize = filesUtils.getFileSizeFromListArray(itemsToUpload);
                    long cloudFreeSpace = SharedPrefsUtils.getLongPreference(getActivity().getApplicationContext(), Keys.DROPBOX_FREE_SPACE_LONG, totalUploadSize);
                    if ((cloudFreeSpace - totalUploadSize) < 0) {
                        Log.e(TAG, "There is no free space on cloud...");
                        Snackbar.make(getView(), "Upload failed. There is no free space on cloud...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else {
                        Snackbar.make(getView(), "Starting to upload " + selectedPackageNameList.size() + " applications...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Keys.APPS_UPLOAD_ARRAYLIST, itemsToUpload);


                        Intent intent = new Intent(getActivity().getApplicationContext(), DropboxUploadIntentService.class);
                        intent.putExtras(bundle);

                        Log.d(TAG, "Starting the intent....");

                        getActivity().startService(intent);

                        mAdapter.clearSelectedList();
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    Log.d(TAG, "There is no connection...");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                break;
            }
            case R.id.menuUninstallAPK: {
                ArrayList<AppDataItem> uninstallList = mAdapter.getSelectedCustomArrayList();
                filesUtils.uninstallAppFromList(uninstallList);
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
    public boolean onQueryTextSubmit(String query) {
        appsListData = appsDataUtils.getFilteredListByString(query);
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        appsListData = appsDataUtils.getFilteredListByString(newText);
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
        return true;
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
    public void onUserNameReceived() {

    }

    @Override
    public void onFinishUploadFiles() {

    }

    public boolean isInstalled(String packageName) {
        for (AppDataItem item: appsListData) {
            if (packageName.equals(item.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFinishGeneratingCloudList(ArrayList<AppDataItem> arrayList) {
        cloudAppsList.clear();
        Log.d(TAG, "onFinishGeneratingCloudList Started arrayList = " + String.valueOf(arrayList.size()));
        Log.d(TAG, "onFinishGeneratingCloudList Started appsListData = " + String.valueOf(appsListData.size()));
        for (AppDataItem cloudApp: arrayList) {
            if (isInstalled(cloudApp.getPackageName()))
                    cloudAppsList.add(cloudApp.getPackageName());

        }
        //??
        Log.d(TAG, " Done loading list from cloud - List size is: " + String.valueOf(cloudAppsList.size()));
        setRecyclerLayoutType();
        updateBottomBar();
    }

    @Override
    public void onFinishDeletingFiles() {

    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            appsDataUtils.startGettingInfo();
            appsListData.clear();
            appsListInfo = appsDataUtils.getAppInfoList();
            appsListData = appsDataUtils.getAppDataList();

//            updateBottomBar();
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
//            mRecyclerView.setAdapter(mAdapter);
            progress.dismiss();
            Log.d(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            mAdapter.setItems(appsListData);
            if (dropBoxManager.cloudAppsList.size() > 0) {
                onFinishGeneratingCloudList(dropBoxManager.cloudAppsList);
            }
            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "LoadApplications Started appsListData = " + String.valueOf(appsListData.size()));
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null,
                    "Loading application info...");

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
