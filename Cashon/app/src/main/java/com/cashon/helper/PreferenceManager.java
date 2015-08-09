package com.cashon.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


/**
 * Created by Rohit on 6/8/2015.
 */
public class PreferenceManager {

    /**
     * Get Default Shared Preference for the Application
     * @param context Context to be used to get SharedPreferences
     * @param mode Mode to be used for getting SharedPreferences
     * @return Default App SharedPreferences
     * @throws IllegalArgumentException If context provided is null
     */
    public static SharedPreferences getSharedPreferences(Context context, int mode)
            throws IllegalArgumentException {
        if(context == null) {
            throw new IllegalArgumentException(Constants.EXCEPTION_INVALID_CONTEXT);
        }
        return context.getSharedPreferences(Constants.CASHON_SHARED_PREF, mode);
    }

    /**
     * Get boolean value of particular key in SharedPreferences
     * @param context Context to be used to get SharedPreferences
     * @param key Key whose boolean value needs to be found
     * @param mode Mode to be used for getting SharedPreferences
     * @param defaultValue Default value, if Key not found then this will be returned
     * @return Boolean value for the key in SharedPreferences
     * @throws IllegalArgumentException If context provided is null or key provided is empty or null
     */
    public static boolean getDefaultSharedPreferenceValue(
            Context context, String key, int mode, boolean defaultValue)
            throws IllegalArgumentException{
        if(key == null || TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException(Constants.EXCEPTION_INVALID_KEY);
        }
        if(context == null) {
            throw new IllegalArgumentException(Constants.EXCEPTION_INVALID_CONTEXT);
        }
        return getSharedPreferences(context, mode).getBoolean(key, defaultValue);
    }

    /**
     * Get boolean value of particular key in SharedPreferences
     * @param context Context to be used to get SharedPreferences
     * @param key Key whose boolean value needs to be set
     * @param mode Mode to be used for getting SharedPreferences
     * @param value value which will be set for provided key
     * @throws IllegalArgumentException If context provided is null or key provided is empty or null
     */
    public static void setDefaultSharedPreferenceValue(
            Context context, String key, int mode, boolean value)
            throws IllegalArgumentException{
        if(key == null || TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException(Constants.EXCEPTION_INVALID_KEY);
        }
        if(context == null) {
            throw new IllegalArgumentException(Constants.EXCEPTION_INVALID_CONTEXT);
        }
        SharedPreferences.Editor editor = getSharedPreferences(context, mode).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
