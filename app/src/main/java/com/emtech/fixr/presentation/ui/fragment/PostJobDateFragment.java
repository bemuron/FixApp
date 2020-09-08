package com.emtech.fixr.presentation.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostJobDateFragment.OnJobDateFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostJobDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostJobDateFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = PostJobDateFragment.class.getSimpleName();
    private static final String JOB_ID = "job_id";
    private static final String JOB_DATE = "job_date";
    private static final String JOB_TIME = "job_time";
    private EditText jobDateEt;
    private Switch jobAtSepcificTimeSwicth;
    private CheckBox morningCheck, middayCheck, afternoonCheck, eveningCheck;
    private Button continueButton;
    private Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    private DatePickerDialog mDatePickerDialog;
    private String userTimeSelected,
            morningTimeSelected, middaySelected, afternoonSelected, eveningSelected;
    private boolean isSwitchOn = false;

    private int mJobId;
    private String mJobDate, mJobTime;

    private OnJobDateFragmentInteractionListener mListener;

    public PostJobDateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostJobDateFragment.
     */
    public static PostJobDateFragment newInstance(int jobId, String jobDate, String jobTime) {
        PostJobDateFragment fragment = new PostJobDateFragment();
        Bundle args = new Bundle();
        args.putInt(JOB_ID, jobId);
        args.putString(JOB_DATE, jobDate);
        args.putString(JOB_TIME, jobTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mJobId = bundle.getInt(JOB_ID);
            mJobDate = bundle.getString(JOB_DATE);
            mJobTime = bundle.getString(JOB_TIME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_job_date, container, false);

        //set up the calendar
        setUpCalendar();

        //inflate the views
        setUpWidgetViews(view);
        //if we have a job id then the user is editing a job
        if (mJobId > 0){
            inflateViews();
        }

        return view;
    }

    //if we have a job id then the user is just editing a job
    private void inflateViews(){

        //handle the job time
        try {
            if (mJobDate != null){
                //when the job is expected to be done
                formatDate();
                Log.e(TAG, "job date to be edited is "+ mJobDate);
            }

            if (mJobTime != null) {
                //if the user selected specific time then switch on the switch
                jobAtSepcificTimeSwicth.setChecked(true);
                morningCheck.setVisibility(View.VISIBLE);
                middayCheck.setVisibility(View.VISIBLE);
                afternoonCheck.setVisibility(View.VISIBLE);
                eveningCheck.setVisibility(View.VISIBLE);
                isSwitchOn = true;

                String[] jobTime = mJobTime.trim().split("\\s*,\\s*");

                for (String s : jobTime) {
                    System.out.println(s);
                    if (s.equals(getResources().getString(R.string.check_box_morning))) {
                        morningCheck.setChecked(true);
                    } else if (s.equals(getResources().getString(R.string.check_box_midday))) {
                        middayCheck.setChecked(true);
                    } else if (s.equals(getResources().getString(R.string.check_box_afternoon))) {
                        afternoonCheck.setChecked(true);
                    } else if (s.equals(getResources().getString(R.string.check_box_evening))) {
                        eveningCheck.setChecked(true);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

    }

    //change the date received from mysql to a more eye pleasing one
    private void formatDate(){
        String date_of_job = null;

        SimpleDateFormat mysqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        try{
            Date d = mysqlDateFormat.parse(mJobDate);
            date_of_job = sdf.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        jobDateEt.setText(date_of_job);
    }

    //setup the views
    private void setUpWidgetViews(View view){
        jobDateEt = view.findViewById(R.id.job_date_edit_text);
        //prevent the system keyboard from showing up when the edit text is clicked
        jobDateEt.setRawInputType(InputType.TYPE_CLASS_TEXT);
        jobDateEt.setOnClickListener(this); //when clicked should show a calendar

        morningCheck = view.findViewById(R.id.checkbox_morning_time);
        morningCheck.setOnClickListener(this);
        middayCheck = view.findViewById(R.id.checkbox_midday_time);
        middayCheck.setOnClickListener(this);
        afternoonCheck = view.findViewById(R.id.checkbox_afternoon_time);
        afternoonCheck.setOnClickListener(this);
        eveningCheck = view.findViewById(R.id.checkbox_evening_time);
        eveningCheck.setOnClickListener(this);
        continueButton = view.findViewById(R.id.continue_two);
        continueButton.setEnabled(false);
        continueButton.setOnClickListener(this);

        //when on, show the other checkbox options for time
        jobAtSepcificTimeSwicth = view.findViewById(R.id.switch_time_needed);
        //set the switch change listener
        jobAtSepcificTimeSwicth.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                morningCheck.setVisibility(View.VISIBLE);
                middayCheck.setVisibility(View.VISIBLE);
                afternoonCheck.setVisibility(View.VISIBLE);
                eveningCheck.setVisibility(View.VISIBLE);
                isSwitchOn = true;

            } else {
                morningCheck.setVisibility(View.GONE);
                middayCheck.setVisibility(View.GONE);
                afternoonCheck.setVisibility(View.GONE);
                eveningCheck.setVisibility(View.GONE);
                isSwitchOn = false;
            }

        });
    }

    //set up the calendar to capture the user input job date
    private void setUpCalendar() {
        mDatePickerDialog = new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(year, monthOfYear, dayOfMonth);
            updateLabel();
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    //update the edit text with the selected date
    private void updateLabel(){
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        //update the edit text view with the time selected
        jobDateEt.setText(sdf.format(myCalendar.getTime()));
        continueButton.setEnabled(true);
    }

    //method to get what user has filled in
    private void getUserInput(){
        StringBuilder s = new StringBuilder(100);
        //make sure a date is selected
        String jobDate = jobDateEt.getText().toString().trim();
        if (TextUtils.isEmpty(jobDate)) {
            jobDateEt.setError("Job Date is required");
        }
        if (isSwitchOn){
            if (morningTimeSelected != null){
                s.append(morningTimeSelected);
                s.append(", ");
                userTimeSelected = s.toString();
            }
            if (middaySelected != null){
                s.append(middaySelected);
                s.append(", ");
                userTimeSelected = s.toString();
            }
            if (afternoonSelected != null){
                s.append(afternoonSelected);
                s.append(", ");
                userTimeSelected = s.toString();
            }
            if (eveningSelected != null){
                s.append(eveningSelected);
                s.append(", ");
                userTimeSelected = s.toString();
            }

        }
        if (!jobDate.isEmpty() && isSwitchOn) {
            if (mListener != null) {
                //send data to parent activity to be posted to the server
                mListener.onJobDateFragmentInteraction(jobDate, userTimeSelected);
            }
        }else if (!jobDate.isEmpty()){
            //send data to parent activity to be posted to the server
            mListener.onJobDateFragmentInteraction(jobDate, null);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnJobDateFragmentInteractionListener) {
            mListener = (OnJobDateFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnJobDateFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //handle clicks on the views
    @Override
    public void onClick(View view) {
        // Is the view now checked?
        //boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.continue_two:
                //check for internet connectivity
                if (isNetworkAvailable()) {
                    //send the data to PostJobActivity to be posted to the server
                    getUserInput();
                }else{
                    Toast.makeText(getActivity(),"Try checking your internet connection",
                            Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.job_date_edit_text:
                //send the data to PostJobActivity to be posted to the server
                mDatePickerDialog.show();
                break;

            case R.id.checkbox_morning_time:
                boolean checked = ((CheckBox) view).isChecked();
                if (checked){
                    morningTimeSelected = getResources().getString(R.string.check_box_morning);
                }
                break;
            case R.id.checkbox_midday_time:
                boolean checked2 = ((CheckBox) view).isChecked();
                if (checked2){
                    middaySelected = getResources().getString(R.string.check_box_midday);
                }
                break;
            case R.id.checkbox_afternoon_time:
                boolean checked3 = ((CheckBox) view).isChecked();
                if (checked3){
                    afternoonSelected = getResources().getString(R.string.check_box_afternoon);
                }
                break;
            case R.id.checkbox_evening_time:
                boolean checked4 = ((CheckBox) view).isChecked();
                if (checked4){
                    eveningSelected = getResources().getString(R.string.check_box_evening);
                }
                break;
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
    public interface OnJobDateFragmentInteractionListener{
        // TODO: Update argument type and name
        void onJobDateFragmentInteraction(String date, String timeOfDay);
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
