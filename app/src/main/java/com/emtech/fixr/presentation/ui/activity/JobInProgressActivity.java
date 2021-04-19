package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JobInProgressActivity extends AppCompatActivity {
    private static final String LOG_TAG = JobInProgressActivity.class.getSimpleName();
    public static JobInProgressActivity jobInProgressActivity;
    private MaterialButton finishJobBtn;
    private MyJobsActivityViewModel mViewModel;
    private int offer_id, jobId, userId;
    private String jobName, userRole;
    private SessionManager session;
    private ProgressBar pBar;
    private Offer offer,mOfferDetails;
    private TextView jobTitleTV, postedByTV, jobStartDate,
            jipCostTV, fixerTv, jobActualStartDate, jobActualStartDateTitle;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_in_progress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        jobInProgressActivity = this;

        // Progress bar
        pBar = findViewById(R.id.jip_progress_bar);
        showBar();

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        if (mOfferDetails != null){
            mOfferDetails = null;
        }

        offer_id = getIntent().getIntExtra("offerID", 0);
        jobId = getIntent().getIntExtra("jobID",0);
        jobName = getIntent().getStringExtra("jobName");

        //initialise the views
        setUpWidgets();

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this.getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(MyJobsActivityViewModel.class);

        mViewModel.getJIPDetails(offer_id).observe(this, offerDetails -> {
            //clearViews();
            mOfferDetails = offerDetails;

            if (mOfferDetails != null) {
                offer = new Offer();
                offer.setOffered_by(mOfferDetails.getOffered_by());
                offer.setJob_id(mOfferDetails.getJob_id());
                offer.setOffer_amount(mOfferDetails.getOffer_amount());
                offer.setMessage(mOfferDetails.getMessage());
                offer.setLast_edited_on(mOfferDetails.getLast_edited_on());
                offer.setSeen_by_poster(mOfferDetails.getSeen_by_poster());
                offer.setEdit_count(mOfferDetails.getEdit_count());
                offer.setOffer_accepted(mOfferDetails.getOffer_accepted());
                offer.setName(mOfferDetails.getName());
                offer.setPoster_user_name(mOfferDetails.getPoster_user_name());
                offer.setFixer_user_name(mOfferDetails.getFixer_user_name());
                offer.setFixer_profile_pic(mOfferDetails.getFixer_profile_pic());
                offer.setPoster_profile_pic(mOfferDetails.getPoster_profile_pic());
                offer.setEst_tot_budget(mOfferDetails.getEst_tot_budget());
                offer.setEst_tot_budget(mOfferDetails.getFinal_job_cost());
                offer.setPosted_by(mOfferDetails.getPosted_by());
                offer.setPosted_on(mOfferDetails.getPosted_on());
                offer.setJob_date(mOfferDetails.getJob_date());

                Log.e(LOG_TAG, "Offer details name is " + mOfferDetails.getName());
                displayJipDetails();
                //hideDialog();
            }else {
                hideBar();
                Log.e(LOG_TAG, "Offer details not retrieved");
            }

        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            //remove app name from toolbar
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public JobInProgressActivity getInstance(){
        return jobInProgressActivity;
    }

    //initialise the view widgets
    private void setUpWidgets(){
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        jobTitleTV = findViewById(R.id.jip_job_title);
        postedByTV = findViewById(R.id.jip_poster);
        jobStartDate = findViewById(R.id.jip_scheduled_start_date);
        jobActualStartDate = findViewById(R.id.jip_actual_start_date);
        jobActualStartDateTitle = findViewById(R.id.jip_actual_start_date_title);
        jipCostTV = findViewById(R.id.jip_amount_content);
        fixerTv = findViewById(R.id.jip_fixer);
        finishJobBtn = findViewById(R.id.jip_finishJob);
        finishJobBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBar();
                //change job status in db to 5 - completed
                mViewModel.fixerFinishJob(offer_id, jobId);
            }
        });
    }

    //method to handle population of the views with the content
    private void displayJipDetails(){
        collapsingToolbar.setTitle(offer.getName());
        jobTitleTV.setText(offer.getName());
        postedByTV.setText(offer.getPoster_user_name());
        fixerTv.setText(offer.getFixer_user_name());
        formatDate();
        //toBeDoneTimeTV.setText(offer.getJob_date());
        jipCostTV.setText("UGX." + offer.getFinal_job_cost());
        //offerMsgTV.setText(offer.getMessage());
        //editCount = offer.getEdit_count();
        hideBar();
    }

    private void formatDate(){
        String jobDate = null, todayDate = null;

        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.US);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        try{
            Date d = mysqlDateFormat.parse(offer.getJob_date());
            jobDate = sdf.format(d);

            todayDate = dateFormat.format(c.getTime());
        }catch (Exception e){
            e.printStackTrace();
        }

        jobStartDate.setText(jobDate);
        jobActualStartDate.setText(todayDate);
    }

    //called in GetMyJobs to provide the response to the finish job action
    public void jobFinishedResponse(Boolean isJobFinished, String message){
        hideBar();
        if(isJobFinished){
            Intent intent = new Intent(JobInProgressActivity.this, PaymentActivity.class);
            intent.putExtra("job_id", offer.getJob_id());
            intent.putExtra("poster_id", offer.getPosted_by());
            intent.putExtra("fixer_id", offer.getOffered_by());
            intent.putExtra("job_cost", offer.getFinal_job_cost());
            intent.putExtra("fixer_prof_pic", offer.getFixer_profile_pic());
            intent.putExtra("poster_prof_pic", offer.getPoster_profile_pic());
            intent.putExtra("fixer_name", offer.getFixer_user_name());
            intent.putExtra("poster_name", offer.getPoster_user_name());
            intent.putExtra("offer_id", offer_id);
            intent.putExtra("jobName", jobName);
            startActivity(intent);
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showBar() {
        pBar.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        pBar.setVisibility(View.INVISIBLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}