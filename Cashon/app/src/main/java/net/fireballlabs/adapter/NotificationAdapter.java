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
        TextView title = (TextView) v.findViewById(R.id.notification_list_item_type_text_view);
        TextView message = (TextView) v.findViewById(R.id.notification_list_item_message_text_view);
        ViewHolder holder = new ViewHolder(v, title, message, type);

        return holder;
    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.ViewHolder holder, final int position) {
        NotificationHelper.Notification notification = mNotifications.get(position);
        holder.setTextViewMessageText(notification.getMessage());

        switch(notification.getCode()) {
            case Constants.PUSH_NOTIFICATION_RECHARGE_DONE:
                holder.setTextViewTitleText("Recharge Successful");
                break;
            case Constants.PUSH_NOTIFICATION_REFERRAL:
                holder.setTextViewTitleText("Referral Credited");
                break;
            case Constants.PUSH_NOTIFICATION_INSTALL_CONVERSION:
                holder.setTextViewTitleText("Credit");
                break;
        }
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
        private final TextView textViewTitle;
        TextView textViewMessage;
        View parent;

        public ViewHolder(View itemView, TextView textViewTitle, TextView textViewMessage, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewMessage = textViewMessage;
            this.textViewTitle = textViewTitle;
            this.type = type;

            this.parent.setTag(type);
            this.parent.setOnClickListener(NotificationAdapter.this);
        }

        public TextView getTextViewTitle() {
            return textViewTitle;
        }

        public void setTextViewTitleText(String text) {
            if(this.textViewTitle != null) {
                this.textViewTitle.setText(text);
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

        public View getParent() {
            return parent;
        }

        public void setParent(View parent) {
            this.parent = parent;
        }
    }
}
