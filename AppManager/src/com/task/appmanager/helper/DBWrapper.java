package com.task.appmanager.helper;

public class DBWrapper {

	public static class Apps {
		private int id, uid;
		String packageName, logo;
		boolean available;

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the uid
		 */
		public int getUid() {
			return uid;
		}
		/**
		 * @param uid the uid to set
		 */
		public void setUid(int uid) {
			this.uid = uid;
		}

		/**
		 * @return the packageName
		 */
		public String getPackageName() {
			return packageName;
		}
		/**
		 * @param packageName the packageName to set
		 */
		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		/**
		 * @return the logo
		 */
		public String getLogo() {
			return logo;
		}
		/**
		 * @param logo the logo to set
		 */
		public void setLogo(String logo) {
			this.logo = logo;
		}

		/**
		 * @return the available
		 */
		public boolean isAvailable() {
			return available;
		}
		/**
		 * @param available the available to set
		 */
		public void setAvailable(boolean available) {
			this.available = available;
		}
	}

	public static class Updates {
		private int id, uid, timesUpdated;
		String date;

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the timesUpdated
		 */
		public int getNumberTimesUpdated() {
			return timesUpdated;
		}
		/**
		 * @param timesUpdated the timesUpdated to set
		 */
		public void setNumberTimesUpdated(int timesUpdated) {
			this.timesUpdated = timesUpdated;
		}

		/**
		 * @return the uid
		 */
		public int getUid() {
			return uid;
		}
		/**
		 * @param uid the uid to set
		 */
		public void setUid(int uid) {
			this.uid = uid;
		}

		/**
		 * @return the date
		 */
		public String getDate() {
			return date;
		}
		/**
		 * @param date the date to set
		 */
		public void setDate(String date) {
			this.date = date;
		}
	}
}
