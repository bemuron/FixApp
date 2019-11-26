package com.emtech.fixr.presentation.viewmodels;

import android.arch.lifecycle.ViewModel;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PostJobActivityViewModel extends ViewModel {

    //private member variable to hold reference to the repository
    private FixAppRepository mRepository;

    //constructor that gets a reference to the repository and gets the categories
    public PostJobActivityViewModel(FixAppRepository repository) {
        mRepository = repository;
    }

    //call repository method to handle posting data to server
    public void postJob(int userId, String jobTitle, String jobDesc, String jobLocation,
                        String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                        File file, int categoryId, PostJobActivity postJobActivity){
        mRepository.postJobService(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, file, categoryId, postJobActivity);
    }
}
