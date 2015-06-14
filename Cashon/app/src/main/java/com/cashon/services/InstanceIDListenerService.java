package com.cashon.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// TODO Temporary Listener Service for Instance ID, need to change it as per requirement
public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {
    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        // Need to update new token on server
        // lets leave it for now, and complete in future
    }
    // [END refresh_token]
}
