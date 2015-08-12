package com.fireballlabs.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fireballlabs.MainActivityCallBacks;
import com.fireballlabs.cashguru.R;
import com.fireballlabs.helper.Constants;
import com.fireballlabs.helper.model.InstallationHelper;
import com.parse.ParseInstallation;


public class ReferFriendsFragment extends Fragment {

    private static MainActivityCallBacks mCallBacks;

    public static ReferFriendsFragment newInstance(MainActivityCallBacks callBacks) {
        ReferFriendsFragment fragment = new ReferFriendsFragment();
        mCallBacks = callBacks;
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
        final TextView referalTextView = (TextView) view.findViewById(R.id.textReferralCode);
        referalTextView.setText(referalCode);

        referalTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVisible()) {
                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(referalTextView.getText().toString());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Referral Code",referalTextView.getText().toString());
                        clipboard.setPrimaryClip(clip);
                    }
                }
                Toast.makeText(getActivity(), Constants.REFERRAL_CODE_COPIED, Toast.LENGTH_LONG).show();
            }
        });

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
