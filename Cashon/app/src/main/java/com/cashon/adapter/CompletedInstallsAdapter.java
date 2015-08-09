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
import com.cashon.impl.Utility;
import com.cashon.sql.SQLWrapper;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rohit on 6/16/2015.
 */
public class CompletedInstallsAdapter extends RecyclerView.Adapter<CompletedInstallsAdapter.ViewHolder> implements SimpleDelayHandler.SimpleDelayHandlerCallback {
    Context mContext;
    List<Offer> mOffers;

    @Override
    public CompletedInstallsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = null;
        if(type == 1) {
            v = vi.inflate(R.layout.app_install_list_item_1, parent, false);
            TextView title = (TextView) v.findViewById(R.id.app_install_list_item_title);
            TextView payout = (TextView) v.findViewById(R.id.app_install_list_item_payout);
            TextView description = (TextView) v.findViewById(R.id.app_install_list_item_description);
//            TextView payoutDescription = (TextView) v.findViewById(R.id.app_install_list_item_payout_description);
            ImageView image = (ImageView) v.findViewById(R.id.app_install_list_item_image_view);
            Button button = (Button) v.findViewById(R.id.app_install_list_item_Button);
            ViewHolder holder = new ViewHolder(v, title, payout, description, image, button);

            return holder;
        } else {
            // TODO need to check if we need multiple type of offers
        }
        return null;
    }

    @Override
    public void onBindViewHolder(CompletedInstallsAdapter.ViewHolder holder, final int position) {
        holder.setTextViewTitleText(mOffers.get(position).getTitle());
        holder.setTextViewPayoutText(String.valueOf(mOffers.get(position).getPayout()));
        holder.setTextViewDescriptionText("ABCD");

        /*Picasso.with(mContext)
                .load(AppInstallsFragment.SERVER_IMAGES_ROOT_ADDRESS
                        + mOffers.get(position).images.xhdpi)
                .into(holder.imageView);
        holder.clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLWrapper.Offer.updateInstallTry(mOffers.get(position).id);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mOffers.get(position).affLink));
                mContext.startActivity(browserIntent);
            }
        });*/
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

    public CompletedInstallsAdapter(final Context context) {
        mContext = context;

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if(installation == null) {
            return;
        }

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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle = null;
        TextView textViewPayout = null;
        TextView textViewDescription = null;
        TextView textViewPayoutDescription = null;
        Button clickButton = null;
        ImageView imageView = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewPayout,
                          TextView textViewDescription, ImageView imageView, Button clickButton) {
            this(itemView, textViewTitle, textViewPayout, textViewDescription, null, imageView, clickButton);
        }

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewPayout,
                          TextView textViewDescription, TextView textViewPayoutDescription,
                          ImageView imageView, Button clickButton) {
            super(itemView);
            this.parent = itemView;
            this.textViewTitle = textViewTitle;
            this.textViewPayout = textViewPayout;
            this.textViewDescription = textViewDescription;
            this.textViewPayoutDescription = textViewPayoutDescription;
            this.imageView = imageView;
            this.clickButton = clickButton;

            this.clickButton.setVisibility(View.GONE);
//            this.textViewPayoutDescription.setVisibility(View.GONE);
        }


        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public void setTextViewTitleText(String text) {
            if(this.textViewTitle != null) {
                this.textViewTitle.setText(text);
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
