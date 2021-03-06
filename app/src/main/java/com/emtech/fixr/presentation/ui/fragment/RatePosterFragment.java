package com.emtech.fixr.presentation.ui.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

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
 * {@link RatePosterFragment.OnRatePosterListener} interface
 * to handle interaction events.
 * Use the {@link RatePosterFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This class is shown to the tutor to rate the user
 */
public class RatePosterFragment extends Fragment {
    private static final String TAG = RatePosterFragment.class.getSimpleName();
    private static final String JOB_ID = "job_id";
    //ID of the current user in this case a poster
    private static final String POSTER_ID = "poster_id";
    private static final String FIXER_ID = "fixer_id";
    private static final String POSTER_PROF_PIC = "poster_prof_pic";
    private static final String POSTER_NAME = "poster_name";
    private int job_id, poster_id, fixer_id;
    private float posterRating;
    private String posterName, posterProfPic;
    private RatingBar posterRatingBar;
    private Button submitPosterRatingButton;
    private TextView posterNameTv, ratePosterInstruction;
    private ImageView posterIcon;
    private TextInputEditText ratePosterEditText;

    private OnRatePosterListener mListener;

    public RatePosterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param job_id Parameter 1: id of the meeting.
     * @param poster_id Parameter 2: id of the user.
     * @return A new instance of fragment RateUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RatePosterFragment newInstance(int job_id, int fixer_id, int poster_id, String posterProfPic, String posterName) {
        RatePosterFragment fragment = new RatePosterFragment();
        Bundle args = new Bundle();
        args.putInt(JOB_ID, job_id);
        args.putInt(FIXER_ID, fixer_id);
        args.putInt(POSTER_ID, poster_id);
        args.putString(POSTER_PROF_PIC, posterProfPic);
        args.putString(POSTER_NAME, posterName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            job_id = getArguments().getInt(JOB_ID);
            fixer_id = getArguments().getInt(FIXER_ID);
            poster_id = getArguments().getInt(POSTER_ID);
            posterProfPic = getArguments().getString(POSTER_PROF_PIC);
            posterName = getArguments().getString(POSTER_NAME);
        }

        //set the name of this fragment in the toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Rate Poster");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_poster, container,false);

        getAllWidgets(view);

        //first check if we have an internet connection
        if (isNetworkAvailable()) {
            if (job_id > 0) {
                //retrofit call to get the selected pending request details
                //getMeetingDetailsForTutor(job_id);

            }
        }
        return view;
    }

    private void getAllWidgets(View view){
        posterNameTv = view.findViewById(R.id.rate_poster_name);
        posterNameTv.setText(posterName);
        ratePosterInstruction = view.findViewById(R.id.rate_poster_instruction);
        ratePosterInstruction.setText(R.string.rate_poster);
        submitPosterRatingButton = view.findViewById(R.id.submitPosterRatingButton);
        posterIcon = view.findViewById(R.id.rate_poster_icon);
        posterRatingBar = view.findViewById(R.id.posterRatingBar);
        ratePosterEditText = view.findViewById(R.id.edit_text_rate_poster_comment);

        //get poster profile pic
        try {
            Glide.with(this)
                    .load("http://www.emtechint.com/fixapp/assets/images/profile_pics/" + posterProfPic)
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(new RequestOptions().fitCenter()
                            .transform(new CircleTransform(getActivity())).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(posterIcon);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        handlePosterRating();
        handleSubmitRating();
    }

    //handles click on rating submit button
    private void handleSubmitRating(){
        submitPosterRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (posterRating != 0) {
                        //get the comments
                        String fixerComment = Objects.requireNonNull(ratePosterEditText.getText()).toString().trim();
                        //take result to RatingActivity to
                        //send rating to server
                        if (mListener != null) {
                            mListener.onRatePosterInteraction(job_id, fixer_id, poster_id, posterRating, fixerComment);
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
        if (context instanceof OnRatePosterListener) {
            mListener = (OnRatePosterListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRatePosterListener");
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
    public interface OnRatePosterListener {
        void onRatePosterInteraction(int job_id, int fixer_id, int poster_id, float posterRating, String comment);
    }

    //handle the rating
    private void handlePosterRating(){
        posterRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                posterRating = ratingBar.getRating();
                Log.e(TAG, "poster rating = "+posterRating);
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
