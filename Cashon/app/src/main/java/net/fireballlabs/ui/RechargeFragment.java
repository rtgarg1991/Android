package net.fireballlabs.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.Conversions;
import net.fireballlabs.impl.Utility;

import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class RechargeFragment extends Fragment implements Utility.DialogCallback {

    public static String PARSE_TABLE_NAME_RECHARGE = "Recharge";
    public static String PARSE_TABLE_COLUMN_USER_ID = "userId";
    public static String PARSE_TABLE_COLUMN_EMAIL_ID = "emailId";
    public static String PARSE_TABLE_COLUMN_AMOUNT = "amount";
    public static String PARSE_TABLE_COLUMN_NUMBER = "number";
    public static String PARSE_TABLE_COLUMN_COMPANY = "company";
    public static String PARSE_TABLE_COLUMN_COMMENT = "comment";
    public static String PARSE_TABLE_COLUMN_IS_PREPAID = "isPrepaid";
    public static String PARSE_TABLE_COLUMN_TYPE = "type";

    public static String TYPE_MOBILE = "Mobile";
    public static String TYPE_DATA_CARD = "DataCard";
    public static String TYPE_DTH = "DTH";
    private static MainActivityCallBacks mCallBacks;
    private float balance = 0f;

    public static RechargeFragment newInstance(MainActivityCallBacks callBacks) {
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

        final EditText editTextNumber = (EditText)view.findViewById(R.id.recharge_mobile_number);
        final EditText editTextAmount = (EditText)view.findViewById(R.id.recharge_amount);
        final EditText editTextMessage = (EditText)view.findViewById(R.id.recharge_message);

        final TextView balanceTextView = (TextView)view.findViewById(R.id.recharge_edit_text_balance);
        final CheckBox specialRechargeCheckBox = (CheckBox)view.findViewById(R.id.recharge_check_box_special_recharge);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                balance = Conversions.getBalance();
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

        /*CompoundButton.OnCheckedChangeListener prePostChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    switch (buttonView.getId()) {
                        case R.id.recharge_radio_button_prepaid:
                            if(radioButtonMobile.isChecked()) {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_mobile_prepaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            } else if(radioButtonDatacard.isChecked()) {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_data_card_prepaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            }
                            break;
                        case R.id.recharge_radio_button_postpaid:
                            if(radioButtonMobile.isChecked()) {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_mobile_postpaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            } else if(radioButtonDatacard.isChecked()) {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_data_card_postpaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            }
                            break;
                    }
                }
            }
        };


        CompoundButton.OnCheckedChangeListener typeChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    switch (buttonView.getId()) {
                        case R.id.recharge_radio_button_mobile:
                            prePostRadioGroup.setVisibility(View.VISIBLE);
                            if(radioButtonPrepaid.isChecked()) {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_mobile_prepaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            } else {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_mobile_postpaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            }
                            break;
                        case R.id.recharge_radio_button_dth:
                            prePostRadioGroup.setVisibility(View.GONE);
                            ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(),
                                    R.array.recharge_dth_company, R.layout.simple_spinner_item);
                            adapter1.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                            spinnerCompanyName.setAdapter(adapter1);
                            break;
                        case R.id.recharge_radio_button_data_card:
                            prePostRadioGroup.setVisibility(View.VISIBLE);
                            if(radioButtonPrepaid.isChecked()) {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_data_card_prepaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            } else {
                                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                                        R.array.recharge_data_card_postpaid_company, R.layout.simple_spinner_item);
                                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                                spinnerCompanyName.setAdapter(adapter);
                            }
                            break;
                    }
                }
            }
        };
        radioButtonMobile.setOnCheckedChangeListener(typeChangeListener);
        radioButtonDTH.setOnCheckedChangeListener(typeChangeListener);
        radioButtonDatacard.setOnCheckedChangeListener(typeChangeListener);

        radioButtonPostpaid.setOnCheckedChangeListener(prePostChangeListener);
        radioButtonPrepaid.setOnCheckedChangeListener(prePostChangeListener);*/

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.recharge_mobile_prepaid_company, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerCompanyName.setAdapter(adapter);

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
                    /*if (radioButtonDTH.isChecked()) {
                        ParseObject object = new ParseObject(PARSE_TABLE_NAME_RECHARGE);
                        object.put(PARSE_TABLE_COLUMN_AMOUNT, editTextAmount.getText().toString());
                        object.put(PARSE_TABLE_COLUMN_NUMBER, editTextNumber.getText().toString());
                        object.put(PARSE_TABLE_COLUMN_COMMENT, editTextMessage.getText().toString());
                        object.put(PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
                        object.put(PARSE_TABLE_COLUMN_COMPANY, spinnerCompanyName.getSelectedItem().toString());
                        object.put(PARSE_TABLE_COLUMN_TYPE, TYPE_DTH);

                        // set public access so that referrer can access this entry
                        ParseACL groupACL = new ParseACL(user);
                        groupACL.setPublicWriteAccess(true);
                        groupACL.setPublicReadAccess(true);
                        object.setACL(groupACL);

                        object.saveEventually();
                        if(isVisible()) {
                            Utility.showInformativeDialog(RechargeFragment.this, getActivity(), null, Constants.RECHARGE_SENT_SUCCESSFUL, null, false);
                        }
                    } else {*/
                        if (Utility.isValidMobile(editTextNumber.getText().toString())) {
                            ParseObject object = new ParseObject(PARSE_TABLE_NAME_RECHARGE);
                            object.put(PARSE_TABLE_COLUMN_AMOUNT, editTextAmount.getText().toString());
                            object.put(PARSE_TABLE_COLUMN_COMMENT, editTextMessage.getText().toString());
                            object.put(PARSE_TABLE_COLUMN_NUMBER, editTextNumber.getText().toString());
                            object.put(PARSE_TABLE_COLUMN_COMPANY, spinnerCompanyName.getSelectedItem().toString());
                            object.put(PARSE_TABLE_COLUMN_USER_ID, user.getObjectId());
                            /*object.put(PARSE_TABLE_COLUMN_IS_PREPAID, radioButtonPrepaid.isChecked());
                            object.put(PARSE_TABLE_COLUMN_TYPE, (radioButtonMobile.isChecked() ? TYPE_MOBILE : TYPE_DATA_CARD));*/
                            object.put(PARSE_TABLE_COLUMN_IS_PREPAID, true);
                            object.put(PARSE_TABLE_COLUMN_TYPE, TYPE_MOBILE);

                            // set public access so that referrer can access this entry
                            ParseACL groupACL = new ParseACL(user);
                            groupACL.setPublicWriteAccess(true);
                            groupACL.setPublicReadAccess(true);
                            object.setACL(groupACL);

                            object.saveEventually();
                            if(isVisible()) {
                                Utility.showInformativeDialog(RechargeFragment.this, getActivity(), null, Constants.RECHARGE_SENT_SUCCESSFUL, null, false);
                            }
                        } else {
                            editTextNumber.setError("Number Not valid");
                        }
                    //}
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
        mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_RECHARGE, Constants.ID_APP_RECHARGE, R.drawable.topup));
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showInformativeDialog(null, null, null, null, null, false);
    }
}
