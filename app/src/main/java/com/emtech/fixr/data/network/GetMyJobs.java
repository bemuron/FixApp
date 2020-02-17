package com.emtech.fixr.data.network;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.UserJobs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetMyJobs {
    private static final String LOG_TAG = GetMyJobs.class.getSimpleName();

    // LiveData storing the latest downloaded jobs list
    private final MutableLiveData<List<Job>> mDownloadedJobs, mDownloadedJobsByStatus,
            mJobsForBrowsing, mSearchedJobs;
    private final AppExecutors mExecutors;

    private final MutableLiveData<Job> mJobDetails;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static GetMyJobs sInstance;
    private final Context mContext;
    private Job job;
    private List<Job> jobList = new ArrayList<Job>();

    public GetMyJobs(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedJobs = new MutableLiveData<>();
        mJobDetails = new MutableLiveData<>();
        mDownloadedJobsByStatus = new MutableLiveData<>();
        mJobsForBrowsing = new MutableLiveData<>();
        mSearchedJobs = new MutableLiveData<>();
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

    //returned list of jobs
    public LiveData<List<Job>> getJobsForUser() {
        return mDownloadedJobs;
    }

    //returned list of jobs by status
    public LiveData<List<Job>> getJobsForUserByStatus() {
        return mDownloadedJobsByStatus;
    }

    //returned job details
    public LiveData<Job> getJobDetails() {
        return mJobDetails;
    }

    //returned jobs for browsing
    public LiveData<List<Job>> getJobsForBrowsing() {
        return mJobsForBrowsing;
    }

    //returned jobs after search
    public LiveData<List<Job>> getSearchedJobs() {
        return mSearchedJobs;
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

                    //clear the previous search list if it has content
                    if (jobList != null) {
                        jobList.clear();
                    }

                    for (int i = 0; i < response.body().getUserJobs().size(); i++) {
                        job = new Job();
                        job.setCategory_id(response.body().getUserJobs().get(i).getCategory_id());
                        job.setJob_id(response.body().getUserJobs().get(i).getJob_id());
                        job.setPosted_by(response.body().getUserJobs().get(i).getPosted_by());
                        job.setName(response.body().getUserJobs().get(i).getName());
                        job.setDescription(response.body().getUserJobs().get(i).getDescription());
                        job.setMust_have_one(response.body().getUserJobs().get(i).getMust_have_one());
                        job.setMust_have_two(response.body().getUserJobs().get(i).getMust_have_two());
                        job.setMust_have_three(response.body().getUserJobs().get(i).getMust_have_three());
                        job.setIs_job_remote(response.body().getUserJobs().get(i).getIs_job_remote());
                        job.setLocation(response.body().getUserJobs().get(i).getLocation());
                        job.setImage1(response.body().getUserJobs().get(i).getImage1());
                        job.setJob_date(response.body().getUserJobs().get(i).getJob_date());
                        job.setJob_time(response.body().getUserJobs().get(i).getJob_time());
                        job.setTotal_budget(response.body().getUserJobs().get(i).getTotal_budget());
                        job.setPrice_per_hr(response.body().getUserJobs().get(i).getPrice_per_hr());
                        job.setTotal_hrs(response.body().getUserJobs().get(i).getTotal_hrs());
                        job.setEst_tot_budget(response.body().getUserJobs().get(i).getEst_tot_budget());
                        job.setJob_status(response.body().getUserJobs().get(i).getJob_status());
                        job.setCompleted_by(response.body().getUserJobs().get(i).getCompleted_by());
                        job.setPosted_on(response.body().getUserJobs().get(i).getPosted_on());
                        job.setCompleted_on(response.body().getUserJobs().get(i).getCompleted_on());

                        jobList.add(job);
                    }

                    //add the profile pic of the user who posted the job
                    job.setProfile_pic(response.body().getProfilePic());

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mDownloadedJobs.postValue(jobList);

                    Log.d(LOG_TAG, "Size of list: "+response.body().getUserJobs().size());
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

    public void GetJobDetails(int job_id) {
        Log.d(LOG_TAG, "Fetch job details started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.getJobDetails(job_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                if (response.body() != null) {
                    //count what we have in the response
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, "JSON not null");

                        job = new Job();
                        job.setCategory_id(response.body().getJobDetails().getCategory_id());
                        job.setJob_id(response.body().getJobDetails().getJob_id());
                        job.setPosted_by(response.body().getJobDetails().getPosted_by());
                        job.setName(response.body().getJobDetails().getName());
                        job.setDescription(response.body().getJobDetails().getDescription());
                        job.setMust_have_one(response.body().getJobDetails().getMust_have_one());
                        job.setMust_have_two(response.body().getJobDetails().getMust_have_two());
                        job.setMust_have_three(response.body().getJobDetails().getMust_have_three());
                        job.setIs_job_remote(response.body().getJobDetails().getIs_job_remote());
                        job.setLocation(response.body().getJobDetails().getLocation());
                        job.setImage1(response.body().getJobDetails().getImage1());
                        job.setJob_date(response.body().getJobDetails().getJob_date());
                        job.setJob_time(response.body().getJobDetails().getJob_time());
                        job.setTotal_budget(response.body().getJobDetails().getTotal_budget());
                        job.setPrice_per_hr(response.body().getJobDetails().getPrice_per_hr());
                        job.setTotal_hrs(response.body().getJobDetails().getTotal_hrs());
                        job.setEst_tot_budget(response.body().getJobDetails().getEst_tot_budget());
                        job.setJob_status(response.body().getJobDetails().getJob_status());
                        job.setCompleted_by(response.body().getJobDetails().getCompleted_by());
                        job.setPosted_on(response.body().getJobDetails().getPosted_on());
                        job.setCompleted_on(response.body().getJobDetails().getCompleted_on());

                        //add the profile pic of the user who posted the job
                        job.setProfile_pic(response.body().getProfilePic());

                        //add the name of the user who posted the job
                        job.setUserName(response.body().getName());

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mJobDetails.postValue(job);

                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got all job details");
                    }
                }else {
                    Log.e(LOG_TAG, "response.body() is null");
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

    //getting the user jobs by status
    public void GetJobsBySatus(int user_id, int status) {
        Log.d(LOG_TAG, "Fetch user jobs by status started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.getJobsByStatus(user_id, status);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null) {
                    Log.d(LOG_TAG, "JSON not null");

                    //clear the previous search list if it has content
                    if (jobList != null) {
                        jobList.clear();
                    }

                    for (int i = 0; i < response.body().getJobsListByStatus().size(); i++) {
                        job = new Job();
                        job.setCategory_id(response.body().getJobsListByStatus().get(i).getCategory_id());
                        job.setJob_id(response.body().getJobsListByStatus().get(i).getJob_id());
                        job.setPosted_by(response.body().getJobsListByStatus().get(i).getPosted_by());
                        job.setName(response.body().getJobsListByStatus().get(i).getName());
                        job.setDescription(response.body().getJobsListByStatus().get(i).getDescription());
                        job.setMust_have_one(response.body().getJobsListByStatus().get(i).getMust_have_one());
                        job.setMust_have_two(response.body().getJobsListByStatus().get(i).getMust_have_two());
                        job.setMust_have_three(response.body().getJobsListByStatus().get(i).getMust_have_three());
                        job.setIs_job_remote(response.body().getJobsListByStatus().get(i).getIs_job_remote());
                        job.setLocation(response.body().getJobsListByStatus().get(i).getLocation());
                        job.setImage1(response.body().getJobsListByStatus().get(i).getImage1());
                        job.setJob_date(response.body().getJobsListByStatus().get(i).getJob_date());
                        job.setJob_time(response.body().getJobsListByStatus().get(i).getJob_time());
                        job.setTotal_budget(response.body().getJobsListByStatus().get(i).getTotal_budget());
                        job.setPrice_per_hr(response.body().getJobsListByStatus().get(i).getPrice_per_hr());
                        job.setTotal_hrs(response.body().getJobsListByStatus().get(i).getTotal_hrs());
                        job.setEst_tot_budget(response.body().getJobsListByStatus().get(i).getEst_tot_budget());
                        job.setJob_status(response.body().getJobsListByStatus().get(i).getJob_status());
                        job.setCompleted_by(response.body().getJobsListByStatus().get(i).getCompleted_by());
                        job.setPosted_on(response.body().getJobsListByStatus().get(i).getPosted_on());
                        job.setCompleted_on(response.body().getJobsListByStatus().get(i).getCompleted_on());

                        jobList.add(job);
                    }

                    //add the profile pic of the user who posted the job
                    job.setProfile_pic(response.body().getProfilePic());

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mDownloadedJobsByStatus.postValue(jobList);

                    Log.d(LOG_TAG, "Size of list: "+response.body().getJobsListByStatus().size());
                    // If the code reaches this point, we have successfully performed our sync
                    Log.d(LOG_TAG, "Successfully got all jobs associated " +
                            "to this user by status = "+status);
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

    //method to get all jobs for browsing
    public void BrowseJobs() {
        Log.d(LOG_TAG, "Browse jobs started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.browseAllJobs(1,1);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null) {
                    Log.d(LOG_TAG, "JSON not null");

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

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mJobsForBrowsing.postValue(jobList);

                    Log.d(LOG_TAG, "Size of list: "+response.body().getBrowsedJobsList().size());
                    // If the code reaches this point, we have successfully performed our sync
                    Log.d(LOG_TAG, "Successfully got all jobs for browsing");
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
