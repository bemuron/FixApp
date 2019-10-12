package com.emtech.fixr.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by BE on 2/3/2018.
 */

@Dao
public interface CategoriesDao {

    /*
    insert categories into db
     */
    @Insert
    void insertCategory(Category[] category);

    //@Insert
    //void insertCategory(Category category);

    @Query("Delete from categories")
    void deleteAll();

    @Query("SELECT * from categories ORDER BY category_id ASC")
    LiveData<List<Category>> getAllCategories();
}
