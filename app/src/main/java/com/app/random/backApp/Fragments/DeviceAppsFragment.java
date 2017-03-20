package com.app.random.backApp.Fragments;



import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Utils.AppsDataUtils;

import java.util.ArrayList;


public class DeviceAppsFragment  extends Fragment {

    private ArrayList<ApplicationInfo> appsListInfo =  new ArrayList<>();
    private ArrayList<AppDataItem> appsListData = new ArrayList<>();

    private AppsDataUtils appsDataUtils;
    private String PACKAGE_NAME;
    public int sortType = 0; // 1 = Dsc | 0 = Asc

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_apps_list, null);
        PACKAGE_NAME = getContext().getPackageName();
        appsDataUtils = new AppsDataUtils(getContext().getPackageManager(), PACKAGE_NAME, sortType);

        //RecyclerView - Apps list
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData);
        mRecyclerView.setAdapter(mAdapter);

        new LoadApplications().execute();

        return view;
    }


    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            appsDataUtils.startGettingInfo();
            appsListInfo = appsDataUtils.getAppInfoList();
            appsListData = appsDataUtils.getAppDataList();
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
            mAdapter.setItems(appsListData);
            mAdapter.notifyDataSetChanged();


//            try {
//                selectedAmountHolder.setText(String.valueOf(selectedApps.size())+ "/" + String.valueOf(appsListAI.size()));
//            }
//            catch (NullPointerException error){
//                Log.e(TAG, error.getMessage());
//            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getContext(), null,
                    "Loading application info...");

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
