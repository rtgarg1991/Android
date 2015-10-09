package net.fireballlabs.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.ContactUs;
import net.fireballlabs.helper.model.Recharge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by Rohit on 6/16/2015.
 */
public class ContactUsHistoryAdapter extends RecyclerView.Adapter<ContactUsHistoryAdapter.ViewHolder> {
    Context mContext;
    List<ContactUs> mContactUs;

    public void addContactUsHistory(List<ContactUs> contactUs) {
        if(mContactUs == null) {
            mContactUs = new ArrayList<ContactUs>();
        } else {
            mContactUs.clear();
        }
        mContactUs.addAll(contactUs);
        notifyDataSetChanged();
    }

    @Override
    public ContactUsHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.contact_us_history_list_item, parent, false);
        TextView in = (TextView) v.findViewById(R.id.contact_us_history_list_item_message_in_text_view);
        TextView out = (TextView) v.findViewById(R.id.contact_us_history_list_item_message_out_text_view);
        TextView inTime = (TextView) v.findViewById(R.id.contact_us_history_list_item_message_in_date_text_view);
        TextView outTime = (TextView) v.findViewById(R.id.contact_us_history_list_item_message_out_date_text_view);

        RelativeLayout inLayout = (RelativeLayout)v.findViewById(R.id.contact_us_history_list_item_message_in_view);

        ViewHolder holder = new ViewHolder(v, in, out, inTime, outTime, inLayout, type);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ContactUsHistoryAdapter.ViewHolder holder, final int position) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
        ContactUs contactUs = mContactUs.get(position);

        if(holder.type == 1) {
            holder.setTextViewInText(contactUs.getReply());
            holder.setTextViewInTimeText(formatter.format(contactUs.getUpdatedAt()));
        }

        holder.setTextViewOutText(contactUs.getMessage());
        holder.setTextViewOutTimeText(formatter.format(contactUs.getCreatedAt()));

    }

    @Override
    public int getItemViewType(int position) {
        if(mContactUs.get(position).isResolved()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mContactUs == null ? 0 : mContactUs.size();
    }

    public ContactUsHistoryAdapter(Context context) {
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewIn = null;
        TextView textViewOut = null;
        TextView textViewInTime = null;
        TextView textViewOutTime = null;
        int type;

        View parent;

        public ViewHolder(View itemView, TextView textViewIn, TextView textViewOut,
                          TextView textViewInTime, TextView textViewOutTime, RelativeLayout inRelativeLayout, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewOut = textViewOut;
            this.textViewIn = textViewIn;
            this.textViewInTime = textViewInTime;
            this.textViewOutTime = textViewOutTime;
            this.type = type;

            if(type == 0) {
                inRelativeLayout.setVisibility(View.GONE);
                this.textViewIn.setVisibility(View.GONE);
                this.textViewInTime.setVisibility(View.GONE);
            }
        }

        public View getParent() {
            return parent;
        }

        public void setParent(View parent) {
            this.parent = parent;
        }
        public TextView getTextViewIn() {
            return textViewIn;
        }

        public void setTextViewInText(String text) {
            if(this.textViewIn != null) {
                this.textViewIn.setText(text);
            }
        }

        public TextView getTextViewOut() {
            return textViewOut;
        }

        public void setTextViewOutText(String text) {
            if(this.textViewOut != null) {
                this.textViewOut.setText(text);
            }
        }
        public TextView getTextViewInTime() {
            return textViewInTime;
        }

        public void setTextViewInTimeText(String text) {
            if(this.textViewInTime != null) {
                this.textViewInTime.setText(text);
            }
        }

        public TextView getTextViewOutTime() {
            return textViewOutTime;
        }

        public void setTextViewOutTimeText(String text) {
            if(this.textViewOutTime != null) {
                this.textViewOutTime.setText(text);
            }
        }
    }
}
