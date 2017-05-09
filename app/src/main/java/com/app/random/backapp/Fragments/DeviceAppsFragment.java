package com.app.random.backapp.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.random.backapp.Activitys.AppInfoDialogActivity;
import com.app.random.backapp.Dropbox.DropBoxManager;
import com.app.random.backapp.Dropbox.DropboxCallBackListener;
import com.app.random.backapp.R;
import com.app.random.backapp.Receiver.MessageEvent;
import com.app.random.backapp.Recycler.AppDataItem;
import com.app.random.backapp.Recycler.MyRecyclerAdapter;
import com.app.random.backapp.Recycler.UpdateBottomBar;
import com.app.random.backapp.Services.DropboxUploadIntentService;
import com.app.random.backapp.Utils.AppsDataUtils;
import com.app.random.backapp.Utils.ConnectionDetector;
import com.app.random.backapp.Utils.FilesUtils;
import com.app.random.backapp.Utils.Keys;
import com.app.random.backapp.Utils.SharedPrefsUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;


public class DeviceAppsFragment  extends Fragment implements SearchView.OnQueryTextListener, DropboxCallBackListener, SharedPreferences.OnSharedPreferenceChangeListener{
//    private PackageChangeReceiver packageChangeReceiver;
//    private LocalBroadcastManager localBroadcastManager;
    private ArrayList<ApplicationInfo> appsListInfo =  new ArrayList<>();
    private ArrayList<AppDataItem> appsListData;

    private EventBus bus = EventBus.getDefault();
    private MessageEvent.OnFinishLoadinfIcons event;

    HashSet<String> cloudAppsList;

    private static final String TAG = "DeviceAppsFragment";
    private FilesUtils filesUtils;
    private AppsDataUtils appsDataUtils;
    public int sortType = 0; // 1 = Dsc | 0 = Asc

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private TextView listAmountTextField;
    private TextView selectedAmountTextField;

    private DropBoxManager dropBoxManager = null;
    private ViewTreeObserver.OnGlobalLayoutListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dropBoxManager = DropBoxManager.getInstance(getActivity().getApplicationContext());
        filesUtils = FilesUtils.getInstance(getActivity().getApplicationContext());
        Log.d(TAG, "Building this Fragment!!!!");
        appsListData = new ArrayList<>();
        cloudAppsList = new HashSet<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
//        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps_list, null);

        setHasOptionsMenu(true);

        appsDataUtils = AppsDataUtils.getInstance(getActivity().getApplicationContext());

        //Bottom Bar init
        listAmountTextField = (TextView) view.findViewById(R.id.itemsInListValueText);
        selectedAmountTextField = (TextView) view.findViewById(R.id.ItemsSelectedValueText);

        //RecyclerView - Apps list
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setRecyclerLayoutType();


        new LoadApplications().execute();


