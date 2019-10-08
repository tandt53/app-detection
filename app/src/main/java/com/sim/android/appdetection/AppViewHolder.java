package com.sim.android.appdetection;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ADMIN on 8/16/2016.
 */
public class AppViewHolder extends RecyclerView.ViewHolder {

    public CardView cv;
    public TextView appName;
    public TextView installDate;
    public TextView updateDate;
    public ImageView imgAppIcon;
    public TextView txtStore;

    public TextView btnUninstall;
    public TextView btnGetApk;

    public AppViewHolder(View itemView) {
        super(itemView);
        cv = itemView.findViewById(R.id.cardView);
        appName = itemView.findViewById(R.id.txtAppName);
        installDate = itemView.findViewById(R.id.txtInstallDate);
        updateDate = itemView.findViewById(R.id.txtUpdatedDate);
        imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
        txtStore = itemView.findViewById(R.id.store);

        btnGetApk = itemView.findViewById(R.id.btnGetApk);
        btnUninstall = itemView.findViewById(R.id.btnUninstall);

    }
}
