package com.sim.android.appdetection;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    Button btnEmail;
    Button btnSubmit;
    TextView txtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        btnEmail = findViewById(R.id.btnEmail);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtVersion = findViewById(R.id.app_version);

        PackageInfo pInfo = null;
        try {
            pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = pInfo.versionName;
        txtVersion.setText("Version " + version);


        btnEmail.setOnClickListener(listner);
        btnSubmit.setOnClickListener(listner);


    }

    public View.OnClickListener listner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btnEmail) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "simteam8@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Please update application as my favor");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Dear Simply Dev Team,\n\nPlease update web list with my favorites as following:\n");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            } else if (v == btnSubmit) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        }
    };
}
