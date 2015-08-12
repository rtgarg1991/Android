package com.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
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

import com.fireballlabs.MainActivityCallBacks;
import com.fireballlabs.adapter.MainDrawerAdapter;
import com.fireballlabs.helper.Constants;
import com.fireballlabs.helper.model.Conversions;
import com.fireballlabs.helper.model.InstallationHelper;
import com.fireballlabs.helper.model.UserHelper;
import com.fireballlabs.impl.Utility;
import com.fireballlabs.ui.ContactUsFragment;
import com.fireballlabs.ui.LatestDealsFragment;
import com.fireballlabs.ui.RechargeFragment;
import com.fireballlabs.ui.ReferFriendsFragment;
import com.fireballlabs.ui.SlidingTabFragment;
import com.fireballlabs.view.WalletWidget;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.util.List;

public class MainActivity extends FragmentActivity implements MainDrawerAdapter.DrawerAdapterCallbacks, MainActivityCallBacks {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mDrawerList;
    String mTitle = null;
    String mDrawerTitle = null;

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
            finish();
            super.onCreate(savedInstanceState);
            return;
        }
        installation.put(InstallationHelper.PARSE_TABLE_COLUMN_REFER_CODE, ParseUser.getCurrentUser().getObjectId());
        installation.put(InstallationHelper.PARSE_TABLE_COLUMN_EMAIL, ParseUser.getCurrentUser().getUsername());
        installation.put(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, ParseUser.getCurrentUser().get(UserHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if(e == null) {
                    // TODO Empty
                } else {
                    // TODO error reporting
                }
            }
        });
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/

        // Title for our Activity
        mDrawerTitle = mTitle = getTitle().toString();
        // Drawer List
        mDrawerList = (RecyclerView)findViewById(R.id.list_drawer);
//        mDrawerList.setBackgroundColor(getResources().getColor(R.color.primary_dim));
        // Drawer View
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
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
                R.drawable.drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
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
        // show first time popup
        Intent intent = getIntent();
        if(intent != null) {
            boolean isNewLogin = intent.getBooleanExtra(Constants.IS_NEW_LOGIN, false);
            if(isNewLogin) {
                Utility.showFirstTimePopup(this);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if current API is 3.0 or more, i.e. API level 11 or more
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(R.drawable.logo_no_shadow);
                }
                actionBar.setCustomView(R.layout.wallet_widget);
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
                final WalletWidget wallet = (WalletWidget)actionBar.getCustomView();
                if(wallet != null) {
                    // TODO update with correct wallet balance
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            wallet.updateWalletBalance(Constants.INR_LABEL, Conversions.getBalance());
                        }
                    });
                    thread.start();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Constants.appInstallSyncNeeded = true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // sync the toggle state after onRestoreInstanceState has occurred
        mDrawerToggle.syncState();
    }

    private void selectFeature(MainDrawerAdapter.MainAppFeature feature) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        // select which fragment to be shown on the screen
        switch (feature.id) {
            case Constants.ID_APP_INSTALLS:
                fragment = SlidingTabFragment.newInstance(new String[] {Constants.TITLE_APP_INSTALLS, Constants.TITLE_PENDING_INSTALLS, Constants.TITLE_COMPLETED_INSTALLS},
                        new int[] {Constants.ID_APP_INSTALLS, Constants.ID_PENDING_INSTALLS, Constants.ID_COMPLETED_INSTALLS}, this);
                break;
            case Constants.ID_APP_LATEST_DEALS:
                fragment = LatestDealsFragment.newInstance(this);
                break;
            case Constants.ID_APP_CONTACT_US:
                fragment = ContactUsFragment.newInstance(this);
                break;
            case Constants.ID_APP_RECHARGE:
                fragment = RechargeFragment.newInstance(this);
                break;
            case Constants.ID_APP_REFER:
                fragment = ReferFriendsFragment.newInstance(this);
                break;
        }
        transaction.replace(R.id.content_fragment, fragment);
        transaction.commit();

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
}
