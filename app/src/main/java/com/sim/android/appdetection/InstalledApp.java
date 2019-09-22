package com.sim.android.appdetection;
import android.graphics.drawable.Drawable;
import android.media.Image;

import java.util.Date;

/**
 * Created by ADMIN on 8/15/2016.
 */
public class InstalledApp {
    public String name;
    public boolean isSystemApp;
    public long installDate;
    public long lastUpdatedDate;
    public Drawable icon;
    public String packageName;
    public String sourceDir;
    public String store;

    public InstalledApp(String name, boolean isSystemApp, long installDate, long lastUpdatedDate, Drawable icon, String packageName, String sourceDir, String store){
        this.name = name;
        this.isSystemApp = isSystemApp;
        this.installDate = installDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.icon = icon;
        this.packageName = packageName;
        this.sourceDir = sourceDir;
        this.store = store;
    }

}
