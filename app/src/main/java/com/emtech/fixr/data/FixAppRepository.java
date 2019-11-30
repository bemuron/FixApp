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
import com.emtech.fixr.models.FixAppCategory;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * the FixAppRepository is a singleton.
 * Handles data operations in FixApp. Acts as a mediator between {@link FixAppNetworkDataSource}
 * and {@link CategoriesDao}
 */

public class FixAppRepository {
    private static final String LOG_TAG = FixAppRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static FixAppRepository sInstance;
    private CategoriesDao mCategoryDao;
    private AppExecutors mExecutors;
    private FetchCategories mFetchCategories;
    private PostFixAppJob mPostFixAppJob;
    private LoginUser mLoginUser;
    private RegisterUser mRegisterUser;
    private boolean mInitialized = false;
    private UsersDao mUsersDao;
    private Cursor mUserDetail;
    private int mCount;

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

    //update job details async task when image is attached
    public static class UpdateJobDetailsTask extends AsyncTask<String, Float, Long>
    {
        public UpdateJobDetailsTaskListener mListener;

        public interface UpdateJobDetailsTaskListener
        {
            public void onUpdateFinish();
            public void onUpdateProgress(float progress);
        }

        @Override
        protected Long doInBackground(String... arg0)
        {
            //start downloading

            return 0L; //return download size
        }

        protected void onProgressUpdate(Float... progress)
        {
            if(mListener != null)
            {
                mListener.onUpdateProgress(progress[0]);
            }
        }

        protected void onPostExecute(Long result)
        {
            if(mListener != null)
            {
                mListener.onUpdateFinish();
            }
        }
    }

}
