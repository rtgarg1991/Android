package net.fireballlabs;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.fireballlabs.cashguru.R;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by Rohit on 8/14/2015.
 */
public class URLShortener {
    public static final String GOOGLE_URL_SHORTNER_LINK = "https://www.googleapis.com/urlshortener/v1/url?key=%s";
    private static final String REFERAL_URL = "http://app.appsflyer.com/net.fireballlabs.cashguru?pid=User_invite&c=%s";

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public static String getShortenedUrl(Context context, String userId) {
        /*try {
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
            Crashlytics.logException(e);
        } catch (IOException e) {
            Crashlytics.logException(e);
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
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        // Parse the String to a JSON Object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }

        // Return JSON String
        return jObj;*/

        final String referralUrl = String.format(Locale.US, REFERAL_URL, userId);
        final String shortenerApiUrl = String.format(Locale.US, GOOGLE_URL_SHORTNER_LINK, context.getResources().getString(R.string.google_url_shortner_api_key));

        return getShortUrl(context, referralUrl);
    }

    @Nullable
    public static String getShortUrl(Context context, String longUrl) {
        GoogleShortenerPerformer shortener = new GoogleShortenerPerformer(new OkHttpClient());

        GooglShortenerResult result;
        result = shortener.shortenUrl(
                new GooglShortenerRequestBuilder()
                        .buildRequest(longUrl, context.getResources().getString(R.string.google_url_shortner_api_key))
        );

        if ( GooglShortenerResult.Status.SUCCESS.equals(result.getStatus())) {
            return result.getShortenedUrl();
            // all ok result.getShortenedUrl() contains the shortened url!
        } else {
            // let's try again
            result = shortener.shortenUrl(
                    new GooglShortenerRequestBuilder()
                            .buildRequest(longUrl, context.getResources().getString(R.string.google_url_shortner_api_key))
            );
            if ( GooglShortenerResult.Status.SUCCESS.equals(result.getStatus())) {
                return result.getShortenedUrl();
                // all ok result.getShortenedUrl() contains the shortened url!
            } else {
                return null;
            }
        }
    }

    public static class GooglShortenerRequestBuilder {
        private static final String GOOGL_SCHEMA           = "https";
        private static final String GOOGL_AUTHORITY        = "www.googleapis.com";
        private static final String GOOGL_PATH             = "/urlshortener/v1/url";
        private static final String GOOGL_APIKEY_PARAMETER = "key";

        public Request buildRequest(String urlToShorten) {
            return buildRequest(urlToShorten, null);
        }
        public Request buildRequest(String urlToShorten, String apiKey) {
            return new Request.Builder()
                    .url(buildUrl(apiKey))
                    .post(
                            RequestBody.create(
                                    MediaType.parse("application/json"),
                                    new Gson().toJson(new GooglShortenBody(urlToShorten))
                            )
                    )
                    .build();
        }

        private String buildUrl(String apiKey) {
            Uri.Builder builder = new Uri.Builder()
                    .scheme(getSchema())
                    .encodedAuthority(getAuthority())
                    .appendEncodedPath(getPath());

            if ( ! TextUtils.isEmpty(apiKey) ) {
                builder.appendQueryParameter(getApiKeyUrlParameterName(), apiKey);
            }

            String lol = builder.toString();
            return lol;
        }

        protected String getApiKeyUrlParameterName() {
            return GOOGL_APIKEY_PARAMETER;
        }

        protected String getPath() {
            return GOOGL_PATH;
        }

        protected String getAuthority() {
            return GOOGL_AUTHORITY;
        }

        protected String getSchema() {
            return GOOGL_SCHEMA;
        }

        protected static class GooglShortenBody {
            private final String longUrl;

            private GooglShortenBody(String longUrl) {
                this.longUrl = longUrl;
            }


        }
    }

    public static class GoogleShortenerPerformer {
        private final OkHttpClient httpClient;


        public GoogleShortenerPerformer(OkHttpClient client) { this.httpClient = client; }

        public GooglShortenerResult shortenUrl(Request request)  {
            Response r = null;
            try {
                r = httpClient.newCall(request).execute();
                if (r.code() != 200) {
                    return GooglShortenerResult.buildFail("Status Code is not 200 -> Received: " + r.code());
                } else {
                    String responseBody = r.body().string();
                    try {
                        GooglShortenResult googlShortenResult = new Gson().fromJson(responseBody, GooglShortenResult.class);
                        if (googlShortenResult != null && ! TextUtils.isEmpty(googlShortenResult.getId())) {
                            return GooglShortenerResult.buildSuccess(googlShortenResult.getId());
                        } else {
                            return GooglShortenerResult.buildFail("Shortened url is null. Response body: " + responseBody);
                        }
                    } catch (JsonSyntaxException e) {
                        return GooglShortenerResult.buildFail(e.getMessage());
                    }
                }
            } catch (IOException e) {
                return GooglShortenerResult.fromFail(e);
            }

        }





        private static class GooglShortenResult {
            private String id;

            public String getId() {
                return id;
            }
        }

    }

    public static class GooglShortenerResult {


        public static enum Status {
            SUCCESS,
            IO_EXCEPTION,
            RESPONSE_ERROR
        }
        private Status status;
        private String shortenedUrl;
        private Exception exception;

        private GooglShortenerResult() {};
        public GooglShortenerResult(Status status, String shortenedUrl) {
            this.status = status;
            this.shortenedUrl = shortenedUrl;
            this.exception = null;
        }

        public Status getStatus() {
            return status;
        }

        public String getShortenedUrl() {
            return shortenedUrl;
        }

        public Exception getException() {
            return exception;
        }

        public static GooglShortenerResult buildSuccess(String shortenedUrl) {
            GooglShortenerResult toRet = new GooglShortenerResult();
            toRet.status = Status.SUCCESS;
            toRet.shortenedUrl = shortenedUrl;
            return toRet;
        }

        public static GooglShortenerResult fromFail(IOException e) {
            GooglShortenerResult toRet = new GooglShortenerResult();
            toRet.status = Status.IO_EXCEPTION;
            toRet.exception = e;
            return toRet;
        }

        public static GooglShortenerResult buildFail(String s) {
            GooglShortenerResult toRet = new GooglShortenerResult();
            toRet.status = Status.RESPONSE_ERROR;
            toRet.exception = new GooglShortenerException(s);
            return toRet;
        }
    }

    public static class GooglShortenerException extends Exception {
        public GooglShortenerException(String detailMessage) {
            super(detailMessage);
        }
    }
}
