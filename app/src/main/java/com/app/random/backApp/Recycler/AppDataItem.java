package com.app.random.backApp.Recycler;


import java.io.Serializable;

public class AppDataItem implements Serializable {

    private String name;
    private String packageName;
    private String sourceDir;
    private String appVersion;
    private String apkSize;
    private boolean isCloudApp;

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public AppDataItem(String name, String packageName, String sourceDir) {
        this.name = name;
        this.packageName = packageName;
        this.sourceDir = sourceDir;
    }

    public AppDataItem(String name, String packageName, String sourceDir, String appVersion, boolean isCloudApp) {
        this.name = name;
        this.packageName = packageName;
        this.sourceDir = sourceDir;
        this.appVersion = appVersion;
        this.isCloudApp = isCloudApp;

    }

    public String getApkSize() {
        return apkSize;
    }

    public void setApkSize(String apkSize) {
        this.apkSize = apkSize;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isCloudApp() {
        return isCloudApp;
    }

    public void setCloudApp(boolean cloudApp) {
        isCloudApp = cloudApp;
    }
}
