package com.emtech.fixr.data.database;

/*
The database is an abstract class that extends RoomDatabase. It is annotated with @Database,
which is where it defines its entities. It then has abstract getters for each of your DAOs.
It is also a singleton.
 */

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.emtech.fixr.models.User;

@Database(entities = {User.class,Category.class}, version = 1, exportSchema = false)
public abstract class FixAppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "fixapp.db";

    /*
    Having more than one instance of a database running causes consistency problems,
    if, for example, you try to read the database with one instance while you're
    writing with another instance. To make sure that you're only creating one instance
    of the RoomDatabase, your database class should be a Singleton.
     */
    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static volatile FixAppDatabase sInstance;

    public static FixAppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            FixAppDatabase.class, FixAppDatabase.DATABASE_NAME).build();
                }
            }
        }
        return sInstance;
    }

    // The associated DAOs for the database
    public abstract CategoriesDao categoriesDao();

    public abstract UsersDao usersDao();
}
