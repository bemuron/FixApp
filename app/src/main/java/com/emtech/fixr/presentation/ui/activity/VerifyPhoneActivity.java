package com.emtech.fixr.presentation.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SMSBroadcastReceiver;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * This class handles the phone number verification
* */
public class VerifyPhoneActivity extends AppCompatActivity implements SMSBroadcastReceiver.OTCReceivedInterface {
    private static final String TAG = VerifyPhoneActivity.class.getSimpleName();
    SMSBroadcastReceiver smsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    private EditText inputMobNumEt, inputOtcEt;
    private Button sendPhoneNumber, verifyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        //init the sms broadcast receiver
        smsBroadcastReceiver = new SMSBroadcastReceiver();
        smsBroadcastReceiver.setOnOTCListeners(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(smsBroadcastReceiver, intentFilter);

        //get the phone numbers already on the phone
        requestPhoneNumberHint();

    }

    // Construct a request for phone numbers and show the picker
    private void requestPhoneNumberHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        //PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
          //      apiClient, hintRequest);
        /*try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        }catch (IntentSender.SendIntentException e){
            e.printStackTrace();
        }*/

    }

    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId(); <-- E.164 format phone number on 10.2.+ devices
                inputMobNumEt.setText(credential.getId());
            }
        }
    }

    //listens for the sms sent to the users phone number
    public void startSmsListener(){
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Successfully started retriever, expect broadcast intent
                Log.e(TAG, "Successfully started retriever");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever, inspect Exception for more details
                Log.e(TAG, "Failed to start retriever");
                Toast.makeText(VerifyPhoneActivity.this,
                        "An error occurred. Please try again",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onOTCReceived(String otc) {
        Log.e(TAG, "OTC received");
        inputOtcEt.setText(otc);
    }

    @Override
    public void onOTCTimeout() {
        Log.e(TAG, "Timeout waiting for OTC");
    }
}
