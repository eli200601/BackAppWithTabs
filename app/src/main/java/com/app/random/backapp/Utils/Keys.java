package com.app.random.backapp.Utils;


public class Keys {

    public static final String SORT_TYPE_INSTALLED_APPS = "sortType";
    public static final String ORIGIN_DEVICEAPPSFRAGMENT = "DeviceAppsFragment";
    public static final String ORIGIN_CLOUDMAINFRAGMENT = "CloudMainFragment";
    public static final String ORIGIN_DOWNLOADFOLDERFRAGMENT = "DownloadFolderFragment";

    public static final int NUMBER_OF_COLUMNS = 3;

    /*  SharedPreferences Key's   */
    public static final String PREF_NAME = "Settings";
    public static final String PREF_ACTIVATE = "Activate";
    public static final String PREF_VIEWTYPE = "view_preference";
    public static final String PREF_VIEWTYPE_DEVICE = "view_preference_device";
    public static final String PREF_VIEWTYPE_CLOUD = "view_preference_cloud";
    public static final String PREF_VIEWTYPE_FOLDER = "view_preference_folder";
    public static final String PREF_VIEWTYPE_LIST = "List View";
    public static final String PREF_VIEWTYPE_CARD = "Card View";
    public static final String PREF_VIEWTYPE_GRID = "Grid View";
    /* Broadcast names */
    public final static String BC_ON_FINISH_UPLOAD = "com.app.random.backApp.OnFinishUploadReceiver";
    public final static String BC_ON_FINISH_DOWNLOAD = "com.app.random.backApp.OnFinishDownloadReceiver";
    public final static String BC_ON_FINISH_UNINSTALL = "android.intent.action.PACKAGE_REMOVED";

    /*  DropBox info   */
    public final static String DROPBOX_FILE_DIR = "/Apps/BackAppFolder/";
    public final static String DROPBOX_NAME = "BackAppFolder";
    public final static String DROPBOX_APP_KEY = "r1qzs8cbwnhlnlb";
    public final static String DROPBOX_APP_SECRET = "abfz67yh01qz456";
    public final static String DROPBOX_ACCESS_TOKEN = "accessToken";

    public final static String DROPBOX_DISPLAY_NAME = "DisplayName";
    public final static String DROPBOX_USER_NAME = "UserName";
    public final static String DROPBOX_LAST_NAME = "LastName";
    public final static String DROPBOX_UID = "uid";
    public final static String DROPBOX_REFERRAL_URL = "referralURL";
    public final static String DROPBOX_TOTAL_SPACE = "Total_quota";
    public final static String DROPBOX_USED_SPACE = "Used_quota";

    public final static String DROPBOX_USED_SPACE_LONG = "Used_quota_long";
    public final static String DROPBOX_TOTAL_SPACE_LONG = "Total_quota_long";
    public final static String DROPBOX_FREE_SPACE_LONG = "Free_space_long";

    /* Service */
    public final static String APPS_UPLOAD_ARRAYLIST = "Apps_to_upload_arrayList";
    public final static String APPS_DOWNLOAD_ARRAYLIST = "Apps_to_download_arrayList";
    public final static String NOT_FINISH_UPLOAD_LIST = "NotFinishUploadList";
    public final static String SERVICE_UPLOAD_STATUS = "NotFinishUploadList";
    public final static String SERVICE_DOWNLOAD_STATUS = "Download_finish";
    public final static String STARTED_FROM = "Started_From";
    public final static String DROPBOX_UPLOAD_INTENT_SERVICE = "DropboxUploadIntentService";
    public final static String DROPBOX_DOWNLOAD_INTENT_SERVICE = "DropboxDownloadService";
    /* Se*/
}
