package com.app.random.backApp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.HashSet;
import java.util.List;


public class AppsListRecyclerAdapter extends RecyclerView.Adapter<AppsListRowViewHolder> {

    static final private String TAG = "AppsListRecyclerAdapter";

    private List<AppsListItems> listItemsList;
    private HashSet<String> selectedApps;
    private Context mContext;
    private PackageManager packageManager;
    private View view;
    private int focusedItem = 0;
    private MyListener ca;

    public AppsListRecyclerAdapter(Context context, List<AppsListItems> listItemsList, HashSet<String> selectedApps, MyListener ca) {
        this.listItemsList = listItemsList;
        this.mContext= context;
        this.packageManager = context.getPackageManager();
        this.selectedApps = selectedApps;
        this.ca = ca;
    }

    @Override
    public AppsListRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.apps_list_row, null);
        final AppsListItems item = listItemsList.get(position);

        final AppsListRowViewHolder holder = new AppsListRowViewHolder(v);
        if (item.getCheckBox()){
            holder.checkBox.setChecked(true);
        }
        else {
            holder.checkBox.setChecked(false);
        }

        holder.leftSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Eliran Is hare!","Left side");
                try {
                    Intent intent = packageManager.getLaunchIntentForPackage(holder.packageName.getText().toString());
                    if (intent != null) {
                        mContext.startActivity(intent);
                    }
                }
                catch (ActivityNotFoundException e) {
                    Log.e(TAG, "onCreateViewHolder:" + e.getMessage());
                }
                catch (Exception e) {
                    Log.e(TAG, "onCreateViewHolder:" + e.getMessage());
                }
            }
        });


        if (item.getCheckBox()){
            holder.checkBox.setChecked(true);
        }
        else {
            holder.checkBox.setChecked(false);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final AppsListRowViewHolder holder, int position) {
        final AppsListItems listItems = listItemsList.get(position);
        holder.itemView.setSelected(focusedItem == position);

        holder.getLayoutPosition();

        if (listItems.getCheckBox()){
            holder.checkBox.setChecked(true);
        }
        else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    TextView selectedAmountHolder = (TextView) view.findViewById(R.id.ItemsSelectedValueText);
                if (buttonView != null) {
                    if (isChecked & (listItems.getCheckBox() == false)) {
//                        listItems.setCheckBox(isChecked);
//                        Log.d(TAG, listItems.getPackageName());
//                        selectedApps.add(listItems.getPackageName());
//                        Log.d(TAG, "Adding To 'selectedApps' table = " + listItems.getPackageName());
                        Log.d(TAG, "Table is " + listItems.getPackageName());
                        ca.callback(listItems.getPackageName());
                    } else {
                        if (isChecked & (listItems.getCheckBox() == true)) {
                            Log.d(TAG, "Removing from 'selectedApps' table =" + listItems.getPackageName());
//                            selectedApps.remove(listItems.getPackageName());
//                            listItems.setCheckBox(isChecked);
                            ca.callback(listItems.getPackageName());
                        }

                    }
                }
            }

        });
        holder.appName.setText(listItems.getAppName());
        holder.packageName.setText((listItems.getPackageName()));
        try{
            holder.iconView.setImageDrawable(packageManager.getApplicationIcon(holder.packageName.getText().toString()));
        }
        catch (PackageManager.NameNotFoundException error) {
            Log.e(TAG, error.getMessage());
        }


    }

    public void setSelectedApps(HashSet<String> selectedApps) {
        // This is called when new data in the list, and you want to refresh the view.
        this.selectedApps = selectedApps;
        notifyDataSetChanged();
    }

    public void clearAdapter () {
        listItemsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != listItemsList ? listItemsList.size() : 0);
    }
}
