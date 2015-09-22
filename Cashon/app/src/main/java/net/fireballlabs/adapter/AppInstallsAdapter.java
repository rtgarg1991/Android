package net.fireballlabs.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.ui.AppInstallsFragment;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;


/**
 * Created by Rohit on 6/16/2015.
 */
public class AppInstallsAdapter extends RecyclerView.Adapter<AppInstallsAdapter.ViewHolder> {
    private final AppInstallsFragment mFragment;
    Context mContext;
    List<String> mOffers;

    private static final int MSG_TIMEOUT = 1;
    private static final long TIMEOUT = 15000;

    TimerHandler handler = new TimerHandler();

    public class TimerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TIMEOUT:
                    Utility.showProgress(null, false, null);
                    Utility.showInformativeDialog(null, mContext, "Timeout", "Connection has timed out.", "Ok", true);
                    break;
            }
        }
    }

    @Override
    public AppInstallsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = null;
        if(type == 1) {
            v = vi.inflate(R.layout.app_install_list_item, parent, false);
            TextView title = (TextView) v.findViewById(R.id.app_install_list_item_title);
            TextView subtitle = (TextView) v.findViewById(R.id.app_install_list_item_subtitle);
            TextView payout = (TextView) v.findViewById(R.id.app_install_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_install_list_item_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_install_list_item_image_view);
            Button button = (Button) v.findViewById(R.id.app_install_list_item_Button);
            ViewHolder holder = new ViewHolder(v, title, subtitle, payout, description, image, button, type);

            return holder;
        } else if(type == -1){
            ViewHolder holder = new ViewHolder(new View(mContext), null, null, null, null, null, null, -1);
            return holder;
        } else {
            // TODO need to check if we need multiple type of offers
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final AppInstallsAdapter.ViewHolder holder, final int position) {
        final Offer offer = Offer.getOffer(mOffers.get(position));
        holder.setTextViewTitleText(offer.getTitle());
        holder.setTextViewSubtitleText(offer.getSubTitle());
        holder.setTextViewPayoutText(Constants.INR_LABEL + String.valueOf(offer.getPayout()));
        if(holder.type == 1) {
            holder.setTextViewDescriptionText("Click here and install this app to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        } else if(holder.type == 2) {
            holder.setTextViewDescriptionText("Install this app and register to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        }

        StringBuilder allPayout = new StringBuilder();
        /*for (AppInstallsFragment.Offer.Payout payout:
                mOffers.get(position).payouts) {
            allPayout.append(payout.description + "\t" + payout.currency + payout.payout);
        }
        holder.textViewReferenceNumber.setText(allPayout);*/
        String url = Offer.IMAGE_SERVER_URL
                + String.format(Locale.ENGLISH, offer.getImageName(), Utility.getDeviceDensity(mContext));
        holder.setImageView(url);

        if(holder.clickButton != null) {
            holder.clickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.showInformativeDialog(new Utility.DialogCallback() {
                        @Override
                        public void onDialogCallback(boolean success) {
                            if(!Utility.isInternetConnected(mContext)) {
                                HardwareAccess.access(mContext, mFragment, HardwareAccess.ACCESS_INTERNET);
                                return;
                            }
                            final WebView webView = new WebView(mContext);
                            final String affUrl = Utility.getRefUrlString(offer.getAffLink(),
                                    ParseUser.getCurrentUser().getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID), offer.getId());
                            final String trackId = ParseUser.getCurrentUser().getObjectId() + "_" + offer.getId() + "_" + 1;

                            webView.setWebViewClient(new WebViewClient() {
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    if(handler.hasMessages(MSG_TIMEOUT)) {
                                        handler.removeMessages(MSG_TIMEOUT);
                                    }
                                    Logger.doSecureLogging(Log.INFO, url);
                                    if (url.indexOf("https://play.google.com/store/apps/details") == 0) {
                                        UsedOffer.recordInstallAttempt(trackId, mContext);
                                        url = url.replace("https://play.google.com/store/apps/details", "market://details");
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        if(browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                            mContext.startActivity(browserIntent);
                                        } else {
                                            // do nothing for now as there is no Play Store app there in this device
                                        }
                                        Utility.showProgress(mContext, false, null);
                                        return true;
                                    } else if (url.contains("market://details")) {
                                        UsedOffer.recordInstallAttempt(trackId, mContext);
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        if(browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                            mContext.startActivity(browserIntent);
                                        } else {
                                            // do nothing for now as there is no Play Store app there in this device
                                        }
                                        Utility.showProgress(mContext, false, null);
                                        return true;
                                    }
                                    handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT);
                                    view.loadUrl(url);
                                    return false; // then it is not handled by default action
                                }
                            });

                            if (affUrl.contains("play.google.com")) {
                                String url = affUrl;
                                UsedOffer.recordInstallAttempt(trackId, mContext);
                                url = url.replace("https://play.google.com/store/apps/details", "market://details");
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                if(browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                    mContext.startActivity(browserIntent);
                                } else {
                                    // do nothing for now as there is no Play Store app there in this device
                                }
                                Utility.showProgress(mContext, false, null);
                            } else if (affUrl.contains("market://details")) {
                                String url = affUrl;
                                UsedOffer.recordInstallAttempt(trackId, mContext);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                if(browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                    mContext.startActivity(browserIntent);
                                } else {
                                    // do nothing for now as there is no Play Store app there in this device
                                }
                                Utility.showProgress(mContext, false, null);
                            } else {
                                webView.loadUrl(affUrl);
                                Logger.doSecureLogging(Log.INFO, "1" + affUrl);
                                Utility.showProgress(mContext, true, "Please Wait...");
                            }
                            if(handler.hasMessages(MSG_TIMEOUT)) {
                                handler.removeMessages(MSG_TIMEOUT);
                            }
                            handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT);
                        }
                    }, mContext, "Attention!", offer.getDescription(), "OK", true);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            // get package manager for querying all data
            PackageManager pm = mContext.getPackageManager();
            // get all installed applications
            try {
                PackageInfo info= pm.getPackageInfo(Offer.getOffer(mOffers.get(position)).getPackageName(), PackageManager.GET_META_DATA);
                if(info != null) {
                    return -1;
                } else {
                    return Offer.getOffer(mOffers.get(position)).getType();
                }
            } catch (PackageManager.NameNotFoundException e) {
                return Offer.getOffer(mOffers.get(position)).getType();
            }
        } catch(NumberFormatException ex) {
            Crashlytics.logException(ex);
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mOffers == null ? 0 : mOffers.size();
    }

    public AppInstallsAdapter(Context context, AppInstallsFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    public void addAppInstallOffers(List<String> offers) {
        if(mOffers == null) {
            mOffers = new ArrayList<String>();
        } else {
            mOffers.clear();
        }
        if(offers == null) {
            mFragment.setEmptyViewVisibility(View.GONE);
            mOffers.addAll(new ArrayList<String>());
        } else {
            mOffers.addAll(offers);
            if(mOffers != null && mOffers.size() > 0) {
                mFragment.setEmptyViewVisibility(View.GONE);
            } else {
                mFragment.setEmptyViewVisibility(View.VISIBLE);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final int type;
        TextView textViewTitle = null;
        TextView textViewSubtitle;
        TextView textViewPayout = null;
        TextView textViewDescription = null;
        TextView textViewPayoutDescription = null;
        Button clickButton = null;
        ImageView imageView = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewSubtitle, TextView textViewPayout,
                          TextView textViewDescription, ImageView imageView, Button clickButton, int type) {
            this(itemView, textViewTitle, textViewSubtitle, textViewPayout, textViewDescription, null, imageView, clickButton, type);
        }

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewSubtitle, TextView textViewPayout,
                          TextView textViewDescription, TextView textViewPayoutDescription,
                          ImageView imageView, Button clickButton, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewTitle = textViewTitle;
            this.textViewSubtitle = textViewSubtitle;
            this.textViewPayout = textViewPayout;
            this.textViewDescription = textViewDescription;
            this.textViewPayoutDescription = textViewPayoutDescription;
            this.imageView = imageView;
            this.clickButton = clickButton;
            this.type = type;
        }


        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public void setTextViewTitleText(String text) {
            if(this.textViewTitle != null) {
                this.textViewTitle.setText(text);
            }
        }

        public TextView getTextViewSubtitle() {
            return textViewSubtitle;
        }

        public void setTextViewSubtitleText(String text) {
            if(this.textViewSubtitle != null) {
                this.textViewSubtitle.setText(text);
            }
        }

        public TextView getTextViewPayout() {
            return textViewPayout;
        }

        public void setTextViewPayoutText(String text) {
            if(this.textViewPayout != null) {
                this.textViewPayout.setText(text);
            }
        }

        public TextView getTextViewDescription() {
            return textViewDescription;
        }

        public void setTextViewDescriptionText(String text) {
            if(this.textViewDescription != null) {
                this.textViewDescription.setText(text);
            }
        }

        public TextView getTextViewPayoutDescription() {
            return textViewPayoutDescription;
        }

        public void setTextViewPayoutDescriptionText(String text) {
            if(this.textViewPayoutDescription != null) {
                this.textViewPayoutDescription.setText(text);
            }
        }

        public Button getClickButton() {
            return clickButton;
        }

        public void setClickButtonText(String text) {
            if(this.clickButton != null) {
                this.clickButton.setText(text);
            }
        }

        public ImageView getImageView() {
            return imageView;
        }

        public void setImageView(String url) {
            if(this.imageView != null) {
                Picasso.with(mContext)
                        .load(url)
                        .into(this.imageView);
            }
        }

        public View getParent() {
            return parent;
        }

        public void setParent(View parent) {
            this.parent = parent;
        }
    }
}
