package net.fireballlabs;

import android.content.Context;
import android.util.Log;

import net.fireballlabs.cashguru.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by Rohit on 8/14/2015.
 */
public class URLShortener {
    public static final String GOOGLE_URL_SHORTNER_LINK = "https://www.googleapis.com/urlshortener/v1/url?key=%s";
    private static final String REFERAL_URL = "https://play.google.com/store/apps/details?id=net.fireballlabs.cashguru?sub=%s";

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public static JSONObject getJSONFromUrl(Context context, String userId) {
        try {
            final String referralUrl = String.format(Locale.US, REFERAL_URL, userId);
            final String shortenerApiUrl = String.format(Locale.US, GOOGLE_URL_SHORTNER_LINK, context.getResources().getString(R.string.google_url_shortner_api_key));
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(shortenerApiUrl);
            httpPost.setEntity(new StringEntity("{\"longUrl\":\""+referralUrl+"\"}"));
            httpPost.setHeader("Content-Type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.e("JSON", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // Parse the String to a JSON Object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // Return JSON String
        return jObj;
    }
}
