package com.emtech.fixr.presentation.adapters;

import android.annotation.SuppressLint;
import androidx.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.helpers.CircleTransform;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class BrowseJobsAdapter extends PagedListAdapter<Job, BrowseJobsAdapter.ItemViewHolder> {

    //private List<Job> jobList;
    private LayoutInflater inflater;
    private Context context;
    private BrowseJobsListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;

    public BrowseJobsAdapter(Context context, BrowseJobsListAdapterListener listener){
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
        }else {
            holder.job_location.setText(location + "Remote Job");
        }
        holder.job_time.setText(time + job.getJob_time());
        holder.job_amount.setText(currency + job.getEst_tot_budget());

        // 0 - draft, 1 - posted, 2 - assigned, 3 - offers, 4 - complete
        jobStatus = job.getJob_status();
        switch (jobStatus){
            case 0:
                holder.job_status.setText("Draft");
                break;
            case 1:
                holder.job_status.setText("Posted");
                break;
            case 2:
                holder.job_status.setText("Assigned");
                break;
            case 3:
                holder.job_status.setText("Offers");
                break;
            case 4:
                holder.job_status.setText("Complete");
                break;
        }

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
    private void applyClickEvents(BrowseJobsAdapter.ItemViewHolder holder, final int position) {
        holder.jobContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onJobRowClicked(position);
            }
        });
    }

    private void applyProfilePicture(BrowseJobsAdapter.ItemViewHolder holder, Job job) {
        if (!TextUtils.isEmpty(job.getImage1())) {
            Glide.with(context).load(job.getImage1())
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

    /*public void setList(List<Job> job) {
        this.jobList = job;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return jobList.get(position).getJob_id();
    }*/

    /*@Override
    public int getItemCount() {
        return jobList.size();
    }*/

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

    public interface BrowseJobsListAdapterListener {

        void onJobRowClicked(int position);

    }
}
