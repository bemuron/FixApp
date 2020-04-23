package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.presentation.adapters.BrowseJobsAdapter;
import com.emtech.fixr.presentation.adapters.SearchJobsAdapter;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.SearchJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.SearchJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchJobsActivity extends AppCompatActivity implements SearchJobsAdapter.SearchJobsListAdapterListener,
        AdapterView.OnItemSelectedListener{
    private static final String LOG_TAG = SearchJobsActivity.class.getSimpleName();
    private SearchJobsActivityViewModel viewModel;
    private SearchJobsAdapter jobsAdapter;
    private ProgressBar progressBar;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecyclerView recyclerView;
    private List<Job> jobList = new ArrayList<Job>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_jobs);
        setupActionBar();

        // Progress bar
        progressBar = findViewById(R.id.search_jobs_progress_bar);

        //clear previous list of it exists
        clearData();

        SearchJobsViewModelFactory searchFactory = InjectorUtils.provideSearchJobsViewModelFactory(getApplicationContext());
        viewModel = new ViewModelProvider
                (this, searchFactory).get(SearchJobsActivityViewModel.class);

        getAllWidgets();
        setAdapter();

        //show the keyboard
        /*((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);*/

        handleIntent(getIntent());
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
        recyclerView = findViewById(R.id.search_jobs_recycler_view);
        hideBar();
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        jobsAdapter = new SearchJobsAdapter(this,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(jobsAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        setIntent(intent);
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        //showBar();
        //if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            //showResults(query);
            Log.e(LOG_TAG, "Search query = "+query);
            //send query to server to search
            viewModel.searchForJobs(query).observe(this, searchResultsList -> {
                jobsAdapter.submitList(searchResultsList);
                Log.e(LOG_TAG, "search results list size is " +searchResultsList.size());
                hideBar();

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
            });
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        //show the keyboard
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search){
            return true;
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
