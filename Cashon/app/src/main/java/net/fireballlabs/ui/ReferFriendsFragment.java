package net.fireballlabs.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.URLShortener;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.UserHelper;


public class ReferFriendsFragment extends BaseFragment {

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

        final TextView payoutTextView = (TextView) view.findViewById(R.id.refer_friend_payout_view);
        final TextView shareConditionTextView = (TextView) view.findViewById(R.id.refer_friend_share_conditions_text_view);

        final TextView referralLinkTextView = (TextView) view.findViewById(R.id.refer_friend_share_link_text_view);

        int refer1 = PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_REFERRAL_BONUS_1, Context.MODE_PRIVATE, 10);
        int refer2 = PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_REFERRAL_BONUS_2, Context.MODE_PRIVATE, 10);
        int refer3 = PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_REFERRAL_BONUS_3, Context.MODE_PRIVATE, -1);
        int count1 = PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_REFERRAL_BONUS_COUNT_1, Context.MODE_PRIVATE, 1);
        int count2 = PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_REFERRAL_BONUS_COUNT_2, Context.MODE_PRIVATE, 2);
        int count3 = PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_REFERRAL_BONUS_COUNT_3, Context.MODE_PRIVATE, 2);

        String referralConditions = "";
        if(refer3 > 0) {
            referralConditions = getResources().getString(R.string.referral_conditions_2);
        } else {
            referralConditions = getResources().getString(R.string.referral_conditions);
        }
        referralConditions = referralConditions.replace("{refer}", String.valueOf(refer1 + refer2));
        referralConditions = referralConditions.replace("{refer_1}", String.valueOf(refer1));
        referralConditions = referralConditions.replace("{refer_2}", String.valueOf(refer2));
        referralConditions = referralConditions.replace("{refer_3}", String.valueOf(refer3));
        referralConditions = referralConditions.replace("{count_1}", String.valueOf(count1));
        referralConditions = referralConditions.replace("{count_2}", String.valueOf(count2));
        referralConditions = referralConditions.replace("{count_3}", String.valueOf(count3));

        shareConditionTextView.setText(referralConditions);
        payoutTextView.setText(Constants.INR_LABEL + String.valueOf(refer1 + refer2));

        String referralCode = (String)ParseUser.getCurrentUser().get(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE);
        if(referralCode == null) {
            try {
                ParseUser.getCurrentUser().fetch();
                referralCode = (String)ParseUser.getCurrentUser().get(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        referralUrl = (String) ParseUser.getCurrentUser().get(UserHelper.PARSE_TABLE_COLUMN_REFER_URL);

        if(referralUrl == null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(isValidContext(getActivity())) {
                        String url = URLShortener.getShortenedUrl(getActivity(), ParseUser.getCurrentUser().getString(UserHelper.PARSE_TABLE_COLUMN_REFER_CODE));
                        referralUrl = url;
                        if (referralUrl != null) {
                            ParseUser.getCurrentUser().put(UserHelper.PARSE_TABLE_COLUMN_REFER_URL, referralUrl);
                            referralLinkTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    referralLinkTextView.setText(referralUrl);
                                }
                            });
                        }
                        ParseUser.getCurrentUser().saveEventually();
                    }

                }
            });
            thread.start();
        } else {
            referralLinkTextView.setText(referralUrl);
        }

        referralLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVisible()) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(referralLinkTextView.getText().toString());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Referral Code", referralLinkTextView.getText().toString());
                        clipboard.setPrimaryClip(clip);
                    }
                }
                Toast.makeText(getActivity(), Constants.REFERRAL_LINK_COPIED, Toast.LENGTH_LONG).show();
            }
        });

        Button shareButton = (Button)view.findViewById(R.id.refer_friends_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(Constants.TEXT_REFERAL, referralUrl));
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
            }
        });

        RelativeLayout ll = (RelativeLayout)view.findViewById(R.id.refer_friend_payout_layout);
        ll.bringToFront();



        return view;
    }


}
