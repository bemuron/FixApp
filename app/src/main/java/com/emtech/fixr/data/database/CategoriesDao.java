package com.emtech.fixr.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
    void deleteAllCategories();

    @Query("SELECT * from categories ORDER BY category_id ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT COUNT(category_id) FROM categories")
    int countCategoriesInDb();
}
