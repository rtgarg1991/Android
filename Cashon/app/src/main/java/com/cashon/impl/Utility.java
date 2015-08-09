package com.cashon.impl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cashon.adapter.MainDrawerAdapter;
import com.cashon.helper.Constants;
import com.cashon.helper.Logger;
import com.cashon.helper.PreferenceManager;
import com.cashon.sql.SQLWrapper;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Rohit on 6/8/2015.
 */
public class Utility {
    private static MaterialDialog mProgressDialog;
    private static int shown = 0;

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
                context, Constants.PREF_USER_REGISTERED, Context.MODE_PRIVATE, false);
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


    /**
     * checks whether supplied text is valid Password string or not
     * @param password Password string to be checked
     * @return whether provided text is valid password or not
     */
    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public static List<MainDrawerAdapter.MainAppFeature> prepareFeatureList() {
        // TODO Build Feature list based on requiement
        // We can add Country checks or some other checks based on other information to decide feature list
        // For now returning only App Installs based Feature
        List<MainDrawerAdapter.MainAppFeature> featureList = new ArrayList<MainDrawerAdapter.MainAppFeature>();
        featureList.add(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS, Constants.ID_APP_INSTALLS));
        featureList.add(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_LATEST_DEALS, Constants.ID_APP_LATEST_DEALS));
        featureList.add(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_REFER, Constants.ID_APP_REFER));
        featureList.add(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_CONTACT_US, Constants.ID_APP_CONTACT_US));

        featureList.add(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_SETTINGS, Constants.ID_APP_SETTINGS));
        featureList.add(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_HELP, Constants.ID_APP_HELP));
        return featureList;
    }

    public static String getSuitableImage(SQLWrapper.Offer.Images images, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return images.ldpi;
            case DisplayMetrics.DENSITY_MEDIUM:
                return images.mdpi;
            case DisplayMetrics.DENSITY_HIGH:
                return images.hdpi;
            case DisplayMetrics.DENSITY_XHIGH:
                return images.xhdpi;
            default:
                if(metrics.densityDpi > DisplayMetrics.DENSITY_XHIGH)
                    return images.xhdpi;
                else if(metrics.densityDpi < DisplayMetrics.DENSITY_MEDIUM)
                    return images.ldpi;
                else
                    return images.hdpi;
        }
    }

    public static void setSuitableImage(SQLWrapper.Offer.Images images, Context context, String image) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                images.ldpi = image;
            case DisplayMetrics.DENSITY_MEDIUM:
                images.mdpi = image;
            case DisplayMetrics.DENSITY_HIGH:
                images.hdpi = image;
            case DisplayMetrics.DENSITY_XHIGH:
                images.xhdpi = image;
            default:
                if(metrics.densityDpi > DisplayMetrics.DENSITY_XHIGH)
                    images.xhdpi = image;
                else if(metrics.densityDpi < DisplayMetrics.DENSITY_MEDIUM)
                    images.ldpi = image;
                else
                    images.hdpi = image;
        }
    }

    public static String getCurrencySymbol(String currency) {
        if(Constants.INR_TEXT.equals(currency)) {
            return Constants.INR_LABEL;
        }
        return Constants.INR_LABEL;
    }

    public static int MAX_LENGTH = 15;
    public static int MAX_RANGE = 5;
    public static String REF_URL_STRING = "%s_%s";

    public static String BASE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String getRefUrlString(String userId, String id) {
        return userId + "_" + id + "_" + 1;
//        return String.format(Locale.US, REF_URL_STRING, userId, generateRandomString());
    }
    public static String generateRandomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_RANGE);
        randomLength = MAX_LENGTH - randomLength;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) BASE.charAt(generator.nextInt(BASE.length()));
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static String getRefUrlStringWithoutOfferSubType(String userId, String id) {
        return userId + "_" + id;
    }

    public static void showProgress(Context context, boolean show, String text) {
        if(show) {
            shown++;
            if(mProgressDialog != null && mProgressDialog.isShowing()) {

            } else {
                mProgressDialog = new MaterialDialog.Builder(context)
                        .content(text)
                        .progress(true, 0)
                        .autoDismiss(false)
                        .show();
            }
        } else {
            if(mProgressDialog != null && mProgressDialog.isShowing()) {
                shown--;
                if(shown == 0) {
                    mProgressDialog.dismiss();
                }
            }
        }

    }
}
