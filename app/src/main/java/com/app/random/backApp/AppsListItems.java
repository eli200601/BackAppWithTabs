package com.app.random.backApp;


// Should be app data info - in list
public class AppsListItems {

    private String appName;
    private String packageName;
    private boolean checkBox;

//    (ImageView) appIcon;
//    private String
    public String getAppName(){
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean getCheckBox() {return checkBox; }

    public void setCheckBox(boolean checkBox) { this.checkBox = checkBox; }

//    public Drawable getAppIcon() { return appIcon; }
//
//    public void setAppIcon(Drawable appIcon) { this.appIcon = appIcon; }



}
