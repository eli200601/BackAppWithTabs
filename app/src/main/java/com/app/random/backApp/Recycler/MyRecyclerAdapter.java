package com.app.random.backApp.Recycler;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.random.backApp.R;
import com.app.random.backApp.Utils.Keys;
import com.app.random.backApp.Utils.SharedPrefsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;


public class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> {
    Context context;
    public ArrayList<AppDataItem> appsListData;

    private HashSet<String> selectedAppsList = new HashSet<>();

    private PackageManager packageManager;
    private String origin;

    private static String TAG = "MyRecyclerAdapter";

    UpdateBottomBar updateBottomBar;

    public MyRecyclerAdapter(Context context, ArrayList<AppDataItem> appsListData, String origin) {
        this.origin = origin;
        this.context = context;
        this.appsListData = appsListData;
        this.packageManager = context.getPackageManager();
    }

    public void setItems(ArrayList<AppDataItem> appDataItemList){
        this.appsListData = appDataItemList;
    }

    public int getSelectedAppsListSize() {
        return selectedAppsList.size();
    }

//    Initialize Holder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        String viewTypePref = SharedPrefsUtils.getStringPreference(parent.getContext(), Keys.PREF_VIEWTYPE);
        View view = null;
        if (origin.equals(Keys.ORIGIN_DEVICEAPPSFRAGMENT)) {
            switch (viewTypePref) {
                case Keys.PREF_VIEWTYPE_LIST: {
                    Log.d(TAG, Keys.PREF_VIEWTYPE_LIST);
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_row, null);
                    break;
                }
                case Keys.PREF_VIEWTYPE_CARD:{
                    Log.d(TAG, Keys.PREF_VIEWTYPE_CARD);
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_card, null);
                    break;
                }
                case Keys.PREF_VIEWTYPE_GRID:{
                    Log.d(TAG, Keys.PREF_VIEWTYPE_GRID);
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_grid, null);
                    break;
                }
            }
        }
        else {
            if (origin.equals(Keys.ORIGIN_DOWNLOADFOLDERFRAGMENT)) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_grid, null);
            }
            else {
                if (origin.equals(Keys.ORIGIN_CLOUDMAINFRAGMENT)) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_card, null);
                }
            }
        }
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    public void setUpdateBottomBar (UpdateBottomBar updateBottomBar) {
        this.updateBottomBar = updateBottomBar;
    }

    public ArrayList<AppDataItem> getSelectedAppsListCloud() {
        ArrayList<AppDataItem> selectedList = new ArrayList<>();

        for (AppDataItem item: appsListData) {
            if (selectedAppsList.contains(item.getPackageName())) {
                selectedList.add(item);
            }
        }
        return selectedList;
    }

//    Bind data to view
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        String apkSizeInit = "APK Size: ";
        String apkVersionInit = "Version: ";
        String apkSize = appsListData.get(holder.getAdapterPosition()).getApkSize();
        String version = appsListData.get(holder.getAdapterPosition()).getAppVersion();

        if (!version.contains("v")) {
            version = "v" + version;
        }

        holder.appName.setText(appsListData.get(holder.getAdapterPosition()).getName());

        switch (origin) {
            case Keys.ORIGIN_DEVICEAPPSFRAGMENT: {
                if (SharedPrefsUtils.getStringPreference(context, Keys.PREF_VIEWTYPE).equals(Keys.PREF_VIEWTYPE_GRID)){
                    holder.apkSize.setText(apkSize);
                    holder.version.setText(version);
                }
                else {
                    holder.apkSize.setText(apkSizeInit + apkSize);
                    holder.version.setText(apkVersionInit + version);
                }
                break;
            }
            case Keys.ORIGIN_CLOUDMAINFRAGMENT: {
                holder.apkSize.setText(apkSizeInit + apkSize);
                holder.version.setText(apkVersionInit + version);
                break;
            }
            case Keys.ORIGIN_DOWNLOADFOLDERFRAGMENT: {
                holder.apkSize.setText(apkSize);
                holder.version.setText(version);
                break;
            }
        }

        try {
            if (origin.equals("DownloadFolderFragment")) {
                holder.appIcon.setImageResource(R.mipmap.ic_folder_icon);
            }
            else {
                holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(appsListData.get(holder.getAdapterPosition()).getPackageName()));
            }
        }
        catch (PackageManager.NameNotFoundException error) {
            Log.e(TAG, error.getMessage());
        }


        if (selectedAppsList.contains(appsListData.get(holder.getAdapterPosition()).getPackageName())){
            holder.checkBox.setChecked(true);
        }
        else {
            holder.checkBox.setChecked(false);
        }

