package com.cashon.cashon;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Rohit on 6/24/2015.
 */
public class CashOnApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

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
