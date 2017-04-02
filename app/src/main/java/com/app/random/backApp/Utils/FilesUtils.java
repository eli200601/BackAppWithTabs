package com.app.random.backApp.Utils;


import android.content.Context;

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
}
