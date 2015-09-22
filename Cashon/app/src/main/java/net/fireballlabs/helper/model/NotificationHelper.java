package net.fireballlabs.helper.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.fireballlabs.helper.Logger;
import net.fireballlabs.sql.CashGuruSqliteOpenHelper;
import net.fireballlabs.sql.SQLWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohitgarg on 9/18/15.
 */
public class NotificationHelper {

    public static class Notification {
        int code;
        int amount;
        String message;
        String extra;

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private static List<Notification> notifications;

    public static List<Notification> getNotifications() {
        return notifications;
    }

    public static List<Notification> getNotifications(Context context) {
        if(notifications != null) {
            return notifications;
        } else {
            notifications = new ArrayList<Notification>();
        }


        SQLiteDatabase db = SQLWrapper.getReadableSqLiteDatabase(context);
        if(db != null) {
            Cursor cursor = db.query(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS, null, null, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                // TODO do Parse error reporting
                Logger.doSecureLogging(Log.WARN, NotificationHelper.class.getName()
                        + " An Error occurred while retrieving data from db");
                if(cursor != null) {
                    cursor.close();
                }
            } else {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Notification notification = new Notification();
                    notification.setAmount(cursor.getInt(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_AMOUNT)));
                    notification.setCode(cursor.getInt(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_CODE)));
                    notification.setExtra(cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_EXTRA)));
                    notification.setMessage(cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_MESSAGE)));
                    notifications.add(notification);
                    cursor.moveToNext();
                }
                cursor.close();
            }
            db.close();
        }
        return notifications;
    }

    public static void addNotification(Context context, int amount, int code, String extra, String message, String refId) {
        SQLiteDatabase db = SQLWrapper.getWritableSqLiteDatabase(context);
        if(db != null) {
            String whereClause = CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_REF_ID + "=?";
            String[] whereArgs = new String[]{refId};
            Cursor cursor = null;
            if(refId != null && !"".equals(refId)) {
                cursor = db.query(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS, null, whereClause, whereArgs, null, null, null);
            }
            if(cursor == null || cursor.getCount() == 0) {
                if(cursor != null) {
                    cursor.close();
                }

                ContentValues values = new ContentValues();
                values.put(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_AMOUNT, amount);
                values.put(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_CODE, code);
                values.put(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_EXTRA, extra);
                values.put(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_MESSAGE, message);
                values.put(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS_COLUMN_REF_ID, refId);
                long id = db.insert(CashGuruSqliteOpenHelper.TABLE_NOTIFICATIONS, null, values);
                if(id == -1) {
                    // TODO do Parse error reporting
                    Logger.doSecureLogging(Log.WARN, NotificationHelper.class.getName()
                            + " An Error occured while adding Offer data for id = " + id);
                } else {
                    if(notifications != null) {
                        Notification notification = new Notification();
                        notification.setExtra(extra);
                        notification.setMessage(message);
                        notification.setCode(code);
                        notification.setAmount(amount);
                        notifications.add(notification);
                    }
                }
            } else {
                cursor.close();
            }
            db.close();
        }
    }
}