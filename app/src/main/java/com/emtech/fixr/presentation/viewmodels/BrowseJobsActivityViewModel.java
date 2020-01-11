package com.emtech.fixr.presentation.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PageKeyedDataSource;
import android.arch.paging.PagedList;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.BrowseJobsDataFactory;
import com.emtech.fixr.data.network.BrowsedJobsDataSource;

import java.util.List;

/**
 * Created by BE on 2/3/2018.
 */

public class BrowseJobsActivityViewModel extends ViewModel {

    //creating livedata for PagedList  and PagedKeyedDataSource
    LiveData<PagedList<Job>> jobsPagedList;
    LiveData<PageKeyedDataSource<Integer, Job>> liveDataSource;

    //private member variable to hold reference to the repository
    private FixAppRepository mRepository;

    //constructor that gets a reference to the repository and gets the categories
    public BrowseJobsActivityViewModel(FixAppRepository repository) {
        mRepository = repository;

        //getting our data source factory
        BrowseJobsDataFactory browseJobsDataFactory = new BrowseJobsDataFactory();

        //getting the live data source from data source factory
        liveDataSource = browseJobsDataFactory.getBrowsedJobsLiveDataSource();

        //Getting PagedList config
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(BrowsedJobsDataSource.PAGE_SIZE).build();

        //Building the paged list
        jobsPagedList = (new LivePagedListBuilder(browseJobsDataFactory, pagedListConfig))
                .build();
    }

    //a getter method for all the jobs.
    // This hides the implementation from the UI
    public LiveData<List<Job>> browseAllJobs(){
        return mRepository.browseAllJobs();
    }

    //a getter method to search jobs based on the query inserted by the user
    // This hides the implementation from the UI
    public LiveData<List<Job>> searchForJobs(String searchQuery){
        return mRepository.searchForJobs(searchQuery);
    }

    public LiveData<PagedList<Job>> getBrowsedJobsLiveData() {
        return jobsPagedList;
    }

}
