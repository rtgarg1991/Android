package net.fireballlabs.ui;


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
import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.adapter.PendingInstallsAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PendingInstallsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PendingInstallsFragment extends Fragment implements HardwareAccess.HardwareAccessCallbacks {
    private static MainActivityCallBacks mCallBacks;
    RecyclerView mRecyclerView;
    PendingInstallsAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mEmptyTextView;
    private LinearLayoutManager mLayoutManager;

    public static PendingInstallsFragment newInstance(String title, MainActivityCallBacks callBacks) {
        PendingInstallsFragment fragment = new PendingInstallsFragment();
        mCallBacks = callBacks;
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
        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mEmptyTextView = (TextView)rootView.findViewById(R.id.app_install_pending_text_view);

        mAdapter = new PendingInstallsAdapter(getActivity(), this);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*if(isVisible()) {
                    mAdapter = new PendingInstallsAdapter(getActivity(), PendingInstallsFragment.this);
                }*/
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
        mRefreshLayout.setRefreshing(false);
        mAdapter.updateOfferList();
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {
        setUpOffers();
    }

    public void setFragment(int idAppOffer, String offId) {
        mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_OFFER, Constants.ID_APP_OFFER, 0), offId);
    }
}
