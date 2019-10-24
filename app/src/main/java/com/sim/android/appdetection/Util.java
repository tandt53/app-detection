package com.sim.android.appdetection;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ADMIN on 8/17/2016.
 */
public class Util {

    public static ArrayList<InstalledApp> getListApp(Context context, int sortType, boolean isAllApp) {
        ArrayList<InstalledApp> listInstalledApps ;
        listInstalledApps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        if (isAllApp) {
            for (ApplicationInfo packageInfo : packages) {
                ApplicationInfo ai = null;
                try {
                    ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                    if (!packageInfo.packageName.equals(context.getPackageName())) {
                        String name = pm.getApplicationLabel(ai).toString();
                        boolean isSystem = false;
                        long install = pm.getPackageInfo(packageInfo.packageName, 0).firstInstallTime;
                        long update = new File(packageInfo.sourceDir).lastModified();
                        Drawable appIcon = pm.getApplicationIcon(ai);
                        String sourceDir = packageInfo.sourceDir;
                        String store = getStore(pm.getInstallerPackageName(packageInfo.packageName));

                        InstalledApp app = new InstalledApp(name, isSystem, install, update, appIcon, packageInfo.packageName, sourceDir, store);
                        listInstalledApps.add(app);

                        sort(sortType, listInstalledApps);

                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (ApplicationInfo packageInfo : packages) {
                ApplicationInfo ai = null;
                try {
                    ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                    if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !packageInfo.packageName.equals(context.getPackageName())) {
                        String name = pm.getApplicationLabel(ai).toString();
                        boolean isSystem = false;
                        long install = pm.getPackageInfo(packageInfo.packageName, 0).firstInstallTime;
                        long update = new File(packageInfo.sourceDir).lastModified();
                        Drawable appIcon = pm.getApplicationIcon(ai);
                        String sourceDir = packageInfo.sourceDir;
                        String store = getStore(pm.getInstallerPackageName(packageInfo.packageName));

                        InstalledApp app = new InstalledApp(name, isSystem, install, update, appIcon, packageInfo.packageName, sourceDir, store);
                        listInstalledApps.add(app);

                        sort(sortType, listInstalledApps);

                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return listInstalledApps;
    }

    public static String getStore(String installerPackageName) {
        if (installerPackageName != null) {
            if (installerPackageName.equals("com.android.vending"))
                return "Google Play";
            else if (installerPackageName.equals("com.amazon.venezia"))
                return "Amazon";
            else if (installerPackageName.equals("com.sec.android.app.samsungapps"))
                return "Samsung Apps";
            else return installerPackageName;
        }
        return "Unknown";
    }

    public static ArrayList<InstalledApp> sort(int type, ArrayList<InstalledApp> listApp) {
        switch (type) {
            case 0:
                Collections.sort(listApp, new Comparator<InstalledApp>() {
                    public int compare(InstalledApp emp1, InstalledApp emp2) {
                        long time = emp1.lastUpdatedDate - emp2.lastUpdatedDate;
                        // sort bigger to smaller
                        if (time > 0)
                            return -1;
                        if (time < 0)
                            return 1;
                        return 0;
                    }
                });
                break;
            case 1:
                Collections.sort(listApp, new Comparator<InstalledApp>() {
                    public int compare(InstalledApp emp1, InstalledApp emp2) {
                        int time = emp1.name.compareTo(emp2.name);
                        // sort bigger to smaller
                        if (time > 0)
                            return 1;
                        if (time < 0)
                            return -1;
                        return 0;
                    }
                });
                break;
            case 2:
                Collections.sort(listApp, new Comparator<InstalledApp>() {
                    public int compare(InstalledApp emp1, InstalledApp emp2) {
                        int time = emp1.store.compareTo(emp2.store);
                        // sort bigger to smaller
                        if (time > 0)
                            return -1;
                        if (time < 0)
                            return 1;
                        return 0;
                    }
                });
                break;
        }


        return null;
    }
}
