package com.emtech.fixr.presentation.ui.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.emtech.fixr.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VerifyPhoneNumberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerifyPhoneNumberFragment extends Fragment {
    private static final String TAG = VerifyPhoneNumberFragment.class.getSimpleName();
    private static final String USER_ID = "user_id";
    private static final String OTC = "otc";
    private int mUserId;
    private String mOtc;
    private Button verifyButton;
    private TextInputEditText otcET;
    private OnVerifyPhoneInteractionListener mListener;

    public VerifyPhoneNumberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId Parameter 1.
     * @param otc Parameter 2.
     * @return A new instance of fragment VerifyPhoneNumberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VerifyPhoneNumberFragment newInstance(int userId, String otc) {
        VerifyPhoneNumberFragment fragment = new VerifyPhoneNumberFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        args.putString(OTC, otc);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getInt(USER_ID);
            mOtc = getArguments().getString(OTC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verify_phone_number, container, false);

        //init the view widgets
        getAllWidgets(view);

        //if network is available, go ahead and get the otp then send it to the activity
        if (isNetworkAvailable())
        onVerifyBtnClick();

        return view;
    }

    private void getAllWidgets(View view){
        verifyButton = view.findViewById(R.id.btnVerify);
        otcET = view.findViewById(R.id.otpET);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVerifyPhoneInteractionListener) {
            mListener = (OnVerifyPhoneInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyPhoneInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnVerifyPhoneInteractionListener {
        void onVerifyPhoneInteraction(int user_id, String otc);
    }

    private void getOtc(){
        String otc = otcET.getText().toString().trim();
        if (TextUtils.isEmpty(otc)) {
            otcET.setError("Please enter the code received");
        }

        if (!otc.isEmpty()){
            Log.e(TAG,"OTP received is "+otc);
            mListener.onVerifyPhoneInteraction(mUserId, otc);
        }
    }

    private void onVerifyBtnClick(){
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOtc();
            }
        });
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}