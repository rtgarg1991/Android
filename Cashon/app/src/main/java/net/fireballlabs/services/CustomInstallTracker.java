package net.fireballlabs.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.PreferenceManager;

/**
 * Created by Rohit on 7/9/2015.
 */
public class CustomInstallTracker extends BroadcastReceiver {
    private static final String TAG = "InstallReceiver";
    private static final String PID = "pid";
    private static final String REFERRAL_ID = "c";
    private static final String USER_INVITE = "User_invite";
    private static final String REFERRER = "referrer";
    private String clickId;

    public CustomInstallTracker() {
        super();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String referralId = "";
        String referType = "";
        if (extras != null) {
            for (String key : extras.keySet()) {
                if(REFERRER.equals(key)) {
                    String value = extras.getString(key);
                    if(value != null) {
                        String values[] = value.split("&");
                        for(String v : values) {
                            String[] ids = v.split("=");
                            if(ids.length == 2) {
                                if(REFERRAL_ID.equals(ids[0])) {
                                    referralId = ids[1];
                                } else if(PID.equals(ids[0])) {
                                    referType = ids[1];
                                } else if(Constants.CLICK_ID.equals(ids[0])) {
                                    clickId = ids[1];
                                }
                            }
                        }
                    }
                } else if(key.equals(REFERRAL_ID)) {
                    referralId = extras.getString(key);
                } else if(key.equals(PID)) {
                    referType = extras.getString(key);
                } else if(key.equals(Constants.CLICK_ID)) {
                    clickId = extras.getString(key);
                }
            }

            if(referType != null && USER_INVITE.equals(referType) && referralId != null) {
                PreferenceManager.setDefaultSharedPreferenceValue(context,
                        Constants.PREF_REFERRAL_ID, Context.MODE_PRIVATE, Constants.USER_INVITE);
                PreferenceManager.setDefaultSharedPreferenceValue(context,
                        Constants.PREF_CLICK_ID, Context.MODE_PRIVATE, referralId);
            } else if(referType != null) {

                PreferenceManager.setDefaultSharedPreferenceValue(context,
                        Constants.PREF_REFERRAL_ID, Context.MODE_PRIVATE, referType);
                if(clickId != null) {
                    PreferenceManager.setDefaultSharedPreferenceValue(context,
                            Constants.PREF_CLICK_ID, Context.MODE_PRIVATE, clickId);
                }
                if(referralId != null) {
                    PreferenceManager.setDefaultSharedPreferenceValue(context,
                            Constants.PREF_CAMPAIGN, Context.MODE_PRIVATE, referralId);
                }
            }
            if(referType != null) {
                try {
                    BroadcastReceiver e = (BroadcastReceiver)Class.forName("com.appsflyer.AppsFlyerLib").newInstance();
                    e.onReceive(context, intent);
                } catch (InstantiationException e) {
                    Crashlytics.logException(e);
                    (new AppsFlyerLib()).onReceive(context, intent);
                } catch (IllegalAccessException e) {
                    Crashlytics.logException(e);
                    (new AppsFlyerLib()).onReceive(context, intent);
                } catch (ClassNotFoundException e) {
                    Crashlytics.logException(e);
                    (new AppsFlyerLib()).onReceive(context, intent);
                }
            }
        }
    }
}
