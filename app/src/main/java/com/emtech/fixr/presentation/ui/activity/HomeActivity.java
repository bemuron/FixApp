package com.emtech.fixr.presentation.ui.activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import com.emtech.fixr.app.Config;
import com.emtech.fixr.fcm.MyNotificationManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.app.MyApplication;
import com.emtech.fixr.data.database.Category;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.DashboardFragment;
import com.emtech.fixr.presentation.ui.fragment.MyJobsFragment;
import com.emtech.fixr.presentation.ui.fragment.PaymentHistoryFragment;
import com.emtech.fixr.presentation.viewmodels.HomeActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.HomeViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CategoriesAdapter.CategoriesAdapterOnItemClickHandler, MyJobsFragment.OnMyJobsInteractionListener
        ,PaymentHistoryFragment.OnPaymentHistoryInteractionListener,
        DashboardFragment.OnDashboardInteractionListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private MyNotificationManager myNotificationManager;
    private CategoriesAdapter mCategoriesAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private HomeActivityViewModel mViewModel;
    private ProgressBar mLoadingIndicator;
    private SessionManager session;
    private String userRole;
    private FrameLayout navDrawerFragmentContainer;
    private int userId;

   // private CategoryGridAdapter categoryGridAdapter;
    private ArrayList<Category> categoryArrayList; //= new ArrayList<Category>();

    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Post a job");

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        //check for login status if user
        session.checkLogin();

        /*
        if (!session.isLoggedIn()) {
            logoutUser();
        }
        */

        //we use this to associate the lifecycle observer(MyApplication) with this class(lifecycle owner)
        //it'll help us know when the app is in back ground or foreground
        //getLifecycle().addObserver(new MyApplication());
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new MyApplication());

        /**
         * Broadcast receiver calls in two scenarios
         * 1. fcm registration is completed
         * 2. when new push notification is received
         * */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.e(TAG, "Broadcast receiver action: "+intent.getAction());
                // checking for type intent filter
                if (Config.REGISTRATION_COMPLETE.equals(intent.getAction())) {
                    // fcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    subscribeToGlobalTopic();
                }else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    Log.e(TAG, "Push notification: "+message);
                }
            }
        };

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = findViewById(R.id.recyclerview_categories);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(this, 2);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * PostJobActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mCategoriesAdapter = new CategoriesAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mCategoriesAdapter);

        //view model set up
        /*
        Use ViewModelProviders to associate your ViewModel with your UI controller.
        When your app first starts, the ViewModelProviders will create the ViewModel.
        When the activity is destroyed, for example through a configuration change,
        the ViewModel persists. When the activity is re-created, the ViewModelProviders
        return the existing ViewModel
         */
        HomeViewModelFactory factory = InjectorUtils.provideMainActivityViewModelFactory(this.getApplicationContext());
        //mViewModel = ViewModelProviders.of
          //      (this, factory).get(HomeActivityViewModel.class);
        // With ViewModelFactory
        mViewModel = new ViewModelProvider(this, factory).get(HomeActivityViewModel.class);

        mViewModel.getAllCategories().observe(this, categoryList -> {
            mCategoriesAdapter.swapForecast(categoryList);

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);

            // Show the weather list or the loading screen based on whether the forecast data exists
            // and is loaded
            if (categoryList != null && categoryList.size() != 0) showCategoryDataView();
            else showLoading();
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        navDrawerFragmentContainer = findViewById(R.id.nav_drawer_fragments_container);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Here in onResume() method we are registering the broadcast receivers.
    // So that this activity gets the push messages and registration id
    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        MyNotificationManager.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * This method is for responding to clicks from our grid list.
     *
     * @param categoryId category ID
     */
    @Override
    public void onItemClick(int categoryId, String categoryName) {
        Intent intent = new Intent(HomeActivity.this, PostJobActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("category_id", categoryId);
        intent.putExtra("category_name", categoryName);
        //Toast.makeText(this, "cat ID = "+ categoryId, Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showCategoryDataView() {
        // First, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // Finally, make sure the category data is visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the loading indicator visible and hide the category View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        // Then, hide the category data
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Finally, show the loading indicator
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        myNotificationManager = new MyNotificationManager(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myNotificationManager.showNotificationMessage(title, message, timeStamp, intent);
    }

    // subscribing to global topic
    private void subscribeToGlobalTopic() {
        Log.e(TAG, "Subscribing to global topic");
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(HomeActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
// [END subscribe_topics]
    }

    @Override
    public void onDashboardInteraction(Uri uri) {

    }

    //callback from MyJobsFragment, launch activity to display job selected details
    @Override
    public void onMyjobsInteraction(int jobID, String jobName, String userRole) {
            Intent intent = new Intent(this, JobDetailsActivity.class);
            intent.putExtra("jobID", jobID);
            intent.putExtra("jobName", jobName);
            startActivity(intent);
    }

    @Override
    public void onPaymentHistoryInteraction(Uri uri) {

    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //this method displays the screen/fragment the user has selected using the navigation bar
    //its called in the onNavigationItemSelected() method
    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;
        Intent intent;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_poster_offers_accepted:
                intent = new Intent(this, PosterOffersListActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("offerType", "accepted");
                startActivity(intent);
                break;

            case R.id.nav_poster_offers_received:
                intent = new Intent(this, PosterOffersListActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("offerType", "received");
                startActivity(intent);
                break;

            case R.id.poster_my_jobs:
                intent = new Intent(this, MyJobsListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                break;

            case R.id.nav_fixer_offers_accepted:
                intent = new Intent(this, FixerOffersListActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("offerType", "accepted");
                startActivity(intent);
                break;

            case R.id.nav_fixer_offers_made:
                intent = new Intent(this, FixerOffersListActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("offerType", "made");
                startActivity(intent);
                break;

            case R.id.browse_jobs:
                intent = new Intent(this, BrowseJobsActivity.class);
                startActivity(intent);
                break;

            case R.id.my_profile:
                intent = new Intent(this, MyProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                break;

            case R.id.dashboard:
                navDrawerFragmentContainer.setVisibility(View.VISIBLE);
                fragment = new DashboardFragment();
                break;

            case R.id.payment_history:
                navDrawerFragmentContainer.setVisibility(View.VISIBLE);
                //fragment = new PaymentHistoryFragment(userId, "fixer");
                break;

            case R.id.settings:

                break;
        }
        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_drawer_fragments_container, fragment);
            ft.commit();
        }

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(id);
        return true;
    }

    //check for google play services in device
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
                Log.e(TAG, "Google play services check result code = "+resultCode);
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);
        MyApplication.getInstance().logout();
        session.logoutUser();
        mViewModel.delete();
    }
}
