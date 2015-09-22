package net.fireballlabs.ui;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.PendingInstallsAdapter;
import net.fireballlabs.adapter.RechargeHistoryAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.model.Recharge;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RechargeHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RechargeHistoryFragment extends Fragment implements HardwareAccess.HardwareAccessCallbacks {
    private static MainActivityCallBacks mCallBacks;
    RecyclerView mRecyclerView;
    RechargeHistoryAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private TextView mEmptyTextView;
    private LinearLayoutManager mLayoutManager;
    private boolean mDetatched;

    public static RechargeHistoryFragment newInstance(String title, MainActivityCallBacks callBacks) {
        RechargeHistoryFragment fragment = new RechargeHistoryFragment();
        mCallBacks = callBacks;
        return fragment;
    }

    public RechargeHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recharge_history, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recharge_history_recycler_view);
        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mEmptyTextView = (TextView)rootView.findViewById(R.id.recharge_history_empty_text_view);

        mAdapter = new RechargeHistoryAdapter(getActivity());
        setUpOffers();

        // return root view which will be shown in the content area of activity
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpOffers();

    }

    public void setEmptyViewVisibility(int visibility) {
        mEmptyTextView.setVisibility(visibility);
        if(visibility == View.GONE) {
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }
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

        if(Recharge.getRecharges() != null) {
            mAdapter.addRechargeHistory(Recharge.getRecharges());
            if(Recharge.getRecharges().size() == 0) {
                setEmptyViewVisibility(View.VISIBLE);
            } else {
                setEmptyViewVisibility(View.GONE);
            }
        } else {
            AppInstallAsyncTask task = new AppInstallAsyncTask();
            task.execute((Void) null);
        }
    }



    class AppInstallAsyncTask extends AsyncTask<Void, Void, List<Recharge>> {

        @Override
        protected List<Recharge> doInBackground(Void... voids) {
            if(getActivity() == null) {
                return null;
            }
            try {
                return Recharge.getRechargeHistory(getActivity());
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
        protected void onPostExecute(List<Recharge> recharges) {
            super.onPostExecute(recharges);
            showProgress(false);
            if(recharges == null) {
                mAdapter.addRechargeHistory(new ArrayList<Recharge>());
                setEmptyViewVisibility(View.VISIBLE);
            } else {
                mAdapter.addRechargeHistory(recharges);

                if(recharges.size() == 0) {
                    setEmptyViewVisibility(View.VISIBLE);
                } else {
                    setEmptyViewVisibility(View.GONE);
                }
            }
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
