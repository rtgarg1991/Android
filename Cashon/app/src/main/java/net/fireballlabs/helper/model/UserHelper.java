package net.fireballlabs.helper.model;

import java.util.Date;

/**
 * Created by Rohit on 8/3/2015.
 */
public class UserHelper {
    String name;
    String email;
    String countryCode;
    String phoneNumber;
    String sex;
    Date dob;

    boolean phoneVerified;
    boolean emailVerified;

    public static String PARSE_TABLE_COLUMN_COUNTRY_CODE = "countryCode";
    public static String PARSE_TABLE_COLUMN_MOBILE = "mobile";
    public static String PARSE_TABLE_COLUMN_REFER_CODE = "referCode";
    public static String PARSE_TABLE_COLUMN_REFER_URL = "referUrl";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    // TODO
    /* Location and Interests*/
}
