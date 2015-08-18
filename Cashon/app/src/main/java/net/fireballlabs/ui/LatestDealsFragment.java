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

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.LatestDealsAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.model.LatestDeal;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;

import java.util.List;


public class LatestDealsFragment extends Fragment implements HardwareAccess.HardwareAccessCallbacks {

    private static MainActivityCallBacks mCallBacks;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private LatestDealsAdapter mAdapter;
    private boolean mDetatched;

    public static LatestDealsFragment newInstance(String title, MainActivityCallBacks callBacks) {
        LatestDealsFragment fragment = new LatestDealsFragment();
        mCallBacks = callBacks;
        return fragment;
    }

    public LatestDealsFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_latest_deals, container, false);
        mRefreshLayout = (SwipeRefreshLayout) rootView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.latest_deals_recycler_view);
        mAdapter = new LatestDealsAdapter(getActivity(), this);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpOffers();
            }
        });
        mRefreshLayout.setEnabled(false);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });
        setUpOffers();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showProgress(getActivity(), false, null);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        LatestDealsAsyncTask task = new LatestDealsAsyncTask();
        task.execute((Void) null);
    }

    public void showProgress(boolean show) {
        mRefreshLayout.setRefreshing(false);
        if(isAdded() && !mDetatched) {
            Utility.showProgress(getActivity(), show, String.valueOf(getResources().getText(R.string.please_wait_app_offers)));
        }
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {
        setUpOffers();
    }

    class LatestDealsAsyncTask extends AsyncTask<Void, Void, List<LatestDeal>> {

        @Override
        protected List<LatestDeal> doInBackground(Void... params) {
            List<LatestDeal> deals = null;
            try {
                deals = LatestDeal.getAllDeals();
                return deals;
            } catch (ParseException e) {
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected void onPostExecute(List<LatestDeal> latestDeals) {
            super.onPostExecute(latestDeals);
            mAdapter.addAll(latestDeals);
            showProgress(false);
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

}
