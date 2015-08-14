package net.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.impl.Utility;
import com.parse.LogInCallback;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.ParseException;

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
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(net.fireballlabs.cashguru.R.layout.activity_login);

        // Set up the login form.
        mEmailView = (EditText) findViewById(net.fireballlabs.cashguru.R.id.email);

        mPasswordView = (EditText) findViewById(net.fireballlabs.cashguru.R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == net.fireballlabs.cashguru.R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(net.fireballlabs.cashguru.R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(net.fireballlabs.cashguru.R.id.email_register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

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
                    actionBar.setLogo(net.fireballlabs.cashguru.R.drawable.logo_no_shadow);
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
        if(mEmailView.hasFocus()) {
            imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
        } else if(mPasswordView.hasFocus()) {
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
        }
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !Utility.isPasswordValid(password)) {
            mPasswordView.setError(getString(net.fireballlabs.cashguru.R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(net.fireballlabs.cashguru.R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Utility.isValidEmail(email)) {
            mEmailView.setError(getString(net.fireballlabs.cashguru.R.string.error_invalid_email));
            focusView = mEmailView;
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
            ParseUser.logInInBackground(email, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        showProgress(false);

                        // save current installation
                        try {
                            ParseInstallation.getCurrentInstallation().save();
                        } catch (ParseException e1) {
                            ParseInstallation.getCurrentInstallation().saveInBackground();
                            e1.printStackTrace();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }

                        // lets show some offers to the user
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(Constants.IS_NEW_LOGIN, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        showProgress(false);
                        if(e.getCode() == ParseException.EMAIL_MISSING || e.getCode() == ParseException.EMAIL_NOT_FOUND || e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
                            mEmailView.setError(e.getMessage());
                            mEmailView.requestFocus();
                        } else if(e.getCode() == ParseException.VALIDATION_ERROR || e.getCode() == ParseException.OBJECT_NOT_FOUND){
                            mPasswordView.setError(e.getMessage());
                            mPasswordView.requestFocus();
                        } else {
                            // TODO Need to check for other exceptions
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
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.progress_bar_holo);
            rotation.setRepeatCount(Animation.INFINITE);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.startAnimation(rotation);
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
            mProgressView.clearAnimation();
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }*/
    }
}

