package com.app.random.backApp.Fragments;


import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.random.backApp.Dropbox.DropBoxManager;
import com.app.random.backApp.Dropbox.DropboxCallBackListener;
import com.app.random.backApp.R;
import com.app.random.backApp.Recycler.AppDataItem;
import com.app.random.backApp.Recycler.MyRecyclerAdapter;
import com.app.random.backApp.Recycler.UpdateBottomBar;
import com.app.random.backApp.Services.DropboxDownloadService;
import com.app.random.backApp.Services.DropboxUploadIntentService;
import com.app.random.backApp.Utils.AppsDataUtils;
import com.app.random.backApp.Utils.ConnectionDetector;
import com.app.random.backApp.Utils.FilesUtils;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;
import com.dropbox.client2.DropboxAPI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CloudMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CloudMainFragment extends Fragment implements DropboxCallBackListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private OnFinishUploadReceiver onFinishUploadReceiver;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

//    private LocalBroadcastManager localBroadcastManager;
    private static final String TAG = "CloudMainFragment";

    private DropBoxManager dropBoxManager = null;

    private MyRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private AppsDataUtils appsDataUtils;

    private FilesUtils filesUtils;

    private ArrayList<AppDataItem> appsListData = new ArrayList<>();

    public int sortType = 0; // 1 = Dsc | 0 = Asc
    private TextView listAmountTextField;
    private TextView selectedAmountTextField;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public CloudMainFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static CloudMainFragment newInstance(String param1, String param2) {
        CloudMainFragment fragment = new CloudMainFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onFinishUploadReceiver = new OnFinishUploadReceiver();
        setHasOptionsMenu(true);

        dropBoxManager = DropBoxManager.getInstance(getActivity().getApplicationContext());
        filesUtils = FilesUtils.getInstance(getActivity().getApplicationContext());
        dropBoxManager.loginDropbox();

        appsDataUtils = AppsDataUtils.getInstance(getActivity().getApplicationContext());

        checkIfUploadDisrupted();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
//        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_main, container, false);

        sortType = SharedPrefsUtils.getIntegerPreference(getActivity().getApplicationContext(), Keys.SORT_TYPE_INSTALLED_APPS, 0);

        //RecyclerView - Apps list
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_cloud);
        setRecyclerLayoutType();
        listAmountTextField = (TextView) view.findViewById(R.id.itemsInListValueText_cloud);
        selectedAmountTextField = (TextView) view.findViewById(R.id.ItemsSelectedValueText_cloud);
        return view;
    }

    public void setRecyclerLayoutType() {
        String prefViewType = SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_CLOUD);
        Log.d(TAG, "setRecyclerLayoutType(): " + SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.PREF_VIEWTYPE_CLOUD));
        switch (prefViewType) {
            case Keys.PREF_VIEWTYPE_LIST: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(), appsListData, TAG);
                mRecyclerView.setAdapter(mAdapter);
                break;
            }
            case Keys.PREF_VIEWTYPE_CARD: {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(), appsListData, TAG);
                mRecyclerView.setAdapter(mAdapter);
                break;
            }
            case Keys.PREF_VIEWTYPE_GRID: {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), Keys.NUMBER_OF_COLUMNS));
                mRecyclerView.setHasFixedSize(true);
                mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(), appsListData, TAG);
                mRecyclerView.setAdapter(mAdapter);
                break;
            }
        }
        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
            @Override
            public void onCheckBoxClick() {
                updateBottomBar();
            }

            @Override
            public void onShareAPKButtonClick(AppDataItem app) {
                //Do Stuff hare
                Log.d(TAG, "Here is onShareAPKButtonClick");
                new GetShareAPKURL().execute(app);
            }

            @Override
            public void onDownloadAPKButtonClick(AppDataItem app) {
                Log.d(TAG, "Here is onDownloadAPKButtonClick");
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
                    Bundle bundle = new Bundle();
                    ArrayList<AppDataItem> list = new ArrayList<AppDataItem>();
                    list.add(app);
                    bundle.putSerializable(Keys.APPS_DOWNLOAD_ARRAYLIST, list);
                    Intent intent = new Intent(getActivity().getApplicationContext(), DropboxDownloadService.class);
                    intent.putExtras(bundle);
                    mAdapter.clearSelectedList();
                    mAdapter.notifyDataSetChanged();
                    Snackbar.make(getView(), "Starting to download the selected files", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Log.d(TAG, "Starting the intent....");
                    getActivity().startService(intent);
                }
                else {
                    Log.d(TAG, "There is NO connection!");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }

            @Override
            public void onDeleteAPKButtonClick(AppDataItem app) {
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
                    ArrayList<AppDataItem> list = new ArrayList<AppDataItem>();
                    list.add(app);
                    dropBoxManager.deleteFileListFromCloud(list);
                    mAdapter.clearSelectedList();
                }
                else {
                    Log.d(TAG, "There is NO connection!");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
//        updateBottomBar();
    }
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
//        mAdapter = new MyRecyclerAdapter(this.getActivity().getApplicationContext(),appsListData, TAG);
//        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.setUpdateBottomBar(new UpdateBottomBar() {
//            @Override
//            public void onCheckBoxClick() {
//                updateBottomBar();
//            }
//        });
//}
    public void updateBottomBar() {

        String appsListSize = String.valueOf(dropBoxManager.cloudAppsList.size());
        String selectedAppsListSize = String.valueOf(mAdapter.getSelectedAppsListSize());

        listAmountTextField.setText(appsListSize);
        selectedAmountTextField.setText(selectedAppsListSize + "/" + appsListSize);

        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.d(TAG, "Here i am now after change");
        switch (key) {
            case Keys.PREF_VIEWTYPE_CLOUD: {
                setRecyclerLayoutType();
                break;
            }
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cloud_frag_menu, menu);

        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
//        MenuItem sort_a_zItem = menu.findItem(R.id.action_sort_a_z_cloud);
//        MenuItem sort_z_aItem = menu.findItem(R.id.action_sort_z_a_cloud);
        MenuItem selectAllItem = menu.findItem(R.id.menuSelectAll_cloud);
        MenuItem unSelectAllItem = menu.findItem(R.id.menuUnSelectAll_cloud);
        MenuItem downloadItem = menu.findItem(R.id.action_download);


        Log.d(TAG, "dropbox is login? " + String.valueOf(dropBoxManager.isLoginToDropbox()));
        downloadItem.setVisible(dropBoxManager.isLoginToDropbox() && (mAdapter.getSelectedAppsListSize() > 0) );
        refreshItem.setVisible(dropBoxManager.isLoginToDropbox());
        deleteItem.setVisible(dropBoxManager.isLoginToDropbox() && (mAdapter.getSelectedAppsListSize() > 0));
//        sort_a_zItem.setVisible(dropBoxManager.isLoginToDropbox());
//        sort_z_aItem.setVisible(dropBoxManager.isLoginToDropbox());
        selectAllItem.setVisible(dropBoxManager.isLoginToDropbox());
        unSelectAllItem.setVisible(dropBoxManager.isLoginToDropbox());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh: {
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
                    dropBoxManager.getDropBoxFileListMethod();
                    mAdapter.clearSelectedList();
                    mAdapter.notifyDataSetChanged();
                }
                else {
                    Log.d(TAG, "There is NO connection!");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }

                break;
            }
//            case R.id.action_sort_a_z_cloud: {
//
//                break;
//            }
//            case R.id.action_sort_z_a: {
//
//                break;
//            }
            case R.id.menuSelectAll_cloud: {
                mAdapter.setAllListSelected();
                mAdapter.notifyDataSetChanged();
                break;
            }
            case R.id.menuUnSelectAll_cloud: {
                mAdapter.clearSelectedList();
                mAdapter.notifyDataSetChanged();
                break;
            }


            case R.id.action_delete: {
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
                    if (mAdapter.getSelectedAppsListCloud().size() > 0) {
                        dropBoxManager.deleteFileListFromCloud(mAdapter.getSelectedAppsListCloud());
                        mAdapter.clearSelectedList();
                    }
                }
                else {
                    Log.d(TAG, "There is NO connection!");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }


                //Continue here !!!!!!!!!!!!!!!!!!!!!!!!!!
            }
            case R.id.action_download: {
                if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
                    Log.d(TAG, "There is connection...");
                    ArrayList<AppDataItem> itemsToDownload;
                    itemsToDownload = mAdapter.getSelectedAppsListCloud();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Keys.APPS_DOWNLOAD_ARRAYLIST, itemsToDownload);
                    Intent intent = new Intent(getActivity().getApplicationContext(), DropboxDownloadService.class);
                    intent.putExtras(bundle);
                    mAdapter.clearSelectedList();
                    mAdapter.notifyDataSetChanged();
                    Snackbar.make(getView(), "Starting to download the selected files", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Log.d(TAG, "Starting the intent....");
                    getActivity().startService(intent);
                }
                else {
                    Log.d(TAG, "There is NO connection!");
                    Snackbar.make(getView(), "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }

            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    @Override
    public void onPause() {
        super.onPause();
        dropBoxManager.removeDropboxListener(TAG);
        getActivity().unregisterReceiver(onFinishUploadReceiver);
//        localBroadcastManager.unregisterReceiver(onFinishUploadReceiver);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Adding " + TAG + " TO Listener List");
        dropBoxManager.addDropboxListener(this, TAG);
//        localBroadcastManager.registerReceiver(onFinishUploadReceiver, new IntentFilter(Keys.BC_ON_FINISH_UPLOAD));
        getActivity().registerReceiver(onFinishUploadReceiver, new IntentFilter(Keys.BC_ON_FINISH_UPLOAD));

    }



    @Override
    public void onUserNameReceived() {

    }

    @Override
    public void onFinishUploadFiles() {
        if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
            Log.d(TAG, "There is connection...");
            dropBoxManager.getDropBoxFileListMethod();
            mAdapter.clearSelectedList();
            mAdapter.notifyDataSetChanged();
            updateBottomBar();
            dropBoxManager.loadUserInfo();
        }
    }

    @Override
    public void onFinishGeneratingCloudList(ArrayList<AppDataItem> cloudList) {
        appsListData = cloudList;
        mAdapter.setItems(appsListData);
        mAdapter.notifyDataSetChanged();
        updateBottomBar();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onFinishDeletingFiles() {
        View view = getView();
        assert view != null;
        Snackbar.make(view, "Files deleted...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        dropBoxManager.getDropBoxFileListMethod();
        updateBottomBar();
    }

    @Override
    public void onFileUploadProgress(int percentage, long bytes, long total, AppDataItem app) {

    }



    public void updateUIList(Intent intent) {
        dropBoxManager.getDropBoxFileListMethod();
        mAdapter.clearSelectedList();
        mAdapter.notifyDataSetChanged();
        View view = getView();
        assert view != null;

    }

    public boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    public void checkIfUploadDisrupted() {
        if (new ConnectionDetector(getActivity().getApplicationContext()).isConnectedToInternet()) {
            Log.d(TAG, "There is connection to internet");
            String jsonNotFinishList = SharedPrefsUtils.getStringPreference(getActivity().getApplicationContext(), Keys.NOT_FINISH_UPLOAD_LIST);
            if (!isServiceRunning("DropboxUploadIntentService")) {
                if (jsonNotFinishList != null) {
                    ArrayList<AppDataItem> appsList = filesUtils.getArrayFromJSONString(jsonNotFinishList);
                    if (appsList.size() > 0) {
                        long totalUploadSize = filesUtils.getFileSizeFromListArray(appsList);
                        long cloudFreeSpace = SharedPrefsUtils.getLongPreference(getActivity().getApplicationContext(), Keys.DROPBOX_FREE_SPACE_LONG, totalUploadSize);
                        if (( cloudFreeSpace - totalUploadSize) < 0) {
                            Log.e(TAG, "There is no free space on cloud...");
                            Snackbar.make(getView(), "Upload failed. There is no free space on cloud...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                        else {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Keys.APPS_UPLOAD_ARRAYLIST, appsList);
                            Intent intent = new Intent(getActivity().getApplicationContext(), DropboxUploadIntentService.class);
                            intent.putExtras(bundle);
                            Log.d(TAG, "Found that there is unfinished upload, starting again");
                            getActivity().startService(intent);
                        }
                    }
                }
            }
        }
        else {
            Log.d(TAG, "There is no connection to internet");
//            Snackbar.make(this., "No connection to internet, Turn on your WiFi/3G", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

    }
    public void createShareAPKDialog(final AppDataItem app, Date expires, final String url) {
        // custom dialog
//        Log.d(TAG, "position is: " + String.valueOf(position));

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        View mView = View.inflate(getContext(),R.layout.share_cloud_dialog, null);

        TextView dialog_title = (TextView) mView.findViewById(R.id.title_share_dialog);
        ImageView dialog_app_icon = (ImageView) mView.findViewById(R.id.share_app_icon_apk_dialog);
        TextView dialog_app_name = (TextView) mView.findViewById(R.id.app_name_share_dialog);
        TextView dialog_app_version = (TextView) mView.findViewById(R.id.app_version_share_dialog);
        TextView dialog_app_size = (TextView) mView.findViewById(R.id.app_size_share_dialog);
        EditText dialog_url_edit_text = (EditText)  mView.findViewById(R.id.share_url_edit_text_dialog);
        TextView dialog_expiration = (TextView) mView.findViewById(R.id.link_expire_dialog);
        Button dialog_copy_button = (Button) mView.findViewById(R.id.copy_to_clipboard_dialog);
        Button dialog_share_button = (Button) mView.findViewById(R.id.share_url_button_dialog);
        Button dialog_done_button = (Button) mView.findViewById(R.id.done_share_apk_dialog);

        String dateString = "URL expired on: " + DateFormat.format("MM/dd/yyyy", new Date(expires.getTime())).toString();
        Log.d(TAG, "Date String = " + dateString);

        dialog_app_name.setText(app.getName());
        dialog_app_version.setText(app.getAppVersion());
        dialog_app_size.setText(app.getApkSize());
        dialog_url_edit_text.setText(url);
        dialog_expiration.setText(dateString);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        try {
            dialog_app_icon.setImageDrawable(getActivity().getPackageManager().getApplicationIcon(app.getPackageName()));
        }
        catch (PackageManager.NameNotFoundException error) {
            Log.e(TAG, error.getMessage());
            dialog_app_icon.setImageResource(R.mipmap.ic_launcher);
        }

        // **************************Dialog Listeners **********************************
        dialog_copy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click on Copy to clipboard

                ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(getContext().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        dialog_share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Click on Share url
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String share = SharedPrefsUtils.getStringPreference(getContext(), Keys.DROPBOX_USER_NAME) + ", want's to share an apk file: " + url;
                String text = "\nApp Name: " + app.getName() + " | Version: " + app.getAppVersion() + " | File size: " + app.getApkSize() + " |";
                sendIntent.putExtra(Intent.EXTRA_TEXT, share + text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        dialog_done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private class GetShareAPKURL extends AsyncTask<AppDataItem, Void, DropboxAPI.DropboxLink> {
        private ProgressDialog progress = null;
        private AppDataItem appDataItem;

        @Override
        protected DropboxAPI.DropboxLink doInBackground(AppDataItem... params) {
            Log.d(TAG,"GetShareAPKURL doInBackground");
            DropboxAPI.DropboxLink app_share = null;
            app_share = dropBoxManager.shareAPKFromItem(params[0]);
            appDataItem = params[0];
            return app_share;

        }

        @Override
        protected void onPostExecute(DropboxAPI.DropboxLink result) {
//            mRecyclerView.setAdapter(mAdapter);
            progress.dismiss();
            Log.d(TAG, "app_share is: " + String.valueOf(result == null));
            if (result != null) {
                createShareAPKDialog(appDataItem, result.expires, result.url);
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(getContext(), "Generating", "Generating share url...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public class OnFinishUploadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver finish upload");
            if (intent.getBooleanExtra(Keys.SERVICE_UPLOAD_STATUS,false)) {
                Snackbar.make(getView(), "Uploaded successfully", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                updateUIList(intent);
                dropBoxManager.loadUserInfo();
            }
            else {
                Snackbar.make(getView(), "Upload Failed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }


        }
    }

}
