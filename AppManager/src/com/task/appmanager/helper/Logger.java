package com.task.appmanager.helper;

import android.util.Log;

public class Logger {
	public static void logDebug(String text) {
		Log.d(Constants.TAG, text);
	}
	public static void logError(String text) {
		Log.e(Constants.TAG, text);
	}
}
