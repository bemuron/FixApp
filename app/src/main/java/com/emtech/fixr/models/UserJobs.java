package com.emtech.fixr.models;

import com.emtech.fixr.data.database.Job;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserJobs {
    //private Job[] jobs;
    private List<Job> jobsList;

    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("profile_pic")
    private String profile_pic;

    @SerializedName("fixer_profile_pic")
    private String fixer_profile_pic;

    @SerializedName("poster_profile_pic")
    private String poster_profile_pic;

    @SerializedName("name")
    private String name;

    @SerializedName("jobDetails")
    private Job jobDetails;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("fixer_user_name")
    private String fixer_user_name;

    @SerializedName("poster_user_name")
    private String poster_user_name;

    //@SerializedName("jobDetails")
    private List<Job> jobsListByStatus;

    //list of jobs for browsing
    private List<Job> browsedJobsList;

    //jobs browsed by query
    private List<Job> jobSearchResults;

    //number of pages
    private int pages_count;

    //list of jobs a fixer has made an offer for
    private List<Offer> offersMadeList;

    //list of jobs whose offers have been accepted for specific fixer
    private List<Offer> offersAcceptedList;

    private List<Offer> fixerOffersAcceptedList;

    //list of jobs by a poster which have offers made to
    private List<Offer> offersReceived;

    private Offer offerDetails;

    public UserJobs() {

    }

    public List<Job> getUserJobs() {
        return jobsList;
    }

    public Job getJobDetails() {
        return jobDetails;
    }

    public String getProfilePic() {
        return profile_pic;
    }

    public String getFixer_profile_pic() {
        return fixer_profile_pic;
    }

    public String getPoster_profile_pic() {
        return poster_profile_pic;
    }

    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public List<Job> getJobsListByStatus() {
        return jobsListByStatus;
    }

    public List<Job> getBrowsedJobsList() {
        return browsedJobsList;
    }

    public List<Job> getJobSearchResults() {
        return jobSearchResults;
    }

    public int getPages_count() {
        return pages_count;
    }

    public List<Offer> getOffersMadeList() {
        return offersMadeList;
    }

    public List<Offer> getOffersAcceptedList() {
        return offersAcceptedList;
    }

    public Offer getOfferDetails() {
        return offerDetails;
    }

    public List<Offer> getOffersReceived() {
        return offersReceived;
    }

    public List<Offer> getFixerOffersAcceptedList() {
        return fixerOffersAcceptedList;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getPoster_user_name() {
        return poster_user_name;
    }

    public String getFixer_user_name() {
        return fixer_user_name;
    }

    //LiveData<List<FixAppCategory>> getCategories();

}
