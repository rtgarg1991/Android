package net.fireballlabs.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
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
public class RechargeHistoryAdapter extends RecyclerView.Adapter<RechargeHistoryAdapter.ViewHolder> {
    Context mContext;
    List<Recharge> mRecharges;

    public void addRechargeHistory(List<Recharge> recharges) {
        if(mRecharges == null) {
            mRecharges = new ArrayList<Recharge>();
        } else {
            mRecharges.clear();
        }
        mRecharges.addAll(recharges);
        notifyDataSetChanged();
    }

    @Override
    public RechargeHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.recharge_history_list_item, parent, false);
        TextView number = (TextView) v.findViewById(R.id.recharge_history_list_item_number_text_view);
        TextView amount = (TextView) v.findViewById(R.id.recharge_history_list_item_amount_text_view);
        TextView success = (TextView) v.findViewById(R.id.recharge_history_list_item_success_text_view);
        TextView date = (TextView) v.findViewById(R.id.recharge_history_list_item_date_text_view);
        TextView reference = (TextView) v.findViewById(R.id.recharge_history_list_item_reference_text_view);

        ImageView image = (ImageView) v.findViewById(R.id.recharge_history_list_item_type_image_view);
        ViewHolder holder = new ViewHolder(v, number, success, amount, date, reference, image, type);

        return holder;
    }

    @Override
    public void onBindViewHolder(final RechargeHistoryAdapter.ViewHolder holder, final int position) {
        Recharge recharge = mRecharges.get(position);
        holder.setTextViewAmountText(Constants.RS_TEXT + String.valueOf(recharge.getAmount()));
        holder.setTextViewSuccessText();
        holder.setTextViewNumberText(recharge.getNumber());
        holder.setTextViewDateText(recharge.getRequestedDate());
        holder.setImageView(R.drawable.mobile);
        holder.setTextViewReferenceText(recharge.getReferenceNumber());
    }

    @Override
    public int getItemViewType(int position) {
        if(mRecharges.get(position).isCompleted()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mRecharges == null ? 0 : mRecharges.size();
    }

    public RechargeHistoryAdapter(Context context) {
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final int type;
        TextView textViewNumber = null;
        TextView textViewSuccess = null;
        TextView textViewAmount = null;
        TextView textViewDate = null;
        TextView textViewReference = null;
        Button clickButton = null;
        ImageView imageView = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewNumber, TextView textViewSuccess, TextView textViewAmount,
                          TextView textViewDate, TextView textViewReference, ImageView imageView, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewNumber = textViewNumber;
            this.textViewSuccess = textViewSuccess;
            this.textViewAmount = textViewAmount;
            this.textViewDate = textViewDate;
            this.imageView = imageView;
            this.textViewReference = textViewReference;
            this.type = type;

            if(type == 1) {
                this.textViewAmount.setTextColor(mContext.getResources().getColor(R.color.material_color_teal));
            } else {
                this.textViewAmount.setTextColor(mContext.getResources().getColor(R.color.material_color_red));
                this.textViewReference.setVisibility(View.GONE);
            }
        }

        public TextView getTextViewReference() {
            return textViewReference;
        }

        public void setTextViewReferenceText(String text) {
            if(this.textViewReference != null) {
                if(this.type == 1) {
                    this.textViewReference.setText(this.textViewReference.getText() + text);
                }
            }
        }


        public TextView getTextViewNumber() {
            return textViewNumber;
        }

        public void setTextViewNumberText(String text) {
            if(this.textViewNumber != null) {
                this.textViewNumber.setText(text);
            }
        }

        public TextView getTextViewCSuccess() {
            return textViewSuccess;
        }

        public void setTextViewSuccessText() {
            if(this.textViewSuccess != null) {
                if(this.type == 1) {
                    this.textViewSuccess.setText("Success");
                } else {
                    this.textViewSuccess.setText("Pending");
                }
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

        public TextView getTextViewDate() {
            return textViewDate;
        }

        public void setTextViewDateText(Date requestedDate) {
            if(this.textViewDate != null) {
                Calendar c1 = Calendar.getInstance();
                Calendar c2 = Calendar.getInstance();
                c2.setTime(requestedDate);
                if(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
                    if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
                        this.textViewDate.setText("Today");
                    } else if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) + 1) {
                        this.textViewDate.setText("Yesterday");
                    } else {
                        DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
                        this.textViewDate.setText(format.format(requestedDate));
                    }
                } else {
                    DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
                    this.textViewDate.setText(format.format(requestedDate));
                }
            }
        }

        public ImageView getImageView() {
            return imageView;
        }

        public void setImageView(int id) {
            if(this.imageView != null) {
                this.imageView.setImageResource(id);
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
