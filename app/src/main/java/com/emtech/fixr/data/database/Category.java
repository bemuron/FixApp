package com.emtech.fixr.data.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by BE on 2/3/2018.
 */

@Entity(tableName = "categories", indices = {@Index(value = {"name"}, unique = true)})
public class Category {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    private int category_id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "image")
    private String imageName;

    private int color = -1;

    @Ignore
    public Category(String name, String imageName){
        this.name = name;
        this.imageName = imageName;
    }

    public Category(int category_id, String name, String imageName){
        this.category_id = category_id;
        this.name = name;
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public int getCategory_id() {
        return category_id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
