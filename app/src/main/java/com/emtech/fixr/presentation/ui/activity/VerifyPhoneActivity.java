package com.emtech.fixr.presentation.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SMSBroadcastReceiver;
import com.emtech.fixr.presentation.ui.fragment.RateFixerFragment;
import com.emtech.fixr.presentation.ui.fragment.VerifyPhoneNumberFragment;
import com.emtech.fixr.presentation.viewmodels.LoginRegisterActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.LoginRegistrationViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

/**
 * This class handles the phone number verification
* */
public class VerifyPhoneActivity extends AppCompatActivity implements VerifyPhoneNumberFragment.OnVerifyPhoneInteractionListener,
        SMSBroadcastReceiver.OTCReceivedInterface, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = VerifyPhoneActivity.class.getSimpleName();
    SMSBroadcastReceiver smsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    public static VerifyPhoneActivity verifyPhoneActivity;
    private TextInputEditText phoneNumberEt;
    private Button sendButton, verifyCode;
    private LinearLayout verificationProgressContainer;
    private int user_id;
    private ProgressBar pBar;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        verifyPhoneActivity = this;

        user_id = getIntent().getIntExtra("user_id", 0);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.verify_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }

        //init the views
        getViewWidgets();

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this);
        loginRegisterActivityViewModel = new ViewModelProvider(this, factory).get(LoginRegisterActivityViewModel.class);

        //init the sms broadcast receiver
        smsBroadcastReceiver = new SMSBroadcastReceiver();

        //set google api client for hint request
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        smsBroadcastReceiver.setOnOTCListeners(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(smsBroadcastReceiver, intentFilter);

        //get the phone numbers already on the phone
        requestPhoneNumberHint();

        //send the phone number to the server
        sendButton.setOnClickListener(v -> {
            // Call server API for requesting OTP and when you got success start
            String phoneNumber = phoneNumberEt.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber)){
                phoneNumberEt.setError("Please enter your phone number");
            }else{
                loginRegisterActivityViewModel.sendPhoneNumber(user_id, phoneNumber);
            }

            // SMS Listener for listing auto read message listener
            startSmsListener();
        });

    }

    private void getViewWidgets(){
        sendButton = findViewById(R.id.btnSendNumber);
        phoneNumberEt = findViewById(R.id.phoneNumberET);
        verificationProgressContainer = findViewById(R.id.verifying_progress_container);
        pBar = findViewById(R.id.verifying_progress);
    }

    // Construct a request for phone numbers and show the picker
    private void requestPhoneNumberHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        }catch (IntentSender.SendIntentException e){
            e.printStackTrace();
        }

    }

    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId(); <-- E.164 format phone number on 10.2.+ devices
                phoneNumberEt.setText(credential.getId());
            }
        }
    }

    //listens for the sms sent to the users phone number
    public void startSmsListener(){
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(this);

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

        task.addOnFailureListener(e -> {
            // Failed to start retriever, inspect Exception for more details
            Log.e(TAG, "Failed to start retriever");
            Toast.makeText(VerifyPhoneActivity.this,
                    "An error occurred. Please try again",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        });

    }

    @Override
    public void onOTCReceived(String otc) {
        Log.e(TAG, "OTC received");
        //launch the verify phone number fragment
        VerifyPhoneNumberFragment rateFixerFragment = VerifyPhoneNumberFragment.newInstance(user_id, otc);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.verify_fragment_container, rateFixerFragment)
                .commit();
    }

    @Override
    public void onOTCTimeout() {
        Log.e(TAG, "Timeout waiting for OTC");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //this is callback from the VerifyPhoneFragment
    //gives us the otp code the user types in
    //send data to the server
    @Override
    public void onVerifyPhoneInteraction(int user_id, String otc) {
        Log.e(TAG, "User ID: "+user_id + "OTP: "+otc);
        loginRegisterActivityViewModel.verifyOtpReceived(user_id,otc);
    }

    //this is called in LoginUser when the phone number has been successfully sent
    public void onPhoneNumberSent(Boolean isNumberSent, String msg){
        Log.e(TAG, "Status: "+isNumberSent + "MSG: "+msg);
    }

    //this is called in LoginUser when the phone number has been successfully sent
    public void onOtpSent(Boolean isOtpSent, String msg){
        Log.e(TAG, "Status: "+isOtpSent + "MSG: "+msg);
    }

    private void showBar() {
        pBar.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        pBar.setVisibility(View.INVISIBLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
