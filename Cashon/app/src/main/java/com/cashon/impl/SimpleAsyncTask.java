package com.cashon.impl;

import android.os.AsyncTask;

/**
 * Created by Rohit on 6/13/2015.
 */
public class SimpleAsyncTask extends AsyncTask<Object, Object, Object> {

    private SimpleAsyncTaskCallbacks callback = null;

    public interface SimpleAsyncTaskCallbacks {
        public void onPreExecute();
        public Object doInBackground(Object... params);
        public void onPostExecute(Object o);
        public void onProgressUpdate(Object... values);
        public void onCancelled(Object o);
        public void onCancelled();
    }

    public SimpleAsyncTask(SimpleAsyncTaskCallbacks callback) {
        this.callback = callback;
    }
    @Override
    protected Object doInBackground(Object... params) {
        return callback.doInBackground(params);
    }

    @Override
    protected void onPreExecute() {
        callback.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        callback.onPostExecute(o);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        callback.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Object o) {
        callback.onCancelled(o);
    }

    @Override
    protected void onCancelled() {
        callback.onCancelled();
    }
}
