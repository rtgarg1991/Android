package com.cashon.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cashon.helper.Constants;
import com.cashon.helper.Logger;
import com.cashon.helper.model.Referrals;
import com.cashon.impl.Utility;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rohit on 7/9/2015.
 */
public class CustomPushReceiver extends ParsePushBroadcastReceiver {
    public CustomPushReceiver() {
        super();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        JSONObject obj = null;
        try {
            obj = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException ex) {
            Logger.doSecureLogging(Log.WARN, "Exception occurred while getting data from push notification");
        }
        if(obj != null) {
            if (obj.has("code") && obj.has("email")) {
                if(Constants.REFERRAL_PUSH_NOTIFICATION == obj.optInt("code")) {
                    Referrals.verifyReferralAndCredit(obj.optString("email", null));
                }
            }
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return super.getNotification(context, intent);
    }
}
