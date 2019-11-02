package com.emtech.fixr.presentation.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    // TODO: Rename and change types and number of parameters
    public static PostJobDateFragment newInstance() {
        PostJobDateFragment fragment = new PostJobDateFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_job_date, container, false);

        //inflate the views
        setUpWidgetViews(view);

        //handle checkbox changes
        onCheckboxClicked(view);
        return view;
    }

    //setup the views
    private void setUpWidgetViews(View view){
        jobDateEt = view.findViewById(R.id.job_date_edit_text);
        setUpCalendar(); //set up the calendar
        jobDateEt.setOnClickListener(this); //when clicked should show a calendar

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
                morningCheck.setVisibility(View.INVISIBLE);
                middayCheck.setVisibility(View.INVISIBLE);
                afternoonCheck.setVisibility(View.INVISIBLE);
                eveningCheck.setVisibility(View.INVISIBLE);
                isSwitchOn = false;
            }

        });
        morningCheck = view.findViewById(R.id.checkbox_morning_time);
        middayCheck = view.findViewById(R.id.checkbox_midday_time);
        afternoonCheck = view.findViewById(R.id.checkbox_afternoon_time);
        eveningCheck = view.findViewById(R.id.checkbox_evening_time);
        continueButton = view.findViewById(R.id.continue_two);
        continueButton.setEnabled(false);
        continueButton.setOnClickListener(this);
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

    //get changes on the time checkboxes
    public void onCheckboxClicked(View view){
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()){
            case R.id.checkbox_morning_time:
                if (checked){
                    morningTimeSelected = getResources().getString(R.string.check_box_morning);
                }
                break;
            case R.id.checkbox_midday_time:
                if (checked){
                    middaySelected = getResources().getString(R.string.check_box_midday);
                }
                break;
            case R.id.checkbox_afternoon_time:
                if (checked){
                    afternoonSelected = getResources().getString(R.string.check_box_afternoon);
                }
                break;
            case R.id.checkbox_evening_time:
                if (checked){
                    eveningSelected = getResources().getString(R.string.check_box_evening);
                }
                break;
        }
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
        switch (view.getId()) {
            case R.id.continue_two:
                //send the data to PostJobActivity to be posted to the server
                getUserInput();
                break;

            case R.id.job_date_edit_text:
                //send the data to PostJobActivity to be posted to the server
                mDatePickerDialog.show();
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
}
