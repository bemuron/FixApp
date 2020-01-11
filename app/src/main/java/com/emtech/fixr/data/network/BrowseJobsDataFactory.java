package com.emtech.fixr.data.network;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.PageKeyedDataSource;

import com.emtech.fixr.data.database.Job;

public class BrowseJobsDataFactory extends DataSource.Factory {

    //creating the mutable live data
    private MutableLiveData<PageKeyedDataSource<Integer, Job>> browsedJobsLiveDataSource = new MutableLiveData<>();

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
}
