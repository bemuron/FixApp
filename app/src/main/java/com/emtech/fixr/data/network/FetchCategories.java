package com.emtech.fixr.data.network;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Toast;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.APIUrl;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.Categories;

public class FetchCategories {
    private static final String LOG_TAG = FetchCategories.class.getSimpleName();

    // LiveData storing the latest downloaded weather forecasts
    private final MutableLiveData<Category[]> mDownloadedCategories;
    private final AppExecutors mExecutors;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static FetchCategories sInstance;
    private final Context mContext;

    public FetchCategories(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedCategories = new MutableLiveData<Category[]>();
    }

    /**
     * Get the singleton for this class
     */
    public static FetchCategories getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FetchCategories(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public LiveData<Category[]> getCurrentCategories() {
        return mDownloadedCategories;
    }

    /**
     * Starts an intent service to fetch the categories.
     */
    public void startFetchCategoryService() {
        Intent intentToFetch = new Intent(mContext, FixAppSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "Fetch categories service created");
    }

    public void GetAppCategories() {
        Log.d(LOG_TAG, "Fetch categories started");
/*
        //defining a progress dialog to show while signing up
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing Up...");
        progressDialog.show();
*/
            //building retrofit object
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(APIUrl.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            //Defining retrofit com.emtech.retrofitexample.api service
            //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Categories> call = service.getCategories();

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Categories>() {
            @Override
            public void onResponse(Call<Categories> call, Response<Categories> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null && response.body().getCategories().length > 0) {
                    Log.d(LOG_TAG, "JSON not null and has " + response.body().getCategories().length
                            + " values");

                    /*
                    //array to hold the category details from the response
                    Category[] categoryList = new Category[response.body().getCategories().length];

                    //loop through the response values extracting the category details
                    for (int i =0; i < response.body().getCategories().length; i++){
                        Category newCategory = new Category(response.body().getName(), response.body().getDescription());

                        Log.d(LOG_TAG, "******** Category Name: "+ response.body().getName());

                        categoryList[i] = newCategory;
                    }
                    */

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mDownloadedCategories.postValue(response.body().getCategories());

                    // If the code reaches this point, we have successfully performed our sync
                    Log.d(LOG_TAG, "Successfully performed our sync");
                }
            }

            @Override
            public void onFailure(Call<Categories> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
            }
        });

    }

}
