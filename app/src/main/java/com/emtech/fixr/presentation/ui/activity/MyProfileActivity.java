package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.helpers.CircleTransform;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.viewmodels.UserProfileActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.UserProfileActivityViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MyProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MyProfileActivity.class.getSimpleName();
    private ImageView userProfilePicIv;
    private TextView userNameTv, userLocationTv, memberSinceTv,
            ratingAsUserTv, ratingAsFixerTv, aboutUserTv;
    private RelativeLayout userRatingContainer;
    private User mUserDetails, user;
    private UserProfileActivityViewModel userProfileActivityViewModel;
    private ProgressBar pBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        setupActionBar();

        // Progress bar
        //pBar = findViewById(R.id.forFixer_progress_bar);
        //showBar();

        UserProfileActivityViewModelFactory factory = InjectorUtils.provideUserProfileViewModelFactory(this.getApplicationContext());
        userProfileActivityViewModel = new ViewModelProvider
                (this, factory).get(UserProfileActivityViewModel.class);

        userProfileActivityViewModel.getUserDetails().observe(this, userDetails -> {

            mUserDetails = userDetails;

            if (user != null) {
                //create new user object
                user = new User();
                user.setUser_id(mUserDetails.getUser_id());
                user.setEmail(mUserDetails.getEmail());
                user.setCreated_on(mUserDetails.getCreated_on());
                user.setRole(mUserDetails.getRole());
                user.setDescription(mUserDetails.getDescription());
                user.setPhone_number(mUserDetails.getPhone_number());
                user.setProfile_pic(mUserDetails.getProfile_pic());
                user.setDate_of_birth(mUserDetails.getDate_of_birth());
                user.setGender(mUserDetails.getGender());
                user.setName(mUserDetails.getName());

                populateViews();

                Log.i(TAG, "User name " + user.getName());
            }else{
                //hideBar();
                Log.e(TAG, "User details not retrieved");
            }

        });

        //initialise the views
        setUpWidgets();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //initialise the view widgets
    private void setUpWidgets(){
        userProfilePicIv = findViewById(R.id.profile_pic);
        userNameTv = findViewById(R.id.profile_user_name);
        userLocationTv = findViewById(R.id.profile_user_location);
        memberSinceTv = findViewById(R.id.profile_member_since);
        userRatingContainer = findViewById(R.id.profile_user_rating_container);
        userRatingContainer.setOnClickListener(this);
        ratingAsFixerTv = findViewById(R.id.profile_rating_as_fixer);
        ratingAsUserTv = findViewById(R.id.profile_rating_as_poster);
        aboutUserTv = findViewById(R.id.profile_about_text);
    }

    //method to handle population of the views with the content
    private void populateViews(){
        userNameTv.setText(user.getName());
        userLocationTv.setText(user.getLocation());
        memberSinceTv.setText(user.getCreated_on());
        userRatingContainer.setOnClickListener(this);
        //ratingAsUserTv.setText(user.getRatingAsPoster);
        //ratingAsFixerTv.setText(user.getRatingAsFixer);
        aboutUserTv.setText(user.getDescription());
        formatDate();

        //set user profile pic
        try {
            if (!TextUtils.isEmpty(user.getProfile_pic())) {
                Glide.with(this)
                        .load("http://www.emtechint.com/fixapp/assets/images/profile_pics/" + user.getProfile_pic())
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .apply(new RequestOptions().fitCenter()
                                .transform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(userProfilePicIv);
                userProfilePicIv.setColorFilter(null);
            }
        }catch (Exception e){
            Log.e(TAG, "Could not load image");
            e.printStackTrace();
        }
    }

    //method to convert from the mysql date format to a more readable one
    private void formatDate(){
        String memberSince = null;

        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.US);

        try{
            Date date = mysqlDateTimeFormat.parse(user.getCreated_on());
            memberSince = sdf.format(date);
        }catch (Exception e){
            e.printStackTrace();
        }

        memberSinceTv.setText(memberSince);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit_profile){

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_user_rating_container){
            //open user reviews and ratings activity
        }
    }
}
