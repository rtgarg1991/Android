package net.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.impl.Utility;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    // UI references.
    private EditText mMobileNumberEditText;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.fireballlabs.cashguru.R.layout.activity_login);

        // Set up the login form.
        mMobileNumberEditText = (EditText) findViewById(R.id.login_mobile_number);

        /*mMobileNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == net.fireballlabs.cashguru.R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });*/

        Button mEmailSignInButton = (Button) findViewById(net.fireballlabs.cashguru.R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

//        Button mRegisterButton = (Button) findViewById(net.fireballlabs.cashguru.R.id.email_register_button);
        /*mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra(Constants.MOBILE_NUMBER, mMobileNumberEditText.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });*/

        mLoginFormView = findViewById(net.fireballlabs.cashguru.R.id.login_form);
        mProgressView = findViewById(net.fireballlabs.cashguru.R.id.login_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(net.fireballlabs.cashguru.R.color.primary)));
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(R.drawable.logo_status_bar);
                }
                actionBar.setDisplayUseLogoEnabled(true);
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if(mMobileNumberEditText.hasFocus()) {
            imm.hideSoftInputFromWindow(mMobileNumberEditText.getWindowToken(), 0);
        }
        // Reset errors.
        mMobileNumberEditText.setError(null);

        // Store values at the time of the login attempt.
        String mobile = mMobileNumberEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(mobile) && !Utility.isValidMobile(mobile)) {
            mMobileNumberEditText.setError(getString(net.fireballlabs.cashguru.R.string.error_invalid_mobile));
            focusView = mMobileNumberEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            ParseUser.logInInBackground(mobile, mobile, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {

                        // save current installation
                        try {
                            ParseInstallation.getCurrentInstallation().save();
                        } catch (ParseException e1) {
                            ParseInstallation.getCurrentInstallation().saveInBackground();
                            Crashlytics.logException(e1);
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            Crashlytics.logException(e1);
                        }

                        // lets show some offers to the user
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(Constants.IS_NEW_LOGIN, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        showProgress(false);
                    } else {
                        if(e != null) {
                            Crashlytics.logException(e);
                            if (e.getCode() == ParseException.VALIDATION_ERROR || e.getCode() == ParseException.OBJECT_NOT_FOUND) {
//                                mMobileNumberEditText.setError(e.getMessage());
//                                Crashlytics.logException(e);
                                mMobileNumberEditText.requestFocus();
                            } else {
                                // TODO Need to check for other exceptions
//                                mMobileNumberEditText.setError(e.getMessage());
//                                Crashlytics.logException(e);
                            }
                        }
                        showProgress(false);
                        if(e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            Toast.makeText(getApplicationContext(), "Redirecting to Registration Page.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra(Constants.MOBILE_NUMBER, mMobileNumberEditText.getText().toString());
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                }
            });
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
    }
}

