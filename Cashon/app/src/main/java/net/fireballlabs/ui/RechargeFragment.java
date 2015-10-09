package net.fireballlabs.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.Conversions;
import net.fireballlabs.helper.model.Recharge;
import net.fireballlabs.impl.Utility;

import com.crashlytics.android.Crashlytics;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;


public class RechargeFragment extends Fragment implements Utility.DialogCallback {

    public static String PARSE_TABLE_COLUMN_USER_ID = "userId";

    public static String TYPE_MOBILE = "Mobile";
    public static String TYPE_DATA_CARD = "DataCard";
    public static String TYPE_DTH = "DTH";
    private static MainActivityCallBacks mCallBacks;
    private float balance = 0f;

    public static RechargeFragment newInstance(String title, MainActivityCallBacks callBacks) {
        RechargeFragment fragment = new RechargeFragment();
        mCallBacks = callBacks;
        return fragment;
    }

    public RechargeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_recharge, container, false);

        /*RadioGroup typeRadioGroup = (RadioGroup)view.findViewById(R.id.recharge_radio_group_type);
        final RadioGroup prePostRadioGroup = (RadioGroup)view.findViewById(R.id.recharge_radio_group_pre_post);

        final RadioButton radioButtonMobile = (RadioButton)view.findViewById(R.id.recharge_radio_button_mobile);
        final RadioButton radioButtonDTH = (RadioButton)view.findViewById(R.id.recharge_radio_button_dth);
        final RadioButton radioButtonDatacard = (RadioButton)view.findViewById(R.id.recharge_radio_button_data_card);

        RadioButton radioButtonPostpaid = (RadioButton)view.findViewById(R.id.recharge_radio_button_postpaid);
        final RadioButton radioButtonPrepaid = (RadioButton)view.findViewById(R.id.recharge_radio_button_prepaid);*/

        final Spinner spinnerCompanyName = (Spinner)view.findViewById(R.id.recharge_spinner_company);
        final Spinner spinnerCircleName = (Spinner)view.findViewById(R.id.recharge_spinner_circle);

        final EditText editTextNumber = (EditText)view.findViewById(R.id.recharge_mobile_number);
        final EditText editTextAmount = (EditText)view.findViewById(R.id.recharge_amount);
        final EditText editTextMessage = (EditText)view.findViewById(R.id.recharge_message);

        final TextView balanceTextView = (TextView)view.findViewById(R.id.recharge_edit_text_balance);
        final CheckBox specialRechargeCheckBox = (CheckBox)view.findViewById(R.id.recharge_check_box_special_recharge);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                balance = Conversions.getBalance(getActivity(), true);
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            balanceTextView.setText(Constants.INR_LABEL + String.valueOf(balance));
                        }
                    });
                }
            }
        });
        thread.start();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.recharge_mobile_prepaid_company, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerCompanyName.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterCircle = ArrayAdapter.createFromResource(getActivity(),
                R.array.recharge_mobile_circle, R.layout.simple_spinner_item);
        adapterCircle.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerCircleName.setAdapter(adapterCircle);

        specialRechargeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editTextMessage.setVisibility(View.VISIBLE);
                } else {
                    editTextMessage.setVisibility(View.GONE);
                }
            }
        });

        Button sendButton = (Button)view.findViewById(R.id.recharge_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextAmount.setError(null);
                editTextNumber.setError(null);

                if ("".equals(editTextAmount.getText().toString())) {
                    editTextAmount.setError("Amount cannot be empty");
                } else if ("".equals(editTextNumber.getText().toString())) {
                    editTextNumber.setError("Number cannot be empty");
                } else if (Utility.isValidRechargeAmount(editTextAmount.getText().toString(), balance)) {
                    ParseUser user = ParseUser.getCurrentUser();
                    if (user == null || !user.isAuthenticated()) {
                        // TODO track this and noify user that he/she has to login
                        return;
                    }
                    if (Utility.isValidMobile(editTextNumber.getText().toString())) {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put(Recharge.PARSE_TABLE_COLUMN_AMOUNT, editTextAmount.getText().toString());
                        params.put(Recharge.PARSE_TABLE_COLUMN_COMMENT, editTextMessage.getText().toString());
                        params.put(Recharge.PARSE_TABLE_COLUMN_NUMBER, editTextNumber.getText().toString());
                        params.put(Recharge.PARSE_TABLE_COLUMN_COMPANY, spinnerCompanyName.getSelectedItem().toString());
                        params.put(Recharge.PARSE_TABLE_COLUMN_CIRCLE, spinnerCircleName.getSelectedItem().toString());
                        params.put(PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
                        params.put(Recharge.PARSE_TABLE_COLUMN_PREPAID, true);
                        params.put(Recharge.PARSE_TABLE_COLUMN_TYPE, TYPE_MOBILE);


                        ParseCloud.callFunctionInBackground("addNewRechargeRequest", params, new FunctionCallback<Boolean>() {
                            public void done(Boolean success, ParseException e) {
                                if (e == null) {
                                    if(success) {
                                        Utility.showInformativeDialog(new Utility.DialogCallback() {
                                                      @Override
                                                      public void onDialogCallback(boolean success) {
                                                          mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS
                                                                  , Constants.ID_APP_INSTALLS, R.drawable.offerwall), null);
                                                      }
                                                  }, getActivity(), "Recharge Sent Successfully",
                                            "Recharge request sent successfully, and it will be processed within 48 hours",
                                            "OK", true);
                                        Recharge.clearRechargeHistory();
                                    } else {
                                        Utility.showInformativeDialog(new Utility.DialogCallback() {
                                                      @Override
                                                      public void onDialogCallback(boolean success) {
                                                          mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS
                                                                  , Constants.ID_APP_INSTALLS, R.drawable.offerwall), null);
                                                      }
                                                  }, getActivity(), "Recharge Not Sent",
                                            "Recharge request not sent, we already have one pending request for Recharge from your id",
                                            "OK", true);
                                    }
                                } else {
                                    Logger.doSecureLogging(Log.WARN, "Recharge request not sent successfully." + e.getCode());
                                    Crashlytics.logException(e);
                                }
                            }
                        });

                        if(isVisible()) {
                            Utility.showInformativeDialog(RechargeFragment.this, getActivity(), null, Constants.RECHARGE_SENT_SUCCESSFUL, null, false);
                        }
                    } else {
                        editTextNumber.setError("Number Not valid");
                    }
                } else {
                    editTextAmount.setError("Recharge Amount Should be in range " + Constants.RECHARGE_AMOUNT_MIN + " to " + Constants.RECHARGE_AMOUNT_MAX
                            + " and should be less than available balance credits");
                }
            }
        });
        return view;
    }


    @Override
    public void onDialogCallback(boolean success) {
        mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_RECHARGE, Constants.ID_APP_RECHARGE, R.drawable.topup), null);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showInformativeDialog(null, null, null, null, null, false);
    }
}
