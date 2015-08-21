package net.fireballlabs.ui;


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

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.URLShortener;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.InstallationHelper;
import net.fireballlabs.helper.model.UserHelper;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;


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
    String referralUrl = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_refer_friends, container, false);
        final String referralCode = (String)ParseUser.getCurrentUser().get(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE);
        referralUrl = (String) ParseUser.getCurrentUser().get(UserHelper.PARSE_TABLE_COLUMN_REFER_URL);

        if(referralUrl == null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = URLShortener.getJSONFromUrl(getActivity(), ParseUser.getCurrentUser().getString(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE));
                    if (json != null) {
                        try {
                            referralUrl = json.getString("id");
                            if (referralUrl != null) {
                                ParseUser.getCurrentUser().put(UserHelper.PARSE_TABLE_COLUMN_REFER_URL, referralUrl);
                            }
                            ParseUser.getCurrentUser().saveEventually();
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                        }
                    }

                }
            });
            thread.start();
        }

        final TextView referralTextView = (TextView) view.findViewById(R.id.textReferralCode);
        referralTextView.setText(referralCode);

        referralTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVisible()) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(referralTextView.getText().toString());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Referral Code", referralTextView.getText().toString());
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
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(Constants.TEXT_REFERAL, referralCode, referralUrl));
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
            }
        });
        return view;
    }


}
