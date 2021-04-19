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
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.models.UserJobs;
import com.emtech.fixr.presentation.ui.activity.OfferAcceptedDetailsForPosterActivity;
import com.emtech.fixr.presentation.ui.activity.OfferReceivedDetailsForPosterActivity;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emtech.fixr.presentation.ui.activity.JobDetailsActivity.jobDetailsActivity;
import static com.emtech.fixr.presentation.ui.activity.JobInProgressActivity.jobInProgressActivity;
import static com.emtech.fixr.presentation.ui.activity.OfferAcceptedDetailsForFixerActivity.offerAcceptedDetailsForFixerActivity;
import static com.emtech.fixr.presentation.ui.activity.OfferAcceptedDetailsForPosterActivity.offerAcceptedDetailsForPosterActivity;
import static com.emtech.fixr.presentation.ui.activity.OfferMadeDetailsForFixerActivity.offerMadeDetailsForFixerActivity;
import static com.emtech.fixr.presentation.ui.activity.OfferReceivedDetailsForPosterActivity.offerReceivedDetailsForPosterActivity;
import static com.emtech.fixr.presentation.ui.activity.RatingActivity.ratingActivity;

public class GetMyJobs {
    private static final String LOG_TAG = GetMyJobs.class.getSimpleName();

    // LiveData storing the latest downloaded jobs list
    private final MutableLiveData<List<Job>> mDownloadedJobs, mDownloadedJobsByStatus,
            mJobsForBrowsing, mSearchedJobs;
    private final AppExecutors mExecutors;

    private final MutableLiveData<Job> mJobDetails;
    private final MutableLiveData<Offer> mOfferDetails;
    private final MutableLiveData<Offer> mJIPDetails;
    private final MutableLiveData<List<Offer>> mFixerOffersMadeList;
    private final MutableLiveData<List<Offer>> mFixerOffersAcceptedList;
    private final MutableLiveData<List<Offer>> mPosterOffersReceivedList;
    private final MutableLiveData<List<Offer>> mPosterOffersAcceptedList;
    private final MutableLiveData<Integer> mJobStatus;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static GetMyJobs sInstance;
    private final Context mContext;
    private Job job;
    private Offer offer;
    private List<Job> jobList = new ArrayList<Job>();
    private List<Offer> offersList = new ArrayList<Offer>();

