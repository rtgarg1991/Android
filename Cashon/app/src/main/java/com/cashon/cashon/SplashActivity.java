package com.cashon.cashon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cashon.helper.Constants;
import com.cashon.impl.SimpleDelayHandler;
import com.cashon.helper.Logger;
import com.cashon.impl.Utility;

public class SplashActivity extends Activity implements SimpleDelayHandler.SimpleDelayHandlerCallback {

    private boolean isInternetConnected = false;
    private boolean isUserDeviceRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.splashProgressBar);

        SimpleDelayHandler simpleDelayHandler = SimpleDelayHandler.getInstance();
        simpleDelayHandler.startDelayed(this, Constants.SPLASH_SCREEN_TIMEOUT, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utility.isInternetConnected(this)) {
            isInternetConnected = true;
        }

        if(Utility.isUserRegistered(this)) {
            isUserDeviceRegistered = true;
        }

    }

    @Override
    public void handleDelayedHandlerCallback() {
        Logger.doSecureLogging(Log.DEBUG, "Checking Status of User Account.");
        if(!isInternetConnected) {
            Logger.doSecureLogging(Log.DEBUG, "Internet not Connected, lets show a dialog to connect to Internet");
            //TODO remove Toast
            Toast.makeText(getApplicationContext(), "Internet not connected", Toast.LENGTH_SHORT).show();
            // TODO show user a dialog to connect to internet
        } else if(!isUserDeviceRegistered) {
            Logger.doSecureLogging(Log.DEBUG, "User not registered, lets show Register Activity!");
            //TODO remove Toast
            Toast.makeText(getApplicationContext(), "User not registered", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // TODO user is registered and internet is working
            Logger.doSecureLogging(Log.INFO, "User is registered and Internet is also working, so lets show some offers!");
        }
    }

}
