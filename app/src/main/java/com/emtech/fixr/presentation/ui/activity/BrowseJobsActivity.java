package com.emtech.fixr.presentation.ui.activity;

import android.app.SearchManager;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.BrowseJobsDataFactory;
import com.emtech.fixr.presentation.adapters.BrowseJobsAdapter;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.SearchJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.SearchJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class BrowseJobsActivity extends AppCompatActivity implements BrowseJobsAdapter.BrowseJobsListAdapterListener,
        AdapterView.OnItemSelectedListener{
    private static final String LOG_TAG = BrowseJobsActivity.class.getSimpleName();
    private BrowseJobsActivityViewModel mViewModel;
    private BrowseJobsAdapter jobsAdapter;
    private ProgressBar progressBar;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecyclerView recyclerView;
    private List<Job> jobList = new ArrayList<Job>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_jobs);
        setupActionBar();

        // Progress bar
        progressBar = findViewById(R.id.browse_jobs_progress_bar);
        showBar();

        //clear previous list of it exists
        clearData();

        BrowseJobsViewModelFactory factory = InjectorUtils.provideBrowseJobsViewModelFactory(getApplicationContext());
        mViewModel = new ViewModelProvider
                (this, factory).get(BrowseJobsActivityViewModel.class);

        mViewModel.getBrowsedJobsLiveData().observe(this, browsedJobsList -> {
            jobList = browsedJobsList;
            jobsAdapter.submitList(browsedJobsList);
            Log.e(LOG_TAG, "Browsed jobs list size is " +browsedJobsList.size());

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);
        });

        getAllWidgets();
        setAdapter();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //get the widgets
    public void getAllWidgets(){
        recyclerView = findViewById(R.id.browse_jobs_recycler_view);
        hideBar();
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        jobsAdapter = new BrowseJobsAdapter(this,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(jobsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browse_jobs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search){
            //open the search activity if the search icon is clicked on
            Intent intent = new Intent(this, SearchJobsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onJobRowClicked(int position) {
        Job job = jobList.get(position);

        Intent intent = new Intent(this, JobDetailsActivity.class);
        intent.putExtra("jobID", job.getJob_id());
        intent.putExtra("jobName", job.getName());
        startActivity(intent);
    }

    //method to clear the previous list if it exists
    private void clearData(){
        if(jobList != null){
            jobList.clear(); // clear list
        }
        if (jobsAdapter != null) {
            jobsAdapter.notifyDataSetChanged(); // let your adapter know about the changes and reload view.
        }
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
          //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}