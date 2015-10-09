package net.fireballlabs.helper.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.sql.CashGuruSqliteOpenHelper;
import net.fireballlabs.sql.SQLWrapper;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rohit on 8/3/2015.
 */
public class UsedOffer {

    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID = "userId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID = "deviceId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID = "offerId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT = "payout";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_INSTALL_DATE = "installDate";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_UNINSTALL_DATE = "uninstallDate";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED = "converted";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OUR_AFFILIATION = "ourAffiliation";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME = "package";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_TYPE_ID = "offerType";
    public static String PARSE_TABLE_NAME_INSTALLS = "Installs";

    private static long MAX_ALLOWED_TIME_SINCE_LAST_ATTEMPT = 6 * 60 * 60 * 1000;
    private static ArrayList<String> completedOffers;
    private static ArrayList<String> pendingOffers;
    private static ArrayList<String> availableOffers;

    String userId;
    String deviceId;
    String offerId;
    int payout;
    int offerType;
    boolean converted;
    boolean ourAffiliation;
    Date installDate;
    Date uninstallDate;
    String packageName;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public int getPayout() {
        return payout;
    }

    public void setPayout(int payout) {
        this.payout = payout;
    }

    public boolean isConverted() {
        return converted;
    }

    public void setConverted(boolean converted) {
        this.converted = converted;
    }

    public Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(Date installDate) {
        this.installDate = installDate;
    }

    public Date getUninstallDate() {
        return uninstallDate;
    }

    public void setUninstallDate(Date uninstallDate) {
        this.uninstallDate = uninstallDate;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isOurAffiliation() {
        return ourAffiliation;
    }

    public void setOurAffiliation(boolean ourAffiliation) {
        this.ourAffiliation = ourAffiliation;
    }

    public int getOfferType() {
        return offerType;
    }

    public void setOfferType(int offerType) {
        this.offerType = offerType;
    }

    public static boolean addPackageToDB(Context context, String packageName, boolean needCheck) {
        SQLiteDatabase database = SQLWrapper.getWritableSqLiteDatabase(context);
        Cursor cursor = null;
        if(needCheck) {
            String whereClause = CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_PACKAGE + "=?";
            String[] whereArgs = new String[]{packageName};

            cursor = database.query(CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS,
                    new String[]{CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_ID},
                    whereClause, whereArgs, null, null, null);
        }

        if(cursor == null || cursor.getCount() == 0 || !needCheck) {
            if(cursor != null || !needCheck) {
                if(cursor != null) {
                    cursor.close();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateAndTime = sdf.format(new Date());
                ContentValues values = new ContentValues();
                values.put(CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_PACKAGE, packageName);
                values.put(CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_INSTALL_DATE, currentDateAndTime);

                long id = database.insert(CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS, null, values);
                if(id == -1) {
                    Logger.doSecureLogging(Log.WARN, "Some problem Occurred while adding new package data with package name : " + packageName);
                } else {
                    Logger.doSecureLogging(Log.INFO, "Package " + packageName + " added in DB");
                }
                values.clear();
                database.close();
                return true;
            }
        } else {
            Logger.doSecureLogging(Log.INFO, "Application with same package and uid already present, package = " + packageName);
            cursor.close();
            database.close();
        }

        return false;
    }

    public static boolean deletePackageUpdateDB(Context context, String packageName) {
        SQLiteDatabase database = SQLWrapper.getWritableSqLiteDatabase(context);

        String whereClause = CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_PACKAGE + "=?";
        String[] whereArgs = new String[]{packageName};

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        ContentValues values = new ContentValues();
        values.put(CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_UNINSTALL_DATE, currentDateAndTime);

        // update data in DB
        int rowsUpdated = database.update(CashGuruSqliteOpenHelper.TABLE_INSTALLED_APPS, values, whereClause, whereArgs);
        if(rowsUpdated == 0) {
            Logger.doSecureLogging(Log.INFO, "Package " + packageName + " uninstalled and it wasn't available in our DB");
            database.close();
            return false;
        }
        database.close();
        return true;
    }


    public static boolean checkAndAddPackageOnCloud(Context context, final String packageName) {
        // lets first check if this package is affiliated by us
        final float payout = Offer.checkIfOffer(context, packageName);
        // if payout is -1, then we don't affiliate this package
        if(payout != -1) {
            final ParseInstallation installation = ParseInstallation.getCurrentInstallation();

            final ParseUser user = ParseUser.getCurrentUser();
            if(user == null || !user.isAuthenticated() || installation == null) {
                // TODO store this info somewhere that user wasn't login and app was installed
                return true;
            }
            try {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, packageName);
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OUR_AFFILIATION, UsedOffer.checkIfThisOfferUserHasAttempted(packageName, context));
                params.put("isInstallation", true);
                ParseCloud.callFunction(ParseConstants.FUNCTION_ADD_UPDATE_INSTALL, params);
            } catch (ParseException e) {
                Crashlytics.logException(e);
                return true;
            }
        }
        return false;
    }

