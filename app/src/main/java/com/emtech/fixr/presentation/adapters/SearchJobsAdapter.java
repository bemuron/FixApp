package com.emtech.fixr.presentation.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.helpers.CircleTransform;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SearchJobsAdapter extends PagedListAdapter<Job, SearchJobsAdapter.ItemViewHolder> {

    //private List<Job> jobList;
    private LayoutInflater inflater;
    private Context context;
    private SearchJobsListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;

    public SearchJobsAdapter(Context context, SearchJobsListAdapterListener listener){
        super(DIFF_CALLBACK);
        inflater = LayoutInflater.from(context);
        this.context = context;
        //this.jobList = jobs;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();

    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView job_name, job_location, job_date, job_time, job_amount, job_status;
        public ImageView imgProfile;
        public LinearLayout jobContainer;
        public RelativeLayout iconContainer;

        public ItemViewHolder(View view) {
            super(view);
            job_name = view.findViewById(R.id.my_jobs_job_title);
            job_date = view.findViewById(R.id.my_jobs_date);
            job_location = view.findViewById(R.id.my_jobs_job_location);
            job_time = view.findViewById(R.id.my_jobs_time);
            job_amount = view.findViewById(R.id.my_jobs_amount);
            job_status = view.findViewById(R.id.job_status);
            imgProfile = view.findViewById(R.id.icon_profile);
            jobContainer = view.findViewById(R.id.my_jobs_job_container);
            iconContainer = view.findViewById(R.id.icon_container);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.my_jobs_list_item, viewGroup, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Job job = getItem(position);
        int jobStatus;

        //make these texts bold
        SpannableStringBuilder location = new SpannableStringBuilder("Location: ");
        SpannableStringBuilder date = new SpannableStringBuilder("Date: ");
        SpannableStringBuilder currency = new SpannableStringBuilder("UGX. ");
        SpannableStringBuilder time = new SpannableStringBuilder("Time: ");

        location.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, location.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        date.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, date.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        currency.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, currency.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        time.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, time.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // displaying text view data
        holder.job_name.setText(job.getName());
        holder.job_date.setText(date + job.getJob_date());
        if (job.getIs_job_remote() == 0){
            holder.job_location.setText(location + job.getLocation());
        }else if(job.getIs_job_remote() == 1){
            holder.job_location.setText(location + "Remote Job");
        }else{
            holder.job_location.setVisibility(View.GONE);
        }
        if (job.getJob_time() != null || !job.getJob_time().isEmpty()) {
            holder.job_time.setText(time + job.getJob_time());
        }else{
            holder.job_time.setVisibility(View.GONE);
        }
        holder.job_amount.setText(currency + job.getEst_tot_budget());

        holder.job_status.setVisibility(View.GONE);

        // displaying the first letter of From in icon text
        //holder.iconText.setText(tutor.getName().substring(0, 1));

        // change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        // apply click events
        applyClickEvents(holder, position);

        // display profile image
        applyProfilePicture(holder, job);

    }

    //handling different click events
    private void applyClickEvents(SearchJobsAdapter.ItemViewHolder holder, final int position) {
        holder.jobContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onJobRowClicked(position);
            }
        });
    }

    private void applyProfilePicture(SearchJobsAdapter.ItemViewHolder holder, Job job) {
        if (!TextUtils.isEmpty(job.getImage1())) {
            Glide.with(context).load("http://emtechint.com/fixapp/assets/images/profile_pics/"+job.getProfile_pic())
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(new RequestOptions().fitCenter()
                            .transform(new CircleTransform(context)).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.imgProfile);
            holder.imgProfile.setColorFilter(null);
        } else {
            holder.imgProfile.setImageResource(R.drawable.bg_circle);
            //holder.imgProfile.setColorFilter(job.getColor());
            //holder.iconText.setVisibility(View.VISIBLE);
        }
    }

    private static DiffUtil.ItemCallback<Job> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Job>() {
                @Override
                public boolean areItemsTheSame(Job oldItem, Job newItem) {
                    return oldItem.getJob_id() == newItem.getJob_id();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(Job oldItem, @NonNull Job newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public interface SearchJobsListAdapterListener {

        void onJobRowClicked(int position);

    }
}
