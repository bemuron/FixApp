package com.emtech.fixr.data.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Job;

public class BrowseJobsDataFactory extends DataSource.Factory {
    private FixAppRepository repository;

    //creating the mutable live data
    private MutableLiveData<PageKeyedDataSource<Integer, Job>> browsedJobsLiveDataSource = new MutableLiveData<>();
    //private MutableLiveData<BrowsedJobsDataSource> browsedJobsLiveDataSource = new MutableLiveData<>();

    @NonNull
    @Override
    public DataSource<Integer, Job> create() {
        //getting our data source object
        BrowsedJobsDataSource browsedJobsDataSource = new BrowsedJobsDataSource();

        //posting the datasource to get the values
        browsedJobsLiveDataSource.postValue(browsedJobsDataSource);

        //returning the datasource
        return browsedJobsDataSource;
    }


    //getter for itemlivedatasource
    public MutableLiveData<PageKeyedDataSource<Integer, Job>> getBrowsedJobsLiveDataSource() {
        return browsedJobsLiveDataSource;
    }

    /*public MutableLiveData<BrowsedJobsDataSource> getBrowsedJobsLiveDataSource() {
        return browsedJobsLiveDataSource;
    }*/
}
