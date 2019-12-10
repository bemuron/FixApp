package com.emtech.fixr.data;

import android.arch.lifecycle.LiveData;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.database.CategoriesDao;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.database.UsersDao;
import com.emtech.fixr.data.network.FixAppNetworkDataSource;
import com.emtech.fixr.data.network.FetchCategories;
import com.emtech.fixr.data.network.LoginUser;
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.data.network.RegisterUser;
import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.FixAppCategory;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emtech.fixr.presentation.ui.activity.PostJobActivity.postJobActivity;

/**
 * the FixAppRepository is a singleton.
 * Handles data operations in FixApp. Acts as a mediator between {@link FixAppNetworkDataSource}
 * and {@link CategoriesDao}
 */

public class FixAppRepository implements PostFixAppJob.JobUpdatedCallBack {
    private static final String LOG_TAG = FixAppRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static FixAppRepository sInstance;
    private CategoriesDao mCategoryDao;
    private AppExecutors mExecutors;
    private FetchCategories mFetchCategories;
    private static PostFixAppJob mPostFixAppJob;
    private LoginUser mLoginUser;
    private RegisterUser mRegisterUser;
    private boolean mInitialized = false;
    private UsersDao mUsersDao;
    private Cursor mUserDetail;
    private int mCount;
    private static boolean isUpdated;
    private static String updateResponseMessage;
    private static String jobDetailsSection;
    public UpdateJobDetailsTaskListener mListener;

    private FixAppRepository(CategoriesDao categoryDao, UsersDao usersDao, FetchCategories fetchCategories,
                               PostFixAppJob postFixAppJob, RegisterUser registerUser,
                             LoginUser loginUser, AppExecutors executors) {
        mUsersDao = usersDao;
        mCategoryDao = categoryDao;
        mFetchCategories = fetchCategories;
        mPostFixAppJob = postFixAppJob;
        mRegisterUser = registerUser;
        mLoginUser = loginUser;
        mExecutors = executors;
        LiveData<Category[]> fixAppCategories = mFetchCategories.getCurrentCategories();

        /*
        * Why use observeForever()?

          observeForever() is very similar to observe, with one major difference, it is always considered
          active. Because of this, it does not take an object with a Lifecycle. Why are you using it here?
          FixAppRepository is observing FetchCategories; neither of these have an associated UI
          controller lifecycle, rather, they exist for the entire lifecycle of the app. Therefore,
          you can safely use observeForever().

        * */

        fixAppCategories.observeForever(newCategoriesFromNetwork -> mExecutors.diskIO().execute(() -> {
            // Insert our categories into FixApp's database
            mCategoryDao.insertCategory(newCategoriesFromNetwork);

            Log.d(LOG_TAG, newCategoriesFromNetwork.length +" categories inserted");
        }));

    }

