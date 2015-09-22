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

import java.util.ArrayList;
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
        TextView company = (TextView) v.findViewById(R.id.recharge_history_list_item_company_text_view);
        TextView amount = (TextView) v.findViewById(R.id.recharge_history_list_item_amount_text_view);
        TextView reference = (TextView) v.findViewById(R.id.recharge_history_list_item_reference_text_view);
        TextView referenceNumber = (TextView) v.findViewById(R.id.recharge_history_list_item_reference_number_text_view);
        ImageView image = (ImageView) v.findViewById(R.id.recharge_history_list_item_type_image_view);
        ViewHolder holder = new ViewHolder(v, number, company, amount, reference, referenceNumber, image, type);

        return holder;
    }

    @Override
    public void onBindViewHolder(final RechargeHistoryAdapter.ViewHolder holder, final int position) {
        Recharge recharge = mRecharges.get(position);
        holder.setTextViewAmountText(Constants.INR_LABEL + String.valueOf(recharge.getAmount()));
        holder.setTextViewCompanyText(recharge.getCompany());
        holder.setTextViewNumberText(recharge.getNumber());
        holder.setTextViewReferenceText(recharge.getReferenceNumber());
        holder.setImageView(R.drawable.mobile);
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
        TextView textViewCompany;
        TextView textViewAmount = null;
        TextView textViewReference = null;
        TextView textViewReferenceNumber = null;
        Button clickButton = null;
        ImageView imageView = null;
        View parent;

        public ViewHolder(View itemView, TextView textViewNumber, TextView textViewCompany, TextView textViewAmount,
                          TextView textViewReference, TextView textViewReferenceNumber,
                          ImageView imageView, int type) {
            super(itemView);
            this.parent = itemView;
            this.textViewNumber = textViewNumber;
            this.textViewCompany = textViewCompany;
            this.textViewAmount = textViewAmount;
            this.textViewReference = textViewReference;
            this.textViewReferenceNumber = textViewReferenceNumber;
            this.imageView = imageView;
            this.type = type;

            if(type == 1) {
                this.textViewAmount.setBackgroundColor(mContext.getResources().getColor(R.color.material_color_teal));
                this.textViewReference.setVisibility(View.GONE);
            } else {
                this.textViewAmount.setBackgroundColor(mContext.getResources().getColor(R.color.material_color_red));
                this.textViewReferenceNumber.setVisibility(View.GONE);
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

        public TextView getTextViewCompany() {
            return textViewCompany;
        }

        public void setTextViewCompanyText(String text) {
            if(this.textViewCompany != null) {
                this.textViewCompany.setText(text);
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

        public TextView getTextViewReference() {
            return textViewReference;
        }

        public void setTextViewReferenceText(String text) {
            if(this.textViewReference != null) {
                if(type == 1) {
                    this.textViewReference.setText("Reference :");
                    setTextViewReferenceNumberText(text);
                } else {
                    this.textViewReference.setText("Recharge Pending.");
                }
            }
        }

        public TextView getTextViewReferenceNumber() {
            return textViewReferenceNumber;
        }

        public void setTextViewReferenceNumberText(String text) {
            if(this.textViewReferenceNumber != null) {
                this.textViewReferenceNumber.setText(text);
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
