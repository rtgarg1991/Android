package com.cashon.helper;

import android.os.Handler;
import android.os.Message;

/**
 *
 * Created by Rohit on 6/8/2015.
 */
public class DelayHandler extends Handler{

    // Singleton instance of DelayHandler
    private static DelayHandler handler;

    @Override
    public void handleMessage(Message message) {
        DelayHandlerCallback callback = (DelayHandlerCallback)message.obj;
        callback.handleDelayedHandlerCallback();
    }

    /**
     * get DelayHandler Class object to run your callback after specific time
     * @return DelayHandler object
     */
    public static DelayHandler getInstance() {
        if(handler == null) {
            synchronized (DelayHandler.class) {
                if(handler == null) {
                    handler = new DelayHandler();
                }
            }
        }
        return handler;
    }

    /**
     * Provide DelayHandlerCallback and delay time
     * DelayHandlerCallback.handleDelayedHandlerCallback() function will be called after this much time
     * This is UI dependant, if UI thread is busy, this call can delay more
     * @param callback
     * @param delay
     */
    public void startDelayed(DelayHandlerCallback callback, int delay) {
        Message message = this.obtainMessage();
        message.obj = callback;
        this.sendMessageDelayed(message, delay);
    }

    /**
     * Implement this callback and use DelayHandler class to run UI task after specified time
     */
    public static interface DelayHandlerCallback {
        void handleDelayedHandlerCallback();
    }
}
