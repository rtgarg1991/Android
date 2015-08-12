package com.cashon.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cashon.adapter.LatestDealsAdapter;
import com.cashon.cashon.R;
import com.cashon.helper.model.LatestDeal;
import com.cashon.impl.Utility;
import com.parse.ParseException;

import java.util.List;


public class LatestDealsFragment extends Fragment {

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private LatestDealsAdapter mAdapter;

    public static LatestDealsFragment newInstance() {
        LatestDealsFragment fragment = new LatestDealsFragment();
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
        setUpOffers();
        return rootView;
    }

    private void setUpOffers() {
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
        if(isVisible()) {
            Utility.showProgress(getActivity(), show, String.valueOf(getResources().getText(R.string.please_wait_app_offers)));
        }
    }

    class LatestDealsAsyncTask extends AsyncTask<Void, Void, List<LatestDeal>> {

        @Override
        protected List<LatestDeal> doInBackground(Void... params) {
            List<LatestDeal> deals = null;
            try {
                deals = LatestDeal.getAllDeals();
                return deals;
            } catch (ParseException e) {
                e.printStackTrace();
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

}
