package com.emtech.fixr.presentation.ui.activity;

import android.app.ProgressDialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.emtech.fixr.R;

import com.emtech.fixr.app.Config;
import com.emtech.fixr.app.MyApplication;
import com.emtech.fixr.data.network.LoginUser;
import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.helpers.InputValidator;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.viewmodels.LoginRegisterActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.LoginRegistrationViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements LoginUser.SuccessfulLoginCallBack {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    public static LoginActivity loginActivityInstance;
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginActivityInstance = this;

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = ViewModelProviders.of
                (this, factory).get(LoginRegisterActivityViewModel.class);

        inputEmail = (EditText) findViewById(R.id.edit_text_email);
        inputPassword = (EditText) findViewById(R.id.edit_text_password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //first check if we have an internet connection
                if (isNetworkAvailable()) {
                    //validate user input details and
                    //attempt to login
                    attemptLogin();
                }else {
                    // show user that they may not be having an internet connection
                    Toast.makeText(getApplicationContext(),
                            "Something is not right, try checking your internet connection.", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }//close

    public static LoginActivity getInstance() {
        return loginActivityInstance;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        inputEmail.setError(null);
        inputPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        InputValidator inputValidator = new InputValidator();
        // Check for a valid password, if the user entered one.
        if ( !inputValidator.isPasswordValid(password)) {

            Toast toast = Toast.makeText(this, "This password is too short",Toast.LENGTH_LONG);

            toast.show();

            focusView = inputPassword;
            cancel = true;
        }
        //check if password field is empty
        if (TextUtils.isEmpty(password)) {

            Toast toast = Toast.makeText(this, "Password is required",Toast.LENGTH_LONG);

            toast.show();
            // mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = inputPassword;
            cancel = true;
        }

        //check if email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast toast = Toast.makeText(this, "Email is required",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputEmail;
            cancel = true;
            // Check for a valid email address.
        } else if (!inputValidator.isEmailValid(email)) {
            Toast toast = Toast.makeText(this, "This email address is invalid",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            pDialog.setMessage("Logging in ...");
            showDialog();

            //login the user
            //function to verify login details in mysql db
            //Calls viewmodel method
            loginRegisterActivityViewModel.loginUser(email,password);
            //checkLogin(email,password);

            /*pDialog.setMessage("Logging in ...");
            showDialog();*/

        }
    }

    /**
     * function to verify login details in mysql db
     * Calls viewmodel method
     * */
    private void checkLogin(final String email, final String password) {

        pDialog.setMessage("Logging in ...");
        showDialog();


        btnLogin.setClickable(false);

        Log.d(TAG, "User login started");

        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.userLogin(email, password);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                    //if response body is not null, we have some data
                    //successful login
                    if (!response.body().getError()) {
                        //Result resBean = response.body();

                        //Log.d("Response status", response.body().getMessage());

                        Log.d(TAG, "Successful login");

                        //create new user object
                        user = new User();
                        user.setUser_id(response.body().getUser().getUser_id());
                        user.setEmail(response.body().getUser().getEmail());
                        user.setCreated_on(response.body().getUser().getCreated_on());
                        user.setRole(response.body().getUser().getRole());
                        user.setDescription(response.body().getUser().getDescription());
                        user.setPhone_number(response.body().getUser().getPhone_number());
                        user.setProfile_pic(response.body().getUser().getProfile_pic());
                        user.setDate_of_birth(response.body().getUser().getDate_of_birth());
                        user.setGender(response.body().getUser().getGender());
                        user.setName(response.body().getUser().getName());
                        user.setPassword(response.body().getUser().getPassword());
                        Log.d(TAG, user.getEmail() + " user email");

                        //insert user to the local db
                        loginRegisterActivityViewModel.insert(user);

                        // user successfully logged in
                        // Create login session
                        session.createLoginSession(user.getUser_id(), user.getName(),
                                user.getEmail(), user.getRole(), user.getProfile_pic());

                        Toast.makeText(LoginActivity.this, "Welcome "+user.getName(), Toast.LENGTH_LONG).show();


                        //start home activity
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();

                    }else{
                        btnLogin.setClickable(true);

                        Toast.makeText(LoginActivity.this,response.body().getMessage(),Toast.LENGTH_SHORT).show();

                        Log.e(TAG, response.body().getMessage());

                    }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                hideDialog();
                //print out any error we may get
                //probably server connection
                Log.e(TAG, t.getMessage());

                Toast toast = Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT);

                toast.show();

                btnLogin.setClickable(true);
            }
        });

    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //callback from the login user service
    @Override
    public void onLoginSuccessful(Boolean isLoginSuccessful, User user) {
        if (isLoginSuccessful){
            hideDialog();
            Log.d(TAG, "Successful login");
            //insert user to the local db
            loginRegisterActivityViewModel.insert(user);
            // user successfully logged in
            // Create login session
            session.createLoginSession(user.getUser_id(), user.getName(),
                    user.getEmail(), user.getRole(), user.getProfile_pic());

            Toast.makeText(LoginActivity.this, "Welcome "+user.getName(), Toast.LENGTH_LONG).show();

            /**
             * Always check for google play services availability before
             * proceeding further with FCM
             * */

            if (checkPlayServices()) {
                registerFCM(user.getUser_id());
            }

            //start home activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }else{
            hideDialog();
            Log.d(TAG, "login not successful");
            //display any error msg that may be received
            Toast.makeText(LoginActivity.this, "login not successful",
                    Toast.LENGTH_LONG).show();
            btnLogin.setClickable(true);
        }

    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     * @param userId the current user logged in id
     */
    private void sendRegistrationToServer(int userId, String token) {

        //call model view method to update fcm
        loginRegisterActivityViewModel.updateFCM(userId,token);
    }

    //this methos is called by the repository method after the FCM id has been
    //saved in the db
    public void updateUIAfterFcmUpdate(Boolean isUpdated, String message){
        if (isUpdated){
            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(LoginActivity.this).sendBroadcast(registrationComplete);

            Log.d(TAG, "Successful device fcm id registration");
        }else{
            Log.e(TAG, message);
            Log.e(TAG, "Unable to send fcm registration id to our sever.");
            //Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Registering with FCM and obtaining the fcm registration id
     */
    private void registerFCM(int userId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try{
            String token = MyApplication.getInstance().getPrefManager().getDeviceToken();
            Log.e(TAG, "FCM Registration Token: " + token);

            sendRegistrationToServer(userId, token);

            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true).apply();

        }catch (Exception e){
            Log.e(TAG, "Failed to complete token refresh", e);

            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, false).apply();
        }

    }

    //check for google play services in device
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }
}
