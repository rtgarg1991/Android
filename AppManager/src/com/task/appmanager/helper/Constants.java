package com.task.appmanager.helper;

import java.io.File;

import android.os.Environment;

public class Constants {
	public static final int APP_VERSION = 1;
	public static final String APP_NAME = "APP MANAGER";
	public static final String APP_LOGO_PATH = Environment.getExternalStorageDirectory().toString() + "/APP_MANAGER";

	public static final boolean DEBUG = true;
	public static final String TAG = "ROHIT";

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "app_manager.db";

	public static final String REQUEST_CODE_STRING = "REQUEST";
	public static final String INTENT_UID = "UID";
	public static final String INTENT_PACKAGE = "PACKAGE";
	public static final String CALLBACK_OBJECT = "CALLBACK";

	public static final int MESSAGE_CREATE_SERVICE = 1;
	public static final int MESSAGE_ADD_PACKAGES = 2;
	public static final int MESSAGE_ADD_PACKAGE = 3;
	public static final int MESSAGE_DELETE_PACKAGE = 4;
	public static final int MESSAGE_UPDATE_PACKAGE = 5;
	public static final int MESSAGE_RECEIVE_UPDATED_APPS = 6;
	public static final int MESSAGE_RECEIVE_UNINSTALLED_APPS = 7;

	public static final int NOTIFY_DATASET_CHANGED = 21;
	public static final int NOTIFY_DATA_CHANGED = 22;

	public static final String TABLE_APPS = "apps";
	public static final String TABLE_UPDATES = "updates";

	public static final String TABLE_APPS_COLUMN_ID = "_id";
	public static final String TABLE_APPS_COLUMN_APP_UID = "_uid";
	public static final String TABLE_APPS_COLUMN_PACKAGE = "_package";
	public static final String TABLE_APPS_COLUMN_APP_NAME = "_name";
	public static final String TABLE_APPS_COLUMN_LOGO_NAME = "_logo";
	public static final String TABLE_APPS_COLUMN_AVAILABLE = "_available";
	public static final String TABLE_APPS_COLUMN_LAST_ACCESSED = "_access";

	public static final String TABLE_UPDATES_COLUMN_ID = "_id";
	public static final String TABLE_UPDATES_COLUMN_APP_ID = "_appid";
	public static final String TABLE_UPDATES_COLUMN_DATE = "_date";

	public static final String LOGO_FILE_EXTENSION = ".png";

	public static final String PREF_FILE = "pref.xml";
	public static final String PREF_FINISHED_SYNC = "sync_done";

	public static final String PACKAGE_DATA_START = "package:";

    public static final int VIEW_PAGER_ID = 100000001;

	public static final int TYPE_INSTALLED = 10;
	public static final int TYPE_UPDATED = 11;
	public static final int TYPE_UNINSTALLED = 12;

	public static final String UPDATED_STRING = "Updated %s Times";
	public static final long DELAYED_TIME = 500;

	static {
		File folder = new File(APP_LOGO_PATH);
		if(!folder.exists()) {
			folder.mkdir();
		}
	}
}
