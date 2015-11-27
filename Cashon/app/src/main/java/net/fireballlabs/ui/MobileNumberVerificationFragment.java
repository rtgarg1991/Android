package net.fireballlabs.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
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

import com.appsflyer.AppsFlyerLib;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.ParseConstants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.UserHelper;
import net.fireballlabs.impl.Utility;

import java.util.HashMap;

public class MobileNumberVerificationFragment extends BaseFragment implements View.OnClickListener {

    public static final String VERIFICATION_DONE = "Verification Done!";
    public static final String PHONE_SUCCESSFULLY_VERIFIED = "Your Phone is successfully verified";
    public static final String WRONG_OTP = "Provided OTP is wrong.";
    public static final String DONE = "Done";
    private static MainActivityCallBacks mCallBacks;

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
        sendVerificationMessage(user.getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
        return rootView;
    }

    private void editMobileNumber() {
        String mobile = mMobileEditText.getText().toString();
        if(Utility.isValidMobile(mobile)) {
            user.setUsername(mobile);
            user.setPassword(mobile);
            user.put(UserHelper.PARSE_TABLE_COLUMN_MOBILE, mobile);
            user.saveEventually();
            sendVerificationMessage(user.getObjectId(), ParseInstallation.getCurrentInstallation().getString(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));
        }
    }

    private void sendVerificationMessage(String userId, String deviceId) {
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

        sendMessage(userId, deviceId);
    }

    private void sendMessage(String userId, String deviceId) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("deviceId", deviceId);

        ParseCloud.callFunctionInBackground(ParseConstants.FUNCTION_SEND_OTP, params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean success, ParseException e) {
                if (e == null) {
                    // No use for now
                } else {
                    ParseCloud.callFunctionInBackground(ParseConstants.FUNCTION_SEND_OTP, params);
                }
            }
        });
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
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", user.getObjectId());
        params.put("deviceId", ParseInstallation.getCurrentInstallation().get(InstallationHelper.PARSE_TABLE_COLUMN_DEVICE_ID));

        if(sender != null) {
            if (sender.length() > OTP_SENDER_ID.length() && OTP_SENDER_ID.equals(sender.substring(sender.length() - OTP_SENDER_ID.length()))) {
                params.put("message", message);
            }
        } else {
            params.put("otp", message);
        }

        ParseCloud.callFunctionInBackground(ParseConstants.FUNCTION_VERIFY_OTP, params, new FunctionCallback<Boolean>() {
            @Override
            public void done(Boolean success, ParseException e) {
                if(isValidContext(getActivity())) {
                    if (e == null) {
                        if (success) {
                            if (isVisible()) {
                                Toast.makeText(getActivity(), "Your mobile number is verified.", Toast.LENGTH_SHORT).show();
                            }
                            PreferenceManager.setDefaultSharedPreferenceValue(getActivity(), Constants.PREF_MOBILE_VERIFIED, Context.MODE_PRIVATE, true);
                            Utility.showInformativeDialog(new Utility.DialogCallback() {
                                @Override
                                public void onDialogCallback(boolean success) {
                                    if (mCallBacks != null) {
                                        mCallBacks.setFragment(Constants.ID_APP_INSTALLS, null);
                                    }
                                }
                            }, getActivity(), VERIFICATION_DONE, PHONE_SUCCESSFULLY_VERIFIED, DONE, true);

                            HashMap<String, Object> map2 = new HashMap<String, Object>();
                            map2.put("verified", true);
                            AppsFlyerLib.trackEvent(getActivity(), "af_verified_mobile", map2);
                        } else {
                            Utility.showInformativeDialog(new Utility.DialogCallback() {
                                @Override
                                public void onDialogCallback(boolean success) {
                                    //mCallBacks.setFragment(Constants.ID_APP_INSTALLS, null);
                                }
                            }, getActivity(), null, WRONG_OTP, DONE, true);
                        }
                    } else {
                        Utility.showInformativeDialog(new Utility.DialogCallback() {
                            @Override
                            public void onDialogCallback(boolean success) {
                                //mCallBacks.setFragment(Constants.ID_APP_INSTALLS, null);
                            }
                        }, getActivity(), null, WRONG_OTP, DONE, true);
                    }
                }
            }
        });
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
