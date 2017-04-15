package com.app.random.backApp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;

import com.app.random.backApp.Recycler.AppDataItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import static com.app.random.backApp.Dropbox.DropBoxManager.deCamelCasealize;


public class AppsDataUtils {

    private static String TAG = "AppsDataUtils";
    private ArrayList<ApplicationInfo> appsListInfo = new ArrayList<>();
    private ArrayList<AppDataItem> appsListData = new ArrayList<>();
    private ArrayList<AppDataItem> folderAppsList = new ArrayList<>();



    private String PACKAGE_NAME;
    private int sortType = 0; // 1 = Dsc | 0 = Asc
    private int listSize;
    PackageManager packageManager;
    private Context context;

    private static AppsDataUtils instance;

    //Constructor
    private AppsDataUtils(Context context) {
        this.context = context;
        //Refactor sortType name
        this.sortType = SharedPrefsUtils.getIntegerPreference(context, Keys.SORT_TYPE_INSTALLED_APPS, 0);
        this.packageManager = context.getPackageManager();
        this.PACKAGE_NAME = context.getPackageName();

    }

    public static AppsDataUtils getInstance(Context context) {
        if (instance == null) {
            instance = new AppsDataUtils(context);
        }
        return instance;
    }

    public void startGettingInfo(){

        appsListInfo = new ArrayList<>(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
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
        String version = null;
        boolean isCloudApp = false;


        for (ApplicationInfo info : appsListInfo){

            name = packageManager.getApplicationLabel(info).toString();
            packageName = info.packageName;
            sourceDir = info.sourceDir;

            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                version = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "*** adding application to list ***");
            Log.d(TAG, name + packageName + sourceDir + version + String.valueOf(isCloudApp));
            File apk = new File(sourceDir);
            AppDataItem appsListItem = new AppDataItem(name,packageName,sourceDir, version, isCloudApp);
            appsListItem.setApkSize(Formatter.formatShortFileSize(context, apk.length()));
            appsListData.add(appsListItem);
        }

    }

    public ArrayList<AppDataItem> getAPKArrayListFromPackageNames(HashSet<String> packageList) {
        ArrayList<AppDataItem> apkPathList = new ArrayList<>();


        for (String packageName: packageList) {
            AppDataItem item = getAppInfoToUpload(packageName);
            if (item != null) {
                apkPathList.add(item);
            }
        }
        return apkPathList;

    }

    public AppDataItem getAppInfoToUpload(String packageName) {
        for (AppDataItem item: appsListData) {
            if (item.getPackageName().equals(packageName)) {
                return item;
            }
        }
        return null;
    }


    public String getAPKPathFromPackageName(String packageName) {
        for (ApplicationInfo info : appsListInfo){
            if (info.packageName.equals(packageName)) {
                return info.sourceDir;
            }
        }
        return null;
    }

    public ArrayList<AppDataItem> getFilteredListByString(String query) {
        String name;
        String packageName;
        String sourceDir;
        String version = null;
        String apkSize;
        boolean isCloudApp = false;

        ArrayList<AppDataItem> newList = new ArrayList<>();
        //TOdo: need to change it to modify the current list source, from appListData
        for (AppDataItem info : appsListData){

            name = info.getName();
            packageName = info.getPackageName();
            sourceDir = info.getSourceDir();
            apkSize = info.getApkSize();
            version = info.getAppVersion();

            if(name.toLowerCase().contains(query.toLowerCase())) {
                AppDataItem appsListItem = new AppDataItem(name, packageName, sourceDir , version, false);
                appsListItem.setApkSize(apkSize);
                newList.add(appsListItem);
            }

        }

//        appsListData.clear();
//        appsListData = newList;
        return newList;
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
                    Log.d("Package1: ", "PACKAGE_NAME=" + PACKAGE_NAME + " info=" + info.packageName);
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

    private Comparator<AppDataItem> nameDscComparatorAppDataItem = new Comparator<AppDataItem>() {
        @Override
        public int compare(AppDataItem app1, AppDataItem app2) {
            return app1.getName().trim().compareToIgnoreCase(app2.getName().trim());
        }
    };
    private Comparator<AppDataItem> nameAscComparatorAppDataItem = new Comparator<AppDataItem>() {
        @Override
        public int compare(AppDataItem app1, AppDataItem app2) {
            return app2.getName().trim().compareToIgnoreCase(app1.getName().trim());
        }
    };

    public ArrayList<AppDataItem> getFolderAppsList() {

        String pathLocal = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String separator = "_";

        folderAppsList.clear();

        Log.d("Files", "Path: " + pathLocal);
        File directory = new File(pathLocal);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (File file : files) {
            Log.d("Files", "FileName:" + file.getName());
            Log.d("Files", "Path:" + file.getPath());
            if (file.getName().contains(".apk")) {
                String appName = null;
                String appPackageName = null;
                String appVersion = null;
                String[] fileNameOutput = null;
                String fileSize = null;
                String filePath = null;

                fileNameOutput = file.getName().split(separator);
                try {
                    appName = deCamelCasealize(fileNameOutput[0].trim());
                    appPackageName = fileNameOutput[1].trim();
                    appVersion = fileNameOutput[2].trim().replace(".apk", "");
                    fileSize = Formatter.formatFileSize(context, file.length());
                    filePath = file.getPath();
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Cannot resolve file type");
                }
                Log.d(TAG, "appName = " + appName);
                Log.d(TAG, "appPackageName = " + appPackageName);
                Log.d(TAG, "appVersion = " + appVersion);
                Log.d(TAG, "path = " + filePath);
                if (appName != null && appPackageName != null && filePath != null && appVersion != null && fileSize != null) {
                    AppDataItem appItem = new AppDataItem(appName, appPackageName, filePath, appVersion, false);
                    appItem.setApkSize(fileSize);
                    folderAppsList.add(appItem);
                }
            }
        }
        if (folderAppsList != null) {
            if (sortType == 0 ) {
                Collections.sort(folderAppsList, nameDscComparatorAppDataItem);
            } else if (sortType == 1) {
                Collections.sort(folderAppsList, nameAscComparatorAppDataItem);
            }
        }
        return folderAppsList;
    }

}

