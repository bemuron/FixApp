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

    //returned the job status
    public LiveData<Integer> getJobStatus() {
        return this.jobStatus;
    }

    //if fixer was paid in cash
    public void madeCashPayment(){

    }

    //make mobile money payment
    public void makeMobileMoneyPayment(int job_id, int poster_id, int fixer_id, int offer_id,
                                       int job_cost, int service_fee, int amnt_fixer_gets){
        mRepository.makeMobileMoneyPayment(job_id, poster_id, fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);

    }

    //make cash payment
    public void makeCashPayment(int job_id, int poster_id, int fixer_id, int offer_id,
                                int job_cost, int service_fee, int amnt_fixer_gets){
        mRepository.makeCashPayment(job_id, poster_id, fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);
    }

}
