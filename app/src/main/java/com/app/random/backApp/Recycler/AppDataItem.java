package com.app.random.backApp.Recycler;


public class AppDataItem {

    private String name;
    private String packageName;
    private String sourceDir;
    private String appVersion;
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
