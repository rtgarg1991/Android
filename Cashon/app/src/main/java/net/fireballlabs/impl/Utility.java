package net.fireballlabs.impl;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.sql.SQLWrapper;

import java.util.Random;

/**
 * Created by Rohit on 6/8/2015.
 */
public class Utility {
    private static MaterialDialog mProgressDialog;
    private static int shown = 0;
    private static MaterialDialog mInformationDialog;

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
        /*if(TextUtils.isEmpty(mobile) || mobile.length() < 10 || mobile.charAt(0) < 7) {
            return false;
        }
        return true;*/
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
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
    public static final String AFF_LINK_USER_ID = "{user_id}";
    public static final String AFF_LINK_DEVICE_ID = "{device_id}";
    public static final String AFF_LINK_OFFER_ID = "{offer_id}";
    public static final String AFF_LINK_TYPE = "{type}";
    public static final String AFF_LINK_S_ID = "{sid}";

    public static final String DENSITY_MDPI = "mdpi";
    public static final String DENSITY_HDPI = "hdpi";
    public static final String DENSITY_XHDPI = "xhdpi";
    public static final String DENSITY_XXHDPI = "xxhdpi";
    public static final String DENSITY_XXXHDPI = "xxxhdpi";

    public static String getRefUrlString(String affLink, String userId, String deviceId, String offerId) {
        return affLink.replace(AFF_LINK_USER_ID, userId)
                .replace(AFF_LINK_DEVICE_ID, deviceId)
                .replace(AFF_LINK_OFFER_ID, offerId)
                .replace(AFF_LINK_TYPE, "2")
                .replace(AFF_LINK_S_ID, userId + "_" + deviceId + "_" + offerId + "_" + 2);
//        return userId + "_" + id + "_" + 1;
//        return String.format(Locale.US, REF_URL_STRING, userId, generateRandomString());
    }

    public static String getAppShareUrlString(String affLink, String userId, String offerId) {
        return affLink.replace(AFF_LINK_USER_ID, userId)
                .replace(AFF_LINK_DEVICE_ID, "")
                .replace(AFF_LINK_OFFER_ID, offerId)
                .replace(AFF_LINK_TYPE, "")
                .replace(AFF_LINK_S_ID, "");
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

    public static void showProgress(Context context, boolean show, String text) {
        if(show) {
//            shown++;
            if(mProgressDialog != null && mProgressDialog.isShowing()) {

            } else {
                mProgressDialog = new MaterialDialog.Builder(context)
                        .content(text)
                        .progress(true, 0)
                        .autoDismiss(false)
                        .cancelable(false)
                        .widgetColor(context.getResources().getColor(R.color.primary))
                        .show();
            }
        } else {
            if(mProgressDialog != null && mProgressDialog.isShowing()) {
//                shown--;
//                if(shown == 0) {
                    mProgressDialog.dismiss();
                mProgressDialog = null;
//                }
            }
        }

    }

    public static boolean isValidRechargeAmount(String amountString, float balance) {
        try {
            int amount = Integer.parseInt(amountString);
            if(amount >= Constants.RECHARGE_AMOUNT_MIN && amount <= Constants.RECHARGE_AMOUNT_MAX && amount <= balance) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException ex) {
            Crashlytics.logException(ex);
            return false;
        }
    }
    static MaterialDialog firstTimeDialog = null;

    public static void showFirstTimePopup(Context context, boolean show) {
        if(show) {
            if(firstTimeDialog != null && firstTimeDialog.isShowing()) {
                try {
                    firstTimeDialog.dismiss();
                    firstTimeDialog = null;
                } catch(IllegalArgumentException ex) {
                    firstTimeDialog = null;
                    Crashlytics.logException(ex);
                }
            }
            firstTimeDialog = new MaterialDialog.Builder(context)
                    .title(R.string.new_user_popup_title)
                    .content(R.string.new_user_popup_content)
                    .positiveText(R.string.new_user_popup_button)
                    .positiveColor(context.getResources().getColor(R.color.primary))
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    })
                    .show();
        } else {
            if(firstTimeDialog != null && firstTimeDialog.isShowing()) {
                firstTimeDialog.dismiss();
                firstTimeDialog = null;
            }
        }
    }

    public static void showInformativeDialog(final DialogCallback callback, Context context, String title,
                                             String content, String positiveButtonText, boolean show) {

        if(isValidContext(context)) {
            showInformativeDialog(callback, context, title, content, positiveButtonText, null, show);
        }
    }

    public static void showInformativeDialog(final DialogCallback callback, Context context, String title,
                                             String content, String positiveButtonText, String negativeButtonText,
                                             boolean show) {

        if(show) {
            if (mInformationDialog != null && mInformationDialog.isShowing()) {
                mInformationDialog.dismiss();
                mInformationDialog = null;
            }
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .content(content)
                    .positiveText(positiveButtonText == null ? "OK" : positiveButtonText)
                    .title(title)
                    .positiveColor(context.getResources().getColor(R.color.primary))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                if (callback != null) {
                                    callback.onDialogCallback(true);
                                }
                            }
                        }
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                if (callback != null) {
                                    callback.onDialogCallback(false);
                                }
                            }
                        }
                    });
            if(negativeButtonText != null) {
                builder.negativeText(negativeButtonText);
            }
            mInformationDialog = builder.show();
        } else {
            if (mInformationDialog != null && mInformationDialog.isShowing()) {
                mInformationDialog.dismiss();
                mInformationDialog = null;
            }
        }
    }

    public static String getDeviceDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
            case DisplayMetrics.DENSITY_MEDIUM:
                return DENSITY_MDPI;
            case DisplayMetrics.DENSITY_HIGH:
                return DENSITY_HDPI;
            case DisplayMetrics.DENSITY_XHIGH:
                return DENSITY_XHDPI;
            case DisplayMetrics.DENSITY_XXHIGH:
                return DENSITY_XXHDPI;
            case DisplayMetrics.DENSITY_XXXHIGH:
                return DENSITY_XXXHDPI;
            default:
                return DENSITY_MDPI;
        }
    }

    public interface DialogCallback {
        public void onDialogCallback(boolean success);
    }

    public static String generateDeviceUniqueId(Context context) {
        // GET DEVICE ID
        // as per Android blog
        // this unique id has bug in major manufacturer, so lets leave it for now
        /*final String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);*/

        // serial number of the device
        // it is helpful where telephony services are not available
        final String deviceSerialNumber = Build.SERIAL;

        // GET IMEI NUMBER
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceIMEI = tManager.getDeviceId();
        // lets just return device imei id for now
        // for wifi devices, it will be garbage entry
        // for devices without telephony this will be garbage
        return deviceIMEI;
    }

    public static Boolean isValidContext(Context context) {
        if(context == null) {
            return false;
        }
        if(context instanceof Activity) {
            if(((Activity)context).isFinishing()) {
                return false;
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if(((Activity)context).isDestroyed()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
