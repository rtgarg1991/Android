package net.fireballlabs.impl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;

/**
 * Created by Rohit on 6/14/2015.
 */
public class HardwareAccess {
    private static final String MESSAGE_INTERNET_CONNECT = "Please check your internet connection.";
    private static final String POSITIVE_INTERNET_CONNECT = "WiFi";
    private static final String NEUTRAL_INTERNET_CONNECT = "Mobile Network";
    private static final String NEGATIVE_INTERNET_CONNECT = "Try again";

    public static final int ACCESS_INTERNET = 0x01;

    /**
     * Call this function to access some hardware feature in the device
     * You will be notified by calling {@link HardwareAccess.HardwareAccessCallbacks#accessCompleted(int, boolean) after trying to get access}
     * @param context Context to be used for enabling the feature
     * @param callback Callback whose {@link HardwareAccess.HardwareAccessCallbacks#accessCompleted} will be called
     * @param accessFeature Feature needed by the application
     */
    public static void access(Context context, HardwareAccessCallbacks callback, int accessFeature) {
        Logger.doSecureLogging(Log.DEBUG, "Trying to Access Feature : " + accessFeature);
        if(context == null) {
            Logger.doSecureLogging(Log.DEBUG, "Provided context is null, so throwing IllegalArgumentException");
            throw new IllegalArgumentException(Constants.EXCEPTION_INVALID_CONTEXT);
        }
        switch (accessFeature) {
            case ACCESS_INTERNET:
                accessInternet(context, callback);
                break;
        }
    }

    /**
     * Function to show Alert Dialog to user to enable internet in the device
     * @param context Context to be used for enabling the feature
     * @param callback Callback whose {@link HardwareAccess.HardwareAccessCallbacks#accessCompleted} will be called
     *                 with true, if user is trying to enable the internet, otherwise with false
     *                 note : true doesn't mean user actually enabled the internet
     *                 need to recheck by the calling component about internet availability
     */
    private static void accessInternet(final Context context, final HardwareAccessCallbacks callback) {
        Logger.doSecureLogging(Log.DEBUG, "Trying to Access Internet by showing dialog to user");
        AlertDialog.Builder builder = null;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(context);
        } else {
            builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        }
        builder.setMessage(MESSAGE_INTERNET_CONNECT)
                .setCancelable(false)
                /*.setPositiveButton(POSITIVE_INTERNET_CONNECT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Logger.doSecureLogging(Log.DEBUG, "User chose to enable internet by enabling WiFi");
                        // Open WiFi Settings Activity
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        callback.accessCompleted(ACCESS_INTERNET, true);
                    }
                })
                .setNeutralButton(NEUTRAL_INTERNET_CONNECT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.doSecureLogging(Log.DEBUG, "User chose to enable internet by enabling Mobile Data");
                        // Open Mobile Data Settings Activity
                        context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                        callback.accessCompleted(ACCESS_INTERNET, true);
                    }
                })*/
                .setNegativeButton(NEGATIVE_INTERNET_CONNECT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Logger.doSecureLogging(Log.DEBUG, "User chose not to enable Internet right now");
                        // Notify calling component about user denial to enable the internet
                        callback.accessCompleted(ACCESS_INTERNET, true);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public interface HardwareAccessCallbacks {
        /**
         * Function which will be called by {@link HardwareAccess} Class to notify the requesting class
         * about user behaviour for enabling disabling some feature
         * @param access Integer for which {@link HardwareAccess} Class was used
         * @param isSuccess Whether user tried to enable the feature or not
         */
        void accessCompleted(int access, boolean isSuccess);
    }
}
