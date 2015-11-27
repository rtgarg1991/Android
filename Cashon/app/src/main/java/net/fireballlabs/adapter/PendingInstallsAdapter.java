package net.fireballlabs.adapter;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.SimpleDelayHandler;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.ui.PendingInstallsFragment;

import java.util.List;
import java.util.Locale;

/**
 * Created by Rohit on 6/16/2015.
 */
public class PendingInstallsAdapter extends RecyclerView.Adapter<PendingInstallsAdapter.ViewHolder> implements SimpleDelayHandler.SimpleDelayHandlerCallback {
    PendingInstallsFragment mFragment = null;
    Context mContext;
    List<String> mOffers;

    @Override
    public PendingInstallsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = null;
        if(type > 0) {
            v = vi.inflate(R.layout.app_pending_list_item, parent, false);
            TextView title = (TextView) v.findViewById(R.id.app_pending_list_item_title);
            TextView subtitle = (TextView) v.findViewById(R.id.app_pending_list_item_subtitle);
            TextView payout = (TextView) v.findViewById(R.id.app_pending_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_pending_list_item_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_pending_list_item_image_view);
            return new ViewHolder(v, title, subtitle, description, payout, image);
        } else {
            // TODO need to check if we need multiple type of offers
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final PendingInstallsAdapter.ViewHolder holder, final int position) {
        final Offer offer = Offer.getOffer(mOffers.get(position));
        holder.setTextViewTitleText(offer.getTitle());
        holder.setTextViewSubtitleText(offer.getSubTitle());
        holder.setTextViewPayoutText(Constants.INR_LABEL + String.valueOf(offer.getPayout()));
        holder.setTextViewDescriptionText("Waiting for confirmation from " + offer.getTitle() + ".!");

        String url = Offer.IMAGE_SERVER_URL
                + String.format(Locale.ENGLISH, offer.getImageName(), Utility.getDeviceDensity(mContext));
        holder.setImageView(url);

        holder.getParent().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragment.setFragment(Constants.ID_APP_OFFER, offer.getId());
            }
        });



        holder.parent.setTranslationY(200);
        holder.parent.setAlpha(0);

        PropertyValuesHolder propx = PropertyValuesHolder.ofFloat("translationY", 0);
        PropertyValuesHolder propa = PropertyValuesHolder.ofFloat("alpha", 1);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(holder.parent, propx, propa);
//        animator.setStartDelay(0);
        //scrollDelay++;
        animator.setDuration(400);
        animator.start();
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return Offer.getOffer(mOffers.get(position)).type;
        } catch(NumberFormatException ex) {
            Crashlytics.logException(ex);
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mOffers == null ? 0 : mOffers.size();
    }

    public PendingInstallsAdapter(final Context context, PendingInstallsFragment fragment) {
        mContext = context;

        /*ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if(installation == null) {
            return;
        }*/
        mFragment = fragment;
        updateOfferList();
    }

    public void updateOfferList() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> offers = UsedOffer.getPendingOffers(mContext);
                    mOffers = offers;
                    SimpleDelayHandler handler = SimpleDelayHandler.getInstance(mContext);
                    handler.startDelayed(PendingInstallsAdapter.this, 0, true);
                } catch (ParseException e) {
                    Crashlytics.logException(e);
                }
            }
        });
        thread.start();
        notifyDataSetChanged();
    }

    @Override
    public void handleDelayedHandlerCallback() {
        if(mOffers != null && mOffers.size() > 0) {
            mFragment.setEmptyViewVisibility(View.GONE);
        } else {
            mFragment.setEmptyViewVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
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
