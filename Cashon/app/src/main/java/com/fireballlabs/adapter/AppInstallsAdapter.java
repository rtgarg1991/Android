package com.fireballlabs.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fireballlabs.cashguru.R;
import com.fireballlabs.helper.Constants;
import com.fireballlabs.helper.model.Offer;
import com.fireballlabs.helper.model.UsedOffer;
import com.fireballlabs.impl.Utility;
import com.fireballlabs.ui.AppInstallsFragment;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rohit on 6/16/2015.
 */
public class AppInstallsAdapter extends RecyclerView.Adapter<AppInstallsAdapter.ViewHolder> {
    private final AppInstallsFragment mFragment;
    Context mContext;
    List<Offer> mOffers;

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
            ViewHolder holder = new ViewHolder(v, title, subtitle, payout, description, image, button);

            return holder;
        } else if(type == -1){
            ViewHolder holder = new ViewHolder(new View(mContext), null, null, null, null, null, null);
            return holder;
        } else {
            // TODO need to check if we need multiple type of offers
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final AppInstallsAdapter.ViewHolder holder, final int position) {
        holder.setTextViewTitleText(mOffers.get(position).getTitle());
        holder.setTextViewSubtitleText(mOffers.get(position).getSubTitle());
        holder.setTextViewPayoutText(Constants.INR_LABEL + String.valueOf(mOffers.get(position).getPayout()));
        holder.setTextViewDescriptionText(mOffers.get(position).getDescription());

        StringBuilder allPayout = new StringBuilder();
        /*for (AppInstallsFragment.Offer.Payout payout:
                mOffers.get(position).payouts) {
            allPayout.append(payout.description + "\t" + payout.currency + payout.payout);
        }
        holder.textViewPayoutDescription.setText(allPayout);*/
        String url = Offer.IMAGE_SERVER_URL
                + mOffers.get(position).getImageName().replace("*", "xhdpi");
        holder.setImageView(url);

        if(holder.clickButton != null) {
            holder.clickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final WebView webView = new WebView(mContext);
                    final String uniqueClick = Utility.getRefUrlString(ParseUser.getCurrentUser().getObjectId(), mOffers.get(position).getId());

                    webView.setWebViewClient(new WebViewClient() {
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {

                            if (url.contains("play.google.com")) {
                                UsedOffer.recordInstallAttempt(uniqueClick, mContext);
                                url = url.replace("https://play.google.com/store/apps/details", "market://details");
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mContext.startActivity(browserIntent);
                                Utility.showProgress(mContext, false, null);
                                return true;
                            } else if (url.contains("market://details")) {
                                UsedOffer.recordInstallAttempt(uniqueClick, mContext);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                mContext.startActivity(browserIntent);
                                Utility.showProgress(mContext, false, null);
                                return true;
                            }
                            view.loadUrl(url);
                            return false; // then it is not handled by default action
                        }
                    });
                    webView.loadUrl(String.format(Locale.US, mOffers.get(position).getAffLink(),
                            uniqueClick));
                    Utility.showProgress(mContext, true, "Please Wait...");
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
                PackageInfo info= pm.getPackageInfo(mOffers.get(position).getPackageName(), PackageManager.GET_META_DATA);
                if(info != null) {
                    return -1;
                } else {
                    return mOffers.get(position).getType();
                }
            } catch (PackageManager.NameNotFoundException e) {
                return mOffers.get(position).getType();
            }
        } catch(NumberFormatException ex) {
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

    public void addAppInstallOffers(List<Offer> offers) {
        if(mOffers == null) {
            mOffers = new ArrayList<Offer>();
        } else {
            mOffers.clear();
        }
        if(offers == null) {
            mFragment.setEmptyViewVisibility(View.GONE);
            mOffers.addAll(new ArrayList<Offer>());
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
        TextView textViewTitle = null;
        TextView textViewSubtitle;
        TextView textViewPayout = null;
        TextView textViewDescription = null;
        TextView textViewPayoutDescription = null;
        Button clickButton = null;
        ImageView imageView = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewSubtitle, TextView textViewPayout,
                          TextView textViewDescription, ImageView imageView, Button clickButton) {
            this(itemView, textViewTitle, textViewSubtitle, textViewPayout, textViewDescription, null, imageView, clickButton);
        }

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewSubtitle, TextView textViewPayout,
                          TextView textViewDescription, TextView textViewPayoutDescription,
                          ImageView imageView, Button clickButton) {
            super(itemView);
            this.parent = itemView;
            this.textViewTitle = textViewTitle;
            this.textViewSubtitle = textViewSubtitle;
            this.textViewPayout = textViewPayout;
            this.textViewDescription = textViewDescription;
            this.textViewPayoutDescription = textViewPayoutDescription;
            this.imageView = imageView;
            this.clickButton = clickButton;
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
