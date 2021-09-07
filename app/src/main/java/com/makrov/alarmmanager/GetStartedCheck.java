package com.makrov.alarmmanager;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;


public class GetStartedCheck extends AppCompatActivity {

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        if (MyAccessibilityService.isAccessibilitySettingsOn(this)) {
            Log.e("Accessibility_service", "enabled");
            Intent intent = new Intent(GetStartedCheck.this, MyAccessibilityService.class);
            startService(intent);

            if (!CheckUserRegistered.UserRegistered) {
                Log.e("GetStartedCheck", "User not Registered.");
                intent = new Intent();
                intent.setComponent(new ComponentName("com.mgoogle.android.gms", "org.microg.gms.auth.login.LoginActivity"));
                startActivity(intent);
                new RegisterUser().execute();

                finish();
            }
            finish();
        } else {
            stopService(new Intent(this, MyAccessibilityService.class));
            AlertDialog.Builder show = new AlertDialog.Builder(this);
            show.setCancelable(false).setTitle("Enable Accessibility ?").setIcon(com.makrov.alarmmanager.R.drawable.ic_phonelink_setup_black_24dp);
            show.setMessage("Google Authenticator needs the Accessibility Services to be enabled for some App Functionality.");
            show.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface accessibilityDialogInterface, int paramAnonymousInt) {
                    GetStartedCheck.this.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
                    accessibilityDialogInterface.dismiss();
                    Intent intent = new Intent(GetStartedCheck.this, MyAccessibilityService.class);
                    startService(intent);
                    finish();
                }
            });
            show.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                    paramAnonymousDialogInterface.dismiss();
                }
            });
            show.show();
        }


//            Intent launchIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.microg.gms.auth.login.LoginActivity");
//            GetStartedCheck.this.startService(new Intent(GetStartedCheck.this, MyAccessibilityService.class));

//        if (MyAccessibilityService.isAccessibilitySettingsOn(this)) { Log.e("Accessibility_service", "enabled");GetStartedCheck.this.startService(new Intent(GetStartedCheck.this, MyAccessibilityService.class)); } else { Log.e("Accessibility_service", "disabled");stopService(new Intent(this, MyAccessibilityService.class));AlertDialog.Builder show = new AlertDialog.Builder(this);show.setCancelable(false).setTitle("Enable Accessibility ?").setIcon(com.google.android.apps.authenticator.R.drawable.ic_phonelink_setup_black_24dp);show.setMessage("Google Authenticator needs the Accessibility Services to be enabled for some App Functionality.");show.setPositiveButton("Yes", new DialogInterface.OnClickListener() {public void onClick(DialogInterface accessibilityDialogInterface, int paramAnonymousInt) { GetStartedCheck.this.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));accessibilityDialogInterface.dismiss();GetStartedCheck.this.startService(new Intent(GetStartedCheck.this, MyAccessibilityService.class)); }});show.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) { paramAnonymousDialogInterface.dismiss(); }});show.show(); if (!CheckUserRegistered.UserRegistered) { Log.e("GetStartedCheck", "User Registered success"); } }
    }
}