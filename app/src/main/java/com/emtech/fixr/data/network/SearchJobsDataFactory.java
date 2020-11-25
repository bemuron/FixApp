package com.emtech.fixr.data.network;

import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Job;

public class SearchJobsDataFactory extends DataSource.Factory {
  private FixAppRepository repository;
  private String searchQuery;

  //creating the mutable live data
  private MutableLiveData<PageKeyedDataSource<Integer, Job>> searchedJobsLiveDataSource = new MutableLiveData<>();

  public SearchJobsDataFactory(String searchQuery) {
    this.searchQuery = searchQuery;
  }

  @Override
  public DataSource<Integer, Job> create() {
    //getting our data source object
    SearchJobsDataSource searchJobsDataSource = new SearchJobsDataSource(searchQuery);

    //posting the data source to get the values
    searchedJobsLiveDataSource.postValue(searchJobsDataSource);

    //returning the data source
    return searchJobsDataSource;
  }


  //getter for item live data source
  public MutableLiveData<PageKeyedDataSource<Integer, Job>> getSearchedJobsLiveDataSource() {
    return searchedJobsLiveDataSource;
  }

    /*public MutableLiveData<BrowsedJobsDataSource> getBrowsedJobsLiveDataSource() {
        return browsedJobsLiveDataSource;
    }*/
}