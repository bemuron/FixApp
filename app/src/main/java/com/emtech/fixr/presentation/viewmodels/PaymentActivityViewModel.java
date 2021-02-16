package com.emtech.fixr.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.presentation.ui.activity.OfferAcceptedDetailsForPosterActivity;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.util.ArrayList;

public class PaymentActivityViewModel extends ViewModel {
    private static final String TAG = PaymentActivityViewModel.class.getSimpleName();
    //private member variable to hold reference to the repository
    private FixAppRepository mRepository;

    private final MutableLiveData<Integer> newJobStatus = new MutableLiveData<>();
    private LiveData<Integer> jobStatus;
    //constructor that gets a reference to the repository and gets the categories
    public PaymentActivityViewModel(FixAppRepository repository) {
        mRepository = repository;
    }

    public LiveData<Integer> getJobStatusForPoster(int jobId, OfferAcceptedDetailsForPosterActivity activity){
        this.jobStatus = mRepository.getJobStatusForPoster(jobId, activity);
        newJobStatus.postValue(jobStatus.getValue());
        return  this.jobStatus;
    }

    //returned the new job created id
    public LiveData<Integer> getJobStatus() {
        return this.jobStatus;
    }

}
