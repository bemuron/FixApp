package com.emtech.fixr.presentation.ui.activity;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.app.MyApplication;
import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.PostJobBudgetFragment;
import com.emtech.fixr.presentation.ui.fragment.PostJobDateFragment;
import com.emtech.fixr.presentation.ui.fragment.PostJobFragment;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.io.File;

import static com.emtech.fixr.utilities.InjectorUtils.provideRepository;

public class PostJobActivity extends AppCompatActivity implements PostJobFragment.OnPostButtonListener,
        PostFixAppJob.JobCreatedCallBack,PostJobBudgetFragment.OnJobBudgetFragmentInteractionListener,
        PostJobDateFragment.OnJobDateFragmentInteractionListener, FixAppRepository.UpdateJobDetailsTaskListener{
    private static final String LOG_TAG = PostJobActivity.class.getSimpleName();
    private PostJobActivityViewModel postJobActivityViewModel;
    private SessionManager session;
    private ProgressDialog pDialog;
    private ScrollView layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;
    public static PostJobActivity postJobActivity;
    private int jobCreatedId = 0;
    private FixAppRepository repository;
    private boolean isUpdated;
    private String updateResponseMessage, jobDetailsSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        postJobActivity = this;

        repository = provideRepository(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        //we use this to associate the lifecycle observer(MyApplication) with this class(lifecycle owner)
        //it'll help us know when the app is in back ground or foreground
        getLifecycle().addObserver(new MyApplication());

        // Progress dialog
        pDialog = new ProgressDialog(PostJobActivity.this);
        pDialog.setCancelable(false);

        PostJobViewModelFactory factory = InjectorUtils.providePostJobActivityViewModelFactory(this.getApplicationContext());
        postJobActivityViewModel = ViewModelProviders.of
                (this, factory).get(PostJobActivityViewModel.class);

        //find the bottom sheet layout
        //layoutBottomSheet = findViewById(R.id.bottom_sheet);
        //sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
//        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                switch (newState) {
//                    case BottomSheetBehavior.STATE_HIDDEN:
//                        break;
//                    case BottomSheetBehavior.STATE_EXPANDED: {
//                        Log.e(LOG_TAG, "close sheet");
//                    }
//                    break;
//                    case BottomSheetBehavior.STATE_COLLAPSED: {
//                        Log.e(LOG_TAG, "Expand sheet");
//                    }
//                    break;
//                    case BottomSheetBehavior.STATE_DRAGGING:
//                        break;
//                    case BottomSheetBehavior.STATE_SETTLING:
//                        break;
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//            }
//        });

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.post_job_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }

        PostJobFragment postJobFragment = findOrCreateViewFragment();
        setupViewFragment(postJobFragment);

        //postJobFragment();
    }//close oncreate

    public static PostJobActivity getInstance() {
        return postJobActivity;
    }

    @Override
    public void onResume(){
        super.onResume();
        postJobActivity = this;

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //set up the post job details fragment
    private void setupViewFragment(PostJobFragment postJobFragment) {

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("PostJobFragment")
                .replace(R.id.post_job_fragment_container, postJobFragment)
                .commit();
    }

    @NonNull
    private PostJobFragment findOrCreateViewFragment() {
        //get intent from which this activity is called and the id of language selected
        //Bundle bundle = getArguments();
        int user_id = getIntent().getIntExtra("user_id", 0);
        int language_id = getIntent().getIntExtra("category_id", 0);
        String language_name = getIntent().getStringExtra("category_name");

        PostJobFragment postJobFragment = (PostJobFragment) getSupportFragmentManager()
                .findFragmentById(R.id.post_job_fragment_container);

        if (postJobFragment == null) {
            postJobFragment = PostJobFragment.newInstance(user_id, language_id, language_name);
        }
        return postJobFragment;
    }

    //open the next fragment(date and time) when the first(job details) is done
    private void setUpJobDateFragment(){
        PostJobDateFragment jobDateFragment = new PostJobDateFragment();

        if (jobDateFragment== null) {
            jobDateFragment = PostJobDateFragment.newInstance();
        }

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("PostJobDateFragment")
                .replace(R.id.post_job_fragment_container, jobDateFragment)
                .commit();
    }

    //open the next fragment(job budget) when the previous(job date time) is done
    private void setUpJobBudgetFragment(){
        PostJobBudgetFragment jobBudgetFragment = new PostJobBudgetFragment();

        if (jobBudgetFragment== null) {
            jobBudgetFragment = PostJobBudgetFragment.newInstance();
        }

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("PostJobBudgetFragment")
                .replace(R.id.post_job_fragment_container, jobBudgetFragment)
                .commit();
    }


    //post job fragment callback
    @Override
    public void jobPostDataCallback(int userId, String jobTitle, String jobDesc, String jobLocation,
                                    String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                    File file, int categoryId, PostJobActivity postJobActivityInstance) {
        pDialog.setMessage("Posting job details ...");
        showDialog();
        //call the viewmodel method to send the job to the server
        //Log.d(LOG_TAG, "Passing job details to view model");

        //if job id is > 0 we are updating the job, else its at the default
        // state of 0 - posting fresh details
        if (jobCreatedId > 0) {
            //we are updating the existing job details
            repository.getJobUpdateDetails(jobCreatedId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                    mustHaveThree, isJobRemote, file);
            Log.e(LOG_TAG, "Inside job posted callback job id = "+jobCreatedId);
            //if the file is null, then we are updating the details without an image attached
            if (file == null){
                repository.getJobUpdateDetailsWithoutImage(jobCreatedId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                        mustHaveThree, isJobRemote);
            }

        }else {
            //post details to local db and get the id of that record to keep updating with more
            //input from the user
            postJobActivityViewModel.postJob(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                    mustHaveThree, isJobRemote, file, categoryId, PostJobActivity.getInstance());
            setUpJobDateFragment();
        }
        Log.e(LOG_TAG, "Job details: userid = " + userId+ ", job title = "+ jobTitle+
                        ", job desc = " +jobDesc+", job location = "+jobLocation+ ", musthaveone = "
                        +mustHaveOne+ ", musthavetwo = "+mustHaveTwo+ ", musthavethree = "+
                mustHaveThree+ ", isJobRemote = " +isJobRemote+ ", file = " +file+", categoryid = " +categoryId);

    }

    //the job was created
    //job id has been returned
    //or not
    @Override
    public void onJobCreated(Boolean isJobCreated, String message, int job_id) {
        if (isJobCreated){
            hideDialog();
            Log.e(LOG_TAG, "New job Id = "+job_id);
            jobCreatedId = job_id;
            Toast.makeText(postJobActivity, message, Toast.LENGTH_SHORT).show();
            //go back to home activity
            /*Intent intent = new Intent(PostJobActivity.this, HomeActivity.class);
            startActivity(intent);
            this.finish();*/
        }else{
            jobCreatedId = 0;
            Log.e(LOG_TAG, "something isn't right job id = "+job_id);
            //if the job wasnt posted, display error message
            Toast.makeText(postJobActivity, message, Toast.LENGTH_SHORT).show();
        }

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);
        session.logoutUser();
        //mViewModel.delete();
    }

    //receive the user input from the job budget fragment
    //store in the local db if there is no network connection *** up for debate
    //otherwise get everything the user put in and upload to the db
    //change status of the job to 1 - posted from the default 0 -draft
    @Override
    public void onJobBudgetFragmentInteraction(String totalBudget, String estTotBudget,
                                               String pricePerHr, String totalHrs) {
        pDialog.setMessage("Posting job details ...");
        showDialog();

        //send data to the server to update the job details
        int totBudget = Integer.parseInt(totalBudget);
        int estBudget = Integer.parseInt(estTotBudget.substring(4));
        int perHrPrice = Integer.parseInt(pricePerHr);
        int totHrs = Integer.parseInt(totalHrs);

        if (perHrPrice != 0 && totHrs != 0){
            //at this point we expect the job id to be present no matter the case
            if (jobCreatedId > 0) {
                //we are updating the existing job details
                repository.getJobBudgetUpdate(jobCreatedId, 0, perHrPrice, totHrs, estBudget, 1);
            }else{
                hideDialog();
                Log.e(LOG_TAG, "Job ID not present = "+jobCreatedId);
            }
            Log.e(LOG_TAG, "From job budget frag: total budget = "+totalBudget);
            Log.e(LOG_TAG, "From job budget frag: est tot budget = "+estTotBudget);
        }
        if (totBudget != 0){
            //at this point we expect the job id to be present no matter the case
            if (jobCreatedId > 0) {
                //we are updating the existing job details
                repository.getJobBudgetUpdate(jobCreatedId, totBudget, 0, 0, estBudget, 1);
            }else{
                hideDialog();
                Log.e(LOG_TAG, "Job ID not present = "+jobCreatedId);
            }
            Log.e(LOG_TAG, "From job budget frag: price per hour = "+pricePerHr);
            Log.e(LOG_TAG, "From job budget frag: total hrs = "+totalHrs);
            Log.e(LOG_TAG, "From job budget frag: est tot budget = "+estTotBudget);
        }
    }

    //receive the user input from the job date fragment
    //cache it in the local db
    @Override
    public void onJobDateFragmentInteraction(String jobDate, String timeSelected) {
        showDialog();
        if (timeSelected == null) {
            //at this point we expect the job id to be present no matter the case
            if (jobCreatedId > 0) {
                //call async task to post job update details
                //new FixAppRepository.UpdateJobDateTimeTask(this, jobCreatedId, jobDate, null).execute();
                //we are updating the existing job details
                repository.getJobUpdateDateTime(jobCreatedId, jobDate, null);
                //checking the response status from the server
                /*if (jobDetailsSection.equals("dateTime")){
                    if (isUpdated) {
                        hideDialog();
                        Log.e(LOG_TAG, "Job date time updated successfully = " + jobCreatedId);
                        Toast.makeText(this, updateResponseMessage, Toast.LENGTH_SHORT).show();
                        //go to the next section
                        setUpJobBudgetFragment();
                    }else {
                        hideDialog();
                        Log.e(LOG_TAG, "Job date time not updated");
                    }
                }*/
            }else{
                hideDialog();
                Log.e(LOG_TAG, "Job ID not present = "+jobCreatedId);
            }
            Log.e(LOG_TAG, "From Date Frag: jobDate = " + jobDate + " timselected = " + timeSelected);
        }else{
            //at this point we expect the job id to be present no matter the case
            if (jobCreatedId > 0) {
                //we are updating the existing job details
                repository.getJobUpdateDateTime(jobCreatedId, jobDate, timeSelected);
                //call async task to post job update details
                //new FixAppRepository.UpdateJobDateTimeTask(this, jobCreatedId, jobDate, timeSelected).execute();
                //checking the response status from the server
                /*if (jobDetailsSection.equals("dateTime")){
                    if (isUpdated) {
                        hideDialog();
                        Log.e(LOG_TAG, "Job date time updated successfully = " + jobCreatedId);
                        Toast.makeText(this, updateResponseMessage, Toast.LENGTH_SHORT).show();
                        //go to the next section
                        setUpJobBudgetFragment();
                    }else {
                        hideDialog();
                        Log.e(LOG_TAG, "Job date time not updated");
                    }
                }*/
            }else{
                hideDialog();
                Log.e(LOG_TAG, "Job ID not present = "+jobCreatedId);
            }
            Log.e(LOG_TAG, "From Date Frag: jobDate = " + jobDate + " timselected = " + timeSelected);
        }

    }

    //method to update UI after job update details without image completes
    //go to next fragment
    public void updateUiAfterJobDetailsWithoutImage(Boolean isJobUpdated, String message, String jobSection){
        //checking the response status from the server
        if (jobDetailsSection.equals("basicsWithImage")){
            if (isJobUpdated) {
                hideDialog();
                Log.e(LOG_TAG, "Job details updated successfully = " + jobCreatedId);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                //go to the next section
                setUpJobDateFragment();
            }else {
                hideDialog();
                Log.e(LOG_TAG, "Job date time not updated");
            }
        }

        //checking the response status from the server
        if (jobDetailsSection.equals("basicsWithoutImage")){
            if (isJobUpdated) {
                hideDialog();
                Log.e(LOG_TAG, "Job details updated successfully = " + jobCreatedId);
                Toast.makeText(this, updateResponseMessage, Toast.LENGTH_SHORT).show();
                //go to the next section
                setUpJobDateFragment();
            }else {
                hideDialog();
                Log.e(LOG_TAG, "Job details without image not updated");
            }
        }
    }

    //method to update UI after job update date time completes
    public void updateUiAfterJobDateUpdate(Boolean isJobUpdated, String message, String jobSection){
        //if (jobDetailsSection.equals("dateTime")){
            if (isJobUpdated) {
                hideDialog();
                Log.e(LOG_TAG, "Job date time updated successfully = " + jobCreatedId);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                //go to the next section
                setUpJobBudgetFragment();
            }else {
                hideDialog();
                Log.e(LOG_TAG, "Job date time not updated");
            }
        //}
    }

    //method to update UI after job update date time completes
    public void updateUiAfterJobBudgetUpdate(Boolean isJobUpdated, String message, String jobSection){
        if (isJobUpdated) {
            hideDialog();
            Log.e(LOG_TAG, "Job posted successfully = " + jobCreatedId);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else {
            hideDialog();
            Log.e(LOG_TAG, message);
        }
    }

    //receive the response from the repository on whether the job details have been updated or not
    @Override
    public void onUpdateFinish(Boolean isJobUpdated, String message, String jobSection) {
        isUpdated = isJobUpdated;
        updateResponseMessage = message;
        jobDetailsSection = jobSection;
    }
}