    public synchronized static FixAppRepository getInstance(
            CategoriesDao categoryDao, UsersDao usersDao, FetchCategories fetchCategories,
            PostFixAppJob postFixAppJob, RegisterUser registerUser, LoginUser loginUser,
            AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FixAppRepository(categoryDao, usersDao, fetchCategories,
                        postFixAppJob, registerUser, loginUser, executors);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    private synchronized void initializeData() {

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;

        if (isFetchNeeded()) {
            mExecutors.diskIO().execute(this::startFetchCategoryService);
        }
    }

    /**
     * Database related operations
     **/

    public LiveData<List<Category>> getAllCategories(){
        initializeData();
        return mCategoryDao.getAllCategories();
    }

    public Cursor getUser(){
        mUserDetail = mUsersDao.getUserDetails();

        return mUserDetail;
    }

    //a wrapper for the insert() method. Must be called on a non UI thread
    //or the app will crash
    public void insertUser (User user){
        mExecutors.diskIO().execute(() ->{
            mUsersDao.insertUser(user);

            Log.d(LOG_TAG, user.getEmail()+" user inserted into db");
        });
    }

    //delete user details from db
    //required when user logs out of app
    public void deleteUser (){
        mUsersDao.deleteUser();
    }

    /**
     * Checks if there are enough days of future weather for the app to display all the needed data.
     *
     * @return Whether a fetch is needed
     */
    /**
     * Checks if there are more than one category in the db.
     *
     * @return Whether a fetch is needed
     */
    private boolean isFetchNeeded() {
        mExecutors.diskIO().execute(() ->
                mCount = mCategoryDao.countCategoriesInDb());
        return (mCount < 1);
    }

    /**
     * Network related operation
     */

    /*
     * call startFetchCategoryService() from FetchCategories which
     * creates and starts the IntentService.
     * */
    private void startFetchCategoryService() {
        mFetchCategories.startFetchCategoryService();
    }

    public void postJobService(int userId, String jobTitle, String jobDesc, String jobLocation,
                               String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                               File file, int categoryId, PostJobActivity postJobActivity){
        mPostFixAppJob.startPostJobService(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, file, categoryId, postJobActivity);
    }

    //method to call service to login user
    public void loginFixAppUser(String email, String password){
        mLoginUser.startLoginUserService(email, password);
    }

    //returning if login is successful or not
    public void OnSuccessfulLogin(Boolean isLoginSuccessful){

    }

    //method to register user in database
    //calls service
    public void registerFixAppUser(String name, String date_of_birth, String gender, String email, String password){
        mRegisterUser.startRegisterUserService(name, date_of_birth, gender, email, password);
    }

    //method to receive updated job details from activity and pass them to the
    //Async task to post to the db
    //this one is called when the image is attached too
    public void getJobUpdateDetails(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                     String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                     File file){

        //call async task to post job update details
        new UpdateJobDetailsTask(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, file, mListener).execute();

    }

    //method to receive updated job details from activity and pass them to the
    //Async task to post to the db
    //this one is called when the image is attached too
    public void getJobUpdateDetailsWithoutImage(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                    String mustHaveTwo, String mustHaveThree, int isJobRemote){

        //call async task to post job update details
        new UpdateJobDetailsWithouImageTask(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, mListener).execute();

    }

    //method to receive updated job date and time from activity and pass them to the
    //Async task to post to the db
    public void getJobUpdateDateTime(int jobId, String jobDate, String jobTime){

        //call async task to post job update details
       // new UpdateJobDateTimeTask(jobId, jobDate, jobTime).execute();
        mExecutors.diskIO().execute(() -> updateDateTime(jobId, jobDate, jobTime));

    }

    //method to receive updated job budget from activity and pass them to the
    //Async task to post to the db
    public void getJobBudgetUpdate(int jobId, int totalBudget, int pricePerHr, int totalHrs, int estTotalBudget){

        //call async task to post job update details
        //new UpdateJobBudgetTask(jobId, totalBudget, pricePerHr, totalHrs, estTotalBudget, mListener).execute();
        mExecutors.diskIO().execute(() -> updateBudget(jobId, totalBudget, pricePerHr, totalHrs, estTotalBudget));
    }

    //interface method from PostFixAppJob to get the status of the job details update or post
    @Override
    public void onJobUpdated(Boolean isJobUpdated, String message, String jobSection) {
        isUpdated = isJobUpdated;
        updateResponseMessage = message;
        jobDetailsSection = jobSection;

    }

    //update job details async task when image is attached
    public class UpdateJobDetailsTask extends AsyncTask<Void, Void, Void>
    {
        private UpdateJobDetailsTaskListener mListener;
        private int jobId, isJobRemote;
        private String jobTitle, jobDesc,jobLocation, mustHaveOne, mustHaveTwo, mustHaveThree;
        private File file;

        public UpdateJobDetailsTask(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                    String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                    File file, UpdateJobDetailsTaskListener listener){
            this.jobId = jobId;
            this.jobTitle = jobTitle;
            this.jobDesc = jobDesc;
            this.jobLocation = jobLocation;
            this.mustHaveOne = mustHaveOne;
            this.mustHaveTwo = mustHaveTwo;
            this.mustHaveThree = mustHaveThree;
            this.isJobRemote = isJobRemote;
            this.file = file;
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            //call method to update details
            mPostFixAppJob.updateJobDetails(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                    mustHaveThree, isJobRemote, file);

            return null;
        }

        protected void onPostExecute(Void result)
        {
            if(mListener != null)
            {
                if (jobDetailsSection.equals("basicsWithImage"))
                    mListener.onUpdateFinish(isUpdated, updateResponseMessage, jobDetailsSection);
            }
        }
    }

    //update job details async task when image is attached
    public class UpdateJobDetailsWithouImageTask extends AsyncTask<Void, Void, Void>
    {
        private UpdateJobDetailsTaskListener mListener;
        private int jobId, isJobRemote;
        private String jobTitle, jobDesc,jobLocation, mustHaveOne, mustHaveTwo, mustHaveThree;

        public UpdateJobDetailsWithouImageTask(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                              String mustHaveTwo, String mustHaveThree,
                                              int isJobRemote, UpdateJobDetailsTaskListener listener){
            this.jobId = jobId;
            this.jobTitle = jobTitle;
            this.jobDesc = jobDesc;
            this.jobLocation = jobLocation;
            this.mustHaveOne = mustHaveOne;
            this.mustHaveTwo = mustHaveTwo;
            this.mustHaveThree = mustHaveThree;
            this.isJobRemote = isJobRemote;
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            //call method to update details
            mPostFixAppJob.updateJobDetailsWithoutImage(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                    mustHaveThree, isJobRemote);

            return null;
        }

        protected void onPostExecute(Void result)
        {
            if(mListener != null)
            {
                if (jobDetailsSection.equals("basicsWithoutImage"))
                mListener.onUpdateFinish(isUpdated, updateResponseMessage, jobDetailsSection);
            }
        }
    }

    //update job details date and time
    public static class UpdateJobDateTimeTask extends AsyncTask<Void, Void, Boolean>
    {
        private WeakReference<PostJobActivity> activityWeakReference;
        private UpdateJobDetailsTaskListener mListener;
        private int jobId;
        private String jobDate,jobTime;

        //only retain a weak reference to the activity
        public UpdateJobDateTimeTask(PostJobActivity context, int jobId, String jobDate,
                                     String jobTime){
            activityWeakReference = new WeakReference<>(context);
            this.jobId = jobId;
            this.jobDate = jobDate;
            this.jobTime = jobTime;
            //this.mListener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            try {
                //call method to update details
                return true;//updateDateTime(jobId, jobDate, jobTime);
            }catch(Exception e){
                Log.e(LOG_TAG, e.getMessage());
                Log.e(LOG_TAG, "JOB DateTime update failed");
                return false;
            }
        }

        protected void onPostExecute(Boolean success)
        {
            //get a reference to the activity if its still there
            PostJobActivity postJobActivity = activityWeakReference.get();
            if (postJobActivity == null || postJobActivity.isFinishing())return;

            if (success) {
                updateResponseMessage = "Job details updated";
                jobDetailsSection = "dateTime";
            }else{
                updateResponseMessage = "Could not update job details";
                jobDetailsSection = "dateTime";
            }

            //call activity method to update UI
            postJobActivity.updateUiAfterJobDateUpdate(success, updateResponseMessage, jobDetailsSection);
            Log.e(LOG_TAG, "isUpdated value in postExecute JobDateTime = "+ success);
            Log.e(LOG_TAG, "isUpdated value in postExecute JobDateTime = "+ isUpdated);
            Log.e(LOG_TAG, "isUpdated value in postExecute updateResponseMessage = "+ updateResponseMessage);
            Log.e(LOG_TAG, "isUpdated value in postExecute jobDetailsSection = "+ jobDetailsSection);

            /*if(mListener != null)
            {
                if (jobDetailsSection.equals("dateTime"))
                    mListener.onUpdateFinish(isUpdated, updateResponseMessage, jobDetailsSection);
            }*/
        }
    }

