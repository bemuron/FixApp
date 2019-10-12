package com.emtech.fixr.presentation.ui.activity;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.PostJobFragment;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PostJobActivity extends AppCompatActivity implements PostJobFragment.OnPostButtonListener, PostFixAppJob.JobPostedCallBack {
    private static final String LOG_TAG = PostJobActivity.class.getSimpleName();
    private PostJobActivityViewModel postJobActivityViewModel;
    private SessionManager session;
    private ProgressDialog pDialog;
    private ScrollView layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;
    public static PostJobActivity postJobActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        postJobActivity = this;

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
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        Log.e(LOG_TAG, "close sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        Log.e(LOG_TAG, "Expand sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

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

    //set up the tutor list fragment
    private void setupViewFragment(PostJobFragment postJobFragment) {

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                //.addToBackStack(TUTOR_LIST_FRAGMENT_TAG )
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


    //post job fragment callback
    @Override
    public void jobPostDataCallback(int userId, String jobTitle, String jobDesc,
                                    File file, int categoryId, PostJobActivity postJobActivityInstance) {
        pDialog.setMessage("Posting job details ...");
        showDialog();
        //call the viewmodel method to send the job to the server
        Log.d(LOG_TAG, "Passing job details to view model");
        postJobActivityViewModel.postJob(userId, jobTitle, jobDesc,file, categoryId, postJobActivityInstance);

    }

    @Override
    public void onJobPosted(Boolean isJobPosted, String message) {
        if (isJobPosted){
            hideDialog();
            Toast.makeText(postJobActivity, message, Toast.LENGTH_SHORT).show();
            //go back to home activity
            Intent intent = new Intent(PostJobActivity.this, HomeActivity.class);
            startActivity(intent);
            this.finish();
        }else{
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
}
