/**
 * 
 */
package com.cashon.services;


import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cashon.helper.Constants;
import com.cashon.helper.Logger;
import com.cashon.helper.PreferenceManager;
import com.cashon.helper.model.UsedOffer;

/**
 * @author Rohit
 *
 */
public class AppService extends Service {

	private AppServiceHandler mAppServiceHandler;


	@Override
	public void onCreate() {
		super.onCreate();

		// Create one HandlerThread whose looper we will use to run our Handler in background
		HandlerThread thread = new HandlerThread("[" + Constants.APP_NAME + "]");
		thread.start();

		// get the background looper
        Looper looper = thread.getLooper();
		// initialize our handler
		mAppServiceHandler = new AppServiceHandler(looper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// send this intent to handler for handling it in Background Thread
		sendIntentToHandler(intent);
		return START_STICKY;
	}

	private void sendIntentToHandler(Intent intent) {
		// Create one message to send using Handler
		Message message = mAppServiceHandler.obtainMessage();
		message.obj = intent;

		if(intent == null) {
			// start sticky worked
			// lets just start our service and load some data if we need
			message.what = Constants.MESSAGE_CREATE_SERVICE;
		} else {
			// Check the req code from BroadcastReceiver or Activity
			// and perform operations accordingly
			message.what = intent.getIntExtra(Constants.REQUEST_CODE_STRING, Constants.MESSAGE_CREATE_SERVICE);
		}

		// send the message
		mAppServiceHandler.sendMessage(message);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	class AppServiceHandler extends Handler {

		public AppServiceHandler(Looper looper) {
			super(looper);
		}


		@Override
		public void handleMessage(Message msg) {
			Intent intent;
			int uid;
			String packageName;
			switch(msg.what) {
			case Constants.MESSAGE_CREATE_SERVICE:

				// check if we need to sync app data with server for current device
				checkFullSyncNeeded(null);
				break;
			case Constants.MESSAGE_ADD_PACKAGES:
				Logger.doSecureLogging(Log.INFO, "Add Packages to DB");

                checkFullSyncNeeded(null);
				break;
			case Constants.MESSAGE_ADD_PACKAGE:
				Logger.doSecureLogging(Log.INFO, "Add Package to DB");

                intent = (Intent)msg.obj;
                if(intent == null) {
                    // we don't have any information to add in DB
                    // so just return
                    return;
                }
                // get uid and package of the application installed by user
                uid = intent.getIntExtra(Constants.INTENT_UID, -1);
                packageName = intent.getStringExtra(Constants.INTENT_PACKAGE);

                addPackageToDB(packageName, true);

				// check if we need to sync app data with server for current device
				checkFullSyncNeeded(null);
				break;
			case Constants.MESSAGE_DELETE_PACKAGE:
				Logger.doSecureLogging(Log.INFO, "Delete Package from DB");

				// delete package from DB with provided info / or just reset available info

				intent = (Intent)msg.obj;
				if(intent == null) {
					// we don't have any information to delete/ update data in DB
					// so just return
					return;
				}

				// get package of the application un-installed by user
				packageName = intent.getStringExtra(Constants.INTENT_PACKAGE);

				// lets check if we have already full synced data earlier or not
				checkFullSyncNeeded(packageName);

				deletePackageUpdateDB(packageName);
				break;
			}
		}


		private void checkFullSyncNeeded(String packageName) {
			boolean syncDone = PreferenceManager.getDefaultSharedPreferenceValue(getApplicationContext(),
					Constants.PREF_FINISHED_SYNC, MODE_PRIVATE, false);
			if(!syncDone) {
				addPackagesToDB();
				if(packageName != null) {
					addPackageToDB(packageName, false);
				}
			}
		}
		
	}

	private void deletePackageUpdateDB(String packageName) {
		// get writable database for our query
        boolean packagUpdated = UsedOffer.deletePackageUpdateDB(getApplicationContext(), packageName);

        if(packagUpdated) {
            //TODO lets check and add this package to cloud DB for affiliation
            boolean needRetry = UsedOffer.checkAndRemovePackageOnCloud(getApplicationContext(), packageName);
			if(needRetry) {
				UsedOffer.checkAndAddPackageOnCloud(getApplicationContext(), packageName);
			}
        }
	}

	public void addPackagesToDB() {
		// get package manager for querying all data
		PackageManager pm = getPackageManager();
		// get all installed applications
		List<ApplicationInfo> appInfos = pm.getInstalledApplications(0);
		// loop through all the apps
		for(ApplicationInfo info : appInfos) {
			// if not system app, only then add to db
			if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				// add data to db
				addPackageToDB(info.packageName, false);
			}
		}
        PreferenceManager.setDefaultSharedPreferenceValue(getApplicationContext(), Constants.PREF_FINISHED_SYNC, MODE_PRIVATE, true);
	}

	public void addPackageToDB(String packageName, boolean needCheck) {
		// first lets check whether this package is already present in DB

        boolean packagAdded = UsedOffer.addPackageToDB(getApplicationContext(), packageName, needCheck);

        // if package is added in local DB meaning new app
        // and this package is being added because of new installation i.e. not a full sync
        // then check if we need to sync this info on cloud
        if(packagAdded && needCheck) {
			boolean needRetry = UsedOffer.checkAndAddPackageOnCloud(getApplicationContext(), packageName);
			if(needRetry) {
				UsedOffer.checkAndAddPackageOnCloud(getApplicationContext(), packageName);
			}
        }
	}
}
