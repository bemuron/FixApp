package com.emtech.fixr.models;


public class Offer {

    private int offer_id;

    private int offered_by;

    private int job_id;

    private int job_status;

    private String job_name;

    private String offer_amount;

    private String message;

    private String last_edited_on;

    private int seen_by_poster;

    private int edit_count;

    private int offer_accepted;

    private String name;

    private String poster_user_name;

    private String fixer_user_name;

    private String est_tot_budget;

    private String final_job_cost;

    private int posted_by;

    private String user_name;

    private String posted_on;

    private String job_date;

    private String profile_pic;

    private String poster_profile_pic;

    private String fixer_profile_pic;

    private int color = -1;

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

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getJob_status() {
        return job_status;
    }

    public void setJob_status(int job_status) {
        this.job_status = job_status;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getFinal_job_cost() {
        return final_job_cost;
    }

    public void setFinal_job_cost(String final_job_cost) {
        this.final_job_cost = final_job_cost;
    }

    public String getPoster_user_name() {
        return poster_user_name;
    }

    public void setPoster_user_name(String poster_user_name) {
        this.poster_user_name = poster_user_name;
    }

    public String getFixer_user_name() {
        return fixer_user_name;
    }

    public void setFixer_user_name(String fixer_user_name) {
        this.fixer_user_name = fixer_user_name;
    }

    public String getPoster_profile_pic() {
        return poster_profile_pic;
    }

    public void setPoster_profile_pic(String poster_profile_pic) {
        this.poster_profile_pic = poster_profile_pic;
    }

    public String getFixer_profile_pic() {
        return fixer_profile_pic;
    }

    public void setFixer_profile_pic(String fixer_profile_pic) {
        this.fixer_profile_pic = fixer_profile_pic;
    }
}