    public static boolean checkAndRemovePackageOnCloud(Context context, String packageName) {
        final ParseUser user = ParseUser.getCurrentUser();
        final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if(packageName != null && user != null && user.isAuthenticated() && installation != null) {
            try {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
                params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, packageName);
                params.put("isInstallation", false);

                ParseCloud.callFunction(ParseConstants.FUNCTION_ADD_UPDATE_INSTALL, params);
            } catch (ParseException e) {
                Crashlytics.logException(e);
                return true;
            }
        }
        return false;
    }

    public static void recordInstallAttempt(String uniqueClick, Context context) {
        SQLiteDatabase database = SQLWrapper.getWritableSqLiteDatabase(context);

        Date date = new Date();
        String currentDateAndTime = String.valueOf(date.getTime());
        ContentValues values = new ContentValues();
        values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_OFF_ID, uniqueClick);
        values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_TIME , currentDateAndTime);

        long id = database.insert(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT, null, values);
        if(id == -1) {
            Logger.doSecureLogging(Log.WARN, "Some problem Occurred while adding new install attempt entry : ");
        } else {
            Logger.doSecureLogging(Log.INFO, uniqueClick + "Install Attempt added in DB");
        }
        values.clear();
        database.close();
    }

    public static boolean checkIfThisOfferUserHasAttempted(String packageName, Context context) {
        SQLiteDatabase database = SQLWrapper.getWritableSqLiteDatabase(context);

        String whereClause = CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_OFF_ID + "=?";
        String[] whereArgs = new String[]{packageName};

        Date date = new Date();
        long currentTime = date.getTime();

        // update data in DB
        Cursor cursor = database.query(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT, new String[] {CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_TIME},
                whereClause, whereArgs, null, null, null);
        if(cursor == null) {
            Logger.doSecureLogging(Log.INFO, "Error occurred while getting app attempt data from db");
            database.close();
            return false;
        } else if(cursor.getCount() == 0) {
            Logger.doSecureLogging(Log.INFO, "No install attempt data in db for offer : " + packageName);
            database.close();
            return false;
        } else {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {
                long installAttemptTime = Long.valueOf(cursor.getString(0));
                if (currentTime - installAttemptTime > MAX_ALLOWED_TIME_SINCE_LAST_ATTEMPT) {
                    cursor.moveToNext();
                } else {
                    Logger.doSecureLogging(Log.INFO, "Last install attempt was within 6 hours");
                    break;
                }
            }
            if(!cursor.isAfterLast()) {
                cursor.close();
                database.close();
                return true;
            } else {
                cursor.close();
                database.close();
                return false;
            }
        }
    }

    public static List<String> getAvailableOffers(Context context) throws ParseException {
        if(availableOffers == null) {
            synchronized (UsedOffer.class) {
                if(availableOffers == null) {
                    getInstallDataFromParse(ParseUser.getCurrentUser(), context);
                }
            }
        }
        return availableOffers;
    }

    public static List<String> getPendingOffers(Context context) throws ParseException {
        if(pendingOffers == null) {
            synchronized (UsedOffer.class) {
                if(pendingOffers == null) {
                    getInstallDataFromParse(ParseUser.getCurrentUser(), context);
                }
            }
        }
        return pendingOffers;
    }

    public static List<String> getCompletedOffers(Context context) throws ParseException {
        if(completedOffers == null) {
            synchronized (UsedOffer.class) {
                if(completedOffers == null) {
                    getInstallDataFromParse(ParseUser.getCurrentUser(), context);
                }
            }
        }
        return completedOffers;
    }

    private static void getInstallDataFromParse(ParseUser user, Context context) throws ParseException {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
        params.put(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
        ArrayList<HashMap<String, Object>> convertedOffers = ParseCloud.callFunction(ParseConstants.FUNCTION_GET_INSTALLED_OFFERS, params);

        List<Offer> offers = Offer.getAllOffers(context);
        for(HashMap<String, Object> map : convertedOffers) {
            String offerId = (String)map.get(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID);
            int typeId = (Integer)map.get(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_TYPE_ID);
            boolean converted = (Boolean)map.get(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED);
            boolean ourAffiliation = (Boolean)map.get(UsedOffer.PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OUR_AFFILIATION);

            Offer offer = Offer.findOffer(offers, offerId);
            if(offer == null) {
                PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_CLOUD_DATA_CHANGED, Context.MODE_PRIVATE, true);
                offers = Offer.getAllOffers(context);
                offer = Offer.findOffer(offers, offerId);
                PreferenceManager.setDefaultSharedPreferenceValue(context, Constants.PREF_CLOUD_DATA_CHANGED, Context.MODE_PRIVATE, false);
            }
            if (!ourAffiliation) {
                offer.setIsAvailable(false);
            } else {
                offer.setPayoutConverted(typeId, converted);
            }
        }
        clearSavedData();
        for(Offer offer : offers) {
            if(offer.isAvailable()) {
                if(offer.isConverted()) {
                    initializeSavedData();
                    completedOffers.add(offer.getId());
                } else if(!offer.isInstallAvailable()) {
                    initializeSavedData();
                    pendingOffers.add(offer.getId());
                } else {
                    initializeSavedData();

                    // check if it is already installed in the device

                    // get package manager for querying all data
                    PackageManager pm = context.getPackageManager();
                    // get all installed applications
                    try {
                        PackageInfo info= pm.getPackageInfo(offer.getPackageName(), PackageManager.GET_META_DATA);
                        if(info != null) {
                            continue;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        // No need
                    }
                    availableOffers.add(offer.getId());
                }
            }
        }
    }

    private static void initializeSavedData() {

        if(completedOffers == null) {
            completedOffers = new ArrayList<String>();
        }
        if(pendingOffers == null) {
            pendingOffers = new ArrayList<String>();
        }
        if(availableOffers == null) {
            availableOffers = new ArrayList<String>();
        }
    }

    public static void clearSavedData() {
        if(availableOffers != null) {
            availableOffers.clear();
            availableOffers = null;
        }
        if(pendingOffers != null) {
            pendingOffers.clear();
            pendingOffers = null;
        }
        if(completedOffers != null) {
            completedOffers.clear();
            completedOffers = null;
        }
    }
}