        return view;
    }





    public void setRecyclerLayoutType(){
//        mRecyclerView.setLayoutManager(null);
        String prefViewType = SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_DEVICE);
        Log.d(TAG, "setRecyclerLayoutType(): " + SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_DEVICE));
        Log.d(TAG, "onFinishGeneratingCloudList Started appsListData = " + String.valueOf(appsListData.size()));
        switch (prefViewType) {
            case Keys.PREF_VIEWTYPE_LIST: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);


                break;
            }
            case Keys.PREF_VIEWTYPE_CARD: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
                break;
            }
            case Keys.PREF_VIEWTYPE_GRID: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), Keys.NUMBER_OF_COLUMNS));
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
                break;
            }
        }
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick() {
                updateBottomBar();
            }

            @Override
            public void onShareAPKButtonClick(AppDataItem app) {
                //Open Info Dialog
                infoDialog(app);
            }

            @Override
            public void onDownloadAPKButtonClick(AppDataItem app) {
                //Upload APK !!!
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");

                    ArrayList<AppDataItem> itemsToUpload = new ArrayList<>();


                    itemsToUpload.add(app);
                    long totalUploadSize = filesUtils.getFileSizeFromListArray(itemsToUpload);
                    long cloudFreeSpace = SharedPrefsUtils.getLongPreference(getActivity().getApplicationContext(), Keys.DROPBOX_FREE_SPACE_LONG, totalUploadSize);
                    if ((cloudFreeSpace - totalUploadSize) < 0) {
                        Log.e(TAG, "There is no free space on cloud...");
                        Snackbar.make(getView(), "Upload failed. There is no free space on cloud...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else {
                        Snackbar.make(getView(), "Starting to upload " + String.valueOf(itemsToUpload.size()) + " applications...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Keys.APPS_UPLOAD_ARRAYLIST, itemsToUpload);

                        Intent intent = new Intent(getActivity().getApplicationContext(), DropboxUploadIntentService.class);
                        intent.putExtras(bundle);

                        Log.d(TAG, "Starting the intent....");

                        getActivity().startService(intent);

//                        mAdapter.clearSelectedList();
//                        mAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    Log.d(TAG, "There is no connection...");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
            @Override
            public void onDeleteAPKButtonClick(AppDataItem app) {
                //Uninstall button in card
                ArrayList<AppDataItem> uninstallList = new ArrayList<>();
                uninstallList.add(app);
                filesUtils.uninstallAppFromList(uninstallList);
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
            }
        });
        mAdapter.setItems(appsListData);

        mAdapter.setCloudSavedList(cloudAppsList);
        Log.d(TAG, "Setting up cloud list = " + String.valueOf(mAdapter.getSelectedAppsListSize()));
        mAdapter.notifyDataSetChanged();
//        updateBottomBar();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.d(TAG, "Getting nuts");
        switch (key) {
            case Keys.PREF_VIEWTYPE_DEVICE: {
                if (mAdapter.getSelectedAppsListSize() > 0) {
                    cloudAppsList = mAdapter.getCloudSavedList();
                }
                setRecyclerLayoutType();
                break;
            }
        }
    }

    public void updateBottomBar() {
        Log.d(TAG, "updateBottomBar called");
        String appsListSize = String.valueOf(appsDataUtils.getAppsListInfoListSize());
        String selectedAppsListSize = String.valueOf(mAdapter.getSelectedAppsListSize());

        listAmountTextField.setText(appsListSize);
        selectedAmountTextField.setText(selectedAppsListSize + "/" + appsListSize);

        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apps_frag_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem uploadItem = menu.findItem(R.id.action_upload);
        MenuItem uninstallItem = menu.findItem(R.id.menuUninstallAPK);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);


        searchView.setOnQueryTextListener(this);

        if (mAdapter.getSelectedAppsListSize() > 0) {
            uninstallItem.setVisible(true);

        }
        else {
            uninstallItem.setVisible(false);

        }

        if (mAdapter.getSelectedAppsListSize() > 0 && dropBoxManager.isLogIn) {
            uploadItem.setVisible(true);
        }
        else {
            uploadItem.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_a_z: {
//               1 = Dsc | 0 = Asc
                SharedPrefsUtils.setIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE_INSTALLED_APPS, 0);
                appsListData = appsDataUtils.updateSort(0);
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_sort_z_a: {
//              1 = Dsc | 0 = Asc
                SharedPrefsUtils.setIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE_INSTALLED_APPS, 1);
                appsListData = appsDataUtils.updateSort(1);
                mAdapter.setItems(appsListData);
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuSelectAll: {
                mAdapter.setAllListSelected();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuUnSelectAll: {
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.action_upload: {
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
//                    IntentManager.getInstance();
                    HashSet<String> selectedPackageNameList;
                    ArrayList<AppDataItem> itemsToUpload;

                    selectedPackageNameList = mAdapter.getSelectedPackageNamesList();

                    Log.d(TAG, "Path ArrayList:::::::::::::::::::");
                    Log.d(TAG, appsDataUtils.getAPKArrayListFromPackageNames(selectedPackageNameList).toString());

                    itemsToUpload = appsDataUtils.getAPKArrayListFromPackageNames(selectedPackageNameList);
                    long totalUploadSize = filesUtils.getFileSizeFromListArray(itemsToUpload);
                    long cloudFreeSpace = SharedPrefsUtils.getLongPreference(getActivity().getApplicationContext(), Keys.DROPBOX_FREE_SPACE_LONG, totalUploadSize);
                    if ((cloudFreeSpace - totalUploadSize) < 0) {
                        Log.e(TAG, "There is no free space on cloud...");
                        Snackbar.make(getView(), "Upload failed. There is no free space on cloud...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                    else {
                        Snackbar.make(getView(), "Starting to upload " + selectedPackageNameList.size() + " applications...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Keys.APPS_UPLOAD_ARRAYLIST, itemsToUpload);



                        Intent intent = new Intent(getActivity().getApplicationContext(), DropboxUploadIntentService.class);
                        intent.putExtras(bundle);

                        Log.d(TAG, "Starting the intent....");

//                        IntentManager.getInstance().startUploadIntent(bundle,getContext());
                        getActivity().startService(intent);

                        mAdapter.clearSelectedList();
                        mAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    Log.d(TAG, "There is no connection...");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                break;
            }
            case R.id.menuUninstallAPK: {
                ArrayList<AppDataItem> uninstallList = mAdapter.getSelectedCustomArrayList();
                filesUtils.uninstallAppFromList(uninstallList);
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuRefreshDevice: {
                new LoadApplications().execute();
                break;
            }

            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }






    @Override
    public boolean onQueryTextSubmit(String query) {
        appsListData = appsDataUtils.getFilteredListByString(query);
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        appsListData = appsDataUtils.getFilteredListByString(newText);
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        dropBoxManager.removeDropboxListener(TAG);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        localBroadcastManager.unregisterReceiver(packageChangeReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Adding " + TAG + " TO Listener List");
        dropBoxManager.addDropboxListener(this, TAG);
//        localBroadcastManager.registerReceiver(packageChangeReceiver, new IntentFilter(Keys.BC_ON_FINISH_UNINSTALL));

    }

    @Override
    public void onUserNameReceived() {

    }

    @Override
    public void onFinishUploadFiles() {

    }

    public boolean isInstalled(String packageName) {
        for (AppDataItem item: appsListData) {
            if (packageName.equals(item.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFinishGeneratingCloudList(ArrayList<AppDataItem> arrayList) {
        cloudAppsList.clear();
        Log.d(TAG, "onFinishGeneratingCloudList Started arrayList = " + String.valueOf(arrayList.size()));
        Log.d(TAG, "onFinishGeneratingCloudList Started appsListData = " + String.valueOf(appsListData.size()));
        for (AppDataItem cloudApp: arrayList) {
            if (isInstalled(cloudApp.getPackageName()))
                    cloudAppsList.add(cloudApp.getPackageName());

        }
        //??
        Log.d(TAG, " Done loading list from cloud - List size is: " + String.valueOf(cloudAppsList.size()));
//        setRecyclerLayoutType();
        mAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    @Override
    public void onFinishDeletingFiles() {

    }

    @Override
    public void onFileUploadProgress(final int percentage, long bytes, long total, final AppDataItem app) {
        Log.d(TAG, "Starting onFileUploadProgress");
//        mAdapter.updateUploadProgress(percentage,bytes,total,app);
        if (getActivity() != null && mAdapter.getItemCount() > 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"Setting progress for item: " + String.valueOf(percentage));
                    mAdapter.appsListData.get(mAdapter.getItemPosition(app)).setProgress(percentage);
//                    mAdapter.notifyDataSetChanged();
//                    mAdapter.printINTOfList();
                    mAdapter.notifyItemChanged(mAdapter.getItemPosition(app));
//                    mAdapter.
                }
            });
        }



    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            appsDataUtils.startGettingInfo();
            appsListData = new ArrayList<>();
            appsListInfo = appsDataUtils.getAppInfoList();
            appsListData = appsDataUtils.getAppDataList();
            appsDataUtils.generateIconsList();
            Log.d(TAG,"appsListData = " + String.valueOf(appsListData.size()) + "appsListInfo = " + String.valueOf(appsListInfo.size()));

//            updateBottomBar();
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
//            mRecyclerView.setAdapter(mAdapter);
            progress.dismiss();
            Log.d(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            mAdapter.setItems(appsListData);
            if (dropBoxManager.cloudAppsList.size() > 0) {
                onFinishGeneratingCloudList(dropBoxManager.cloudAppsList);
            }


            Log.d("EventBus", "Sending undate request to cloud");

            event = new MessageEvent.OnFinishLoadinfIcons("Done");
            bus.post(event);

            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "LoadApplications Started appsListData = " + String.valueOf(appsListData.size()));
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getActivity(), null,
                    "Loading application info...");

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public void infoDialog(AppDataItem app) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("AppInfo", app);

        int[] originalPos = new int[2];
        int[] originalIconPos = new int[2];

        ImageView childImageView;
        TextView childTextView;
        Button childButtonView;

        View app_icon = null;
        View app_name = null;
        View app_size = null;
        View appShareAPK = null;
        View appUploadAPK = null;
        View appDeleteAPK = null;
        View app_version = null;

        // Getting the item x,y
        View view_item = mRecyclerView.getLayoutManager().findViewByPosition(mAdapter.getItemPosition(app));
//        view_item.setVisibility(View.GONE);

        boolean found = false;

        ArrayList<View> allViewsWithinMyTopView = getAllChildren(view_item);
        for (View child : allViewsWithinMyTopView) {
            if (child instanceof ImageView) {
                childImageView = (ImageView) child;
                if (childImageView.getId() == R.id.app_icon) {
                    found = true;
                    app_icon = child;
                }
            } else {
                if (child instanceof TextView) {
                    childTextView = (TextView) child;
                    if (childTextView.getId() == R.id.app_name) {
                        app_name = child;
                        Log.d(TAG, "Found App name = " + childTextView.getText());
                    } else {
                        if (childTextView.getId() == R.id.item_size) {
                            app_size = child;
                            Log.d(TAG, "Found Size = " + childTextView.getText());
                        } else {
                            if (childTextView.getId() == R.id.item_version) {
                                app_version = child;
                                Log.d(TAG, "Found App Version = " + childTextView.getText());
                            } else {
                                childButtonView = (Button) child;
                                if (childButtonView.getId() == R.id.share_apk_card_action) {
                                    appShareAPK = child;
                                    Log.d(TAG, "Found Share button");
                                } else {
                                    if (childButtonView.getId() == R.id.download_apk_card_action) {
                                        appUploadAPK = child;
                                        Log.d(TAG, "Found Download button");
                                    } else {
                                        if (childButtonView.getId() == R.id.delete_apk_card_action) {
                                            appDeleteAPK = child;
                                            Log.d(TAG, "Found Uninstall button");
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        if (!found) {
            Log.d(TAG, "icon not found within TopView");
        } else {
            Log.d(TAG, "app_icon View found!!!");
        }
        //Checking if the icon is fully appear on screen
        Rect rect = new Rect();
        if(app_icon.getGlobalVisibleRect(rect) && app_icon.getHeight() == rect.height() && app_icon.getWidth() == rect.width() ) {
            Log.d(TAG, "Icon is fully visible on screen :)");

            app_icon.getLocationInWindow(originalIconPos);
            Log.d(TAG, "Icon X=" + String.valueOf(originalIconPos[0]) + " Y= " + String.valueOf(originalIconPos[1]));

            view_item.getLocationInWindow(originalPos);

            Intent dialogActivity = new Intent(getContext(), AppInfoDialogActivity.class);

            dialogActivity.putExtra("x", originalIconPos[0]); //Optional parameters
            dialogActivity.putExtra("y", originalIconPos[1]); //Optional parameters
            dialogActivity.putExtra("bundleAppInfo", bundle);
            Pair<View, String> iconPair = Pair.create(app_icon, "AppIcon");
            Pair<View, String> namePair = Pair.create(app_name, "AppName");
            Pair<View, String> sizePair = Pair.create(app_size, "AppSize");
            Pair<View, String> versionPair = Pair.create(app_version, "AppVersion");

            Pair<View, String> sharePair = Pair.create(appShareAPK, "AppShare");
            Pair<View, String> uploadPair = Pair.create(appUploadAPK, "AppDownload");
            Pair<View, String> deletePair = Pair.create(appDeleteAPK, "AppDelete");


            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), iconPair, namePair, sizePair, versionPair, sharePair, uploadPair, deletePair);
//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), app_icon, "AppIcon");

            this.startActivity(dialogActivity, options.toBundle());

        } else {
            Log.d(TAG, "Icon is NOT fully visible on screen :(");
            final int[] point = originalIconPos;
            final int x = originalIconPos[0];
            final int y = originalIconPos[1];
            final View new_app_icon = app_icon;
            final Bundle newBundle = bundle;
            
            listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    new_app_icon.getLocationInWindow(point);
                    Log.d(TAG, "Icon X=" + String.valueOf(x) + " Y= " + String.valueOf(y));

//                    view_item.getLocationInWindow(point);

                    Intent dialogActivity = new Intent(getContext(), AppInfoDialogActivity.class);

                    dialogActivity.putExtra("x", x); //Optional parameters
                    dialogActivity.putExtra("y", y); //Optional parameters
                    dialogActivity.putExtra("bundleAppInfo", newBundle);


                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), new_app_icon, "AppIcon");

                    startActivity(dialogActivity, options.toBundle());
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
                }
            };


            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
            mRecyclerView.scrollToPosition(mAdapter.getItemPosition(app));

        }


    }

    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }




//        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
//        View mView = View.inflate(getContext(),R.layout.app_info_dialog, null);
//
//        ImageView icon = (ImageView) mView.findViewById(R.id.dialog_app_icon);
//        TextView title = (TextView) mView.findViewById(R.id.dialog_title);
//        TextView version = (TextView) mView.findViewById(R.id.dialog_version);
//        TextView size = (TextView) mView.findViewById(R.id.dialog_file_size);
//        TextView packageName = (TextView) mView.findViewById(R.id.dialog_package);
//        TextView path = (TextView) mView.findViewById(R.id.dialog_path);
//        Button done = (Button) mView.findViewById(R.id.dialog_main_action);
//
//
//
//        try {
//            icon.setImageDrawable(getActivity().getApplicationContext().getPackageManager().getApplicationIcon(app.getPackageName()));
//
//        }
//        catch (PackageManager.NameNotFoundException error) {
//            Log.e(TAG, error.getMessage());
//            icon.setImageResource(R.mipmap.ic_launcher);
//        }
//
//        //                *************************************************
////        icon.setVisibility(View.GONE);
//        title.setVisibility(View.GONE);
//        version.setVisibility(View.GONE);
//        size.setVisibility(View.GONE);
//        packageName.setVisibility(View.GONE);
//        path.setVisibility(View.GONE);
//        done.setVisibility(View.GONE);
//
////        Animation anim_fade= AnimationUtils.loadAnimation(getContext(), R.anim.translate);
////        anim_fade.reset();
////        AnimationSet replaceAnimation = new AnimationSet(false);
////        // animations should be applied on the finish line
////        replaceAnimation.setFillAfter(true);
////
////        // create scale animation
////        ScaleAnimation scale = new ScaleAnimation(1.0f, originalPos[0], 1.0f, originalPos[1]);
////        scale.setDuration(1000);
////
//        int[] targetPos = new int[2];
//        icon.getLocationInWindow(targetPos);
////
////        // create translation animation
////        TranslateAnimation trans = new TranslateAnimation(0, 0,
////                TranslateAnimation.ABSOLUTE, targetPos[0], 0, 0,
////                TranslateAnimation.ABSOLUTE, targetPos[1]);
////        trans.setDuration(1000);
////
////        // add new animations to the set
////        replaceAnimation.addAnimation(scale);
////        replaceAnimation.addAnimation(trans);
////
////        // start our animation
////        icon.startAnimation(replaceAnimation);
//        icon.animate().scaleX(originalPos[0])
//                .scaleY(originalPos[1])
//                .x(targetPos[0])
//                .y(targetPos[1])
//                .setDuration(4000)
//                .start();
//        icon.setVisibility(View.VISIBLE);
//
////********************** Intro animation ************************************************
////        final ProgressBar bar = (ProgressBar) mView.findViewById(R.id.progressBar);
////
////        ValueAnimator animator = new ValueAnimator();
////        animator.setObjectValues(0, 80);
////        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
////            public void onAnimationUpdate(ValueAnimator animation) {
////                bar.setProgress((int) animation.getAnimatedValue());
////            }
////        });
////        animator.setEvaluator(new TypeEvaluator<Integer>() {
////            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
////                return Math.round(startValue + (endValue - startValue) * fraction);
////            }
////        });
////        animator.setDuration(2000);
////        animator.start();
////                *************************************************
//
//
//
//        title.setText(app.getName());
//        version.setText("App Version: " + app.getAppVersion());
//
//        size.setText("APK Size: " + app.getApkSize());
//        packageName.setText("Package Name: " + app.getPackageName());
//        path.setText("Path: " + app.getSourceDir());
//
//        mBuilder.setView(mView);
//        final AlertDialog dialog = mBuilder.create();
//        //set animation to dialog
////        dialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
//        done.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.cancel();
//            }
//        });
//        dialog.show();

//    }

//    public class PackageChangeReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "BroadcastReceiver - PackageChangeReceiver");
//            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
//                String packageName = intent.getDataString();
//                Log.d(TAG, "uninstall:" + packageName + "package name of the program");
//                Toast.makeText(context.getApplicationContext(), "Removed!", Toast.LENGTH_LONG).show();
//            }
//            new LoadApplications().execute();
//        }
//
//    }

}
