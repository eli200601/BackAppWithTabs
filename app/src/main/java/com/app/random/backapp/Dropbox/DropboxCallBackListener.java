package com.app.random.backapp.Dropbox;


import com.app.random.backapp.Recycler.AppDataItem;

import java.util.ArrayList;

public interface DropboxCallBackListener {

    void onUserNameReceived();
    void onFinishUploadFiles();
    void onFinishGeneratingCloudList(ArrayList<AppDataItem> arrayList);
    void onFinishDeletingFiles();
    void onFileUploadProgress(int percentage, long bytes, long total, AppDataItem app);

}
