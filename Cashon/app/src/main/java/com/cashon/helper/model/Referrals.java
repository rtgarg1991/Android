package com.cashon.helper.model;

import android.util.Log;

import com.cashon.helper.Logger;
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

/**
 * Created by Rohit on 8/8/2015.
 */
public class Referrals {
    String referCode;
    String userEmail;
    boolean creditedAtReferral;
    boolean creditedAtReferred;

    private static String PARSE_TABLE_NAME_REFERRALS = "Referrals";
    private static String PARSE_TABLE_COLUMN_USER_EMAIL = "userEmail";
    private static String PARSE_TABLE_COLUMN_USER_CODE = "userCode";
    private static String PARSE_TABLE_COLUMN_REFER_CODE = "referCode";
    private static String PARSE_TABLE_COLUMN_CREDITED_AT_REFERRER = "creditedAtReferrer";
    private static String PARSE_TABLE_COLUMN_CREDITED_AT_REFERRED = "creditedAtReferred";
    private static String PARSE_TABLE_COLUMN_AMOUNT = "amount";

    private static String PARSE_TABLE_COLUMN_TO_USER_EMAIL = "toUserEmail";
    private static String PARSE_TABLE_COLUMN_FROM_USER_EMAIL = "fromUserEmail";
    private static String PARSE_TABLE_COLUMN_TO_USER_CODE = "toUserCode";
    private static String PARSE_TABLE_COLUMN_FROM_USER_CODE = "fromUserCode";

    public static void addReferral(String email, final String referCode) {
        if (referCode != null) {
            final ParseUser user = ParseUser.getCurrentUser();
            final ParseInstallation installation = ParseInstallation.getCurrentInstallation();

            ParseObject object = new ParseObject(PARSE_TABLE_NAME_REFERRALS);
            object.put(PARSE_TABLE_COLUMN_USER_EMAIL, email);
            object.put(PARSE_TABLE_COLUMN_USER_CODE, installation.getObjectId());
            object.put(PARSE_TABLE_COLUMN_REFER_CODE, referCode);
            object.put(PARSE_TABLE_COLUMN_CREDITED_AT_REFERRER, false);
            object.put(PARSE_TABLE_COLUMN_CREDITED_AT_REFERRED, false);
            object.put(PARSE_TABLE_COLUMN_AMOUNT, 10);

            // set public access so that referrer can access this entry
            ParseACL groupACL = new ParseACL(user);
            groupACL.setPublicWriteAccess(true);
            groupACL.setPublicReadAccess(true);
            object.setACL(groupACL);

            // TODO change amount
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        // TODO log errors
                    }
                }
            });
        }
    }


    public static void verifyReferralAndCredit(final String email) {
        final ParseUser user = ParseUser.getCurrentUser();
        if(email != null && user != null && user.isAuthenticated()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_REFERRALS);
            query.whereEqualTo(PARSE_TABLE_COLUMN_USER_EMAIL, email);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject parseObject, ParseException e) {
                    if(e == null) {
                        if(parseObject == null) {
                            // TODO no such referral present
                        } else {
                            if(parseObject.get(PARSE_TABLE_COLUMN_REFER_CODE).equals(ParseInstallation.getCurrentInstallation().getObjectId())
                                    && !((Boolean)parseObject.get(PARSE_TABLE_COLUMN_CREDITED_AT_REFERRER))) {

                                // credit amount in current user's account
                                HashMap<String, Object> params = new HashMap<String, Object>();
                                params.put(PARSE_TABLE_COLUMN_TO_USER_EMAIL, user.getUsername());
                                params.put(PARSE_TABLE_COLUMN_FROM_USER_EMAIL, email);
                                params.put(PARSE_TABLE_COLUMN_TO_USER_CODE, ParseInstallation.getCurrentInstallation().getObjectId());
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
                                            e.printStackTrace();
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
                    }
                }
            });
        }
    }
}
