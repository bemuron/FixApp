package com.emtech.fixr.presentation.ui.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.helpers.CircleTransform;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RateFixerFragment.OnRateFixerInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RateFixerFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * this class is shown to the user to rate the tutor
 */
public class RateFixerFragment extends Fragment {
    private static final String TAG = RateFixerFragment.class.getSimpleName();
    private static final String JOB_ID = "job_id";
    private static final String POSTER_ID = "poster_id";
    private static final String FIXER_ID = "fixer_id";
    private static final String FIXER_PROF_PIC = "fixer_prof_pic";
    private static final String FIXER_NAME = "fixer_name";
    private int job_id, poster_id, fixer_id;
    private float fixer_rating;
    private String fixerName, fixerProfPic;
    private RatingBar fixerRatingBar;
    private Button submitFixerRatingButton;
    private TextView fixerNameTv, rateFixerInstruction;
    private ImageView fixerIcon;
    private TextInputEditText rateFixerEditText;

    private OnRateFixerInteractionListener mListener;

    public RateFixerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param job_id Parameter 1: id of the job.
     * @param poster_id Parameter 2: id of the poster.
     * @return A new instance of fragment RateUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RateFixerFragment newInstance(int job_id, int poster_id, int fixer_id, String fixerProfPic, String fixerName) {
        RateFixerFragment fragment = new RateFixerFragment();
        Bundle args = new Bundle();
        args.putInt(JOB_ID, job_id);
        args.putInt(POSTER_ID, poster_id);
        args.putInt(FIXER_ID, fixer_id);
        args.putString(FIXER_PROF_PIC, fixerProfPic);
        args.putString(FIXER_NAME, fixerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            job_id = getArguments().getInt(JOB_ID);
            poster_id = getArguments().getInt(POSTER_ID);
            fixer_id = getArguments().getInt(FIXER_ID);
            fixerProfPic = getArguments().getString(FIXER_PROF_PIC);
            fixerName = getArguments().getString(FIXER_NAME);
        }

        //set the name of this fragment in the toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Rate Fixer");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_fixer, container,false);

        getAllWidgets(view);

        //first check if we have an internet connection
        if (isNetworkAvailable()) {
            if (job_id > 0) {
                //retrofit call to get the fixer details
                //getConfirmedRequestDetails(job_id);

            }
        }
        return view;
    }

    private void getAllWidgets(View view){
        fixerNameTv = view.findViewById(R.id.rate_fixer_name);
        fixerNameTv.setText(fixerName);
        submitFixerRatingButton = view.findViewById(R.id.submitFixerRatingButton);
        fixerIcon = view.findViewById(R.id.rate_fixer_icon);
        rateFixerInstruction = view.findViewById(R.id.rate_fixer_instruction);
        rateFixerInstruction.setText(R.string.rate_fixer_instruction);
        fixerRatingBar = view.findViewById(R.id.fixerRatingBar);
        rateFixerEditText = view.findViewById(R.id.edit_text_rate_fixer_comment);

        //get fixer profile pic
        try {
            Glide.with(this)
                    .load("http://www.emtechint.com/fixapp/assets/images/profile_pics/" + fixerProfPic)
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(new RequestOptions().fitCenter()
                            .transform(new CircleTransform(getActivity())).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(fixerIcon);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        handleFixerRating();
        handleSubmitRating();
    }

    //handles click on rating submit button
    private void handleSubmitRating(){
        submitFixerRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (fixer_rating != 0) {
                        //get the comments
                        String posterComment = Objects.requireNonNull(rateFixerEditText.getText()).toString().trim();
                        //take to RatingActivity which then posts to the db
                        if (mListener != null) {
                            mListener.onRateFixerInteraction(job_id, poster_id, fixer_id, fixer_rating, posterComment);
                        }
                    }
                }catch (Exception e){
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRateFixerInteractionListener) {
            mListener = (OnRateFixerInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRateFixerInteractionListener");
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
    public interface OnRateFixerInteractionListener {
        void onRateFixerInteraction(int job_id, int poster_id, int fixer_id, float fixer_rating, String comment);
    }

    //handle the rating
    private void handleFixerRating(){
        fixerRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                fixer_rating = ratingBar.getRating();
                Log.e(TAG, "fixer rating = "+fixer_rating);
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
