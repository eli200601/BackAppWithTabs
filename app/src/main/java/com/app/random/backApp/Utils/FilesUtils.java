package com.app.random.backApp.Utils;


import android.content.Context;

import com.app.random.backApp.Recycler.AppDataItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FilesUtils {

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
}
