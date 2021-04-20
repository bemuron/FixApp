package com.emtech.fixr.presentation.ui.activity;

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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.helpers.CircleTransform;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.ui.fragment.MakeOfferDialogFragment;
import com.emtech.fixr.presentation.ui.fragment.NoticeDialogFragment;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PaymentActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PaymentViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

//import de.hdodenhof.circleimageview.CircleImageView;

public class OfferAcceptedDetailsForPosterActivity extends AppCompatActivity implements View.OnClickListener,
        FixAppRepository.OfferEditedListener, FixAppRepository.OfferSavedListener, NoticeDialogFragment.OnNoticeDialogListener
        {
    private static final String LOG_TAG = OfferAcceptedDetailsForPosterActivity.class.getSimpleName();
    private TextView jobTitleTV, offerByTV, offeredAmountTV, offerMsgTV,
            lastEditDateTv;
    private MaterialButton viewJobDetailsButton;
    private Button rejectOfferButton, callFixerButton, acceptOfferButton;
    private MyJobsActivityViewModel mViewModel;
    private PostJobActivityViewModel postJobActivityViewModel;
    private PaymentActivityViewModel paymentActivityViewModel;
    public static OfferAcceptedDetailsForPosterActivity offerAcceptedDetailsForPosterActivity;
    private Offer offer, offerDet;
    private ProgressBar pBar;
    private SessionManager session;
    private MakeOfferDialogFragment dialogFragment;
    private int userId, job_id, offer_id, editCount;
    private String userRole, jobName, jobPoster;
    private ImageView fixerImageView;
    private RelativeLayout afterAcceptOfferContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_accepted_details_for_poster);
        setupActionBar();
        offerAcceptedDetailsForPosterActivity = this;

        // Progress bar
        pBar = findViewById(R.id.forPoster_progress_bar);
        showBar();

        //get the data load details from the calling activity
        offer_id = getIntent().getIntExtra("offerID", 0);
        Log.e(LOG_TAG, "offer id received is "+ offer_id);
        jobName = getIntent().getStringExtra("jobName");

        PostJobViewModelFactory factory1 = InjectorUtils.providePostJobActivityViewModelFactory(this.getApplicationContext());
        postJobActivityViewModel = new ViewModelProvider
                (this, factory1).get(PostJobActivityViewModel.class);

        PaymentViewModelFactory factory2 = InjectorUtils.providePaymentActivityViewModelFactory(this.getApplicationContext());
        paymentActivityViewModel = new ViewModelProvider
                (this, factory2).get(PaymentActivityViewModel.class);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this.getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(MyJobsActivityViewModel.class);

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        //initialise the views
        setUpWidgets();

        mViewModel.getOfferDetailsForPoster(offer_id).observe(this, offerDetails -> {
            offerDet = offerDetails;
            if (offerDet != null) {
                offer = new Offer();
                offer.setOffered_by(offerDet.getOffered_by());
                offer.setJob_id(offerDet.getJob_id());
                offer.setOffer_amount(offerDet.getOffer_amount());
                offer.setMessage(offerDet.getMessage());
                offer.setLast_edited_on(offerDet.getLast_edited_on());
                offer.setSeen_by_poster(offerDet.getSeen_by_poster());
                offer.setEdit_count(offerDet.getEdit_count());
                offer.setOffer_accepted(offerDet.getOffer_accepted());
                offer.setName(offerDet.getName());
                offer.setEst_tot_budget(offerDet.getEst_tot_budget());
                offer.setFinal_job_cost(offerDet.getFinal_job_cost());
                offer.setPosted_by(offerDet.getPosted_by());
                offer.setProfile_pic(offerDet.getProfile_pic());
                offer.setUser_name(offerDet.getUser_name());
                offer.setPosted_on(offerDet.getPosted_on());
                offer.setJob_date(offerDet.getJob_date());
                offer.setJob_status(offerDet.getJob_status());
                offer.setJob_name(offerDet.getJob_name());

                Log.e(LOG_TAG, "Offer details name is " + offerDet.getName());
                //get the status of the job and launch the respective activity based on it
                getJobStatus();
                displayDetails();
            }else {
                hideBar();
                Log.e(LOG_TAG, "Offer details not retrieved");
            }

        });

        /* check if poster accepted this offer (status - 1)
         *  if accepted, check if job is in progress (jip - 5)
         *  if offer status = 1 and jip status = 5 launch JobInProgress activity
         * */
        if (isJobInProgress()){
            Intent intent2 = new Intent(this, JobInProgressActivity.class);
            intent2.putExtra("jobID", offer.getJob_id());
            intent2.putExtra("jobName", offer.getJob_name());
            intent2.putExtra("poster", offer.getPosted_by());
            intent2.putExtra("fixer", offer.getOffered_by());
            intent2.putExtra("postedOn", offer.getPosted_on());
            startActivity(intent2);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (isJobInProgress()){
                    Intent intent2 = new Intent(this, JobInProgressActivity.class);
                    intent2.putExtra("jobID", offer.getJob_id());
                    intent2.putExtra("jobName", offer.getJob_name());
                    intent2.putExtra("poster", offer.getPosted_by());
                    intent2.putExtra("fixer", offer.getOffered_by());
                    intent2.putExtra("postedOn", offer.getPosted_on());
                    startActivity(intent2);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        //clear the views if they had any data before
        clearViews();
    }

    public static OfferAcceptedDetailsForPosterActivity getInstance(){
        return offerAcceptedDetailsForPosterActivity;
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
        jobTitleTV = findViewById(R.id.forPoster_job_title);
        offerByTV = findViewById(R.id.forPoster_offer_by_name);
        offeredAmountTV = findViewById(R.id.forPoster_offer_amount);
        offerMsgTV = findViewById(R.id.forPoster_offer_details_message);
        lastEditDateTv = findViewById(R.id.forPoster_offer_lastEditDate);
        fixerImageView = findViewById(R.id.forPoster_fixerImageView);
        fixerImageView.setOnClickListener(this);
        acceptOfferButton = findViewById(R.id.forPoster_acceptOffer_button);
        acceptOfferButton.setOnClickListener(this);
        rejectOfferButton = findViewById(R.id.forPoster_rejectOffer_button);
        rejectOfferButton.setOnClickListener(this);
        callFixerButton = findViewById(R.id.forPoster_callFixer_button);
        callFixerButton.setOnClickListener(this);
        viewJobDetailsButton = findViewById(R.id.forPoster_viewJobDetails);
        viewJobDetailsButton.setOnClickListener(this);
        afterAcceptOfferContainer = findViewById(R.id.forPoster_afterAcceptOffer_container);
        //if the current user is the one that posted this job
        //hide the edit button
        //if (userId == offer.getPosted_by()) {
        //}
    }

    //check if poster accepted this offer (status - 1)
    //if accepted, check if job is in progress (jip - 5)
    //if offer status = 1 and jip status = 5 launch JobInProgress activity

     private boolean isJobInProgress(){
        return offer.getJob_status() == 5 && offer.getOffer_accepted() == 1;
    }

    //method to handle population of the views with the content
    private void displayDetails(){
        hideBar();
        jobTitleTV.setText(offer.getName());
        offerByTV.setText(offer.getUser_name());
        //toBeDoneDateTV.setText(job.getJob_date());
        formatDate();
        offeredAmountTV.setText("UGX." + offer.getFinal_job_cost());
        offerMsgTV.setText(offer.getMessage());
        editCount = offer.getEdit_count();

        //set user profile pic
        try {
            if (!TextUtils.isEmpty(offer.getProfile_pic())) {
                Glide.with(this)
                        .load("http://www.emtechint.com/fixapp/assets/images/profile_pics/" + offer.getProfile_pic())
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .apply(new RequestOptions().fitCenter()
                                .transform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL))
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

    //check the status of the job, if 5(in progress), go to job in progress activity
    //if 4(complete), check if payment has been made, if not, go to payment activity,
            //after payment activity go to rating activity
    private void getJobStatus(){
        //observes for the job status received
        paymentActivityViewModel.getJobStatusForPoster(offer.getJob_id(),
                OfferAcceptedDetailsForPosterActivity.getInstance()).observe(this, jobStatus ->{
            Log.e(LOG_TAG,"Job status "+jobStatus);
            switch (jobStatus){
                case 5:
                    Intent intent = new Intent(this, JobInProgressActivity.class);
                    intent.putExtra("jobID", offer.getJob_id());
                    intent.putExtra("jobName", jobName);
                    startActivity(intent);
                    break;
                case 4:
                    Intent intent2 = new Intent(this, PaymentActivity.class);
                    intent2.putExtra("job_id", offer.getJob_id());
                    intent2.putExtra("poster_id", offer.getPosted_by());
                    intent2.putExtra("fixer_id", offer.getOffered_by());
                    intent2.putExtra("job_cost", offer.getOffer_amount());
                    intent2.putExtra("jobName", jobName);
                    startActivity(intent2);
            }
        });
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
        if (isOfferSaved){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

    }



    //called in GetMyJobs to get the response after a job is accepted
    public void updateUiAfterPosterAcceptOffer(Boolean isOfferAccepted, String message){
        hideBar();
        if (isOfferAccepted){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    //called in GetMyJobs to get the response after a job is accepted
    public void updateUiAfterPosterRejectOffer(Boolean isOfferRejected, String message){
        hideBar();
        if (isOfferRejected) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

            /**
             * After accepting the offer, the fixer gets a notification that the offer was accepted
             * the fixer can go ahead to start the job, this shows "job in progress". Whe the fixer
             * is done they can complete the job, and the poster can confirm if it is finished, if it
             * isn't yet complete they can reject and the job status goes back to "job in progress".
             * If the poster confirms that the job is complete, they can go ahead and make the
             * payment to the fixer via mm, visa, cash
             * @param menu
             * @return
             */

            @Override
            public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.accepted_offer_menu, menu);
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
                    //confirm delete
                    showNoticeDialog();
                } else if (id == R.id.action_view_details){
                    Intent intent = new Intent(this, JobDetailsActivity.class);
                    intent.putExtra("jobID", offer.getJob_id());
                    intent.putExtra("jobName", jobName);
                    startActivity(intent);
                }

                return super.onOptionsItemSelected(item);
            }

            //show dialog to confirm with user if they really want to delete the accepted offer
            private void showNoticeDialog(){
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("posterDeleteAcceptedOffer");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                NoticeDialogFragment dialogFragment = NoticeDialogFragment.newInstance("Delete this offer?");
                dialogFragment.show(getSupportFragmentManager(), "posterDeleteAcceptedOffer");
            }

            @Override
            public void onDialogPositiveClick(DialogFragment dialog) {
                //delete offer
                mViewModel.posterRejectOffer(offer_id, offer.getJob_id());
            }

            @Override
            public void onDialogNegativeClick(DialogFragment dialog) {

            }
        }
