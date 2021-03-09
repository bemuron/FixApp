package com.emtech.fixr.presentation.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.RateFixerFragment;
import com.emtech.fixr.presentation.ui.fragment.RatePosterFragment;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

public class RatingActivity extends AppCompatActivity implements
        RateFixerFragment.OnRateFixerInteractionListener, RatePosterFragment.OnRatePosterListener {
    private static final String TAG = RatingActivity.class.getSimpleName();
    private SessionManager session;
    private String userRole, fixerProfPicName, posterProfPicName, posterName, fixerName;
    private int userId, job_id, poster_id, fixer_id;
    public static RatingActivity ratingActivity;
    private MyJobsActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingActivity = this;

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        //get intent from which this activity is called and the id of the job
        job_id = getIntent().getIntExtra("job_id", 0);
        //get the id of the poster
        poster_id = getIntent().getIntExtra("poster_id", 0);

        //get the id of the fixer
        fixer_id = getIntent().getIntExtra("fixer_id", 0);

        fixerProfPicName = getIntent().getStringExtra("fixer_prof_pic");
        posterProfPicName = getIntent().getStringExtra("poster_prof_pic");

        fixerName = getIntent().getStringExtra("fixer_name");
        posterName = getIntent().getStringExtra("poster_name");

        Log.e(TAG,"fixer_prof_pic is "+ fixerProfPicName +" and poster_prof_pic is "+posterProfPicName);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, factory).get(MyJobsActivityViewModel.class);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.rating_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }
        //show the right fragment based on the user role
        setUpRespectiveFragment(userRole);

    }//close onCreate

    public static RatingActivity getInstance(){
        return ratingActivity;
    }

    //show the respective fragment based on the role of the user
    private void setUpRespectiveFragment(String role){
        if (userId == poster_id) {
            //if the current user of the app is a poster then the userId passed here is for the user/job poster
            RateFixerFragment rateFixerFragment = RateFixerFragment.newInstance(job_id, userId, fixerProfPicName, fixerName);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rating_fragment_container, rateFixerFragment)
                    .commit();

        }else if (userId == fixer_id){
            //if the current user of the app is a fixer then the userId passed here is for the fixer
            RatePosterFragment ratePosterFragment = RatePosterFragment.newInstance(job_id, userId, posterProfPicName, posterName);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rating_fragment_container, ratePosterFragment)
                    .commit();
        }
    }

    //callback from rate fixer fragment
    //this submits the rating to the db
    @Override
    public void onRateFixerInteraction(int job_id, int poster_id, int fixer_id, float fixer_rating, String comment) {
        //submit the fixer's ration
        mViewModel.submitFixerRating(job_id, poster_id, fixer_id, fixer_rating, comment);
    }

    //callback from rate poster fragment
    @Override
    public void onRatePosterInteraction(int job_id, int fixer_id, int poster_id, float posterRating, String comment) {
        mViewModel.submitPosterRating(job_id, fixer_id, poster_id, posterRating, comment);
    }

    //called in GetMyJobs to get the response after the fixer rating is submitted
    //when this is called, the poster has successfully rated the fixer
    //start home activity clearing the back stack
    public void updateUiAfterFixerRating(Boolean isFixerRated, String message){
        if (isFixerRated) {
            Intent intent = new Intent(RatingActivity.this, HomeActivity.class );
            // Closing all the Activities
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            Toast.makeText(this, "Rating Submitted", Toast.LENGTH_SHORT).show();
            this.finish();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //called in GetMyJobs to get the response after the fixer rating is submitted
    //when this is called, the fixer has successfully rated the poster
    //start home activity clearing the back stack
    public void updateUiAfterPosterRating(Boolean isPosterRated, String message){
        if (isPosterRated) {
            Intent intent = new Intent(RatingActivity.this, HomeActivity.class );
            // Closing all the Activities
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            Toast.makeText(this, "Rating Submitted", Toast.LENGTH_SHORT).show();
            this.finish();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
