package net.fireballlabs.ui;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.impl.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


public class OfferFragment extends Fragment {
    private static MainActivityCallBacks mCallBacks;
    Offer offer;
    private boolean mDetatched;
    private TextView titleTextView;
    private TextView payoutTextView;
    private ImageView imageView;
    private LinearLayout descriptionLinearLayout;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_offer, container, false);

        titleTextView = (TextView) view.findViewById(R.id.offer_fragment_title_view);
        payoutTextView = (TextView) view.findViewById(R.id.offer_fragment_payout_view);
        imageView = (ImageView) view.findViewById(R.id.offer_fragment_image_view);
        descriptionLinearLayout = (LinearLayout) view.findViewById(R.id.offer_fragment_description_view);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String offerId = bundle.getString("offerId");
            if(offerId != null) {
                offer = Offer.getOffer(offerId);
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
        return view;
    }

    private void getLatestOfferData(final String offerId) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {


                // lets try cloud function to retrieve data
                ParseUser user = ParseUser.getCurrentUser();
                final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                String deviceId = installation.getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID);
                if(user == null || !user.isAuthenticated() || deviceId == null || "".equals(deviceId)) {
                    return false;
                }
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userId", user.getObjectId());
                params.put("offerId", offerId);
                params.put("deviceId", deviceId);

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
                    offer.setTitle((String) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_TITLE));
                    offer.setType((Integer) cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_TYPE));

                    ArrayList<HashMap<String, Object>> subOffers = (ArrayList<HashMap<String, Object>>)cloudOffer.get(Offer.PARSE_TABLE_OFFERS_COLUMN_CLOUD_SUB_OFFERS);
                    for(int j = 0; j < subOffers.size(); j++) {
                        Offer.Payout p = offer.new Payout();
                        p.setOfferType((Integer) subOffers.get(j).get(Offer.PARSE_TABLE_PAYOUT_COLUMN_OFFER_TYPE));
                        p.setDescription((String) subOffers.get(j).get(Offer.PARSE_TABLE_PAYOUT_COLUMN_DESCRIPTION));
                        p.setPayout((Integer) subOffers.get(j).get(Offer.PARSE_TABLE_PAYOUT_COLUMN_OFFER_PAYOUT));
                        p.setInstalled((Boolean) subOffers.get(j).get("installed"));
                        p.setConverted((Boolean) subOffers.get(j).get("converted"));

                        if(offer.payouts == null) {
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
                showProgress(true);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                showProgress(false);
                if(offer != null && success) {
                    formulateOfferDescriptionView();
                }
            }
        }.execute();
    }

    private void formulateOfferDescriptionView() {

        titleTextView.setText(offer.getTitle());
        payoutTextView.setText(Constants.INR_LABEL + String.valueOf(offer.getPayout()));

        String url = Offer.IMAGE_SERVER_URL
                + String.format(Locale.ENGLISH, offer.getImageName(), Utility.getDeviceDensity(getActivity()));
        Picasso.with(getActivity())
                .load(url)
                .into(imageView);
        descriptionLinearLayout.removeAllViews();
        Context context = getActivity();

        LayoutInflater inflater = (LayoutInflater.from(context));
        if(inflater != null) {
            for (Offer.Payout payout : offer.payouts) {
                View offerDetailView = inflater.inflate(R.layout.offer_description_view, descriptionLinearLayout, false);
                TextView offerDescriptionTextView = (TextView) offerDetailView.findViewById(R.id.offer_description_view_text_view);
                TextView offerPayoutTextView = (TextView) offerDetailView.findViewById(R.id.offer_description_view_payout_view);
                ImageView installImageView = (ImageView)offerDetailView.findViewById(R.id.offer_description_view_install_image_view);
                ImageView confirmationImageView = (ImageView)offerDetailView.findViewById(R.id.offer_description_view_confirmation_image_view);
                View centerImageView = (View)offerDetailView.findViewById(R.id.offer_description_view_center_point_view);

                offerDescriptionTextView.setText(payout.getDescription());
                offerPayoutTextView.setText(Constants.INR_LABEL + String.valueOf(payout.getPayout()));

                if(payout.isConverted()) {
                    installImageView.setImageResource(R.drawable.install_done);
                    confirmationImageView.setImageResource(R.drawable.confirmation_done);
                    centerImageView.setBackgroundResource(R.drawable.offer_description_view_circle_completed);
                } else if(payout.isInstalled()) {
                    installImageView.setImageResource(R.drawable.install_done);
                    confirmationImageView.setImageResource(R.drawable.confirmation_pending);
                    centerImageView.setBackgroundResource(R.drawable.offer_description_view_circle_active);
                } else {
                    installImageView.setImageResource(R.drawable.install_pending);
                    confirmationImageView.setImageResource(R.drawable.confirmation_pending);
                    centerImageView.setBackgroundResource(R.drawable.offer_description_view_circle_inactive);
                }
                descriptionLinearLayout.addView(offerDetailView);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDetatched = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetatched = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showInformativeDialog(null, null, null, null, null, false);
    }

    public void showProgress(boolean show) {
        if(isAdded() && !mDetatched) {
            Utility.showProgress(getActivity(), show, String.valueOf(getResources().getText(R.string.please_wait_app_offers)));
        }
    }
}
