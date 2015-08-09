package com.cashon.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cashon.helper.Logger;
import com.cashon.impl.Utility;
import com.cashon.ui.AppInstallsFragment;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rohit on 7/12/2015.
 */
public class SQLWrapper {
    public static class Offer {
        public String name;
        public String id;
        @SerializedName("afflink")
        public String affLink;
        public String description;
        public String type;
        public String packageName;
        public Images images;
        public List<Payout> payouts;

        public static void clearCurrentDataFromDB(Context context) {

            SQLiteDatabase db = getWritableSqLiteDatabase(context);
            if(db != null) {
                db.delete(CashOnSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS, null, null);
                db.delete(CashOnSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT, null, null);
            } else {
                // TODO do Parse error reporting
                Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                        + " An Error occured while retrieving Writable Database");
            }
        }

        public static class Images {
            public String ldpi;
            public String mdpi;
            public String hdpi;
            public String xhdpi;
        }
        public static class Payout {
            public String description;
            public String payout;
            public String currency;
            public String code;
            public String days;
        }

    }

    public static SQLiteDatabase getReadableSqLiteDatabase(Context context) {
        CashOnSqliteOpenHelper sqlHelper = new CashOnSqliteOpenHelper(context);
        return sqlHelper.getReadableDatabase();
    }

    public static SQLiteDatabase getWritableSqLiteDatabase(Context context) {
        CashOnSqliteOpenHelper sqlHelper = new CashOnSqliteOpenHelper(context);
        return sqlHelper.getWritableDatabase();
    }

}
