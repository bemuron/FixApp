package com.emtech.fixr.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.emtech.fixr.presentation.ui.activity.LoginActivity;

import java.util.HashMap;

/*
 *This class maintains session data across the app using shared prefs.
 * We store a boolean flag isLoggedIn in shared prefs to check the login status
 *
 */
public class SessionManager {
  // LogCat tag
  private static String TAG = SessionManager.class.getSimpleName();

  // Shared Preferences
  SharedPreferences pref;

  Editor editor;
  Context _context;

  // Shared pref mode
  int PRIVATE_MODE = 0;

  // Shared preferences file name
  private static final String PREF_NAME = "FIxAppUserPref";

  private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

  //user id
  public static final String KEY_USER_ID = "userId";

  //username
  public static final String KEY_NAME = "name";

  //email
  public static final String KEY_EMAIL = "email";

  //user role (can be poster or fixer)
  public static final String KEY_ROLE = "role";

  //phone number
  public static final String KEY_PHONE_NUMBER = "phone_number";

  //user profile pic
  public static final String KEY_PROFILE_PIC = "profile_pic";

  //session in progress
  private static final String KEY_SESSION_IN_PROGRESS = "isSessionInProgress";

  //session: payment received
  private static final String KEY_SESSION_PAYMENT_RECEIVED = "isPaymentReceived";

  //session job id
  private static final String KEY_JOB_ID = "job_id";

  //session user id
  //id of the user who requested for a tutor
  //the student
  public static final String KEY_SESSION_USER_ID = "sessionUserId";

  public SessionManager(Context context) {
    this._context = context;
    pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    editor = pref.edit();
  }

  //create login session
  public void createLoginSession(int user_id, String name, String email, String role, String profile_pic){
    // Storing login value as TRUE
    editor.putBoolean(KEY_IS_LOGGED_IN, true);

    // Storing user id in pref
    editor.putInt(KEY_USER_ID, user_id);

    // Storing name in pref
    editor.putString(KEY_NAME, name);

    // Storing email in pref
    editor.putString(KEY_EMAIL, email);

    // Storing role in pref
    editor.putString(KEY_ROLE, role);

    // Storing role in pref
    editor.putString(KEY_PROFILE_PIC, profile_pic);

    // commit changes
    editor.commit();
  }

  /**
   * Check login method wil check user login status
   * If false it will redirect user to login page
   * Else won't do anything
   * */
  public void checkLogin(){
    // Check login status
    if(!this.isLoggedIn()){
      // user is not logged in redirect him to Login Activity
      Intent i = new Intent(_context, LoginActivity.class);
      // Closing all the Activities
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

      // Add new Flag to start new Activity
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      // Staring Login Activity
      _context.startActivity(i);
    }

  }

  /**
   * Get stored session data
   * */
  public HashMap<String, String> getUserDetails(){
    HashMap<String, String> user = new HashMap<String, String>();
    // user name
    user.put(KEY_NAME, pref.getString(KEY_NAME, null));

    // user email id
    user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

    // return user
    return user;
  }

  /**
   * Clear session details
   * */
  public void logoutUser(){
    // Clearing all data from Shared Preferences
    editor.clear();
    editor.commit();

    // After logout redirect user to Login Activity
    Intent i = new Intent(_context, LoginActivity.class);
    // Closing all the Activities
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    // Add new Flag to start new Activity
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // Starting Login Activity
    _context.startActivity(i);
  }

  //get user id
  public int  getUserId(){
    return pref.getInt(KEY_USER_ID, 0);
  }

  //get user role
  public String getUserRole(){
    return pref.getString(KEY_ROLE, null);
  }

  /**
   * Quick check for login
   * **/
  // Get Login State
  public boolean isLoggedIn(){
    return pref.getBoolean(KEY_IS_LOGGED_IN, false);
  }

  public void setLogin(boolean isLoggedIn) {

    editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

    // commit changes
    editor.commit();

    Log.d(TAG, "User login session modified!");
  }
}
