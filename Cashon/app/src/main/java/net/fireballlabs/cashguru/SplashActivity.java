package net.fireballlabs.cashguru;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.Referrals;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.SimpleDelayHandler;
import net.fireballlabs.impl.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class SplashActivity extends Activity implements SimpleDelayHandler.SimpleDelayHandlerCallback, HardwareAccess.HardwareAccessCallbacks {

    private static final int MAX_RETRIES_ENABLE_INTERNET = 2;
    private int mInternetConnectivityTried = 0;
    private boolean mEnableInternetCallback = false;

    private boolean isInternetConnected = false;
    private boolean isUserDeviceRegistered = false;
    private boolean mStopped;
    private boolean mDataSynced = true;

    private boolean mPermissionsGranted = false;
    private View mLayout;


    private String[] permissions = {"android.permission.GET_ACCOUNTS", "android.permission.READ_PROFILE",
            "android.permission.READ_CONTACTS", "android.permission.INTERNET",
            "android.permission.READ_PHONE_STATE", "com.google.android.c2dm.permission.RECEIVE",
            "android.permission.RECEIVE_SMS", "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.WAKE_LOCK", "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.VIBRATE", "com.android.launcher.permission.INSTALL_SHORTCUT",
            "android.permission.ACCESS_COARSE_LOCATION"
    };

    private int[] permissionRationale = {R.string.permission_get_account_rationale, R.string.permission_read_profile_rationale,
            R.string.permission_read_contacts_rationale, R.string.permission_internet_rationale,
            R.string.permission_read_phone_state_rationale, R.string.permission_c2dm_rationale,
            R.string.permission_receive_sms_rationale, R.string.permission_access_network_state_rationale,
            R.string.permission_wake_lock_rationale, R.string.permission_boot_completed_rationale,
            R.string.permission_vibrate_rationale, R.string.permission_install_shortcut_rationale,
            R.string.permission_access_coarse_location
    };
    private String mOfferId;
    private int mFragmentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppsFlyerLib.setAppsFlyerKey("g4pnvQJmRGE9k3ic9RVSHa");
        AppsFlyerLib.sendTracking(getApplicationContext());

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            ActionBar actionBar = getActionBar();
            if(actionBar != null) {
                actionBar.hide();
            }
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
        mLayout = findViewById(R.id.activity_splash_main_layout);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissionStatus();
        } else {
            mPermissionsGranted = true;
        }

        ProgressBar progressBar = (ProgressBar)findViewById(net.fireballlabs.cashguru.R.id.splashProgressBar);

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.getStringExtra("com.parse.Data") != null) {

                JSONObject obj = null;
                try {
                    obj = new JSONObject(intent.getStringExtra("com.parse.Data"));
                } catch (JSONException ex) {
                    Crashlytics.logException(ex);
                }
                if (obj != null) {
                    if (obj.has("deal_link")) {

                        Intent intent2 = null;
                        try {
                            intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(obj.getString("deal_link")));
                            startActivity(intent2);
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                        }
                    } else if(obj.has("offer_id")) {
                        try {
                            mOfferId = obj.getString("offer_id");
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                        }
                    } else if(obj.has("fragment_id")) {
                        try {
                            mFragmentId = obj.getInt("fragment_id");
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                        }
                    }
                }
            }
        }

        /*Intent i = new Intent("com.android.vending.INSTALL_REFERRER");
        i.setPackage("net.fireballlabs.cashguru"); //referrer is a composition of the parameter of the campaign
        i.putExtra("referrer", "af_tranid=C6G39N5ENDS9R9W&c=ZZZYX&pid=User_invite");
        sendBroadcast(i);*/
    }

    private void checkPermissionStatus() {
        for(int i = 0; i < permissions.length; i++) {
            if (ActivityCompat.checkSelfPermission(this, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(permissions[i], permissionRationale[i], i);
                mPermissionsGranted = false;
                return;
            }
        }
        mPermissionsGranted = true;
    }

    private void requestPermission(final String permission, int stringId, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mLayout, stringId, Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK!", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(SplashActivity.this,
                                    new String[]{permission},
                                    requestCode);
                        }
                    })
                    .show();
        } else {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode < permissions.length) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission has been granted, preview can be displayed
                checkPermissionStatus();
                if(mPermissionsGranted) {
                    initialize();
                }
            } else {
                requestPermission(permissions[requestCode], permissionRationale[requestCode], requestCode);

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

        if(mPermissionsGranted) {
            initialize();
        }


    }

    private void initialize() {

        // lets create a timeout request
        SimpleDelayHandler simpleDelayHandler = SimpleDelayHandler.getInstance(this);
        simpleDelayHandler.startDelayed(this, Constants.SPLASH_SCREEN_TIMEOUT, true);

        if (Utility.isInternetConnected(this)) {
            isInternetConnected = true;
        }

        // get all offers from Parse and sync data to local db
        boolean newInstallation = PreferenceManager.getDefaultSharedPreferenceValue(this, Constants.PREF_FIRST_TIME, MODE_PRIVATE, true);

        if(newInstallation) {
            String referral = PreferenceManager.getDefaultSharedPreferenceValue(this,
                    Constants.PREF_REFERRAL_ID, Context.MODE_PRIVATE, "");
            if(referral != null && !"".equals(referral) && !Constants.USER_INVITE.equals(referral)) {
                String clickId = PreferenceManager.getDefaultSharedPreferenceValue(this,
                        Constants.PREF_CLICK_ID, Context.MODE_PRIVATE, "");
                String campaign = PreferenceManager.getDefaultSharedPreferenceValue(this,
                        Constants.PREF_CAMPAIGN, Context.MODE_PRIVATE, "");
                String deviceId = Utility.generateDeviceUniqueId(this);
                HashMap<String, Object> map = new HashMap<>();

                if(deviceId != null && !"".equals(deviceId)) {
                    map.put("deviceId", deviceId);
                }
                if(referral != null && !"".equals(referral)) {
                    map.put("referrer", referral);
                }
                if(campaign != null && !"".equals(campaign)) {
                    map.put("campaign", campaign);
                }
                if(clickId != null && !"".equals(clickId)) {
                    map.put("clickId", clickId);
                }

                ParseCloud.callFunctionInBackground(ParseConstants.FUNCTION_ADD_NEW_REFERRED_INSTALL, map);
            }
            PreferenceManager.setDefaultSharedPreferenceValue(SplashActivity.this, Constants.PREF_FIRST_TIME, MODE_PRIVATE, false);
        }

        if(newInstallation && isInternetConnected) {
            mDataSynced = false;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Offer> offers = Offer.getAllOffers(SplashActivity.this);

                        Offer.clearCurrentDataFromDB(SplashActivity.this);
                        if(offers != null) {
                            for (Offer offer : offers) {
                                offer.saveData(SplashActivity.this);
                            }
                        }
                        mDataSynced = true;
                    } catch (ParseException e) {
                        Crashlytics.logException(e);
                        mDataSynced = true;
                    }
                }
            });
            thread.start();
        } else {
            mDataSynced = true;
        }

        ParseUser user = ParseUser.getCurrentUser();

        if(user != null && user.isAuthenticated()) {
//            user.logOut();
            isUserDeviceRegistered = true;
        }
        // TODO Temporary skip
//        isInternetConnected = true;
//        isUserDeviceRegistered = true;

        mStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStopped = true;
    }

    @Override
    public void handleDelayedHandlerCallback() {
        if(mStopped) {
            return;
        }
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
        } else if(!mDataSynced) {
            Logger.doSecureLogging(Log.DEBUG, "Parse data not synced, lets wait more so that data might sync by the time next timeout happen!");
            SimpleDelayHandler simpleDelayHandler = SimpleDelayHandler.getInstance(this);
            simpleDelayHandler.startDelayed(this, Constants.TEMP_TIMEOUT, true);
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
            if(mOfferId != null && !"".equals(mOfferId)) {
                intent.putExtra(Constants.ACTIVITY_LAUNCH_PARAM_OFFER_ID, mOfferId);
            }
            if(mFragmentId != -1) {
                intent.putExtra(Constants.ACTIVITY_LAUNCH_PARAM_FRAGMENT_ID, mFragmentId);
                if(mFragmentId == Constants.ID_APP_REFER) {
                    Referrals.syncReferralBonus(getApplicationContext());
                }
            }
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
