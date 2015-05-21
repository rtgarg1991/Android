package com.task.appmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.task.appmanager.helper.Constants;

public class AbstractBaseAdapter {

	static BaseAdapter getAdapter(int type, Context context) {
		switch(type) {
		case Constants.TYPE_INSTALLED:
			return new InstalledAppsBaseAdapter(context);
		case Constants.TYPE_UPDATED:
			return new UpdatedAppsBaseAdapter(context);
		case Constants.TYPE_UNINSTALLED:
			return new UninstalledAppsBaseAdapter(context);
		}
		return null;
	}

	private static class InstalledAppsBaseAdapter extends BaseAdapter {

		private static class InstalledApp {
			String name;
			Drawable icon;

			private void setName(String name) {
				this.name = name;
			}
			private void setIcon(Drawable icon) {
				this.icon = icon;
			}
		}

		private static class ViewHolder {
			ImageView imageView;
			TextView textView;
			int position;
		}

		class CustomHandler extends Handler {

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case Constants.NOTIFY_DATASET_CHANGED:
					notifyDataSetChanged();
					break;
				case Constants.NOTIFY_DATA_CHANGED:
					getInstalledApps(context);
					break;
				}
			}
			
		}

		Context context;
		List<InstalledApp> installedApps = new ArrayList<InstalledApp>();
		CustomHandler handler = new CustomHandler();

		public InstalledAppsBaseAdapter(final Context context) {
			this.context = context;

			ServiceCallbackForActivity callback = (ServiceCallbackForActivity)context;
			callback.setAdapterInfo(null, Constants.TYPE_INSTALLED, handler);

			getInstalledApps(context);
		}

		private void getInstalledApps(final Context context) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// get package manager for querying all data
					PackageManager pm = context.getPackageManager();
					// get all installed applications
					List<ApplicationInfo> appInfos = pm.getInstalledApplications(0);
					// loop through all the apps
					for(ApplicationInfo info : appInfos) {
						// if not system app, only then show it in GridView
						if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
							InstalledApp app = new InstalledApp();
							app.setName((String) (info != null ? pm.getApplicationLabel(info) : "(unknown)"));
							app.setIcon(info.loadIcon(pm));
							installedApps.add(app);
						} else {
							// system application
							// skipping for now
							// lots of faltu applications and services
						}
					}

					handler.sendEmptyMessage(Constants.NOTIFY_DATASET_CHANGED);
				}
			}).start();
		}

		@Override
		public int getCount() {
			return installedApps.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = convertView != null ? (ViewHolder) convertView.getTag() : null;
			// check if we need to inflate layout xml
			if(convertView == null || holder == null
					|| (holder != null && holder.position != position)) {
				// inflate layout xml for single entry
				convertView = LayoutInflater.from(this.context).inflate(R.layout.installed_item, null);
				// create view holder for this item and set its values
				holder = new ViewHolder();
				holder.imageView = (ImageView)convertView.findViewById(R.id.installedAppImageView);
				holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.textView = (TextView)convertView.findViewById(R.id.installedAppTextView);
				holder.position = position;

				convertView.setTag(holder);
			}
			// assign data to view holder and which will reflect in inflate view as well
			holder.imageView.setBackground(installedApps.get(position).icon);
			holder.textView.setText(installedApps.get(position).name);
			return convertView;
		}
		
	}

	private static class UpdatedAppsBaseAdapter extends BaseAdapter
			implements ServiceCallbackForAdapter {

		private static class ViewHolder {
			ImageView imageView;
			TextView nameTextView;
			TextView noTimesTextView;
			TextView dateUpdatedTextView;
			int position;
		}

		class CustomHandler extends Handler {

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case Constants.NOTIFY_DATASET_CHANGED:
					notifyDataSetChanged();
					break;
				case Constants.NOTIFY_DATA_CHANGED:
					callback.requestAppsList(Constants.TYPE_UPDATED);
					break;
				}
			}
			
		}

		Context context;
		List<UpdatedApp> updatedApps = new ArrayList<UpdatedApp>();
		CustomHandler handler = new CustomHandler();
		ServiceCallbackForActivity callback;

		public UpdatedAppsBaseAdapter(final Context context) {
			this.context = context;
			callback = (ServiceCallbackForActivity)context;
			callback.setAdapterInfo(this, Constants.TYPE_UPDATED, handler);
			callback.requestAppsList(Constants.TYPE_UPDATED);
		}

		@Override
		public int getCount() {
			return updatedApps.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = convertView != null ? (ViewHolder) convertView.getTag() : null;
			// check if we need to inflate layout xml
			if(convertView == null || holder == null
					|| (holder != null && holder.position != position)) {
				// inflate layout xml for single entry
				convertView = LayoutInflater.from(this.context).inflate(R.layout.updated_item, null);
				// create view holder for this item and set its values
				holder = new ViewHolder();
				holder.imageView = (ImageView)convertView.findViewById(R.id.updatedAppImageView);
				holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

				holder.nameTextView = (TextView)convertView.findViewById(R.id.updatedAppNameTextView);
				holder.noTimesTextView = (TextView)convertView.findViewById(R.id.updatedAppTimesTextView);
				holder.dateUpdatedTextView = (TextView)convertView.findViewById(R.id.updatedAppDateTextView);
				holder.position = position;

				convertView.setTag(holder);
			}
			// assign data to view holder and which will reflect in inflate view as well
			holder.imageView.setBackground(updatedApps.get(position).icon);
			holder.nameTextView.setText(updatedApps.get(position).name);
			holder.noTimesTextView.setText(String.format(Constants.UPDATED_STRING, String.valueOf(updatedApps.get(position).noOfTimes)));
			holder.dateUpdatedTextView.setText(updatedApps.get(position).date);
			return convertView;
		}

		@Override
		public void setData(List<?> data) {
			updatedApps = (List<UpdatedApp>) data;
			handler.sendEmptyMessage(Constants.NOTIFY_DATASET_CHANGED);
		}
		
	}

	private static class UninstalledAppsBaseAdapter extends BaseAdapter
			implements ServiceCallbackForAdapter {

		private static class ViewHolder {
			ImageView imageView;
			TextView nameTextView;
			TextView dateUpdatedTextView;
			int position;
		}

		class CustomHandler extends Handler {

			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case Constants.NOTIFY_DATASET_CHANGED:
					notifyDataSetChanged();
					break;
				case Constants.NOTIFY_DATA_CHANGED:
					callback.requestAppsList(Constants.TYPE_UNINSTALLED);
					break;
				}
			}
			
		}

		Context context;
		List<UpdatedApp> uninstalledApps = new ArrayList<UpdatedApp>();
		CustomHandler handler = new CustomHandler();
		ServiceCallbackForActivity callback;

		public UninstalledAppsBaseAdapter(final Context context) {
			this.context = context;
			callback = (ServiceCallbackForActivity)context;
			callback.setAdapterInfo(this, Constants.TYPE_UNINSTALLED, handler);
			callback.requestAppsList(Constants.TYPE_UNINSTALLED);
		}

		@Override
		public int getCount() {
			return uninstalledApps.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = convertView != null ? (ViewHolder) convertView.getTag() : null;
			// check if we need to inflate layout xml
			if(convertView == null || holder == null
					|| (holder != null && holder.position != position)) {
				// inflate layout xml for single entry
				convertView = LayoutInflater.from(this.context).inflate(R.layout.uninstalled_item, null);
				// create view holder for this item and set its values
				holder = new ViewHolder();
				holder.imageView = (ImageView)convertView.findViewById(R.id.uninstalledAppImageView);
				holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

				holder.nameTextView = (TextView)convertView.findViewById(R.id.uninstalledAppNameTextView);
				holder.dateUpdatedTextView = (TextView)convertView.findViewById(R.id.uninstalledAppDateTextView);
				holder.position = position;

				convertView.setTag(holder);
			}
			// assign data to view holder and which will reflect in inflate view as well
			if(uninstalledApps.get(position).icon != null) {
				holder.imageView.setBackground(uninstalledApps.get(position).icon);
			} else if(uninstalledApps.get(position).bitmapIcon != null) {
				holder.imageView.setImageBitmap(uninstalledApps.get(position).bitmapIcon);
			}
			holder.nameTextView.setText(uninstalledApps.get(position).name);
			holder.dateUpdatedTextView.setText(uninstalledApps.get(position).date);
			return convertView;
		}

		@Override
		public void setData(List<?> data) {
			uninstalledApps = (List<UpdatedApp>) data;
			handler.sendEmptyMessage(Constants.NOTIFY_DATASET_CHANGED);
		}
		
	}
}
