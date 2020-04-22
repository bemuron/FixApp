package com.emtech.fixr.data.network;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import androidx.annotation.NonNull;
import android.util.Log;

import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.UserJobs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowsedJobsDataSource extends PageKeyedDataSource<Integer, Job> {
    private static final String LOG_TAG = BrowsedJobsDataSource.class.getSimpleName();
    //private final Context mContext;
    //private final AppExecutors mExecutors;

    //the size of a page that we want
    public static final int PAGE_SIZE = 10;

    //we will start from the first page which is 1
    //this is an index value, so 1 is at position 0
    private static final int FIRST_PAGE = 0;

    private static final Object LOCK = new Object();
    private static BrowsedJobsDataSource sInstance;
    //private final Context mContext;
    //private final AppExecutors mExecutors;

    private List<Job> jobList = new ArrayList<Job>();
    private Job job;
    // LiveData storing the latest downloaded jobs list
    private final MutableLiveData<List<Job>> mJobsForBrowsing, mSearchedJobs;

    /*public BrowsedJobsDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mJobsForBrowsing = new MutableLiveData<>();
        mSearchedJobs = new MutableLiveData<>();
    }*/

    public BrowsedJobsDataSource() {
        //mContext = context;
        //mExecutors = executors;
        mJobsForBrowsing = new MutableLiveData<>();
        mSearchedJobs = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static BrowsedJobsDataSource getInstance() {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new BrowsedJobsDataSource();
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    //returned jobs for browsing
    public LiveData<List<Job>> getJobsForBrowsing() {
        return mJobsForBrowsing;
    }

    //returned jobs after search
    public LiveData<List<Job>> getSearchedJobs() {
        return mSearchedJobs;
    }

    /*
     * Step 1: This method is responsible to load the data initially
     * when app screen is launched for the first time.
     * We are fetching the first page data from the api
     * and passing it via the callback method to the UI.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Job> callback) {
        Log.d(LOG_TAG, "Loading initial Browse jobs started");

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.browseAllJobs(FIRST_PAGE, PAGE_SIZE);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                if (response.body() != null) {
                    Log.d(LOG_TAG, "Initial JSON not null");
                    callback.onResult(response.body().getBrowsedJobsList(), null, PAGE_SIZE);

                    //clear the previous search list if it has content
                    /*if (jobList != null) {
                        jobList.clear();
                    }*/

                    for (int i = 0; i < response.body().getBrowsedJobsList().size(); i++) {
                        job = new Job();
                        job.setCategory_id(response.body().getBrowsedJobsList().get(i).getCategory_id());
                        job.setJob_id(response.body().getBrowsedJobsList().get(i).getJob_id());
                        job.setPosted_by(response.body().getBrowsedJobsList().get(i).getPosted_by());
                        job.setName(response.body().getBrowsedJobsList().get(i).getName());
                        job.setDescription(response.body().getBrowsedJobsList().get(i).getDescription());
                        job.setMust_have_one(response.body().getBrowsedJobsList().get(i).getMust_have_one());
                        job.setMust_have_two(response.body().getBrowsedJobsList().get(i).getMust_have_two());
                        job.setMust_have_three(response.body().getBrowsedJobsList().get(i).getMust_have_three());
                        job.setIs_job_remote(response.body().getBrowsedJobsList().get(i).getIs_job_remote());
                        job.setLocation(response.body().getBrowsedJobsList().get(i).getLocation());
                        job.setImage1(response.body().getBrowsedJobsList().get(i).getImage1());
                        job.setJob_date(response.body().getBrowsedJobsList().get(i).getJob_date());
                        job.setJob_time(response.body().getBrowsedJobsList().get(i).getJob_time());
                        job.setTotal_budget(response.body().getBrowsedJobsList().get(i).getTotal_budget());
                        job.setPrice_per_hr(response.body().getBrowsedJobsList().get(i).getPrice_per_hr());
                        job.setTotal_hrs(response.body().getBrowsedJobsList().get(i).getTotal_hrs());
                        job.setEst_tot_budget(response.body().getBrowsedJobsList().get(i).getEst_tot_budget());
                        job.setJob_status(response.body().getBrowsedJobsList().get(i).getJob_status());
                        job.setCompleted_by(response.body().getBrowsedJobsList().get(i).getCompleted_by());
                        job.setPosted_on(response.body().getBrowsedJobsList().get(i).getPosted_on());
                        job.setCompleted_on(response.body().getBrowsedJobsList().get(i).getCompleted_on());

                        jobList.add(job);
                    }

                    //add the profile pic of the user who posted the job
                    job.setProfile_pic(response.body().getProfilePic());

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mJobsForBrowsing.postValue(jobList);

                    Log.d(LOG_TAG, "Initial Size of list: "+response.body().getBrowsedJobsList().size());
                    // If the code reaches this point, we have successfully performed our sync
                    Log.d(LOG_TAG, "Successfully got initial list of jobs for browsing");
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

    //this will load the previous page
    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Job> callback) {

        Log.d(LOG_TAG, "Loading previous Browse jobs list started");

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.browseAllJobs(params.key, PAGE_SIZE);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if the current page is greater than one
                //we are decrementing the page number
                //else there is no previous page
                Integer adjacentKey = (params.key > 1) ? params.key - 1 : null;

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null) {
                    Log.d(LOG_TAG, "Previous JSON not null");
                    //passing the loaded data
                    //and the previous page key
                    callback.onResult(response.body().getBrowsedJobsList(), adjacentKey);

                    //clear the previous search list if it has content
                    /*if (jobList != null) {
                        jobList.clear();
                    }*/

                    for (int i = 0; i < response.body().getBrowsedJobsList().size(); i++) {
                        job = new Job();
                        job.setCategory_id(response.body().getBrowsedJobsList().get(i).getCategory_id());
                        job.setJob_id(response.body().getBrowsedJobsList().get(i).getJob_id());
                        job.setPosted_by(response.body().getBrowsedJobsList().get(i).getPosted_by());
                        job.setName(response.body().getBrowsedJobsList().get(i).getName());
                        job.setDescription(response.body().getBrowsedJobsList().get(i).getDescription());
                        job.setMust_have_one(response.body().getBrowsedJobsList().get(i).getMust_have_one());
                        job.setMust_have_two(response.body().getBrowsedJobsList().get(i).getMust_have_two());
                        job.setMust_have_three(response.body().getBrowsedJobsList().get(i).getMust_have_three());
                        job.setIs_job_remote(response.body().getBrowsedJobsList().get(i).getIs_job_remote());
                        job.setLocation(response.body().getBrowsedJobsList().get(i).getLocation());
                        job.setImage1(response.body().getBrowsedJobsList().get(i).getImage1());
                        job.setJob_date(response.body().getBrowsedJobsList().get(i).getJob_date());
                        job.setJob_time(response.body().getBrowsedJobsList().get(i).getJob_time());
                        job.setTotal_budget(response.body().getBrowsedJobsList().get(i).getTotal_budget());
                        job.setPrice_per_hr(response.body().getBrowsedJobsList().get(i).getPrice_per_hr());
                        job.setTotal_hrs(response.body().getBrowsedJobsList().get(i).getTotal_hrs());
                        job.setEst_tot_budget(response.body().getBrowsedJobsList().get(i).getEst_tot_budget());
                        job.setJob_status(response.body().getBrowsedJobsList().get(i).getJob_status());
                        job.setCompleted_by(response.body().getBrowsedJobsList().get(i).getCompleted_by());
                        job.setPosted_on(response.body().getBrowsedJobsList().get(i).getPosted_on());
                        job.setCompleted_on(response.body().getBrowsedJobsList().get(i).getCompleted_on());

                        jobList.add(job);
                    }

                    //add the profile pic of the user who posted the job
                    job.setProfile_pic(response.body().getProfilePic());

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mJobsForBrowsing.postValue(jobList);

                    Log.d(LOG_TAG, "Previous Size of list: "+response.body().getBrowsedJobsList().size());
                    // If the code reaches this point, we have successfully performed our sync
                    Log.d(LOG_TAG, "Successfully got previous list of jobs for browsing");
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

    //this will load the next page
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Job> callback) {
        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.browseAllJobs(params.key, PAGE_SIZE);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if the current page is greater than one
                //we are incrementing the page number
                //else there is no previous page
                //Integer adjacentKey = (params.key > 1) ? params.key + 1 : null;

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null) {
                    Log.d(LOG_TAG, "Next JSON not null");

                    Integer adjacentKey;
                    if (response.body().getPages_count() == params.key) {
                        adjacentKey = null;
                        Log.e(LOG_TAG, " adjacentKey = null | Pages count from server is " + response.body().getPages_count() + " " +
                                "and current page count is " + params.key);

                        //passing the loaded data
                        //and the previous page key
                        callback.onResult(response.body().getJobSearchResults(), adjacentKey);

                    }else if (response.body().getPages_count() < params.key && response.body().getPages_count() != 0) {
                        adjacentKey = null;
                        Log.e(LOG_TAG, " adjacentKey = null | Pages count from server is " + response.body().getPages_count() + " " +
                                "and current page count is " + params.key);

                        //passing the loaded data
                        //and the previous page key
                        callback.onResult(response.body().getBrowsedJobsList(), adjacentKey);

                    }else if (response.body().getPages_count() > params.key){
                        adjacentKey = params.key + 1;

                        //Integer adjacentKey = ((response.body().getPages_count() > params.key || response.body().getPages_count() != params.key)) ? params.key + 1 : null;

                        Log.e(LOG_TAG, "Pages count from server is " + response.body().getPages_count() + " " +
                                "and current page count is " + params.key);

                        //passing the loaded data
                        //and the previous page key
                        callback.onResult(response.body().getBrowsedJobsList(), adjacentKey);

                        //clear the previous search list if it has content
                        if (jobList != null) {
                            jobList.clear();
                        }

                        for (int i = 0; i < response.body().getBrowsedJobsList().size(); i++) {
                            job = new Job();
                            job.setCategory_id(response.body().getBrowsedJobsList().get(i).getCategory_id());
                            job.setJob_id(response.body().getBrowsedJobsList().get(i).getJob_id());
                            job.setPosted_by(response.body().getBrowsedJobsList().get(i).getPosted_by());
                            job.setName(response.body().getBrowsedJobsList().get(i).getName());
                            job.setDescription(response.body().getBrowsedJobsList().get(i).getDescription());
                            job.setMust_have_one(response.body().getBrowsedJobsList().get(i).getMust_have_one());
                            job.setMust_have_two(response.body().getBrowsedJobsList().get(i).getMust_have_two());
                            job.setMust_have_three(response.body().getBrowsedJobsList().get(i).getMust_have_three());
                            job.setIs_job_remote(response.body().getBrowsedJobsList().get(i).getIs_job_remote());
                            job.setLocation(response.body().getBrowsedJobsList().get(i).getLocation());
                            job.setImage1(response.body().getBrowsedJobsList().get(i).getImage1());
                            job.setJob_date(response.body().getBrowsedJobsList().get(i).getJob_date());
                            job.setJob_time(response.body().getBrowsedJobsList().get(i).getJob_time());
                            job.setTotal_budget(response.body().getBrowsedJobsList().get(i).getTotal_budget());
                            job.setPrice_per_hr(response.body().getBrowsedJobsList().get(i).getPrice_per_hr());
                            job.setTotal_hrs(response.body().getBrowsedJobsList().get(i).getTotal_hrs());
                            job.setEst_tot_budget(response.body().getBrowsedJobsList().get(i).getEst_tot_budget());
                            job.setJob_status(response.body().getBrowsedJobsList().get(i).getJob_status());
                            job.setCompleted_by(response.body().getBrowsedJobsList().get(i).getCompleted_by());
                            job.setPosted_on(response.body().getBrowsedJobsList().get(i).getPosted_on());
                            job.setCompleted_on(response.body().getBrowsedJobsList().get(i).getCompleted_on());

                            jobList.add(job);
                        }

                        //add the profile pic of the user who posted the job
                        job.setProfile_pic(response.body().getProfilePic());

                        Log.d(LOG_TAG, "Next Size of list: "+response.body().getBrowsedJobsList().size());
                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got next list of jobs for browsing");
                    }

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mJobsForBrowsing.postValue(jobList);
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
