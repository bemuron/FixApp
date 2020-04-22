package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.adapters.MyJobsListAdapter;
import com.emtech.fixr.presentation.adapters.OffersListAdapter;
import com.emtech.fixr.presentation.ui.fragment.MyJobsFragment;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class MyJobsListActivity extends AppCompatActivity implements MyJobsListAdapter.MyJobsListAdapterListener,
        AdapterView.OnItemSelectedListener{
    private static final String LOG_TAG = MyJobsListActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private List<Job> jobList = new ArrayList<Job>();
    private MyJobsActivityViewModel mViewModel;
    private Job job;
    private MyJobsListAdapter jobsAdapter;
    private int mUserId;
    private String mUserRole;
    private TextView emptyView;
    private Spinner jobsFilterSpinner;
    private int statusJobDisplay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_jobs_list);
        setupActionBar();

        mUserId = getIntent().getIntExtra("userId", 0);
        String user_role = getIntent().getStringExtra("userRole");

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, factory).get(MyJobsActivityViewModel.class);

        getAllWidgets();
        setAdapter();
        setSpinnerAdapter();

        mViewModel.getAllJobsForUser(mUserId).observe(this, userJobsList -> {
            jobList = userJobsList;
            jobsAdapter.setList(userJobsList);
            Log.e(LOG_TAG, "Jobs list size is " + jobList.size());

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getAllWidgets(){
        // Progress bar
        progressBar = findViewById(R.id.my_jobs_progress_bar);
        showBar();

        recyclerView = findViewById(R.id.my_jobs_recycler_view);
        emptyView = findViewById(R.id.empty_jobs_list_view);

        jobsFilterSpinner = findViewById(R.id.spinner_filter_jobs);
        jobsFilterSpinner.setOnItemSelectedListener(this);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        jobsAdapter = new MyJobsListAdapter(this, jobList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(jobsAdapter);
    }

    //setting up jobs spinner adapter
    public void setSpinnerAdapter() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> jobsFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.poster_jobs_filter_array, android.R.layout.simple_spinner_item);
        jobsFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobsFilterSpinner.setAdapter(jobsFilterAdapter);
    }

    @Override
    public void onJobRowClicked(int position) {
        Job job = jobList.get(position);
        jobList.set(position, job);

        if (job.getJob_status() == 0){
            Intent intent = new Intent(this, PostJobActivity.class);
            intent.putExtra("job_id", job.getJob_id());
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, JobDetailsActivity.class);
            intent.putExtra("jobID", job.getJob_id());
            intent.putExtra("jobName", job.getName());
            startActivity(intent);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        showBar();
        if ("All".equals(parent.getItemAtPosition(position))) {
            showBar();
            loadSelectedJobs(5);
            Log.e(LOG_TAG, "Displaying all jobs");
        }else if ("Draft".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 0;
            loadSelectedJobs(0);
            Log.e(LOG_TAG, "Draft jobs selected");
        }else if ("Posted".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 1;
            loadSelectedJobs(1);
            Log.e(LOG_TAG, "Posted jobs selected");
        }else if ("Assigned".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 2;
            loadSelectedJobs(2);
            Log.e(LOG_TAG, "Assigned jobs selected");
        }else if ("Offers".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 3;
            loadSelectedJobs(3);
            Log.e(LOG_TAG, "Offered jobs selected");
        }else if ("Completed".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 4;
            loadSelectedJobs(4);
            Log.e(LOG_TAG, "Completed jobs selected");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //method to load the jobs selected from the spinner dropdown
    private void loadSelectedJobs(int status){
        if (status == 5) {//spinner item on "ALL" show all jobs
            mViewModel.getAllJobsForUser(mUserId).observe(this, userJobsList -> {
                jobList = userJobsList;
                jobsAdapter.setList(userJobsList);
                Log.e(LOG_TAG, "Jobs list size is " + jobList.size());
                hideBar();

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
            });
        }else{
            mViewModel.getAllJobsByStatus(mUserId, status).observe(this, userJobsByStatus -> {
                jobsAdapter.setList(userJobsByStatus);
                Log.e(LOG_TAG, "Jobs list size is " +jobList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
                hideBar();
            });
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
}
