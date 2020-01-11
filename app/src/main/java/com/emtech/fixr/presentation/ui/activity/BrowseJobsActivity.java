package com.emtech.fixr.presentation.ui.activity;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.presentation.adapters.BrowseJobsAdapter;
import com.emtech.fixr.presentation.adapters.MyJobsListAdapter;
import com.emtech.fixr.presentation.ui.fragment.MyJobsFragment;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class BrowseJobsActivity extends AppCompatActivity implements MyJobsListAdapter.MyJobsListAdapterListener,
        AdapterView.OnItemSelectedListener{
    private static final String LOG_TAG = BrowseJobsActivity.class.getSimpleName();
    private BrowseJobsActivityViewModel mViewModel;
    private BrowseJobsAdapter jobsAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecyclerView recyclerView;
    private List<Job> jobList = new ArrayList<Job>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_jobs);
        setupActionBar();

        BrowseJobsViewModelFactory factory = InjectorUtils.provideBrowseJobsViewModelFactory(getApplicationContext());
        mViewModel = ViewModelProviders.of
                (this, factory).get(BrowseJobsActivityViewModel.class);

        mViewModel.getBrowsedJobsLiveData().observe(this, browsedJobsList -> {
            //jobList = userJobsList;
            //jobsAdapter.setList(browsedJobsList);
            jobsAdapter.submitList(browsedJobsList);
            Log.e(LOG_TAG, "Browsed jobs list size is " +browsedJobsList.size());

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);
        });

        getAllWidgets();
        setAdapter();

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
        recyclerView = findViewById(R.id.browse_jobs_recycler_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        jobsAdapter = new BrowseJobsAdapter(this);

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
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
            //Intent wordIntent = new Intent(this, BibleVerses.class);
            //intent.putExtra("issue_ID", issue.getId());
            //intent.putExtra("issue_name", issue.getIssueName());
            //wordIntent.setData(intent.getData());
            //startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            //showResults(query);
            Log.e(LOG_TAG, "Search query = "+query);
            //send query to server to search
            mViewModel.searchForJobs(query).observe(this, browsedJobsList -> {
                //jobList = userJobsList;
                //jobsAdapter.setList(browsedJobsList);
                Log.e(LOG_TAG, "Browsed jobs list size is " +browsedJobsList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //searchView.setSuggestionsAdapter(words);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

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
        jobList.set(position, job);

    }
}