//        holder.appIcon TODO

        //Listeners
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton checkBoxView, boolean b) {
                if(checkBoxView.isChecked())
                {
                    checkBoxView.setChecked(true);
                    selectedAppsList.add(appsListData.get(holder.getAdapterPosition()).getPackageName());
                    updateBottomBar.onCheckBoxClick();
                }
                else
                {
                    checkBoxView.setChecked(false);
                    if (selectedAppsList.contains(appsListData.get(holder.getAdapterPosition()).getPackageName())){
                        selectedAppsList.remove(appsListData.get(holder.getAdapterPosition()).getPackageName());
                        updateBottomBar.onCheckBoxClick();
                    }
                }
            }
        });

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onDataItemClick(final View view, int position) {
                //When clicking on the Data area
                //ToDo
                // custom dialog
                Log.d(TAG, "position is: " + String.valueOf(position));

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(view.getContext());
                View mView = View.inflate(view.getContext(),R.layout.app_info_dialog, null);

                ImageView icon = (ImageView) mView.findViewById(R.id.dialog_app_icon);
                TextView title = (TextView) mView.findViewById(R.id.dialog_title);
                TextView version = (TextView) mView.findViewById(R.id.dialog_version);
                TextView size = (TextView) mView.findViewById(R.id.dialog_file_size);
                TextView packageName = (TextView) mView.findViewById(R.id.dialog_package);
                TextView path = (TextView) mView.findViewById(R.id.dialog_path);
                Button done = (Button) mView.findViewById(R.id.dialog_main_action);

//                *************************************************
                final ProgressBar bar = (ProgressBar) mView.findViewById(R.id.progressBar);

                ValueAnimator animator = new ValueAnimator();
                animator.setObjectValues(0, 80);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        bar.setProgress((int) animation.getAnimatedValue());
                    }
                });
                animator.setEvaluator(new TypeEvaluator<Integer>() {
                    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                        return Math.round(startValue + (endValue - startValue) * fraction);
                    }
                });
                animator.setDuration(2000);
                animator.start();
//                *************************************************


                try {
                    if (origin.equals("DownloadFolderFragment")) {
                        icon.setImageResource(R.mipmap.ic_folder_icon);
                    }
                    else {
                        icon.setImageDrawable(packageManager.getApplicationIcon(appsListData.get(position).getPackageName()));
                    }
                }
                catch (PackageManager.NameNotFoundException error) {
                    Log.e(TAG, error.getMessage());
                    icon.setImageResource(R.mipmap.ic_launcher);
                }
                title.setText(appsListData.get(position).getName());
                version.setText("App Version: " + appsListData.get(position).getAppVersion());

                size.setText("APK Size: " + appsListData.get(position).getApkSize());
                packageName.setText("Package Name: " + appsListData.get(position).getPackageName());
                path.setText("Path: " + appsListData.get(position).getSourceDir());

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();

                done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
                dialog.show();

            }
        });
    }

    public void setAllListSelected(){
        HashSet<String> selectedList = new HashSet<>();

        for (AppDataItem info: appsListData){
            selectedList.add(info.getPackageName());
        }
        selectedAppsList = selectedList;
        updateBottomBar.onCheckBoxClick();


    }

    public void clearSelectedList() {
        selectedAppsList.clear();
        updateBottomBar.onCheckBoxClick();
    }


    @Override
    public int getItemCount() {
        return appsListData.size();
    }

    public HashSet<String> getSelectedPackageNamesList(){
         return selectedAppsList;



//        ArrayList<String> apk_list = new ArrayList<>();
//
//        for (String app_name : selectedAppsList) {
//
//            appsListData.
//        }
//
//        return selectedAppsList.contains()
    }

}
