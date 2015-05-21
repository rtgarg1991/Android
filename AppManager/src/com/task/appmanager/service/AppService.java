/**
 * 
 */
package com.task.appmanager.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.task.appmanager.ServiceCallbackForActivity;
import com.task.appmanager.ServiceCallbackForAdapter;
import com.task.appmanager.ServiceCallbackForAdapter.UpdatedApp;
import com.task.appmanager.helper.Constants;
import com.task.appmanager.helper.Logger;
import com.task.appmanager.helper.SQLHelper;

/**
 * @author Rohit
 *
 */
public class AppService extends Service {

	private Looper mLooper;
	private AppServiceHandler mAppServiceHandler;
	private SQLHelper mSQLHelper;
	private LocalBinder mBinder = new LocalBinder();
	private ServiceCallbackForActivity mActivityCallbacks;

	public class LocalBinder extends Binder {
		public void getUninstalledApps(ServiceCallbackForAdapter callback) {
			// Create one message to send using Handler
			Message message = mAppServiceHandler.obtainMessage();
			message.what = Constants.TYPE_UNINSTALLED;

			message.obj = callback;
			// send the message
			mAppServiceHandler.sendMessage(message);
		}

		public void getUpdatedApps(ServiceCallbackForAdapter callback) {
			// Create one message to send using Handler
			Message message = mAppServiceHandler.obtainMessage();
			message.what = Constants.TYPE_UPDATED;

			message.obj = callback;
			// send the message
			mAppServiceHandler.sendMessage(message);
		}

		public void setActivityCallbacks(
				ServiceCallbackForActivity callback) {
			mActivityCallbacks = callback;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Create one HandlerThread whose looper we will use to run our Handler in background
		HandlerThread thread = new HandlerThread("[" + Constants.APP_NAME + "]");
		thread.start();

		// get the background looper
		mLooper = thread.getLooper();
		// initialize our handler
		mAppServiceHandler = new AppServiceHandler(mLooper);
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
			int req = intent.getIntExtra(Constants.REQUEST_CODE_STRING, Constants.MESSAGE_CREATE_SERVICE);
			message.what = req;
		}

