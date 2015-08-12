package com.fireballlabs.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fireballlabs.MainActivityCallBacks;
import com.fireballlabs.cashguru.R;
import com.fireballlabs.helper.Constants;
import com.fireballlabs.view.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SlidingTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SlidingTabFragment extends Fragment {
    private static final String ARG_PARAM_TITLES = "titles";
    private static final String ARG_PARAM_FRAGMENTS = "fragments";
    private static MainActivityCallBacks mCallbacks;
    int[] fragmentIds;
    private String[] titles;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * List of {@link PagerItem} which represent this sample's tabs.
     */
    private List<PagerItem> mTabs = new ArrayList<PagerItem>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragmentIds unique ids of all the fragments which needs to be shown as paged.
     * @return A new instance of fragment SlidingTabFragment.
     */

    public static SlidingTabFragment newInstance(String[] titles, int[] fragmentIds, MainActivityCallBacks callbacks) {
        SlidingTabFragment fragment = new SlidingTabFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_PARAM_TITLES, titles);
        args.putIntArray(ARG_PARAM_FRAGMENTS, fragmentIds);
        fragment.setArguments(args);
        mCallbacks = callbacks;
        return fragment;
    }

    public SlidingTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            titles = getArguments().getStringArray(ARG_PARAM_TITLES);
            fragmentIds = getArguments().getIntArray(ARG_PARAM_FRAGMENTS);
        }

        if(titles == null || fragmentIds == null || titles.length != fragmentIds.length) {
            throw new RuntimeException("Title Array size doesn't match with Fragment Array size, or any of these array's size is null");
        }
        for (int i = 0; i < titles.length; i++) {
            mTabs.add(new PagerItem(titles[i], fragmentIds[i], mCallbacks));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sliding_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()));
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
        // the tab at the position, and return it's set color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
//                return mTabs.get(position).getIndicatorColor();
                return getResources().getColor(R.color.primary);
            }

            @Override
            public int getDividerColor(int position) {
//                return mTabs.get(position).getDividerColor();
                return getResources().getColor(R.color.primary);
            }

        });
    }

    /**
     * This class represents a tab to be displayed by {@link ViewPager} and it's associated
     * {@link SlidingTabLayout}.
     */
    static class PagerItem {
        private final String mTitle;
        private final int mFragmentId;
        private final MainActivityCallBacks mCallbacks;

        PagerItem(String title, int fragmentId, MainActivityCallBacks callback) {
            mTitle = title;
            mFragmentId = fragmentId;
            mCallbacks = callback;
        }

        /**
         * @return A new {@link Fragment} to be displayed by a {@link ViewPager}
         */
        Fragment createFragment() {
            if(Constants.TITLE_APP_INSTALLS.equals(mTitle)
                    && mFragmentId == Constants.ID_APP_INSTALLS) {
                return AppInstallsFragment.newInstance(mTitle, mCallbacks);
            } else if(Constants.TITLE_PENDING_INSTALLS.equals(mTitle)
                    && mFragmentId == Constants.ID_PENDING_INSTALLS) {
                return PendingInstallsFragment.newInstance(mTitle, mCallbacks);
            } else if(Constants.TITLE_COMPLETED_INSTALLS.equals(mTitle)
                    && mFragmentId == Constants.ID_COMPLETED_INSTALLS) {
                return CompletedInstallsFragment.newInstance(mTitle, mCallbacks);
            }
            return null;
        }

        /**
         * @return the title which represents this tab.
         * {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
         */
        String getTitle() {
            return mTitle;
        }

    }

    /**
     * The {@link android.support.v4.app.FragmentPagerAdapter} used to display pages. The individual pages
     * are instances of {@link Fragment} which just display information as they desire. Each page is
     * created by the relevant {@link PagerItem} for the requested position.
     * <p>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link SlidingTabLayout}.
     */
    class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        FragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
         * <p>
         * Here we return the value returned from {@link PagerItem#createFragment()}.
         */
        @Override
        public Fragment getItem(int i) {
            return mTabs.get(i).createFragment();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we return the value returned from {@link PagerItem#getTitle()}.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }
        // END_INCLUDE (pageradapter_getpagetitle)

    }
}
