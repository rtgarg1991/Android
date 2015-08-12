package com.fireballlabs.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fireballlabs.helper.Logger;

/**
 * Created by Rohit on 7/5/2015.
 */
public class CashGuruSqliteOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_APP_INSTALL_OFFERS = "apps";
    public static final String TABLE_APP_INSTALL_PAYOUT = "app_payouts";
    public static final String TABLE_INSTALLED_APPS = "installed_apps";
    public static final String TABLE_APP_INSTALL_ATTEMPT = "install_attempt";

    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_ID = "_id";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_AFFID = "_affid";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_TITLE = "_title";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_SUB_TITLE = "_subTitle";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_AFFLINK = "_affLink";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_DESCRIPTION = "_description";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_TYPE = "_type";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_PACKAGE_NAME = "_packageName";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_IMAGE = "_image";
    public static final String TABLE_APP_INSTALL_OFFERS_COLUMN_PAYOUT = "_payout";

    public static final String TABLE_APP_INSTALL_PAYOUT_COLUMN_ID = "_id";
    public static final String TABLE_APP_INSTALL_PAYOUT_COLUMN_OFFER_AFFID = "_oid";
    public static final String TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT = "_payout";
    public static final String TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_DESCRIPTION = "_description";
    public static final String TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_TYPE = "_type";

    public static final String TABLE_INSTALLED_APPS_COLUMN_ID = "_id";
    public static final String TABLE_INSTALLED_APPS_COLUMN_PACKAGE = "_package";
    public static final String TABLE_INSTALLED_APPS_COLUMN_INSTALL_DATE = "_install_date";
    public static final String TABLE_INSTALLED_APPS_COLUMN_UNINSTALL_DATE = "_uninstall_date";

    public static final String TABLE_APP_INSTALL_ATTEMPT_COLUMN_ID = "_id";
    public static final String TABLE_APP_INSTALL_ATTEMPT_COLUMN_OFF_ID = "_oid";
    public static final String TABLE_APP_INSTALL_ATTEMPT_COLUMN_TIME = "_time";

    private static final String DATABASE_NAME = "cashon.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE_TABLE_APP_INSTALL = "create table "
            + TABLE_APP_INSTALL_OFFERS + "(" + TABLE_APP_INSTALL_OFFERS_COLUMN_ID
            + " integer primary key autoincrement, " + TABLE_APP_INSTALL_OFFERS_COLUMN_TITLE
            + " text not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_SUB_TITLE
            + " text not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_AFFID
            + " text not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_AFFLINK
            + " text not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_DESCRIPTION
            + " text not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_TYPE
            + " integer not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_PAYOUT
            + " integer not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_PACKAGE_NAME
            + " text not null, " + TABLE_APP_INSTALL_OFFERS_COLUMN_IMAGE
            + " text not null);";

    private static final String DATABASE_CREATE_TABLE_APP_INSTALL_PAYOUT = "create table "
            + TABLE_APP_INSTALL_PAYOUT + "(" + TABLE_APP_INSTALL_PAYOUT_COLUMN_ID
            + " integer primary key autoincrement, " + TABLE_APP_INSTALL_PAYOUT_COLUMN_OFFER_AFFID
            + " text not null, " + TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT
            + " integer not null, " + TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_DESCRIPTION
            + " text not null, " + TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_TYPE
            + " integer not null);";

    private static final String DATABASE_CREATE_TABLE_TABLE_INSTALLED_APPS = "create table "
            + TABLE_INSTALLED_APPS + "(" + TABLE_INSTALLED_APPS_COLUMN_ID
            + " integer primary key autoincrement, " + TABLE_INSTALLED_APPS_COLUMN_PACKAGE
            + " text not null, " + TABLE_INSTALLED_APPS_COLUMN_INSTALL_DATE
            + " text not null, " + TABLE_INSTALLED_APPS_COLUMN_UNINSTALL_DATE
            + " text);";

    private static final String DATABASE_CREATE_TABLE_APP_INSTALL_ATTEMPT = "create table "
            + TABLE_APP_INSTALL_ATTEMPT + "(" + TABLE_APP_INSTALL_ATTEMPT_COLUMN_ID
            + " integer primary key autoincrement, " + TABLE_APP_INSTALL_ATTEMPT_COLUMN_OFF_ID
            + " text not null, " + TABLE_APP_INSTALL_ATTEMPT_COLUMN_TIME
            + " text);";

    public CashGuruSqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.doSecureLogging(Log.DEBUG, CashGuruSqliteOpenHelper.class.getName() + " Database onCreate");
        db.execSQL(DATABASE_CREATE_TABLE_APP_INSTALL);
        db.execSQL(DATABASE_CREATE_TABLE_APP_INSTALL_PAYOUT);
        db.execSQL(DATABASE_CREATE_TABLE_TABLE_INSTALLED_APPS);
        db.execSQL(DATABASE_CREATE_TABLE_APP_INSTALL_ATTEMPT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.doSecureLogging(Log.DEBUG, CashGuruSqliteOpenHelper.class.getName()
                + "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_INSTALL_OFFERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_INSTALL_PAYOUT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSTALLED_APPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_INSTALL_ATTEMPT);

        onCreate(db);
    }
}
