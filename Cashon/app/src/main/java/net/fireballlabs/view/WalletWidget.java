package net.fireballlabs.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.fireballlabs.cashguru.R;

/**
 * Created by Rohit on 7/4/2015.
 */
public class WalletWidget extends RelativeLayout {
    private TextView mWalletTextView;

    public WalletWidget(Context context) {
        this(context, null);
    }

    public WalletWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WalletWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateWalletBalance(final String currency, final float i) {
        if(mWalletTextView == null) {
            mWalletTextView = (TextView)findViewById(R.id.wallet_widget_text_view);
        }
        if(mWalletTextView != null) {
            mWalletTextView.post(new Runnable() {
                @Override
                public void run() {
                    mWalletTextView.setText(" " + currency + " " + String.valueOf(i));
                }
            });
        }
    }
}
