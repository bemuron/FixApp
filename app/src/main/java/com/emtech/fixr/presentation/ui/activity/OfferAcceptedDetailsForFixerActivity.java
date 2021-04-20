package com.emtech.fixr.presentation.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.emtech.fixr.R;
import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.ui.fragment.MakeOfferDialogFragment;
import com.emtech.fixr.presentation.ui.fragment.NoticeDialogFragment;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OfferAcceptedDetailsForFixerActivity extends AppCompatActivity implements View.OnClickListener,
        NoticeDialogFragment.OnNoticeDialogListener, FixAppRepository.OfferEditedListener,
        FixAppRepository.OfferSavedListener
        {
    private static final String LOG_TAG = OfferAcceptedDetailsForFixerActivity.class.getSimpleName();
    public static OfferAcceptedDetailsForFixerActivity offerAcceptedDetailsForFixerActivity;
    private TextView jobTitleTV, postedByTV, timePostedTV, offerStatusTV,
            toBeDoneDateTV, toBeDoneTimeTV, offeredAmountTV, offerMsgTV,
            lastEditDateTv;
    private MaterialButton viewJobDetailsButton, startJobButton;
    private Button rejectJobButton, callPosterButton;
    private MyJobsActivityViewModel mViewModel;
    private PostJobActivityViewModel postJobActivityViewModel;
    private Offer mOfferDetails;
    private Offer offer;
    private ProgressBar pBar;
    private SessionManager session;
    private MakeOfferDialogFragment dialogFragment;
    private int userId, job_id, offer_id, editCount;
    private String userRole, jobName, jobPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_accepted_details_for_fixer);
        setupActionBar();

        offerAcceptedDetailsForFixerActivity = this;

        // Progress bar
        pBar = findViewById(R.id.forFixer_progress_bar);
        showBar();

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        if (mOfferDetails != null){
            mOfferDetails = null;
        }

        offer_id = getIntent().getIntExtra("offerID", 0);
        jobName = getIntent().getStringExtra("jobName");

        //initialise the views
        setUpWidgets();

        PostJobViewModelFactory factory1 = InjectorUtils.providePostJobActivityViewModelFactory(this.getApplicationContext());
        postJobActivityViewModel = new ViewModelProvider
                (this, factory1).get(PostJobActivityViewModel.class);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this.getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(MyJobsActivityViewModel.class);

        mViewModel.getOfferDetailsForFixer(offer_id).observe(this, offerDetails -> {
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
                offer.setUser_name(mOfferDetails.getUser_name());
                offer.setEst_tot_budget(mOfferDetails.getEst_tot_budget());
                offer.setPosted_by(mOfferDetails.getPosted_by());
                offer.setPosted_on(mOfferDetails.getPosted_on());
                offer.setJob_date(mOfferDetails.getJob_date());

                Log.e(LOG_TAG, "Offer details name is " + mOfferDetails.getName());
                displayDetails();
                //hideDialog();
            }else {
                hideBar();
                Log.e(LOG_TAG, "Offer details not retrieved");
            }

        });
    }

    @Override
    public void onResume(){
        super.onResume();
        showBar();
    }

    @Override
    public void onPause(){
        super.onPause();
        //clear the views if they had any data before
        clearViews();
    }

    public OfferAcceptedDetailsForFixerActivity getInstance(){
        return offerAcceptedDetailsForFixerActivity;
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
        jobTitleTV = findViewById(R.id.forFixer_job_title);
        postedByTV = findViewById(R.id.forFixer_job_postedBy);
        offerStatusTV = findViewById(R.id.forFixer_offer_status);
        offeredAmountTV = findViewById(R.id.forFixer_offer_amount_content);
        offerMsgTV = findViewById(R.id.forFixer_offer_details_message);
        lastEditDateTv = findViewById(R.id.forFixer_offer_last_edit_date);
        startJobButton = findViewById(R.id.forFixer_startJob);
        startJobButton.setOnClickListener(this);
        viewJobDetailsButton = findViewById(R.id.forFixer_viewJobDetails);
        viewJobDetailsButton.setOnClickListener(this);
        //if the current user is the one that posted this job
        //hide the edit button
        //if (userId == offer.getPosted_by()) {
            //makeOfferButton.setBackgroundDrawable(null);
        //}
    }

    //method to handle population of the views with the content
    private void displayDetails(){
        jobTitleTV.setText(offer.getName());
        postedByTV.setText(offer.getUser_name());
        //timePostedTV.setText(offer.getPosted_on());
        if(offer.getSeen_by_poster() == 0) {
            offerStatusTV.setText("Not yet seen by poster");
            offerStatusTV.setTextColor(getResources().getColor(R.color.draft_job));
        }else if(offer.getSeen_by_poster() == 1){
            offerStatusTV.setText("Seen by poster");
            offerStatusTV.setTextColor(getResources().getColor(R.color.completed_job));
        }
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
        //toBeDoneTimeTV.setText(offer.getJob_date());
        offeredAmountTV.setText("UGX." + offer.getOffer_amount());
        offerMsgTV.setText(offer.getMessage());
        editCount = offer.getEdit_count();
        hideBar();
    }

    //method to handle clearing of the views with the content
    private void clearViews(){
        jobTitleTV.setText("");
        postedByTV.setText("");
        //timePostedTV.setText("");
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
        //toBeDoneTimeTV.setText("");
        offeredAmountTV.setText("");
        offerMsgTV.setText("");
    }

    private void formatDate(){
        String jobDate = null;
        String postedOn = null;
        String lastEditedOn = null;

        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.US);

        try{
            Date d = mysqlDateFormat.parse(offer.getJob_date());
            jobDate = sdf.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            Date d = mysqlDateTimeFormat.parse(offer.getPosted_on());
            postedOn = myFormat.format(d);

            Date date = mysqlDateTimeFormat.parse(offer.getLast_edited_on());
            lastEditedOn = myFormat.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }

        //toBeDoneDateTV.setText(jobDate);
        //timePostedTV.setText(postedOn);
        lastEditDateTv.setText(lastEditedOn);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.forFixer_rejectOffer_button:
                mViewModel.fixerRejectOffer(offer_id, offer.getJob_id());
                /**
                 * TODO
                 * Implement what happens when poster rejects offer
                 * Accept offer button should be reactivated
                 */
                break;

            case R.id.forFixer_callPoster_button:
                /**
                 * TODO
                 * call the dialer to initiate call to the poster
                 */
                break;

            case R.id.forFixer_viewJobDetails:
                Intent intent = new Intent(this, JobDetailsActivity.class);
                intent.putExtra("jobID", offer.getJob_id());
                intent.putExtra("jobName", jobName);
                startActivity(intent);
                break;

            //launch activity to show job is in progress
            case R.id.forFixer_startJob:
                //change job status in db to in progress
                //launch the JIP activity
                updateJobStatus(offer_id, offer.getJob_id());
                break;
        }
    }

    /*@Override
    public void onBackPressed(){
        clearViews();
    }*/

            // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    //when the buttons on the dialog are clicked these are the methods called
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        showBar();
        // User touched the dialog's positive button
        //delete offer
        mViewModel.fixerRejectOffer(offer_id, offer.getJob_id());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        //dialogFragment.getDialog().cancel();
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

    @Override
    public void onOfferEdited(Boolean isOfferPosted, String message) {
        if (isOfferPosted){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOfferSaved(Boolean isOfferSaved, String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

     //called in GetMyJobs to get the response after a job is rejected
     public void updateUiAfterFixerRejectOffer(Boolean isOfferRejected, String message){
        hideBar();
        if (isOfferRejected) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //called in GetMyJobs to start the job, set the status to 5
    public void jobStartedResponse(Boolean isJobStarted, String message){
        if (isJobStarted){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            Intent intent2 = new Intent(this, JobInProgressActivity.class);
            intent2.putExtra("jobID", offer.getJob_id());
            intent2.putExtra("offerID", offer_id);
            intent2.putExtra("jobName", jobName);
            startActivity(intent2);
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //update job status in the db if fixer starts the job to 4 - in progress
    private void updateJobStatus(int offerId, int jobId){
        mViewModel.fixerStartJob(offerId, jobId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fixer_accepted_offer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_call_fixer){
            //launch call app with number of fixer to call

        } else if (id == R.id.action_delete_offer){
            //confirm if they want to delete
            showNoticeDialog();
        } else if (id == R.id.action_view_details){
            Intent intent = new Intent(this, JobDetailsActivity.class);
            intent.putExtra("jobID", offer.getJob_id());
            intent.putExtra("jobName", jobName);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

            //show dialog to confirm with user if they really want to delete the offer
            private void showNoticeDialog(){
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("fixerDeleteAcceptedOffer");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                NoticeDialogFragment dialogFragment = NoticeDialogFragment.newInstance("Delete this offer?");
                dialogFragment.show(getSupportFragmentManager(), "fixerDeleteAcceptedOffer");
            }

            //method to check for internet connection
            public static boolean isNetworkAvailable(Context context) {
                if(context == null)  return false;

                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager != null) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                        if (capabilities != null) {
                            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                                return true;
                            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return true;
                            }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                                return true;
                            }
                        }
                    }

                    else {

                        try {
                            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                                Log.i("update_status", "Network is available : true");
                                return true;
                            }
                        } catch (Exception e) {
                            Log.i("update_status", "" + e.getMessage());
                        }
                    }
                }
                Log.i("update_status","Network is available : FALSE ");
                return false;
            }
}
