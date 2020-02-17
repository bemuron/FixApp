package com.emtech.fixr.presentation.ui.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.emtech.fixr.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostJobBudgetFragment.OnJobBudgetFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostJobBudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostJobBudgetFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{
    private static final String TAG = PostJobBudgetFragment.class.getSimpleName();
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RadioGroup budgetTypeRadioGroup;
    private RadioButton totalBudgetRadioButton, hourlyRadioButton;
    private TextView totalBudgetTv,perHourTv, totHrsTv, estTotBudgetTv;
    private EditText totalBudgetEt, perHourEt, totHrsEt;
    private Button postJobButton;
    private String budgetTypeSelected,totalBudget;
    private int hoursTotal,perHourPrice;
    private boolean isTotalBudgetChecked = false, isHourlyChecked = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private OnJobBudgetFragmentInteractionListener mListener;

    public PostJobBudgetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostJobBudgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostJobBudgetFragment newInstance() {
        PostJobBudgetFragment fragment = new PostJobBudgetFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
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
        View view = inflater.inflate(R.layout.fragment_post_job_budget, container, false);

        //inflate the views
        setUpWidgetViews(view);

        realTimePerHrPriceEtChange();
        realTimeTotHrsEtChange();
        realTimeTotBudgetEtChange();

        return view;
    }

    //set up the view widgets
    public void setUpWidgetViews(View view){
        totalBudgetRadioButton = view.findViewById(R.id.job_budget_total_radio_button);
        hourlyRadioButton = view.findViewById(R.id.job_budget_hourly_radio_button);
        budgetTypeRadioGroup = view.findViewById(R.id.job_budget_radio_group);
        budgetTypeRadioGroup.setOnCheckedChangeListener(this);
        totalBudgetTv = view.findViewById(R.id.total_budget_text_view);
        totalBudgetEt = view.findViewById(R.id.total_budget_edit_text);
        totHrsEt = view.findViewById(R.id.total_hours_edit_text);
        perHourEt = view.findViewById(R.id.price_per_hr_edit_text);
        perHourTv = view.findViewById(R.id.title_price_per_hr_text_view);
        totHrsTv = view.findViewById(R.id.title_hours_text_view);
        estTotBudgetTv = view.findViewById(R.id.summary_budget_value);
        postJobButton = view.findViewById(R.id.post_job_button);
        postJobButton.setEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnJobBudgetFragmentInteractionListener) {
            mListener = (OnJobBudgetFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnJobBudgetFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //handle the radio button selected: either total or hourly
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = radioGroup.findViewById(radioButtonId);

        budgetTypeSelected = (String)radioButton.getText();
        Log.i(TAG, "radio selected = "+budgetTypeSelected);
        postJobButton.setEnabled(true);

        //if total budget is selected, hide hourly and vice verse
        if (budgetTypeSelected.equals("Total")){
            isTotalBudgetChecked = true;
            totalBudgetEt.setVisibility(View.VISIBLE);
            totalBudgetTv.setVisibility(View.VISIBLE);
            totHrsEt.setVisibility(View.GONE);
            perHourEt.setVisibility(View.GONE);
            perHourTv.setVisibility(View.GONE);
            totHrsTv.setVisibility(View.GONE);
            onButtonClickedOnTotalBudget();
            realTimeTotBudgetEtChange();
            estTotBudgetTv.setText(totalBudgetEt.getText().toString().trim());

        }else if (budgetTypeSelected.equals("Hourly rate")){
            isHourlyChecked = true;
            totHrsEt.setVisibility(View.VISIBLE);
            perHourEt.setVisibility(View.VISIBLE);
            perHourTv.setVisibility(View.VISIBLE);
            totHrsTv.setVisibility(View.VISIBLE);
            totalBudgetEt.setVisibility(View.GONE);
            totalBudgetTv.setVisibility(View.GONE);
            onButtonClickedOnHourlyBudget();
            realTimeTotHrsEtChange();
            realTimeTotBudgetEtChange();
            calculateTotalBudget(perHourPrice, hoursTotal);
        }
    }

    //handle click on the post job button when the user has selected total budget option
    //it will mainly transfer the input to the parent activity to be posted to
    // the server
    private void onButtonClickedOnTotalBudget(){
        postJobButton.setOnClickListener(view -> {
            //check for internet connectivity
            if (isNetworkAvailable()) {
                //make sure total hours have been entered
                totalBudget = totalBudgetEt.getText().toString().trim();
                if (TextUtils.isEmpty(totalBudget)) {
                    totalBudgetEt.setError("Total budget is required");
                }
                if (!totalBudget.isEmpty()){

                    mListener.onJobBudgetFragmentInteraction(totalBudget,
                            estTotBudgetTv.getText().toString(),"0","0");
                }
            }else{
                Toast.makeText(getActivity(),"Try checking your internet connection",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    //handle click on the post job button when the user has selected hourly budget option
    //it will mainly transfer the input to the parent activity to be posted to
    // the server
    private void onButtonClickedOnHourlyBudget(){
        postJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check for internet connectivity
                if (isNetworkAvailable()) {
                    //make sure price per hour have been entered
                    String pricePerHour = perHourEt.getText().toString().trim();
                    if (TextUtils.isEmpty(pricePerHour)) {
                        perHourEt.setError("Price per hour is required");
                    }
                    //make sure total hours have been entered
                    String totalHours = totHrsEt.getText().toString().trim();
                    if (TextUtils.isEmpty(totalHours)) {
                        totHrsEt.setError("Total hours required");
                    }
                    if (!pricePerHour.isEmpty() && !totalHours.isEmpty()){
                        //if all is ok, send the input to the parent activity
                        mListener.onJobBudgetFragmentInteraction("0",
                                estTotBudgetTv.getText().toString(),pricePerHour,totalHours);
                    }
                }else{
                    Toast.makeText(getActivity(),"Try checking your internet connection",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //calculate total budget based on the price per hour and the total hours
    private void calculateTotalBudget(int perHrPrice, int totalHrs){
            int estTotBudget = totalHrs * perHrPrice;
            estTotBudgetTv.setText("UGX." + estTotBudget);
    }

    private void realTimeTotBudgetEtChange(){
        totalBudgetEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count > 0) {
                    estTotBudgetTv.setText("UGX." + s);
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    estTotBudgetTv.setText("UGX." + s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                    estTotBudgetTv.setText("UGX." + s);

            }
        });
    }

    //handle input on the per hour price edit text
    //display it in the summarytext view after calculating the total price
    private void realTimePerHrPriceEtChange(){
        perHourEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    perHourPrice = Integer.parseInt(s.toString());
                    calculateTotalBudget(perHourPrice, hoursTotal);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void realTimeTotHrsEtChange(){
        totHrsEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    hoursTotal = Integer.parseInt(s.toString());
                    calculateTotalBudget(perHourPrice, hoursTotal);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
    public interface OnJobBudgetFragmentInteractionListener {
        void onJobBudgetFragmentInteraction(String totalBudget, String estTotBudget,
                                            String pricePerHr, String totalHrs);
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
