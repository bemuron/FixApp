package com.emtech.fixr.presentation.ui.activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.app.ProgressDialog;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.MakeOfferDialogFragment;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JobDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        MakeOfferDialogFragment.MakeOfferDialogListener
        {
    private static final String LOG_TAG = JobDetailsActivity.class.getSimpleName();
    private TextView jobTitleTV, postedByTV, timePostedTV, locationTV,
            toBeDoneDateTV, toBeDoneTimeTV, jobPriceTV, jobDetailsET;
    private Button makeOfferButton;
    private MyJobsActivityViewModel mViewModel;
    private PostJobActivityViewModel postJobActivityViewModel;
    private Job job;
    private ProgressDialog pDialog;
    private SessionManager session;
    private MakeOfferDialogFragment dialogFragment;
    private int userId, job_id;
    private String userRole, jobName, jobPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);
        setupActionBar();

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Fetching job details ...");
        showDialog();

        job_id = getIntent().getIntExtra("jobID", 0);
        jobName = getIntent().getStringExtra("jobName");

        //initialise the views
        setUpWidgets();

        PostJobViewModelFactory factory1 = InjectorUtils.providePostJobActivityViewModelFactory(this.getApplicationContext());
        postJobActivityViewModel = ViewModelProviders.of
                (this, factory1).get(PostJobActivityViewModel.class);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this.getApplicationContext());
        mViewModel = ViewModelProviders.of
                (this, factory).get(MyJobsActivityViewModel.class);

        mViewModel.getJobDetails(job_id).observe(this, jobDetails -> {
            clearViews();

            if (jobDetails != null) {
                job = new Job();
                job.setCategory_id(jobDetails.getCategory_id());
                job.setJob_id(jobDetails.getJob_id());
                job.setPosted_by(jobDetails.getPosted_by());
                job.setName(jobDetails.getName());
                job.setDescription(jobDetails.getDescription());
                job.setMust_have_one(jobDetails.getMust_have_one());
                job.setMust_have_two(jobDetails.getMust_have_two());
                job.setMust_have_three(jobDetails.getMust_have_three());
                job.setIs_job_remote(jobDetails.getIs_job_remote());
                job.setLocation(jobDetails.getLocation());
                job.setImage1(jobDetails.getImage1());
                job.setJob_date(jobDetails.getJob_date());
                job.setJob_time(jobDetails.getJob_time());
                job.setTotal_budget(jobDetails.getTotal_budget());
                job.setPrice_per_hr(jobDetails.getPrice_per_hr());
                job.setTotal_hrs(jobDetails.getTotal_hrs());
                job.setEst_tot_budget(jobDetails.getEst_tot_budget());
                job.setJob_status(jobDetails.getJob_status());
                job.setCompleted_by(jobDetails.getCompleted_by());
                job.setPosted_on(jobDetails.getPosted_on());
                job.setCompleted_on(jobDetails.getCompleted_on());
                //add the profile pic of the user who posted the job
                job.setProfile_pic(jobDetails.getProfile_pic());

                //add the name of the user who posted the job
                job.setUserName(jobDetails.getUserName());
                jobPoster = jobDetails.getUserName();
                Log.e(LOG_TAG, "Job details name is " + jobDetails.getName());
                displayDetails();
                //hideDialog();
            }else {
                hideDialog();
                Log.e(LOG_TAG, "Job details not retrieved");
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

    //initialise the view widgets
    private void setUpWidgets(){
        jobTitleTV = findViewById(R.id.JD_job_title);
        postedByTV = findViewById(R.id.JD_posted_by_content);
        timePostedTV = findViewById(R.id.JD_posted_by_time);
        locationTV = findViewById(R.id.JD_location_content);
        toBeDoneDateTV = findViewById(R.id.JD_to_be_done_date);
        toBeDoneTimeTV = findViewById(R.id.JD_to_be_done_time);
        jobPriceTV = findViewById(R.id.JD_job_price_content);
        jobDetailsET = findViewById(R.id.JD_job_details);
        makeOfferButton = findViewById(R.id.JD_make_offer);
        makeOfferButton.setOnClickListener(this);
    }

    //method to handle population of the views with the content
    private void displayDetails(){
        jobTitleTV.setText(job.getName());
        postedByTV.setText(job.getUserName());
        timePostedTV.setText(job.getPosted_on());
        locationTV.setText(job.getLocation());
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
        toBeDoneTimeTV.setText(job.getJob_time());
        jobPriceTV.setText("UGX." + job.getEst_tot_budget());
        //if the current user is the one that posted this job
        //give them the option of editing it
        /*if (userId == job.getPosted_by()) {
            //makeOfferButton.setBackgroundDrawable(null);
            makeOfferButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            //makeOfferButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            makeOfferButton.setText("You posted this job. Edit it");
        }*/
        jobDetailsET.setText(job.getDescription());
        hideDialog();
    }

    //method to handle clearing of the views with the content
    private void clearViews(){
        showDialog();
        jobTitleTV.setText("");
        postedByTV.setText("");
        timePostedTV.setText("");
        locationTV.setText("");
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
        toBeDoneTimeTV.setText("");
        jobPriceTV.setText("");
        jobDetailsET.setText("");
    }

    private void formatDate(){
        String jobDate = null;
        String postedOn = null;

        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.US);

        try{
            Date d = mysqlDateFormat.parse(job.getJob_date());
            jobDate = sdf.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            Date d = mysqlDateTimeFormat.parse(job.getPosted_on());
            postedOn = myFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        toBeDoneDateTV.setText(jobDate);
        timePostedTV.setText(postedOn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.JD_make_offer:
                //if the job poster is not the same as the fixer/jobber
                if (userId != job.getPosted_by()) {
                    //show the make offer dialog
                    showMakeOfferDialog();
                }
                showMakeOfferDialog();
                /**
                 * TODO
                 * check if the user has a valid phone number registered, profile pic,
                 * bank a/c(optional), billing address(optional)
                 * if cash is used then there should be a max number of transactions
                 * the user can make by cash before they have to pay the outstanding
                 * amount
                 */
                break;

        }
    }



    /*@Override
    public void onBackPressed(){
        clearViews();
    }*/

    //this code instantiates the offer dialog fragment and shows it
    public void showMakeOfferDialog(){
        //dialogFragment = new MakeOfferDialogFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("MakeOfferDialogFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = MakeOfferDialogFragment.newInstance(jobName, jobPoster);
        dialogFragment.show(getSupportFragmentManager(), "MakeOfferDialogFragment");

    }

            // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    //when the buttons on the dialog are clicked these are the methods called
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //dialogFragment.getDialog().cancel();
    }

    @Override
    public void returnOfferDetails(int amountOffered, String offerMessage) {
        postJobActivityViewModel.saveOffer(amountOffered,offerMessage, userId, job_id);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
