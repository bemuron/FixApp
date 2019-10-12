package com.emtech.fixr.data.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.Category;

public class FixAppNetworkDataSource {
    private static final String LOG_TAG = FixAppNetworkDataSource.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static FixAppNetworkDataSource sInstance;
    private final Context mContext;

    private final AppExecutors mExecutors;

    // LiveData storing the latest downloaded categories
    private final MutableLiveData<Category[]> mDownloadedCategories;

    private FixAppNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedCategories = new MutableLiveData<Category[]>();
    }

    /**
     * Get the singleton for this class
     */
    public static FixAppNetworkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FixAppNetworkDataSource(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    /*
     *  a getter method for mDownloadedCategories
     * */
    public LiveData<Category[]> getCurrentCategories() {
        return mDownloadedCategories;
    }

    /**
     * Starts an intent service to fetch the weather.
     */
    /*
    public void startFetchCategoryService() {
        Intent intentToFetch = new Intent(mContext, FixAppSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(LOG_TAG, "Service created");
    }
*/
    /**
     * Gets the newest weather
     */
    void fetchCategories() {

    }
}
