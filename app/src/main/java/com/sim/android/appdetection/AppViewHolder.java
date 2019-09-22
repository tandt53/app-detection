package com.sim.android.appdetection;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

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
        cv = (CardView) itemView.findViewById(R.id.cardView);
        appName = (TextView) itemView.findViewById(R.id.txtAppName);
        installDate = (TextView) itemView.findViewById(R.id.txtInstallDate);
        updateDate = (TextView) itemView.findViewById(R.id.txtUpdatedDate);
        imgAppIcon = (ImageView) itemView.findViewById(R.id.imgAppIcon);
        txtStore = (TextView) itemView.findViewById(R.id.store);

        btnGetApk = (TextView) itemView.findViewById(R.id.btnGetApk);
        btnUninstall = (TextView) itemView.findViewById(R.id.btnUninstall);

    }
}
