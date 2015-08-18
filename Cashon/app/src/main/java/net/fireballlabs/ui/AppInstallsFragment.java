package net.fireballlabs.ui;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.AppInstallsAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.Logger;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.sql.SQLWrapper;
import net.fireballlabs.impl.Utility;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;

import java.util.List;

public class AppInstallsFragment extends Fragment implements HardwareAccess.HardwareAccessCallbacks {
    private static MainActivityCallBacks mCallBacks;
    RecyclerView mRecyclerView;
    AppInstallsAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mEmptyTextView;
    private boolean mDetatched;

    public static AppInstallsFragment newInstance(String title, MainActivityCallBacks callBacks) {
        AppInstallsFragment fragment = new AppInstallsFragment();
        mCallBacks = callBacks;
        return fragment;
    }

    public AppInstallsFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_app_installs, container, false);
        mRefreshLayout = (SwipeRefreshLayout) rootView;
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.app_installs_recycler_view);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mAdapter = new AppInstallsAdapter(getActivity(), this);
        mEmptyTextView = (TextView)rootView.findViewById(R.id.app_install_empty_text_view);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Constants.appInstallSyncNeeded = true;
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

        // return root view which will be shown in the content area of activity
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpOffers();
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

        AppInstallAsyncTaks task = new AppInstallAsyncTaks();
        task.execute((Void) null);
    }

    public void setEmptyViewVisibility(int visibility) {
        mEmptyTextView.setVisibility(visibility);
        if(visibility == View.GONE) {
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void accessCompleted(int access, boolean isSuccess) {
        setUpOffers();
    }

    class AppInstallAsyncTaks extends AsyncTask<Void, Void, List<Offer>> {

        @Override
        protected List<Offer> doInBackground(Void... params) {
            if(Constants.appInstallSyncNeeded) {

                try {
                    List<Offer> offers = Offer.getAllOffers(getActivity());

                    if(getActivity() != null) {
                        synchronized (getActivity()) {
                            if (Constants.appInstallSyncNeeded) {
                                SQLWrapper.Offer.clearCurrentDataFromDB(getActivity());
                                for (Offer offer : offers) {
                                    offer.saveData(getActivity());
                                }
                                Constants.appInstallSyncNeeded = false;
                            }
                        }
                    }
                    return offers;
                } catch (ParseException e) {
                    Logger.doSecureLogging(Log.WARN, getClass().getSimpleName()
                            + " Error  While getting Offer details from Parse");
                    Crashlytics.logException(e);
                }
            } else {
                List<Offer> offers = Offer.getData(getActivity());
                return offers;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected void onPostExecute(List<Offer> offers) {
            super.onPostExecute(offers);
            showProgress(false);
            mAdapter.addAppInstallOffers((List<Offer>) offers);
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
            mRefreshLayout.setRefreshing(false);
            Utility.showProgress(getActivity(), show, String.valueOf(getResources().getText(R.string.please_wait_app_offers)));
        }
    }

}
