package net.fireballlabs.helper.model;

import android.util.Log;

import net.fireballlabs.helper.Logger;

import com.crashlytics.android.Crashlytics;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Rohit on 8/8/2015.
 */
public class Referrals {
    String referCode;
    String userEmail;
    boolean creditedAtReferral;
    boolean creditedAtReferred;

    private static String PARSE_TABLE_NAME_REFERRALS = "Referrals";

    private static String PARSE_TABLE_COLUMN_AMOUNT = "amount";
    private static String PARSE_TABLE_COLUMN_REFERRER = "referrer";
    private static String PARSE_TABLE_COLUMN_REFERRAL = "referral";
    private static String PARSE_TABLE_COLUMN_CONVERTED = "converted";

    public static void addReferral(String referral, final String referrer) {
        if (referral != null && referrer != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(PARSE_TABLE_COLUMN_REFERRER, referrer);
            params.put(PARSE_TABLE_COLUMN_REFERRAL, referral);
            ParseCloud.callFunctionInBackground("addNewReferral", params, new FunctionCallback<Boolean>() {
                public void done(Boolean success, ParseException e) {
                    if (e == null) {
                        Logger.doSecureLogging(Log.DEBUG, "Referral sent successfully.");
                    } else {
                        Logger.doSecureLogging(Log.WARN, "Referral not sent successfully." + e.getCode());
                        Crashlytics.logException(e);
                    }
                }
            });
        }
    }
/*


    public static void verifyReferralAndCredit(final String email) {
        final ParseUser user = ParseUser.getCurrentUser();
        if(email != null && user != null && user.isAuthenticated()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_REFERRALS);
            query.whereEqualTo(PARSE_TABLE_COLUMN_USER_EMAIL, email);
            query.whereEqualTo(PARSE_TABLE_COLUMN_CREDITED_AT_REFERRER, false);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject parseObject, ParseException e) {
                    if(e == null) {
                        if(parseObject == null) {
                            // TODO no such referral present
                        } else {
                            if(parseObject.get(PARSE_TABLE_COLUMN_REFER_CODE).equals(user.getObjectId())
                                    && !((Boolean)parseObject.get(PARSE_TABLE_COLUMN_CREDITED_AT_REFERRER))) {

                                // credit amount in current user's account
                                HashMap<String, Object> params = new HashMap<String, Object>();
                                params.put(PARSE_TABLE_COLUMN_TO_USER_EMAIL, user.getUsername());
                                params.put(PARSE_TABLE_COLUMN_FROM_USER_EMAIL, email);
                                params.put(PARSE_TABLE_COLUMN_TO_USER_CODE, user.getObjectId());
                                params.put(PARSE_TABLE_COLUMN_FROM_USER_CODE, parseObject.get(PARSE_TABLE_COLUMN_USER_CODE));
                                params.put(PARSE_TABLE_COLUMN_AMOUNT, parseObject.get(PARSE_TABLE_COLUMN_AMOUNT));
                                ParseCloud.callFunctionInBackground("addReferral", params, new FunctionCallback<Boolean>() {
                                    public void done(Boolean success, ParseException e) {
                                        if (e == null) {
                                            Logger.doSecureLogging(Log.DEBUG, "Referral sent successfully.");
                                            // set boolean variable showing amount is already credited to referrer's account
                                            parseObject.put("creditedAtReferrer", true);
                                            parseObject.saveEventually();
                                        } else {
                                            Logger.doSecureLogging(Log.WARN, "Referral not sent successfully." + e.getCode());
                                            Crashlytics.logException(e);
                                        }
                                    }
                                });
                            } else {
                                Logger.doSecureLogging(Log.INFO, "Either referral is already credited or refercode doesn't match with this user");
                                // TODO log it on server
                            }
                        }
                    } else {
                        // TODO handle error
                        Crashlytics.logException(e);
                    }
                }
            });
        }
    }
*/

    public static class ReferralMap {
        int referrer;
        int referral;
        int installCount;
        float amount;
    }
}
