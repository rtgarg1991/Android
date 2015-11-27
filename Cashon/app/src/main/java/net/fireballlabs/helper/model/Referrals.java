package net.fireballlabs.helper.model;

import android.content.Context;
import android.util.Log;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static void syncReferralBonus(final Context context) {
        if(context != null) {
            Map<String, String> params = new HashMap<String, String>();
            ParseCloud.callFunctionInBackground("getReferralBonus", params, new FunctionCallback<ArrayList<HashMap<String, Object>>>() {
                public void done(ArrayList<HashMap<String, Object>> object, ParseException e) {
                    if (e == null) {
                        if(object != null) {
                            boolean firstDone = false;
                            for(HashMap<String, Object> obj : object) {
                                int type = (Integer) obj.get("type");
                                int count = (Integer) obj.get("count");
                                int payout = (Integer) obj.get("payout");

                                if(type == 1) {
                                    if(!firstDone) {
                                        PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_REFERRAL_BONUS_1, Context.MODE_PRIVATE, (int)payout);
                                        PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_REFERRAL_BONUS_COUNT_1, Context.MODE_PRIVATE, (int)count);
                                        firstDone = true;
                                    } else {
                                        PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_REFERRAL_BONUS_2, Context.MODE_PRIVATE, (int)payout);
                                        PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_REFERRAL_BONUS_COUNT_2, Context.MODE_PRIVATE, (int)count);
                                    }
                                } else if(type == 2) {
                                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_REFERRAL_BONUS_3, Context.MODE_PRIVATE, (int)payout);
                                    PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_REFERRAL_BONUS_COUNT_3, Context.MODE_PRIVATE, (int)count);
                                }
                            }
                        }
                    } else {
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
                            if(parseObject.get(PARSE_TABLE_COLUMN_USER_ID).equals(user.getObjectId())
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
