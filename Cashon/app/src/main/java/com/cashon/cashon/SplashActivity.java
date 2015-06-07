package com.cashon.cashon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cashon.helper.Constants;
import com.cashon.helper.DelayHandler;
import com.cashon.helper.DelayHandler.DelayHandlerCallback;
import com.cashon.helper.Utility;

public class SplashActivity extends Activity implements DelayHandlerCallback {

    private boolean isInternetConnected = false;
    private boolean isUserDeviceRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.splashProgressBar);

        DelayHandler delayHandler = DelayHandler.getInstance();
        delayHandler.startDelayed(this, Constants.SPLASH_SCREEN_TIMEOUT);
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
        if(!isInternetConnected) {
            Toast.makeText(getApplicationContext(), "Internet not connected", Toast.LENGTH_SHORT).show();
            // TODO show user a dialog to connect to internet
        } else if(!isUserDeviceRegistered) {
            Toast.makeText(getApplicationContext(), "User not registered", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // TODO user is registered and internet is working
        }
    }

}
