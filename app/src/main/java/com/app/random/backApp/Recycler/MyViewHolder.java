package com.app.random.backApp.Recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.random.backApp.R;


public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView appName;
    TextView packageName;
    ImageView appIcon;
    CheckBox checkBox;
    LinearLayout dataView;

    ItemClickListener itemClickListener;



    public MyViewHolder(View itemView) {
        super(itemView);

        appName = (TextView) itemView.findViewById(R.id.app_name);
        packageName = (TextView) itemView.findViewById(R.id.app_paackage);
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        dataView = (LinearLayout) itemView.findViewById(R.id.leftSide);

        dataView.setOnClickListener(this);
    }



    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        this.itemClickListener.onDataItemClick(view, getLayoutPosition());
    }




}
