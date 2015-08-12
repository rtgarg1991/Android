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
import android.widget.ImageView;
import android.widget.TextView;

import com.fireballlabs.cashguru.R;
import com.fireballlabs.helper.model.LatestDeal;
import com.fireballlabs.helper.model.UsedOffer;
import com.fireballlabs.impl.Utility;
import com.fireballlabs.ui.LatestDealsFragment;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rohit on 6/16/2015.
 */
public class LatestDealsAdapter extends RecyclerView.Adapter<LatestDealsAdapter.ViewHolder> {
    private final LatestDealsFragment mFragment;
    Context mContext;
    List<LatestDeal> mOffers;

    @Override
    public LatestDealsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.latest_deals_list_item, parent, false);
        TextView title = (TextView) v.findViewById(R.id.latest_deals_list_item_title);
        TextView description = (TextView) v.findViewById(R.id.latest_deals_list_item_description);
        ImageView image = (ImageView) v.findViewById(R.id.latest_deals_list_item_image_view);
        ViewHolder holder = new ViewHolder(v, title, description, image);

        return holder;
    }

    @Override
    public void onBindViewHolder(LatestDealsAdapter.ViewHolder holder, final int position) {
        holder.setTitleText(mOffers.get(position).getTitle());
        holder.setDescriptionText(mOffers.get(position).getDescription());
        String url = LatestDeal.IMAGE_SERVER_URL
                + mOffers.get(position).getImageName().replace("*", "xhdpi");
        holder.setImageUrl(url);

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOffers.get(position).getPackageName() == null) {
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mOffers.get(position).getDealAffUrl().replace("{Email}", ParseUser.getCurrentUser().getUsername())));
                mContext.startActivity(intent);
            }

            private void handleAffUrl() {
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
                webView.loadUrl(String.format(Locale.US, mOffers.get(position).getAppAffUrl(),
                        uniqueClick));
                Utility.showProgress(mContext, true, "Please Wait...");
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
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

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewDescription, ImageView imageView) {
            super(itemView);
            this.parent = itemView;
            this.textViewTitle = textViewTitle;
            this.textViewDescription = textViewDescription;
            this.imageView = imageView;
        }

        public void setTitleText(String title) {
            if(textViewTitle != null) {
                textViewTitle.setText(title);
            }
        }

        public void setDescriptionText(String description) {
            if(textViewDescription != null) {
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
    }
}
