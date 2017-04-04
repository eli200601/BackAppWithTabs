package com.app.random.backApp.Dropbox;


import com.app.random.backApp.Recycler.AppDataItem;

import java.util.ArrayList;

public interface DropboxCallBackListener {

    void onUserNameReceived();
    void onFinishUploadFiles();
    void onFinishGeneratingCloudList(ArrayList<AppDataItem> arrayList);

}
