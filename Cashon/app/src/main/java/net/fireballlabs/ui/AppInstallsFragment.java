package net.fireballlabs.ui;

import android.app.Activity;
import android.content.Context;
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
import net.fireballlabs.helper.PreferenceManager;
import net.fireballlabs.helper.model.Offer;
import net.fireballlabs.helper.model.UsedOffer;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class AppInstallsFragment extends Fragment implements HardwareAccess.HardwareAccessCallbacks {
    private static MainActivityCallBacks mCallBacks;
    RecyclerView mRecyclerView;
    AppInstallsAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mEmptyTextView;
    private boolean mDetatched;
    private List<String> mOffers;
    private LinearLayoutManager layoutManager;

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
        mRecyclerView.setHasFixedSize(true);
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
        //if(layoutManager == null) {
            layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
        //}
        mRecyclerView.setAdapter(mAdapter);


        // if we already have offers loaded, lets just check if we need to remove any offer from this list
        // and show all the offers to user
        if(mOffers != null) {
            mAdapter.addAppInstallOffers(mOffers);
            showProgress(false);
        } else {
            AppInstallAsyncTask task = new AppInstallAsyncTask();
            task.execute((Void) null);
        }
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

    class AppInstallAsyncTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            if(getActivity() == null) {
                return null;
            }
            if(PreferenceManager.getDefaultSharedPreferenceValue(getActivity(), Constants.PREF_CLOUD_DATA_CHANGED, Context.MODE_PRIVATE, true)) {

                try {
                    // sync all offers
                    List<Offer> offers = Offer.getAllOffers(getActivity());
                    UsedOffer.clearSavedData();
                    for (Offer offer : offers) {
                        offer.saveData(getActivity());
                    }
                    PreferenceManager.setDefaultSharedPreferenceValue(getActivity(), Constants.PREF_CLOUD_DATA_CHANGED, Context.MODE_PRIVATE, false);
                } catch (ParseException e) {
                    Logger.doSecureLogging(Log.WARN, getClass().getSimpleName()
                            + " Error  While getting Offer details from Parse");
                    Crashlytics.logException(e);
                }
            }
            // now retrieve needed offers
            List<String> availableOffers = null;
            try {
                availableOffers = UsedOffer.getAvailableOffers(getActivity());
                return availableOffers;
            } catch (ParseException e) {
                Logger.doSecureLogging(Log.WARN, getClass().getSimpleName()
                        + " Error  While getting Offer details from Parse");
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
        protected void onPostExecute(List<String> offers) {
            super.onPostExecute(offers);
            showProgress(false);
            if(offers == null) {
                mAdapter.addAppInstallOffers(new ArrayList<String>());
            } else {
                mAdapter.addAppInstallOffers((List<String>) offers);
            }
            mOffers = offers;
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
