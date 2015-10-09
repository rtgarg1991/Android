package net.fireballlabs.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.fireballlabs.cashguru.R;
import net.fireballlabs.impl.SimpleDelayHandler;


/**
 * Created by Rohit on 7/4/2015.
 */
public class WalletWidget extends RelativeLayout implements SimpleDelayHandler.SimpleDelayHandlerCallback {
    private TextView mWalletTextView;
    private static float mAmount;
    private static String mCurrency;

    public WalletWidget(Context context) {
        this(context, null);
    }

    public WalletWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WalletWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateWalletBalance(final String currency, final float i, Context context) {
        if(mWalletTextView == null) {
            mWalletTextView = (TextView)findViewById(R.id.wallet_widget_text_view);
        }
        if(mWalletTextView != null) {
            mAmount = i;
            mCurrency = currency;
            SimpleDelayHandler.getInstance(context).startDelayed(this, 0, true);
        }
    }

    @Override
    public void handleDelayedHandlerCallback() {
        mWalletTextView.setText(" " + mCurrency + " " + String.valueOf(mAmount));
    }
}
