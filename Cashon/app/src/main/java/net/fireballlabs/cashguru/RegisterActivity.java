package net.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.fireballlabs.URLShortener;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.Referrals;
import net.fireballlabs.helper.model.UserHelper;
import net.fireballlabs.impl.SimpleAsyncTask;
import net.fireballlabs.impl.Utility;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {

    static final String SERVER_URL = "http://192.168.42.33:80/gcm_server_php/register.php";
    static final String BROADCAST_RECEIVER_ACTION = "net.fireballlabs.cashguru.receiver";

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
    private View mProgressView;
    private View mLoginFormView;
    private Spinner mCountrySpinner;

    int serviceResponse = -1;
    private EditText mReferralView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.fireballlabs.cashguru.R.layout.activity_register);

        // Set up the login form.
        mEmailView = (EditText) findViewById(net.fireballlabs.cashguru.R.id.textEmail);

        mMobileView = (EditText) findViewById(net.fireballlabs.cashguru.R.id.textMobileNumber);
        mMobileView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Logger.doSecureLogging(Log.DEBUG, "User Trying to Register, lets check progress!");
                if (id == net.fireballlabs.cashguru.R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        mCountrySpinner = (Spinner)findViewById(net.fireballlabs.cashguru.R.id.spinnerCountryCode);
        mReferralView = (EditText) findViewById(net.fireballlabs.cashguru.R.id.textReferral);

        Button registerButton = (Button) findViewById(net.fireballlabs.cashguru.R.id.buttonRegister);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(net.fireballlabs.cashguru.R.id.login_form);
        mProgressView = findViewById(net.fireballlabs.cashguru.R.id.login_progress);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                net.fireballlabs.cashguru.R.array.country_codes, net.fireballlabs.cashguru.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(net.fireballlabs.cashguru.R.layout.simple_spinner_dropdown_item);
        mCountrySpinner.setAdapter(adapter);

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(Constants.MOBILE_NUMBER)) {
            String extra = intent.getStringExtra(Constants.MOBILE_NUMBER);
            if(extra != null && !"".equals(extra)) {
                mMobileView.setText(extra);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(net.fireballlabs.cashguru.R.color.primary)));
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(net.fireballlabs.cashguru.R.drawable.logo_no_shadow);
                }
                actionBar.setDisplayUseLogoEnabled(true);
            }
        }
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
//        mNameView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        email = email.trim();
        String mobile = mMobileView.getText().toString();
        mobile = mobile.trim();
//        String name = mNameView.getText().toString();
        String referral = mReferralView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for valid Mobile Number
        if (!Utility.isValidMobile(mobile)) {
            Logger.doSecureLogging(Log.DEBUG, "Wrong Mobile Entered by User, set error in EditText");
            mMobileView.setError(getString(net.fireballlabs.cashguru.R.string.error_invalid_mobile));
            focusView = mMobileView;
            cancel = true;
        }

        // Check for a valid email, if the user entered one.
        if (!Utility.isValidEmail(email)) {
            Logger.doSecureLogging(Log.DEBUG, "Wrong Email Entered by User, set error in EditText");
            mEmailView.setError(getString(net.fireballlabs.cashguru.R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid Name
        /*if (TextUtils.isEmpty(name)) {
            Logger.doSecureLogging(Log.DEBUG, "Wrong Name Entered by User, set error in EditText");
            mNameView.setError(getString(net.fireballlabs.cashguru.R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }*/

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
            tryRegister(email, mobile, referral);
        }
    }

    private void tryRegister(String email, String mobile, final String referral) {
        String countryCode = mCountrySpinner.getSelectedItem().toString();
        // e.g of country code is +91 (IN), so lets just pull out 91 from it
        countryCode = countryCode.substring(1, 3);

        final ParseUser user = new ParseUser();
        user.setUsername(mobile);
        user.setPassword(mobile);
        user.setEmail(email);

        // other fields related to this user
        user.put(UserHelper.PARSE_TABLE_COLUMN_MOBILE, mobile);
        user.put(UserHelper.PARSE_TABLE_COLUMN_COUNTRY_CODE, countryCode);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! User is registered now, lets show him/her some offers.

                    // get default shared preferences
                    SharedPreferences prefs = PreferenceManager.getSharedPreferences(RegisterActivity.this, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Constants.PREF_USER_REGISTERED, true);
                    editor.commit();

                    final ParseUser usr = ParseUser.getCurrentUser();

                    // add referral for current signup
                    if(referral != null) {
                        Referrals.addReferral(ParseUser.getCurrentUser().getObjectId(), referral);
                    }

                    // save current installation
                    try {
                        ParseInstallation.getCurrentInstallation().save();
                    } catch (ParseException e1) {
                        ParseInstallation.getCurrentInstallation().saveInBackground();
                        Crashlytics.logException(e1);
                    }

                    if(usr != null) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject json = URLShortener.getJSONFromUrl(RegisterActivity.this, usr.getString(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE));
                                if (json != null) {
                                    try {
                                        String url = json.getString("id");
//                                        usr.put(UserHelper.PARSE_TABLE_COLUMN_USER_ID, usr.getObjectId());
                                        if (url != null) {
                                            usr.put(UserHelper.PARSE_TABLE_COLUMN_REFER_URL, url);
                                        }
                                        usr.saveEventually();
                                    } catch (JSONException e) {
                                        Crashlytics.logException(e);
                                    }
                                }

                            }
                        });
                        thread.start();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        Crashlytics.logException(e1);
                    }

                    // Open Main Activity notifying the user is registered
                    // and lets show him/her some awesome offers to earn some money
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra(Constants.IS_NEW_LOGIN, true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    showProgress(false);
                } else {
                    Crashlytics.logException(e);
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    if(e.getCode() == ParseException.USERNAME_TAKEN) {
                        mMobileView.setError(mMobileView.getText().toString() + " already registered!");
                    } else if(e.getCode() == ParseException.TIMEOUT) {
                        mMobileView.setError(e.getMessage());
                    } else {
                        // TODO need to check for all type of errors
                        mMobileView.setError(e.getMessage());
                    }
                    showProgress(false);
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

        if(show) {
            int shortAnimTime = getResources().getInteger(net.fireballlabs.cashguru.R.integer.min_progress_bar_anim_time);
            Animation rotation = AnimationUtils.loadAnimation(this, net.fireballlabs.cashguru.R.anim.progress_bar_holo);
            mProgressView.setVisibility(View.VISIBLE);
            mProgressView.startAnimation(rotation);
            mProgressView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mProgressView.getVisibility() == View.VISIBLE) {
                        mProgressView.clearAnimation();
                        mProgressView.setVisibility(View.GONE);
                    }
                }
            }, shortAnimTime);
        } else {
            mProgressView.clearAnimation();
            mProgressView.setVisibility(View.GONE);
        }
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        }*/
    }
}