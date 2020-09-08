package com.emtech.fixr.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

/**
 * This class receives and broadcasts the message received from the server
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 * */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = SMSBroadcastReceiver.class.getSimpleName();
    private OTCReceivedInterface receiverInterface;

    public void setOnOTCListeners(OTCReceivedInterface otcReceivedInterface){
        this.receiverInterface = otcReceivedInterface;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    Log.d(TAG, "Message received: success "+message);
                    if (receiverInterface != null) {
                        String otc = message.replace("<#> Your verification code is : ", "");
                        receiverInterface.onOTCReceived(otc);
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    Log.e(TAG, "onReceive: failure");
                    if (receiverInterface != null) {
                        receiverInterface.onOTCTimeout();
                    }
                    break;
            }
        }
    }

    public interface OTCReceivedInterface {
        void onOTCReceived(String otc);
        void onOTCTimeout();
    }

}