		// send the message
		mAppServiceHandler.sendMessage(message);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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
			ServiceCallbackForAdapter callback;
			switch(msg.what) {
			case Constants.MESSAGE_CREATE_SERVICE:
				if(Constants.DEBUG) Logger.logDebug("Create Service");

				// create request for this service
				// lets initialize our service with references to DB if its not done already
				if(mSQLHelper == null) {
					mSQLHelper = new SQLHelper(AppService.this);
				}

				checkFullSyncNeeded();
				break;
			case Constants.MESSAGE_ADD_PACKAGES:
				if(Constants.DEBUG) Logger.logDebug("Add Packages to DB");

				// lets check all packages and load it in DB
				// TODO no need of this for now
				break;
			case Constants.MESSAGE_ADD_PACKAGE:
				if(Constants.DEBUG) Logger.logDebug("Add Package to DB");

				// lets check if we have already full synced data earlier or not
				checkFullSyncNeeded();
				intent = (Intent)msg.obj;
				if(intent == null) {
					// we don't have any information to add in DB
					// so just return
					return;
				}
				// get uid and package of the application installed by user
				uid = intent.getIntExtra(Constants.INTENT_UID, -1);
				packageName = intent.getStringExtra(Constants.INTENT_PACKAGE);
				Drawable icon = null;

				try {
					icon = getPackageManager().getApplicationIcon(packageName);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					icon = getPackageManager().getDefaultActivityIcon();
				}

				boolean success = saveIconToFileSystem(icon, uid);
				addPackageToDB(uid, packageName, success);

				// notify activity about add package
				if(mActivityCallbacks != null) {
					mActivityCallbacks.refreshAppsList(Constants.MESSAGE_ADD_PACKAGE);
				}
				break;
			case Constants.MESSAGE_DELETE_PACKAGE:
				if(Constants.DEBUG) Logger.logDebug("Delete Package from DB");

				// lets check if we have already full synced data earlier or not
				checkFullSyncNeeded();

				// delete package from DB with provided info / or just reset available info

				intent = (Intent)msg.obj;
				if(intent == null) {
					// we don't have any information to delete/ update data in DB
					// so just return
					return;
				}

				// get uid and package of the application un-installed by user
				uid = intent.getIntExtra(Constants.INTENT_UID, -1);
				packageName = intent.getStringExtra(Constants.INTENT_PACKAGE);

				deletePackageUpdateDB(uid, packageName);

				// notify activity about delete package
				if(mActivityCallbacks != null) {
					mActivityCallbacks.refreshAppsList(Constants.MESSAGE_DELETE_PACKAGE);
				}
				break;
			case Constants.MESSAGE_UPDATE_PACKAGE:
				if(Constants.DEBUG) Logger.logDebug("Package Updated, add info to DB");

				// lets check if we have already full synced data earlier or not
				checkFullSyncNeeded();

				intent = (Intent)msg.obj;
				if(intent == null) {
					// we don't have any information to update data in DB
					// so just return
					return;
				}

				// get uid and package of the application un-installed by user
				uid = intent.getIntExtra(Constants.INTENT_UID, -1);
				packageName = intent.getStringExtra(Constants.INTENT_PACKAGE);

				addUpdateInfoToDB(uid, packageName);

				// notify activity about update package
				if(mActivityCallbacks != null) {
					mActivityCallbacks.refreshAppsList(Constants.MESSAGE_UPDATE_PACKAGE);
				}
				break;
			case Constants.TYPE_UPDATED:
				// get updated apps info
				if(Constants.DEBUG) Logger.logDebug("Get Updated Apps");

				// callback where the fethed data will be sent
				callback = (ServiceCallbackForAdapter)msg.obj;
				List<ServiceCallbackForAdapter.UpdatedApp> updatedAppList = new ArrayList<ServiceCallbackForAdapter.UpdatedApp>();
				// get data from DB
				getUpdatedAppsList(updatedAppList);
				// send data to calback and update it in UI thread
				callback.setData(updatedAppList);
				break;
			case Constants.TYPE_UNINSTALLED:
				// get updated apps info
				if(Constants.DEBUG) Logger.logDebug("Get Uninstalled Apps");

				// callback where the fethed data will be sent
				callback = (ServiceCallbackForAdapter)msg.obj;
				List<ServiceCallbackForAdapter.UpdatedApp> uninstalledAppList = new ArrayList<ServiceCallbackForAdapter.UpdatedApp>();
				// get data from DB
				getUninstalledAppsList(uninstalledAppList);
				// send data to calback and update it in UI thread
				callback.setData(uninstalledAppList);
				break;
			}
		}


