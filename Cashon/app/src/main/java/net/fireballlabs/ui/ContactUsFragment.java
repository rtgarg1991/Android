package net.fireballlabs.ui;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.ContactUsHistoryAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.ContactUs;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ContactUsFragment extends Fragment implements Utility.DialogCallback, HardwareAccess.HardwareAccessCallbacks {

    public static String PARSE_TABLE_NAME_CONTACT_US = "ContactUs";
    public static String PARSE_TABLE_NAME_COLUMN_USER_ID = "userId";
    public static String PARSE_TABLE_NAME_COLUMN_MESSAGE = "message";

    private static MainActivityCallBacks mCallBacks;
    RecyclerView mRecyclerView;
    ContactUsHistoryAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private TextView mEmptyTextView;
    private LinearLayoutManager mLayoutManager;
    private boolean mDetatched;

    public static ContactUsFragment newInstance(String titles, MainActivityCallBacks callBacks) {
        ContactUsFragment fragment = new ContactUsFragment();
        mCallBacks = callBacks;
        return fragment;
    }

    public ContactUsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        final EditText et = (EditText)view.findViewById(R.id.contact_us_edit_text);

        Button shareButton = (Button)view.findViewById(R.id.contact_us_send_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et.getText() == null || et.getText().equals("")) {
                    et.setError("Enter some text.");
                } else {
                    ParseUser user = ParseUser.getCurrentUser();
                    if(user == null || !user.isAuthenticated()) {
                        // TODO track this and noify user that he/she has to login
                        return;
                    } else {
                        ContactUs.addContactUsEntry(getActivity(), user.getObjectId(), et.getText().toString());

                        Utility.showInformativeDialog(ContactUsFragment.this, getActivity(), null, Constants.CONTACT_US_SENT_SUCCESSFUL, null, true);

                        et.setText("");

                        ContactUs.resetData();
                        setUpOffers();
                    }
                }
            }
        });
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recharge_history_recycler_view);
        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mEmptyTextView = (TextView)view.findViewById(R.id.recharge_history_empty_text_view);

        mAdapter = new ContactUsHistoryAdapter(getActivity());
        setUpOffers();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpOffers();

    }

    private void setUpOffers() {
        if(!Utility.isInternetConnected(getActivity())) {
            HardwareAccess.access(getActivity(), this, HardwareAccess.ACCESS_INTERNET);
            return;
        }

        if(mRecyclerView == null || mAdapter == null) {
            // TODO error, need to check if this case can happen
            return;
        }
        //if(mLayoutManager == null) {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //}
        mRecyclerView.setAdapter(mAdapter);

        if(ContactUs.getContactUsHistory() != null) {
            mAdapter.addContactUsHistory(ContactUs.getContactUsHistory());
            if(ContactUs.getContactUsHistory().size() == 0) {
                setEmptyViewVisibility(View.VISIBLE);
            } else {
                setEmptyViewVisibility(View.GONE);
                mRecyclerView.scrollToPosition(ContactUs.getContactUsHistory().size() - 1);
            }
        } else {
            AppInstallAsyncTask task = new AppInstallAsyncTask();
            task.execute((Void) null);
        }
    }

    @Override
    public void onDialogCallback(boolean success) {

    }


    class AppInstallAsyncTask extends AsyncTask<Void, Void, List<ContactUs>> {

        @Override
        protected List<ContactUs> doInBackground(Void... voids) {
            if(getActivity() == null) {
                return null;
            }
            try {
                return ContactUs.getContactUsHistory(getActivity());
            } catch (ParseException e) {
                Crashlytics.logException(e);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected void onPostExecute(List<ContactUs> contactUs) {
            super.onPostExecute(contactUs);
            showProgress(false);
            if(contactUs == null) {
                mAdapter.addContactUsHistory(new ArrayList<ContactUs>());
                setEmptyViewVisibility(View.VISIBLE);
            } else {
                mAdapter.addContactUsHistory(contactUs);

                if(contactUs.size() == 0) {
                    setEmptyViewVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.scrollToPosition(contactUs.size() - 1);
                    setEmptyViewVisibility(View.GONE);
                }
            }
        }
    }

    public void setEmptyViewVisibility(int visibility) {
        mEmptyTextView.setVisibility(visibility);
        if(visibility == View.GONE) {
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }
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
    public void accessCompleted(int access, boolean isSuccess) {
        setUpOffers();
    }
}
