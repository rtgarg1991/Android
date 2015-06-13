package com.cashon.helper;

/**
 * Created by Rohit on 6/6/2015.
 */
public class Constants {
    /**
     * Version of the App
     */
    public static final int VERSION = 1;

    /**
     * TAG for the Logs to be printed
     */
    public static final String TAG = "CashOn";

    /**
     * To check Whether secure logging is on or not
     */
    public static final boolean DEBUG = true;

    /***
     * Name of the Shared Preference file for this application
     */
    public static final String CASHON_SHARED_PREF = "cashon_pref";

    /***
     * splash screen timeout time in milliseconds
     * to check for internet connectivity and user profile in device
     */
    public static final int SPLASH_SCREEN_TIMEOUT = 3000;

    /***
     * Temporary timeout time in milliseconds
     * just to wait for any retry attempt
     */
    public static final int TEMP_TIMEOUT = 500;

    /***
     * String value which will store whether user is registered or not
     */
    public static final String USER_REGISTERED = "user_registered";

    // Custom Exception Messages [START]
    public static final String EXCEPTION_INVALID_CONTEXT = "Context Invalid";
    public static final String EXCEPTION_INVALID_KEY = "Key Invalid";
    // Custom Exception Messages [END]
}
