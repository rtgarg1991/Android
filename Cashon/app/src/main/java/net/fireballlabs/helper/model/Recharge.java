package net.fireballlabs.helper.model;

import android.content.Context;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import net.fireballlabs.helper.ParseConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rohitgarg on 9/18/15.
 */
public class Recharge {

    private int amount;
    private String company;
    private boolean isCompleted;
    private boolean isPrepaid;
    private String number;
    private String referenceNumber;
    private String type;
    private Date requestedDate;
    private Date completedDate;


    public static String PARSE_TABLE_COLUMN_AMOUNT = "amount";
    public static String PARSE_TABLE_COLUMN_COMPANY = "company";
    public static String PARSE_TABLE_COLUMN_COMPLETED = "isCompleted";
    public static String PARSE_TABLE_COLUMN_PREPAID = "isPrepaid";
    public static String PARSE_TABLE_COLUMN_NUMBER = "number";
    public static String PARSE_TABLE_COLUMN_REFERENCE_NUMBER = "referenceNumber";
    public static String PARSE_TABLE_COLUMN_TYPE = "type";
    public static String PARSE_TABLE_COLUMN_REQUESTED_DATE = "requestedDate";
    public static String PARSE_TABLE_COLUMN_COMPLETED_DATE = "completedDate";
    public static String PARSE_TABLE_COLUMN_COMMENT = "comment";
    public static String PARSE_TABLE_COLUMN_CIRCLE = "circle";


    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public boolean isPrepaid() {
        return isPrepaid;
    }

    public void setIsPrepaid(boolean isPrepaid) {
        this.isPrepaid = isPrepaid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    private static List<Recharge> recharges;

    public static List<Recharge> getRechargeHistory(Context context) throws ParseException {
        if(recharges != null) {
            return recharges;
        } else {
            recharges = new ArrayList<>();
        }
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return null;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getObjectId());
        ArrayList<HashMap<String, Object>> cloudRecharges = ParseCloud.callFunction(ParseConstants.FUNCTION_GET_RECHARGE_HISTORY, params);
        List<Offer> offers = new ArrayList<Offer>();
        for(int i = 0; i < cloudRecharges.size(); i++) {
            Recharge recharge = new Recharge();
            HashMap<String, Object> rech = cloudRecharges.get(i);
            recharge.setAmount(Integer.parseInt((String) rech.get(PARSE_TABLE_COLUMN_AMOUNT)));
            recharge.setNumber((String) rech.get(PARSE_TABLE_COLUMN_NUMBER));
            recharge.setCompany((String) rech.get(PARSE_TABLE_COLUMN_COMPANY));
            recharge.setCompletedDate((Date) rech.get(PARSE_TABLE_COLUMN_COMPLETED_DATE));
            recharge.setIsPrepaid((Boolean) rech.get(PARSE_TABLE_COLUMN_PREPAID));
            recharge.setRequestedDate((Date) rech.get(PARSE_TABLE_COLUMN_REQUESTED_DATE));
            recharge.setType((String) rech.get(PARSE_TABLE_COLUMN_TYPE));

            if(rech.get(PARSE_TABLE_COLUMN_REFERENCE_NUMBER) != null) {
                recharge.setReferenceNumber((String) rech.get(PARSE_TABLE_COLUMN_REFERENCE_NUMBER));
            }
            if(rech.get(PARSE_TABLE_COLUMN_COMPLETED) != null) {
                recharge.setIsCompleted((Boolean) rech.get(PARSE_TABLE_COLUMN_COMPLETED));
            } else {
                recharge.setIsCompleted(false);
            }
            recharges.add(recharge);
        }
        return recharges;
    }

    public static void clearRechargeHistory() {
        recharges = null;
    }

    public static List<Recharge> getRecharges() {
        return recharges;
    }
}
