package net.fireballlabs.ui;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.fireballlabs.MainActivityCallBacks;
import net.fireballlabs.adapter.MainDrawerAdapter;
import net.fireballlabs.adapter.NotificationAdapter;
import net.fireballlabs.cashguru.R;
import net.fireballlabs.helper.Constants;
import net.fireballlabs.helper.model.NotificationHelper;
import net.fireballlabs.impl.HardwareAccess;
import net.fireballlabs.impl.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment implements HardwareAccess.HardwareAccessCallbacks {
    private static MainActivityCallBacks mCallBacks;
    RecyclerView mRecyclerView;
    NotificationAdapter mAdapter;
    MaterialDialog mProgressDialog;
    private TextView mEmptyTextView;
    private LinearLayoutManager mLayoutManager;
    private boolean mDetatched;

    public static NotificationFragment newInstance (MainActivityCallBacks callBacks) {
        NotificationFragment fragment = new NotificationFragment();
        mCallBacks = callBacks;
        return fragment;
    }

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.notification_recycler_view);
        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));
        mEmptyTextView = (TextView)rootView.findViewById(R.id.notification_empty_text_view);

        mAdapter = new NotificationAdapter(getActivity(), this);
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

        if(NotificationHelper.getNotifications() != null) {
            mAdapter.addNotifications(NotificationHelper.getNotifications());
            if(NotificationHelper.getNotifications().size() == 0) {
                setEmptyViewVisibility(View.VISIBLE);
            } else {
                setEmptyViewVisibility(View.GONE);
            }
        } else {
            AppInstallAsyncTask task = new AppInstallAsyncTask();
            task.execute((Void) null);
        }
    }

    public void selectFeature(int id) {
        switch (id) {
            case Constants.ID_APP_REFER:
                mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_REFER, Constants.ID_APP_REFER, R.drawable.refericon), null);
                break;
            case Constants.ID_APP_INSTALLS:
                mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_INSTALLS, Constants.ID_APP_INSTALLS, R.drawable.offerwall), null);
                break;
            case Constants.ID_APP_RECHARGE:
                mCallBacks.setFragment(new MainDrawerAdapter.MainAppFeature(Constants.TITLE_APP_RECHARGE, Constants.ID_APP_RECHARGE, R.drawable.topup), null);
                break;
        }
    }


    class AppInstallAsyncTask extends AsyncTask<Void, Void, List<NotificationHelper.Notification>> {

        @Override
        protected List<NotificationHelper.Notification> doInBackground(Void... voids) {
            if(getActivity() == null) {
                return null;
            }
            return NotificationHelper.getNotifications(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected void onPostExecute(List<NotificationHelper.Notification> recharges) {
            super.onPostExecute(recharges);
            showProgress(false);
            if(recharges == null) {
                mAdapter.addNotifications(new ArrayList<NotificationHelper.Notification>());
                setEmptyViewVisibility(View.VISIBLE);
            } else {
                mAdapter.addNotifications(recharges);

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
