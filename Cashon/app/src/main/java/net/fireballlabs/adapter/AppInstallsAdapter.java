package net.fireballlabs.adapter;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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

import com.crashlytics.android.Crashlytics;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.ui.AppInstallsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by Rohit on 6/16/2015.
 */
public class AppInstallsAdapter extends RecyclerView.Adapter<AppInstallsAdapter.ViewHolder> {
    private final AppInstallsFragment mFragment;
    static Context mContext;
    List<String> mOffers;

    private static final int MSG_TIMEOUT = 1;
    private static final long TIMEOUT = 35000;

    TimerHandler handler = new TimerHandler();

    public static class TimerHandler extends Handler {

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
        if(type == Constants.OFFER_TYPE_NORMAL_INSTALL || type == Constants.OFFER_TYPE_REGISTER_INSTALL
                || type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL || type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER
                || type == Constants.OFFER_TYPE_LEAD || type == Constants.OFFER_TYPE_APP_SHARE) {
            v = vi.inflate(R.layout.app_install_list_item, parent, false);
            TextView title = (TextView) v.findViewById(R.id.app_install_list_item_title);
            TextView subtitle = (TextView) v.findViewById(R.id.app_install_list_item_subtitle);
            TextView payout = (TextView) v.findViewById(R.id.app_install_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_install_list_item_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_install_list_item_image_view);
            Button button = (Button) v.findViewById(R.id.app_install_list_item_Button);
            ViewHolder holder = new ViewHolder(v, title, subtitle, payout, description, image, button, type);

            return holder;
        } else if(type == Constants.OFFER_TYPE_NEW_INSTALL || type == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL
                || type == Constants.OFFER_TYPE_NEW_LEAD || type == Constants.OFFER_TYPE_NEW_APP_SHARE) {
            v = vi.inflate(R.layout.app_install_list_item_new, parent, false);
            TextView title = (TextView) v.findViewById(R.id.app_install_list_item_title);
            TextView subtitle = (TextView) v.findViewById(R.id.app_install_list_item_subtitle);
            TextView payout = (TextView) v.findViewById(R.id.app_install_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_install_list_item_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_install_list_item_image_view);
            Button button = (Button) v.findViewById(R.id.app_install_list_item_Button);
            ViewHolder holder = new ViewHolder(v, title, subtitle, payout, description, image, button, type);
            return holder;
        } else if(type > Constants.OFFER_TYPE_APP_REDIRECT_BASE) {
            v = vi.inflate(R.layout.app_install_list_item_navigation, parent, false);
            TextView subtitle = (TextView) v.findViewById(R.id.app_install_list_item_subtitle);
            Button button = (Button) v.findViewById(R.id.app_install_list_item_Button);
            /*TextView title = (TextView) v.findViewById(R.id.app_install_list_item_title);
            TextView payout = (TextView) v.findViewById(R.id.app_install_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_install_list_item_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_install_list_item_image_view);*/
            ViewHolder holder = new ViewHolder(v, null, subtitle, null, null, null, button, type);
            return holder;
        } else if(type == -1) {
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
        if(holder.type == Constants.OFFER_TYPE_NORMAL_INSTALL || holder.type == Constants.OFFER_TYPE_NEW_INSTALL) {
            holder.setTextViewDescriptionText("Click here and install this app to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        } else if(holder.type == Constants.OFFER_TYPE_REGISTER_INSTALL) {
            holder.setTextViewDescriptionText("Install this app and register to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        } else if(holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL || holder.type == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL) {
            holder.setTextViewDescriptionText("Download and Install this app to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        } else if(holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER) {
            holder.setTextViewDescriptionText("Download, Install and Register in this app to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        } else if(holder.type == Constants.OFFER_TYPE_NEW_LEAD || holder.type == Constants.OFFER_TYPE_LEAD) {
            holder.setTextViewDescriptionText("Open and complete this survey to earn " + Constants.INR_LABEL + "" + offer.getPayout());
        } else if(holder.type == Constants.OFFER_TYPE_APP_SHARE || holder.type == Constants.OFFER_TYPE_NEW_APP_SHARE) {
            holder.setTextViewDescriptionText(offer.getDescription());
        }

        if(offer.getImageName() != null) {
            String url = Offer.IMAGE_SERVER_URL
                    + String.format(Locale.ENGLISH, offer.getImageName(), Utility.getDeviceDensity(mContext));
            holder.setImageView(url);
        }

        holder.getParent().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFragment != null) {
                    if (holder.type <= Constants.OFFER_TYPE_APP_REDIRECT_BASE) {
                        mFragment.setFragment(Constants.ID_APP_OFFER, offer.getId());
                    } else {
                        mFragment.setFragment(offer.getType() - Constants.OFFER_TYPE_APP_REDIRECT_BASE, null);
                    }
                }
            }
        });

        if(holder.type > Constants.OFFER_TYPE_APP_REDIRECT_BASE) {
            if (holder.clickButton != null) {
                holder.clickButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFragment.setFragment(offer.getType() - Constants.OFFER_TYPE_APP_REDIRECT_BASE, null);
                    }
                });
            }
        } else if(holder.type == Constants.OFFER_TYPE_APP_SHARE
                || holder.type == Constants.OFFER_TYPE_NEW_APP_SHARE) {

            holder.clickButton.setText("Share & Earn");
            if (holder.clickButton != null) {
                holder.clickButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFragment.setFragment(Constants.ID_APP_OFFER, offer.getId());
                    }
                });
            }

        } else if(holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL
                || holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER
                || holder.type == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL
                || holder.type == Constants.OFFER_TYPE_LEAD
                || holder.type == Constants.OFFER_TYPE_NEW_LEAD) {
            if(holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL
                    || holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER
                    || holder.type == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL) {
                holder.clickButton.setText("Download & Earn");
            } else {
                holder.clickButton.setText("Complete & Earn");
            }
            if (holder.clickButton != null) {
                holder.clickButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utility.showInformativeDialog(new Utility.DialogCallback() {
                            @Override
                            public void onDialogCallback(boolean success) {
                                if (!Utility.isInternetConnected(mContext)) {
                                    HardwareAccess.access(mContext, mFragment, HardwareAccess.ACCESS_INTERNET);
                                    return;
                                }
                                final String affUrl = Utility.getRefUrlString(offer.getAffLink(),
                                        ParseUser.getCurrentUser().getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID), offer.getId());

                                if(holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL
                                        || holder.type == Constants.OFFER_TYPE_DOWNLOAD_INSTALL_REGISTER
                                        || holder.type == Constants.OFFER_TYPE_DOWNLOAD_NEW_INSTALL) {
                                    UsedOffer.recordInstallAttempt(offer.getPackageName(), mContext);
                                }
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(affUrl));
                                mContext.startActivity(intent);
                            }
                        }, mContext, "Attention!", offer.getTnc(), "OK", true);
                    }
                });
            }
        } else {
            if (holder.clickButton != null) {
                holder.clickButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utility.showInformativeDialog(new Utility.DialogCallback() {
                            @Override
                            public void onDialogCallback(boolean success) {
                                if (!Utility.isInternetConnected(mContext)) {
                                    HardwareAccess.access(mContext, mFragment, HardwareAccess.ACCESS_INTERNET);
                                    return;
                                }
                                final WebView webView = new WebView(mContext);
                                final String affUrl = Utility.getRefUrlString(offer.getAffLink(),
                                        ParseUser.getCurrentUser().getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID), offer.getId());
                                final String trackId = offer.getPackageName();
                                webView.getSettings().setJavaScriptEnabled(true);
                                webView.setWebViewClient(new WebViewClient() {
                                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                        if (handler.hasMessages(MSG_TIMEOUT)) {
                                            handler.removeMessages(MSG_TIMEOUT);
                                        }
                                        Logger.doSecureLogging(Log.INFO, url);
                                        if (url.indexOf("https://play.google.com/store/apps/details") == 0) {
                                            UsedOffer.recordInstallAttempt(trackId, mContext);
                                            url = url.replace("https://play.google.com/store/apps/details", "market://details");
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            if (browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                                mContext.startActivity(browserIntent);
                                            } else {
                                                // do nothing for now as there is no Play Store app there in this device
                                            }
                                            Utility.showProgress(mContext, false, null);
                                            return true;
                                        } else if (url.contains("market://details")) {
                                            UsedOffer.recordInstallAttempt(trackId, mContext);
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            if (browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                                mContext.startActivity(browserIntent);
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
                                    if (browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                        mContext.startActivity(browserIntent);
                                    } else {
                                        // do nothing for now as there is no Play Store app there in this device
                                    }
                                    Utility.showProgress(mContext, false, null);
                                } else if (affUrl.contains("market://details")) {
                                    String url = affUrl;
                                    UsedOffer.recordInstallAttempt(trackId, mContext);
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    if (browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
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
                                if (handler.hasMessages(MSG_TIMEOUT)) {
                                    handler.removeMessages(MSG_TIMEOUT);
                                }
                                handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT);
                            }
                        }, mContext, "Attention!", offer.getTnc(), "OK", true);

                        //UsedOffer.checkAndAddPackageOnCloud(mContext, offer.getPackageName());
                    }
                });
            }
        }

        if(mFragment.isScrollDown(position)) {
            holder.parent.setTranslationY(200);
            holder.parent.setAlpha(0);

            PropertyValuesHolder propx = PropertyValuesHolder.ofFloat("translationY", 0);
            PropertyValuesHolder propa = PropertyValuesHolder.ofFloat("alpha", 1);

            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(holder.parent, propx, propa);
//        animator.setStartDelay(0);
            //scrollDelay++;
            animator.setDuration(400);
            animator.start();
        } else {
            holder.parent.setTranslationY(-200);
            holder.parent.setAlpha(0);

            PropertyValuesHolder propx = PropertyValuesHolder.ofFloat("translationY", 0);
            PropertyValuesHolder propa = PropertyValuesHolder.ofFloat("alpha", 1);

            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(holder.parent, propx, propa);
//        animator.setStartDelay(0);
            //scrollDelay++;
            animator.setDuration(400);
            animator.start();
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
        for(int i = 0; i < mOffers.size(); i++) {
            String offerId = mOffers.get(i);
            if(Offer.getOffer(offerId).getType() > Constants.OFFER_TYPE_APP_REDIRECT_BASE) {
                int pos = Offer.getOffer(offerId).getPayout();
                if(mOffers.size() > pos) {
                    mOffers.remove(offerId);
                    mOffers.add(pos, offerId);
                } else {
                    mOffers.remove(offerId);
                    mOffers.add(mOffers.size(), offerId);
                }
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
