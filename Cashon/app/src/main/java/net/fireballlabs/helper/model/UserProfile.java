package net.fireballlabs.helper.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Spinner;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Rohit on 8/17/2015.
 */
public class UserProfile {
    public static final String PARSE_TABLE_NAME_USER_PROFILE = "UserProfile";
    public static final String PARSE_TABLE_COLUMN_USER_ID = "userId";
    public static final String PARSE_TABLE_COLUMN_NAME = "name";
    public static final String PARSE_TABLE_COLUMN_SEX = "sex";
    public static final String PARSE_TABLE_COLUMN_DOB = "dob";

    public static final String PARSE_TABLE_COLUMN_OPERATOR = "operator";
    public static final String PARSE_TABLE_COLUMN_CIRCLE = "circle";
    public static final String PARSE_TABLE_COLUMN_PHONE_TYPE = "phoneType";

    public static final String PARSE_TABLE_COLUMN_CITY = "city";
    public static final String PARSE_TABLE_COLUMN_STATE = "state";

    public static final String PARSE_TABLE_COLUMN_ANDROID_API = "androidApi";
    public static final String PARSE_TABLE_COLUMN_SCREEN_SIZE_X = "screenSizeX";
    public static final String PARSE_TABLE_COLUMN_SCREEN_SIZE_Y = "screenSizeY";

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

    private String name;
    private String dob;
    private int sex; // 1 is for Male, 2 for Female
    private String phone;
    private String operator;
    private String circle;
    private int phoneType; // 1 is for prepaid, 2 for postpaid
    private String city;
    private String State;
    private int sdk;
    private int width;
    private int height;

