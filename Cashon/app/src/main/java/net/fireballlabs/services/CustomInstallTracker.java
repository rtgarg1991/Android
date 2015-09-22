package net.fireballlabs.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.ParsePushBroadcastReceiver;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rohit on 7/9/2015.
 */
public class CustomInstallTracker extends BroadcastReceiver {
    private static final String TAG = "InstallReceiver";
    private static final String PID = "pid";
    private static final String REFERRAL_ID = "c";
    private static final String USER_INVITE = "User_invite";
    private static final String REFERRER = "referrer";

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
                                }
                            }
                        }
                    }
                } else if(key.equals(REFERRAL_ID)) {
                    referralId = extras.getString(key);
                } else if(key.equals(PID)) {
                    referType = extras.getString(key);
                }
            }
            Log.i("CG", "CIT:Referral Id : " + referralId);
            Log.i("CG", "CIT:pid : " + referType);

            if(referType != null && USER_INVITE.equals(referType) && referralId != null) {
                PreferenceManager.setDefaultSharedPreferenceValue(context,
                        Constants.PREF_REFERRAL_ID, Context.MODE_PRIVATE, referralId);
            }
        }
    }
}
