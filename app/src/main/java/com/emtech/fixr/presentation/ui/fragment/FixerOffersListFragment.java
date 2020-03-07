package com.emtech.fixr.presentation.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.models.Offer;
import com.emtech.fixr.presentation.adapters.MyJobsListAdapter;
import com.emtech.fixr.presentation.adapters.OffersListAdapter;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMyJobsInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FixerOffersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FixerOffersListFragment extends Fragment implements OffersListAdapter.OffersListAdapterListener{
    private static final String LOG_TAG = FixerOffersListFragment.class.getSimpleName();
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

    private OnMyJobsInteractionListener mListener;

    public FixerOffersListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId This user's ID.
     * @return A new instance of fragment BrowseJobsFragment.
     */
    public static FixerOffersListFragment newInstance(int userId, String offerType) {
        FixerOffersListFragment fragment = new FixerOffersListFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        args.putString(OFFER_TYPE, offerType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUserId = getArguments().getInt(USER_ID);
            mOfferType = getArguments().getString(OFFER_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_jobs, container, false);

        getAllWidgets(view);
        setAdapter();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(getActivity().getApplicationContext());
        mViewModel = new ViewModelProvider(this, factory).get(MyJobsActivityViewModel.class);

        if (mOfferType.equals("accepted")) {
            //offers accepted for fixer
            mViewModel.getAllOffersAccepted(mUserId).observe(getActivity(), offersAccepted -> {
                offersAdapter.setList(offersAccepted);
                Log.e(LOG_TAG, "offers accepted list size is " + offerList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
            });
        }else if (mOfferType.equals("made")){
            mViewModel.getAllOffersMade(mUserId).observe(getActivity(), offersMadeList -> {
                offersAdapter.setList(offersMadeList);
                Log.e(LOG_TAG, "offers made list size is " +offerList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
                hideBar();
            });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        if (mOfferType.equals("accepted")) {
            getActivity().setTitle("Offers Accepted");
            if (offerList == null || offerList.size() == 0){
                emptyView.setText(R.string.empty_fixer_offers_accepted_list);
            }
        }else if (mOfferType.equals("made")){
            getActivity().setTitle("Offers Made");
            if (offerList == null || offerList.size() == 0){
                emptyView.setText(R.string.empty_fixer_offers_made_list);
            }
        }
    }

    private void getAllWidgets(View view){
        // Progress bar
        progressBar = view.findViewById(R.id.my_jobs_progress_bar);
        showBar();

        recyclerView = view.findViewById(R.id.fixer_offers_list_recycler_view);
        emptyView = view.findViewById(R.id.fixerOffers_empty_list_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        offersAdapter = new OffersListAdapter(getActivity(), offerList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(offersAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onOfferRowClicked(int position) {
        Offer offer = offerList.get(position);
        offerList.set(position, offer);
        if (mListener != null) {
            //master detail flow callback
            //send to the parent activity then call the activity to display details
            mListener.onMyjobsInteraction(offer.getOffer_id(), offer.getName(), "fixer");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnMyJobsInteractionListener {
        void onMyjobsInteraction(int jobID, String jobName, String userRole);
    }
}
