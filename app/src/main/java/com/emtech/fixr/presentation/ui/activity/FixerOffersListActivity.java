package com.emtech.fixr.presentation.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emtech.fixr.R;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.adapters.OffersListAdapter;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class FixerOffersListActivity extends AppCompatActivity implements
        OffersListAdapter.OffersListAdapterListener {
    private static final String LOG_TAG = FixerOffersListActivity.class.getSimpleName();
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
    private int statusJobDisplay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixer_offers_list);
        setupActionBar();

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(this);
        mViewModel = new ViewModelProvider(this, factory).get(MyJobsActivityViewModel.class);

        //get the user id sent by the home activity
        mUserId = getIntent().getIntExtra(USER_ID, 0);
        mOfferType = getIntent().getStringExtra(OFFER_TYPE);

        getAllWidgets();
        setAdapter();
        showBar();

        //first clear the previous list
        offersAdapter.clearData();

        if (mOfferType.equals("accepted")) {
            //set title for this activity
            setTitle("Offers Accepted");
            //offers accepted for fixer
            mViewModel.getAllOffersAccepted(mUserId).observe(this, offersAccepted -> {
                hideBar();
                offerList = offersAccepted;
                offersAdapter.setList(offersAccepted);
                Log.e(LOG_TAG, "offers accepted list size is " + offerList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);

                //if empty show relevant text
                if (offerList == null || offerList.size() == 0){
                    emptyView.setText(R.string.empty_fixer_offers_accepted_list);
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            });
        }else if (mOfferType.equals("made")){
            setTitle("Offers Made");

            mViewModel.getAllOffersMade(mUserId).observe(this, offersMadeList -> {
                hideBar();
                offerList = offersMadeList;
                offersAdapter.setList(offersMadeList);
                Log.e(LOG_TAG, "offers made list size is " +offerList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);

                if (offerList == null || offerList.size() == 0){
                    emptyView.setText(R.string.empty_fixer_offers_made_list);
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getAllWidgets(){
        // Progress bar
        progressBar = findViewById(R.id.fixer_offers_progress_bar);
        showBar();

        recyclerView = findViewById(R.id.fixer_offers_list_recycler_view);
        emptyView = findViewById(R.id.fixerOffers_empty_list_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        recyclerView.setAdapter(null);
        offersAdapter = new OffersListAdapter(this, offerList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(offersAdapter);
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onOfferRowClicked(String jobName, int position) {
        Offer offer = offerList.get(position);
        offerList.set(position, offer);
        Log.e(LOG_TAG, "Fixer Offer clicked ID = "+position);
            Intent intent = new Intent(this, OfferDetailsForFixerActivity.class);
            intent.putExtra("offerID", position);
            intent.putExtra("jobName", jobName);
            startActivity(intent);
    }
}
