package com.emtech.fixr.presentation.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PaymentSummaryFragment.OnPaymentSummaryListener} interface
 * to handle interaction events.
 * Use the {@link PaymentSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaymentSummaryFragment extends Fragment {
    private static final String TAG = PaymentSummaryFragment.class.getSimpleName();
    private static final String JOB_ID = "job_id";
    private static final String POSTER_ID = "poster_id";
    private static final String JOB_COST = "job_cost";
    private TextView jobCost, serviceCharge, finalTotalCostLarge,
            finalTotalCost, paymentIstruction;
    private Button confirmPaymentButton;
    private SessionManager session;
    private String userRole;
    private int job_id, poster_id, total_cost, job_cost, serviceFee;

    private OnPaymentSummaryListener mListener;

    public PaymentSummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param job_id id of the meeting.
     * @return A new instance of fragment PaymentSummaryFragment.
     */
    // Start fragment passing required arguments
    public static PaymentSummaryFragment newInstance(int job_id, int poster_id, int job_cost) {
        PaymentSummaryFragment fragment = new PaymentSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(JOB_ID, job_id);
        args.putInt(POSTER_ID, poster_id);
        args.putInt(JOB_COST, job_cost);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            job_id = getArguments().getInt(JOB_ID);
            poster_id = getArguments().getInt(POSTER_ID);
            job_cost = getArguments().getInt(JOB_COST);
        }

        //set the name of this fragment in the toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Payment Summary");

        // session manager
        session = new SessionManager(getActivity());
        userRole = session.getUserRole();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment_summary, container, false);

        getAllWidgets(view);

        // Inflate the layout for this fragment
        return view;
    }

    private void getAllWidgets(View view){
        jobCost = view.findViewById(R.id.jobCostValue);
        serviceCharge = view.findViewById(R.id.serviceFeeValue);
        finalTotalCost = view.findViewById(R.id.totalCostValue);
        finalTotalCostLarge = view.findViewById(R.id.totalCostLargeTv);
        paymentIstruction = view.findViewById(R.id.paymentInstructionTv);
        confirmPaymentButton = view.findViewById(R.id.confirmPaymentButton);
        //only show this button if the role of the user is fixer
        if (userRole.equals("poster")) {
            confirmPaymentButton.setVisibility(View.GONE);
            paymentIstruction.setText("Please pay the amount below to the job fixer");
        }else{
            confirmPaymentButton.setVisibility(View.VISIBLE);
            paymentIstruction.setText("Amount to be received from the job owner");
        }

        //calculate the amount the fixer will receive
        //this is where we remove the service fee 15%
        calculatePayment(job_cost);

        handleConfirmPayment();
    }

    //method to handle click on confirm payment button by fixer
    //just take us back to the parent activity which then launches the
    //rate poster/fixer activity
    private void handleConfirmPayment(){
        confirmPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    //retrofit call to update db with confirmation of payment
                    //update job status to completed
                    //save the service fee we deducted and
                    //the final cost received by the fixer
                    //paymentReceived(job_id, poster_id, total_cost);

                    mListener.onPaymentSummaryInteraction(job_id, poster_id);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPaymentSummaryListener) {
            mListener = (OnPaymentSummaryListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPaymentSummaryInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnPaymentSummaryListener {
        // send meeting id to parent activity
        void onPaymentSummaryInteraction(int job_id, int poster_id);
    }

    //this method calculates the amount to be paid to the fixer by deducting
    //the service fee
    private void calculatePayment(int job_cost){
        float serviceFesPercentage = 0.15f;
        Log.e(TAG, "service fee = "+ serviceFesPercentage);
        Log.e(TAG, "job cost = "+ job_cost);

        //calculate the service fee
        //this is what we get to keep
        serviceFee = (int) (serviceFesPercentage * job_cost);

        //calculate the final cost
        //this what the fixer receives
        total_cost = (job_cost - serviceFee);

        Log.e(TAG, "service fee = "+ serviceFee);
        Log.e(TAG, "total cost = "+ total_cost);

        jobCost.setText(job_cost);
        serviceCharge.setText(serviceFee);
        finalTotalCost.setText(total_cost);
        finalTotalCostLarge.setText(total_cost);

    }

    //retrofit call to add session cost to db table
    /*private void paymentReceived(final int meeting_id, int notify_user_id, int price) {
        ProgressDialog pdLoading = new ProgressDialog(getActivity());

        //this method will be running on UI thread
        pdLoading.setMessage("\tWorking...");
        pdLoading.setCancelable(false);
        pdLoading.show();

        //Defining retrofit api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.sessionPaymentReceived(meeting_id, notify_user_id, price);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                pdLoading.dismiss();

                //ResBeanSignup resBean = response.body();
                //String response_status = Encryption.decrypt(resBean.getResponse_status());

                if (!response.body().getError()) {
                    //Log.d(LOG_TAG, EnumAppMessages.REGISTER_SUCCESS_TITLE.getValue());
                    Log.d(TAG, response.body().getMessage());

                    //save to prefs that the payment is received
                    session.spPaymentReceived(meeting_id);

                    //show toast to inform user
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                pdLoading.dismiss();
                //print out any error we may get
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.getMessage());
            }
        });

    }*/
}
