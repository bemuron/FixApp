package com.emtech.fixr.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.models.Offer;

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

    //a getter method for all the jobs associated with this user.
    // This hides the implementation from the UI
    public LiveData<List<Job>> getAllJobsForUser(int userid){
        return mRepository.getAllJobs(userid);
    }

    //a getter method for all the jobs associated with this user by status
    // This hides the implementation from the UI
    public LiveData<List<Job>> getAllJobsByStatus(int userid, int status){
        return mRepository.getJobsByStatus(userid, status);
    }

    //a getter method for the job details. This hides the implementation from the UI
    public LiveData<Job> getJobDetails(int jobId){
        return mRepository.getJobDetails(jobId);
    }

    //a getter method for all the jobs a fixer has made an offer to.
    public LiveData<List<Offer>> getAllOffersMade(int userId){
        return mRepository.getOffersMade(userId);
    }

    //a getter method for all the job offers by this fixer that have been accepted
    public LiveData<List<Offer>> getOffersAcceptedForFixer(int userId){
        return mRepository.getOffersAcceptedForFixer(userId);
    }

    //a getter method for all the jobs whose offers a poster has accepted
    public LiveData<List<Offer>> getAllOffersAccepted(int userId){
        return mRepository.getOffersAccepted(userId);
    }

    //a getter method for the offer details. This hides the implementation from the UI
    public LiveData<Offer> getOfferDetailsForFixer(int offerId){
        return mRepository.getOfferDetailsForFixer(offerId);
    }

    //a getter method for all the jobs whose offers have been accepted by this poster
    public LiveData<List<Offer>> getAllOffersReceived(int userId){
        return mRepository.getOffersReceived(userId);
    }

    //a getter method for the offer details for poster
    public LiveData<Offer> getOfferDetailsForPoster(int offerId){
        return mRepository.getOfferDetailsForPoster(offerId);
    }

    //method to update the status of this offer to 1 (seen by poster)
    public void updateOfferSeenByPosterStatus(int offerId){
        mRepository.updateOfferSeenByPosterStatus(offerId);
    }

    //method to update the offer to accepted by the poster - 1
    public void posterAcceptOffer(int offerId, int jobId){
        mRepository.posterAcceptOffer(offerId, jobId);
    }

    //method to update the offer to rejected by the poster - 2
    public void posterRejectOffer(int offerId, int jobId){
        mRepository.posterRejectOffer(offerId, jobId);
    }

    //method to update the offer to rejected by the poster - 3
    public void fixerRejectOffer(int offerId, int jobId){
        mRepository.fixerRejectOffer(offerId, jobId);
    }

    //method to check if the fixer already made an offer for a job
    //a fixer cant make an offer for the same job more than once
    //they can only edit what they had already made
    public void checkIfOfferIsAlreadyMade(int userId, int jobId){
        mRepository.checkIfOfferIsAlreadyMade(userId, jobId);
    }

    //handles the submission of the fixer's rating
    public void submitFixerRating(int job_id, int poster_id, int fixer_id, float fixer_rating, String comment){
        mRepository.submitFixerRating(job_id, poster_id, fixer_id, fixer_rating, comment);
    }

    //handles the submission of the poster's rating
    public void submitPosterRating(int job_id, int fixer_id, int poster_id, float poster_rating, String comment){
        mRepository.submitPosterRating(job_id, fixer_id, poster_id, poster_rating, comment);
    }



}
