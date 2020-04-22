package com.emtech.fixr.data.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.recyclerview.widget.DiffUtil;

@Entity(tableName = "jobs", indices = {@Index(value = {"name"}, unique = true)})
public class Job {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "job_id")
  private int job_id;

  @ColumnInfo(name = "name")
  private String name;

  @ColumnInfo(name = "description")
  private String description;

  @ColumnInfo(name = "location")
  private String location;

  @ColumnInfo(name = "image1")
  private String image1;

  @ColumnInfo(name = "image2")
  private String image2;

  @ColumnInfo(name = "image3")
  private String image3;

  @ColumnInfo(name = "posted_by")
  private int posted_by; //userid

  @ColumnInfo(name = "category_id")
  private int category_id;

  @ColumnInfo(name = "posted_on")
  private String posted_on;

  @ColumnInfo(name = "job_status")
  private int job_status; // 0 - draft, 1 - posted, 2 - assigned, 3 - offers, 4 - complete

  @ColumnInfo(name = "completed_by")
  private int completed_by; //professional's id

  @ColumnInfo(name = "completed_on")
  private String completed_on;

  @ColumnInfo(name = "job_date")
  private String job_date;

  @ColumnInfo(name = "job_time")
  private String job_time;

  @ColumnInfo(name = "total_budget")
  private String total_budget;

  @ColumnInfo(name = "price_per_hr")
  private String price_per_hr;

  @ColumnInfo(name = "total_hrs")
  private String total_hrs;

  @ColumnInfo(name = "est_tot_budget")
  private String est_tot_budget;

  @ColumnInfo(name = "must_have_one")
  private String must_have_one;

  @ColumnInfo(name = "must_have_two")
  private String must_have_two;

  @ColumnInfo(name = "must_have_three")
  private String must_have_three;

  @ColumnInfo(name = "is_job_remote")
  private int is_job_remote;   // 0 - false (Job is not remote), 1 - true (Job is remote)

  @ColumnInfo(name = "profile_pic")
  private String profile_pic;

  @ColumnInfo(name = "userName")
  private String userName;


    /*public Job(String name, String description, String location, String image1, String image2,
               String image3, int posted_by, int category_id, Date posted_on,
               int job_status, int completed_by, Date completed_on) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.posted_by = posted_by;
        this.category_id = category_id;
        this.posted_on = posted_on;
        this.job_status = job_status;
        this.completed_by = completed_by;
        this.completed_on = completed_on;
    }

    public Job(int job_id, String name, String description, String location, String image1,
               String image2, String image3, int posted_by, int category_id,
               Date posted_on, int job_status, int completed_by, Date completed_on) {
        this.job_id = job_id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.posted_by = posted_by;
        this.category_id = category_id;
        this.posted_on = posted_on;
        this.job_status = job_status;
        this.completed_by = completed_by;
        this.completed_on = completed_on;
    }*/

  public int getJob_id() {
    return job_id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getImage1() {
    return image1;
  }

  public String getImage2() {
    return image2;
  }

  public String getImage3() {
    return image3;
  }

  public int getPosted_by() {
    return posted_by;
  }

  public int getCategory_id() {
    return category_id;
  }

  public String getPosted_on() {
    return posted_on;
  }

  public int getJob_status() {
    return job_status;
  }

  public int getCompleted_by() {
    return completed_by;
  }

  public String getCompleted_on() {
    return completed_on;
  }

  public void setJob_id(int job_id) {
    this.job_id = job_id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setImage1(String image1) {
    this.image1 = image1;
  }

  public void setImage2(String image2) {
    this.image2 = image2;
  }

  public void setImage3(String image3) {
    this.image3 = image3;
  }

  public void setPosted_by(int posted_by) {
    this.posted_by = posted_by;
  }

  public void setCategory_id(int category_id) {
    this.category_id = category_id;
  }

  public void setPosted_on(String posted_on) {
    this.posted_on = posted_on;
  }

  public void setJob_status(int job_status) {
    this.job_status = job_status;
  }

  public void setCompleted_by(int completed_by) {
    this.completed_by = completed_by;
  }

  public void setCompleted_on(String completed_on) {
    this.completed_on = completed_on;
  }

  public String getJob_date() {
    return job_date;
  }

  public void setJob_date(String job_date) {
    this.job_date = job_date;
  }

  public String getJob_time() {
    return job_time;
  }

  public void setJob_time(String job_time) {
    this.job_time = job_time;
  }

  public String getTotal_budget() {
    return total_budget;
  }

  public void setTotal_budget(String total_budget) {
    this.total_budget = total_budget;
  }

  public String getPrice_per_hr() {
    return price_per_hr;
  }

  public void setPrice_per_hr(String price_per_hr) {
    this.price_per_hr = price_per_hr;
  }

  public String getTotal_hrs() {
    return total_hrs;
  }

  public void setTotal_hrs(String total_hrs) {
    this.total_hrs = total_hrs;
  }

  public String getEst_tot_budget() {
    return est_tot_budget;
  }

  public void setEst_tot_budget(String est_tot_budget) {
    this.est_tot_budget = est_tot_budget;
  }

  public String getMust_have_one() {
    return must_have_one;
  }

  public void setMust_have_one(String must_have_one) {
    this.must_have_one = must_have_one;
  }

  public String getMust_have_two() {
    return must_have_two;
  }

  public void setMust_have_two(String must_have_two) {
    this.must_have_two = must_have_two;
  }

  public String getMust_have_three() {
    return must_have_three;
  }

  public void setMust_have_three(String must_have_three) {
    this.must_have_three = must_have_three;
  }

  public int getIs_job_remote() {
    return is_job_remote;
  }

  public void setIs_job_remote(int is_job_remote) {
    this.is_job_remote = is_job_remote;
  }

  public String getProfile_pic() {
    return profile_pic;
  }

  public void setProfile_pic(String profile_pic) {
    this.profile_pic = profile_pic;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
