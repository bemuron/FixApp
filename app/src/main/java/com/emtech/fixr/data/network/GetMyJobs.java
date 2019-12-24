package com.emtech.fixr.data.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.Categories;
import com.emtech.fixr.models.UserJobs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMyJobs {
    private static final String LOG_TAG = GetMyJobs.class.getSimpleName();

    // LiveData storing the latest downloaded weather forecasts
    private final MutableLiveData<List<Job>> mDownloadedJobs;
    private final AppExecutors mExecutors;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static GetMyJobs sInstance;
    private final Context mContext;

    public GetMyJobs(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedJobs = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static GetMyJobs getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new GetMyJobs(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public LiveData<List<Job>> getJobsForUser() {
        return mDownloadedJobs;
    }

    /**
     * Starts an intent service to fetch the categories.
     */
    public void startFetchCategoryService() {
        Intent intentToFetch = new Intent(mContext, FixAppSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "Fetch categories service created");
    }

    public void GetJobs(int user_id) {
        Log.d(LOG_TAG, "Fetch user jobs started");

            //Defining retrofit com.emtech.retrofitexample.api service
            //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.getAllJobsForUser(user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null) {
                    Log.d(LOG_TAG, "JSON not null");

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mDownloadedJobs.postValue(response.body().getUserJobs());

                    // If the code reaches this point, we have successfully performed our sync
                    Log.d(LOG_TAG, "Successfully got all jobs associated to this user");
                }
            }

            @Override
            public void onFailure(Call<UserJobs> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
            }
        });

    }

}
