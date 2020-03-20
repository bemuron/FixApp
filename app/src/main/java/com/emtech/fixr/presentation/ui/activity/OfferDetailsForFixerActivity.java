package com.emtech.fixr.presentation.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OfferDetailsForFixerActivity extends AppCompatActivity implements View.OnClickListener,
        MakeOfferDialogFragment.MakeOfferDialogListener, FixAppRepository.OfferEditedListener,
        FixAppRepository.OfferSavedListener
        {
    private static final String LOG_TAG = OfferDetailsForFixerActivity.class.getSimpleName();
    private TextView jobTitleTV, postedByTV, timePostedTV, offerStatusTV,
            toBeDoneDateTV, toBeDoneTimeTV, offeredAmountTV, offerMsgTV,
            lastEditDateTv;
    private MaterialButton viewJobDetailsButton;
    private Button editOfferButton, rejectJobButton, callPosterButton;
    private MyJobsActivityViewModel mViewModel;
    private PostJobActivityViewModel postJobActivityViewModel;
    private Offer offer;
    private ProgressBar pBar;
    private SessionManager session;
    private MakeOfferDialogFragment dialogFragment;
    private int userId, job_id, offer_id, editCount;
    private String userRole, jobName, jobPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details_for_fixer);
        setupActionBar();

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        // Progress bar
        pBar = findViewById(R.id.forFixer_progress_bar);
        hideBar();

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
            clearViews();

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
                offer.setPosted_on(offerDetails.getPosted_on());
                offer.setJob_date(offerDetails.getJob_date());

                Log.e(LOG_TAG, "Offer details name is " + offerDetails.getName());
                displayDetails();
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
        //if the current user is the one that posted this job
        //hide the edit button
        //if (userId == offer.getPosted_by()) {
            //makeOfferButton.setBackgroundDrawable(null);
        //}
        editOfferButton = findViewById(R.id.forFixer_editOffer_button);
        editOfferButton.setOnClickListener(this);
    }

    //method to handle population of the views with the content
    private void displayDetails(){
        jobTitleTV.setText(offer.getName());
        //timePostedTV.setText(offer.getPosted_on());
        if(offer.getSeen_by_poster() == 0) {
            offerStatusTV.setText("Not yet seen by poster");
        }else if(offer.getSeen_by_poster() == 1){
            offerStatusTV.setText("Seen by poster");
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
        showBar();
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
            case R.id.forFixer_editOffer_button:
                //if the job poster is not the same as the fixer/jobber
                if (userId != offer.getPosted_by()) {
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

            case R.id.forFixer_rejectOffer_button:
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
                /**
                 * TODO
                 * Take the poster to the details of the job he posted
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
        Fragment prev = getSupportFragmentManager().findFragmentByTag("EditOfferDialogFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = MakeOfferDialogFragment.newInstance(jobName, offer.getOffer_amount(),
                offer.getMessage(),"editOffer");
        dialogFragment.show(getSupportFragmentManager(), "EditOfferDialogFragment");

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
        postJobActivityViewModel.editOffer(offer_id, amountOffered,offerMessage, editCount++);
    }

    private void showBar() {
        pBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        pBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        }
