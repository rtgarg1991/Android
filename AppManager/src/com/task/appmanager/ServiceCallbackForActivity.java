package com.task.appmanager;

import android.os.Handler;

public interface ServiceCallbackForActivity {
	void setAdapterInfo(ServiceCallbackForAdapter callback, int type, Handler handler);

	void requestAppsList(int typeUpdated);

	void refreshAppsList(int type);
}
