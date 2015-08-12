package com.cashon.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cashon.adapter.AppInstallsAdapter;
import com.cashon.adapter.PendingInstallsAdapter;
import com.cashon.cashon.R;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PendingInstallsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PendingInstallsFragment extends Fragment {
    RecyclerView mRecyclerView;
    PendingInstallsAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mEmptyTextView;

    public static PendingInstallsFragment newInstance(String title) {
        PendingInstallsFragment fragment = new PendingInstallsFragment();
        return fragment;
    }

    public PendingInstallsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pending_installs, container, false);
        mRefreshLayout = (SwipeRefreshLayout) rootView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.pending_installs_recycler_view);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mEmptyTextView = (TextView)rootView.findViewById(R.id.app_install_pending_text_view);

        mAdapter = new PendingInstallsAdapter(getActivity(), this);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isVisible()) {
                    mAdapter = new PendingInstallsAdapter(getActivity(), PendingInstallsFragment.this);
                }
                setUpOffers();
            }
        });
        setUpOffers();

        // return root view which will be shown in the content area of activity
        return rootView;
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

        if(mRecyclerView == null || mAdapter == null) {
            // TODO error, need to check if this case can happen
            return;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setRefreshing(false);
    }
}
