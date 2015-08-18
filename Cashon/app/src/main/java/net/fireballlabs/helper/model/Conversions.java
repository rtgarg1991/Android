package net.fireballlabs.helper.model;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by Rohit on 8/9/2015.
 */
public class Conversions {

    public static float getBalance() {
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return 0;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getObjectId());
        try {
            Object amount = ParseCloud.callFunction("GetUserBalance", params);
            if(amount instanceof Integer) {
                return Float.valueOf((int)amount);
            } else {
                return (float)amount;
            }
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }

        return 0;
    }
}
