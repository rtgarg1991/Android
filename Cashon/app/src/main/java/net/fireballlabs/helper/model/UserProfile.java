package net.fireballlabs.helper.model;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by Rohit on 8/17/2015.
 */
public class UserProfile {
    public static final String PARSE_TABLE_NAME_USER_PROFILE = "UserProfile";
    public static final String PARSE_TABLE_COLUMN_SEX = "sex";
    public static final String PARSE_TABLE_COLUMN_DOB = "dob";
    public static final String PARSE_TABLE_COLUMN_ANDROID_API = "androidApi";
    public static final String PARSE_TABLE_COLUMN_SCREEN_SIZE_X = "screenSizeX";
    public static final String PARSE_TABLE_COLUMN_SCREEN_SIZE_Y = "screenSizeY";
    public static final String PARSE_TABLE_COLUMN_USER_ID = "userId";
    public static final String PARSE_TABLE_COLUMN_NAME = "name";

    public static boolean needProfileUpdation() {
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return true;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getObjectId());
        try {
            return ParseCloud.callFunction("CheckUserProfileNeedsUpdate", params);
        } catch (ParseException e) {
            Crashlytics.logException(e);
            return true;
        }
    }
}
