package com.emtech.fixr.presentation.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.database.Job;

import java.util.List;

/**
 * Created by BE on 2/3/2018.
 */

public class MyJobsActivityViewModel extends ViewModel {

    //private member variable to hold reference to the repository
    private FixAppRepository mRepository;

    //private LiveData member variable to cache the categories
    private LiveData<List<Job>> mAllJobs;

    //constructor that gets a reference to the repository and gets the categories
    public MyJobsActivityViewModel(FixAppRepository repository) {
        mRepository = repository;
    }

    //a getter method for all the categories. This hides the implementation from the UI
    public LiveData<List<Job>> getAllJobsForUser(int userid){
        return mRepository.getAllJobs(userid);
    }

    //a wrapper insert() method that calls the Repository's insert() method. In this way,
    // the implementation of insert() is completely hidden from the UI.
    //public void insert(Category category) { mRepository.insert(category); }

    public void delete() { mRepository.deleteUser();}


}
