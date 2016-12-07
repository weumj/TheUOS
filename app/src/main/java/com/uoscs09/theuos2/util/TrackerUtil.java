package com.uoscs09.theuos2.util;


import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.UOSApplication;

public class TrackerUtil {
    private static final String APP_VERSION = "app_version";
    private static final String SCREEN_NAME = "screen_name";
    private final String appVersion;
    private FirebaseAnalytics firebaseAnalytics;

    public TrackerUtil(Context context) {
        if (context != null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.setAnalyticsCollectionEnabled(true);
            appVersion = context.getString(R.string.setting_app_version_name);
        } else {
            firebaseAnalytics = null;
            appVersion = null;
        }
    }


    public void sendEvent(String screen, String eventName) {
        if (UOSApplication.DEBUG || firebaseAnalytics == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screen);
        bundle.putString(APP_VERSION, appVersion);
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    public void sendEvent(String screen, String eventName, String label) {
        if (UOSApplication.DEBUG || firebaseAnalytics == null)
            return;


        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screen);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        bundle.putString(APP_VERSION, appVersion);
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    public void sendEvent(String screen, String eventName, String label, long value) {
        if (UOSApplication.DEBUG || firebaseAnalytics == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screen);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        bundle.putLong(FirebaseAnalytics.Param.VALUE, value);
        bundle.putString(APP_VERSION, appVersion);
        firebaseAnalytics.logEvent(eventName, bundle);

    }

    public void sendViewEvent(String screen, String label) {
        if (UOSApplication.DEBUG || firebaseAnalytics == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screen);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        bundle.putString(APP_VERSION, appVersion);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public void sendClickEvent(String screen, String label) {
        if (UOSApplication.DEBUG || firebaseAnalytics == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screen);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        bundle.putString(APP_VERSION, appVersion);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void sendClickEvent(String screen, String label, long value) {
        if (UOSApplication.DEBUG || firebaseAnalytics == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screen);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, label);
        bundle.putLong(FirebaseAnalytics.Param.VALUE, value);
        bundle.putString(APP_VERSION, appVersion);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

}
