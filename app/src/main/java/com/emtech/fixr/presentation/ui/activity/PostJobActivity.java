package com.emtech.fixr.presentation.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.emtech.fixr.R;
import com.emtech.fixr.app.MyApplication;
import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.PostJobBudgetFragment;
import com.emtech.fixr.presentation.ui.fragment.PostJobDateFragment;
import com.emtech.fixr.presentation.ui.fragment.PostJobDetailsFragment;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.emtech.fixr.presentation.adapters.SectionsPagerAdapter;

import java.io.File;

import static com.emtech.fixr.utilities.InjectorUtils.provideRepository;

public class PostJobActivity extends AppCompatActivity implements PostJobDetailsFragment.OnPostButtonListener,
        PostFixAppJob.JobCreatedCallBack, PostJobBudgetFragment.OnJobBudgetFragmentInteractionListener,
        PostJobDateFragment.OnJobDateFragmentInteractionListener, FixAppRepository.UpdateJobDetailsTaskListener{
private static final String LOG_TAG = PostJobActivity.class.getSimpleName();
private PostJobActivityViewModel postJobActivityViewModel;
private SessionManager session;
private ProgressBar progressBar;
private ScrollView layoutBottomSheet;
private BottomSheetBehavior sheetBehavior;
public static PostJobActivity postJobActivity;
private int jobCreatedId = 0;
private FixAppRepository repository;
private boolean isUpdated;
private String updateResponseMessage, jobDetailsSection;
private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        setupActionBar();

        int user_id = getIntent().getIntExtra("user_id", 0);
        int category_id = getIntent().getIntExtra("category_id", 0);
        String category_name = getIntent().getStringExtra("category_name");

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

        // Progress bar
        progressBar = findViewById(R.id.post_job_progress_bar);
        hideBar();

        PostJobViewModelFactory factory = InjectorUtils.providePostJobActivityViewModelFactory(this.getApplicationContext());
        postJobActivityViewModel = new ViewModelProvider
                (this, factory).get(PostJobActivityViewModel.class);

        TabLayout tabs = findViewById(R.id.tabs);

        mViewPager = findViewById(R.id.view_pager);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(),
                user_id, category_id, category_name);

        tabs.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(sectionsPagerAdapter);

        //viewPager.setOffscreenPageLimit(3);
        //viewPager.setAdapter(sectionsPagerAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static PostJobActivity getInstance() {
        return postJobActivity;
    }

    //set up the post job details fragment
    private void setupViewFragment(PostJobDetailsFragment postJobDetailsFragment) {

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("PostJobDetailsFragment")
                .replace(R.id.post_job_fragment_container, postJobDetailsFragment)
                .commit();
    }

    //open the next fragment(date and time) when the first(job details) is done
    private void setUpJobDateFragment(){
        mViewPager.setCurrentItem(1,true);
        /*PostJobDateFragment jobDateFragment = new PostJobDateFragment();

        if (jobDateFragment== null) {
            jobDateFragment = PostJobDateFragment.newInstance();
        }

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("PostJobDateFragment")
                .replace(R.id.post_job_fragment_container, jobDateFragment)
                .commit();*/
    }

    //open the next fragment(job budget) when the previous(job date time) is done
    private void setUpJobBudgetFragment(){
        mViewPager.setCurrentItem(2,true);
        /*PostJobBudgetFragment jobBudgetFragment = new PostJobBudgetFragment();

        if (jobBudgetFragment== null) {
            jobBudgetFragment = PostJobBudgetFragment.newInstance();
        }

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("PostJobBudgetFragment")
                .replace(R.id.post_job_fragment_container, jobBudgetFragment)
                .commit();*/
    }

    //post job fragment callback
    @Override
    public void jobPostDataCallback(int userId, String jobTitle, String jobDesc, String jobLocation,
                                    String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                    File file, int categoryId, PostJobActivity postJobActivityInstance) {
        showBar();
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
            Log.e(LOG_TAG, "New job Id = "+job_id);
            jobCreatedId = job_id;
            Toast.makeText(postJobActivity, message, Toast.LENGTH_SHORT).show();
            //go back to home activity
            /*Intent intent = new Intent(PostJobActivity.this, HomeActivity.class);
            startActivity(intent);
            this.finish();*/
            hideBar();
        }else{
            jobCreatedId = 0;
            Log.e(LOG_TAG, "something isn't right job id = "+job_id);
            //if the job wasnt posted, display error message
            Toast.makeText(postJobActivity, message, Toast.LENGTH_SHORT).show();
            hideBar();
        }

    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        showBar();

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
        showBar();
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
                        hideBar();
                        Log.e(LOG_TAG, "Job date time updated successfully = " + jobCreatedId);
                        Toast.makeText(this, updateResponseMessage, Toast.LENGTH_SHORT).show();
                        //go to the next section
                        setUpJobBudgetFragment();
                    }else {
                        hideBar();
                        Log.e(LOG_TAG, "Job date time not updated");
                    }
                }*/
            }else{
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
                        hideBar();
                        Log.e(LOG_TAG, "Job date time updated successfully = " + jobCreatedId);
                        Toast.makeText(this, updateResponseMessage, Toast.LENGTH_SHORT).show();
                        //go to the next section
                        setUpJobBudgetFragment();
                    }else {
                        hideBar();
                        Log.e(LOG_TAG, "Job date time not updated");
                    }
                }*/
            }else{
                Log.e(LOG_TAG, "Job ID not present = "+jobCreatedId);
            }
            Log.e(LOG_TAG, "From Date Frag: jobDate = " + jobDate + " timselected = " + timeSelected);
        }

    }

    //method to update UI after job update details without image completes
    //go to next fragment
    public void updateUiAfterJobDetailsWithoutImage(Boolean isJobUpdated, String message, String jobSection){
        hideBar();
        //checking the response status from the server
        if (jobDetailsSection.equals("basicsWithImage")){
            if (isJobUpdated) {
                //hideBar();
                Log.e(LOG_TAG, "Job details updated successfully = " + jobCreatedId);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                //go to the next section
                setUpJobDateFragment();
            }else {
                //hideBar();
                Log.e(LOG_TAG, "Job date time not updated");
            }
        }

        //checking the response status from the server
        if (jobDetailsSection.equals("basicsWithoutImage")){
            if (isJobUpdated) {
                //hideBar();
                Log.e(LOG_TAG, "Job details updated successfully = " + jobCreatedId);
                Toast.makeText(this, updateResponseMessage, Toast.LENGTH_SHORT).show();
                //go to the next section
                setUpJobDateFragment();
            }else {
                //hideBar();
                Log.e(LOG_TAG, "Job details without image not updated");
            }
        }
    }

    //method to update UI after job update date time completes
    public void updateUiAfterJobDateUpdate(Boolean isJobUpdated, String message, String jobSection){
        hideBar();
        //if (jobDetailsSection.equals("dateTime")){
        if (isJobUpdated) {
            //hideBar();
            Log.e(LOG_TAG, "Job date time updated successfully = " + jobCreatedId);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            //go to the next section
            setUpJobBudgetFragment();
        }else {
            //hideBar();
            Log.e(LOG_TAG, "Job date time not updated");
        }
        //}
    }

    //method to update UI after job update date time completes
    public void updateUiAfterJobBudgetUpdate(Boolean isJobUpdated, String message, String jobSection){
        hideBar();
        if (isJobUpdated) {
            //hideBar();
            Log.e(LOG_TAG, "Job posted successfully = " + jobCreatedId);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }else {
            //hideBar();
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