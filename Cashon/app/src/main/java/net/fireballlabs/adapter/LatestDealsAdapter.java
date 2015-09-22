package net.fireballlabs.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.LatestDeal;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.ui.LatestDealsFragment;

import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Rohit on 6/16/2015.
 */
public class LatestDealsAdapter extends RecyclerView.Adapter<LatestDealsAdapter.ViewHolder> {
    private final LatestDealsFragment mFragment;
    Context mContext;
    List<LatestDeal> mOffers;

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
    public LatestDealsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.latest_deals_list_item, parent, false);
        TextView title = (TextView) v.findViewById(R.id.latest_deals_list_item_title);
        TextView description = (TextView) v.findViewById(R.id.latest_deals_list_item_description);
        ImageView image = (ImageView) v.findViewById(R.id.latest_deals_list_item_image_view);
        ViewHolder holder = new ViewHolder(v, title, description, image, type);

        return holder;
    }

    @Override
    public void onBindViewHolder(final LatestDealsAdapter.ViewHolder holder, final int position) {
        // if online deal, then set title as title text
        // otherwise we will set this text as description because there is no need of title in that case

        String[] coupons = mOffers.get(position).getDealAffUrl().split(";");
        int random = new Random().nextInt(coupons.length);
        final String couponCode = coupons[random];

        if(holder.getType() == LatestDeal.TYPE_ONLINE_DEAL) {
            holder.setTitleText(mOffers.get(position).getTitle());
            holder.setDescriptionText(mOffers.get(position).getDescription());
        } else {
            // because for refer code based deals, we don't need title and description text view will serve as single text based source
            holder.setDescriptionText(Html.fromHtml(String.format(Locale.US, mOffers.get(position).getTitle(), "<b>" + couponCode + "</b>")), TextView.BufferType.SPANNABLE);
        }
        String url = Offer.IMAGE_SERVER_URL
                + String.format(Locale.ENGLISH, mOffers.get(position).getImageName(), Utility.getDeviceDensity(mContext));
        holder.setImageUrl(url);

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utility.isInternetConnected(mContext)) {
                    HardwareAccess.access(mContext, mFragment, HardwareAccess.ACCESS_INTERNET);
                    return;
                }
                if (mOffers.get(position).getPackageName() == null || mOffers.get(position).getType() == LatestDeal.TYPE_REFER_CODE_DEAL) {
                    handleDealUrl();
                } else {
                    PackageManager pm = mContext.getPackageManager();
                    // get all installed applications
                    try {
                        PackageInfo info = pm.getPackageInfo(mOffers.get(position).getPackageName(), PackageManager.GET_META_DATA);
                        if (info == null) {
                            handleAffUrl();
                        } else {
                            handleDealUrl();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        handleAffUrl();
                    }
                }
            }

            private void handleDealUrl() {
                if (holder.getType() == LatestDeal.TYPE_ONLINE_DEAL) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mOffers.get(position).getDealAffUrl().replace("{Email}", ParseUser.getCurrentUser().getUsername())));
                    mContext.startActivity(intent);
                } else if(holder.getType() == LatestDeal.TYPE_REFER_CODE_DEAL) {
                    final String code;
                    if(couponCode == null) {
                        String[] coupons = mOffers.get(position).getDealAffUrl().split(";");
                        int random = new Random().nextInt(coupons.length);
                        code = coupons[random];
                    } else {
                        code = couponCode;
                    }
                    Utility.showInformativeDialog(new Utility.DialogCallback() {
                        @Override
                        public void onDialogCallback(boolean success) {
                            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(code);
                            } else {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Referral Code", code);
                                clipboard.setPrimaryClip(clip);
                            }
                            Toast.makeText(mContext, Constants.COUPON_CODE_COPIED, Toast.LENGTH_LONG).show();
                            handleAffUrl();
                        }
                    }, mContext, "Description", String.format(Locale.US, mOffers.get(position).getDescription(), code), "Copy code & Activate Offer", true);
                }
            }

            private void handleAffUrl() {
                final WebView webView = new WebView(mContext);
                final String affUrl;
                if(mOffers.get(position).getType() == LatestDeal.TYPE_REFER_CODE_DEAL) {
                    affUrl = String.format(mOffers.get(position).getAppAffUrl(), ParseUser.getCurrentUser().getObjectId());
                } else {
                    affUrl = Utility.getRefUrlString(mOffers.get(position).getAppAffUrl(),
                            ParseUser.getCurrentUser().getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID), mOffers.get(position).getId());
                }

                final String trackId = ParseUser.getCurrentUser().getObjectId() + "_" + mOffers.get(position).getId() + "_" + 1;

                webView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if(handler.hasMessages(MSG_TIMEOUT)) {
                            handler.removeMessages(MSG_TIMEOUT);
                        }

                        if (url.contains("play.google.com")) {
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
                    Utility.showProgress(mContext, true, "Please Wait...");
                    if(handler.hasMessages(MSG_TIMEOUT)) {
                        handler.removeMessages(MSG_TIMEOUT);
                    }
                    handler.sendEmptyMessageDelayed(MSG_TIMEOUT, TIMEOUT);
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return mOffers.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mOffers == null ? 0 : mOffers.size();
    }

    public LatestDealsAdapter(final Context context, LatestDealsFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    public void addAll(List<LatestDeal> latestDeals) {
        if(mOffers == null) {
            mOffers = new ArrayList<LatestDeal>();
        } else {
            mOffers.clear();
        }
        mOffers.addAll(latestDeals);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle = null;
        TextView textViewDescription = null;
        ImageView imageView = null;
        View parent;
        int type;

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewDescription, ImageView imageView, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewTitle = textViewTitle;
            this.textViewDescription = textViewDescription;
            this.imageView = imageView;
            this.type = type;
            if(type == LatestDeal.TYPE_REFER_CODE_DEAL) {
                this.textViewTitle.setVisibility(View.GONE);
            }
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setTitleText(String title) {
            if (textViewTitle != null) {
                textViewTitle.setText(title);
            }
        }

        public void setDescriptionText(String description) {
            if (textViewDescription != null) {
                textViewDescription.setText(description);
            }
        }

        public void setImageUrl(String url) {
            if(this.imageView != null) {
                Picasso.with(mContext)
                        .load(url)
                        .into(this.imageView);
            }
        }

        public void setDescriptionText(Spanned spanned, TextView.BufferType spannable) {
            if (textViewDescription != null) {
                textViewDescription.setText(spanned, spannable);
            }
        }
    }
}
