package net.fireballlabs.cashguru;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.Conversions;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.Referrals;
import net.fireballlabs.helper.model.UserProfile;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.ui.ContactUsFragment;
import net.fireballlabs.ui.MobileNumberVerificationFragment;
import net.fireballlabs.ui.NotificationFragment;
import net.fireballlabs.ui.OfferFragment;
import net.fireballlabs.ui.ReferFriendsFragment;
import net.fireballlabs.ui.SlidingTabFragment;
import net.fireballlabs.ui.UserProfileFragment;
import net.fireballlabs.view.WalletWidget;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity implements MainActivityCallBacks, HardwareAccess.HardwareAccessCallbacks {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
//    private RecyclerView mDrawerList;
    String mTitle = null;
    String mDrawerTitle = null;
    private boolean mOnCreateFinished = false;
    private int mCurrentFeature = -1;
    private NavigationView navigationView;
    private Toolbar mToolbar;
    private View wallet;

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
        //PushService.setDefaultPushCallback(this, MainActivity.class);
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

        if(ParseUser.getCurrentUser() == null || !ParseUser.getCurrentUser().isAuthenticated()) {
            finish();
            return;
        }

        // load profile data from shared preferences
        UserProfile.loadSavedProfileData(this);

        String offerId = null;
        int fragmentId = 1;
        // show first time popup
        Intent intent = getIntent();
        if(intent != null) {
            boolean isNewLogin = intent.getBooleanExtra(Constants.IS_NEW_LOGIN, false);
            if(isNewLogin) {
                Utility.showFirstTimePopup(this, true);
                UserProfile.syncProfileData(this);
                readAllContacts(this);
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
                if(user != null) {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
                    params.put(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, ParseInstallation
                            .getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                    ParseCloud.callFunctionInBackground("AddNewInstallVerification", params, new FunctionCallback<Boolean>() {
                        @Override
                        public void done(Boolean success, ParseException e) {
                            if (success != null) {

                            }
                        }
                    });
                }
            }
            if(intent.hasExtra(Constants.ACTIVITY_LAUNCH_PARAM_OFFER_ID)) {
                offerId = intent.getStringExtra(Constants.ACTIVITY_LAUNCH_PARAM_OFFER_ID);
            } else if(intent.hasExtra(Constants.ACTIVITY_LAUNCH_PARAM_FRAGMENT_ID)) {
                fragmentId = intent.getIntExtra(Constants.ACTIVITY_LAUNCH_PARAM_FRAGMENT_ID, 1);
            }
        }

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        super.onCreate(savedInstanceState);
        setContentView(net.fireballlabs.cashguru.R.layout.activity_main);


        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        wallet = getLayoutInflater().inflate(R.layout.wallet_widget, null);
        mToolbar.addView(wallet);


        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            //actionBar.setHomeAsUpIndicator(R.drawable.cashguru_ic_navigation_drawer);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setLogo(R.drawable.logo_status_bar);
            actionBar.setDisplayShowTitleEnabled(false);
//            mToolbar.setNavigationIcon(R.drawable.cashguru_ic_navigation_drawer);
        }

        /*ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/

        // Title for our Activity
        mDrawerTitle = mTitle = getTitle().toString();
        // Drawer List
//        mDrawerList = (RecyclerView)findViewById(net.fireballlabs.cashguru.R.id.list_drawer);
//        mDrawerList.setBackgroundColor(getResources().getColor(R.color.primary_dim));
        // Drawer View
        mDrawerLayout = (DrawerLayout) findViewById(net.fireballlabs.cashguru.R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(net.fireballlabs.cashguru.R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerLayout = navigationView.inflateHeaderView(R.layout.drawer_header);

        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFeature(Constants.ID_APP_PROFILE, null);
            }
        });
        TextView emailView = (TextView)headerLayout.findViewById(R.id.drawer_header_text_view);
        emailView.setText(ParseUser.getCurrentUser().getEmail());


//Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                mDrawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.offer_wall:
                        selectFeature(Constants.ID_APP_INSTALLS, null);
                        break;
                    case R.id.hot_deals:
                        selectFeature(Constants.ID_APP_LATEST_DEALS, null);
                        break;
                    case R.id.refer:
                        selectFeature(Constants.ID_APP_REFER, null);
                        break;
                    case R.id.top_up:
                        selectFeature(Constants.ID_APP_RECHARGE, null);
                        break;
                    case R.id.contact_us:
                        selectFeature(Constants.ID_APP_CONTACT_US, null);
                        break;
                    case R.id.tandc:
                        selectFeature(Constants.ID_APP_TANDC, null);
                        break;
                    case R.id.faq:
                        selectFeature(Constants.ID_APP_FAQ, null);
                        break;
                }
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                mToolbar,
                net.fireballlabs.cashguru.R.string.drawer_open,  /* "open drawer" description for accessibility */
                net.fireballlabs.cashguru.R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            // TOOD Check for previous versions
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            // TODO Check for previous versions
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // check if there is any previous state saved
        /*if (savedInstanceState == null) {
            // TODO check for user's setting, what kind of offers he/she needs at the home activity
            // offers like App Installs, Cashback etc.
            // for now setting it to 0th index
            selectFeature(featureList.get(1), null);

        }*/
        mOnCreateFinished = true;

        if(installation.getString(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID) == null
                || installation.getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID) == null) {
            String deviceId = Utility.generateDeviceUniqueId(this);
            installation.put(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID, ParseUser.getCurrentUser().getObjectId());
            installation.put(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, deviceId);
        }
        if(offerId != null && !"".equals(offerId)) {
            selectFeature(Constants.ID_APP_OFFER, offerId);
        } else {
            selectFeature(fragmentId, null);
        }

        Referrals.syncReferralBonus(getApplicationContext());
    }

    private void readAllContacts(final Context context) {
        if(PreferenceManager.getDefaultSharedPreferenceValue(this, Constants.PREF_SEND_CONTACTS, MODE_PRIVATE, true)) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ContentResolver cr = getContentResolver();
                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);
                    if (cur != null && cur.getCount() > 0) {
                        StringBuilder data = new StringBuilder();
                        while (cur.moveToNext()) {
                            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            if (Integer.parseInt(cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                Cursor pCur = cr.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{id}, null);
                                if (pCur != null) {
                                    while (pCur.moveToNext()) {
                                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        data.append(name + " : " + phoneNo + "\n");
                                    }
                                    pCur.close();
                                }
                            }
                        }
                        cur.close();

                        byte[] byteData = data.toString().getBytes();
                        final ParseFile file = new ParseFile("contacts.txt", byteData);
                        file.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {

                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    params.put("userId", ParseUser.getCurrentUser().getObjectId());
                                    params.put("file", file);
                                    try {
                                        ParseCloud.callFunction(ParseConstants.FUNCTION_SAVE_CONTACTS, params);
                                        PreferenceManager.setDefaultSharedPreferenceValue(MainActivity.this, Constants.PREF_SEND_CONTACTS, MODE_PRIVATE, false);
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else if (cur != null) {
                        cur.close();
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String amount = PreferenceManager.getDefaultSharedPreferenceValue(this, Constants.PREF_MOBILE_RECHARGE_DONE, Context.MODE_PRIVATE, "");
        if(!(amount == null || "".equals(amount))) {
            Utility.showInformativeDialog(new Utility.DialogCallback() {
                @Override
                public void onDialogCallback(boolean success) {
                    if(success) {
                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                        }
                    }
                }
            }, this, "Recharge Done!", "Your Recharge request of " + Constants.INR_LABEL + "" + amount + " has been processed successfully.\r\n\r\n" +
                    "If you like our service, rate us on the Play store!", "Rate Now", "Not Now", true);

            PreferenceManager.setDefaultSharedPreferenceValue(this, Constants.PREF_MOBILE_RECHARGE_DONE, Context.MODE_PRIVATE, "");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if current API is 3.0 or more, i.e. API level 11 or more
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(net.fireballlabs.cashguru.R.color.primary)));
                /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    actionBar.setLogo(net.fireballlabs.cashguru.R.drawable.logo_status_bar);
                }*/

                //actionBar.setCustomView(net.fireballlabs.cashguru.R.layout.wallet_widget);
                updateWalletWidget(actionBar);
            }
        }
    }

    private void updateWalletWidget(ActionBar actionBar) {
        if(actionBar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //final WalletWidget wallet = (WalletWidget) actionBar.getCustomView();
            if (wallet != null) {
                // TODO update with correct wallet balance
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ((WalletWidget) wallet).updateWalletBalance(Constants.INR_LABEL, Conversions.getBalance(MainActivity.this, false), MainActivity.this);
                    }
                });
                thread.start();
                ImageView notification = (ImageView)wallet.findViewById(R.id.wallet_widget_notification);
                notification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectFeature(Constants.ID_APP_NOTIFICATION, null);
                    }
                });
                TextView walletView = (TextView)wallet.findViewById(R.id.wallet_widget_text_view);
                walletView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectFeature(Constants.ID_APP_RECHARGE, null);
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

        Utility.showFirstTimePopup(this, false);
        Utility.showInformativeDialog(null, null, null, null, null, false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // sync the toggle state after onRestoreInstanceState has occurred
        mDrawerToggle.syncState();
    }

    private void selectFeature(final int id, Object extra) {
        if(!Utility.isInternetConnected(this)) {
            HardwareAccess.access(this, this, HardwareAccess.ACCESS_INTERNET);
            return;
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            updateWalletWidget(getSupportActionBar());
        }
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        // select which fragment to be shown on the screen
        switch (id) {
            case Constants.ID_APP_INSTALLS:
                fragment = SlidingTabFragment.newInstance(new String[] {Constants.TITLE_APP_INSTALLS, Constants.TITLE_PENDING_INSTALLS, Constants.TITLE_COMPLETED_INSTALLS},
                        new int[] {Constants.ID_APP_INSTALLS, Constants.ID_PENDING_INSTALLS, Constants.ID_COMPLETED_INSTALLS}, this);
                break;
            case Constants.ID_APP_LATEST_DEALS:
                fragment = SlidingTabFragment.newInstance(new String[]{Constants.TITLE_APP_LATEST_DEALS},
                        new int[]{Constants.ID_APP_LATEST_DEALS}, this);
                break;
            case Constants.ID_APP_CONTACT_US:
                fragment = ContactUsFragment.newInstance(Constants.TITLE_APP_CONTACT_US, this);
                break;
            case Constants.ID_APP_RECHARGE:
                if(!PreferenceManager.getDefaultSharedPreferenceValue(this, Constants.PREF_MOBILE_VERIFIED, MODE_PRIVATE, false)) {
                    final ParseUser user = ParseUser.getCurrentUser();
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileVerifications");
                    query.whereEqualTo(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, ParseInstallation.getCurrentInstallation().get(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                    query.whereEqualTo(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
                    Utility.showProgress(this, true, "Please Wait!");
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (parseObject != null) {
                                if (!parseObject.getBoolean("verified")) {
                                    Utility.showInformativeDialog(new Utility.DialogCallback() {
                                                                      @Override
                                                                      public void onDialogCallback(boolean success) {
                                                                          if(Utility.isValidContext(MainActivity.this)) {
                                                                              Fragment fragment = MobileNumberVerificationFragment.newInstance(MainActivity.this);
                                                                              transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
                                                                              //transaction.addToBackStack(null);

                                                                              transaction.commitAllowingStateLoss();
                                                                              mDrawerLayout.closeDrawers();
                                                                          }
                                                                      }
                                                                  }, MainActivity.this, "Need Action!",
                                            "You need to verify your Mobile Number before you can access Recharge page."
                                            , "Verify Now!", true);
                                    Utility.showProgress(MainActivity.this, false, null);
                                    return;
                                } else {
                                    if(Utility.isValidContext(MainActivity.this)) {
                                        PreferenceManager.setDefaultSharedPreferenceValue(MainActivity.this, Constants.PREF_MOBILE_VERIFIED, MODE_PRIVATE, true);
                                        Fragment fragment = SlidingTabFragment.newInstance(new String[]{Constants.TITLE_APP_RECHARGE, Constants.TITLE_APP_RECHARGE_HISTORY},
                                                new int[]{Constants.ID_APP_RECHARGE, Constants.ID_APP_RECHARGE_HISTORY}, MainActivity.this);
                                        transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
                                        if (mCurrentFeature != id) {
//                                            transaction.addToBackStack(null);
                                            mCurrentFeature = id;
                                        }

                                        transaction.commitAllowingStateLoss();
                                        mDrawerLayout.closeDrawers();
                                    }
                                }

                                if(Utility.isValidContext(MainActivity.this)) {
                                    Utility.showProgress(MainActivity.this, false, null);
                                }
                            } else {
                                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    params.put(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
                                    params.put(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, ParseInstallation
                                            .getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                                    ParseCloud.callFunctionInBackground("AddNewInstallVerification", params, new FunctionCallback<Boolean>() {
                                        @Override
                                        public void done(Boolean success, ParseException e) {
                                            if(Utility.isValidContext(MainActivity.this)) {
                                                if (success != null) {
                                                    Fragment fragment = SlidingTabFragment.newInstance(new String[]{Constants.TITLE_APP_RECHARGE, Constants.TITLE_APP_RECHARGE_HISTORY},
                                                            new int[]{Constants.ID_APP_RECHARGE, Constants.ID_APP_RECHARGE_HISTORY}, MainActivity.this);
                                                    transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
                                                    if (mCurrentFeature != id) {
//                                                        transaction.addToBackStack(null);
                                                        mCurrentFeature = id;
                                                    }

                                                    transaction.commitAllowingStateLoss();
                                                    mDrawerLayout.closeDrawers();
                                                }
                                                Utility.showProgress(MainActivity.this, false, null);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                    return;
                }

                fragment = SlidingTabFragment.newInstance(new String[] {Constants.TITLE_APP_RECHARGE, Constants.TITLE_APP_RECHARGE_HISTORY},
                        new int[] {Constants.ID_APP_RECHARGE, Constants.ID_APP_RECHARGE_HISTORY}, this);
                break;
            case Constants.ID_APP_REFER:
                fragment = ReferFriendsFragment.newInstance(this);
                break;
            case Constants.ID_APP_FAQ: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.FAQ_URI));
                startActivity(intent);
                mDrawerLayout.closeDrawers();
                return;
            }
            case Constants.ID_APP_TANDC: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TNC_URI));
                startActivity(intent);
                mDrawerLayout.closeDrawers();
                return;
            }
            case Constants.ID_APP_PROFILE:
                fragment = UserProfileFragment.newInstance(this);
                break;
            case Constants.ID_APP_NOTIFICATION:
                fragment = NotificationFragment.newInstance(this);
                break;
            case Constants.ID_APP_OFFER:
                fragment = OfferFragment.newInstance(this, (String)extra);
                break;
        }
        transaction.replace(net.fireballlabs.cashguru.R.id.content_fragment, fragment);
        if(mOnCreateFinished && mCurrentFeature != id) {
            //transaction.addToBackStack(null);
            mCurrentFeature = id;
        }
        transaction.commitAllowingStateLoss();

        mDrawerLayout.closeDrawers();

        // set title on the screen
//        setTitle(feature.title);
//        mTitle = feature.title;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title.toString();
    }

    @Override
    public void setFragment(int id, Object extra) {
        selectFeature(id, extra);
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {

    }
    @Override
    public void onBackPressed() {
        if(mCurrentFeature == Constants.ID_APP_INSTALLS) {
            finish();
        } else {
            selectFeature(Constants.ID_APP_INSTALLS, null);
        }
        //super.onBackPressed();
    }
}
