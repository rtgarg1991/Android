package com.cashon.cashon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cashon.helper.Constants;
import com.cashon.helper.Logger;
import com.cashon.helper.PreferenceManager;
import com.cashon.helper.model.Referrals;
import com.cashon.impl.SimpleAsyncTask;
import com.cashon.impl.Utility;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.HashMap;
import java.util.Random;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {

    static final String SERVER_URL = "http://192.168.42.33:80/gcm_server_php/register.php";
    static final String BROADCAST_RECEIVER_ACTION = "com.cashon.cashon.receiver";

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SimpleAsyncTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mMobileView;
    private EditText mPasswordView;
    private EditText mNameView;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner mCountrySpinner;

    int serviceResponse = -1;
    private EditText mReferralView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.textEmail);
        mNameView = (EditText) findViewById(R.id.textName);

        mMobileView = (EditText) findViewById(R.id.textMobileNumber);
        mMobileView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Logger.doSecureLogging(Log.DEBUG, "User Trying to Register, lets check progress!");
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        mPasswordView = (EditText) findViewById(R.id.textPassword);
        mCountrySpinner = (Spinner)findViewById(R.id.spinnerCountryCode);
        mReferralView = (EditText) findViewById(R.id.textReferral);

        Button registerButton = (Button) findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        // if async task already running , then return
        if (mAuthTask != null) {
            Logger.doSecureLogging(Log.DEBUG, "User Already in registration process with Server, so lets decline this request!");
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mMobileView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        email = email.trim();
        String mobile = mMobileView.getText().toString();
        mobile = mobile.trim();
        String name = mNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String referral = mReferralView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for valid Mobile Number
        if (!Utility.isValidMobile(mobile)) {
            Logger.doSecureLogging(Log.DEBUG, "Wrong Mobile Entered by User, set error in EditText");
            mMobileView.setError(getString(R.string.error_invalid_mobile));
            focusView = mMobileView;
            cancel = true;
        }

        // Check for a valid email, if the user entered one.
        if (!Utility.isValidEmail(email)) {
            Logger.doSecureLogging(Log.DEBUG, "Wrong Email Entered by User, set error in EditText");
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid Name
        if (TextUtils.isEmpty(name)) {
            Logger.doSecureLogging(Log.DEBUG, "Wrong Name Entered by User, set error in EditText");
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        // check if password is acceptable
        if (!TextUtils.isEmpty(password) && !Utility.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(referral)) {
            referral = null;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Logger.doSecureLogging(Log.INFO, "Lets try to register user on Server!");
            showProgress(true);
            // Show a progress spinner, and try to register the user in Parse
            tryRegister(email, mobile, name, password, referral);
        }
    }

    private void tryRegister(String email, String mobile, String name, String password, final String referral) {
        String deviceId = generateDeviceUniqueId();
        String countryCode = mCountrySpinner.getSelectedItem().toString();
        // e.g of country code is +91 (IN), so lets just pull out 91 from it
        countryCode = countryCode.substring(1, 3);

        ParseUser user = new ParseUser();
        user.setUsername(email);
        user.setPassword(password);
        user.setEmail(email);

        // other fields related to this user
        user.put("mobile", mobile);
        user.put("country_code", countryCode);
        user.put("device_id", deviceId);
        user.put("mobile_verified", false);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! User is registered now, lets show him/her some offers.

                    showProgress(false);
                    // get default shared preferences
                    SharedPreferences prefs = PreferenceManager.getSharedPreferences(RegisterActivity.this, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Constants.PREF_USER_REGISTERED, true);
                    editor.commit();

                    // add referral for current signup
                    if(referral != null) {
                        Referrals.addReferral(ParseUser.getCurrentUser().getUsername(), referral);
                    }

                    // save current installation
                    try {
                        ParseInstallation.getCurrentInstallation().save();
                    } catch (ParseException e1) {
                        ParseInstallation.getCurrentInstallation().saveInBackground();
                        e1.printStackTrace();
                    }


                    // Open Main Activity notifying the user is registered
                    // and lets show him/her some awesome offers to earn some money
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    showProgress(false);
                    if(e.getCode() == ParseException.USERNAME_TAKEN) {
                        mEmailView.setError(e.getMessage());
                    } else if(e.getCode() == ParseException.TIMEOUT) {
                        mEmailView.setError(e.getMessage());
                    } else {
                        // TODO need to check for all type of errors
                        mEmailView.setError(e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        mLoginFormView.setEnabled(!show);
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private String generateDeviceUniqueId() {
        // GET DEVICE ID
        // as per Android blog
        // this unique id has bug in major manufacturer, so lets leave it for now
        final String deviceId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // serial number of the device
        // it is helpful where telephony services are not available
        final String deviceSerialNumber = Build.SERIAL;

        // GET IMEI NUMBER
        TelephonyManager tManager = (TelephonyManager) getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceIMEI = tManager.getDeviceId();
        // lets just return device imei id for now
        // for wifi devices, it will be garbage entry
        // for devices without telephony this will be garbage
        return deviceIMEI;
    }
}