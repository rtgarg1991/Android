package net.fireballlabs.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.UserProfile;
import net.fireballlabs.impl.Utility;

import java.util.Calendar;
import java.util.Date;

public class UserProfileFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener {

    private static MainActivityCallBacks mCallBacks;

    static UserProfileFragment fr;

    private static final int DATE_DIALOG_ID = 1;
    private int year;
    private int month;
    private int day;
    private EditText mNameEditText;
    private EditText mDobEditText;
    private Spinner mSexSpinner;
    private Button mButton;
    private EditText mPhoneEditText;
    private Spinner mOperatorSpinner;
    private Spinner mCircleSpinner;
    private Spinner mPhoneTypeSpinner;
    private EditText mCityEditText;
    private Spinner mStateSpinner;

    public static UserProfileFragment newInstance(MainActivityCallBacks callBacks) {
        UserProfileFragment fragment = new UserProfileFragment();
        mCallBacks = callBacks;
        fr = fragment;
        return fragment;
    }

    public UserProfileFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mNameEditText = (EditText)rootView.findViewById(R.id.user_profile_name);
        mDobEditText = (EditText)rootView.findViewById(R.id.user_profile_dob);
        mSexSpinner = (Spinner)rootView.findViewById(R.id.user_profile_sex);

        mPhoneEditText = (EditText)rootView.findViewById(R.id.user_profile_phone);
        mOperatorSpinner = (Spinner)rootView.findViewById(R.id.user_profile_operator);
        mCircleSpinner = (Spinner)rootView.findViewById(R.id.user_profile_circle);
        mPhoneTypeSpinner = (Spinner)rootView.findViewById(R.id.user_profile_type);

        mCityEditText = (EditText)rootView.findViewById(R.id.user_profile_city);
        mStateSpinner = (Spinner)rootView.findViewById(R.id.user_profile_state);

        mButton = (Button)rootView.findViewById(R.id.user_profile_button);

        mDobEditText.setKeyListener(null);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.user_profile_sex_array, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mSexSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterMobileType = ArrayAdapter.createFromResource(getActivity(),
                R.array.user_profile_mobile_type_array, R.layout.simple_spinner_item);
        adapterMobileType.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mPhoneTypeSpinner.setAdapter(adapterMobileType);

        ArrayAdapter<CharSequence> adapterState = ArrayAdapter.createFromResource(getActivity(),
                R.array.user_profile_state_array, R.layout.simple_spinner_item);
        adapterState.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mStateSpinner.setAdapter(adapterState);

        ArrayAdapter<CharSequence> adapterCircle = ArrayAdapter.createFromResource(getActivity(),
                R.array.recharge_mobile_circle, R.layout.simple_spinner_item);
        adapterCircle.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mCircleSpinner.setAdapter(adapterCircle);

        ArrayAdapter<CharSequence> adapterOperator = ArrayAdapter.createFromResource(getActivity(),
                R.array.recharge_mobile_prepaid_company, R.layout.simple_spinner_item);
        adapterOperator.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mOperatorSpinner.setAdapter(adapterOperator);

        mDobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
                FragmentManager fm = getFragmentManager();
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.show(fm, "Select Date of Birth");
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateViews()) {
                    DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
                    int height = metrics.heightPixels;
                    int width = metrics.widthPixels;

                    try {
                        Utility.showProgress(getActivity(), true, getActivity().getResources().getText(R.string.please_wait).toString());

                        UserProfile.updateProfile(mNameEditText.getText().toString(), mSexSpinner.getSelectedItem().toString(),
                                mDobEditText.getText().toString(), android.os.Build.VERSION.SDK_INT, width, height,
                                mOperatorSpinner.getSelectedItem().toString(), mCircleSpinner.getSelectedItem().toString(),
                                mPhoneTypeSpinner.getSelectedItem().toString(),
                                mCityEditText.getText().toString(), mStateSpinner.getSelectedItem().toString(),
                                getActivity());
                        Utility.showProgress(getActivity(), false, null);
                        mCallBacks.setFragment(Constants.ID_APP_INSTALLS, null);
                    } catch (ParseException e) {
                        Crashlytics.logException(e);
                        Utility.showProgress(getActivity(), false, null);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                        Utility.showProgress(getActivity(), false, null);
                    }
                }
            }
        });
        return rootView;
    }

    private boolean validateViews() {
        mNameEditText.setError(null);
        mDobEditText.setError(null);
        if("".equals(mNameEditText.getText().toString())) {
            mNameEditText.setError(getActivity().getResources().getString(R.string.error_field_required));
            return false;
        }
        if("".equals(mDobEditText.getText().toString())) {
            mDobEditText.setError(getActivity().getResources().getString(R.string.error_field_required));
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        UserProfile.loadProfileData(mNameEditText, mSexSpinner, mDobEditText,
                mPhoneEditText, mPhoneTypeSpinner, mOperatorSpinner, mCircleSpinner,
                mCityEditText, mStateSpinner, getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showProgress(null, false, null);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mDobEditText.setText((dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth)) + "/"
                + ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1)) + "/"
                + String.valueOf(year));
    }

    public static class TimePickerFragment extends DialogFragment {
//
//        private DatePickerDialog.OnDateSetListener listener;
        public TimePickerFragment(){}

        /*public TimePickerFragment(DatePickerDialog.OnDateSetListener listener) {
            this.listener=listener;
        }*/

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int year = 2003;//c.get(Calendar.YEAR);
            int month = 1;//c.get(Calendar.MONTH);
            int day = 1;//c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), fr, year,month,day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }

    }
}
