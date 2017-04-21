package com.app.random.backApp.Utils;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.app.random.backApp.Recycler.AppDataItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FilesUtils {
    private final String TAG = "FilesUtils";
    private static FilesUtils instance;
    private static Context context;


    public static FilesUtils getInstance(Context context) {
        if (instance == null) {
            instance = new FilesUtils(context);
        }
        return instance;
    }

    private FilesUtils(Context context) {
        this.context = context;


    }

    private FilesUtils() { }

    public String getJSONStringFromArray(ArrayList<AppDataItem> appsList) {
        Gson gson = new Gson();
        String dataListJson = gson.toJson(appsList);
        return dataListJson;
    }

    public ArrayList<AppDataItem> getArrayFromJSONString(String stringJSON) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<AppDataItem>>(){}.getType();
        ArrayList<AppDataItem> appsList = gson.fromJson(stringJSON, type);
        return appsList;
    }

    public long getFileSizeFromListArray(ArrayList<AppDataItem> arrayList) {
        long totalFilesSize = 0;

        for (AppDataItem item : arrayList) {
            File temp = new File(item.getSourceDir());
            totalFilesSize = totalFilesSize + temp.length();
        }

        return totalFilesSize;
    }

    public boolean deleteFilesFromArray(ArrayList<AppDataItem> list) {
        boolean result = true;
        for (AppDataItem item: list) {
            String path = item.getSourceDir();
            File file = new File(path);
            boolean deleted = file.delete();
            if (deleted == false) {
                result = false;
            }
        }
        return result;
    }

    public boolean installFilesFromArray(ArrayList<AppDataItem> list) {
        boolean result = true;
        for (AppDataItem item: list) {
            File toInstall = new File(item.getSourceDir());
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.fromFile(toInstall), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        }
        return result;
    }

    public void uninstallAppFromList( ArrayList<AppDataItem> list) {
        for (AppDataItem item: list) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + item.getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }
}
