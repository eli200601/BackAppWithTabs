package com.app.random.backApp.Recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.random.backApp.R;


public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView appName;
    TextView apkSize;
    TextView version;

    ImageView successIcon;
    ImageView appIcon;
    CheckBox checkBox;
    LinearLayout dataView;

    Button shareAPK;
    Button downloadAPK;
    Button deleteAPK;

    boolean sawAnimationSuccess;
    boolean sawAnimationPregress;

    ProgressBar uploadProgress;

    ItemClickListener itemClickListener;

    public MyViewHolder(View itemView) {
        super(itemView);
        successIcon = (ImageView) itemView.findViewById(R.id.cloud_check_v);
        appName = (TextView) itemView.findViewById(R.id.app_name);
        apkSize = (TextView) itemView.findViewById(R.id.item_size);
        version = (TextView) itemView.findViewById(R.id.item_version);
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        dataView = (LinearLayout) itemView.findViewById(R.id.leftSide);
        shareAPK = (Button) itemView.findViewById((R.id.share_apk_card_action));
        downloadAPK = (Button) itemView.findViewById((R.id.download_apk_card_action));
        deleteAPK = (Button) itemView.findViewById((R.id.delete_apk_card_action));
        uploadProgress = (ProgressBar) itemView.findViewById(R.id.item_upload_progressBar);

        dataView.setOnClickListener(this);
        sawAnimationSuccess = false;
        sawAnimationPregress = false;
    }


    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        this.itemClickListener.onDataItemClick(view, getLayoutPosition());
    }

}
