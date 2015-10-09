package net.fireballlabs.helper;

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
    public static final String TAG = "CashGuru";

    /**
     * To check Whether secure logging is on or not
     */
    public static final boolean DEBUG = false;

    /***
     * Name of the Shared Preference file for this application
     */
    public static final String CASHGURU_SHARED_PREF = "cashguru_pref";

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
    public static final String PREF_FIRST_TIME = "first_time";
    public static final String PREF_CLOUD_DATA_CHANGED = "cloud_data_changed";
    public static final String PREF_MOBILE_VERIFIED = "mobile_verified";
    public static final String PREF_REFERRAL_ID = "referral_id";
    public static final String PREF_MOBILE_RECHARGE_DONE = "mobile_recharge_done";
    public static final String PREF_NEED_WALLET_REFRESH = "need_wallet_refresh";
    public static final String PREF_WALLET_BALANCE = "wallet_balance";

    // Custom Exception Messages [START]
    public static final String EXCEPTION_INVALID_CONTEXT = "Context Invalid";
    public static final String EXCEPTION_INVALID_KEY = "Key Invalid";
    // Custom Exception Messages [END]

    // Feature List [START]
    // We need Titles and Unique IDs of Feature Only
    // TODO Save class reference also for faster processing
    public static final String TITLE_APP_INSTALLS = "Offer Wall";
    public static final int ID_APP_INSTALLS = 1;

    public static final String TITLE_APP_LATEST_DEALS = "Hot Deals";
    public static final int ID_APP_LATEST_DEALS = 2;

    public static final String TITLE_APP_REFER = "Refer & Earn";
    public static final int ID_APP_REFER = 3;

    public static final String TITLE_APP_TANDC = "T & C";
    public static final int ID_APP_TANDC = 4;

    public static final String TITLE_APP_FAQ = "FAQ";
    public static final int ID_APP_FAQ = 5;

    public static final String TITLE_PENDING_INSTALLS = "Pending";
    public static final int ID_PENDING_INSTALLS = 6;

    public static final String TITLE_COMPLETED_INSTALLS = "Completed";
    public static final int ID_COMPLETED_INSTALLS = 7;

    public static final String TITLE_APP_CONTACT_US = "Contact Us";
    public static final int ID_APP_CONTACT_US = 8;

    public static final String TITLE_APP_RECHARGE = "Top Up";
    public static final int ID_APP_RECHARGE = 9;

    public static final String TITLE_APP_PROFILE = "Profile";
    public static final int ID_APP_PROFILE = 10;

    public static final String TITLE_APP_RECHARGE_HISTORY = "History";
    public static final int ID_APP_RECHARGE_HISTORY = 11;

    public static final String TITLE_APP_RECHARGE_NOTIFICATION = "Notification";
    public static final int ID_APP_NOTIFICATION = 12;

    public static final String TITLE_APP_OFFER = "Offer";
    public static final int ID_APP_OFFER = 13;

    public static final String TITLE_APP_CONTACT_US_HISTORY = "History";
    public static final int ID_APP_CONTACT_US_HISTORY = 14;

    public static final String INR_TEXT = "INR";
    public static final String RS_TEXT = "Rs. ";
    public static final String INR_LABEL = "\u20B9";
    public static final String TEXT_REFERAL = "Hey! Checkout CashGuru, Its the best way to get free recharge. Install now and get ₹5 bonus on installing any 2 apps! Get CashGuru %s";
    public static final int PUSH_NOTIFICATION_REFERRAL = 2;
    public static final int PUSH_NOTIFICATION_INSTALL_CONVERSION = 1;
    public static final int PUSH_NOTIFICATION_CLOUD_DATA_CHANGED = 11;
    public static final int PUSH_NOTIFICATION_RECHARGE_DONE = 12;
    public static final int MAX_PAYOUT_APP_INSTALL = 15;
    public static final String IS_NEW_LOGIN = "is_new_login";
    public static final String OTP_MESSAGE = "Your OTP for CashGuru is %s";
    public static final String MOBILE_NUMBER = "mobile_number";
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
    public static final int RECHARGE_AMOUNT_MIN = 10;
    public static final int RECHARGE_AMOUNT_MAX = 100;

    public static final String RECHARGE_SENT_SUCCESSFUL = "Recharge details sent successfully.\r\nYour recharge will be done within 24 hours";
    public static final String CONTACT_US_SENT_SUCCESSFUL = "Query sent successfully.";

    public static final String REFERRAL_CODE_COPIED = "Referral code copied to clipboard";
    public static final String COUPON_CODE_COPIED = "Coupon code copied!";

    public static final String FAQ_URI = "http://cashguru.fireballlabs.net/faq";
    public static final String TNC_URI = "http://cashguru.fireballlabs.net/tnc";
}
