package com.emtech.fixr.data.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emtech.fixr.presentation.ui.activity.JobDetailsActivity.jobDetailsActivity;
import static com.emtech.fixr.presentation.ui.activity.OfferMadeDetailsForFixerActivity.offerMadeDetailsForFixerActivity;
import static com.emtech.fixr.presentation.ui.activity.PostJobActivity.postJobActivity;

public class PostFixAppJob {
    private static final String LOG_TAG = PostFixAppJob.class.getSimpleName();

    private final AppExecutors mExecutors;
    JobCreatedCallBack jobCreatedCallBack;
    JobDetailsUpdatedCallBack jobDetailsUpdatedCallBack;
    JobDetailsNoImageUpdatedCallBack jobDetailsNoImageUpdatedCallBack;
    static JobUpdatedCallBack jobUpdatedCallBack;
    static OfferEditedCallBack offerEditedCallBack;
    static OfferSavedCallBack offerSavedCallBack;
    public PostJobActivity postJobActivity;

    private final MutableLiveData<Integer> newJobId;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static PostFixAppJob sInstance;
    private final Context mContext;

    public PostFixAppJob(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        newJobId = new MutableLiveData<>();
    }

    /**
     * Get the singleton for this class
     */
    public static PostFixAppJob getInstance(Context context, AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PostFixAppJob(context.getApplicationContext(), executors);
            }
        }
        return sInstance;
    }

    //returned the new job created id
    public LiveData<Integer> getJobId() {
        return newJobId;
    }

    /**
     * Starts an intent service to post the job.
     */
    public void startPostJobService(int userId, String jobTitle, String jobDesc, String jobLocation,
                                    String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                    ArrayList<File> file, int categoryId, PostJobActivity postJobActivityInstance) {

        postJobActivity = postJobActivityInstance;
        jobCreatedCallBack = postJobActivity;
        Intent intentToPost = new Intent(mContext, PostJobIntentService.class);

        Bundle jobBundle = new Bundle();
        jobBundle.putInt("userId", userId);
        jobBundle.putString("jobTitle", jobTitle);
        jobBundle.putString("jobDesc", jobDesc);
        jobBundle.putString("jobLocation", jobLocation);
        jobBundle.putString("mustHaveOne", mustHaveOne);
        jobBundle.putString("mustHaveTwo", mustHaveTwo);
        jobBundle.putString("mustHaveThree", mustHaveThree);
        jobBundle.putInt("isJobRemote", isJobRemote);
        jobBundle.putSerializable("filePath", file);
        jobBundle.putInt("categoryId", categoryId);
        intentToPost.putExtras(jobBundle);

        mContext.startService(intentToPost);
        Log.d(LOG_TAG, "Post job service created");
    }

    //retrofit call to create a new job at the server
    public void createNewJob(int userId, String jobTitle, String jobDesc,
                             int categoryId, PostJobActivity postJobActivityInstance){
        jobCreatedCallBack = postJobActivityInstance;

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.createJob(userId, jobTitle, jobDesc, categoryId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());

                        int job_id = response.body().getJob().getJob_id();

                        newJobId.postValue(job_id);
                        //send data to parent activity
                        jobCreatedCallBack.onJobCreated(true, response.body().getMessage(), job_id);

                    }else{
                        Log.e(LOG_TAG, response.body().getMessage());
                        Log.e(LOG_TAG, "Error: job not posted at createNewJob()");
                        //send data to parent activity
                        jobCreatedCallBack.onJobCreated(false, "Sorry job could not be created at this time", 0);
                    }
                }catch (Exception e){
                    Log.d(LOG_TAG, "Error: job not posted at createNewJob()");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                jobCreatedCallBack.onJobCreated(false, t.getMessage(), 0);
            }
        });

    }

    //retrofit call to post the job details to the server
    public void postJobDetails(int userId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                               String mustHaveTwo, String mustHaveThree, int isJobRemote,
                               ArrayList<File> fileArrayList, int categoryId){

        //Map is used to multipart the file using okhttp3.RequestBody
        //File file = new File(mediaPath);
        Log.e(LOG_TAG, "File array list size = "+fileArrayList.size());

        MultipartBody.Part[] fileToUpload = new MultipartBody.Part[fileArrayList.size()];

        for (int pos = 0; pos < fileArrayList.size(); pos++){
            //parsing any media file
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), fileArrayList.get(pos));

            fileToUpload[pos] = MultipartBody.Part.createFormData("file[]",
                    fileArrayList.get(pos).getName(), requestBody);

            //MultipartBody.Part imageFile = MultipartBody.Part.createFormData("file[]",
              //      fileArrayList.get(pos).getName(), requestFile);

            //parts.add(fileToUpload);
        }

        //parsing any media file
        //RequestBody requestBody = RequestBody.create(MediaType.parse("*image/*"), file);
        //MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        //RequestBody fileName = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.postJob(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, fileToUpload, fileToUpload.length, categoryId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());

                        int job_id = response.body().getJob().getJob_id();

                        //send data to parent activity
                        jobCreatedCallBack.onJobCreated(true, response.body().getMessage(), job_id);

                    }else{
                        Log.e(LOG_TAG, response.body().getMessage());
                        Log.e(LOG_TAG, "Error: job not posted at PostJobDetails()");
                        //send data to parent activity
                        jobCreatedCallBack.onJobCreated(false, response.body().getMessage(), 0);
                    }
                }catch (Exception e){
                    Log.d(LOG_TAG, "Error: job not posted at PostJobDetails()");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                jobCreatedCallBack.onJobCreated(false, t.getMessage(), 0);
            }
        });

    }

    //retrofit call to post the job details to the server when an image is not added by the user
    public void postJobDetailsWithoutImage(int userId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                               String mustHaveTwo, String mustHaveThree, int isJobRemote, int categoryId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.postJobWithoutImage(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, categoryId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());

                        int job_id = response.body().getJob().getJob_id();

                        //send data to parent activity
                        jobCreatedCallBack.onJobCreated(true, response.body().getMessage(), job_id);

                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                jobCreatedCallBack.onJobCreated(false, t.getMessage(), 0);
            }
        });

    }

    //retrofit call to save the offer made for the job to the server
    public void saveOffer(int amountOffered, String offerMessage, int userId, int jobId){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.saveOffer(amountOffered, offerMessage, userId, jobId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the repository
                        //success
                        jobDetailsActivity.afterSaveOfferAttempt(true, response.body().getMessage());
                        //offerSavedCallBack.onOfferCreated(true, response.body().getMessage());
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
                //send response data to the repository
                //offerSavedCallBack.onOfferCreated(false, "Offer Not Saved");
                jobDetailsActivity.afterSaveOfferAttempt(false,
                        "Offer Not Saved: "+t.getMessage());
            }
        });

    }

    //retrofit call to edit the offer made for the job to the server
    public void editOffer(int offerId, int amountOffered, String offerMessage, int editCount){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateOffer(offerId, amountOffered, offerMessage, editCount);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the repository
                        //success
                        offerMadeDetailsForFixerActivity.onOfferEdited(true, response.body().getMessage());
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
                //send response data to the repository
                offerMadeDetailsForFixerActivity.onOfferEdited(false, "Offer not Edited");
            }
        });
    }

    //retrofit call to post the job details to the server
    //when image is attached
    public void updateJobDetails(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                 String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                 ArrayList<File> fileArrayList, PostJobActivity postJobActivityInstance){
        jobDetailsUpdatedCallBack = postJobActivityInstance;

        Log.e(LOG_TAG, "File array list size = "+fileArrayList.size());

        MultipartBody.Part[] fileToUpload = new MultipartBody.Part[fileArrayList.size()];

        for (int pos = 0; pos < fileArrayList.size(); pos++){
            //parsing any media file
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), fileArrayList.get(pos));

            fileToUpload[pos] = MultipartBody.Part.createFormData("file[]",
                    fileArrayList.get(pos).getName(), requestBody);
        }

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateJob(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, fileToUpload, fileToUpload.length);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());
                    //send response data to postjobactivity
                    //success
                    jobDetailsUpdatedCallBack.onJobDetailsUpdated(true,
                            response.body().getMessage(),"basicsWithImage");
                    //postJobActivity.updateUiAfterJobDetailsWithoutImage(true,
                      //      response.body().getMessage(),"basicsWithImage");
                }else{
                    jobDetailsUpdatedCallBack.onJobDetailsUpdated(false,
                            "Oops error occurred: Could not update job details", "basicsWithImage");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                //send response data to the postjobactivity
                jobDetailsUpdatedCallBack.onJobDetailsUpdated(false,
                        "Oops error occurred: Could not update job details","basicsWithImage");
            }
        });
    }

    //retrofit call to update the job details to the server when an image is not added by the user
    public void updateJobDetailsWithoutImage(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                             String mustHaveTwo, String mustHaveThree,
                                             int isJobRemote, PostJobActivity postJobActivityInstance){
        jobDetailsNoImageUpdatedCallBack = postJobActivityInstance;

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateJobWithoutImage(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());
                    //send response data to postjobactivity
                    //success
                    jobDetailsNoImageUpdatedCallBack.onJobDetailsNoImageUpdated(true,
                            response.body().getMessage(),"basicsWithoutImage");
                }else{
                    jobDetailsNoImageUpdatedCallBack.onJobDetailsNoImageUpdated(false,
                            "Oops error occurred: Could not update job details","basicsWithoutImage");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                //send response data to postjobactivity
                jobDetailsNoImageUpdatedCallBack.onJobDetailsNoImageUpdated(false,
                        "Oops error occurred: Could not update job details","basicsWithoutImage");
            }
        });

    }

    //retrofit call to update the job details with the date and time
    public void updateDateTime(int jobId, String jobDate, String jobTime){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateJobDateTime(jobId, jobDate, jobTime);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());

                    //send response data to the repository
                    //success
                    jobUpdatedCallBack.onJobUpdated(true, response.body().getMessage(),
                            "dateTime");

                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                //send response data to the repository
                jobUpdatedCallBack.onJobUpdated(false, "Job details not updated",
                        "dateTime");
            }
        });
    }

    /**
     * The interface that receives whether the job was created or not
     */
    public interface JobCreatedCallBack {
        void onJobCreated(Boolean isJobPosted, String message, int job_id);
    }

    public interface JobDetailsUpdatedCallBack {
        void onJobDetailsUpdated(Boolean isJobUpdated, String message, String jobSection);
    }

    public interface JobDetailsNoImageUpdatedCallBack {
        void onJobDetailsNoImageUpdated(Boolean isJobUpdated, String message, String jobSection);
    }

    /**
     * The interface that receives whether the job was updated or not
     * send the info to the repository method which then send the message
     * to the activity which called the update method
     */
    public interface JobUpdatedCallBack {
        void onJobUpdated(Boolean isJobUpdated, String message, String jobSection);
    }

    /**
     * The interface that receives whether the offer was saved or not
     */
    public interface OfferSavedCallBack {
        void onOfferCreated(Boolean isOfferPosted, String message);
    }

    /**
     * The interface that receives whether the offer was edited or not
     */
    public interface OfferEditedCallBack {
        void onOfferEdited(Boolean isOfferEdited, String message);
    }

}
