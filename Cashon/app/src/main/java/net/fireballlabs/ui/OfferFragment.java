package net.fireballlabs.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.cashguru.BuildConfig;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;



public class OfferFragment extends BaseFragment implements HardwareAccess.HardwareAccessCallbacks {
    private static MainActivityCallBacks mCallBacks;
    Offer offer;
    private TextView titleTextView;
    private TextView categoryTextView;
    private TextView descriptionTextView;
    private TextView tncTextView;
    private TextView payoutTextView;
    private ImageView imageView;
    private LinearLayout descriptionLinearLayout;
    private Button installButton;

    public static OfferFragment newInstance(MainActivityCallBacks callBacks, String offerId) {
        OfferFragment fragment = new OfferFragment();
        mCallBacks = callBacks;
        Bundle bundle = new Bundle();
        bundle.putString("offerId", offerId);

        fragment.setArguments(bundle);
        return fragment;
    }

    public OfferFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static final int MSG_TIMEOUT = 1;
    private static final long TIMEOUT = 15000;

    public class TimerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TIMEOUT:
                    if(isValidContext(getActivity())) {
                        Utility.showProgress(null, false, null);
                        Utility.showInformativeDialog(null, getActivity(), "Timeout", "Connection has timed out.", "Ok", true);
                    }
                    break;
            }
        }
    }

    TimerHandler handler = new TimerHandler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_offer, container, false);

        titleTextView = (TextView) view.findViewById(R.id.offer_fragment_title_view);
        categoryTextView = (TextView) view.findViewById(R.id.offer_fragment_category_view);
        descriptionTextView = (TextView) view.findViewById(R.id.offer_fragment_description_text_view);
        tncTextView = (TextView) view.findViewById(R.id.offer_fragment_tnc_text_view);
        payoutTextView = (TextView) view.findViewById(R.id.offer_fragment_payout_view);
        imageView = (ImageView) view.findViewById(R.id.offer_fragment_image_view);
        descriptionLinearLayout = (LinearLayout) view.findViewById(R.id.offer_fragment_description_view);
        installButton = (Button) view.findViewById(R.id.offer_fragment_install_button);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String offerId = bundle.getString("offerId");
            if(offerId != null) {
                offer = Offer.getOffer(offerId, getActivity());
                if(offer != null) {
                    titleTextView.setText(offer.getTitle());
                    payoutTextView.setText(Constants.INR_LABEL + String.valueOf(offer.getPayout()));

                    String url = Offer.IMAGE_SERVER_URL
                            + String.format(Locale.ENGLISH, offer.getImageName(), Utility.getDeviceDensity(getActivity()));
                    Picasso.with(getActivity())
                            .load(url)
                            .into(imageView);
                    getLatestOfferData(offerId);
                } else {
                    getLatestOfferData(offerId);
                }
            }
        }

        final Context context = getActivity();
        installButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Utility.isInternetConnected(context)) {
                    HardwareAccess.access(context, OfferFragment.this, HardwareAccess.ACCESS_INTERNET);
                    return;
                }
                final String affUrl = Utility.getRefUrlString(offer.getAffLink(),
                        ParseUser.getCurrentUser().getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID), offer.getId());
                final String trackId = offer.getPackageName();

                if(offer.getType() == Constants.OFFER_TYPE_NEW_INSTALL
                        || offer.getType() == Constants.OFFER_TYPE_NORMAL_INSTALL
                        || offer.getType() == Constants.OFFER_TYPE_REGISTER_INSTALL) {
                    final WebView webView = new WebView(context);
                    webView.setWebViewClient(new WebViewClient() {
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            if (handler.hasMessages(MSG_TIMEOUT)) {
                                handler.removeMessages(MSG_TIMEOUT);
                            }
                            Logger.doSecureLogging(Log.INFO, url);
                            if (url.indexOf("https://play.google.com/store/apps/details") == 0) {
                                UsedOffer.recordInstallAttempt(trackId, context);
                                url = url.replace("https://play.google.com/store/apps/details", "market://details");
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(browserIntent);
                                } else {
                                    // do nothing for now as there is no Play Store app there in this device
                                }
                                Utility.showProgress(context, false, null);
                                return true;
                            } else if (url.contains("market://details")) {
                                UsedOffer.recordInstallAttempt(trackId, context);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(browserIntent);
                                } else {
                                    // do nothing for now as there is no Play Store app there in this device
                                }
                                Utility.showProgress(context, false, null);
                                return true;
                            }
                            handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT);
                            view.loadUrl(url);
                            return false; // then it is not handled by default action
                        }
                    });

                    if (affUrl.contains("play.google.com")) {
                        String url = affUrl;
                        UsedOffer.recordInstallAttempt(trackId, context);
                        url = url.replace("https://play.google.com/store/apps/details", "market://details");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(browserIntent);
                        } else {
                            // do nothing for now as there is no Play Store app there in this device
                        }
                        Utility.showProgress(context, false, null);
                    } else if (affUrl.contains("market://details")) {
                        String url = affUrl;
                        UsedOffer.recordInstallAttempt(trackId, context);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(browserIntent);
                        } else {
                            // do nothing for now as there is no Play Store app there in this device
                        }
                        Utility.showProgress(context, false, null);
                    } else {
                        webView.loadUrl(affUrl);
                        Logger.doSecureLogging(Log.INFO, "1" + affUrl);
                        Utility.showProgress(context, true, "Please Wait...");
                    }
                    if (handler.hasMessages(MSG_TIMEOUT)) {
                        handler.removeMessages(MSG_TIMEOUT);
                    }
                    handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT);
                } else if(offer.getType() == Constants.OFFER_TYPE_DOWNLOAD_INSTALL
                        || offer.getType() == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER
                        || offer.getType() == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL) {
                    UsedOffer.recordInstallAttempt(trackId, context);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(affUrl));
                    context.startActivity(intent);
                } else if(offer.getType() == Constants.OFFER_TYPE_LEAD
                        || offer.getType() == Constants.OFFER_TYPE_NEW_LEAD) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(affUrl));
                    context.startActivity(intent);
                } else if(offer.getType() == Constants.OFFER_TYPE_APP_SHARE
                        || offer.getType() == Constants.OFFER_TYPE_NEW_APP_SHARE) {
                    final String url = Utility.getAppShareUrlString(offer.getAffLink(),
                            ParseUser.getCurrentUser().getObjectId(), offer.getId());

                    /*Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String shortUrl = URLShortener.getShortUrl(getActivity(), url);
                            if(shortUrl != null && isValidContext(getActivity())) {
                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(Constants.TEXT_APP_SHARE, offer.getTitle(), shortUrl));
                                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
                            }
                        }
                    });
                    thread.start();*/

                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(Constants.TEXT_APP_SHARE, offer.getTitle(), url));
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

                }
            }
        });
        return view;
    }

    private void getLatestOfferData(final String offerId) {

        Offer offerLocal = Offer.getOffer(offerId);
        if(offerLocal != null) {
            offer = offerLocal;
            formulateOfferDescriptionView();
        } else {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... voids) {


                    // lets try cloud function to retrieve data
                    ParseUser user = ParseUser.getCurrentUser();
                    final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    String deviceId = installation.getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID);
                    if (user == null || !user.isAuthenticated() || deviceId == null || "".equals(deviceId)) {
                        return false;
                    }
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("userId", user.getObjectId());
                    params.put("offerId", offerId);
                    params.put("deviceId", deviceId);
                    params.put("appId", BuildConfig.VERSION_CODE);

                    HashMap<String, Object> cloudOffer = null;
                    try {
                        cloudOffer = ParseCloud.callFunction(ParseConstants.FUNCTION_GET_USER_OFFER_DATA, params);

                        offer = new Offer();
                        offer.setId((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_ID));
                        offer.setIsAvailable((Boolean) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_IS_AVAILABLE));
                        offer.setAffLink((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_AFF_LINK));
                        offer.setDescription((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_DESCRIPTION));
                        offer.setImageName((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_IMAGE_NAME));
                        offer.setPackageName((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_PACKAGE_NAME));
                        offer.setPayout((Integer) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_PAYOUT));
                        offer.setSubTitle((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_SUB_TITLE));
                        offer.setTnc((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_TNC));
                        offer.setTitle((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_TITLE));
                        offer.setType((Integer) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_TYPE));

                        ArrayList<HashMap<String, Object>> subOffers = (ArrayList<HashMap<String, Object>>) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_CLOUD_SUB_OFFERS);
                        for (int j = 0; j < subOffers.size(); j++) {
                            Offer.Payout p = offer.new Payout();
                            p.setOfferType((Integer) subOffers.get(j).get(Offer.PARSE_TABLE_PAYOUT_COLUMN_OFFER_TYPE));
                            p.setDescription((String) subOffers.get(j).get(Offer.PARSE_TABLE_PAYOUT_COLUMN_DESCRIPTION));
                            p.setPayout((Integer) subOffers.get(j).get(Offer.PARSE_TABLE_PAYOUT_COLUMN_OFFER_PAYOUT));
                            p.setInstalled((Boolean) subOffers.get(j).get("installed"));
                            p.setConverted((Boolean) subOffers.get(j).get("converted"));

                            if (offer.payouts == null) {
                                offer.payouts = new ArrayList<Offer.Payout>();
                            }
                            offer.payouts.add(p);
                        }

                        return true;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if(isValidContext(getActivity())) {
                        showProgress(true);
                    }
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    super.onPostExecute(success);
                    if(isValidContext(getActivity())) {
                        showProgress(false);
                        if (offer != null && success) {
                            formulateOfferDescriptionView();
                        }
                    }
                }
            }.execute();
        }
    }

    private void formulateOfferDescriptionView() {


        if(offer.getType() == Constants.OFFER_TYPE_NORMAL_INSTALL || offer.getType() == Constants.OFFER_TYPE_NEW_INSTALL) {
            installButton.setText("Install");
        } else if(offer.getType() == Constants.OFFER_TYPE_REGISTER_INSTALL) {
            installButton.setTextSize(20);
            installButton.setText("Install & Register");
        } else if(offer.getType() == Constants.OFFER_TYPE_DOWNLOAD_INSTALL || offer.getType() == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL) {
            installButton.setTextSize(20);
            installButton.setText("Download & Install");
        } else if(offer.getType() == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER) {
            installButton.setTextSize(16);
            installButton.setText("Download, Install & Register");
        } else if(offer.getType() == Constants.OFFER_TYPE_LEAD || offer.getType() == Constants.OFFER_TYPE_NEW_LEAD) {
            installButton.setTextSize(16);
            installButton.setText("Complete Survey & Earn");
        } else if(offer.getType() == Constants.OFFER_TYPE_APP_SHARE || offer.getType() == Constants.OFFER_TYPE_NEW_APP_SHARE) {
            installButton.setTextSize(20);
            installButton.setText("Share & Earn");
        }


        titleTextView.setText(offer.getTitle());
        payoutTextView.setText(Constants.INR_LABEL + String.valueOf(offer.getPayout()));
        categoryTextView.setText(offer.getSubTitle());
        descriptionTextView.setText(String.format(Locale.ENGLISH, offer.getDescription()));
        tncTextView.setText(String.format(Locale.ENGLISH, offer.getTnc()));

        String url = Offer.IMAGE_SERVER_URL
                + String.format(Locale.ENGLISH, offer.getImageName(), Utility.getDeviceDensity(getActivity()));
        Picasso.with(getActivity())
                .load(url)
                .into(imageView);
        descriptionLinearLayout.removeAllViews();
        Context context = getActivity();

        LayoutInflater inflater = (LayoutInflater.from(context));
        boolean next = false;
        if(inflater != null) {
            for (Offer.Payout payout : offer.payouts) {
                if(next) {
                    View view = new View(context);
                    view.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4);
                    params.setMargins(4, 4, 4, 4);
                    view.setLayoutParams(params);
                    descriptionLinearLayout.addView(view);
                }
                View offerDetailView = inflater.inflate(R.layout.offer_description_view, descriptionLinearLayout, false);
                TextView offerDescriptionTextView = (TextView) offerDetailView.findViewById(R.id.offer_description_view_text_view);
                TextView offerPayoutTextView = (TextView) offerDetailView.findViewById(R.id.offer_description_view_payout_view);
                ImageView installImageView = (ImageView)offerDetailView.findViewById(R.id.offer_description_view_install_image_view);

                offerDescriptionTextView.setText(payout.getDescription());
                offerPayoutTextView.setText(Constants.INR_LABEL + String.valueOf(payout.getPayout()));

                if(payout.isConverted()) {
                    installImageView.setImageResource(R.drawable.install_done);
                    installButton.setVisibility(View.GONE);
                } else {
                    installImageView.setImageResource(R.drawable.install_pending);
                    offerDetailView.setAlpha(0.5f);
                }
                descriptionLinearLayout.addView(offerDetailView);
                next = true;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showInformativeDialog(null, null, null, null, null, false);
    }

    public void showProgress(boolean show) {
        if(isAdded() && !mDetached) {
            Utility.showProgress(getActivity(), show, String.valueOf(getResources().getText(R.string.please_wait_app_offers)));
        }
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {

    }
}
