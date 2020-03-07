package com.emtech.fixr.models;


public class Offer {

    private int offer_id;

    private int offered_by;

    private int job_id;

    private String offer_amount;

    private String message;

    private String last_edited_on;

    private int seen_by_poster;

    private int edit_count;

    private int offer_accepted;

    private String name;

    private String est_tot_budget;

    private int posted_by;

    private String posted_on;

    private String job_date;

    private String profile_pic;

    public int getOffer_id() {
        return offer_id;
    }

    public void setOffer_id(int offer_id) {
        this.offer_id = offer_id;
    }

    public int getOffered_by() {
        return offered_by;
    }

    public void setOffered_by(int offered_by) {
        this.offered_by = offered_by;
    }

    public int getJob_id() {
        return job_id;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;
    }

    public String getOffer_amount() {
        return offer_amount;
    }

    public void setOffer_amount(String offer_amount) {
        this.offer_amount = offer_amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLast_edited_on() {
        return last_edited_on;
    }

    public void setLast_edited_on(String last_edited_on) {
        this.last_edited_on = last_edited_on;
    }

    public int getSeen_by_poster() {
        return seen_by_poster;
    }

    public void setSeen_by_poster(int seen_by_poster) {
        this.seen_by_poster = seen_by_poster;
    }

    public int getEdit_count() {
        return edit_count;
    }

    public void setEdit_count(int edit_count) {
        this.edit_count = edit_count;
    }

    public int getOffer_accepted() {
        return offer_accepted;
    }

    public void setOffer_accepted(int offer_accepted) {
        this.offer_accepted = offer_accepted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEst_tot_budget() {
        return est_tot_budget;
    }

    public void setEst_tot_budget(String est_tot_budget) {
        this.est_tot_budget = est_tot_budget;
    }

    public int getPosted_by() {
        return posted_by;
    }

    public void setPosted_by(int posted_by) {
        this.posted_by = posted_by;
    }

    public String getPosted_on() {
        return posted_on;
    }

    public void setPosted_on(String posted_on) {
        this.posted_on = posted_on;
    }

    public String getJob_date() {
        return job_date;
    }

    public void setJob_date(String job_date) {
        this.job_date = job_date;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
