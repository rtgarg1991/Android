package net.fireballlabs.helper.model;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by Rohit on 8/9/2015.
 */
public class Conversions {
    public static String PARSE_TABLE_NAME_CONVERSIONS = "Conversions";
    public static String PARSE_TABLE_COLUMN_EMAIL_ID = "emailId";
    public static String PARSE_TABLE_COLUMN_DEVICE_ID = "deviceId";
    public static String PARSE_TABLE_COLUMN_PAYOUT = "payout";
    public static String PARSE_TABLE_COLUMN_TYPE = "type";
    public static String PARSE_TABLE_COLUMN_COMMENT = "comment";

    public static String TYPE_REFERRAL = "Referral";
    public static String TYPE_APP_INSTALL = "AppInstall";

    public static float getBalance() {
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return 0;
        }
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_CONVERSIONS);
        query.whereEqualTo(PARSE_TABLE_COLUMN_EMAIL_ID, user.getUsername());
        try {
            List<ParseObject> conversions = query.find();
            float totalBalance = 0.0f;
            for(ParseObject conversion : conversions) {
                totalBalance += conversion.getInt(PARSE_TABLE_COLUMN_PAYOUT);
            }
            return totalBalance;
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("deviceId", user.getObjectId());
        try {
            Object amount = ParseCloud.callFunction("GetUserBalance", params);
            if(amount instanceof Integer) {
                return Float.valueOf((int)amount);
            } else {
                return (float)amount;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
