package com.cashon.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.cashon.helper.Constants;
import com.cashon.helper.PreferenceManager;

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
     * @throws IllegalArgumentException If context provided is null
     */
    public static boolean isUserRegistered(Context context)
            throws IllegalArgumentException{
        return PreferenceManager.getDefaultSharedPreferenceValue(
                context, Constants.USER_REGISTERED, Context.MODE_PRIVATE, false);
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
