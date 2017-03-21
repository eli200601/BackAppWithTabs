package com.app.random.backApp;


import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

interface MyListener {
    public void callback(String packegName);
}

public class AppsListFragment extends Fragment implements MyListener {

    private static final String TAG = "AppsListFragment";
    private static String PACKEG_NAME;

    private List<AppsListItems> appsItemsList = new ArrayList<AppsListItems>();
    private List<ApplicationInfo> appsListAI = null;
    private int listSize = -1;
    public int sortType = 0; // 1 = Dsc | 0 = Asc
    private int listPosition = -1;
    private HashSet<String> selectedApps = new HashSet<String>();

    private PackageManager packageManager;

    private RecyclerView mRecyclerView;
    private AppsListRecyclerAdapter mAdapter;

    private TextView listAmountHolder;
    private TextView selectedAmountHolder;


    public static AppsListFragment newInstance() {

        AppsListFragment fragment = new AppsListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps_list, container, false);

        PACKEG_NAME = getContext().getPackageName();

        listAmountHolder = (TextView) container.findViewById(R.id.itemsInListValueText);
        selectedAmountHolder = (TextView) container.findViewById(R.id.ItemsSelectedValueText);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new AppsListRecyclerAdapter(getContext(), appsItemsList, selectedApps, this);
        mRecyclerView.setAdapter(mAdapter);

        //async
        new LoadApplications().execute();
//        createList();


        //onPostExecute
        mAdapter.notifyDataSetChanged();
//        linearLayoutManager.findFirstVisibleItemPosition();

        return view;
    }

    @Override
    public void callback(String packegName) {
        // do your stuff here
        Log.d(TAG, "Table is " + selectedApps.toString());
        try {
            String a,b;
            if (selectedApps.contains(packegName)){
                selectedApps.remove(packegName);
            }
            else {
                selectedApps.add(packegName);
            }
            a = String.valueOf(selectedApps.size());
            b = String.valueOf(appsListAI.size());
            selectedAmountHolder.setText(a + "/" + b);
        }
        catch (NullPointerException error){
            Log.e(TAG, error.getMessage());
        }

    }


//    public void createList() {
//        ArrayList<ApplicationInfo> filterAppsList = new ArrayList<>();
//        packageManager = getContext().getPackageManager();
//        appsListAI = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
//        for (ApplicationInfo info : appsListAI){
//            if ((info.flags & info.FLAG_SYSTEM) == 0 ){
//                filterAppsList.add(info);
//                AppsListItems appsListItem = new AppsListItems();
//                appsListItem.setAppName(packageManager.getApplicationLabel(info).toString());
//                appsListItem.setPackageName(info.packageName);
//                appsItemsList.add(appsListItem);
//            }
//        }
//    }
// Listener


    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> appsList = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if ((info.flags & info.FLAG_SYSTEM) == 0) {
                    Log.d("Package1: ", "PACKEG_NAME=" + PACKEG_NAME + " info=" + info.packageName);
                    if ( !info.packageName.equals(PACKEG_NAME) ) { appsList.add(info); }
//                    Log.d(TAG, "#### This is User app ####");
//                    Log.d(TAG, "#####################################");
//                    Log.d(TAG, "className = " + info.className);
//                    Log.d(TAG, "Name = " + info.name);
//                    Log.d(TAG, "PackageName = " + info.packageName);
//                    Log.d(TAG, "dataDIr = " + info.dataDir);
//                    Log.d(TAG, "processName = " + info.processName);
//                    Log.d(TAG, "sourceDir = " + info.sourceDir);
//                    Log.d(TAG, "is system app = " + String.valueOf(info.FLAG_SYSTEM));
//                    Log.d(TAG, "flags is = " + String.valueOf(info.flags));
//                    Log.d(TAG, "1 App Name is = " + packageManager.getApplicationLabel(info));
//					Log.d(TAG, "2 App Name is = " + info.loadLabel(packageManager));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return appsList;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            MyListener ca = newInstance();
            packageManager = getContext().getPackageManager();
            appsListAI = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            if (appsListAI != null) {
                listSize = appsListAI.size();
                if (sortType == 0 ) {
                    Collections.sort(appsListAI, nameDscComparator);
                } else if (sortType == 1) {
                    Collections.sort(appsListAI, nameAscComparator);
                }
            }
            appsItemsList.clear();
            for (ApplicationInfo info : appsListAI){
                    AppsListItems appsListItem = new AppsListItems();

                    appsListItem.setAppName(packageManager.getApplicationLabel(info).toString());
                    appsListItem.setPackageName(info.packageName);

                    appsItemsList.add(appsListItem);
            }

            Log.d("First item is:", appsItemsList.get(0).getAppName());
            mAdapter = new AppsListRecyclerAdapter(getContext(), appsItemsList, selectedApps, ca);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            mRecyclerView.setAdapter(mAdapter);
            progress.dismiss();
//            if(listPosition != -1) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        getListView().setSelection(listPosition);
//                    }
//                }, 0);
//            }

            try {
                selectedAmountHolder.setText(String.valueOf(selectedApps.size())+ "/" + String.valueOf(appsListAI.size()));
            }
            catch (NullPointerException error){
                Log.e(TAG, error.getMessage());
            }
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

    private Comparator<ApplicationInfo> nameDscComparator = new Comparator<ApplicationInfo>() {
        @Override
        public int compare(ApplicationInfo app1, ApplicationInfo app2) {
            return app1.loadLabel(packageManager).toString().trim().compareToIgnoreCase(app2.loadLabel(packageManager).toString().trim());
        }
    };
    private Comparator<ApplicationInfo> nameAscComparator = new Comparator<ApplicationInfo>() {
        @Override
        public int compare(ApplicationInfo app1, ApplicationInfo app2) {
            return app2.loadLabel(packageManager).toString().trim().compareToIgnoreCase(app1.loadLabel(packageManager).toString().trim());
        }
    };

}