package com.cashon.helper.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cashon.helper.Logger;
import com.cashon.sql.CashOnSqliteOpenHelper;
import com.cashon.sql.SQLWrapper;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rohit on 8/3/2015.
 */
public class UsedOffer {

    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID = "userId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID = "deviceId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID = "emailId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID = "offerId";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT = "payout";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_INSTALL_DATE = "installDate";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_UNINSTALL_DATE = "uninstallDate";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED = "converted";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CREDITED = "credited";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OUT_AFFILIATION = "ourAffiliation";
    public static final String PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME = "packageName";
    public static String PARSE_TABLE_NAME_INSTALLED_OFFERS = "InstalledOffers";

    private static long MAX_ALLOWED_TIME_SINCE_LAST_ATTEMPT = 6 * 60 * 60 * 1000;

    String emailId;
    String userId;
    String deviceId;
    String offerId;
    int payout;
    boolean converted;
    boolean credited;
    boolean ourAffiliation;
    Date installDate;
    Date uninstallDate;
    Date creditedDate;
    String packageName;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
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

    public boolean isCredited() {
        return credited;
    }

    public void setCredited(boolean credited) {
        this.credited = credited;
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

    public Date getCreditedDate() {
        return creditedDate;
    }

    public void setCreditedDate(Date creditedDate) {
        this.creditedDate = creditedDate;
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

    public static boolean addPackageToDB(Context context, String packageName, boolean needCheck) {
        SQLiteDatabase database = SQLWrapper.getWritableSqLiteDatabase(context);
        Cursor cursor = null;
        if(needCheck) {
            String whereClause = CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_PACKAGE + "=?";
            String[] whereArgs = new String[]{packageName};

            cursor = database.query(CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS,
                    new String[]{CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_ID},
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
                values.put(CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_PACKAGE, packageName);
                values.put(CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_INSTALL_DATE, currentDateAndTime);

                long id = database.insert(CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS, null, values);
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

        String whereClause = CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_PACKAGE + "=?";
        String[] whereArgs = new String[]{packageName};

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());

        ContentValues values = new ContentValues();
        values.put(CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS_COLUMN_UNINSTALL_DATE, currentDateandTime);

        // update data in DB
        int rowsUpdated = database.update(CashOnSqliteOpenHelper.TABLE_INSTALLED_APPS, values, whereClause, whereArgs);
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

            final Date date = new Date();

            final ParseUser user = ParseUser.getCurrentUser();
            if(user == null || !user.isAuthenticated()) {
                // TODO store this info somewhere that user wasn't login and app was installed
                return true;
            }
            try {
                final UsedOffer offer = Offer.getOfferData(packageName, null, context, true);
                if(offer == null) {
                    // TODO record that package not added on server
                    return true;
                }
                offer.setEmailId(user.getUsername());
                offer.setUserId(user.getObjectId());

                ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_INSTALLED_OFFERS);
                query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, offer.getEmailId());
                query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, offer.getUserId());
                query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, offer.getOfferId());

                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            if (parseObject == null) {
                                ParseObject object = new ParseObject(PARSE_TABLE_NAME_INSTALLED_OFFERS);
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, offer.getEmailId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, offer.getOfferId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT, offer.getPayout());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED, offer.isConverted());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CREDITED, offer.isCredited());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OUT_AFFILIATION, offer.isOurAffiliation());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_INSTALL_DATE, date);
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, offer.getPackageName());

                                // set public access so that referrer can access this entry
                                ParseACL groupACL = new ParseACL(user);
                                groupACL.setPublicWriteAccess(true);
                                groupACL.setPublicReadAccess(true);
                                object.setACL(groupACL);

                                object.saveEventually();
                            } else {
                                // package already installed on this phone
                                // just return
                            }
                        } else {
                            // TODO handle error
                            // lets just send our new installation to server
                            ParseObject object = new ParseObject(PARSE_TABLE_NAME_INSTALLED_OFFERS);
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, offer.getEmailId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, offer.getOfferId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT, offer.getPayout());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED, offer.isConverted());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CREDITED, offer.isCredited());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OUT_AFFILIATION, offer.isOurAffiliation());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_INSTALL_DATE, date);
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, offer.getPackageName());

                            // set public access so that referrer can access this entry
                            ParseACL groupACL = new ParseACL(user);
                            groupACL.setPublicWriteAccess(true);
                            groupACL.setPublicReadAccess(true);
                            object.setACL(groupACL);

                            object.saveEventually();
                        }
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
                return true;
            }
        }
        return false;
    }

    public static boolean checkAndRemovePackageOnCloud(Context context, String packageName) {
        final ParseUser user = ParseUser.getCurrentUser();
        if(packageName != null && user != null && user.isAuthenticated()) {

            final UsedOffer offer;
            try {
                final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                final Date date = new Date();

                offer = Offer.getOfferData(packageName, null, context, false);

                if(offer == null) {
                    // TODO record that package not added on server
                    return true;
                }

                offer.setEmailId(user.getUsername());
                offer.setUserId(user.getObjectId());

                ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_INSTALLED_OFFERS);
                query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, packageName);
                query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, user.getUsername());

                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if(e == null) {
                            if(parseObject == null) {
                                // no object to update, but lets add uninstall info on server
                                ParseObject object = new ParseObject(PARSE_TABLE_NAME_INSTALLED_OFFERS);
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, offer.getEmailId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, offer.getOfferId());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT, offer.getPayout());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED, offer.isConverted());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CREDITED, offer.isCredited());
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_UNINSTALL_DATE, date);
                                object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, offer.getPackageName());

                                // set public access so that referrer can access this entry
                                ParseACL groupACL = new ParseACL(user);
                                groupACL.setPublicWriteAccess(true);
                                groupACL.setPublicReadAccess(true);
                                object.setACL(groupACL);

                                object.saveEventually();
                            } else {
                                parseObject.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_UNINSTALL_DATE, new Date());
                                parseObject.saveEventually();
                            }
                        } else {
                            // TODO handle error
                            // no object to update, but lets add uninstall info on server
                            ParseObject object = new ParseObject(PARSE_TABLE_NAME_INSTALLED_OFFERS);
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, offer.getEmailId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, offer.getOfferId());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT, offer.getPayout());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED, offer.isConverted());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CREDITED, offer.isCredited());
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_UNINSTALL_DATE, date);
                            object.put(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PACKAGE_NAME, offer.getPackageName());

                            // set public access so that referrer can access this entry
                            ParseACL groupACL = new ParseACL(user);
                            groupACL.setPublicWriteAccess(true);
                            groupACL.setPublicReadAccess(true);
                            object.setACL(groupACL);

                            object.saveEventually();
                        }
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
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
        values.put(CashOnSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_OFF_ID, uniqueClick);
        values.put(CashOnSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_TIME , currentDateAndTime);

        long id = database.insert(CashOnSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT, null, values);
        if(id == -1) {
            Logger.doSecureLogging(Log.WARN, "Some problem Occured while adding new install attempt entry : ");
        } else {
            Logger.doSecureLogging(Log.INFO, uniqueClick + "Install Attempt added in DB");
        }
        values.clear();
        database.close();
    }

    public static boolean checkIfThisOfferUserHasAttempted(String offerId, Context context) {
        SQLiteDatabase database = SQLWrapper.getWritableSqLiteDatabase(context);

        String whereClause = CashOnSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_OFF_ID + "=?";
        String[] whereArgs = new String[]{offerId};

        Date date = new Date();
        long currentTime = date.getTime();

        // update data in DB
        Cursor cursor = database.query(CashOnSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT, new String[] {CashOnSqliteOpenHelper.TABLE_APP_INSTALL_ATTEMPT_COLUMN_TIME},
                whereClause, whereArgs, null, null, null);
        if(cursor == null) {
            Logger.doSecureLogging(Log.INFO, "Error occurred while getting app attempt data from db");
            database.close();
            return false;
        } else if(cursor.getCount() == 0) {
            Logger.doSecureLogging(Log.INFO, "No install attempt data in db for offer : " + offerId);
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

    public static List<Offer> getPendingInstallOffers() throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return null;
        }

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_INSTALLED_OFFERS);
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED, false);
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, user.getUsername());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
        List<ParseObject> list = query.find();

        List<Offer> offers = new ArrayList<Offer>();

        for(ParseObject obj : list) {
            UsedOffer usedOffer = new UsedOffer();
            usedOffer.setOfferId(obj.getString(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID));

            Offer offer = new Offer();
            offer.setPayout(obj.getInt(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT));

            ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Offer.PARSE_TABLE_NAME_OFFERS);
            ParseObject obj2 = query2.get(usedOffer.getOfferId().substring(0, usedOffer.getOfferId().indexOf("_")));

            if(obj2 == null) {
                continue;
            }

            offer.setId(obj2.getObjectId());
            offer.setImageName(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_IMAGE_NAME));
            offer.setPackageName(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_PACKAGE_NAME));
            offer.setAffLink(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_AFF_LINK));
            offer.setTitle(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_TITLE));
            offer.setSubTitle(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_SUB_TITLE));
            offer.setDescription(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_DESCRIPTION));
            offer.setType(obj2.getInt(Offer.PARSE_TABLE_OFFERS_COLUMN_TYPE));

            offers.add(offer);

        }

        return offers;
    }

    public static List<Offer> getCompletedInstallOffers() throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return null;
        }

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_INSTALLED_OFFERS);
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_CONVERTED, true);
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, user.getUsername());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
        List<ParseObject> list = query.find();

        List<Offer> offers = new ArrayList<Offer>();

        for(ParseObject obj : list) {
            UsedOffer usedOffer = new UsedOffer();
            usedOffer.setOfferId(obj.getString(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID));

            Offer offer = new Offer();
            offer.setPayout(obj.getInt(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_PAYOUT));

            ParseQuery<ParseObject> query2 = ParseQuery.getQuery(Offer.PARSE_TABLE_NAME_OFFERS);
            ParseObject obj2 = query2.get(usedOffer.getOfferId().substring(0, usedOffer.getOfferId().indexOf("_")));

            if(obj2 == null) {
                continue;
            }

            offer.setId(obj2.getObjectId());
            offer.setImageName(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_IMAGE_NAME));
            offer.setPackageName(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_PACKAGE_NAME));
            offer.setAffLink(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_AFF_LINK));
            offer.setTitle(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_TITLE));
            offer.setSubTitle(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_SUB_TITLE));
            offer.setDescription(obj2.getString(Offer.PARSE_TABLE_OFFERS_COLUMN_DESCRIPTION));
            offer.setType(obj2.getInt(Offer.PARSE_TABLE_OFFERS_COLUMN_TYPE));

            offers.add(offer);

        }

        return offers;
    }

    public static boolean checkIfUserHasUsedThisOffer(Offer offer) {
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return false;
        }

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_INSTALLED_OFFERS);
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_EMAIL_ID, user.getUsername());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_USER_ID, user.getObjectId());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_DEVICE_ID, installation.getObjectId());
        query.whereEqualTo(PARSE_TABLE_INSTALLED_OFFERS_COLUMN_OFFER_ID, offer.getId() + "_" + offer.getType());
        try {
            List<ParseObject> list = query.find();
            if(list.size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
