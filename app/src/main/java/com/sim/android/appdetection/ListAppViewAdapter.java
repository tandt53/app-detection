package com.sim.android.appdetection;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ADMIN on 8/16/2016.
 */
public class ListAppViewAdapter extends RecyclerView.Adapter<AppViewHolder> {

    ArrayList<InstalledApp> listApp;
    Context context;

    public ListAppViewAdapter(Context context, ArrayList<InstalledApp> list) {
        this.listApp = list;
        this.context = context;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_installed_app, parent, false);
        AppViewHolder holder = new AppViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, final int position) {
        final InstalledApp app = listApp.get(position);

        holder.appName.setText(app.name);
        holder.installDate.setText(context.getResources().getString(R.string.install) + " " + new Date(app.installDate).toString().substring(0, 19));
        holder.updateDate.setText(context.getResources().getString(R.string.updated) + " " + new Date(app.lastUpdatedDate).toString().substring(0, 19));
        holder.imgAppIcon.setImageDrawable(app.icon);
        holder.txtStore.setText(app.store);
        if (app.store.equals("Unknown")) {
            holder.txtStore.setTextColor(Color.parseColor("#FF5722"));
        } else {
            holder.txtStore.setTextColor(Color.parseColor("#424242"));
        }

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ComponentName cName = new ComponentName("packageName", app.packageName);
//
//                Intent intent = new Intent("android.intent.action.MAIN");
//                intent.setComponent(cName);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);

                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(app.packageName));
            }
        });

        holder.btnUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri packageURI = Uri.parse("package:" + app.packageName);
//                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
//                context.startActivity(uninstallIntent);

//                Intent intent = null;
//                intent = new Intent(Intent.ACTION_DELETE);
//
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setData(packageURI);
//                context.startActivity(intent);
//                Log.d(Constants.LOG_MAIN_TAG, "Come here");

                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + app.packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });

        holder.btnGetApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputPath = app.sourceDir.substring(0, 10);
                String inputFile = app.sourceDir.substring(10, app.sourceDir.length());
                String outputPath = "sdcard/Extract/";
                String outputFile = app.name + ".apk";

                File outFile = new File(outputPath + outputFile);
                if (outFile.exists()) {
                    showDeleteAlert(inputPath, inputFile, outputPath, outputFile);
                } else {
                    MyTask task = new MyTask();
                    task.execute(inputPath, inputFile, outputPath, outputFile);
                }
            }

        });

        //animate(holder);
    }

    @Override
    public int getItemCount() {
        return listApp.size();
    }

    void showDeleteAlert(final String inputPath, final String inputFile, final String outputPath, final String outputFile) {
        new AlertDialog.Builder(context)
                .setTitle("File exist!")
                .setMessage("Are you sure you want to overwrite this file?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteFile(outputPath, outputFile);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                        copyFile(inputPath, inputFile, outputPath, outputFile);
                        MyTask task = new MyTask();
                        task.execute(inputPath, inputFile, outputPath, outputFile);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteFile(String inputPath, String inputFile) {
        try {
            // delete the original file
            new File(inputPath + inputFile).delete();
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    // Params are input and output files, progress in Long size of data transferred, Result is Boolean success.
    public class MyTask extends AsyncTask<String, Long, Boolean> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(context, "", context.getResources().getString(R.string.preparing), true);
        }

        @Override
        protected Boolean doInBackground(String... files) {
            String inputPath = files[0];
            String inputFile = files[1];
            String outputPath = files[2];
            String outputFile = files[3];

            InputStream in = null;
            OutputStream out = null;
            try {
                //create output directory if it doesn't exist
                File dir = new File(outputPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File f = new File(inputPath + inputFile);
                long fileSize = f.length();

                in = new FileInputStream(inputPath + inputFile);
                out = new FileOutputStream(outputPath + outputFile);

                byte[] buffer = new byte[1024];
                int read;
                int pro = 0;
                while ((read = in.read(buffer)) != -1) {
                    pro += read;
                    publishProgress((((long) pro * 100) / fileSize));
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;

                // write the output file (You have now copied the file)
                out.flush();
                out.close();
//                out = null;

            } catch (FileNotFoundException fnfe1) {
                Log.d(Constants.LOG_MAIN_TAG, fnfe1.getMessage());
            } catch (Exception e) {
                Log.d(Constants.LOG_MAIN_TAG, e.getMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progress.setMessage(context.getResources().getString(R.string.saving_complete));
            progress.dismiss();
            // Show dialog with result
            Toast.makeText(context, context.getResources().getString(R.string.saving_toast), Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            progress.setMessage(context.getResources().getString(R.string.saving_apk) + values[0] + "%");
        }
    }

    public void insert(ArrayList<InstalledApp> newapp) {
        listApp.addAll(0, newapp);
        notifyDataSetChanged();
    }

    public void remove(ArrayList<InstalledApp> removedApp) {
        listApp.removeAll(removedApp);
        notifyDataSetChanged();
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
//        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.anticipateovershoot_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }
}
