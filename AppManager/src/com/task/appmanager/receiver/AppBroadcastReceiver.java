/**
 * 
 */
package com.task.appmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.task.appmanager.helper.Constants;
import com.task.appmanager.helper.Logger;
import com.task.appmanager.service.AppService;

/**
 * @author Rohit
 *
 */
public class AppBroadcastReceiver extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			if(Constants.DEBUG) Logger.logDebug("Boot Received");

			// Let us start our service
			Intent serviceIntent = new Intent(context, AppService.class);
			serviceIntent.putExtra(Constants.REQUEST_CODE_STRING, Constants.MESSAGE_CREATE_SERVICE);

			context.startService(serviceIntent);

		} else if(Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

			Uri uri = intent.getData(); // package name of the application being added
			Bundle info = intent.getExtras();
			int uid = info.getInt(Intent.EXTRA_UID); // user id of the application
			boolean replacing = info.getBoolean(Intent.EXTRA_REPLACING); // whether this is being replaced

			if(Constants.DEBUG) {
				Logger.logDebug("New Application Package Added");
				Logger.logDebug(String.valueOf(uri.toString()) + " installed with uid = " + uid
						+ " and replacing is = " + String.valueOf(replacing));
			}

			if(!replacing) {
				// if application package is not replaced
				// add it in DB

				Intent serviceIntent = new Intent(context, AppService.class);
				serviceIntent.putExtra(Constants.REQUEST_CODE_STRING, Constants.MESSAGE_ADD_PACKAGE);
				serviceIntent.putExtra(Constants.INTENT_UID, uid);
				serviceIntent.putExtra(Constants.INTENT_PACKAGE, uri.toString().substring(Constants.PACKAGE_DATA_START.length()));
				context.startService(serviceIntent);
			}
		} else if(Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
			Uri uri = intent.getData(); // package name of the application being removed
			Bundle info = intent.getExtras();
			int uid = info.getInt(Intent.EXTRA_UID); // user id of the application

			if(Constants.DEBUG) {
				Logger.logDebug("New Application Package Added");
				Logger.logDebug(String.valueOf(uri.toString()) + " fully removed with uid = " + uid);
			}

			// remove application package from db

			Intent serviceIntent = new Intent(context, AppService.class);
			serviceIntent.putExtra(Constants.REQUEST_CODE_STRING, Constants.MESSAGE_DELETE_PACKAGE);
			serviceIntent.putExtra(Constants.INTENT_UID, uid);
			serviceIntent.putExtra(Constants.INTENT_PACKAGE, uri.toString().substring(Constants.PACKAGE_DATA_START.length()));
			context.startService(serviceIntent);

			// TODO change available in DB
		} else if(Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
			Uri uri = intent.getData(); // package name of the application being updated
			Bundle info = intent.getExtras();
			int uid = info.getInt(Intent.EXTRA_UID); // new user id of the application

			if(Constants.DEBUG) {
				Logger.logDebug("New Application Package Added");
				Logger.logDebug(String.valueOf(uri.toString()) + " updated with new uid = " + uid);
			}

			// add update info of application in DB

			Intent serviceIntent = new Intent(context, AppService.class);
			serviceIntent.putExtra(Constants.REQUEST_CODE_STRING, Constants.MESSAGE_UPDATE_PACKAGE);
			serviceIntent.putExtra(Constants.INTENT_UID, uid);
			serviceIntent.putExtra(Constants.INTENT_PACKAGE, uri.toString().substring(Constants.PACKAGE_DATA_START.length()));
			context.startService(serviceIntent);
		}
	}

}
