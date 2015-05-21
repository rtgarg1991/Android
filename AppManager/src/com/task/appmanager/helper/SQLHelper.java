package com.task.appmanager.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

	// Table Create statement for APPS table
	public static final String CREATE_APPS_TABLE = "CREATE TABLE " + Constants.TABLE_APPS
			+ "(" + Constants.TABLE_APPS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ Constants.TABLE_APPS_COLUMN_APP_UID + " INTEGER NOT NULL, "
			+ Constants.TABLE_APPS_COLUMN_PACKAGE + " TEXT NOT NULL, "
			+ Constants.TABLE_APPS_COLUMN_APP_NAME + " TEXT, "
			+ Constants.TABLE_APPS_COLUMN_LOGO_NAME + " TEXT, "
			+ Constants.TABLE_APPS_COLUMN_AVAILABLE + " INTEGER NOT NULL, "
			+ Constants.TABLE_APPS_COLUMN_LAST_ACCESSED + " String);";

	// Table Create statement for UPDATES table
	public static final String CREATE_UPDATES_TABLE = "CREATE TABLE " + Constants.TABLE_UPDATES
			+ "(" + Constants.TABLE_UPDATES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ Constants.TABLE_UPDATES_COLUMN_APP_ID + " INTEGER NOT NULL, "
			+ Constants.TABLE_UPDATES_COLUMN_DATE + " TEXT);";

	// Constructor requiring only context
	public SQLHelper(Context context) {
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if(Constants.DEBUG) Logger.logDebug("DB ONCREATE");

		// create both of the tables
		db.execSQL(CREATE_APPS_TABLE);
		db.execSQL(CREATE_UPDATES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(Constants.DEBUG) Logger.logDebug("DB ONUPGRADE");

		// Drop Table
		// Only for Testing purpose
		// No need in shipped version
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_APPS);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_UPDATES);

		onCreate(db);
	}

}
