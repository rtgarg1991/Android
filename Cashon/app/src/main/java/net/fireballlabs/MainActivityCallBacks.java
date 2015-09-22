package net.fireballlabs;

import net.fireballlabs.adapter.MainDrawerAdapter;

/**
 * Created by Rohit on 8/12/2015.
 */
public interface MainActivityCallBacks {
    public void setFragment(MainDrawerAdapter.MainAppFeature feature, Object extra);
}
