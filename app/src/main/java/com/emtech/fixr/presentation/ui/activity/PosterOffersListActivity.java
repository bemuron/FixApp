package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.adapters.OffersListAdapter;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class PosterOffersListActivity extends AppCompatActivity implements
        OffersListAdapter.OffersListAdapterListener {
    private static final String LOG_TAG = PosterOffersListActivity.class.getSimpleName();
    private static final String USER_ID = "userId";
    private static final String OFFER_TYPE = "offerType";
    private RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private List<Offer> offerList = new ArrayList<Offer>();
    private MyJobsActivityViewModel mViewModel;
    private OffersListAdapter offersAdapter;
    private int mUserId;
    private String mOfferType;
    private TextView emptyView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster_offers_list);
        setupActionBar();

        if (!isNetworkAvailable(this)){
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
        }

        getAllWidgets();

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, factory).get(MyJobsActivityViewModel.class);

        //get the user id sent by the home activity
        mUserId = getIntent().getIntExtra(USER_ID, 0);
        mOfferType = getIntent().getStringExtra(OFFER_TYPE);

        setAdapter();

        if (mOfferType != null && mOfferType.equals("received")) {
            setTitle(R.string.title_activity_poster_offers_received);
            mViewModel.getAllOffersReceived(mUserId).observe(this, offersReceived -> {
                hideBar();
                offerList = offersReceived;
                offersAdapter.setList(offerList);

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);

                if (offerList == null || offerList.size() == 0) {
                    emptyView.setText(R.string.empty_poster_offers_received_list);
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }else{
                    Log.e(LOG_TAG, "offers to jobs for poster list size is " + offerList.size());
                }
            });
        }else if (mOfferType != null && mOfferType.equals("accepted")) {
            setTitle(R.string.title_activity_poster_offers_accepted);
            mViewModel.getAllOffersAccepted(mUserId).observe(this, offersAccepted -> {
                hideBar();
                offerList = offersAccepted;
                offersAdapter.setList(offerList);

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);

                if (offerList == null || offerList.size() == 0) {
                    emptyView.setText(R.string.empty_poster_offers_accepted_list);
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }else {
                    Log.e(LOG_TAG, "offers accepted by poster list size is " + offerList.size());
                }
            });
        }
    }

    //onResume is called when the activity is relaunched again from the back stack
    @Override
    public void onResume(){
        super.onResume();
        showBar();

    }

    @Override
    public void onPause(){
        super.onPause();
        //first clear the previous list
        clearData();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //set up the view widgets
    private void getAllWidgets(){
        // Progress bar
        progressBar = findViewById(R.id.poster_offers_progress_bar);
        showBar();

        recyclerView = findViewById(R.id.poster_offers_list_recycler_view);
        emptyView = findViewById(R.id.posterOffers_empty_list_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        offersAdapter = new OffersListAdapter(PosterOffersListActivity.this, offerList,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(offersAdapter);
    }

    //method to clear the previous list if it exists
    private void clearData(){
        if(offerList != null){
            offerList.clear(); // clear list
        }
        if (offersAdapter != null) {
            offersAdapter.notifyDataSetChanged(); // let your adapter know about the changes and reload view.
        }
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        /*try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }*/
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        /*try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }*/
    }

    @Override
    public void onOfferRowClicked(String jobName, int position) {
        Offer offer = offerList.get(position);
        offerList.set(position, offer);
        if (mOfferType.equals("received")) {
            Log.e(LOG_TAG, "Offer clicked ID = " + offer.getOffer_id());
            Intent intent = new Intent(PosterOffersListActivity.this,
                    OfferReceivedDetailsForPosterActivity.class);
            intent.putExtra("offerID", offer.getOffer_id());
            intent.putExtra("jobName", jobName);
            startActivity(intent);
        }else if (mOfferType.equals("accepted")) {
            Log.e(LOG_TAG, "Offer clicked ID = " + offer.getOffer_id());
            Intent intent = new Intent(PosterOffersListActivity.this,
                    OfferAcceptedDetailsForPosterActivity.class);
            intent.putExtra("offerID", offer.getOffer_id());
            intent.putExtra("jobName", jobName);
            startActivity(intent);
        }
    }

    //method to check for internet connection
    public static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }

            else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        return false;
    }
}
