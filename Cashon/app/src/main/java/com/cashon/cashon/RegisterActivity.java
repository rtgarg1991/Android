package com.cashon.cashon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.cashon.impl.SimpleAsyncTask;
import com.cashon.impl.Utility;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity implements SimpleAsyncTask.SimpleAsyncTaskCallbacks {

    static final String SERVER_URL = "http://192.168.42.33:80/gcm_server_php/register.php";

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
    private EditText mNameView;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner mCountrySpinner;

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
        mCountrySpinner = (Spinner)findViewById(R.id.spinnerCountryCode);

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
        String mobile = mMobileView.getText().toString();
        String name = mNameView.getText().toString();

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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Logger.doSecureLogging(Log.INFO, "Lets try to register user on Server!");
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new SimpleAsyncTask(this);
            mAuthTask.execute((Void)null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onPreExecute() {
        // No Need to handle anything in this for current scenario
    }

    @Override
    public Boolean doInBackground(Object... params) {

        // lets synchronize this registration request, so that only one registration is sent to server at a time
        synchronized (RegisterActivity.this) {
            // TODO Need to decide whether to check for the internet availability

            try {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                Logger.doSecureLogging(Log.DEBUG, "Token received : " + token);

                // now we have unique token of User's device, lets gather some more information and register on the server
                String gcm_id = token;
                String deviceId = generateDeviceUniqueId();
                String email = mEmailView.getText().toString();
                String mobile = mMobileView.getText().toString();
                String name = mNameView.getText().toString();
                String countryCode = mCountrySpinner.getSelectedItem().toString();
                // e.g of country code is +91 (IN), so lets just pull out 91 from it
                countryCode = countryCode.substring(1, 3);

                // register user on server
                boolean success = sendRegistrationToServer(gcm_id, deviceId, email, mobile, name, countryCode);
                return success;

            } catch (IOException e) {
                e.printStackTrace();
            }
            // [END get_token]

        }
        // send fail to postExecute
        return false;
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

    private boolean sendRegistrationToServer(String token, String deviceId, String email, String mobile, String name, String countryCode) {
        // lets print a log and then try to register this user on server
        Logger.doSecureLogging(Log.DEBUG, "Registering User : " + token + ", " + deviceId
                + ", " + email + ", " + mobile + ", " + name + ", " + countryCode);

        // server url where we will register the user
        String serverUrl = SERVER_URL;
        // store all values we want to send to server in a map
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", token);
        params.put("name", name);
        params.put("email", email);
        params.put("country", countryCode);
        params.put("mobile", mobile);
        params.put("device", deviceId);

        // calculate temporary backoff time in millis
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Logger.doSecureLogging(Log.DEBUG, "Attempt #" + i + " to register");
            try {
                post(serverUrl, params);
                Logger.doSecureLogging(Log.DEBUG, "registered on server");
                return true;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Logger.doSecureLogging(Log.ERROR, "Failed to register on attempt " + i + ":" + e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Logger.doSecureLogging(Log.DEBUG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Logger.doSecureLogging(Log.WARN, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        return false;
    }

    /**
     * Issue a POST request to the server.
     * @param serverUrl POST address, to register a user.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static void post(String serverUrl, Map<String, String> params)
            throws IOException {

        URL url;
        try {
            // construct url object from string url
            url = new URL(serverUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + serverUrl);
        }
        // not lets iterate on all the parameters we want to send to server and create a post request from it
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        // final post request containing all the parameters
        String body = bodyBuilder.toString();
        Logger.doSecureLogging(Log.DEBUG, "Posting '" + body + "' to " + url);

        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    public void onPostExecute(Object success) {

        mAuthTask = null;
        showProgress(false);

        if ((Boolean)success) {
            // update shared preference notifying that the user is registered

            // get default shared preferences
            SharedPreferences prefs = PreferenceManager.getSharedPreferences(RegisterActivity.this, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.USER_REGISTERED, true);
            editor.commit();

            // Open Main Activity notifying the user is registered
            // and lets show him/her some awesome offers to earn some money
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // TODO check for the response sent by server, why registration failed and proceed accordingly
            // Temporarily just set error on mobile view
            mMobileView.setError(getString(R.string.error_invalid_mobile));
            mMobileView.requestFocus();
        }
    }

    @Override
    public void onProgressUpdate(Object... values) {

    }

    @Override
    public void onCancelled(Object o) {

    }

    @Override
    public void onCancelled() {

        mAuthTask = null;
        showProgress(false);
    }
}

