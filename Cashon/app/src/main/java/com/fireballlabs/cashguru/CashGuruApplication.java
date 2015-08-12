package com.fireballlabs.cashguru;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseACL;
//import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Rohit on 6/24/2015.
 */
public class CashGuruApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        // Initialize Crash Reporting.
//        ParseCrashReporting.enable(this);

        // Add your initialization code here
        Parse.initialize(this, "dKemw7BZRk5yohfwgUUEfXpUfEvQ4pGLHJyqfMew", "rL3rC0Ow3I0pBYIlvjLzEZ92ty1w1Ok6N1gHRw10");
        if(ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()) {
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
        /*ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });*/
    }
}
