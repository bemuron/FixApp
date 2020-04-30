package com.emtech.fixr.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.models.User;

import java.util.List;

public class UserProfileActivityViewModel extends ViewModel {

  //private member variable to hold reference to the repository
  private FixAppRepository mRepository;

  //private LiveData member variable to cache the user profile details
  private LiveData<User> mUserDetails;

  //constructor that gets a reference to the repository and gets the categories
  public UserProfileActivityViewModel(FixAppRepository repository) {
    mRepository = repository;
    mUserDetails = mRepository.getUserDetails();
  }

  //a getter method for all the user details. This hides the implementation from the UI
  public LiveData<User> getUserDetails(){
    return mUserDetails;
  }

  //deletes user from sqlite db - room
  public void delete() { mRepository.deleteUser();}

  //updates user profile in room db
  public void updateProfile(int user_id, String email, String created_on,
                            String role, String description, String phone_number,
                            String profile_pic, String date_of_birth, String gender,
                            String name, String location){
    mRepository.updateProfile(user_id, email, created_on, role, description, phone_number,
            profile_pic,  date_of_birth, gender, name, location);
  }


}
