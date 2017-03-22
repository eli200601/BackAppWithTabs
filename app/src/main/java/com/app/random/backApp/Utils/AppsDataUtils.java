package com.app.random.backApp.Utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.app.random.backApp.Recycler.AppDataItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class AppsDataUtils {

    private static String TAG = "AppsDataUtils";
    private ArrayList<ApplicationInfo> appsListInfo = new ArrayList<>();
    private ArrayList<AppDataItem> appsListData = new ArrayList<>();


    private String PACKAGE_NAME;
    private int sortType = 0; // 1 = Dsc | 0 = Asc
    private int listSize;
    PackageManager packageManager;


    //Constructor
    public AppsDataUtils(PackageManager packageManager, String appPackageName, int sortType) {
        this.packageManager = packageManager;
        this.PACKAGE_NAME = appPackageName;
        this.sortType = sortType;

    }


    public void startGettingInfo(){

        appsListInfo = new ArrayList(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
        Log.d(TAG, appsListInfo.toString());

        appsListInfo = filterApplicationList(appsListInfo);
        Log.d(TAG, appsListInfo.toString());
        appsListInfo = sortApplicationList(appsListInfo);
        Log.d(TAG, appsListInfo.toString());

        listSize = appsListInfo.size();

        createAppDataItemList();

    }

    public ArrayList<AppDataItem> updateSort(int sortType) {
        this.sortType = sortType;
        appsListInfo = sortApplicationList(appsListInfo);
        appsListData.clear();
        createAppDataItemList();
        return appsListData;
    }

    public void createAppDataItemList() {

        String name;
        String packageName;
        String sourceDir;


        for (ApplicationInfo info : appsListInfo){

            name = packageManager.getApplicationLabel(info).toString();
            packageName = info.packageName;
            sourceDir = info.sourceDir;

            AppDataItem appsListItem = new AppDataItem(name,packageName);

            appsListData.add(appsListItem);
        }

    }

    public ArrayList<AppDataItem> getFilteredListByString(String query) {
        String name;
        String packageName;
        appsListData.clear();

        for (ApplicationInfo info : appsListInfo){

            name = packageManager.getApplicationLabel(info).toString();
            packageName = info.packageName;
//            sourceDir = info.sourceDir;

            if(name.toLowerCase().contains(query.toLowerCase())) {
                AppDataItem appsListItem = new AppDataItem(name,packageName);
                appsListData.add(appsListItem);
            }

        }
        return appsListData;
    }

    public ArrayList<AppDataItem> getAppDataList() {
        return appsListData;
    }

    public ArrayList<ApplicationInfo> getAppInfoList() { return appsListInfo; }


    public int getAppsListInfoListSize(){
        return appsListInfo.size();
    }



    public ArrayList<ApplicationInfo> filterApplicationList(ArrayList<ApplicationInfo> appsListInfo) {
//        This method filter all system apps and this app package name

        ArrayList<ApplicationInfo> appsList = new ArrayList<>();
        for (ApplicationInfo info : appsListInfo) {
            try {
                if ((info.flags & info.FLAG_SYSTEM) == 0) {
                    Log.d("Package1: ", "PACKEG_NAME=" + PACKAGE_NAME + " info=" + info.packageName);
                    if ( !info.packageName.equals(PACKAGE_NAME) ) {
                        appsList.add(info);
                    }
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
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return appsList;
    }

    public ArrayList<ApplicationInfo> sortApplicationList(ArrayList<ApplicationInfo> appsListInfo) {
        if (appsListInfo != null) {
            if (sortType == 0 ) {
                Collections.sort(appsListInfo, nameDscComparator);
            } else if (sortType == 1) {
                Collections.sort(appsListInfo, nameAscComparator);
            }
        }
        return appsListInfo;
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

