package com.sim.android.appdetection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    InstallReceiver newInstallReceiver;
    Switch switchRegister;
    ArrayList<InstalledApp> listInstalledApps;
    ListAppViewAdapter adapter;
    RecyclerView recyclerView;
    SharedPreferences preferences;
    boolean isRegister = false;
    SharedPreferences.Editor editor;
    int sortType = -1;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
//        registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));

        //ad unit
//        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.ad_unit_main));
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            String app = bundle.getString("app_label");
            String pkg = bundle.getString("package_name");
            if (app != null)
                showAlertDialog(app, pkg);
        }

        preferences = getSharedPreferences(Util.KEY_APP_ACTION, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(Util.KEY_APP_ACTION, Util.DEFAULT_APP_ACTION);
        editor.putInt(Util.KEY_SORT, Util.DEFAULT_SORT);
        editor.commit();

        sortType = preferences.getInt(Util.KEY_SORT, Util.DEFAULT_SORT);

        switchRegister = (Switch) findViewById(R.id.switchRegister);
        isRegister = preferences.getBoolean(Util.KEY_SWITCH_STATUS, true);
        Log.d(Constants.LOG_MAIN_TAG, "isRegister: " + isRegister);
        if (isRegister) {
            switchRegister.setChecked(isRegister);
        }
        switchRegister.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (newInstallReceiver != null) {
                        unregisterReceiver(newInstallReceiver);
                        newInstallReceiver = null;
                    }
                    newInstallReceiver = new InstallReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_PACKAGE_ADDED);
                    filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
                    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                    filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                    filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
                    registerReceiver(newInstallReceiver, filter);
                } else {
                    if (newInstallReceiver != null) {
                        unregisterReceiver(newInstallReceiver);
                        newInstallReceiver = null;
                    }
                }
                isRegister = isChecked;
                editor.putBoolean(Util.KEY_SWITCH_STATUS, isRegister);
                editor.commit();
                Log.d(Constants.LOG_MAIN_TAG, "isRegister: " + isRegister + "; shared: " + preferences.getBoolean(Util.KEY_SWITCH_STATUS, false));
            }
        });

        listInstalledApps = new ArrayList<>();

        checkPermission();
