package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.emtech.fixr.R;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OfferReceivedDetailsForPosterActivity extends AppCompatActivity
        implements View.OnClickListener, NoticeDialogFragment.OnNoticeDialogListener{
    private static final String LOG_TAG = OfferReceivedDetailsForPosterActivity.class.getSimpleName();
    private TextView jobTitleTV, offerByTV, offeredAmountTV, offerMsgTV,
            lastEditDateTv;
    private MaterialButton viewJobDetailsButton;
    private Button rejectOfferButton, callFixerButton, acceptOfferButton;
    private MyJobsActivityViewModel mViewModel;
    private PostJobActivityViewModel postJobActivityViewModel;
    public static OfferReceivedDetailsForPosterActivity offerReceivedDetailsForPosterActivity;
    private Offer offer;
    private ProgressBar pBar;
    private SessionManager session;
    private int userId, job_id, offer_id, editCount;
    private String userRole, jobName, jobPoster;
    private ImageView fixerImageView;
    private RelativeLayout afterAcceptOfferContainer;
    private NoticeDialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_received_details_for_poster);

        setupActionBar();
        offerReceivedDetailsForPosterActivity = this;

        // Progress bar
        pBar = findViewById(R.id.forPosterRec_progress_bar);
        showBar();

        PostJobViewModelFactory factory1 = InjectorUtils.providePostJobActivityViewModelFactory(this.getApplicationContext());
        postJobActivityViewModel = new ViewModelProvider
                (this, factory1).get(PostJobActivityViewModel.class);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this.getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(MyJobsActivityViewModel.class);

        //get the data load details from the calling activity
        offer_id = getIntent().getIntExtra("offerID", 0);
        Log.e(LOG_TAG, "offer id received is "+ offer_id);
        jobName = getIntent().getStringExtra("jobName");

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        //initialise the views
        setUpWidgets();
        //clear the views if they had eny data before
        clearViews();

        mViewModel.getOfferDetailsForPoster(offer_id).observe(this, offerDetails -> {

            if (offerDetails != null) {
                offer = new Offer();
                offer.setOffered_by(offerDetails.getOffered_by());
                offer.setJob_id(offerDetails.getJob_id());
                offer.setOffer_amount(offerDetails.getOffer_amount());
                offer.setMessage(offerDetails.getMessage());
                offer.setLast_edited_on(offerDetails.getLast_edited_on());
                offer.setSeen_by_poster(offerDetails.getSeen_by_poster());
                offer.setEdit_count(offerDetails.getEdit_count());
                offer.setOffer_accepted(offerDetails.getOffer_accepted());
                offer.setName(offerDetails.getName());
                offer.setEst_tot_budget(offerDetails.getEst_tot_budget());
                offer.setPosted_by(offerDetails.getPosted_by());
                offer.setUser_name(offerDetails.getUser_name());
                offer.setProfile_pic(offerDetails.getProfile_pic());
                offer.setPosted_on(offerDetails.getPosted_on());
                offer.setJob_date(offerDetails.getJob_date());

                Log.e(LOG_TAG, "Offer details name is " + offerDetails.getName());
                displayDetails();
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
        clearViews();
    }

    public static OfferReceivedDetailsForPosterActivity getInstance(){
        return offerReceivedDetailsForPosterActivity;
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
        jobTitleTV = findViewById(R.id.forPosterRec_job_title);
        offerByTV = findViewById(R.id.forPosterRec_offer_by_name);
        offeredAmountTV = findViewById(R.id.forPosterRec_offer_amount);
        offerMsgTV = findViewById(R.id.forPosterRec_offer_details_message);
        lastEditDateTv = findViewById(R.id.forPosterRec_offer_lastEditDate);
        fixerImageView = findViewById(R.id.forPosterRec_fixerImageView);
        fixerImageView.setOnClickListener(this);
        acceptOfferButton = findViewById(R.id.forPosterRec_acceptOffer_button);
        acceptOfferButton.setOnClickListener(this);
        rejectOfferButton = findViewById(R.id.forPosterRec_rejectOffer_button);
        rejectOfferButton.setOnClickListener(this);
        callFixerButton = findViewById(R.id.forPosterRec_callFixer_button);
        callFixerButton.setOnClickListener(this);
        viewJobDetailsButton = findViewById(R.id.forPosterRec_viewJobDetails);
        viewJobDetailsButton.setOnClickListener(this);
        afterAcceptOfferContainer = findViewById(R.id.forPosterRec_afterAcceptOffer_container);
        //if the current user is the one that posted this job
        //hide the edit button
        //if (userId == offer.getPosted_by()) {
        //}
    }

    //method to handle population of the views with the content
    private void displayDetails(){
        hideBar();
        jobTitleTV.setText(offer.getName());
        offerByTV.setText(offer.getUser_name());
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
        offeredAmountTV.setText("UGX." + offer.getOffer_amount());
        offerMsgTV.setText(offer.getMessage());
        editCount = offer.getEdit_count();

        //set user profile pic
        try {
            if (!TextUtils.isEmpty(offer.getProfile_pic())) {
                Glide.with(this)
                        .load("http://www.emtechint.com/fixapp/assets/images/profile_pics/" + offer.getProfile_pic())
                        .thumbnail(0.5f)
                        .into(fixerImageView);
                //fixerImageView.setColorFilter(null);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }

        //if it is the first time the poster is seeing this offer,update the seen status
        //to 1 - seen by poster
        if (offer.getSeen_by_poster() == 0){
            mViewModel.updateOfferSeenByPosterStatus(offer_id);
        }

        hideBar();
    }

    //method to handle clearing of the views with the content
    private void clearViews(){
        showBar();
        jobTitleTV.setText("");
        offerByTV.setText("");
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
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
            Date date = mysqlDateTimeFormat.parse(offer.getLast_edited_on());
            lastEditedOn = myFormat.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }

        //toBeDoneDateTV.setText(jobDate);
        //timePostedTV.setText(postedOn);
        lastEditDateTv.setText(lastEditedOn);
    }

    /**
     * TODO
     *create a menu list which has items like view job details, reject offer, call
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.forPoster_acceptOffer_button:
                showBar();
                mViewModel.posterAcceptOffer(offer_id, offer.getJob_id());
                /**
                 * TODO
                 * Implement what happens when poster accepts offer
                 * This button is disabled and the afterAcceptOffer container is made visible
                 */
                break;

            case R.id.forPoster_rejectOffer_button:
                showBar();
                mViewModel.posterRejectOffer(offer_id, offer.getJob_id());
                /**
                 * TODO
                 * Implement what happens when poster rejects offer
                 * Accept offer button should be reactivated
                 */
                break;

            case R.id.forPoster_callFixer_button:
                /**
                 * TODO
                 * call the dialer to initiate call to the fixer
                 */
                break;

            case R.id.forPoster_viewJobDetails:
                Intent intent = new Intent(this, JobDetailsActivity.class);
                intent.putExtra("jobID", offer.getJob_id());
                intent.putExtra("jobName", jobName);
                startActivity(intent);
                break;
        }
    }

    /*@Override
    public void onBackPressed(){
        clearViews();
    }*/

    private void showBar() {
        pBar.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        pBar.setVisibility(View.INVISIBLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /*@Override
    public void onOfferEdited(Boolean isOfferPosted, String message) {
        if (isOfferPosted){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onOfferSaved(Boolean isOfferSaved, String message) {
        if (isOfferSaved){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

    }*/

    //called in GetMyJobs to get the response after a job is accepted
    public void updateUiAfterPosterAcceptOffer(Boolean isOfferAccepted, String message){
        hideBar();
        if (isOfferAccepted){
            //after accepting, the user is also taken to the accepted offer details activity
            Toast.makeText(this, "Offer accepted", Toast.LENGTH_LONG).show();
            afterAcceptOfferNotice();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    //called in GetMyJobs to get the response after a job is accepted
    public void updateUiAfterPosterRejectOffer(Boolean isOfferRejected, String message){
        hideBar();
        if (isOfferRejected) {
            Toast.makeText(this, "Offer deleted", Toast.LENGTH_SHORT).show();
            this.finish();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.offer_received_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_accept_offer){
            //ask the user of they are sure about accepting this offer
            acceptOfferNotice();

        } else if (id == R.id.action_delete_offer){
            //ask the user if they are sure about deleting this offer
            showNoticeDialog();

        } else if (id == R.id.action_view_details){
            //view job details
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
        Fragment prev = getSupportFragmentManager().findFragmentByTag("posterDeleteOffer");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment = NoticeDialogFragment.newInstance("Delete this offer?");
        dialogFragment.show(getSupportFragmentManager(), "posterDeleteOffer");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        showBar();

        Log.e(LOG_TAG, "Yes clicked");
        //delete offer
        mViewModel.posterRejectOffer(offer_id, offer.getJob_id());
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //user canceled event
        //dialogFragment.dismiss();
    }

    //alert dialog to confirm if user wants to accept the offer
    public void acceptOfferNotice(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setMessage("Accept this offer?");
        alertDialog.setPositiveButton(R.string.notice_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showBar();
                //call method to update offer status to accepted at the be
                mViewModel.posterAcceptOffer(offer_id, offer.getJob_id());
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    //alert dialog to inform user after accepting offer
    public void afterAcceptOfferNotice(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Offer Accepted");
        alertDialog.setMessage("You can find your accepted offer in the accepted offers list " +
                "and go ahead to call the fixer");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(OfferReceivedDetailsForPosterActivity.this,
                        OfferAcceptedDetailsForPosterActivity.class);
                intent.putExtra("offerID", offer.getOffer_id());
                intent.putExtra("jobName", jobName);
                startActivity(intent);

                offerReceivedDetailsForPosterActivity.finish();
            }
        });
        alertDialog.show();
    }
}
