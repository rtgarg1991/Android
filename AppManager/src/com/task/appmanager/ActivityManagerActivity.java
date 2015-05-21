package com.task.appmanager;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.task.appmanager.helper.Constants;
import com.task.appmanager.service.AppService;

@SuppressWarnings("deprecation")
public class ActivityManagerActivity extends FragmentActivity
		implements ServiceCallbackForActivity, ServiceConnection {

	ViewPager mViewPager;
	HashMap<Integer, AdapterInfo> mHashmap = new HashMap<Integer, AdapterInfo>();
	CustomHandler mHandler = new CustomHandler();
	boolean mBound;
	AppService.LocalBinder mBinder = null;

	class CustomHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case Constants.TYPE_UPDATED:
				if(!mBound || mBinder == null) {
					bindAppService();
					sendEmptyMessageDelayed(Constants.TYPE_UPDATED, Constants.DELAYED_TIME);
				} else {
					mBinder.getUpdatedApps(mHashmap.get(Constants.TYPE_UPDATED).callback);
				}
				break;
			case Constants.TYPE_UNINSTALLED:
				if(!mBound || mBinder == null) {
					bindAppService();
					sendEmptyMessageDelayed(Constants.TYPE_UNINSTALLED, Constants.DELAYED_TIME);
				} else {
					mBinder.getUninstalledApps(mHashmap.get(Constants.TYPE_UNINSTALLED).callback);
				}
				break;
			}
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create a view pager which will be the host of our Activity
		mViewPager = new ViewPager(this);
		// as it is dynamically created, set its unique id
		mViewPager.setId(Constants.VIEW_PAGER_ID);
		// set this view pager as content view for our Activity
		setContentView(mViewPager);

		// get action bar and set its navigation mode as Navigation Mode Tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Lets create one Adapter for Action Bars' tabs
		ActivityTabAdapter adapter = new ActivityTabAdapter(this, mViewPager);
		// lets add three tabs for our adapter
		adapter.addTab(actionBar.newTab().setText(
				getResources().getString(R.string.title_tab_installed)), Constants.TYPE_INSTALLED);
		adapter.addTab(actionBar.newTab().setText(
				getResources().getString(R.string.title_tab_updated)), Constants.TYPE_UPDATED);
		adapter.addTab(actionBar.newTab().setText(
				getResources().getString(R.string.title_tab_uninstalled)), Constants.TYPE_UNINSTALLED);

		// we are done, Adapter will handle everything else

		bindAppService();

	}

	private void bindAppService() {
		// lets just bind with service
		Intent intent = new Intent(this, AppService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mBound) {
			unbindService(this);
			mBound = false;
		}
	}

	static class ActivityTabAdapter extends FragmentPagerAdapter
			implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

		static final class TagHolder {
			private int type;
			private int index;
			TagHolder(int type, int index) {
                this.type = type;
                this.index = index;
            }
		}

		Context context; // context for inflating the data
		ViewPager viewPager; // view pager which will handle the tabs' data
		ActionBar actionBar; // action bar which will host these tabs
		ArrayList<Tab> tabs = new ArrayList<ActionBar.Tab>(); // all the tabs

		public ActivityTabAdapter(FragmentActivity activity,
				ViewPager viewPager) {
			// send fragment manager to super class
			super(activity.getSupportFragmentManager());

			// save provided info in local objects
			this.context = activity;
			this.viewPager = viewPager;
			this.actionBar = activity.getActionBar();

			// set adapter for our viewPager and also OnPageChangeListener
			this.viewPager.setAdapter(this);
			this.viewPager.setOnPageChangeListener(this);
		}

		public void addTab(Tab tab, int type) {
			TagHolder holder = new TagHolder(type, tabs.size());

			// set unique tag for this tab to easily get its information at runtime
			tab.setTag(holder);

			// set this class to handle tab listener for this tab 
			tab.setTabListener(this);
			// add this tab in local object handling all the tabs
			this.tabs.add(tab);

			// add this tab to action bar
			this.actionBar.addTab(tab);
			// notify data set about new tab addition
			notifyDataSetChanged();
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// no need to handle this for now
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// no need to handle this for now
			
		}

		@Override
		public void onPageSelected(int pos) {
			// set this position in action bar also
			this.actionBar.setSelectedNavigationItem(pos);
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// get position of this tab from its tag
			TagHolder tag = (TagHolder)tab.getTag();
			// set same position in View Pager also
			this.viewPager.setCurrentItem(tag.index);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// no need to handle this for now
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// no need to handle this for now
		}

		@Override
		public Fragment getItem(int position) {
			TagHolder holder = (TagHolder) tabs.get(position).getTag();
			int type = holder.type;
			return new AppFragment(type);
		}

		@Override
		public int getCount() {
			return tabs.size();
		}
	}

	static class AppFragment extends Fragment {
		int type;
		public AppFragment(int type) {
			this.type = type;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			GridView view = (GridView)inflater.inflate(R.layout.fragment_pager, container, false);
			if(type == Constants.TYPE_INSTALLED) {
				view.setColumnWidth((int) getResources().getDimension(R.dimen.grid_view_installed_app_width));
			} else {
				view.setNumColumns(1);
			}
			view.setAdapter(AbstractBaseAdapter.getAdapter(type, getActivity()));
			return view;
		}

	}

	static class AdapterInfo {
		ServiceCallbackForAdapter callback;
		Handler handler;

		public AdapterInfo(ServiceCallbackForAdapter callback, Handler handler) {
			this.callback = callback;
			this.handler = handler;
		}
	}

	@Override
	public void setAdapterInfo(ServiceCallbackForAdapter callback, int type, Handler handler) {
		mHashmap.put(type, new AdapterInfo(callback, handler));
	}

	@Override
	public void requestAppsList(int type) {
		mHandler.sendEmptyMessage(type);
	}

	@Override
	public void refreshAppsList(int type) {
		switch(type) {
		case Constants.MESSAGE_ADD_PACKAGE:
			if(mHashmap != null) {
				AdapterInfo info = mHashmap.get(Constants.TYPE_INSTALLED);
				if(info != null) {
					Handler handler = info.handler;
					if(handler != null) {
						handler.sendEmptyMessage(Constants.NOTIFY_DATA_CHANGED);
					}
				}
			}
			break;
		case Constants.MESSAGE_DELETE_PACKAGE:
			if(mHashmap != null) {
				AdapterInfo info = mHashmap.get(Constants.TYPE_UNINSTALLED);
				if(info != null) {
					Handler handler = info.handler;
					if(handler != null) {
						handler.sendEmptyMessage(Constants.NOTIFY_DATA_CHANGED);
					}
				}
			}
			break;
		case Constants.MESSAGE_UPDATE_PACKAGE:
			if(mHashmap != null) {
				AdapterInfo info = mHashmap.get(Constants.TYPE_UPDATED);
				if(info != null) {
					Handler handler = info.handler;
					if(handler != null) {
						handler.sendEmptyMessage(Constants.NOTIFY_DATA_CHANGED);
					}
				}
			}
			break;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mBound = true;
		mBinder = (AppService.LocalBinder)service;
		mBinder.setActivityCallbacks(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mBound = false;
		mBinder = null;
	}
}
