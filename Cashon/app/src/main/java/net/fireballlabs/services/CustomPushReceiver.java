package net.fireballlabs.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.NotificationHelper;
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
        JSONObject obj = null;
        try {
            obj = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException ex) {
            Crashlytics.logException(ex);
            Logger.doSecureLogging(Log.WARN, "Exception occurred while getting data from push notification");
        }
        if(obj != null) {
            if (obj.has("code")) {
                if(Constants.PUSH_NOTIFICATION_CLOUD_DATA_CHANGED == obj.optInt("code")) {
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_CLOUD_DATA_CHANGED, context.MODE_PRIVATE, true);
                } else if(Constants.PUSH_NOTIFICATION_RECHARGE_DONE == obj.optInt("code")) {
                    super.onPushReceive(context, intent);
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_MOBILE_RECHARGE_DONE, context.MODE_PRIVATE, obj.optString("amount"));
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_NEED_WALLET_REFRESH, context.MODE_PRIVATE, true);
                    NotificationHelper.addNotification(context, Integer.parseInt(obj.optString("amount")), obj.optInt("code"),
                            obj.optString("extra"), obj.optString("alert"), obj.optString("refId"));
                } else {
                    super.onPushReceive(context, intent);
                    if (Constants.PUSH_NOTIFICATION_REFERRAL == obj.optInt("code")) {
                        PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_NEED_WALLET_REFRESH, context.MODE_PRIVATE, true);
                        NotificationHelper.addNotification(context, Integer.parseInt(obj.optString("amount")), obj.optInt("code"),
                                obj.optString("extra"), obj.optString("alert"), "");
                    } else if (Constants.PUSH_NOTIFICATION_INSTALL_CONVERSION == obj.optInt("code")) {
                        PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_NEED_WALLET_REFRESH, context.MODE_PRIVATE, true);
                        NotificationHelper.addNotification(context, Integer.parseInt(obj.optString("amount")), obj.optInt("code"),
                                obj.optString("extra"), obj.optString("alert"), obj.optString("refId"));
                    }
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
