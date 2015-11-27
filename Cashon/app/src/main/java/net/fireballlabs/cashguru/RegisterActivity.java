package net.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import net.fireballlabs.URLShortener;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.Referrals;
import net.fireballlabs.helper.model.UserHelper;
import net.fireballlabs.impl.SimpleAsyncTask;
import net.fireballlabs.impl.Utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
        /*mMobileView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Logger.doSecureLogging(Log.DEBUG, "User Trying to Register, lets check progress!");
                if (id == net.fireballlabs.cashguru.R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });*/
        mCountrySpinner = (Spinner) findViewById(net.fireballlabs.cashguru.R.id.spinnerCountryCode);
//        mReferralView = (EditText) findViewById(net.fireballlabs.cashguru.R.id.textReferral);

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
        if (intent != null && intent.hasExtra(Constants.MOBILE_NUMBER)) {
            String extra = intent.getStringExtra(Constants.MOBILE_NUMBER);
            if (extra != null && !"".equals(extra)) {
                mMobileView.setText(extra);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(net.fireballlabs.cashguru.R.color.primary)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(R.drawable.logo_status_bar);
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
//        String referral = mReferralView.getText().toString();
        String referral = PreferenceManager.getDefaultSharedPreferenceValue(this,
                Constants.PREF_REFERRAL_ID, Context.MODE_PRIVATE, "");

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

        if (TextUtils.isEmpty(referral)) {
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
                    String deviceId = Utility.generateDeviceUniqueId(getApplicationContext());

                    String clickId = PreferenceManager.getDefaultSharedPreferenceValue(RegisterActivity.this,
                            Constants.PREF_CLICK_ID, Context.MODE_PRIVATE, "");
                    // add referral for current signup
                    if (referral != null) {
                        if (Constants.USER_INVITE.equals(referral)) {
                            String referrer = PreferenceManager.getDefaultSharedPreferenceValue(RegisterActivity.this,
                                    Constants.PREF_CLICK_ID, Context.MODE_PRIVATE, "");
                            if (referrer != null) {
                                Referrals.addReferral(usr.getObjectId(), referrer);
                            }
                        } else {
                            if (clickId != null) {
                                String campaign = PreferenceManager.getDefaultSharedPreferenceValue(RegisterActivity.this,
                                        Constants.PREF_CAMPAIGN, Context.MODE_PRIVATE, "");
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                map.put("clickId", clickId);
                                map.put("userId", usr.getObjectId());
                                if (deviceId != null && !"".equals(deviceId)) {
                                    map.put("deviceId", deviceId);
                                }
                                if (referral != null && !"".equals(referral)) {
                                    map.put("referrer", referral);
                                }
                                if (campaign != null && !"".equals(campaign)) {
                                    map.put("campaign", campaign);
                                }
                                ParseCloud.callFunctionInBackground(ParseConstants.FUNCTION_UPDATE_REFERRED_INSTALL, map);
                            }
                        }
                    }
                    HashMap<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("deviceId", deviceId);
                    map2.put("referral", ((referral == null || "".equals(referral)) ? "Empty" : referral));
                    map2.put("clickId", ((clickId == null || "".equals(referral)) ? "Empty" : clickId));

                    AppsFlyerLib.trackEvent(getApplicationContext(), AFInAppEventType.COMPLETE_REGISTRATION, map2);

                    // save current installation
                    try {
                        ParseInstallation.getCurrentInstallation().save();
                    } catch (ParseException e1) {
                        ParseInstallation.getCurrentInstallation().saveInBackground();
                        Crashlytics.logException(e1);
                    }

                    if (usr != null) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (usr.getString(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE) == null) {
                                    try {
                                        usr.fetch();
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                String url = URLShortener.getShortenedUrl(RegisterActivity.this, usr.getString(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE));
                                if (url != null) {
                                    usr.put(UserHelper.PARSE_TABLE_COLUMN_REFER_URL, url);
                                    usr.saveEventually();
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

                    try {
                        // send carrier information
                        sendAdditionalInformation(user.getObjectId());
                    } catch (Exception ex) {
                        Crashlytics.logException(ex);
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
                    if (e.getCode() == ParseException.USERNAME_TAKEN) {
                        mMobileView.setError(mMobileView.getText().toString() + " already registered!");
                    } else if (e.getCode() == ParseException.TIMEOUT) {
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

    private void sendAdditionalInformation(String userId) {
        Context context = RegisterActivity.this;
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("brand", Build.BRAND);
        params.put("device", Build.DEVICE);
        params.put("product", Build.PRODUCT);
        params.put("sdk", Integer.toString(Build.VERSION.SDK_INT));
        params.put("model", Build.MODEL);
        params.put("deviceType", Build.TYPE);

        String installerPackage;
        try {
            installerPackage = context.getPackageManager().getInstallerPackageName(context.getPackageName());
            if (installerPackage != null) {
                params.put("installerPackage", installerPackage);
            }
        } catch (Exception e) {
            ;
        }
        try {
            params.put("lang", Locale.getDefault().getDisplayLanguage());
        } catch (Exception e) {
            ;
        }
        try {
            params.put("langCode", Locale.getDefault().getLanguage());
        } catch (Exception e) {
            ;
        }

        try {
            params.put("country", Locale.getDefault().getCountry());
        } catch (Exception e) {
            ;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            params.put("operator", telephonyManager.getSimOperatorName());
            params.put("carrier", telephonyManager.getNetworkOperatorName());
        } catch (Exception e) {
            ;
        }

        try {
            params.put("network", getNetwork(context));
        } catch (Throwable e) {
            ;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hhmmZ");
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
            params.put("installDate", dateFormat.format(new Date(firstInstallTime)));
        } catch (Exception e) {
            ;
        }
        getUserLocation(params);
        ParseCloud.callFunctionInBackground(ParseConstants.FUNCTION_ADD_ADDITIONAL_INFORMATION, params);

    }

    private void getUserLocation(HashMap<String, Object> params) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location location = locationManager.getLastKnownLocation(provider);


        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            StringBuilder builder = new StringBuilder();
            List<Address> address = null;
            try {
                address = geoCoder.getFromLocation(lat, lng, 1);
                if(address.size() > 0) {
                    Address currentAddress = address.get(0);
                    String adminArea = currentAddress.getAdminArea();
                    String subAdminArea = currentAddress.getSubAdminArea();
                    String locality = currentAddress.getLocality();
                    String subLocality = currentAddress.getSubLocality();
                    String featureName = currentAddress.getFeatureName();

                    params.put("adminArea", adminArea == null ? "" : adminArea);
                    params.put("subAdminArea", subAdminArea == null ? "" : subAdminArea);
                    params.put("locality", locality == null ? "" : locality);
                    params.put("subLocality", subLocality == null ? "" : subLocality);
                    params.put("featureName", featureName == null ? "" : featureName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            return;
        }
    }

    private static String getNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(1);
        if(wifi.isConnectedOrConnecting()) {
            return "WIFI";
        } else {
            NetworkInfo mobile = connectivityManager.getNetworkInfo(0);
            return mobile != null && mobile.isConnectedOrConnecting()?"MOBILE":"unknown";
        }
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