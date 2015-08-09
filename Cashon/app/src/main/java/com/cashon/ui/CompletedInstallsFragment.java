package com.cashon.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cashon.adapter.CompletedInstallsAdapter;
import com.cashon.adapter.PendingInstallsAdapter;
import com.cashon.cashon.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompletedInstallsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompletedInstallsFragment extends Fragment {
    RecyclerView mRecyclerView;
    CompletedInstallsAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private SwipeRefreshLayout mRefreshLayout;

    public static CompletedInstallsFragment newInstance(String title) {
        CompletedInstallsFragment fragment = new CompletedInstallsFragment();
        return fragment;
    }

    public CompletedInstallsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pending_installs, container, false);
        mRefreshLayout = (SwipeRefreshLayout) rootView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.pending_installs_recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mAdapter = new CompletedInstallsAdapter(getActivity());

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setUpOffers();
            }
        });
        setUpOffers();

        // return root view which will be shown in the content area of activity
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
    }
}
