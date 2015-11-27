package net.fireballlabs.ui;

import android.content.Context;
import android.support.v4.app.Fragment;

import net.fireballlabs.impl.Utility;

/**
 * Created by rohitgarg on 10/30/15.
 */
public class BaseFragment extends Fragment {
    public Boolean mDetached = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDetached = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDetached = true;
    }

    protected boolean isValidContext(Context context) {
        if(!this.isAdded()) {
            return false;
        }
        if(mDetached) {
            return false;
        }
        return Utility.isValidContext(context);
    }
}
