package com.emtech.fixr.data.network;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.APIUrl;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.Categories;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostFixAppJob {
    private static final String LOG_TAG = PostFixAppJob.class.getSimpleName();

    // LiveData storing the latest downloaded weather forecasts
    private final AppExecutors mExecutors;
    JobPostedCallBack jobPostedCallBack;
    public PostJobActivity postJobActivity;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static PostFixAppJob sInstance;
    private final Context mContext;

    public PostFixAppJob(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
    }

    /**
     * Get the singleton for this class
     */
    public static PostFixAppJob getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PostFixAppJob(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    /**
     * Starts an intent service to post the job.
     */
    public void startPostJobService(int userId, String jobTitle, String jobDesc,
                                    File file, int categoryId, PostJobActivity postJobActivityInstance) {

        postJobActivity = postJobActivityInstance;
        jobPostedCallBack = postJobActivity;
        Intent intentToPost = new Intent(mContext, PostJobIntentService.class);

        Bundle jobBundle = new Bundle();
        jobBundle.putInt("userId", userId);
        jobBundle.putString("jobTitle", jobTitle);
        jobBundle.putString("jobDesc", jobDesc);
        jobBundle.putSerializable("filePath", file);
        jobBundle.putInt("categoryId", categoryId);
        intentToPost.putExtras(jobBundle);

        mContext.startService(intentToPost);
        Log.d(LOG_TAG, "Post job service created");
    }

    //retrofit call to post the job details to the server
    public void postJobDetails(int userId, String jobTitle, String jobDesc,
                               File file, int categoryId){

        //Map is used to multipart the file using okhttp3.RequestBody
        //File file = new File(mediaPath);

        //parsing any media file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*image/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody fileName = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.postJob(userId, jobTitle, jobDesc, fileToUpload, fileName, categoryId);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());

                    //send data to parent activity
                    jobPostedCallBack.onJobPosted(true, response.body().getMessage());

                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                jobPostedCallBack.onJobPosted(false, t.getMessage());
            }
        });

    }

    /**
     * The interface that receives whether the job was posted or not
     */
    public interface JobPostedCallBack {
        void onJobPosted(Boolean isJobPosted, String message);
    }

}
