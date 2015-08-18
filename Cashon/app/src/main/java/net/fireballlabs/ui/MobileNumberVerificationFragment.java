package net.fireballlabs.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseUser;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.UserHelper;
import net.fireballlabs.impl.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MobileNumberVerificationFragment extends Fragment {

    private static MainActivityCallBacks mCallBacks;
    private boolean mDetatched;

    static MobileNumberVerificationFragment fr;

    private EditText mNameEditText;
    private ProgressBar mMobileVerificationProgressWheel;
    private Button mMobileVerificationEditButton;
    private Button mMobileVerificationResendButton;
    private TextView mMobileVerificationTickTextView;
    private LinearLayout mMobileVerificationUpdateLayout;

    private final static String OTP_SENDER_ID = "Cashgu";

    public static MobileNumberVerificationFragment newInstance(MainActivityCallBacks callBacks) {
        MobileNumberVerificationFragment fragment = new MobileNumberVerificationFragment();
        mCallBacks = callBacks;
        fr = fragment;
        return fragment;
    }

    public MobileNumberVerificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_mobile_verification, container, false);
        final TextView mobileVerificationTextView = (TextView)rootView.findViewById(R.id.mobile_verification_text_view);
        final EditText mobileVerificationMobileNumberEditText = (EditText)rootView.findViewById(R.id.mobile_verification_mobile_number);
        Button mobileVerificationUpdateButton = (Button)rootView.findViewById(R.id.mobile_verification_update_button);

        mMobileVerificationUpdateLayout = (LinearLayout)rootView.findViewById(R.id.mobile_verification_update_layout);
        mMobileVerificationProgressWheel = (ProgressBar)rootView.findViewById(R.id.mobile_verification_progress_bar);
        mMobileVerificationEditButton = (Button)rootView.findViewById(R.id.mobile_verification_edit_button);
        mMobileVerificationResendButton = (Button)rootView.findViewById(R.id.mobile_verification_resend_button);
        mMobileVerificationTickTextView = (TextView)rootView.findViewById(R.id.mobile_verification_tick_text_view);
        final ParseUser user = ParseUser.getCurrentUser();

        mobileVerificationTextView.setText("We have sent verification SMS to " + user.getUsername() + " and we will verify automatically");

        mMobileVerificationEditButton.setVisibility(View.GONE);
        mMobileVerificationResendButton.setVisibility(View.GONE);
        mMobileVerificationEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMobileVerificationUpdateLayout.setVisibility(View.VISIBLE);
                mobileVerificationMobileNumberEditText.setText(user.getUsername());
            }
        });

        mMobileVerificationResendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationMessage();
            }
        });

        mobileVerificationUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mobileVerificationMobileNumberEditText.getText().toString();
                if(Utility.isValidMobile(mobile)) {
                    user.setUsername(mobile);
                    user.setPassword(mobile);
                    user.put(UserHelper.PARSE_TABLE_COLUMN_MOBILE, mobile);
                    user.saveEventually();
                    mobileVerificationTextView.setText("We have sent verification SMS to " + user.getUsername() + " and we will verify automatically");
                    sendVerificationMessage();
                }
            }
        });
        sendVerificationMessage();
        return rootView;
    }

    private void sendVerificationMessage() {
        mMobileVerificationEditButton.setVisibility(View.GONE);
        mMobileVerificationResendButton.setVisibility(View.GONE);
        mMobileVerificationUpdateLayout.setVisibility(View.GONE);

        mMobileVerificationTickTextView.setText(String.valueOf(6000 / 10));
        mMobileVerificationProgressWheel.setVisibility(View.VISIBLE);
        mMobileVerificationTickTextView.setVisibility(View.VISIBLE);
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(isVisible()) {
                    mMobileVerificationTickTextView.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            public void onFinish() {
                mMobileVerificationEditButton.setVisibility(View.VISIBLE);
                mMobileVerificationResendButton.setVisibility(View.VISIBLE);
                mMobileVerificationProgressWheel.clearAnimation();
                mMobileVerificationProgressWheel.setVisibility(View.GONE);
                mMobileVerificationTickTextView.setVisibility(View.GONE);
            }
        }.start();

        sendMessage();
    }

    private void sendMessage() {
        //Your authentication key
        String authKey = getActivity().getResources().getString(R.string.sms_api_key);
            //Multiple mobiles numbers separated by comma
        String mobile = ParseUser.getCurrentUser().getUsername();
        //Sender ID,While using route4 sender id should be 6 characters long.
        String senderId = OTP_SENDER_ID;
        //Your message to send, Add URL encoding here.
        String message = String.format(Constants.OTP_MESSAGE, generateOTP());
        //define route
        String route = "4";

        //encoding message
        String encoded_message = URLEncoder.encode(message);

        //Send SMS API
        String mainUrl = "http://sms.getsetlive.com/sendhttp.php?";

        //Prepare parameter string
        StringBuilder sbPostData = new StringBuilder(mainUrl);
        sbPostData.append("authkey="+authKey);
        sbPostData.append("&mobiles="+mobile);
        sbPostData.append("&message="+encoded_message);
        sbPostData.append("&route="+route);
        sbPostData.append("&sender="+senderId);

        //final string
        mainUrl = sbPostData.toString();
        final String finalMainUrl = mainUrl;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //prepare connection
                    URL myURL = new URL(finalMainUrl);
                    URLConnection myURLConnection = myURL.openConnection();
                    myURLConnection.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));

                    //reading response
                    String response;
                    while ((response = reader.readLine()) != null) {
                        Logger.doSecureLogging(Log.INFO, response);
                    }

                    reader.close();
                }
                catch (IOException e) {
                    Crashlytics.logException(e);
                }
            }
        });
        thread.start();
    }

    BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] pdus = (Object[])intent.getExtras().get("pdus");
            SmsMessage shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);

            String sender = shortMessage.getOriginatingAddress();
            String message = shortMessage.getDisplayMessageBody();

            if(sender.length() > OTP_SENDER_ID.length() && OTP_SENDER_ID.equals(sender.substring(sender.length() - OTP_SENDER_ID.length()))) {
                String tempMessage = String.format(Constants.OTP_MESSAGE, generateOTP());
                if(tempMessage.equals(message)) {
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put(UserHelper.PARSE_TABLE_COLUMN_MOBILE_VERIFIED, true);
                    user.saveEventually();
                    if(isVisible()) {
                        Toast.makeText(getActivity(), "Your mobile number is verified.", Toast.LENGTH_SHORT).show();
                    }
                    mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS, Constants.ID_APP_INSTALLS, R.drawable.offerwall));
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(smsReceiver);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDetatched = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetatched = true;
    }

    public void showProgress(boolean show) {
        if(isAdded() && !mDetatched) {
            Utility.showProgress(getActivity(), show, String.valueOf(getResources().getText(R.string.please_wait)));
        }
    }

    public String generateOTP() {
        ParseUser user = ParseUser.getCurrentUser();
        String uid = user.getObjectId();

        int a = 0, b = 0;

        for(int i = 0; i < (uid.length() + 1) / 2; i++) {
            a += uid.charAt(i);
            b += uid.charAt(uid.length() - 1 - i);
        }
        a = a % 100;
        b = b % 100;
        return String.valueOf(a) + String.valueOf(b);
    }
}
