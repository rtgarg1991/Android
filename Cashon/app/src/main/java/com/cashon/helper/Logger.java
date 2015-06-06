package com.cashon.helper;

import static com.cashon.helper.Constants.TAG;
import static com.cashon.helper.Constants.DEBUG;

import android.util.Log;

/**
 * Created by Rohit on 6/6/2015.
 */
public class Logger {
    /**
     * Only printed if DEBUG is ON
     * @param type type of the log to print
     * @param log String message for the log
     */
    public static void doSecureLogging(int type, String log) {
        if(!DEBUG) {
            return;
        }
        doLogging(type, log);
    }

    /**
     * Will always be printed
     * @param type type of the log to print
     * @param log String message for the log
     */
    private static void doLogging(int type, String log) {
        switch (type) {
            case Log.DEBUG:
                Log.d(TAG, log);
                break;
            case Log.ERROR:
                Log.e(TAG, log);
                break;
            case Log.INFO:
                Log.i(TAG, log);
                break;
            case Log.VERBOSE:
                Log.v(TAG, log);
                break;
            case Log.WARN:
                Log.w(TAG, log);
                break;
        }
    }
}
