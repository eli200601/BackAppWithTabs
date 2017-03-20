package com.app.random.backApp;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class AppsListRowViewHolder extends RecyclerView.ViewHolder {

    protected LinearLayout leftSide;
    protected TextView appName;
    protected TextView packageName;
    protected ImageView iconView;
    protected CheckBox checkBox;

    public AppsListRowViewHolder(View view) {
        super(view);
        this.leftSide = (LinearLayout) view.findViewById(R.id.leftSide);
        this.appName = (TextView) view.findViewById((R.id.app_name));
        this.packageName = (TextView) view.findViewById(R.id.app_paackage);
        this.iconView = (ImageView) view.findViewById(R.id.app_icon);
        this.checkBox = (CheckBox)  view.findViewById(R.id.checkbox);
        view.setClickable(true);
    }


}
