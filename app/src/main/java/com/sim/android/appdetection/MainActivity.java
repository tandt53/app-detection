package com.sim.android.appdetection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private InstallReceiver newInstallReceiver;
    private Switch switchRegister;
    private ArrayList<InstalledApp> listInstalledApps;
    private ListAppViewAdapter adapter;
    private RecyclerView recyclerView;
    private SharedPreferences preferences;
    private boolean isRegister = false;
    private SharedPreferences.Editor editor;
    private int sortType = -1;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
//        registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //ad unit
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.ad_unit_main));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            String app = bundle.getString("app_label");
            String pkg = bundle.getString("package_name");
            if (app != null && pkg != null)
                showAlertDialog(app, pkg);
        }

        preferences = getSharedPreferences(Constants.KEY_APP_ACTION, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(Constants.KEY_APP_ACTION, Constants.DEFAULT_APP_ACTION);
        editor.putInt(Constants.KEY_SORT, Constants.DEFAULT_SORT);
        editor.apply();

        sortType = preferences.getInt(Constants.KEY_SORT, Constants.DEFAULT_SORT);

        switchRegister =  findViewById(R.id.switchRegister);
        isRegister = preferences.getBoolean(Constants.KEY_SWITCH_STATUS, true);
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
                    Log.d(Constants.LOG_MAIN_TAG, "Started broadcast");
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_PACKAGE_ADDED);
                    filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
                    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                    filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                    filter.addAction(Intent.ACTION_PACKAGE_INSTALL);
                    filter.addDataScheme("package");
                    registerReceiver(newInstallReceiver, filter);
                } else {
                    if (newInstallReceiver != null) {
                        unregisterReceiver(newInstallReceiver);
                        newInstallReceiver = null;
                    }
                }
                isRegister = isChecked;
                editor.putBoolean(Constants.KEY_SWITCH_STATUS, isRegister);
                editor.commit();
                Log.d(Constants.LOG_MAIN_TAG, "isRegister: " + isRegister + "; shared: " + preferences.getBoolean(Constants.KEY_SWITCH_STATUS, false));
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


    private void update(int type) {

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

            sortType = preferences.getInt(Constants.KEY_SORT, Constants.DEFAULT_SORT);

            View view = this.getLayoutInflater().inflate(R.layout.dialog_sorting, null);
            final RadioButton rbUpdate = view.findViewById(R.id.rbUpdate);
            final RadioButton rbLabel = view.findViewById(R.id.rbLabel);
            final RadioButton rbStore = view.findViewById(R.id.rbStore);

            final AlertDialog.Builder sortDialogBuilder = new AlertDialog.Builder(this);
            sortDialogBuilder.setView(view);
            sortDialogBuilder.setTitle("Sort type");
            sortDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            sortDialogBuilder.setCancelable(true);
            final AlertDialog dialog = sortDialogBuilder.create();

            if (sortType == Constants.SORT_TYPE_APP_LABEL) {
                rbLabel.setChecked(true);
                rbStore.setChecked(false);
                rbUpdate.setChecked(false);
            }
            if (sortType == Constants.SORT_TYPE_LAST_UPDATED) {
                rbLabel.setChecked(false);
                rbStore.setChecked(false);
                rbUpdate.setChecked(true);
            }
            if (sortType == Constants.SORT_TYPE_APP_STORE) {
                rbLabel.setChecked(false);
                rbStore.setChecked(true);
                rbUpdate.setChecked(false);
            }

            rbLabel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        rbLabel.setChecked(true);
                        rbStore.setChecked(false);
                        rbUpdate.setChecked(false);
                        sortType = Constants.SORT_TYPE_APP_LABEL;
                        updateSort(Constants.SORT_TYPE_APP_LABEL);
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
                        sortType = Constants.SORT_TYPE_APP_STORE;
                        updateSort(Constants.SORT_TYPE_APP_STORE);
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
                        sortType = Constants.SORT_TYPE_LAST_UPDATED;
                        updateSort(Constants.SORT_TYPE_LAST_UPDATED);
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
        this.sortType = sortType;
        editor.putInt(Constants.KEY_SORT, this.sortType);
        editor.commit();
        update(this.sortType);
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendImplicitBroadcast(Context ctxt, Intent i) {
        PackageManager pm=ctxt.getPackageManager();
        List<ResolveInfo> matches=pm.queryBroadcastReceivers(i, 0);

        for (ResolveInfo resolveInfo : matches) {
            Intent explicit=new Intent(i);
            ComponentName cn=
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            explicit.setComponent(cn);
            ctxt.sendBroadcast(explicit);
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


    private void loadFirstTime() {
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
                        long installDate = 0;
                        long updateDate = 0;
                        Drawable appIcon = null;
                        String sourceDir = null;
                        String store = "";


                        name = pm.getApplicationLabel(ai).toString();
                        appIcon = pm.getApplicationIcon(ai);
                        installDate = pm.getPackageInfo(packageInfo.packageName, 0).firstInstallTime;
                        updateDate = new File(packageInfo.sourceDir).lastModified();
                        sourceDir = packageInfo.sourceDir;
                        store = Util.getStore(pm.getInstallerPackageName(packageInfo.packageName));

                        app = new InstalledApp(name, isSystem, installDate, updateDate, appIcon, packageInfo.packageName, sourceDir, store);
//                        Log.d(Constants.LOG_MAIN_TAG, "App: " + name + "Store dir: " + getStore(store));
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
