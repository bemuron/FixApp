package com.emtech.fixr.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.data.database.Job;

import java.util.List;

public class UserJobs {
    //private Job[] jobs;
    private List<Job> jobs;

    public UserJobs() {

    }

    public List<Job> getUserJobs() {
        return jobs;
    }

    //LiveData<List<FixAppCategory>> getCategories();

    public void setMessages(Category[] categories) {
        this.jobs = jobs;
    }

}
