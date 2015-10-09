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
 * Created by rohitgarg on 10/7/15.
 */
public class ContactUs {
    private String message;
    private boolean resolved;
    private String reply;
    private Date createdAt;
    private Date updatedAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }


    public static String PARSE_TABLE_COLUMN_USER_ID = "userId";
    public static String PARSE_TABLE_COLUMN_MESSAGE = "message";
    public static String PARSE_TABLE_COLUMN_RESOLVED = "resolved";
    public static String PARSE_TABLE_COLUMN_REPLY = "reply";
    public static String PARSE_TABLE_COLUMN_CREATED_AT = "createdAt";
    public static String PARSE_TABLE_COLUMN_UPDATED_AT = "updatedAt";

    private static List<ContactUs> contactUs;
    public static void addContactUsEntry(Context context, String userId, String message) {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(PARSE_TABLE_COLUMN_USER_ID, userId);
        params.put(PARSE_TABLE_COLUMN_MESSAGE, message);
        try {
            ParseCloud.callFunction(ParseConstants.FUNCTION_CONTACT_US, params);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static List<ContactUs> getContactUsHistory() {
        return contactUs;
    }

    public static List<ContactUs> getContactUsHistory(Context context) throws ParseException {
        if(contactUs != null) {
            return contactUs;
        } else {
            contactUs = new ArrayList<>();
        }
        ParseUser user = ParseUser.getCurrentUser();
        if(user == null || !user.isAuthenticated()) {
            return null;
        }
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getObjectId());
        ArrayList<HashMap<String, Object>> cloudContactUs = ParseCloud.callFunction(ParseConstants.FUNCTION_GET_CONTACT_US_HISTORY, params);

        for(int i = 0; i < cloudContactUs.size(); i++) {
            ContactUs contact = new ContactUs();
            contact.setMessage((String) cloudContactUs.get(i).get(PARSE_TABLE_COLUMN_MESSAGE));
            contact.setReply((String) cloudContactUs.get(i).get(PARSE_TABLE_COLUMN_REPLY));
            contact.setResolved((Boolean) cloudContactUs.get(i).get(PARSE_TABLE_COLUMN_RESOLVED));
            contact.setCreatedAt((Date) cloudContactUs.get(i).get(PARSE_TABLE_COLUMN_CREATED_AT));
            contact.setUpdatedAt((Date) cloudContactUs.get(i).get(PARSE_TABLE_COLUMN_UPDATED_AT));

            contactUs.add(contact);
        }
        return contactUs;
    }

    public static void resetData() {
        if(contactUs != null) {
            contactUs.clear();
        }
        contactUs = null;
    }

}