    public GetMyJobs(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedJobs = new MutableLiveData<>();
        mJobDetails = new MutableLiveData<>();
        mDownloadedJobsByStatus = new MutableLiveData<>();
        mJobsForBrowsing = new MutableLiveData<>();
        mSearchedJobs = new MutableLiveData<>();
        mFixerOffersMadeList = new MutableLiveData<>();
        mFixerOffersAcceptedList = new MutableLiveData<>();
        mOfferDetails = new MutableLiveData<>();
        mJIPDetails = new MutableLiveData<>();
        mPosterOffersReceivedList = new MutableLiveData<>();
        mPosterOffersAcceptedList = new MutableLiveData<>();
        mJobStatus = new MutableLiveData<>();
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

    //returned list of offers made for jobs
    public LiveData<List<Offer>> getOffersMadeForFixer() {
        return mFixerOffersMadeList;
    }

    //returned offers accepted for fixer
    public LiveData<List<Offer>> getOffersAcceptedForFixer() {
        return mFixerOffersAcceptedList;
    }

    //returned offer details
    public LiveData<Offer> getOfferDetails() {
        return mOfferDetails;
    }

    //returned JIP details
    public LiveData<Offer> getJIPDetails() {
        return mJIPDetails;
    }

    //returned offers received for poster
    public LiveData<List<Offer>> getOffersReceivedForPoster() {
        return mPosterOffersReceivedList;
    }

    //returned offers accepted by poster
    public LiveData<List<Offer>> getOffersAcceptedForPoster() {
        return mPosterOffersAcceptedList;
    }

    //return the job status
    public LiveData<Integer> getJobStatus() {
        return mJobStatus;
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
                        //name is the name of the job
                        job.setName(response.body().getUserJobs().get(i).getName());
                        //user_name is name of the job poster
                        job.setUserName(response.body().getUserJobs().get(i).getUserName());
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

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
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

                        Log.d(LOG_TAG, "Size of list: " + response.body().getJobsListByStatus().size());
                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got all jobs associated " +
                                "to this user by status = " + status);
                    }
                }catch (Exception e){
                    Log.e(LOG_TAG,"Could not get jobs for status " +status);
                    e.printStackTrace();
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

    //getting the jobs the fixer has made an offer on
    public void GetOffersMadeForFixer(int user_id) {
        Log.d(LOG_TAG, "Fetch offers made for jobs for fixer started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        Call<UserJobs> call = service.getOffersMade(user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                try {
                    if (response.body().getOffersMadeList() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous search list if it has content
                        //if (offersList != null) {
                            offersList.clear();
                        //}

                        for (int i = 0; i < response.body().getOffersMadeList().size(); i++) {
                            offer = new Offer();
                            offer.setOffered_by(response.body().getOffersMadeList().get(i).getOffered_by());
                            offer.setJob_id(response.body().getOffersMadeList().get(i).getJob_id());
                            offer.setOffer_id(response.body().getOffersMadeList().get(i).getOffer_id());
                            offer.setOffer_amount(response.body().getOffersMadeList().get(i).getOffer_amount());
                            offer.setMessage(response.body().getOffersMadeList().get(i).getMessage());
                            offer.setLast_edited_on(response.body().getOffersMadeList().get(i).getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getOffersMadeList().get(i).getSeen_by_poster());
                            offer.setEdit_count(response.body().getOffersMadeList().get(i).getEdit_count());
                            offer.setOffer_accepted(response.body().getOffersMadeList().get(i).getOffer_accepted());
                            offer.setName(response.body().getOffersMadeList().get(i).getName());
                            offer.setEst_tot_budget(response.body().getOffersMadeList().get(i).getEst_tot_budget());
                            offer.setPosted_by(response.body().getOffersMadeList().get(i).getPosted_by());
                            offer.setPosted_on(response.body().getOffersMadeList().get(i).getPosted_on());
                            offer.setJob_date(response.body().getOffersMadeList().get(i).getJob_date());
                            //add the profile pic of the user who posted the job
                            offer.setProfile_pic(response.body().getBrowsedJobsList().get(i).getProfile_pic());

                            offersList.add(offer);
                        }

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mFixerOffersMadeList.postValue(offersList);

                        Log.d(LOG_TAG, "Size of list: " + response.body().getOffersMadeList().size());
                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got all jobs associated " +
                                "to this user by status = ");
                    }else{
                        mFixerOffersMadeList.postValue(null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error from inside response area in retrofit call --"+e.getMessage());
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

    //getting the jobs this fixer made an offer on and have been accepted
    public void GetOffersAcceptedForFixer(int user_id) {
        Log.d(LOG_TAG, "Fetch offers accepted for jobs for fixer started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        Call<UserJobs> call = service.getOffersAcceptedForFixer(user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                try {
                    if (response.body().getFixerOffersAcceptedList() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous search list if it has content
                        //if (offersList != null) {
                            offersList.clear();
                        //}

                        for (int i = 0; i < response.body().getFixerOffersAcceptedList().size(); i++) {
                            offer = new Offer();
                            offer.setOffered_by(response.body().getFixerOffersAcceptedList().get(i).getOffered_by());
                            offer.setJob_id(response.body().getFixerOffersAcceptedList().get(i).getJob_id());
                            offer.setOffer_id(response.body().getFixerOffersAcceptedList().get(i).getOffer_id());
                            offer.setOffer_amount(response.body().getFixerOffersAcceptedList().get(i).getOffer_amount());
                            offer.setMessage(response.body().getFixerOffersAcceptedList().get(i).getMessage());
                            offer.setLast_edited_on(response.body().getFixerOffersAcceptedList().get(i).getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getFixerOffersAcceptedList().get(i).getSeen_by_poster());
                            offer.setEdit_count(response.body().getFixerOffersAcceptedList().get(i).getEdit_count());
                            offer.setOffer_accepted(response.body().getFixerOffersAcceptedList().get(i).getOffer_accepted());
                            //name is title of the job
                            offer.setName(response.body().getFixerOffersAcceptedList().get(i).getName());
                            //user_name is name of the job poster
                            offer.setUser_name(response.body().getFixerOffersAcceptedList().get(i).getUser_name());
                            offer.setEst_tot_budget(response.body().getFixerOffersAcceptedList().get(i).getEst_tot_budget());
                            offer.setPosted_by(response.body().getFixerOffersAcceptedList().get(i).getPosted_by());
                            offer.setPosted_on(response.body().getFixerOffersAcceptedList().get(i).getPosted_on());
                            offer.setJob_date(response.body().getFixerOffersAcceptedList().get(i).getJob_date());
                            //add the profile pic of the user who posted the job
                            offer.setProfile_pic(response.body().getFixerOffersAcceptedList().get(i).getProfile_pic());

                            offersList.add(offer);
                        }

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mFixerOffersAcceptedList.postValue(offersList);

                        Log.d(LOG_TAG, "Size of list: " + response.body().getFixerOffersAcceptedList().size());
                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got all jobs associated " +
                                "to this user by status = ");
                    }else{
                        mFixerOffersAcceptedList.postValue(null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error from inside response area in retrofit call --"+e.getMessage());
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

    public void GetOfferDetailsForFixer(int offer_id) {
        Log.d(LOG_TAG, "Fetch offer details for fixer started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.getOfferDetailsForFixer(offer_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                try {
                    //if response body is not null, we have some data
                    if (response.body() != null) {
                        //count what we have in the response
                        if (!response.body().getError()) {
                            Log.d(LOG_TAG, "JSON not null");

                            offer = new Offer();
                            offer.setOffer_id(response.body().getOfferDetails().getOffer_id());
                            offer.setOffered_by(response.body().getOfferDetails().getOffered_by());
                            offer.setJob_id(response.body().getOfferDetails().getJob_id());
                            offer.setOffer_amount(response.body().getOfferDetails().getOffer_amount());
                            offer.setMessage(response.body().getOfferDetails().getMessage());
                            offer.setLast_edited_on(response.body().getOfferDetails().getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getOfferDetails().getSeen_by_poster());
                            offer.setEdit_count(response.body().getOfferDetails().getEdit_count());
                            offer.setOffer_accepted(response.body().getOfferDetails().getOffer_accepted());
                            offer.setName(response.body().getOfferDetails().getName());
                            offer.setEst_tot_budget(response.body().getOfferDetails().getEst_tot_budget());
                            offer.setPosted_by(response.body().getOfferDetails().getPosted_by());
                            offer.setPosted_on(response.body().getOfferDetails().getPosted_on());
                            offer.setJob_date(response.body().getOfferDetails().getJob_date());
                            //add the profile pic of the user who posted the job
                            offer.setProfile_pic(response.body().getProfilePic());
                            //set the name of the fixer
                            offer.setUser_name(response.body().getUser_name());

                            // When you are off of the main thread and want to update LiveData, use postValue.
                            // It posts the update to the main thread.
                            mOfferDetails.postValue(offer);

                            // If the code reaches this point, we have successfully performed our sync
                            Log.d(LOG_TAG, "Successfully got all offer details");
                        }
                    } else {
                        Log.e(LOG_TAG, "response.body() is null");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "catch block error msg: "+e.getMessage());
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


    public void GetJIPDetails(int offer_id) {
        Log.d(LOG_TAG, "Fetch offer JIP details started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.getJIPDetails(offer_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                try {
                    //if response body is not null, we have some data
                    if (response.body() != null) {
                        //count what we have in the response
                        if (!response.body().getError()) {
                            Log.d(LOG_TAG, "JSON not null");

                            offer = new Offer();
                            offer.setOffer_id(response.body().getOfferDetails().getOffer_id());
                            offer.setOffered_by(response.body().getOfferDetails().getOffered_by());
                            offer.setJob_id(response.body().getOfferDetails().getJob_id());
                            offer.setOffer_amount(response.body().getOfferDetails().getOffer_amount());
                            offer.setMessage(response.body().getOfferDetails().getMessage());
                            offer.setLast_edited_on(response.body().getOfferDetails().getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getOfferDetails().getSeen_by_poster());
                            offer.setEdit_count(response.body().getOfferDetails().getEdit_count());
                            offer.setOffer_accepted(response.body().getOfferDetails().getOffer_accepted());
                            offer.setName(response.body().getOfferDetails().getName());
                            offer.setEst_tot_budget(response.body().getOfferDetails().getEst_tot_budget());
                            offer.setPosted_by(response.body().getOfferDetails().getPosted_by());
                            offer.setPosted_on(response.body().getOfferDetails().getPosted_on());
                            offer.setJob_date(response.body().getOfferDetails().getJob_date());
                            //add the profile pic of the fixer
                            offer.setFixer_profile_pic(response.body().getFixer_profile_pic());
                            //profile pic of the poster
                            offer.setPoster_profile_pic(response.body().getPoster_profile_pic());
                            //set the name of the poster
                            offer.setPoster_user_name(response.body().getPoster_user_name());
                            //set the name of the fixer
                            offer.setFixer_user_name(response.body().getFixer_user_name());

                            // When you are off of the main thread and want to update LiveData, use postValue.
                            // It posts the update to the main thread.
                            mJIPDetails.postValue(offer);

                            // If the code reaches this point, we have successfully performed our sync
                            Log.d(LOG_TAG, "Successfully got all JIP details");
                        }
                    } else {
                        Log.e(LOG_TAG, "response.body() is null");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "catch block error msg: "+e.getMessage());
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

    //getting the offers on jobs the poster has received
    public void GetOffersReceivedForPoster(int user_id) {
        Log.d(LOG_TAG, "Fetch offers received for jobs by poster started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        Call<UserJobs> call = service.getOffersForJobs(user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                try {
                    if (response.body().getOffersReceived() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous search list if it has content
                        //if (offersList != null) {
                            offersList.clear();
                        //}

                        for (int i = 0; i < response.body().getOffersReceived().size(); i++) {
                            offer = new Offer();
                            offer.setOffer_id(response.body().getOffersReceived().get(i).getOffer_id());
                            offer.setOffered_by(response.body().getOffersReceived().get(i).getOffered_by());
                            offer.setJob_id(response.body().getOffersReceived().get(i).getJob_id());
                            offer.setOffer_amount(response.body().getOffersReceived().get(i).getOffer_amount());
                            offer.setMessage(response.body().getOffersReceived().get(i).getMessage());
                            offer.setLast_edited_on(response.body().getOffersReceived().get(i).getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getOffersReceived().get(i).getSeen_by_poster());
                            offer.setEdit_count(response.body().getOffersReceived().get(i).getEdit_count());
                            offer.setOffer_accepted(response.body().getOffersReceived().get(i).getOffer_accepted());
                            //name of job
                            offer.setName(response.body().getOffersReceived().get(i).getName());
                            //name of fixer who made an offer
                            offer.setUser_name(response.body().getOffersReceived().get(i).getUser_name());
                            offer.setEst_tot_budget(response.body().getOffersReceived().get(i).getEst_tot_budget());
                            offer.setPosted_by(response.body().getOffersReceived().get(i).getPosted_by());
                            offer.setPosted_on(response.body().getOffersReceived().get(i).getPosted_on());
                            offer.setJob_date(response.body().getOffersReceived().get(i).getJob_date());
                            //add the profile pic of the user who posted the job
                            offer.setProfile_pic(response.body().getOffersReceived().get(i).getProfile_pic());

                            offersList.add(offer);
                        }

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mPosterOffersReceivedList.postValue(offersList);

                        Log.d(LOG_TAG, "Size of list: " + response.body().getOffersReceived().size());
                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got all offers received for jobs by this poster ");
                    }else{
                        mPosterOffersReceivedList.postValue(null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error from inside response area in retrofit call --"+e.getMessage());
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

    //getting the offers on jobs the poster has received or accepted
    public void GetOffersAcceptedForPoster(int user_id) {
        Log.d(LOG_TAG, "Fetch offers accepted for jobs by poster started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        Call<UserJobs> call = service.getOffersAcceptedForPoster(user_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                try {
                    if (response.body().getOffersAcceptedList() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous search list if it has content
                        //if (offersList != null) {
                            offersList.clear();
                        //}

                        for (int i = 0; i < response.body().getOffersAcceptedList().size(); i++) {
                            offer = new Offer();
                            offer.setOffer_id(response.body().getOffersAcceptedList().get(i).getOffer_id());
                            offer.setOffered_by(response.body().getOffersAcceptedList().get(i).getOffered_by());
                            offer.setJob_id(response.body().getOffersAcceptedList().get(i).getJob_id());
                            offer.setOffer_amount(response.body().getOffersAcceptedList().get(i).getOffer_amount());
                            offer.setMessage(response.body().getOffersAcceptedList().get(i).getMessage());
                            offer.setLast_edited_on(response.body().getOffersAcceptedList().get(i).getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getOffersAcceptedList().get(i).getSeen_by_poster());
                            offer.setEdit_count(response.body().getOffersAcceptedList().get(i).getEdit_count());
                            offer.setOffer_accepted(response.body().getOffersAcceptedList().get(i).getOffer_accepted());
                            //job name
                            offer.setName(response.body().getOffersAcceptedList().get(i).getName());
                            //name of fixer
                            offer.setUser_name(response.body().getOffersAcceptedList().get(i).getUser_name());
                            offer.setEst_tot_budget(response.body().getOffersAcceptedList().get(i).getEst_tot_budget());
                            offer.setPosted_by(response.body().getOffersAcceptedList().get(i).getPosted_by());
                            offer.setPosted_on(response.body().getOffersAcceptedList().get(i).getPosted_on());
                            offer.setJob_date(response.body().getOffersAcceptedList().get(i).getJob_date());
                            //add the profile pic of the user who posted the job
                            offer.setProfile_pic(response.body().getOffersAcceptedList().get(i).getProfile_pic());

                            offersList.add(offer);
                        }

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mPosterOffersAcceptedList.postValue(offersList);

                        Log.d(LOG_TAG, "Size of list: " + response.body().getOffersAcceptedList().size());
                        // If the code reaches this point, we have successfully performed our sync
                        Log.d(LOG_TAG, "Successfully got all offers accepted for jobs by this poster ");
                    }else{
                        mPosterOffersAcceptedList.postValue(null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error from inside response area in retrofit call --"+e.getMessage());
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

    public void GetOfferDetailsForPoster(int offer_id) {
        Log.d(LOG_TAG, "Fetch offer details for poster started");

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserJobs> call = service.getOfferDetailsForPoster(offer_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserJobs>() {
            @Override
            public void onResponse(Call<UserJobs> call, Response<UserJobs> response) {

                try {
                    //if response body is not null, we have some data
                    if (response.body() != null) {
                        //count what we have in the response
                        if (!response.body().getError()) {
                            Log.d(LOG_TAG, "JSON not null");

                            offer = new Offer();
                            offer.setOffer_id(response.body().getOfferDetails().getOffer_id());
                            offer.setOffered_by(response.body().getOfferDetails().getOffered_by());
                            offer.setJob_id(response.body().getOfferDetails().getJob_id());
                            offer.setOffer_amount(response.body().getOfferDetails().getOffer_amount());
                            offer.setMessage(response.body().getOfferDetails().getMessage());
                            offer.setLast_edited_on(response.body().getOfferDetails().getLast_edited_on());
                            offer.setSeen_by_poster(response.body().getOfferDetails().getSeen_by_poster());
                            offer.setEdit_count(response.body().getOfferDetails().getEdit_count());
                            offer.setOffer_accepted(response.body().getOfferDetails().getOffer_accepted());
                            offer.setName(response.body().getOfferDetails().getName());
                            offer.setEst_tot_budget(response.body().getOfferDetails().getEst_tot_budget());
                            offer.setPosted_by(response.body().getOfferDetails().getPosted_by());
                            offer.setPosted_on(response.body().getOfferDetails().getPosted_on());
                            offer.setJob_date(response.body().getOfferDetails().getJob_date());
                            //add the profile pic of the user who posted the job
                            offer.setProfile_pic(response.body().getProfilePic());
                            //set the name of the fixer
                            offer.setUser_name(response.body().getUser_name());

                            // When you are off of the main thread and want to update LiveData, use postValue.
                            // It posts the update to the main thread.
                            mOfferDetails.postValue(offer);

                            // If the code reaches this point, we have successfully performed our sync
                            Log.d(LOG_TAG, "Successfully got all offer details");
                        }
                    } else {
                        Log.e(LOG_TAG, "response.body() is null");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "catch block error msg: "+e.getMessage());
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

    //retrofit call to update offer seen status to 1 (seen by poster)
    public void UpdateOfferSeenByPosterStatus(int offerId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateOfferSeenStatus(offerId, 1);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
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

    //retrofit call to update offer status to accepted by poster - 1
    public void PosterAcceptOffer(int offerId, int jobId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.posterAcceptOffer(offerId, jobId, 1);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the activity
                        //success
                        offerReceivedDetailsForPosterActivity.updateUiAfterPosterAcceptOffer(true,
                                response.body().getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                offerReceivedDetailsForPosterActivity.updateUiAfterPosterAcceptOffer(false,
                        t.getMessage());
            }
        });

    }

    //retrofit call to update offer status to rejected by poster - 2
    public void PosterRejectOffer(int offerId, int jobId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.posterRejectOffer(offerId, jobId,2);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the activity
                        //success
                        if (offerAcceptedDetailsForPosterActivity != null) {
                            offerAcceptedDetailsForPosterActivity.updateUiAfterPosterRejectOffer(true,
                                    response.body().getMessage());
                        }else{
                            offerReceivedDetailsForPosterActivity.updateUiAfterPosterRejectOffer(true,
                                    response.body().getMessage());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                if (offerAcceptedDetailsForPosterActivity != null) {
                    offerAcceptedDetailsForPosterActivity.updateUiAfterPosterRejectOffer(false,
                            t.getMessage());
                }else{
                    offerReceivedDetailsForPosterActivity.updateUiAfterPosterRejectOffer(false,
                            t.getMessage());
                }
            }
        });

    }

    //retrofit call to update offer status to rejected by fixer - 3
    public void FixerRejectOffer(int offerId, int jobId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.fixerRejectOffer(offerId, jobId, 3);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the activity
                        //success
                        if (offerMadeDetailsForFixerActivity != null) {
                            offerMadeDetailsForFixerActivity.updateUiAfterFixerRejectOffer(true,
                                    response.body().getMessage());
                        }else {
                            offerAcceptedDetailsForFixerActivity.updateUiAfterFixerRejectOffer(true,
                                    response.body().getMessage());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                if (offerMadeDetailsForFixerActivity != null) {
                    offerMadeDetailsForFixerActivity.updateUiAfterFixerRejectOffer(false,
                            t.getMessage());
                }else {
                    offerAcceptedDetailsForFixerActivity.updateUiAfterFixerRejectOffer(false,
                            t.getMessage());
                }
            }
        });
    }

    //retrofit call to update job status to 4 - Job In Progress
    //job is started by the fixer
    public void FixerStartJob(int offerId, int jobId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.fixerStartJob(offerId, jobId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the activity
                        //success
                        offerAcceptedDetailsForFixerActivity.jobStartedResponse(true,
                                response.body().getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                offerAcceptedDetailsForFixerActivity.jobStartedResponse(false,
                        t.getMessage());
            }
        });

    }

    //retrofit call to update job status to 5 - complete / finished
    //job is done/completed by the fixer
    public void FixerFinishJob(int offerId, int jobId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.fixerFinishJob(offerId, jobId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the activity
                        //success
                        jobInProgressActivity.jobFinishedResponse(true,
                                response.body().getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                jobInProgressActivity.jobFinishedResponse(false,
                        t.getMessage());
            }
        });

    }

    //retrofit call to get job status
    public void getJobStatusForPoster(int jobId, OfferAcceptedDetailsForPosterActivity activityInstance){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.getStatusForPoster(jobId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());

                        int job_status = response.body().getJob().getJob_status();

                        mJobStatus.postValue(job_status);

                    }else{
                        Log.e(LOG_TAG, response.body().getMessage());
                        Log.e(LOG_TAG, "Error: could not get job status for poster");
                    }
                }catch (Exception e){
                    Log.d(LOG_TAG, "Error: could not get job status for poster");
                    e.printStackTrace();
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

    //check if the fixer already made an offer to this job
    public void CheckIfOfferIsAlreadyMade(int userId, int jobId){
        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.checkOfferAlreadyMade(userId, jobId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.e(LOG_TAG, "Offers made by this fixer = "+response.body().getMessage());
                        //send response data to the activity
                        //success
                        jobDetailsActivity.offerAlreadyMadeCheck(response.body().getIs_offer_already_made());
                        /*if (response.body().getMessage().equals("1")) {
                            jobDetailsActivity.offerAlreadyMadeCheck(true);
                        }else if (response.body().getMessage().equals("0")){
                            jobDetailsActivity.offerAlreadyMadeCheck(false);
                        }*/
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, "Error while checking if offer was already made: "+t.getMessage());
            }
        });
    }

//retrofit call to send fixer rating to db
    public void submitFixerRating(int meeting_id, int poster_id, int fixer_id, float ratingValue, String comment){

        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.submitFixerRating(meeting_id, poster_id, fixer_id, ratingValue, comment);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                //if response body is not null, we have some data
                //successful addition
                if (!response.body().getError()) {
                    Log.e(LOG_TAG, "Rating submitted successfully");

                    //send response data to the activity
                    //success
                    if (ratingActivity != null) {
                        ratingActivity.updateUiAfterFixerRating(true,
                                response.body().getMessage());
                    }else {
                        ratingActivity.updateUiAfterFixerRating(true,
                                response.body().getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                ratingActivity.updateUiAfterFixerRating(false,
                        t.getMessage());
            }
        });
    }

//retrofit call to send poster rating to db
    //this also set the job status to 5 - completed
    public void submitPosterRating(int job_id, int fixer_id, int poster_id, float ratingValue, String comment){
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.submitPosterRating(job_id, fixer_id, poster_id, ratingValue, comment);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                //if response body is not null, we have some data
                //successful addition
                if (!response.body().getError()) {
                    Log.e(LOG_TAG, "Poster rating submitted successfully");
                    //send response data to the activity
                    //success
                    ratingActivity.updateUiAfterPosterRating(true,
                            response.body().getMessage());
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                ratingActivity.updateUiAfterPosterRating(false,
                        t.getMessage());
            }
        });

    }

}
