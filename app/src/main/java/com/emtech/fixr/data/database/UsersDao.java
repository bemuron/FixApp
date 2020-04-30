package com.emtech.fixr.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import android.database.Cursor;

import com.emtech.fixr.models.User;

/**
 * Created by BE on 2/3/2018.
 */

@Dao
public interface UsersDao {

    /*
    insert user into db
     */
    @Insert
    void insertUser(User user);

    @Query("Delete from user")
    void deleteUser();

    @Query("SELECT * from user")
    //HashMap<String, String> getUserDetails();
    //Cursor getUserDetails();
    LiveData<User> getUserDetails();

    //update user details
    @Query("UPDATE user SET email = :email, created_on = :created_on, role = :role, " +
            "name = :name, description = :description, date_of_birth = :date_of_birth, " +
            "gender = :gender, phone_number = :phone_number, profile_pic = :profile_pic, " +
            "location = :location WHERE user_id = :user_id")
    void updateProfile(int user_id, String email, String created_on,
                       String role, String description, String phone_number,
                       String profile_pic, String date_of_birth, String gender,
                       String name, String location);
}
