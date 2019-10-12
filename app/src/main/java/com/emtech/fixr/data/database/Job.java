package com.emtech.fixr.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "jobs", indices = {@Index(value = {"name"}, unique = true)})
public class Job {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "job_id")
    private int job_id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

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
    private Date posted_on;

    @ColumnInfo(name = "job_status")
    private int job_status; // 1 - open, 2 - in progress, 3 - complete

    @ColumnInfo(name = "completed_by")
    private int completed_by; //professional's id

    @ColumnInfo(name = "completed_on")
    private Date completed_on;


    public Job(String name, String description, String image1, String image2,
               String image3, int posted_by, int category_id, Date posted_on,
               int job_status, int completed_by, Date completed_on) {
        this.name = name;
        this.description = description;
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

    public Job(int job_id, String name, String description, String image1,
               String image2, String image3, int posted_by, int category_id,
               Date posted_on, int job_status, int completed_by, Date completed_on) {
        this.job_id = job_id;
        this.name = name;
        this.description = description;
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

    public int getJob_id() {
        return job_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public Date getPosted_on() {
        return posted_on;
    }

    public int getJob_status() {
        return job_status;
    }

    public int getCompleted_by() {
        return completed_by;
    }

    public Date getCompleted_on() {
        return completed_on;
    }
}
