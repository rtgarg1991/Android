package com.cashon.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 * Created by Rohit on 6/8/2015.
 */
public class Utility {
    /**
     * return whether internet is connected or not
     * @param context Context to be used to verify internet connectivity
     */
    public static boolean isInternetConnected(Context context) {
        // use ConnectivityManager and NetworkInfo API to check for internet connectivity
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


    /**
     * return whether user is registered or not
     * @param context Context to be used to check about user registration
     */
    public static boolean isUserRegistered(Context context) {
        // get shared preferences and check boolean value whether user is registered or not
        SharedPreferences pref = context.getSharedPreferences(Constants.CASHON_SHARED_PREF, Context.MODE_PRIVATE);
        boolean isUserRegistered = pref.getBoolean(Constants.USER_REGISTERED, false);

        return isUserRegistered;
    }

    /**
     * checks whether supplied text is valid Email Address
     * @param email Email Address to be checked
     * @return whether provided text is valid email id or not
     */
    public static boolean isValidEmail(String email) {
        if(TextUtils.isEmpty(email)) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * checks whether supplied text is valid Email Address
     * @param mobile Email Address to be checked
     * @return whether provided text is valid email id or not
     */
    public static boolean isValidMobile(String mobile) {
        if(TextUtils.isEmpty(mobile) || mobile.length() < 10) {
            return false;
        }
        return true;
    }
}
