package com.fireballlabs.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.fireballlabs.MainActivityCallBacks;
import com.fireballlabs.cashguru.R;
import com.fireballlabs.helper.Constants;
import com.fireballlabs.impl.Utility;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class ContactUsFragment extends Fragment implements Utility.DialogCallback {

    public static String PARSE_TABLE_NAME_CONTACT_US = "ContactUs";
    public static String PARSE_TABLE_NAME_COLUMN_EMAIL = "email";
    public static String PARSE_TABLE_NAME_COLUMN_MESSAGE = "message";
    private static MainActivityCallBacks mCallBacks;

    public static ContactUsFragment newInstance(MainActivityCallBacks callBacks) {
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
                        String email = user.getUsername();
                        ParseObject object = new ParseObject(PARSE_TABLE_NAME_CONTACT_US);
                        object.put(PARSE_TABLE_NAME_COLUMN_EMAIL, email);
                        object.put(PARSE_TABLE_NAME_COLUMN_MESSAGE, et.getText().toString());

                        // set public access so that referrer can access this entry
                        ParseACL groupACL = new ParseACL(user);
                        groupACL.setPublicWriteAccess(true);
                        groupACL.setPublicReadAccess(true);
                        object.setACL(groupACL);

                        object.saveEventually();

                        Utility.showCompletionDialog(ContactUsFragment.this, getActivity(), Constants.CONTACT_US_SENT_SUCCESSFUL);

                        et.setText("");
                    }
                }
            }
        });
        return view;
    }


    @Override
    public void onDialogCallback(boolean success) {
        // nothing
    }
}
