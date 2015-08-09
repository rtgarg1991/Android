package com.cashon.cashon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cashon.helper.Constants;
import com.cashon.impl.HardwareAccess;
import com.cashon.impl.SimpleDelayHandler;
import com.cashon.helper.Logger;
import com.cashon.impl.Utility;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class SplashActivity extends Activity implements SimpleDelayHandler.SimpleDelayHandlerCallback, HardwareAccess.HardwareAccessCallbacks {

    private static final int MAX_RETRIES_ENABLE_INTERNET = 2;
    private int mInternetConnectivityTried = 0;
    private boolean mEnableInternetCallback = false;

    private boolean isInternetConnected = false;
    private boolean isUserDeviceRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        setContentView(R.layout.activity_splash);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.splashProgressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // lets create a timeout request
        SimpleDelayHandler simpleDelayHandler = SimpleDelayHandler.getInstance(this);
        simpleDelayHandler.startDelayed(this, Constants.SPLASH_SCREEN_TIMEOUT, true);

        if(Utility.isInternetConnected(this)) {
            isInternetConnected = true;
        }
        ParseUser user = ParseUser.getCurrentUser();

        if(user != null && user.isAuthenticated()) {
//            user.logOut();
            isUserDeviceRegistered = true;
        }
        // TODO Temporary skip
//        isInternetConnected = true;
//        isUserDeviceRegistered = true;

    }

    @Override
    public void handleDelayedHandlerCallback() {
        Logger.doSecureLogging(Log.DEBUG, "Checking Status of User Account.");
        if(!isInternetConnected) {
            Logger.doSecureLogging(Log.DEBUG, "Internet not Connected, lets show a dialog to connect to Internet");
            if(mInternetConnectivityTried < MAX_RETRIES_ENABLE_INTERNET) {
                // lets create a dialog for user to help him enable the internet
                HardwareAccess.access(this, this, HardwareAccess.ACCESS_INTERNET);
            } else {
                // user is not enabling the internet
                // so lets first check internet availability, if not available then
                // lets create a broadcast listener to listen for internet availability till our app is opened
                // TODO listen for internet availablity broadcasts
                if(Utility.isInternetConnected(this)) {
                    isInternetConnected = true;

                    SimpleDelayHandler simpleDelayHandler = SimpleDelayHandler.getInstance(this);
                    simpleDelayHandler.startDelayed(this, Constants.TEMP_TIMEOUT, true);
                } else {
                    // TODO Open an empty Activity or Empty list for the user
                    // TODO remove Toast
                    Toast.makeText(getApplicationContext(), "Internet Not Enabled by the User", Toast.LENGTH_SHORT).show();
                }
            }
        } else if(!isUserDeviceRegistered) {
            Logger.doSecureLogging(Log.DEBUG, "User not registered, lets show Register Activity!");
            //TODO remove Toast
            Toast.makeText(getApplicationContext(), "User not registered", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {

            Logger.doSecureLogging(Log.INFO, "User is registered and Internet is also working, so lets show some offers!");
            // Open Main Activity notifying the user is registered
            // and lets show him/her some awesome offers to earn some money
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {
        switch (access) {
            case HardwareAccess.ACCESS_INTERNET:
                // True means user is away from our Activity to enable the internet
                // so lets just wait for onStart callback
                mInternetConnectivityTried++;
                if(!isSuccess) {
                    // user is not enabling the internet, atleast not by using our dialog
                    // so lets first check internet availability, if not available then
                    // lets create a broadcast listener to listen for internet availability till our app is opened
                    // TODO listen for internet availablity broadcasts
                    if(Utility.isInternetConnected(this)) {
                        isInternetConnected = true;
                    }

                    SimpleDelayHandler simpleDelayHandler = SimpleDelayHandler.getInstance(this);
                    simpleDelayHandler.startDelayed(this, Constants.TEMP_TIMEOUT, true);
                }
                break;
        }
    }
}
