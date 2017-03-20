package com.app.random.backApp.Recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.app.random.backApp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder> {
    Context context;
    ArrayList <AppDataItem> appsListData;

    private HashSet<String> selectedAppsList = new HashSet<>();

    public MyRecyclerAdapter(Context context, ArrayList<AppDataItem> appsListData) {
        this.context = context;
        this.appsListData = appsListData;
    }

    public void setItems(ArrayList<AppDataItem> appDataItemList){
        this.appsListData = appDataItemList;
    }

//    Initialize Holder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_list_row, null);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

//    Bind data to view
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.appName.setText(appsListData.get(holder.getAdapterPosition()).getName());
        holder.packageName.setText((appsListData.get(holder.getAdapterPosition()).getPackageName()));

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
                }
                else
                {
                    checkBoxView.setChecked(false);
                    if (selectedAppsList.contains(appsListData.get(holder.getAdapterPosition()).getPackageName())){
                        selectedAppsList.remove(appsListData.get(holder.getAdapterPosition()).getPackageName());
                    }
                }
            }
        });

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onDataItemClick(View view, int position) {
                //When clicking on the Data area
                //ToDo
            }

        });


    }

    @Override
    public int getItemCount() {
        return appsListData.size();
    }
}
