package com.fireballlabs.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.fireballlabs.cashguru.R;

import java.util.List;

/**
 * Created by Rohit on 6/16/2015.
 */
public class MainDrawerAdapter extends RecyclerView.Adapter<MainDrawerAdapter.ViewHolder> {
    DrawerAdapterCallbacks mCallback = null;
    List<MainAppFeature> mFeatureList = null;

    @Override
    public MainDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.drawer_list_item, parent, false);
        TextView tv = (TextView) v.findViewById(R.id.drawer_list_item_text_view);
        ImageView imageView = (ImageView) v.findViewById(R.id.drawer_list_item_image_view);
        return new ViewHolder(v, tv, imageView);
    }

    @Override
    public void onBindViewHolder(MainDrawerAdapter.ViewHolder holder, final int position) {
        holder.textView.setText(mFeatureList.get(position).title);
        holder.imageView.setImageResource(mFeatureList.get(position).iconId);
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.setEnabled(mFeatureList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFeatureList.size();
    }

    public MainDrawerAdapter(List<MainAppFeature> featuresList, DrawerAdapterCallbacks callback) {
        mFeatureList = featuresList;
        mCallback = callback;
    }

    /**
     * This will be the callback our MainActivity will be handling
     * For any selection made through drawer list, MainActivity will get a call
     * with {@link MainAppFeature} and it will have to show fragment for this selected Feature
     */
    public interface DrawerAdapterCallbacks {
        void setEnabled(MainAppFeature feature);
    }

    /**
     * This will contain the list of all the available Features in the app
     * Mostly these features will be a part of the drawer list
     * There can be more features which might not be handled from the drawer list
     */
    public static class MainAppFeature {
        private final int iconId;
        public String title;
        public int id;

        /**
         * New Feature to be added
         * @param title Title of the Feature which will be shown in the Drawer List
         * @param id Unique id of the Feature, which can be saved by user as default feature for the app
         */
        public MainAppFeature(String title, int id, int iconId) {
            this.title = title;
            this.id = id;
            this.iconId = iconId;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView = null;
        ImageView imageView = null;
        View parent = null;

        public ViewHolder(View parent, TextView textView, ImageView imageView) {
            super(parent);
            this.textView = textView;
            this.imageView = imageView;
            this.parent = parent;
        }
    }
}