    //retrofit call to update the job details with the budget
    public void updateBudget(int jobId, int totalBudget, int pricePerHr, int totalHrs, int estTotalBudget){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateJobBudget(jobId, totalBudget, pricePerHr, totalHrs, estTotalBudget);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());
                    //send response data to the repository
                    //success
                    postJobActivity.updateUiAfterJobBudgetUpdate(true, response.body().getMessage(),
                            "budget");
                }else{
                    updateResponseMessage = response.body().getMessage();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                postJobActivity.updateUiAfterJobBudgetUpdate(false, updateResponseMessage,
                        "budget");
            }
        });

    }

    //retrofit calls to perform actual posting of data
    //retrofit call to update the job details with the date and time
    private void updateDateTime(int jobId, String jobDate, String jobTime){
        String mysqlDate = null;
        //convert the date coming in to the one mysql expects
        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        
        try{
            Date d = sdf.parse(jobDate);
            mysqlDate = mysqlDateFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.e(LOG_TAG, mysqlDate);

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateJobDateTime(jobId, mysqlDate, jobTime);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());

                    //send response data to the postjobactivity
                    //success
                    postJobActivity.updateUiAfterJobDateUpdate(true, response.body().getMessage(), "dateTime");
                    /*isUpdated = true;
                    updateResponseMessage = response.body().getMessage();
                    jobDetailsSection = "dateTime";*/
                    Log.e(LOG_TAG, "DateTime Retrofit call success. isUpdated = "+ isUpdated);

                }else{
                    updateResponseMessage = response.body().getMessage();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());

                postJobActivity.updateUiAfterJobDateUpdate(false, updateResponseMessage, "dateTime");
                /*isUpdated = false;
                updateResponseMessage = "job details not updated";
                jobDetailsSection = "dateTime";*/
                Log.e(LOG_TAG, "DateTime Retrofit call success. isUpdated = "+ isUpdated);
            }
        });
    }

    //interface to communicate job details update status to activity
    public interface UpdateJobDetailsTaskListener {
        void onUpdateFinish(Boolean isJobUpdated, String message, String jobSection);
    }

}
