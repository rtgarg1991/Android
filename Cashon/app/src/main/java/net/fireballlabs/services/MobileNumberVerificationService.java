package net.fireballlabs.services;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import net.fireballlabs.helper.Logger;
import net.fireballlabs.impl.SimpleDelayHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MobileNumberVerificationService extends Service implements SimpleDelayHandler.SimpleDelayHandlerCallback {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_MOBILE_NUMBER_VERIFICATION = "net.fireballlabs.services.action.mobile.verification";
    public static final String EXTRA_PARAM_MOBILE_NUMBER = "net.fireballlabs.services.extra.mobile";
    public static final String EXTRA_PARAM_RECEIVER = "net.fireballlabs.services.action.receiver";
    public static final String EXTRA_PARAM_RECEIVER_RESULT = "net.fireballlabs.services.extra.mobile";

    private static final int TIMEOUT = 10000;
    public static final String SENDER_ID = "ALERTS";
    /*public static final String AUTH_KEY = "87349AB0G1ulXlr55893879";*/
    public static final String ROUTE = "4";

    int mStartId;
    int mVerificationCode;
    String mMobileNumber;
    String mReceiver;

    BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.doSecureLogging(Log.DEBUG, "Mobile Number Verification msg received");
            Object[] pdus = (Object[])intent.getExtras().get("pdus");
            SmsMessage shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);

            Logger.doSecureLogging(Log.DEBUG, "SMS message sender: "+
                    shortMessage.getOriginatingAddress());
            Logger.doSecureLogging(Log.DEBUG, "SMS message text: "+
                    shortMessage.getDisplayMessageBody());

            if(mReceiver != null && !mReceiver.equals("") && ("Your Verification code is " + mVerificationCode).equals(shortMessage.getDisplayMessageBody())) {
                Intent receiverIntent = new Intent(mReceiver);
                receiverIntent.putExtra(EXTRA_PARAM_RECEIVER_RESULT, true);
                LocalBroadcastManager.getInstance(MobileNumberVerificationService.this).sendBroadcast(receiverIntent);
            }

            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MOBILE_NUMBER_VERIFICATION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM_MOBILE_NUMBER);
                final String param2 = intent.getStringExtra(EXTRA_PARAM_RECEIVER);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleMobileNumberVerification(param1, param2);
                    }
                });
                thread.start();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.doSecureLogging(Log.DEBUG, "Mobile Number Verification Service destroyed");
        // if mobile number is not null, means we have registered this receiver
        if(mMobileNumber != null) {
            unregisterReceiver(smsReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleMobileNumberVerification(String mobile, String receiver) {

        if((mVerificationCode < 100000 || mVerificationCode > 999999) || !mobile.equals(mMobileNumber)) {
            Random random = new Random();
            int verificationCode = (int) (100000 * random.nextFloat());
            mVerificationCode = verificationCode;
            mMobileNumber = mobile;
            mReceiver = receiver;
        }

        // Authentication key
        String authkey = "";//AUTH_KEY;
        //Multiple mobiles numbers separated by comma
        String mobiles = mobile;
        //Sender ID,While using route4 sender id should be 6 characters long.
        String senderId = SENDER_ID;
        //Your message to send, Add URL encoding here.
        String message = "Your Verification code is " + mVerificationCode;
        //define route
        String route = ROUTE;

        URLConnection myURLConnection = null;
        URL myURL = null;
        BufferedReader reader = null;

        //encoding message
        String encoded_message= URLEncoder.encode(message);
        //Send SMS API
        String mainUrl="http://api.msg91.com/api/sendhttp.php?";

        //Prepare parameter string
        StringBuilder sbPostData= new StringBuilder(mainUrl);
        sbPostData.append("authkey=" + authkey);
        sbPostData.append("&mobiles=" + mobiles);
        sbPostData.append("&message=" + encoded_message);
        sbPostData.append("&route=" + route);
        sbPostData.append("&sender=" + senderId);

        //final string
        mainUrl = sbPostData.toString();
        Logger.doSecureLogging(Log.DEBUG, mainUrl);
        try {
            //prepare connection
            myURL = new URL(mainUrl);
            myURLConnection = myURL.openConnection();
            myURLConnection.connect();
            reader= new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));

            //reading response
            String response;
            while ((response = reader.readLine()) != null)
                //print response
                Logger.doSecureLogging(Log.DEBUG, "response = " + response);

            //finally close connection
            reader.close();

            // now lets register for sms reading
            registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SimpleDelayHandler handler = SimpleDelayHandler.getInstance(null);
            handler.startDelayed(this, TIMEOUT, false);
        }
    }

    @Override
    public void handleDelayedHandlerCallback() {
        Logger.doSecureLogging(Log.DEBUG, "Mobile Number Verification Timeout");
        if(mReceiver != null && !mReceiver.equals("")) {
            Intent receiverIntent = new Intent(mReceiver);
            receiverIntent.putExtra(EXTRA_PARAM_RECEIVER_RESULT, false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(receiverIntent);
        }
        stopSelf();
    }
}
