package net.fireballlabs.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.fireballlabs.helper.Logger;
import net.fireballlabs.ui.AppInstallsFragment;
import com.google.gson.annotations.SerializedName;

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
                db.delete(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS, null, null);
                db.delete(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT, null, null);
            } else {
                // TODO do Parse error reporting
                Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                        + " An Error occured while retrieving Writable Database");
            }
            db.close();
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
        CashGuruSqliteOpenHelper sqlHelper = new CashGuruSqliteOpenHelper(context);
        return sqlHelper.getReadableDatabase();
    }

    public static SQLiteDatabase getWritableSqLiteDatabase(Context context) {
        CashGuruSqliteOpenHelper sqlHelper = new CashGuruSqliteOpenHelper(context);
        return sqlHelper.getWritableDatabase();
    }

}
