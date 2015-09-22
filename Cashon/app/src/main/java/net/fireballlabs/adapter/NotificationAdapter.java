package net.fireballlabs.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.NotificationHelper;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.ui.NotificationFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Rohit on 6/16/2015.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> implements View.OnClickListener {
    private final NotificationFragment mFragment;
    Context mContext;
    List<NotificationHelper.Notification> mNotifications;

    public void addNotifications(List<NotificationHelper.Notification> notifications) {
        if(mNotifications == null) {
            mNotifications = new ArrayList<NotificationHelper.Notification>();
        } else {
            mNotifications.clear();
        }
        mNotifications.addAll(notifications);
        notifyDataSetChanged();
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.notification_list_item, parent, false);
        TextView amount = (TextView) v.findViewById(R.id.notification_list_item_amount_text_view);
        TextView extra = (TextView) v.findViewById(R.id.notification_list_item_extra_text_view);
        TextView message = (TextView) v.findViewById(R.id.notification_list_item_message_text_view);
        ViewHolder holder = new ViewHolder(v, amount, extra, message, type);

        return holder;
    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.ViewHolder holder, final int position) {
        NotificationHelper.Notification notification = mNotifications.get(position);
        holder.setTextViewAmountText(Constants.INR_LABEL + String.valueOf(notification.getAmount()));
        holder.setTextViewMessageText(notification.getMessage());
        holder.setTextViewExtraText(notification.getExtra());
    }

    @Override
    public int getItemViewType(int position) {
        return mNotifications.get(position).getCode();
    }

    @Override
    public int getItemCount() {
        return mNotifications == null ? 0 : mNotifications.size();
    }

    public NotificationAdapter(Context context, NotificationFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    @Override
    public void onClick(View view) {
        int type = (Integer)view.getTag();
        if(type == Constants.PUSH_NOTIFICATION_RECHARGE_DONE) {
            mFragment.selectFeature(Constants.ID_APP_RECHARGE);
        } else if(type == Constants.PUSH_NOTIFICATION_INSTALL_CONVERSION) {
            mFragment.selectFeature(Constants.ID_APP_INSTALLS);
        } else if(type == Constants.PUSH_NOTIFICATION_REFERRAL) {
            mFragment.selectFeature(Constants.ID_APP_REFER);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final int type;
        TextView textViewExtra = null;
        TextView textViewMessage;
        TextView textViewAmount = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewAmount, TextView textViewExtra,
                          TextView textViewMessage, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewExtra = textViewExtra;
            this.textViewMessage = textViewMessage;
            this.textViewAmount = textViewAmount;
            this.type = type;

            if(type == Constants.PUSH_NOTIFICATION_RECHARGE_DONE) {
                this.textViewAmount.setBackgroundColor(mContext.getResources().getColor(R.color.material_color_red));
                this.textViewExtra.setVisibility(View.VISIBLE);
            } else if(type == Constants.PUSH_NOTIFICATION_INSTALL_CONVERSION) {
                this.textViewAmount.setBackgroundColor(mContext.getResources().getColor(R.color.material_color_teal));
                this.textViewExtra.setVisibility(View.VISIBLE);
            } else if(type == Constants.PUSH_NOTIFICATION_REFERRAL) {
                this.textViewAmount.setBackgroundColor(mContext.getResources().getColor(R.color.material_color_teal));
                this.textViewExtra.setVisibility(View.GONE);
            }

            this.parent.setTag(type);
            this.parent.setOnClickListener(NotificationAdapter.this);
        }


        public TextView getTextViewExtra() {
            return textViewExtra;
        }

        public void setTextViewExtraText(String text) {
            if(this.textViewExtra != null) {
                if(text != null) {
                    try {
                        if(type == Constants.PUSH_NOTIFICATION_INSTALL_CONVERSION) {
                            if(Offer.getAllOffers(null) != null) {
                                Offer offer = Offer.getOffer(text);
                                if(offer != null) {
                                    this.textViewExtra.setText(offer.getTitle());
                                } else {
                                    this.textViewExtra.setVisibility(View.GONE);
                                }
                            } else {
                                this.textViewExtra.setVisibility(View.GONE);
                            }
                        } else {
                            this.textViewExtra.setText(text);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.textViewExtra.setVisibility(View.GONE);
                }
            }
        }

        public TextView getTextViewMessage() {
            return textViewMessage;
        }

        public void setTextViewMessageText(String text) {
            if(this.textViewMessage != null) {
                this.textViewMessage.setText(text);
            }
        }

        public TextView getTextViewAmount() {
            return textViewAmount;
        }

        public void setTextViewAmountText(String text) {
            if(this.textViewAmount != null) {
                this.textViewAmount.setText(text);
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
