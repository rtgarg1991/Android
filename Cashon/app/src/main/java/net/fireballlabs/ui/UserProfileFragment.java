package net.fireballlabs.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.AppInstallsAdapter;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.cashguru.MainActivity;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UserProfile;
import net.fireballlabs.impl.Utility;
import net.fireballlabs.sql.SQLWrapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserProfileFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private static MainActivityCallBacks mCallBacks;
    private boolean mDetatched;

    static UserProfileFragment fr;

    private static final int DATE_DIALOG_ID = 1;
    private int year;
    private int month;
    private int day;
    private EditText mNameEditText;
    private EditText mDobEditText;
    private Spinner mSexSpinner;
    private Button mButton;
    private ParseObject mProfile;

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
        mButton = (Button)rootView.findViewById(R.id.user_profile_button);

        mDobEditText.setKeyListener(null);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.user_profile_sex_array, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mSexSpinner.setAdapter(adapter);

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
                    Date date;
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        date = format.parse(mDobEditText.getText().toString());


                        ParseObject object;
                        if(mProfile != null) {
                            object = mProfile;
                        } else {
                            object = new ParseObject(UserProfile.PARSE_TABLE_NAME_USER_PROFILE);
                        }

                        object.put(UserProfile.PARSE_TABLE_COLUMN_SEX, "Male".equals(mSexSpinner.getSelectedItem().toString()) ? false : true);
                        object.put(UserProfile.PARSE_TABLE_COLUMN_DOB, date);
                        object.put(UserProfile.PARSE_TABLE_COLUMN_NAME, mNameEditText.getText().toString());
                        object.put(UserProfile.PARSE_TABLE_COLUMN_USER_ID, ParseUser.getCurrentUser().getObjectId());
                        object.put(UserProfile.PARSE_TABLE_COLUMN_ANDROID_API, android.os.Build.VERSION.SDK_INT);

                        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
                        int height = metrics.heightPixels;
                        int width = metrics.widthPixels;

                        object.put(UserProfile.PARSE_TABLE_COLUMN_SCREEN_SIZE_X, width);
                        object.put(UserProfile.PARSE_TABLE_COLUMN_SCREEN_SIZE_Y, height);

                        try {
                            Utility.showProgress(getActivity(), true, getActivity().getResources().getText(R.string.please_wait).toString());
                            object.save();
                            Utility.showProgress(getActivity(), false, null);
                            mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS, Constants.ID_APP_INSTALLS, R.drawable.offerwall), null);
                        } catch (ParseException e) {
                            Crashlytics.logException(e);
                        }
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
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
        showProgress(true);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(UserProfile.PARSE_TABLE_NAME_USER_PROFILE);
        query.whereEqualTo(UserProfile.PARSE_TABLE_COLUMN_USER_ID, ParseUser.getCurrentUser().getObjectId());
        try {
            mProfile =  query.getFirst();

            mNameEditText.setText(mProfile.getString(UserProfile.PARSE_TABLE_COLUMN_NAME));
            Date cDate = mProfile.getDate(UserProfile.PARSE_TABLE_COLUMN_DOB);
            String fDate = new SimpleDateFormat("dd/MM/yyyy").format(cDate);
            mDobEditText.setText(fDate);
            mSexSpinner.setSelection(mProfile.getBoolean(UserProfile.PARSE_TABLE_COLUMN_SEX) ? 1 : 0);

            mButton.setText("Update");
        } catch (ParseException e) {
//            e.printStackTrace();
            mButton.setText("Save");
            mProfile = null;
        }
        showProgress(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showProgress(null, false, null);
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
            int month = 01;//c.get(Calendar.MONTH);
            int day = 01;//c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of TimePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), fr, year,month,day);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }

    }
}
