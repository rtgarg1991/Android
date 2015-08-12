package com.cashon.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cashon.cashon.R;
import com.cashon.helper.Constants;
import com.cashon.helper.model.InstallationHelper;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class ReferFriendsFragment extends Fragment {

    public static ReferFriendsFragment newInstance() {
        ReferFriendsFragment fragment = new ReferFriendsFragment();
        return fragment;
    }

    public ReferFriendsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_refer_friends, container, false);
        final String referalCode = (String)ParseInstallation.getCurrentInstallation().get(InstallationHelper.PARSE_TABLE_COLUMN_REFER_CODE);
        TextView referalTextView = (TextView) view.findViewById(R.id.textReferralCode);
        referalTextView.setText(referalCode);

        Button shareButton = (Button)view.findViewById(R.id.buttonShareReferal);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(Constants.TEXT_REFERAL, referalCode));
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
            }
        });
        return view;
    }


}
