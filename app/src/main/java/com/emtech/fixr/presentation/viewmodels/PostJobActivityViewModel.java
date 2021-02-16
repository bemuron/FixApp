package com.emtech.fixr.presentation.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.util.ArrayList;

public class PostJobActivityViewModel extends ViewModel {
    private static final String TAG = PostJobActivityViewModel.class.getSimpleName();
    //private member variable to hold reference to the repository
    private FixAppRepository mRepository;

    private final MutableLiveData<Integer> newJobId = new MutableLiveData<>();
    private LiveData<Integer> jobId;
    //constructor that gets a reference to the repository and gets the categories
    public PostJobActivityViewModel(FixAppRepository repository) {
        mRepository = repository;
    }

    public LiveData<Integer> createNewJob(int userId, String jobTitle, String jobDesc,
                                   int categoryId, PostJobActivity postJobActivity){
        jobId = mRepository.createJob(userId, jobTitle, jobDesc, categoryId, postJobActivity);
        newJobId.postValue(jobId.getValue());
        return  this.jobId;
    }

    //returned the new job created id
    public LiveData<Integer> getNewJobId() {
        return this.jobId;
    }

    //call repository method to handle creating new job at the server and returning the job id
    public void createJob(int userId, String jobTitle, String jobDesc,
                          int categoryId, PostJobActivity postJobActivity){
        mRepository.createNewJob(userId, jobTitle, jobDesc, categoryId, postJobActivity);
    }

    //call repository method to handle posting data to server
    public void postJob(int userId, String jobTitle, String jobDesc, String jobLocation,
                        String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                        ArrayList<File> imageFilesList, int categoryId, PostJobActivity postJobActivity){
        mRepository.postJobService(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, imageFilesList, categoryId, postJobActivity);
    }

    public void updateJobDetails(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                 String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                 ArrayList<File> imageFilesList, PostJobActivity postJobActivity){
        mRepository.updateJobDetails(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, imageFilesList, postJobActivity);
    }

    public void updateJobDetailsWithoutImage(int jobId, String jobTitle, String jobDesc, String jobLocation, String mustHaveOne,
                                             String mustHaveTwo, String mustHaveThree, int isJobRemote, PostJobActivity postJobActivity){
        mRepository.UpdateJobDetailsWithoutImage(jobId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, postJobActivity);
    }

    //method to save the offer made by the user
    public void saveOffer(int amountOffered, String offerMessage, int userId, int jobId){
        mRepository.saveFixerOffer(amountOffered, offerMessage, userId, jobId);
    }

    //method to edit the offer made by the user
    public void editOffer(int offerId, int amountOffered, String offerMessage, int editCount){
        mRepository.editFixerOffer(offerId, amountOffered, offerMessage, editCount);
    }
}
