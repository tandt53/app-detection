package com.sim.android.appdetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by ADMIN on 8/9/2016.
 */
public class InstallReceiver extends BroadcastReceiver {
    public MainActivity activity;

    public InstallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_PACKAGE_ADDED:
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SharedPreferences preferences = context.getSharedPreferences(Util.KEY_APP_ACTION, Context.MODE_PRIVATE);
                if (preferences.getBoolean(Util.KEY_SWITCH_STATUS, false)) {
                    if (checkInstalledApp(context)) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(Util.KEY_APP_ACTION, Intent.ACTION_PACKAGE_ADDED);
                        editor.commit();
                        sendBroadcast(context, Intent.ACTION_PACKAGE_ADDED);
                    }
                }
                break;
            case Intent.ACTION_PACKAGE_CHANGED:
                break;
            case Intent.ACTION_PACKAGE_REMOVED:
                updateSharePreference(context, Intent.ACTION_PACKAGE_REMOVED);
                break;
            case Intent.ACTION_PACKAGE_REPLACED:
                break;
        }
    }

    private void sendBroadcast(Context context, String actionPackageAdded) {
        Intent i = new Intent("broadCastName");
        // Data you need to pass to activity
        i.putExtra("message", actionPackageAdded);

        context.sendBroadcast(i);
    }

    private void updateSharePreference(Context context, String action) {
        SharedPreferences preferences = context.getSharedPreferences(Util.KEY_APP_ACTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Util.KEY_APP_ACTION, action);
        editor.commit();
    }

    private boolean checkInstalledApp(Context context) {
        ArrayList<InstalledApp> listapps = Util.getListApp(context, Util.SORT_TYPE_LAST_UPDATED, true);

        Collections.sort(listapps, new Comparator<InstalledApp>() {
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
        InstalledApp app = listapps.get(0);
        if (app.store.equals("Unknown")) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("app_label", app.name);
            intent.putExtra("package_name", app.packageName);
            context.startActivity(intent);
            return false;
        } else
            return true;
    }

    void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

}