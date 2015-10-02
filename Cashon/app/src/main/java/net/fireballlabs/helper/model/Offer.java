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
import net.fireballlabs.ui.AppInstallsFragment;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rohit on 8/3/2015.
 */
public class Offer {
    private static List<Offer> offers;
    String id;
    String imageName;
    String packageName;
    public String affLink;
    public String title;
    public String subTitle;
    public String description;
    public int payout;
    public int type;

    boolean isAvailable;
    public List<Payout> payouts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAffLink() {
        return affLink;
    }

    public void setAffLink(String affLink) {
        this.affLink = affLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPayout() {
        return payout;
    }

    public void setPayout(int payout) {
        this.payout = payout;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Payout> getPayouts() {
        return payouts;
    }

    public void setPayouts(List<Payout> payouts) {
        this.payouts = payouts;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public static Offer findOffer(List<Offer> offers, String offerId) {
        if(offers == null || offerId == null) {
            return null;
        }
        for(Offer offer : offers) {
            if(offer.getId().equals(offerId)) {
                return offer;
            }
        }
        return null;
    }

    public Payout getPayout(int typeId) {
        for(Payout p : getPayouts()) {
            if(p.getOfferType() == typeId) {
                return p;
            }
        }
        return null;
    }

    public void setPayoutConverted(int typeId, boolean converted) {
        Payout p = getPayout(typeId);
        if(p != null) {
            p.setConverted(converted);
        }
    }

    public boolean isConverted() {
        for(Payout p : payouts) {
            if(!p.isConverted()) {
                return false;
            }
        }
        return true;
    }

    public boolean isInstallAvailable() {
        for(Payout p : payouts) {
            if(p.isConverted()) {
                return false;
            }
        }
        return true;
    }

    public static Offer getOffer(String s) {
        for(Offer offer : offers) {
            if(offer.id.equals(s)) {
                return offer;
            }
        }
        return null;
    }

    public class Payout {
        int offerType;
        int payout;
        String description;
        Object extraInfo;
        boolean converted;
        boolean installed;

        public boolean isInstalled() {
            return installed;
        }

        public void setInstalled(boolean installed) {
            this.installed = installed;
        }

        public boolean isConverted() {
            return converted;
        }

        public void setConverted(boolean converted) {
            this.converted = converted;
        }

        public int getOfferType() {
            return offerType;
        }

        public void setOfferType(int offerType) {
            this.offerType = offerType;
        }

        public int getPayout() {
            return payout;
        }

        public void setPayout(int payout) {
            this.payout = payout;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Object getExtraInfo() {
            return extraInfo;
        }

        public void setExtraInfo(Object extraInfo) {
            this.extraInfo = extraInfo;
        }

    }

    public static String IMAGE_SERVER_URL = "http://cashguru.fireballlabs.net/app_images/";

    public static int OFFER_TYPE_INSTALL = 1;
    public static int OFFER_TYPE_LEAD = 2;
    public static int OFFER_TYPE_PROMO = 3;
    public static int OFFER_TYPE_HOT = 4;
    public static int OFFER_TYPE_PAY_BUMP = 5;

    public static int OFFER_SUB_TYPE_INSTALL = 1;
    public static int OFFER_SUB_TYPE_KEEP = 2;

    public static String PARSE_TABLE_NAME_OFFERS = "Offers";
    public static String PARSE_TABLE_NAME_PAYOUT = "Payout";

    public static String PARSE_TABLE_OFFERS_COLUMN_ID = "id";
    public static String PARSE_TABLE_OFFERS_COLUMN_IMAGE_NAME = "imageName";
    public static String PARSE_TABLE_OFFERS_COLUMN_PACKAGE_NAME = "packageName";
    public static String PARSE_TABLE_OFFERS_COLUMN_AFF_LINK = "affLink";
    public static String PARSE_TABLE_OFFERS_COLUMN_TITLE = "title";
    public static String PARSE_TABLE_OFFERS_COLUMN_SUB_TITLE = "subTitle";
    public static String PARSE_TABLE_OFFERS_COLUMN_DESCRIPTION = "description";
    public static String PARSE_TABLE_OFFERS_COLUMN_PAYOUT = "payout";
    public static String PARSE_TABLE_OFFERS_COLUMN_TYPE = "type";
    public static String PARSE_TABLE_OFFERS_COLUMN_IS_AVAILABLE = "isAvailable";
    public static String PARSE_TABLE_OFFERS_COLUMN_CLOUD_SUB_OFFERS = "subOffers";

    public static String PARSE_TABLE_PAYOUT_COLUMN_OFFER_ID = "offerId";
    public static String PARSE_TABLE_PAYOUT_COLUMN_OFFER_TYPE = "offerType";
    public static String PARSE_TABLE_PAYOUT_COLUMN_DESCRIPTION = "description";
    public static String PARSE_TABLE_PAYOUT_COLUMN_OFFER_PAYOUT = "payout";
    // TODO
    /* Category*/

    synchronized public static List<Offer> getAllOffers(Context context) throws ParseException {

        // old approach, no cloud function usage
        /*ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_OFFERS);
        query.whereEqualTo(PARSE_TABLE_OFFERS_COLUMN_IS_AVAILABLE, true);
        List<ParseObject> list = query.find();

        List<Offer> offers = new ArrayList<Offer>();
        for(ParseObject obj : list) {
            Offer offer = new Offer();
            offer.setId(obj.getObjectId());
            offer.setImageName(obj.getString(PARSE_TABLE_OFFERS_COLUMN_IMAGE_NAME));
            offer.setPackageName(obj.getString(PARSE_TABLE_OFFERS_COLUMN_PACKAGE_NAME));
            offer.setAffLink(obj.getString(PARSE_TABLE_OFFERS_COLUMN_AFF_LINK));
            offer.setTitle(obj.getString(PARSE_TABLE_OFFERS_COLUMN_TITLE));
            offer.setSubTitle(obj.getString(PARSE_TABLE_OFFERS_COLUMN_SUB_TITLE));
            offer.setDescription(obj.getString(PARSE_TABLE_OFFERS_COLUMN_DESCRIPTION));
            offer.setPayout(obj.getInt(PARSE_TABLE_OFFERS_COLUMN_PAYOUT));
            offer.setType(obj.getInt(PARSE_TABLE_OFFERS_COLUMN_TYPE));
            offer.setIsAvailable(obj.getBoolean(PARSE_TABLE_OFFERS_COLUMN_IS_AVAILABLE));

            offer.payouts = new ArrayList<Payout>();

            ParseQuery<ParseObject> queryPayout = ParseQuery.getQuery(PARSE_TABLE_NAME_PAYOUT);
            queryPayout.whereEqualTo(PARSE_TABLE_PAYOUT_COLUMN_OFFER_ID, offer.getId());
            List<ParseObject> listPayouts = queryPayout.find();

            for(ParseObject payout : listPayouts) {
                Payout p = offer.new Payout();
                offer.payouts.add(p);
                p.setOfferType(payout.getInt(PARSE_TABLE_PAYOUT_COLUMN_OFFER_TYPE));
                p.setDescription(payout.getString(PARSE_TABLE_PAYOUT_COLUMN_DESCRIPTION));
                p.setPayout(payout.getInt(PARSE_TABLE_PAYOUT_COLUMN_OFFER_PAYOUT));
            }
            PackageManager pm = context.getPackageManager();
            // get all installed applications
            try {
                PackageInfo info= pm.getPackageInfo(offer.getPackageName(), PackageManager.GET_META_DATA);
                if(info == null && offer.isAvailable()) {
                    checkAndAddOffer(offers, offer);
                }
            } catch (PackageManager.NameNotFoundException e) {
                if(offer.isAvailable()) {
                    checkAndAddOffer(offers, offer);
                }
            }
        }*/
        // if we already have offers, then lets sync from it
        if(offers != null || context == null) {
            if(!PreferenceManager.getDefaultSharedPreferenceValue(context, Constants.PREF_CLOUD_DATA_CHANGED, Context.MODE_PRIVATE, true)) {
                return offers;
            }
        } else if(!PreferenceManager.getDefaultSharedPreferenceValue(context, Constants.PREF_CLOUD_DATA_CHANGED, Context.MODE_PRIVATE, true)) {
            List<Offer> offers = getData(context);
            if(offers != null) {
                return offers;
            }
        }

        // lets try cloud function to retrieve data
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return null;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        ArrayList<HashMap<String, Object>> cloudOffers = ParseCloud.callFunction(ParseConstants.FUNCTION_GET_ALL_OFFERS, params);
        List<Offer> offers = new ArrayList<Offer>();
        for(int i = 0; i < cloudOffers.size(); i++) {
            Offer offer = new Offer();
            offer.setId((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_ID));
            offer.setImageName((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_IMAGE_NAME));
            offer.setPackageName((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_PACKAGE_NAME));
            offer.setAffLink((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_AFF_LINK));
            offer.setTitle((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_TITLE));
            offer.setSubTitle((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_SUB_TITLE));
            offer.setDescription((String) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_DESCRIPTION));
            offer.setPayout((Integer) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_PAYOUT));
            offer.setType((Integer) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_TYPE));
            offer.setIsAvailable(true);
            ArrayList<HashMap<String, Object>> subOffers = (ArrayList<HashMap<String, Object>>) cloudOffers.get(i).get(PARSE_TABLE_OFFERS_COLUMN_CLOUD_SUB_OFFERS);
            for(int j = 0; j < subOffers.size(); j++) {
                Payout p = offer.new Payout();
                p.setOfferType((Integer) subOffers.get(j).get(PARSE_TABLE_PAYOUT_COLUMN_OFFER_TYPE));
                p.setDescription((String)subOffers.get(j).get(PARSE_TABLE_PAYOUT_COLUMN_DESCRIPTION));
                p.setPayout((Integer)subOffers.get(j).get(PARSE_TABLE_PAYOUT_COLUMN_OFFER_PAYOUT));

                if(offer.payouts == null) {
                    offer.payouts = new ArrayList<Payout>();
                }
                offer.payouts.add(p);
            }
            if(offer.isAvailable()) {
                offers.add(offer);
            }
        }

        Offer.offers = offers;

        for(Offer offer :offers) {
            offer.saveData(context);
        }

        return offers;
    }


    public void saveData(Context context) {
        clearCurrentDataFromDB(context);
        SQLiteDatabase db = SQLWrapper.getWritableSqLiteDatabase(context);
        if(db != null) {
            ContentValues values = new ContentValues();
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_AFFID, id);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_IMAGE, imageName);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_PACKAGE_NAME, packageName);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_AFFLINK, affLink);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_TITLE, title);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_SUB_TITLE, subTitle);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_DESCRIPTION, description);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_PAYOUT, payout);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_TYPE, type);
            values.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_IS_AVAILABLE, isAvailable ? 1 : 0);
            long id1 = db.insert(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS, null, values);
            if(id1 == -1) {
                // TODO do Parse error reporting
                Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                        + " An Error occured while adding Offer data for id = " + id);
            } else {
                ContentValues values2;
                for (Payout payout :
                        payouts) {
                    values2 = new ContentValues();
                    values2.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_OFFER_AFFID, id);
                    values2.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT, payout.payout);
                    values2.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_TYPE, payout.offerType);
                    values2.put(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_DESCRIPTION, payout.description);
                    long id2 = db.insert(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT, null, values2);
                    if(id2 == -1) {
                        // TODO do Parse error reporting
                        Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                                + " An Error occured while adding Offer Payout data for payout id = " + payout.offerType);
                    }
                }
            }
            db.close();
        } else {
            // TODO do Parse error reporting
            Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                    + " An Error occured while retrieving Writable Database");
        }
    }

    public static List<Offer> getData(Context context) {
        if(offers != null) {
            return offers;
        }
        SQLiteDatabase db = SQLWrapper.getReadableSqLiteDatabase(context);
        if(db != null) {
            Cursor cursor = db.query(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS, null, null, null, null, null, null);
            if(cursor == null || cursor.getCount() == 0) {
                // TODO do Parse error reporting
                Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                        + " An Error occurred while retrieving data from db");
            } else {
                List<Offer> offers = new ArrayList<Offer>();
                cursor.moveToFirst();
                while(!cursor.isAfterLast()) {
                    Offer offer = new Offer();
                    offer.id = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_AFFID));
                    offer.imageName = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_IMAGE));
                    offer.packageName = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_PACKAGE_NAME));
                    offer.affLink = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_AFFLINK));
                    offer.title = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_TITLE));
                    offer.subTitle = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_SUB_TITLE));
                    offer.description = cursor.getString(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_DESCRIPTION));
                    offer.payout = cursor.getInt(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_PAYOUT));
                    offer.type = cursor.getInt(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_TYPE));
                    offer.isAvailable = cursor.getInt(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_IS_AVAILABLE)) == 1 ? true : false;

                    int dbId = cursor.getInt(cursor.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_AFFID));
                    String whereClause = CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_OFFER_AFFID + " = ?";
                    String[] whereArgs = new String[] {
                            String.valueOf(offer.id)
                    };
                    Cursor cursor2 = db.query(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT, null, whereClause, whereArgs, null, null, null);
                    if(cursor2 == null || cursor2.getCount() == 0) {
                        // TODO do Parse error reporting
                        Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                                + " An Error occurred while retrieving data from db for offer with id = " + offer.id);
                    } else {
                        cursor2.moveToFirst();
                        while(!cursor2.isAfterLast()) {
                            if(offer.payouts == null) {
                                offer.payouts = new ArrayList<Payout>();
                            }
                            Payout payout = offer.new Payout();
                            payout.payout = cursor2.getInt(cursor2.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT));
                            payout.offerType = cursor2.getInt(cursor2.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_TYPE));
                            payout.description = cursor2.getString(cursor2.getColumnIndex(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT_DESCRIPTION));
                            offer.payouts.add(payout);
                            cursor2.moveToNext();
                        }
                        cursor2.close();
                    }
                    PackageManager pm = context.getPackageManager();
                    // get all installed applications
                    try {
                        PackageInfo info= pm.getPackageInfo(offer.getPackageName(), PackageManager.GET_META_DATA);
                        if(info == null && offer.isAvailable()) {
                            offers.add(offer);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        if(offer.isAvailable()) {
                            offers.add(offer);
                        }
                    }
                    cursor.moveToNext();
                }
                cursor.close();
                db.close();
                Offer.offers = offers;
                return offers;
            }
        } else {
            // TODO do Parse error reporting
            Logger.doSecureLogging(Log.WARN, AppInstallsFragment.class.getName()
                    + " An Error occured while retrieving Readable Database");
        }
        return null;
    }


    public static float checkIfOffer(Context context, String packageName) {
        SQLiteDatabase database = SQLWrapper.getReadableSqLiteDatabase(context);
        String whereClause = CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_PACKAGE_NAME + "=?";
        String[] whereArgs = new String[]{packageName};
        Cursor cursor = database.query(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS,
                new String[]{CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_OFFERS_COLUMN_AFFID}, whereClause, whereArgs, null, null, null);
        if(cursor == null || cursor.getCount() == 0) {
            if(cursor != null) {
                cursor.close();
            }
            database.close();
            return -1;
        } else {
            cursor.moveToFirst();
            String affId = cursor.getString(0);
            cursor.close();
            whereClause = CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_OFFER_AFFID + "=?";
            whereArgs = new String[]{affId};
            cursor = database.query(CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT,
                    new String[]{CashGuruSqliteOpenHelper.TABLE_APP_INSTALL_PAYOUT_COLUMN_PAYOUT}, whereClause, whereArgs, null, null, null);
            if(cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                database.close();
                return -1;
            } else {
                cursor.moveToFirst();
                String payout = cursor.getString(0);
                cursor.close();
                database.close();
                return Float.parseFloat(payout);
            }
        }
    }

    public static void clearCurrentDataFromDB(Context context) {

        SQLiteDatabase db = SQLWrapper.getWritableSqLiteDatabase(context);
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
}
