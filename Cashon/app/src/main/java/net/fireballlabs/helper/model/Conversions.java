package net.fireballlabs.helper.model;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.PreferenceManager;

import java.util.HashMap;

/**
 * Created by Rohit on 8/9/2015.
 */
public class Conversions {

    public static float getBalance(Context context, boolean force) {
        if(force || PreferenceManager.getDefaultSharedPreferenceValue(context, Constants.PREF_NEED_WALLET_REFRESH, context.MODE_PRIVATE, true)
                || (PreferenceManager.getDefaultSharedPreferenceValue(context, Constants.PREF_WALLET_BALANCE, context.MODE_PRIVATE, -100.0f) < -10.0f)) {
            ParseUser user = ParseUser.getCurrentUser();
            if (user == null || !user.isAuthenticated()) {
                return 0;
            }
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", user.getObjectId());
            try {
                Object amount = ParseCloud.callFunction("GetUserBalance", params);
                if (amount instanceof Integer) {
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_NEED_WALLET_REFRESH, context.MODE_PRIVATE, false);
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_WALLET_BALANCE, context.MODE_PRIVATE, Float.valueOf((int) amount));
                    return Float.valueOf((int) amount);
                } else {
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_NEED_WALLET_REFRESH, context.MODE_PRIVATE, false);
                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_WALLET_BALANCE, context.MODE_PRIVATE, (float) amount);
                    return (float) amount;
                }
            } catch (ParseException e) {
                Crashlytics.logException(e);
            }

        } else {
            return PreferenceManager.getDefaultSharedPreferenceValue(context, Constants.PREF_WALLET_BALANCE, context.MODE_PRIVATE, 0.0f);
        }

        return 0;
    }
}
