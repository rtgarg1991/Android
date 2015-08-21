package net.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.Conversions;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.ui.ContactUsFragment;
import net.fireballlabs.ui.MobileNumberVerificationFragment;
import net.fireballlabs.ui.RechargeFragment;
import net.fireballlabs.ui.ReferFriendsFragment;
import net.fireballlabs.ui.SlidingTabFragment;
import net.fireballlabs.ui.UserProfileFragment;
import net.fireballlabs.view.WalletWidget;

import com.crashlytics.android.Crashlytics;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements MainDrawerAdapter.DrawerAdapterCallbacks, MainActivityCallBacks, HardwareAccess.HardwareAccessCallbacks {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mDrawerList;
    String mTitle = null;
    String mDrawerTitle = null;
    private boolean mOnCreateFinished = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PushService.setDefaultPushCallback(this, MainActivity.class);
        final ParseInstallation installation =
                ParseInstallation.getCurrentInstallation();
        if(installation == null || installation.getObjectId() == null) {
            try {
                ParseInstallation.getCurrentInstallation().save();
            } catch (ParseException e) {
                Crashlytics.logException(e);
                finish();
                super.onCreate(savedInstanceState);
                return;
            }
            if(ParseInstallation.getCurrentInstallation() == null || ParseInstallation.getCurrentInstallation().getObjectId() == null) {
                Crashlytics.logException(new RuntimeException());
                return;
            }
        }
        // show first time popup
        Intent intent = getIntent();
        if(intent != null) {
            boolean isNewLogin = intent.getBooleanExtra(Constants.IS_NEW_LOGIN, false);
            if(isNewLogin) {
                Utility.showFirstTimePopup(this, true);
            }
            if(isNewLogin) {
                String deviceId = Utility.generateDeviceUniqueId(this);
                installation.put(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID, ParseUser.getCurrentUser().getObjectId());
                installation.put(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, deviceId);
                try {
                    installation.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    if(ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()) {
                        ParseUser.getCurrentUser().logOut();
                        finish();
                    }
                }
                ParseUser user = ParseUser.getCurrentUser();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, user.getObjectId());
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, ParseInstallation
                        .getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                ParseCloud.callFunctionInBackground("AddNewInstallVerification", params);
            }
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        super.onCreate(savedInstanceState);
        setContentView(net.fireballlabs.cashguru.R.layout.activity_main);

        /*ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/

        // Title for our Activity
        mDrawerTitle = mTitle = getTitle().toString();
        // Drawer List
        mDrawerList = (RecyclerView)findViewById(net.fireballlabs.cashguru.R.id.list_drawer);
//        mDrawerList.setBackgroundColor(getResources().getColor(R.color.primary_dim));
        // Drawer View
        mDrawerLayout = (DrawerLayout) findViewById(net.fireballlabs.cashguru.R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(net.fireballlabs.cashguru.R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.

        // improve performance by indicating the list if fixed size.
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));

        // set Adapter for the Drawer List
        // User Utility class and prepare Feature List for the Drawer Activity
        List<MainDrawerAdapter.MainAppFeature> featureList = Utility.prepareFeatureList();
        mDrawerList.setAdapter(new MainDrawerAdapter(featureList, this));

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.cashguru_ic_navigation_drawer,  /* nav drawer image to replace 'Up' caret */
                net.fireballlabs.cashguru.R.string.drawer_open,  /* "open drawer" description for accessibility */
                net.fireballlabs.cashguru.R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            // TOOD Check for previous versions
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onDrawerClosed(View view) {
                setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // TOOD Check for previous versions
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onDrawerOpened(View drawerView) {
                setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // check if there is any previous state saved
        if (savedInstanceState == null) {
            // TODO check for user's setting, what kind of offers he/she needs at the home activity
            // offers like App Installs, Cashback etc.
            // for now setting it to 0th index
            selectFeature(featureList.get(0));

        }
        mOnCreateFinished = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if current API is 3.0 or more, i.e. API level 11 or more
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(net.fireballlabs.cashguru.R.color.primary)));
                /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(net.fireballlabs.cashguru.R.drawable.logo_no_shadow);
                }*/
                actionBar.setCustomView(net.fireballlabs.cashguru.R.layout.wallet_widget);
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                        | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
                updateWalletWidget(actionBar);
            }
        }
    }

    private void updateWalletWidget(ActionBar actionBar) {
        if(actionBar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final WalletWidget wallet = (WalletWidget) actionBar.getCustomView();
            if (wallet != null) {
                // TODO update with correct wallet balance
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        wallet.updateWalletBalance(Constants.INR_LABEL, Conversions.getBalance());
                    }
                });
                thread.start();
                ImageView hotDeals = (ImageView)wallet.findViewById(R.id.wallet_widget_hot_deals);
                hotDeals.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectFeature(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_LATEST_DEALS, Constants.ID_APP_LATEST_DEALS, R.drawable.hotdeals_action_bar));
                    }
                });
                ImageView referFriends = (ImageView)wallet.findViewById(R.id.wallet_widget_refer);
                referFriends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectFeature(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_REFER, Constants.ID_APP_REFER, R.drawable.refericon_action_bar));
                    }
                });
                TextView walletView = (TextView)wallet.findViewById(R.id.wallet_widget_text_view);
                walletView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectFeature(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_RECHARGE, Constants.ID_APP_RECHARGE, R.drawable.topup));
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Constants.appInstallSyncNeeded = true;
        // hide first time popup if it is still there
        Intent intent = getIntent();
        if(intent != null) {
            Utility.showFirstTimePopup(this, false);
        }
        Utility.showInformativeDialog(null, null, null, null, null, false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // sync the toggle state after onRestoreInstanceState has occurred
        mDrawerToggle.syncState();
    }

    private void selectFeature(MainDrawerAdapter.MainAppFeature feature) {
        if(!Utility.isInternetConnected(this)) {
            HardwareAccess.access(this, this, HardwareAccess.ACCESS_INTERNET);
            return;
        }
        updateWalletWidget(getActionBar());
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        // select which fragment to be shown on the screen
        switch (feature.id) {
            case Constants.ID_APP_INSTALLS:
                fragment = SlidingTabFragment.newInstance(new String[] {Constants.TITLE_APP_INSTALLS, Constants.TITLE_PENDING_INSTALLS, Constants.TITLE_COMPLETED_INSTALLS},
                        new int[] {Constants.ID_APP_INSTALLS, Constants.ID_PENDING_INSTALLS, Constants.ID_COMPLETED_INSTALLS}, this);
                break;
            case Constants.ID_APP_LATEST_DEALS:
                fragment = SlidingTabFragment.newInstance(new String[]{Constants.TITLE_APP_LATEST_DEALS},
                        new int[]{Constants.ID_APP_LATEST_DEALS}, this);
                break;
            case Constants.ID_APP_CONTACT_US:
                fragment = ContactUsFragment.newInstance(this);
                break;
            case Constants.ID_APP_RECHARGE:
                /*if(UserProfile.needProfileUpdation()) {
                    Utility.showInformativeDialog(new Utility.DialogCallback() {
                        @Override
                        public void onDialogCallback(boolean success) {
                            Fragment fragment = UserProfileFragment.newInstance(MainActivity.this);
                            transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
                            transaction.commitAllowingStateLoss();

                            mDrawerLayout.closeDrawer(mDrawerList);
                        }
                    }, this, "Need Action!", "You need to complete your User Profile before you can access Recharge page."
                    , "Take me to Profile Page", true);
                    return;
                }*/
                /*if(!ParseUser.getCurrentUser().getBoolean("emailVerified")) {
                    try {
                        ParseUser.getCurrentUser().fetch();
                        if(!ParseUser.getCurrentUser().getBoolean("emailVerified")) {
                            Utility.showInformativeDialog(new Utility.DialogCallback() {
                                @Override
                                public void onDialogCallback(boolean success) {
                                    ParseUser user = ParseUser.getCurrentUser();
                                    String email = user.getEmail();
                                    user.setEmail(email);
                                    try {
                                        user.save();
                                        Toast.makeText(MainActivity.this, "Verification email has been sent!", Toast.LENGTH_LONG).show();
                                    } catch (ParseException e) {
                                        Crashlytics.logException(e);
//                                        e.printStackTrace();
                                    }
                                }
                            }, this, "Need Action!", "You need to verify your Email before you can access Recharge page."
                                    , "Resend me verification email", true);
                            return;
                        }
                    } catch (ParseException e) {
                        Crashlytics.logException(e);
                    }
                }*/
                ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileVerifications");
                query.whereEqualTo("deviceId", ParseInstallation.getCurrentInstallation().get("deviceId"));
                query.whereEqualTo("userId", ParseInstallation.getCurrentInstallation().get("userId"));
                Utility.showProgress(this, true, "Please Wait!");
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if(parseObject != null) {
                            if(!parseObject.getBoolean("verified")) {
                                Utility.showInformativeDialog(new Utility.DialogCallback() {
                                    @Override
                                    public void onDialogCallback(boolean success) {
                                        Fragment fragment = MobileNumberVerificationFragment.newInstance(MainActivity.this);
                                        transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
                                        transaction.addToBackStack(null);

                                        transaction.commitAllowingStateLoss();
                                        mDrawerLayout.closeDrawer(mDrawerList);
                                    }
                                }, MainActivity.this, "Need Action!",
                                        "You need to verify your Mobile Number before you can access Recharge page."
                                        , "Verify Now!", true);
                                return;
                            } else {
                                if(e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    ParseUser user = ParseUser.getCurrentUser();
                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, user.getObjectId());
                                    params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, ParseInstallation
                                            .getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                                    ParseCloud.callFunctionInBackground("AddNewInstallVerification", params);
                                }
                                Fragment fragment = RechargeFragment.newInstance(MainActivity.this);
                                transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
                                transaction.addToBackStack(null);

                                transaction.commitAllowingStateLoss();
                                mDrawerLayout.closeDrawer(mDrawerList);
                            }
                        }
                    }
                });
                return;
            case Constants.ID_APP_REFER:
                fragment = ReferFriendsFragment.newInstance(this);
                break;
            case Constants.ID_APP_FAQ: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FAQ_URI));
                startActivity(intent);
                mDrawerLayout.closeDrawer(mDrawerList);
                return;
            }
            case Constants.ID_APP_TANDC: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TNC_URI));
                startActivity(intent);
                mDrawerLayout.closeDrawer(mDrawerList);
                return;
            }
            case Constants.ID_APP_PROFILE:
                fragment = UserProfileFragment.newInstance(this);
                break;
        }
        transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
        if(mOnCreateFinished) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();

        mDrawerLayout.closeDrawer(mDrawerList);

        // set title on the screen
        setTitle(feature.title);
        mTitle = feature.title;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title.toString();
    }

    @Override
    public void setEnabled(MainDrawerAdapter.MainAppFeature feature) {
        selectFeature(feature);
    }

    @Override
    public void setFragment(MainDrawerAdapter.MainAppFeature feature) {
        selectFeature(feature);
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {

    }
}
