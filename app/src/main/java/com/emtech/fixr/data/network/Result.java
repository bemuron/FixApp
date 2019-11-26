package com.emtech.fixr.data.network;

import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.models.User;
import com.google.gson.annotations.SerializedName;

public class Result {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("job")
    private Job job;

    public Result(Boolean error, String message, User user, Job job) {
        this.error = error;
        this.message = message;
        this.user = user;
        this.job = job;
    }


    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Job getJob() {
        return job;
    }
}
