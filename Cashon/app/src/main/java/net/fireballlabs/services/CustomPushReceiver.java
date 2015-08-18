package net.fireballlabs.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.Referrals;

import com.crashlytics.android.Crashlytics;
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
            Crashlytics.logException(ex);
            Logger.doSecureLogging(Log.WARN, "Exception occurred while getting data from push notification");
        }
        if(obj != null) {
            if (obj.has("code") && obj.has("email")) {
                if(Constants.PUSH_NOTIFICATION_REFERRAL == obj.optInt("code")) {
//                    Referrals.verifyReferralAndCredit(obj.optString("email", null));
                } else if(Constants.PUSH_NOTIFICATION_INSTALL_CONVERSION == obj.optInt("code")) {
                    //Referrals.verifyReferralAndCredit(obj.optString("email", null));
                    // do nothing for now
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
