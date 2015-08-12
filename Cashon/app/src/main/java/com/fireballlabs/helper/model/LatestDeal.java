package com.fireballlabs.helper.model;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rohit on 8/7/2015.
 */
public class LatestDeal {
    private static final String PARSE_TABLE_NAME_LATEST_DEALS = "LatestDeal";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_ID = "id";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_PACKAGE_NAME = "packageName";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_APP_AFF_URL = "appAffUrl";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_DEAL_AFF_URL = "dealAffUrl";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_TITLE = "title";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_DESCRIPTION = "description";
    private static final String PARSE_TABLE_LATEST_DEALS_COLUMN_IMAGE_NAME = "imageName";

    public static String IMAGE_SERVER_URL = "http://704tourism.com/ankur/images/";

    String id;
    String packageName;
    String appAffUrl;
    String dealAffUrl;
    String title;
    String description;
    String imageName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppAffUrl() {
        return appAffUrl;
    }

    public void setAppAffUrl(String appAffUrl) {
        this.appAffUrl = appAffUrl;
    }

    public String getDealAffUrl() {
        return dealAffUrl;
    }

    public void setDealAffUrl(String dealAffUrl) {
        this.dealAffUrl = dealAffUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public static List<LatestDeal> getAllDeals() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_NAME_LATEST_DEALS);
        List<ParseObject> list = query.find();

        List<LatestDeal> deals = new ArrayList<LatestDeal>();
        for(ParseObject obj : list) {
            LatestDeal deal = new LatestDeal();
            deal.setAppAffUrl(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_APP_AFF_URL));
            deal.setDealAffUrl(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_DEAL_AFF_URL));
            deal.setDescription(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_DESCRIPTION));
            deal.setId(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_ID));
            deal.setImageName(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_IMAGE_NAME));
            deal.setPackageName(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_PACKAGE_NAME));
            deal.setTitle(obj.getString(PARSE_TABLE_LATEST_DEALS_COLUMN_TITLE));

            deals.add(deal);
        }

        return deals;
    }
}
