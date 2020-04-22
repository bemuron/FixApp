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

    @SerializedName("is_offer_already_made")
    private Boolean is_offer_already_made;

    public Result(Boolean error, String message, User user, Job job, Boolean is_offer_already_made) {
        this.error = error;
        this.message = message;
        this.user = user;
        this.job = job;
        this.is_offer_already_made = is_offer_already_made;
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

    public Boolean getIs_offer_already_made() {
        return is_offer_already_made;
    }
}
