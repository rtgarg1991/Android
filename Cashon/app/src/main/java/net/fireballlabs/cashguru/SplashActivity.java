package net.fireballlabs.cashguru;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.SimpleDelayHandler;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.impl.Utility;
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

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = getActionBar();
            actionBar.hide();
        }

        boolean newInstallation = PreferenceManager.getDefaultSharedPreferenceValue(this, Constants.PREF_FIRST_TIME, MODE_PRIVATE, true);
        /*if(newInstallation) {
            addShortcut();

            SharedPreferences prefs = PreferenceManager.getSharedPreferences(this, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.PREF_FIRST_TIME, false);
            editor.commit();
        }*/

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        setContentView(net.fireballlabs.cashguru.R.layout.activity_splash);

        ProgressBar progressBar = (ProgressBar)findViewById(net.fireballlabs.cashguru.R.id.splashProgressBar);
    }
    private void addShortcut() {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(), SplashActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra("duplicate", false);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "CashGuru");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.logo));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
                //actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(R.drawable.logo);
                }
                actionBar.setDisplayUseLogoEnabled(true);
            }
        }*/

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
            intent.putExtra(Constants.IS_NEW_LOGIN, false);
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