//        update(sortType);
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new ListAppViewAdapter(this, listInstalledApps);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadFirstTime();
    }


    public void update(int type) {

        listInstalledApps = Util.getListApp(this, type, false);
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new ListAppViewAdapter(this, listInstalledApps);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_sort) {

            sortType = preferences.getInt(Util.KEY_SORT, Util.DEFAULT_SORT);

            View view = this.getLayoutInflater().inflate(R.layout.dialog_sorting, null);
//            LinearLayout sortUpdate = (LinearLayout) view.findViewById(R.id.layout_sort_last_update);
//            LinearLayout sortLabel = (LinearLayout) view.findViewById(R.id.layout_sort_label);
//            LinearLayout sortStore = (LinearLayout) view.findViewById(R.id.layout_sort_store);
            final RadioButton rbUpdate = (RadioButton) view.findViewById(R.id.rbUpdate);
            final RadioButton rbLabel = (RadioButton) view.findViewById(R.id.rbLabel);
            final RadioButton rbStore = (RadioButton) view.findViewById(R.id.rbStore);

            final AlertDialog.Builder sortDialogBuilder = new AlertDialog.Builder(this);
            sortDialogBuilder.setView(view);
            sortDialogBuilder.setTitle("Sort type");
            sortDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            sortDialogBuilder.setCancelable(true);
            final AlertDialog dialog = sortDialogBuilder.create();

            if (sortType == Util.SORT_TYPE_APP_LABEL) {
                rbLabel.setChecked(true);
                rbStore.setChecked(false);
                rbUpdate.setChecked(false);
            }
            if (sortType == Util.SORT_TYPE_LAST_UPDATED) {
                rbLabel.setChecked(false);
                rbStore.setChecked(false);
                rbUpdate.setChecked(true);
            }
            if (sortType == Util.SORT_TYPE_APP_STORE) {
                rbLabel.setChecked(false);
                rbStore.setChecked(true);
                rbUpdate.setChecked(false);
            }

//            rbUpdate.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    rbLabel.setChecked(false);
//                    rbStore.setChecked(false);
//                    rbUpdate.setChecked(true);
//                    updateSort(Util.SORT_TYPE_LAST_UPDATED);
//                    dialog.dismiss();
//                }
//            });
//            rbLabel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    rbLabel.setChecked(true);
//                    rbStore.setChecked(false);
//                    rbUpdate.setChecked(false);
//                    updateSort(Util.SORT_TYPE_APP_LABEL);
//                    dialog.dismiss();
//                }
//            });
//            rbStore.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    rbLabel.setChecked(false);
//                    rbStore.setChecked(true);
//                    rbUpdate.setChecked(false);
//                    updateSort(Util.SORT_TYPE_APP_STORE);
//                    dialog.dismiss();
//                }
//            });

            rbLabel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        rbLabel.setChecked(true);
                        rbStore.setChecked(false);
                        rbUpdate.setChecked(false);
                        sortType = Util.SORT_TYPE_APP_LABEL;
                        updateSort(Util.SORT_TYPE_APP_LABEL);
                        dialog.dismiss();
                    } else {
                        rbLabel.setChecked(false);
                    }

                }
            });

            rbStore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        rbStore.setChecked(true);
                        rbLabel.setChecked(false);
                        rbUpdate.setChecked(false);
                        sortType = Util.SORT_TYPE_APP_STORE;
                        updateSort(Util.SORT_TYPE_APP_STORE);
                        dialog.dismiss();
                    } else {
                        rbStore.setChecked(false);
                    }

                }
            });
            rbUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    buttonView.setChecked(true);
                    if (isChecked) {
                        rbUpdate.setChecked(true);
                        rbLabel.setChecked(false);
                        rbStore.setChecked(false);
                        sortType = Util.SORT_TYPE_LAST_UPDATED;
                        updateSort(Util.SORT_TYPE_LAST_UPDATED);
                        dialog.dismiss();
                    } else {
                        rbUpdate.setChecked(false);
                    }
                }
            });
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    void updateSort(int sortType) {
        sortType = sortType;
        editor.putInt(Util.KEY_SORT, sortType);
        editor.commit();
        update(sortType);
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkPermission() {
        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Here, thisActivity is the current activity
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    checkPermission();
                }
                return;
            }
        }
    }

    public void showAlertDialog(String app, final String pkg) {

        String title = getResources().getString(R.string.warning_title);
        String msg01 = getResources().getString(R.string.warning_msg_1st_part);
        String msg02 = getResources().getString(R.string.warning_msg_2nd_part);
        String detail = getResources().getString(R.string.detail);
        String skip = getResources().getString(R.string.skip);

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(msg01 + app + msg02);
        alert.setPositiveButton(detail, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + pkg));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        alert.setNegativeButton(skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.show();
    }

    @Override
    protected void onResume() {
        update(sortType);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(Constants.LOG_MAIN_TAG, "onPause");
//        if (broadcastReceiver != null)
//            unregisterReceiver(broadcastReceiver);
        super.onPause();
    }



    public void loadFirstTime() {
        new LoadingTask().execute();
    }

    private class LoadingTask extends AsyncTask<String, InstalledApp, ArrayList<InstalledApp>> {
        PackageManager pm;
        List<ApplicationInfo> packages;
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            listInstalledApps = new ArrayList<>();
            pm = getPackageManager();
            packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            progress = ProgressDialog.show(context, "", "Loading", true);
        }

        @Override
        protected ArrayList<InstalledApp> doInBackground(String... strings) {
            InstalledApp app;
            for (ApplicationInfo packageInfo : packages) {

                ApplicationInfo ai = null;
                try {
                    ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                    if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !packageInfo.packageName.equals(context.getPackageName())) {
                        String name = null;
                        boolean isSystem = false;
                        long install = 0;
                        long update = 0;
                        Drawable appIcon = null;
                        String sourceDir = null;
                        String store = "";

                        update = new File(packageInfo.sourceDir).lastModified();

                        name = pm.getApplicationLabel(ai).toString();
                        appIcon = pm.getApplicationIcon(ai);
                        install = pm.getPackageInfo(packageInfo.packageName, 0).firstInstallTime;
                        sourceDir = packageInfo.sourceDir;
                        store = Util.getStore(pm.getInstallerPackageName(packageInfo.packageName));

                        app = new InstalledApp(name, isSystem, install, update, appIcon, packageInfo.packageName, sourceDir, store);
//                    Log.d(Constants.LOG_MAIN_TAG, "App: " + name + "Store dir: " + getStore(store));
                        publishProgress(app);
                        listInstalledApps.add(app);
                        Util.sort(sortType, listInstalledApps);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return listInstalledApps;
        }

        @Override
        protected void onProgressUpdate(InstalledApp... values) {
            ArrayList a = new ArrayList<InstalledApp>();
            a.add(values[0]);
            adapter.insert(a);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<InstalledApp> installedApps) {
            progress.dismiss();
//            adapter.insert(listInstalledApps);
            super.onPostExecute(installedApps);
        }
    }
}
