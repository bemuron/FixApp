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

    @SerializedName("name")
    private String name;

    @SerializedName("jobDetails")
    private Job jobDetails;

    //@SerializedName("jobDetails")
    private List<Job> jobsListByStatus;

    //list of jobs for browsing
    private List<Job> browsedJobsList;

    //jobs browsed by query
    private List<Job> searchResultsJobsList;

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

    public List<Job> getSearchResultsJobsList() {
        return searchResultsJobsList;
    }

    //LiveData<List<FixAppCategory>> getCategories();

}
