package com.cashon.helper;

/**
 * Created by Rohit on 6/6/2015.
 */
public class Constants {
    /**
     * Version of the App
     */
    public static final int VERSION = 1;
    public static final String APP_NAME = "APP MANAGER";

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
    public static final String PREF_USER_REGISTERED = "user_registered";
    public static final String PREF_FINISHED_SYNC = "sync_done";

    // Custom Exception Messages [START]
    public static final String EXCEPTION_INVALID_CONTEXT = "Context Invalid";
    public static final String EXCEPTION_INVALID_KEY = "Key Invalid";
    // Custom Exception Messages [END]

    // Feature List [START]
    // We need Titles and Unique IDs of Feature Only
    // TODO Save class reference also for faster processing
    public static final String TITLE_APP_INSTALLS = "App Installs";
    public static final int ID_APP_INSTALLS = 01;

    public static final String TITLE_APP_LATEST_DEALS = "Latest Deals";
    public static final int ID_APP_LATEST_DEALS = 02;

    public static final String TITLE_APP_REFER = "Refer Friends";
    public static final int ID_APP_REFER = 03;

    public static final String TITLE_APP_SETTINGS = "App Settings";
    public static final int ID_APP_SETTINGS = 04;

    public static final String TITLE_APP_HELP = "Help";
    public static final int ID_APP_HELP = 05;

    public static final String TITLE_PENDING_INSTALLS = "Pending Installs";
    public static final int ID_PENDING_INSTALLS = 06;

    public static final String TITLE_COMPLETED_INSTALLS = "Completed Installs";
    public static final int ID_COMPLETED_INSTALLS = 07;

    public static final String TITLE_APP_CONTACT_US = "Contact Us";
    public static final int ID_APP_CONTACT_US = 8;

    public static final String INR_TEXT = "INR";
    public static final String INR_LABEL = "\u20B9";
    public static final String TEXT_REFERAL = "Install CashOn Application, Use my referal code %s to sign up and earn " + INR_LABEL + "15";
    public static final int REFERRAL_PUSH_NOTIFICATION = 1;
    public static final int MAX_PAYOUT_APP_INSTALL = 15;
    // Feature List [END]
    public static boolean appInstallSyncNeeded = true;



    public static final String REQUEST_CODE_STRING = "REQUEST";
    public static final String INTENT_UID = "UID";
    public static final String INTENT_PACKAGE = "PACKAGE";
    public static final String CALLBACK_OBJECT = "CALLBACK";
    public static final String PACKAGE_DATA_START = "package:";

    public static final int MESSAGE_CREATE_SERVICE = 1;
    public static final int MESSAGE_ADD_PACKAGES = 2;
    public static final int MESSAGE_ADD_PACKAGE = 3;
    public static final int MESSAGE_DELETE_PACKAGE = 4;
}
