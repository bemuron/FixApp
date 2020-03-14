package com.emtech.fixr.data;

import androidx.lifecycle.LiveData;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.app.Config;
import com.emtech.fixr.data.database.CategoriesDao;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.database.UsersDao;
import com.emtech.fixr.data.network.BrowsedJobsDataSource;
import com.emtech.fixr.data.network.FixAppNetworkDataSource;
import com.emtech.fixr.data.network.FetchCategories;
import com.emtech.fixr.data.network.GetMyJobs;
import com.emtech.fixr.data.network.LoginUser;
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.data.network.RegisterUser;
import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.ui.activity.LoginActivity;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emtech.fixr.presentation.ui.activity.LoginActivity.loginActivityInstance;
import static com.emtech.fixr.presentation.ui.activity.PostJobActivity.postJobActivity;

/**
 * the FixAppRepository is a singleton.
 * Handles data operations in FixApp. Acts as a mediator between {@link FixAppNetworkDataSource}
 * and {@link CategoriesDao}
 */

public class FixAppRepository implements PostFixAppJob.JobUpdatedCallBack, PostFixAppJob.OfferSavedCallBack,
        PostFixAppJob.OfferEditedCallBack {
    private static final String LOG_TAG = FixAppRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static FixAppRepository sInstance;
    private CategoriesDao mCategoryDao;
    private AppExecutors mExecutors;
    private FetchCategories mFetchCategories;
    private BrowsedJobsDataSource mBrowsedJobs;
    private GetMyJobs mGetMyJobs;
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
                             LoginUser loginUser, GetMyJobs getMyJobs,
                             BrowsedJobsDataSource browsedJobsDataSource, AppExecutors executors) {
        mUsersDao = usersDao;
        mCategoryDao = categoryDao;
        mFetchCategories = fetchCategories;
        mPostFixAppJob = postFixAppJob;
        mRegisterUser = registerUser;
        mLoginUser = loginUser;
        mGetMyJobs = getMyJobs;
        mBrowsedJobs = browsedJobsDataSource;
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
            //delete all previous categories from the db, this ensures that if we have new
            //categories on the main server(online) then we can add them too
            deleteOldData();
            Log.d(LOG_TAG, "Old categories deleted");
            // Insert our categories into FixApp's database
            mCategoryDao.insertCategory(newCategoriesFromNetwork);

            Log.d(LOG_TAG, newCategoriesFromNetwork.length +" categories inserted");
        }));

    }

    public synchronized static FixAppRepository getInstance(
            CategoriesDao categoryDao, UsersDao usersDao, FetchCategories fetchCategories,
            PostFixAppJob postFixAppJob, RegisterUser registerUser, LoginUser loginUser,
            GetMyJobs getMyJobs, BrowsedJobsDataSource browsedJobsDataSource, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new FixAppRepository(categoryDao, usersDao, fetchCategories,
                        postFixAppJob, registerUser, loginUser, getMyJobs,
                        browsedJobsDataSource, executors);
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

        mExecutors.diskIO().execute(() -> {
            if (isFetchNeeded()) {
                startFetchCategoryService();
            }
        });

        if (isFetchNeeded()) {
            mExecutors.diskIO().execute(this::startFetchCategoryService);
        }
    }

    public AppExecutors getExecutors(){
        return mExecutors;
    }

    /**
     * Database related operations
     **/

    private void deleteOldData() {
        mCategoryDao.deleteAllCategories();
    }

    public LiveData<List<Category>> getAllCategories(){
        initializeData();
        return mCategoryDao.getAllCategories();
    }

    //getting all jobs associated with a user
    public LiveData<List<Job>> getAllJobs(int user_id){
        Log.d(LOG_TAG, "calling bg method to get jobs list");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetJobs(user_id));
        return mGetMyJobs.getJobsForUser();
    }

    //getting all jobs associated with a user by status selected
    public LiveData<List<Job>> getJobsByStatus(int user_id, int status){
        Log.d(LOG_TAG, "calling bg method to get jobs list");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetJobsBySatus(user_id, status));
        return mGetMyJobs.getJobsForUserByStatus();
    }

    //getting job details of a selected job
    public LiveData<Job> getJobDetails(int job_id){
        Log.d(LOG_TAG, "calling bg method to get job details");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetJobDetails(job_id));
        return mGetMyJobs.getJobDetails();
    }

    //getting all jobs for browsing
    public LiveData<List<Job>> browseAllJobs(){
        Log.d(LOG_TAG, "calling bg method to get all jobs for browsing");
        //mExecutors.diskIO().execute(() -> mGetMyJobs.BrowseJobs());
        return mBrowsedJobs.getJobsForBrowsing();
    }

    //getting all jobs searched
    public LiveData<List<Job>> searchForJobs(String searchQuery){
        Log.d(LOG_TAG, "calling bg method to return all jobs searched for");
        //mExecutors.diskIO().execute(() -> mGetMyJobs.SearchJobs(searchQuery));
        return mBrowsedJobs.getSearchedJobs();
    }

    //a getter method for all the jobs a fixer has made an offer to.
    public LiveData<List<Offer>> getOffersMade(int user_id){
        Log.d(LOG_TAG, "calling bg method to get offers made list");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetOffers(user_id, "made"));
        return mGetMyJobs.getOffersMade();
    }

    //a getter method for all the jobs a fixer made an offer to and have been accepted
    public LiveData<List<Offer>> getOffersAccepted(int user_id){
        Log.d(LOG_TAG, "calling bg method to get offers accepted list");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetOffers(user_id, "accepted"));
        return mGetMyJobs.getOffersMade();
    }

    //getting offer details of a selected job
    public LiveData<Offer> getOfferDetailsForFixer(int offer_id){
        Log.d(LOG_TAG, "calling bg method to get offer details for fixer");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetOfferDetailsForFixer(offer_id));
        return mGetMyJobs.getOfferDetails();
    }

    //a getter method for all the jobs by a poster to which offers have been made
    public LiveData<List<Offer>> getOffersReceived(int user_id){
        Log.d(LOG_TAG, "calling bg method to get offers received list");
        mExecutors.diskIO().execute(() -> mGetMyJobs.getOffersReceived(user_id));
        return mGetMyJobs.getOffersMade();
    }

    //getting offer details of a selected job
    public LiveData<Offer> getOfferDetailsForPoster(int offer_id){
        Log.d(LOG_TAG, "calling bg method to get offer details for poster");
        mExecutors.diskIO().execute(() -> mGetMyJobs.GetOfferDetailsForPoster(offer_id));
        return mGetMyJobs.getOfferDetails();
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
        Log.e(LOG_TAG, "Category count in db = "+mCount);
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

    //method to post the offer made by the potential fixer/tasker
    public void saveFixerOffer(int amountOffered, String offerMessage, int userId, int jobId){
        //call retrofit in background to post the offer for the job
        mExecutors.diskIO().execute(() -> mPostFixAppJob.saveOffer(amountOffered, offerMessage, userId, jobId) );
    }

    //method to edit the offer made by the fixer/tasker
    public void editFixerOffer(int offerId, int amountOffered, String offerMessage, int editCount){
        //call retrofit in background to post the offer for the job
        mExecutors.diskIO().execute(() -> mPostFixAppJob.editOffer(offerId, amountOffered, offerMessage, editCount) );
    }

    //method to call service to login user
    public void loginFixAppUser(String email, String password){
        mLoginUser.startLoginUserService(email, password);
    }

    //method to update the user's FCM id in the db
    public void updateFcmId(int userId, String token){
        //call retrofit in background to post the offer for the job
        mExecutors.diskIO().execute(() -> updateFcm(userId, token) );
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

        //call retrofit in background to post job update details
        mExecutors.diskIO().execute(() -> updateJobDetails(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, file) );

    }

    //method to receive updated job details from activity and pass them to the
    //Async task to post to the db
    //this one is called when the image is attached too
    public void getJobUpdateDetailsWithoutImage(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                    String mustHaveTwo, String mustHaveThree, int isJobRemote){

        //call retrofit in background to post job update details
        mExecutors.diskIO().execute(() -> updateJobDetailsWithoutImage(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote));

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
    public void getJobBudgetUpdate(int jobId, int totalBudget, int pricePerHr, int totalHrs, int estTotalBudget, int jobStatus){

        //call async task to post job update details
        //new UpdateJobBudgetTask(jobId, totalBudget, pricePerHr, totalHrs, estTotalBudget, mListener).execute();
        mExecutors.diskIO().execute(() -> updateBudget(jobId, totalBudget, pricePerHr, totalHrs, estTotalBudget, jobStatus));
    }

    //interface method from PostFixAppJob to get the status of the job details update or post
    @Override
    public void onJobUpdated(Boolean isJobUpdated, String message, String jobSection) {
        isUpdated = isJobUpdated;
        updateResponseMessage = message;
        jobDetailsSection = jobSection;
    }

    //retrofit call to register device token in mysql for fcm
    private void updateFcm(int userId, String fcm_registration_id){

        Log.d(LOG_TAG, "User device registration for fcm started");

        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateFcm(userId, fcm_registration_id);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                    try {
                        //if response body is not null, we have some data
                        //successful login
                        if (!response.body().getError()) {

                            loginActivityInstance.updateUIAfterFcmUpdate(true, response.body().getMessage());

                        } else {
                            loginActivityInstance.updateUIAfterFcmUpdate(false, response.body().getMessage());
                        }
                    }catch (Exception e){
                        Log.e(LOG_TAG, e.getMessage());
                        e.printStackTrace();
                        loginActivityInstance.updateUIAfterFcmUpdate(false,e.getMessage());
                    }

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                loginActivityInstance.updateUIAfterFcmUpdate(false, t.getMessage());
            }
        });

    }

    //retrofit call to post the job details to the server
    //when image is attached
    public void updateJobDetails(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                 String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                 File file){

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
        Call<Result> call = service.updateJob(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, fileToUpload, fileName);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());
                    //send response data to postjobactivity
                    //success
                    postJobActivity.updateUiAfterJobDetailsWithoutImage(true,
                            response.body().getMessage(),"basicsWithImage");
                }else{
                    updateResponseMessage = response.body().getMessage();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                //send response data to the postjobactivity
                postJobActivity.updateUiAfterJobDetailsWithoutImage(true,
                        updateResponseMessage,"basicsWithImage");
            }
        });

    }

    //retrofit call to update the job details to the server when an image is not added by the user
    public void updateJobDetailsWithoutImage(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                             String mustHaveTwo, String mustHaveThree, int isJobRemote){

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
                    postJobActivity.updateUiAfterJobDetailsWithoutImage(true,
                            response.body().getMessage(),"basicsWithoutImage");
                }else{
                    updateResponseMessage = response.body().getMessage();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                //send response data to postjobactivity
                postJobActivity.updateUiAfterJobDetailsWithoutImage(true,
                        updateResponseMessage,"basicsWithoutImage");
            }
        });

    }

    //retrofit call to update the job details with the budget
    public void updateBudget(int jobId, int totalBudget, int pricePerHr, int totalHrs, int estTotalBudget, int jobStatus){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.updateJobBudget(jobId, totalBudget, pricePerHr, totalHrs, estTotalBudget, jobStatus);

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

    @Override
    public void onOfferCreated(Boolean isOfferPosted, String message) {

    }

    @Override
    public void onOfferEdited(Boolean isOfferEdited, String message) {

    }

    //interface to communicate job details update status to activity
    public interface UpdateJobDetailsTaskListener {
        void onUpdateFinish(Boolean isJobUpdated, String message, String jobSection);
    }

    //interface to communicate job details update status to activity
    public interface OfferEditedListener {
        void onOfferEdited(Boolean isOfferPosted, String message);
    }

    //interface to communicate job details update status to activity
    public interface OfferSavedListener {
        void onOfferSaved(Boolean isOfferSaved, String message);
    }

}
