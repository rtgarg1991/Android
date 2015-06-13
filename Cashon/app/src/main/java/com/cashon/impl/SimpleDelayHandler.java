package com.cashon.impl;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 *
 * Created by Rohit on 6/8/2015.
 */
public class SimpleDelayHandler {

    // Singleton instance of SimpleDelayHandler
    private static SimpleDelayHandler handler;

    // instance for UI Handler
    private Handler mUIHandler;
    // instance of Non-UI Handler
    private Handler mNonUIHandler;

    private static class UIHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            SimpleDelayHandlerCallback callback = (SimpleDelayHandlerCallback)message.obj;
            callback.handleDelayedHandlerCallback();
        }
    }

    private static class NonUIHandler extends Handler {
        public NonUIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            SimpleDelayHandlerCallback callback = (SimpleDelayHandlerCallback)message.obj;
            callback.handleDelayedHandlerCallback();
        }
    }

    private SimpleDelayHandler() {
        mUIHandler = new UIHandler();

        HandlerThread thread = new HandlerThread("ABC");
        thread.start();

        mNonUIHandler = new NonUIHandler(thread.getLooper());
    }

    /**
     * get SimpleDelayHandler Class object to run your callback after specific time
     * @return SimpleDelayHandler object
     */
    public static SimpleDelayHandler getInstance() {
        if(handler == null) {
            synchronized (SimpleDelayHandler.class) {
                if(handler == null) {
                    handler = new SimpleDelayHandler();
                }
            }
        }
        return handler;
    }

    /**
     * Provide SimpleDelayHandlerCallback and delay time
     * SimpleDelayHandlerCallback.handleDelayedHandlerCallback() function will be called after this much time
     * This is UI dependant, if UI thread is busy, this call can delay more
     * @param callback Callback instance which will receive call to handleDelayedHandlerCallback
     * @param delay Delay in milliseconds after which you want to receive callback
     * @param needUICallback whether the callback should be in UI Thread of your Application or in Non-UI thread
     */
    public void startDelayed(SimpleDelayHandlerCallback callback, int delay, boolean needUICallback) {
        if(needUICallback) {
            Message message = mUIHandler.obtainMessage();
            message.obj = callback;
            mUIHandler.sendMessageDelayed(message, delay);
        } else {
            Message message = mNonUIHandler.obtainMessage();
            message.obj = callback;
            mNonUIHandler.sendMessageDelayed(message, delay);
        }
    }

    /**
     * Implement this callback and use SimpleDelayHandler class to run UI task after specified time
     */
    public interface SimpleDelayHandlerCallback {
        void handleDelayedHandlerCallback();
    }
}
