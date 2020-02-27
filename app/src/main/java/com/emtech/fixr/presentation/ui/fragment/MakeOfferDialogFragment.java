package com.emtech.fixr.presentation.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.emtech.fixr.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MakeOfferDialogListener} interface
 * to handle interaction events.
 * Use the {@link MakeOfferDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MakeOfferDialogFragment extends DialogFragment {
    // the fragment initialization parameters
    private static final String JOB_NAME = "job_name";
    private static final String JOB_POSTER = "job_poster";
    private String jobName,jobPoster;
    private MakeOfferDialogListener mListener;
    private Toolbar toolbar;
    private TextView jobNameTextView, jobPosterTextView;
    //private EditText messageEditText;
    private TextInputEditText offeredAmountET, messageEditText;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param jobName The title of the job.
     * @param jobPoster Name of the person who posted the job.
     * @return A new instance of fragment MakeOfferDialogFragment.
     */
    public static MakeOfferDialogFragment newInstance(String jobName, String jobPoster) {
        MakeOfferDialogFragment fragment = new MakeOfferDialogFragment();
        Bundle args = new Bundle();
        args.putString(JOB_NAME, jobName);
        args.putString(JOB_POSTER, jobPoster);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
        if (getArguments() != null) {
            jobName = getArguments().getString(JOB_NAME);
            jobPoster = getArguments().getString(JOB_POSTER);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    /*@NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jobName = getArguments().getString(JOB_NAME);
            jobPoster = getArguments().getString(JOB_POSTER);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.content_make_offer_dialog, null))
                // Add action buttons
                .setPositiveButton(R.string.send_offer, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(MakeOfferDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(MakeOfferDialogFragment.this);

                    }
                });
        return builder.create();
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_make_offer_dialog, container, false);

        getAllWidgets(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Make Offer");
        toolbar.inflateMenu(R.menu.make_offer_dialog);
        toolbar.setOnMenuItemClickListener(item -> {
            getOfferDetails();
            dismiss();
            return true;
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MakeOfferDialogListener) {
            mListener = (MakeOfferDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MakeOfferDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getAllWidgets(View view){
        toolbar = view.findViewById(R.id.toolbar);
        jobNameTextView = view.findViewById(R.id.offer_job_name);
        jobNameTextView.setText(jobName);
        jobPosterTextView = view.findViewById(R.id.offer_job_poster);
        jobPosterTextView.setText(jobPoster);
        messageEditText = view.findViewById(R.id.edit_text_message);
        offeredAmountET = view.findViewById(R.id.edit_text_offer);
    }

    //method to get what user has filled in
    public  void getOfferDetails() {
        String offeredAmount = offeredAmountET.getText().toString().trim();
        if (TextUtils.isEmpty(offeredAmount)) {
            offeredAmountET.setError("Job Title is required");
            //return false;
        }

        String offerMessage = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(offerMessage)) {
            messageEditText.setError("Job description is required");
            //return false;
        }

        //convert the amount entered o integer
        int amountOffered = Integer.parseInt(offeredAmount);

        mListener.returnOfferDetails(amountOffered, offerMessage);
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
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MakeOfferDialogListener {
        public void returnOfferDetails(int offeredAmount, String offerMessage);
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
}
