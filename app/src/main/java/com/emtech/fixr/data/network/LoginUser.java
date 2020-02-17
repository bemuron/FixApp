package com.emtech.fixr.data.network;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.ui.activity.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginUser {
    private static final String LOG_TAG = LoginUser.class.getSimpleName();

    private final AppExecutors mExecutors;
    SuccessfulLoginCallBack successfulLoginCallBack;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static LoginUser sInstance;
    private final Context mContext;
    private User mFixappUser;
    public static LoginActivity loginActivity;

    public LoginUser(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            successfulLoginCallBack = loginActivity.getInstance();
        } catch (ClassCastException e) {
            Log.d(LOG_TAG, e.getMessage());
            throw new ClassCastException(context.toString()
                    + " must implement onLoginSuccessful");
        }

    }

    /**
     * Get the singleton for this class
     */
    public static LoginUser getInstance(Context context, AppExecutors executors) {
        //successfulLoginCallBack = loginActivity.getInstance();
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new LoginUser(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    /**
     * Starts an intent service to log in the user.
     */
    public void startLoginUserService(String email, String password) {

        Intent intentToPost = new Intent(mContext, UserLoginIntentService.class);

        Bundle loginBundle = new Bundle();
        loginBundle.putString("email", email);
        loginBundle.putString("password", password);
        intentToPost.putExtras(loginBundle);

        mContext.startService(intentToPost);
        Log.d(LOG_TAG, "Login user service created");

        //return userLoginIntentService.isLoginSuccess();
    }

    public void UserLogIn(String email, String password) {
        Log.d(LOG_TAG, "User login started");

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.userLogin(email, password);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                //if response body is not null, we have some data
                if (response.body() != null) {
                    //count what we have in the response
                    if (!response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //response.body().getUser();

                        // If the code reaches this point, we have successfully posted the job
                        Log.d(LOG_TAG, "Successful login");

                        //create new user object
                        mFixappUser = new User();
                        mFixappUser.setUser_id(response.body().getUser().getUser_id());
                        mFixappUser.setEmail(response.body().getUser().getEmail());
                        mFixappUser.setCreated_on(response.body().getUser().getCreated_on());
                        mFixappUser.setRole(response.body().getUser().getRole());
                        mFixappUser.setDescription(response.body().getUser().getDescription());
                        mFixappUser.setPhone_number(response.body().getUser().getPhone_number());
                        mFixappUser.setProfile_pic(response.body().getUser().getProfile_pic());
                        mFixappUser.setDate_of_birth(response.body().getUser().getDate_of_birth());
                        mFixappUser.setGender(response.body().getUser().getGender());
                        mFixappUser.setName(response.body().getUser().getName());
                        mFixappUser.setPassword(response.body().getUser().getPassword());
                        Log.d(LOG_TAG, mFixappUser.getEmail() + " user email");

                        //insert user to the local db
                        //mRepository.insertUser(mFixappUser);

                        successfulLoginCallBack.onLoginSuccessful(true, mFixappUser);
                    }
                }else{
                    Log.e(LOG_TAG, "response.body() is null");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    /**
     * The interface that receives whether the login was successful or not
     */
    public interface SuccessfulLoginCallBack {
        void onLoginSuccessful(Boolean isLoginSuccessful, User user);
    }

}
