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
