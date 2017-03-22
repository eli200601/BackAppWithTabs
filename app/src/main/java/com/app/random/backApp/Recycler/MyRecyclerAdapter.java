package com.app.random.backApp.Recycler;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.app.random.backApp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> {
    Context context;
    ArrayList <AppDataItem> appsListData;

    private HashSet<String> selectedAppsList = new HashSet<>();

    private PackageManager packageManager;

    private static String TAG = "MyRecyclerAdapter";

    UpdateBottomBar updateBottomBar;

    public MyRecyclerAdapter(Context context, ArrayList<AppDataItem> appsListData) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_row, null);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    public void setUpdateBottomBar (UpdateBottomBar updateBottomBar) {
        this.updateBottomBar = updateBottomBar;
    }


//    Bind data to view
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.appName.setText(appsListData.get(holder.getAdapterPosition()).getName());
        holder.packageName.setText((appsListData.get(holder.getAdapterPosition()).getPackageName()));
//        viewHolder.iconView.setImageDrawable(data.loadIcon(packageManager));
        try {
            holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(appsListData.get(holder.getAdapterPosition()).getPackageName()));
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
                    updateBottomBar.onCheckBoxClick(selectedAppsList.size());
                }
                else
                {
                    checkBoxView.setChecked(false);
                    if (selectedAppsList.contains(appsListData.get(holder.getAdapterPosition()).getPackageName())){
                        selectedAppsList.remove(appsListData.get(holder.getAdapterPosition()).getPackageName());
                        updateBottomBar.onCheckBoxClick(selectedAppsList.size());
                    }
                }
            }
        });

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onDataItemClick(View view, int position) {
                //When clicking on the Data area
                //ToDo
                // custom dialog
                final Dialog dialog = new Dialog(view.getContext());
                dialog.setContentView(R.layout.dialog_app_info);

                // Custom Android Alert Dialog Title
                TextView package_name = (TextView) dialog.findViewById(R.id.package_name);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.customDialogCancel);
                Button dialogButtonOk = (Button) dialog.findViewById(R.id.customDialogOk);

                dialog.setTitle(appsListData.get(holder.getAdapterPosition()).getName());
                package_name.setText(appsListData.get(holder.getAdapterPosition()).getPackageName());
                // Click cancel to dismiss android custom dialog box
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Your android custom dialog ok action
                // Action for custom dialog ok button click
                dialogButtonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
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
        updateBottomBar.onCheckBoxClick(selectedList.size());


    }

    public void clearSelectedList() {
        selectedAppsList.clear();
        updateBottomBar.onCheckBoxClick(0);
    }


    @Override
    public int getItemCount() {
        return appsListData.size();
    }

}
