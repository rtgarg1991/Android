package com.cashon.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cashon.cashon.R;
import com.cashon.helper.model.Offer;
import com.cashon.helper.model.UsedOffer;
import com.cashon.impl.SimpleDelayHandler;
import com.cashon.ui.CompletedInstallsFragment;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rohit on 6/16/2015.
 */
public class CompletedInstallsAdapter extends RecyclerView.Adapter<CompletedInstallsAdapter.ViewHolder> implements SimpleDelayHandler.SimpleDelayHandlerCallback {
    CompletedInstallsFragment mFragment = null;
    Context mContext;
    List<Offer> mOffers;

    @Override
    public CompletedInstallsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = null;
        if(type == 1) {
            v = vi.inflate(R.layout.app_completed_list_item, parent, false);
            TextView title = (TextView) v.findViewById(R.id.app_completed_list_item_title);
            TextView subtitle = (TextView) v.findViewById(R.id.app_completed_list_item_subtitle);
            TextView payout = (TextView) v.findViewById(R.id.app_completed_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_pending_list_item_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_completed_list_item_image_view);
            return new ViewHolder(v, title, subtitle, description, payout, image);
        } else {
            // TODO need to check if we need multiple type of offers
        }
        return null;
    }

    @Override
    public void onBindViewHolder(CompletedInstallsAdapter.ViewHolder holder, final int position) {
        holder.setTextViewTitleText(mOffers.get(position).getTitle());
        holder.setTextViewSubtitleText(mOffers.get(position).getSubTitle());
        holder.setTextViewPayoutText(String.valueOf(mOffers.get(position).getPayout()));
        holder.setTextViewDescriptionText(mOffers.get(position).getDescription());

        String url = Offer.IMAGE_SERVER_URL
                + mOffers.get(position).getImageName().replace("*", "xhdpi");
        holder.setImageView(url);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return mOffers.get(position).type;
        } catch(NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mOffers == null ? 0 : mOffers.size();
    }

    public CompletedInstallsAdapter(final Context context, CompletedInstallsFragment fragment) {
        mContext = context;

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if(installation == null) {
            return;
        }
        mFragment = fragment;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Offer> offers = UsedOffer.getCompletedInstallOffers();
                    mOffers = offers;
                    SimpleDelayHandler handler = SimpleDelayHandler.getInstance(context);
                    handler.startDelayed(CompletedInstallsAdapter.this, 0, true);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void handleDelayedHandlerCallback() {
        notifyDataSetChanged();
        if(mOffers != null && mOffers.size() > 0) {
            mFragment.setEmptyViewVisibility(View.GONE);
        } else {
            mFragment.setEmptyViewVisibility(View.VISIBLE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle = null;
        TextView textViewSubtitle = null;
        TextView textViewPayout = null;
        TextView textViewDescription = null;
        ImageView imageView = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewSubitle,TextView textViewDescription,
                          TextView textViewPayout, ImageView imageView) {
            super(itemView);
            this.parent = itemView;
            this.textViewTitle = textViewTitle;
            this.textViewSubtitle = textViewSubitle;
            this.textViewDescription = textViewDescription;
            this.textViewPayout = textViewPayout;
            this.imageView = imageView;
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
