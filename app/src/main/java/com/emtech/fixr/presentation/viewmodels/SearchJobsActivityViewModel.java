package com.emtech.fixr.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.data.network.SearchJobsDataFactory;
import com.emtech.fixr.data.network.SearchJobsDataSource;

import java.util.List;

/**
 * Created by BE on 11/02/2020.
 */

public class SearchJobsActivityViewModel extends ViewModel {

    //creating livedata for PagedList  and PagedKeyedDataSource
    LiveData<PagedList<Job>> searchResultsList;
    //LiveData<BrowsedJobsDataSource> liveDataSource;
    LiveData<PageKeyedDataSource<Integer, Job>> liveDataSource;

    //private member variable to hold reference to the repository
    private FixAppRepository mRepository;

    //constructor that gets a reference to the repository and gets the repo
    public SearchJobsActivityViewModel(FixAppRepository repository) {
        mRepository = repository;
    }

    //a getter method to search jobs based on the query inserted by the user
    // This hides the implementation from the UI
    public LiveData<PagedList<Job>> searchForJobs(String searchQuery){
        //getting our data source factory
        SearchJobsDataFactory searchJobsDataFactory = new SearchJobsDataFactory(searchQuery);

        //getting the live data source from data source factory
        liveDataSource = searchJobsDataFactory.getSearchedJobsLiveDataSource();

        //Getting PagedList config
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(SearchJobsDataSource.PAGE_SIZE).build();

        //Building the paged list
        searchResultsList = (new LivePagedListBuilder(searchJobsDataFactory, pagedListConfig))
                .build();

        return searchResultsList;
    }

}
