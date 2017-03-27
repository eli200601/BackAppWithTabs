package com.app.random.backApp.Fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
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

import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Recycler.UpdateBottomBar;
import com.app.random.backApp.Utils.AppsDataUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;

import java.util.ArrayList;


public class DeviceAppsFragment  extends Fragment implements SearchView.OnQueryTextListener{

    private ArrayList<ApplicationInfo> appsListInfo =  new ArrayList<>();
    private ArrayList<AppDataItem> appsListData = new ArrayList<>();

    private AppsDataUtils appsDataUtils;
    private String PACKAGE_NAME;
    public int sortType = 0; // 1 = Dsc | 0 = Asc

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private TextView listAmountTextField;
    private TextView selectedAmountTextField;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_apps_list, null);

        setHasOptionsMenu(true);

        PACKAGE_NAME = getActivity().getPackageName();
        sortType = SharedPrefsUtils.getIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE, 0);

        appsDataUtils = new AppsDataUtils(getActivity().getPackageManager(), PACKAGE_NAME, sortType);

        //Bottom Bar init
        listAmountTextField = (TextView) view.findViewById(R.id.itemsInListValueText);
        selectedAmountTextField = (TextView) view.findViewById(R.id.ItemsSelectedValueText);

        //RecyclerView - Apps list
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData);
        mRecyclerView.setAdapter(mAdapter);

        new LoadApplications().execute();

        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick(int selectedAppsListSize) {
                updateBottomBar();
            }
        });

        return view;
    }
    
    public void updateBottomBar() {
        
        String appsListSize = String.valueOf(appsDataUtils.getAppsListInfoListSize());
        String selectedAppsListSize = String.valueOf(mAdapter.getSelectedAppsListSize());

        listAmountTextField.setText(appsListSize);
        selectedAmountTextField.setText(selectedAppsListSize + "/" + appsListSize);
        
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apps_frag_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_a_z: {
//              sortType = 0;
                SharedPrefsUtils.setIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE, 0);
                appsListData = appsDataUtils.updateSort(0);
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_sort_z_a: {
//              sortType = 1;
                SharedPrefsUtils.setIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE, 1);
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


    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            appsDataUtils.startGettingInfo();
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
            updateBottomBar();
            mAdapter.setItems(appsListData);
            mAdapter.notifyDataSetChanged();

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
