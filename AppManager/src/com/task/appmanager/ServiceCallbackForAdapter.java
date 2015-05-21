package com.task.appmanager;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface ServiceCallbackForAdapter {

	public void setData(List<?> data);

	static class UpdatedApp {
		String name;
		Drawable icon;
		Bitmap bitmapIcon;
		String date;
		int noOfTimes;

		public void setName(String name) {
			this.name = name;
		}
		public void setIcon(Drawable icon) {
			this.icon = icon;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public void setNoOfTimes(int noOfTimes) {
			this.noOfTimes = noOfTimes;
		}
		public void setBitmap(Bitmap bitmapIcon) {
			this.bitmapIcon = bitmapIcon;
		}
	}
}