    public static UserProfile profile;

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(int phoneType) {
        this.phoneType = phoneType;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSdk() {
        return sdk;
    }

    public void setSdk(int sdk) {
        this.sdk = sdk;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static void loadSavedProfileData(Context context) {
        if(profile == null) {
            profile = new UserProfile();
        }
        profile.setName(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_NAME, Context.MODE_PRIVATE, ""));
        profile.setDob(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_DOB, Context.MODE_PRIVATE, ""));
        profile.setSex(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_SEX, Context.MODE_PRIVATE, -1));
        profile.setOperator(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_OPERATOR, Context.MODE_PRIVATE, ""));
        profile.setCircle(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_CIRCLE, Context.MODE_PRIVATE, ""));
        profile.setPhoneType(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_PHONE_TYPE, Context.MODE_PRIVATE, -1));
        profile.setCity(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_CITY, Context.MODE_PRIVATE, ""));
        profile.setState(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_STATE, Context.MODE_PRIVATE, ""));
        profile.setSdk(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_ANDROID_API, Context.MODE_PRIVATE, -1));
        profile.setWidth(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_SCREEN_SIZE_X, Context.MODE_PRIVATE, -1));
        profile.setHeight(PreferenceManager.getDefaultSharedPreferenceValue(context, PARSE_TABLE_COLUMN_SCREEN_SIZE_Y, Context.MODE_PRIVATE, -1));
    }

    public static void updateProfile(String name, String sex, String dob, int sdkInt,
                                     int width, int height, String operator, String circle,
                                     String phoneType, String city, String state, Context context) throws ParseException, java.text.ParseException {
        loadSavedProfileData(context);

        if(profile == null) {
            return;
        }

        SharedPreferences.Editor editor = PreferenceManager.getSharedPreferences(context, Context.MODE_PRIVATE).edit();

        HashMap<String, Object> params = new HashMap<String, Object>();
        if(!name.equals(profile.getName())) {
            params.put(PARSE_TABLE_COLUMN_NAME, name);
            editor.putString(PARSE_TABLE_COLUMN_NAME, name);
        }
        int s = "Male".equals(sex) ? 1 : 2;
        if(!"Select".equals(sex) && !(s == profile.getSex())) {
            params.put(PARSE_TABLE_COLUMN_SEX, s);
            editor.putInt(PARSE_TABLE_COLUMN_SEX, s);
        }
        if(!dob.equals(profile.getDob())) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Date date = format.parse(dob);
            params.put(PARSE_TABLE_COLUMN_DOB, date);
            editor.putString(PARSE_TABLE_COLUMN_DOB, dob);
        }

        if(!operator.equals(profile.getOperator())) {
            params.put(PARSE_TABLE_COLUMN_OPERATOR, operator);
            editor.putString(PARSE_TABLE_COLUMN_OPERATOR, operator);
        }
        if(!circle.equals(profile.getCircle())) {
            params.put(PARSE_TABLE_COLUMN_CIRCLE, circle);
            editor.putString(PARSE_TABLE_COLUMN_CIRCLE, circle);
        }
        int t = "Prepaid".equals(phoneType) ? 1 : 2;
        if(!"Select".equals(phoneType) && !(t == profile.getPhoneType())) {
            params.put(PARSE_TABLE_COLUMN_PHONE_TYPE, t);
            editor.putInt(PARSE_TABLE_COLUMN_PHONE_TYPE, t);
        }

        if(!city.equals(profile.getCity())) {
            params.put(PARSE_TABLE_COLUMN_CITY, city);
            editor.putString(PARSE_TABLE_COLUMN_CITY, city);
        }
        if(!"Select".equals(state) && !state.equals(profile.getState())) {
            params.put(PARSE_TABLE_COLUMN_STATE, state);
            editor.putString(PARSE_TABLE_COLUMN_STATE, state);
        }

        if(sdkInt != profile.getSdk()) {
            params.put(PARSE_TABLE_COLUMN_ANDROID_API, sdkInt);
            editor.putInt(PARSE_TABLE_COLUMN_ANDROID_API, sdkInt);
        }
        if(width != profile.getWidth()) {
            params.put(PARSE_TABLE_COLUMN_SCREEN_SIZE_X, width);
            editor.putInt(PARSE_TABLE_COLUMN_SCREEN_SIZE_X, width);
        }
        if(height != profile.getHeight()) {
            params.put(PARSE_TABLE_COLUMN_SCREEN_SIZE_Y, height);
            editor.putInt(PARSE_TABLE_COLUMN_SCREEN_SIZE_Y, height);
        }

        if(params.size() > 0) {
            params.put(PARSE_TABLE_COLUMN_USER_ID, ParseUser.getCurrentUser().getObjectId());
            ParseCloud.callFunction(ParseConstants.FUNCTION_CREATE_UPDATE_PROFILE, params);
            editor.commit();
        }
    }

    public static void loadProfileData(EditText nameEditText, Spinner sexSpinner, EditText dobEditText,
                                       EditText phoneEditText, Spinner phoneTypeSpinner,
                                       Spinner operatorSpinner, Spinner circleSpinner,
                                       EditText cityEditText, Spinner stateSpinner, Context context) {
        loadSavedProfileData(context);

        if(profile == null) {
            return;
        }
        nameEditText.setText(profile.getName() == null ? "" : profile.getName());
        dobEditText.setText(profile.getDob() == null ? "" : profile.getDob());

        if(profile.getSex() > 0) {
            sexSpinner.setSelection(profile.getSex());
        }

        phoneEditText.setText(ParseUser.getCurrentUser().getUsername());
        phoneEditText.setEnabled(false);
        if(profile.getPhoneType() > 0) {
            phoneTypeSpinner.setSelection(profile.getPhoneType());
        }
        if(profile.getOperator() != null) {
            String[] operators = context.getResources().getStringArray(R.array.recharge_mobile_prepaid_company);
            for(int i = 0; i < operators.length; i++) {
                if(profile.getOperator().equals(operators[i])) {
                    operatorSpinner.setSelection(i);
                    break;
                }
            }
        }
        if(profile.getCircle() != null) {
            String[] circles = context.getResources().getStringArray(R.array.recharge_mobile_circle);
            for(int i = 0; i < circles.length; i++) {
                if(profile.getCircle().equals(circles[i])) {
                    circleSpinner.setSelection(i);
                    break;
                }
            }
        }

        cityEditText.setText(profile.getCity() == null ? "" : profile.getCity());
        if(profile.getState() != null) {
            String[] states = context.getResources().getStringArray(R.array.user_profile_state_array);
            for(int i = 0; i < states.length; i++) {
                if(profile.getState().equals(states[i])) {
                    stateSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public static int getGender(Context context) {
        loadSavedProfileData(context);
        return profile.getSex();
    }

    public static void syncProfileData(Context context) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(PARSE_TABLE_COLUMN_USER_ID, ParseUser.getCurrentUser().getObjectId());
        try {
            HashMap<String, Object> cloudProfileData = ParseCloud.callFunction(ParseConstants.FUNCTION_GET_PROFILE_DATE, params);
            if(profile == null) {
                profile = new UserProfile();
            }
            if(cloudProfileData != null && cloudProfileData.size() > 0) {
                profile.setName((String) cloudProfileData.get(PARSE_TABLE_COLUMN_NAME));
                profile.setSex((Integer) cloudProfileData.get(PARSE_TABLE_COLUMN_SEX));

                if(cloudProfileData.get(PARSE_TABLE_COLUMN_DOB) != null) {
                    Date cDate = (Date) cloudProfileData.get(PARSE_TABLE_COLUMN_DOB);
                    String fDate = new SimpleDateFormat("dd/MM/yyyy").format(cDate);
                    profile.setDob(fDate);
                }

                profile.setOperator((String) cloudProfileData.get(PARSE_TABLE_COLUMN_OPERATOR));
                profile.setCircle((String) cloudProfileData.get(PARSE_TABLE_COLUMN_CIRCLE));
                profile.setPhoneType((Integer) cloudProfileData.get(PARSE_TABLE_COLUMN_PHONE_TYPE));
                profile.setCity((String) cloudProfileData.get(PARSE_TABLE_COLUMN_CITY));
                profile.setState((String) cloudProfileData.get(PARSE_TABLE_COLUMN_STATE));
                profile.setSdk((Integer) cloudProfileData.get(PARSE_TABLE_COLUMN_ANDROID_API));
                profile.setWidth((Integer) cloudProfileData.get(PARSE_TABLE_COLUMN_SCREEN_SIZE_X));
                profile.setHeight((Integer) cloudProfileData.get(PARSE_TABLE_COLUMN_SCREEN_SIZE_Y));


                SharedPreferences.Editor editor = PreferenceManager.getSharedPreferences(context, Context.MODE_PRIVATE).edit();
                editor.putString(PARSE_TABLE_COLUMN_NAME, profile.getName());
                editor.putInt(PARSE_TABLE_COLUMN_SEX, profile.getSex());
                editor.putString(PARSE_TABLE_COLUMN_DOB, profile.getDob());
                editor.putString(PARSE_TABLE_COLUMN_OPERATOR, profile.getOperator());
                editor.putString(PARSE_TABLE_COLUMN_CIRCLE, profile.getCircle());
                editor.putInt(PARSE_TABLE_COLUMN_PHONE_TYPE, profile.getPhoneType());
                editor.putString(PARSE_TABLE_COLUMN_CITY, profile.getCity());
                editor.putString(PARSE_TABLE_COLUMN_STATE, profile.getState());
                editor.putInt(PARSE_TABLE_COLUMN_ANDROID_API, profile.getSdk());
                editor.putInt(PARSE_TABLE_COLUMN_SCREEN_SIZE_X, profile.getWidth());
                editor.putInt(PARSE_TABLE_COLUMN_SCREEN_SIZE_Y, profile.getHeight());
                editor.commit();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