		private void checkFullSyncNeeded() {
			SharedPreferences pref = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);
			int syncDone = pref.getInt(Constants.PREF_FINISHED_SYNC, 0);
			if(syncDone == 0) {
				addPackagesToDB();
			}
		}
		
	}

	private boolean saveIconToFileSystem(Drawable icon, int uid) {
		// get bitmap for this icon
		Bitmap bitmap = getBitmap(icon);
		String logo = String.valueOf(uid) + Constants.LOGO_FILE_EXTENSION;

		String path = Constants.APP_LOGO_PATH;
		File file = new File(path, logo);

		// create file if not exists
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			    Logger.logError("Some Error occured while creating file in file system");
			    return false;
			}
		}

		// file output stream for writing logo to a file
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(file);
		    bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
		} catch (Exception e) {
		    e.printStackTrace();
		    Logger.logError("Some Error occured while writing logo to file system");
		    return false;
		} finally {
		    try {
		        if (out != null) {
		            out.close();
				    if(Constants.DEBUG) Logger.logDebug("Logo successfully written to file system");
				    return true;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
			    return false;
		    }
		}
	    return true;
	}


	private void getUninstalledAppsList(List<UpdatedApp> uninstalledAppList) {
		// get readable database for our query
		SQLiteDatabase database = getReadableDBInstance();
		// where clause and other data to select deleted apps only
		String whereClause = Constants.TABLE_APPS_COLUMN_AVAILABLE + "=?";
		String[] whereArgs = new String[] {"0"};
		Cursor cursor = database.query(Constants.TABLE_APPS, new String[] {Constants.TABLE_APPS_COLUMN_APP_NAME,
				Constants.TABLE_APPS_COLUMN_LOGO_NAME, Constants.TABLE_APPS_COLUMN_LAST_ACCESSED},
				whereClause, whereArgs, null, null, null);
		if(cursor == null) {
			Logger.logError("Problem happened while retreiving data of Uninstalled apps from DB");
			database.close();
		} else if(cursor.getCount() == 0) {
			cursor.close();
			database.close();
		} else {
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				UpdatedApp app = new UpdatedApp();
				app.setDate(cursor.getString(cursor.getColumnIndex(Constants.TABLE_APPS_COLUMN_LAST_ACCESSED)));
				app.setName(cursor.getString(cursor.getColumnIndex(Constants.TABLE_APPS_COLUMN_APP_NAME)));
				String path = Constants.APP_LOGO_PATH;
				File file = new File(path, cursor.getString(cursor.getColumnIndex(Constants.TABLE_APPS_COLUMN_LOGO_NAME)));

				BitmapFactory.Options options = new BitmapFactory.Options();

			    options.inJustDecodeBounds = false;
			    options.inPreferredConfig = Config.RGB_565;
			    options.inDither = true;

				Bitmap myBitmap = null;
				try {
					myBitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if(myBitmap == null) {
					app.setIcon(getPackageManager().getDefaultActivityIcon());
				} else {
					app.setBitmap(myBitmap);
				}
				uninstalledAppList.add(app);
				cursor.moveToNext();
			}
		}
	}

	public void getUpdatedAppsList(List<UpdatedApp> list) {
		// get readable database for our query
		SQLiteDatabase database = getReadableDBInstance();
		Cursor cursor = database.query(Constants.TABLE_APPS, new String[] {Constants.TABLE_APPS_COLUMN_ID, Constants.TABLE_APPS_COLUMN_PACKAGE},
				null, null, null, null, null);

		Cursor cursor2;
		// where clause and other data to select update info of particular app
		String whereClause = Constants.TABLE_UPDATES_COLUMN_APP_ID + "=?";
		String[] whereArgs;
		String orderBy = Constants.TABLE_UPDATES_COLUMN_ID + " DESC";

		// check if cursor is null, then just return
		if(cursor == null) {
			Logger.logError("Problem happened while retreiving updated apps data from DB");
			database.close();
		} else if(cursor.getCount() == 0) {
			// if there is no data returned
			// then try to load data to DB and return
			cursor.close();
			database.close();
			Logger.logError("No Data in DB, so lets just syn are return");
			addPackagesToDB();
		} else {
			// loop through all the app data from DB and find their info from Update Table
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				// create one local app object
				UpdatedApp app = new UpdatedApp();
				String packageName = cursor.getString(1);
				PackageManager pm = getPackageManager();
				ApplicationInfo info;
				try {
					info = pm.getApplicationInfo(packageName, 0);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					cursor.moveToNext();
					continue;
				}
				if(info != null) {
					app.setName(pm.getApplicationLabel(info).toString());
					app.setIcon(info.loadIcon(pm));
					
					// set application id for update table and then get data using 2nd cursor
					whereArgs = new String[] {String.valueOf(cursor.getInt(0))};
					cursor2 = database.query(Constants.TABLE_UPDATES, new String[] {Constants.TABLE_UPDATES_COLUMN_DATE},
							whereClause, whereArgs, null, null, orderBy);
					// check for data in cursor2 and update app object 
					if(cursor2 == null || cursor2.getCount() == 0) {
						if(cursor2 != null) {
							cursor2.close();
						}
						cursor.moveToNext();
						continue;
					} else {
						app.setNoOfTimes(cursor2.getCount());
						cursor2.moveToFirst();
						app.setDate(cursor2.getString(0));
						list.add(app);
					}
					if(cursor2 != null) {
						cursor2.close();
					}
				}

				cursor.moveToNext();
			}
			cursor.close();
			database.close();
		}
	}

	public void addUpdateInfoToDB(int uid, String packageName) {
		// get readable database for our query
		SQLiteDatabase database = getReadableDBInstance();

		String whereClause = Constants.TABLE_APPS_COLUMN_APP_UID + "=? AND " + Constants.TABLE_APPS_COLUMN_PACKAGE + "=?";
		String[] whereArgs = new String[] {String.valueOf(uid), packageName};

		Cursor cursor = database.query(Constants.TABLE_APPS, new String[] {Constants.TABLE_APPS_COLUMN_ID},
				whereClause, whereArgs, null, null, null);

		if(cursor == null || cursor.getCount() == 0) {
			// let try without uid
			whereClause = Constants.TABLE_APPS_COLUMN_PACKAGE + "=?";
			whereArgs = new String[] {packageName};
			cursor = database.query(Constants.TABLE_APPS, new String[] {Constants.TABLE_APPS_COLUMN_ID},
					whereClause, whereArgs, null, null, null);
			if(cursor == null) {
				Logger.logError("Error occurred while querying data from DB for package = " + packageName);
				database.close();
				return;
			} else {
				Logger.logError("No Entry found for package = " + packageName + " to add update info");
			}
		} else {
			cursor.moveToFirst();
			// get app id and add update info to DB
			int appId = cursor.getInt(0);
			cursor.close();
			String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());;

			database.close();

			database = getWritableDBInstance();
			ContentValues values = new ContentValues();
			values.put(Constants.TABLE_UPDATES_COLUMN_APP_ID, appId);
			values.put(Constants.TABLE_UPDATES_COLUMN_DATE, date);

			long id = database.insert(Constants.TABLE_UPDATES, null, values);
			if(id == -1) {
				Logger.logError("Some problem Occured while adding new update data with package name : " + packageName);
			} else {
				if(Constants.DEBUG) Logger.logDebug("update info of Package " + packageName + " added in DB");
			}
			values.clear();
			database.close();
		}
	}

	private Bitmap getBitmap(Drawable icon) {
		// if icon is BitmapDrawable, then return its bitmap
		if (icon instanceof BitmapDrawable) {
	        return ((BitmapDrawable)icon).getBitmap();
	    }

		// create bitmap using canvas and then return
		Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    icon.draw(canvas);

	    return bitmap;
	}

	private void deletePackageUpdateDB(int uid, String packageName) {
		// get writable database for our query
		SQLiteDatabase database = getWritableDBInstance();


		String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());;
		// Create content values for updating row in DB
		ContentValues values = new ContentValues();
		values.put(Constants.TABLE_APPS_COLUMN_AVAILABLE, 0);
		values.put(Constants.TABLE_APPS_COLUMN_LAST_ACCESSED, date);

		String whereClause = Constants.TABLE_APPS_COLUMN_APP_UID + "=? AND " + Constants.TABLE_APPS_COLUMN_PACKAGE + "=?";
		String[] whereArgs = new String[] {String.valueOf(uid), packageName};

		// update data in DB
		int rowsUpdated = database.update(Constants.TABLE_APPS, values, whereClause, whereArgs);

		if(rowsUpdated == 0) {
			// failed to update DB with uid and package, so now let try with only package
			whereClause = Constants.TABLE_APPS_COLUMN_PACKAGE + "=?";
			whereArgs = new String[] {packageName};
			rowsUpdated = database.update(Constants.TABLE_APPS, values, whereClause, whereArgs);
			if(rowsUpdated == 0) {
				// we are not able to update data in DB
				Logger.logError("Some problem Occured while Updating package availability in DB for package : " + packageName);
			} else {
				if(Constants.DEBUG) Logger.logDebug("Package " + packageName + " updated about its non-availability in DB");
			}
		} else {
			if(Constants.DEBUG) Logger.logDebug("Package " + packageName + " updated about its non-availability in DB");
		}
		values.clear();
		database.close();
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
				// get and store icon in db
				boolean success = saveIconToFileSystem(info.loadIcon(pm), info.uid);
				// add data to db
				addPackageToDB(info.uid, info.packageName, success);
			} else {
				// system application
				// skipping for now
				// lots of faltu applications and services
			}
		}
		SharedPreferences pref = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(Constants.PREF_FINISHED_SYNC, 1);
		editor.commit();
	}

	public void addPackageToDB(int uid, String packageName, boolean success) {
		// first lets check whether this package is already present in DB

		// get readable database for our query
		SQLiteDatabase database = getReadableDBInstance();

		String whereClause = Constants.TABLE_APPS_COLUMN_APP_UID + "=? AND " + Constants.TABLE_APPS_COLUMN_PACKAGE + "=?";
		String[] whereArgs = new String[] {String.valueOf(uid), packageName};

		Cursor cursor = database.query(Constants.TABLE_APPS, new String[] {Constants.TABLE_APPS_COLUMN_ID},
				whereClause, whereArgs, null, null, null);

		if(cursor == null || cursor.getCount() == 0) {
			// let try without uid
			whereClause = Constants.TABLE_APPS_COLUMN_PACKAGE + "=?";
			whereArgs = new String[] {packageName};
			cursor = database.query(Constants.TABLE_APPS, new String[] {Constants.TABLE_APPS_COLUMN_ID},
					whereClause, whereArgs, null, null, null);
			if(cursor == null || cursor.getCount() == 0) {
				// no such app present in db
				// so lets add it in db

			} else {
				Logger.logError("Application with same package already present");
				// lets update its uid

				// move to first position
				cursor.moveToFirst();
				// get primary id of this package from db
				int primaryId = cursor.getInt(0);

				// close db and cursor
				database.close();
				cursor.close();

				// get writable db instance
				database = getWritableDBInstance();
				// we need to update app uid
				ContentValues values = new ContentValues();
				values.put(Constants.TABLE_APPS_COLUMN_APP_UID, uid);
				values.put(Constants.TABLE_APPS_COLUMN_AVAILABLE, 1);

				// where clause and where args for this update statement
				whereClause = Constants.TABLE_APPS_COLUMN_ID + "=?";
				whereArgs = new String[] {String.valueOf(primaryId)};

				// update the data
				int rowsAffected = database.update(Constants.TABLE_APPS, values, whereClause, whereArgs);
				if(rowsAffected == 0) {
					Logger.logError("Some problem Occured while updating data with package name : " + packageName);
				} else {
					if(Constants.DEBUG) Logger.logDebug("updated info of Package " + packageName + " in DB");
				}
				values.clear();
				database.close();
				return;
			}
		} else {
			Logger.logError("Application with same package and uid already present");
			cursor.close();
			database.close();
			return;
		}

		database.close();
		if(cursor != null) {
			cursor.close();
		}

		// get writable database for our query
		database = getWritableDBInstance();

		String logo = String.valueOf(uid) + Constants.LOGO_FILE_EXTENSION;

		PackageManager pm = getPackageManager();
		ApplicationInfo info;
		try {
			info = pm.getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			info = null;
		}
		// Create content values for new package in DB
		ContentValues values = new ContentValues();
		values.put(Constants.TABLE_APPS_COLUMN_APP_UID, uid);
		values.put(Constants.TABLE_APPS_COLUMN_PACKAGE, packageName);
		values.put(Constants.TABLE_APPS_COLUMN_APP_NAME, (String) (info != null ? pm.getApplicationLabel(info) : "(unknown)"));
		values.put(Constants.TABLE_APPS_COLUMN_AVAILABLE, 1);
		if(success) {
			values.put(Constants.TABLE_APPS_COLUMN_LOGO_NAME, logo);
		} else {
			values.putNull(Constants.TABLE_APPS_COLUMN_LOGO_NAME);
		}

		// insert data to DB
		long id = database.insert(Constants.TABLE_APPS, null, values);

		if(id == -1) {
			Logger.logError("Some problem Occured while adding new package data with package name : " + packageName);
		} else {
			if(Constants.DEBUG) Logger.logDebug("Package " + packageName + " added in DB");
		}
		values.clear();
		database.close();
	}

	private SQLiteDatabase getWritableDBInstance() {
		// if we don't have SQL Helper instance, then create one
		if(mSQLHelper == null) {
			mSQLHelper = new SQLHelper(AppService.this);
		}
		// get writable instance of the DB
		SQLiteDatabase database = mSQLHelper.getWritableDatabase();
		return database;
	}

	private SQLiteDatabase getReadableDBInstance() {
		// if we don't have SQL Helper instance, then create one
		if(mSQLHelper == null) {
			mSQLHelper = new SQLHelper(AppService.this);
		}
		// get readable instance of the DB
		SQLiteDatabase database = mSQLHelper.getReadableDatabase();
		return database;
	}
}
