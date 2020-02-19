package com.emtech.fixr.presentation.ui.fragment;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.presentation.adapters.MyJobsListAdapter;
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
 * Use the {@link MyJobsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyJobsFragment extends Fragment implements MyJobsListAdapter.MyJobsListAdapterListener,
        AdapterView.OnItemSelectedListener{
    private static final String LOG_TAG = MyJobsFragment.class.getSimpleName();
    private static final String USER_ID = "userId";
    private RecyclerView recyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private List<Job> jobList = new ArrayList<Job>();
    private MyJobsActivityViewModel mViewModel;
    private Job job;
    private MyJobsListAdapter jobsAdapter;
    private int mUserId;
    private TextView emptyView;
    private Spinner jobsFilterSpinner;
    private int statusJobDisplay;
    private ProgressBar progressBar;

    private OnMyJobsInteractionListener mListener;

    public MyJobsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId This user's ID.
     * @return A new instance of fragment BrowseJobsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyJobsFragment newInstance(int userId) {
        MyJobsFragment fragment = new MyJobsFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUserId = getArguments().getInt(USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_jobs, container, false);

        getAllWidgets(view);
        setAdapter();
        setSpinnerAdapter();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MyJobsViewModelFactory factory = InjectorUtils.provideMyJobsViewModelFactory(getActivity().getApplicationContext());
        mViewModel = ViewModelProviders.of
                (this, factory).get(MyJobsActivityViewModel.class);

        mViewModel.getAllJobsForUser(mUserId).observe(this, userJobsList -> {
            jobList = userJobsList;
            jobsAdapter.setList(userJobsList);
            Log.e(LOG_TAG, "Jobs list size is " +jobList.size());

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("My Jobs");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMyJobsInteractionListener) {
            mListener = (OnMyJobsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MakeOfferDialogListener");
        }
    }

    public void getAllWidgets(View view){
        // Progress bar
        progressBar = view.findViewById(R.id.my_jobs_progress_bar);
        showBar();

        recyclerView = view.findViewById(R.id.my_jobs_recycler_view);
        emptyView = view.findViewById(R.id.empty_jobs_list_view);
        jobsFilterSpinner = view.findViewById(R.id.spinner_filter_jobs);
        jobsFilterSpinner.setOnItemSelectedListener(this);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        jobsAdapter = new MyJobsListAdapter(getActivity(), jobList,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(jobsAdapter);
    }

    //setting up jobs spinner adapter
    public void setSpinnerAdapter() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> jobsFilterAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.jobs_filter_array, android.R.layout.simple_spinner_item);
        jobsFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobsFilterSpinner.setAdapter(jobsFilterAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onJobRowClicked(int position) {
        Job job = jobList.get(position);
        jobList.set(position, job);
        if (mListener != null) {
            //master detail flow callback
            //send to the parent activity then call the activity to display details
            mListener.onMyjobsInteraction(job.getJob_id(), job.getName());
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if ("All".equals(parent.getItemAtPosition(position))) {
            showBar();
            loadSelectedJobs(5);
            Log.e(LOG_TAG, "Displaying all jobs");
        }else if ("Draft".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 0;
            loadSelectedJobs(0);
            Log.e(LOG_TAG, "Draft jobs selected");
        }else if ("Posted".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 1;
            loadSelectedJobs(1);
            Log.e(LOG_TAG, "Posted jobs selected");
        }else if ("Assigned".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 2;
            loadSelectedJobs(2);
            Log.e(LOG_TAG, "Assigned jobs selected");
        }else if ("Offers".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 3;
            loadSelectedJobs(3);
            Log.e(LOG_TAG, "Offered jobs selected");
        }else if ("Completed".equals(parent.getItemAtPosition(position))){
            showBar();
            statusJobDisplay = 4;
            loadSelectedJobs(4);
            Log.e(LOG_TAG, "Completed jobs selected");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //method to load the jobs selected from the spinner dropdown
    private void loadSelectedJobs(int status){
        if (status == 5) {
            mViewModel.getAllJobsForUser(mUserId).observe(this, userJobsList -> {
                jobList = userJobsList;
                jobsAdapter.setList(userJobsList);
                Log.e(LOG_TAG, "Jobs list size is " + jobList.size());
                hideBar();

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
            });
        }else {
            mViewModel.getAllJobsByStatus(mUserId, status).observe(this, userJobsByStatus -> {
                jobsAdapter.setList(userJobsByStatus);
                Log.e(LOG_TAG, "Jobs list size is " +jobList.size());

                if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                recyclerView.smoothScrollToPosition(mPosition);
                hideBar();
            });
        }
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
        void onMyjobsInteraction(int jobID, String jobName);
    }
}
