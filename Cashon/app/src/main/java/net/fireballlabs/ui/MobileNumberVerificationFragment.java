package net.fireballlabs.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.UserHelper;
import net.fireballlabs.impl.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MobileNumberVerificationFragment extends Fragment implements View.OnClickListener {

    public static final String VERIFICATION_DONE = "Verification Done!";
    public static final String PHONE_SUCCESSFULLY_VERIFIED = "Your Phone is successfully verified";
    public static final String WRONG_OTP = "Provided OTP is wrong.";
    public static final String DONE = "Done";
    private static MainActivityCallBacks mCallBacks;
    private boolean mDetatched;

    static MobileNumberVerificationFragment fr;


    private final static String OTP_SENDER_ID = "DPAMSG";
    private EditText mOtpEditText;
    private Button mOtpVerificationButton;
    private LinearLayout mEditLayout;
    private ProgressBar mProgressBar;
    private EditText mMobileEditText;
    private ImageButton mMobileSubmitButton;
    private TextView mProgressTextView;
    private ParseUser user;

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

        mOtpEditText = (EditText)rootView.findViewById(R.id.mobile_verification_otp_edit_text);
        mOtpVerificationButton = (Button) rootView.findViewById(R.id.mobile_verification_submit_button);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.mobile_verification_progress_bar);
        mEditLayout = (LinearLayout)rootView.findViewById(R.id.mobile_verification_edit_layout);
        mMobileEditText = (EditText)rootView.findViewById(R.id.mobile_verification_mobile_number_edit_text);
        mMobileSubmitButton = (ImageButton)rootView.findViewById(R.id.mobile_verification_mobile_number_edit_button);
        mProgressTextView = (TextView)rootView.findViewById(R.id.mobile_verification_progress_text_view);

        mOtpVerificationButton.setOnClickListener(this);
        mMobileSubmitButton.setOnClickListener(this);

        user = ParseUser.getCurrentUser();

        mMobileEditText.setText(user.getUsername());
        sendVerificationMessage();
        return rootView;
    }

    private void editMobileNumber() {
        String mobile = mMobileEditText.getText().toString();
        if(Utility.isValidMobile(mobile)) {
            user.setUsername(mobile);
            user.setPassword(mobile);
            user.put(UserHelper.PARSE_TABLE_COLUMN_MOBILE, mobile);
            user.saveEventually();
            sendVerificationMessage();
        }
    }

    private void sendVerificationMessage() {
        mEditLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressTextView.setVisibility(View.VISIBLE);

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                if(isVisible()) {
                    mProgressTextView.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            public void onFinish() {
                mProgressBar.setVisibility(View.GONE);
                mProgressTextView.setVisibility(View.GONE);
                mEditLayout.setVisibility(View.VISIBLE);
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
        String mainUrl = "https://control.msg91.com/sendhttp.php?";

        //Prepare parameter string
        StringBuilder sbPostData = new StringBuilder(mainUrl);
        sbPostData.append("authkey="+authKey);
        sbPostData.append("&mobiles="+mobile);
        sbPostData.append("&message="+encoded_message);
        sbPostData.append("&route="+route);
        sbPostData.append("&sender="+senderId);
        sbPostData.append("&campaign="+"CashGuru");

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

            verifyNumber(sender, message);
        }
    };

    private void verifyNumber(String sender, String message) {
        String tempMessage = null;
        if(sender != null) {
            if (sender.length() > OTP_SENDER_ID.length() && OTP_SENDER_ID.equals(sender.substring(sender.length() - OTP_SENDER_ID.length()))) {
                tempMessage = String.format(Constants.OTP_MESSAGE, generateOTP());
            }
        } else {
            tempMessage = generateOTP();

        }
        if (tempMessage != null && tempMessage.equals(message)) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("MobileVerifications");
            query.whereEqualTo(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID, ParseInstallation.getCurrentInstallation().get(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
            query.whereEqualTo(InstallationHelper.PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
            try {
                ParseObject obj = query.getFirst();
                obj.put("verified", true);
                obj.saveEventually();
                PreferenceManager.setDefaultSharedPreferenceValue(getActivity(), Constants.PREF_MOBILE_VERIFIED, Context.MODE_PRIVATE, true);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (isVisible()) {
                Toast.makeText(getActivity(), "Your mobile number is verified.", Toast.LENGTH_SHORT).show();
            }
            Utility.showInformativeDialog(new Utility.DialogCallback() {
                @Override
                public void onDialogCallback(boolean success) {
                    if (mCallBacks != null) {
                        mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS, Constants.ID_APP_INSTALLS, R.drawable.offerwall), null);
                    }
                }
            }, getActivity(), VERIFICATION_DONE, PHONE_SUCCESSFULLY_VERIFIED, DONE, true);

        } else if(sender == null) {

            Utility.showInformativeDialog(new Utility.DialogCallback() {
                @Override
                public void onDialogCallback(boolean success) {
                    //mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS, Constants.ID_APP_INSTALLS, R.drawable.offerwall));
                }
            }, getActivity(), null, WRONG_OTP, DONE, true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showInformativeDialog(null, null, null, null, null, false);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mobile_verification_submit_button:
                verifyNumber(null, mOtpEditText.getText().toString());
                break;
            case R.id.mobile_verification_mobile_number_edit_button:
                if(mMobileEditText.isEnabled()) {
                    editMobileNumber();
                    mMobileEditText.setEnabled(false);
                    mMobileSubmitButton.setImageResource(R.drawable.mode_edit);
                } else {
                    mMobileEditText.setEnabled(true);
                    mMobileSubmitButton.setImageResource(R.drawable.check);
                }
                break;
        }
    }
}
