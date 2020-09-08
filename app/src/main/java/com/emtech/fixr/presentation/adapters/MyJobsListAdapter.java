package com.emtech.fixr.presentation.adapters;

import android.content.Context;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
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

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


public class MyJobsListAdapter extends RecyclerView.Adapter<MyJobsListAdapter.MyViewHolder> {

  private List<Job> jobList;
  private LayoutInflater inflater;
  Context context;
  private MyJobsListAdapterListener listener;
  private SparseBooleanArray selectedItems;

  // array used to perform multiple animation at once
  private SparseBooleanArray animationItemsIndex;
  private boolean reverseAllAnimations = false;

  // index is used to animate only the selected row
  // dirty fix, find a better solution
  private static int currentSelectedIndex = -1;

  public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
    public TextView job_name, job_location, job_date, job_time,
            job_amount, job_status, iconText;
    public ImageView iconImp, imgProfile;
    public LinearLayout jobContainer;
    public RelativeLayout iconContainer, iconBack, iconFront;

    public MyViewHolder(View view) {
      super(view);
      job_name = view.findViewById(R.id.my_jobs_job_title);
      job_date = view.findViewById(R.id.my_jobs_date);
      job_location = view.findViewById(R.id.my_jobs_job_location);
      job_time = view.findViewById(R.id.my_jobs_time);
      job_amount = view.findViewById(R.id.my_jobs_amount);
      job_status = view.findViewById(R.id.job_status);
      //iconImp = view.findViewById(R.id.icon_star);
      imgProfile = view.findViewById(R.id.icon_profile);
      iconText = view.findViewById(R.id.icon_text);
      jobContainer = view.findViewById(R.id.my_jobs_job_container);
      iconContainer = view.findViewById(R.id.icon_container);
      view.setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {
      view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
      return true;
    }
  }

  public MyJobsListAdapter(Context context, List<Job> jobs, MyJobsListAdapterListener listener) {
    inflater = LayoutInflater.from(context);
    this.context = context;
    this.jobList = jobs;
    this.listener = listener;
    selectedItems = new SparseBooleanArray();
    animationItemsIndex = new SparseBooleanArray();
  }

  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.my_jobs_list_item, parent, false);

    return new MyViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(final MyViewHolder holder, int position) {
    Job job = jobList.get(position);
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
        holder.job_status.setTextColor(context.getResources().getColor(R.color.draft_job));
        break;
      case 1:
        holder.job_status.setText("Posted");
        holder.job_status.setTextColor(context.getResources().getColor(R.color.posted_job));
        break;
      case 2:
        holder.job_status.setText("Assigned");
        holder.job_status.setTextColor(context.getResources().getColor(R.color.assigned_job));
        break;
      case 3:
        holder.job_status.setText("Offers");
        holder.job_status.setTextColor(context.getResources().getColor(R.color.offered_job));
        break;
      case 4:
        holder.job_status.setText("Complete");
        holder.job_status.setTextColor(context.getResources().getColor(R.color.completed_job));
        break;
    }

    // change the row state to activated
    holder.itemView.setActivated(selectedItems.get(position, false));

    // apply click events
    applyClickEvents(holder, position);

    // display profile image
    applyProfilePicture(holder, job);

  }

  //handling different click events
  private void applyClickEvents(MyViewHolder holder, final int position) {
    holder.jobContainer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onJobRowClicked(position);
      }
    });
  }

  private void applyProfilePicture(MyViewHolder holder, Job job) {
    if (!TextUtils.isEmpty(job.getProfile_pic())) {
      Glide.with(context).load("http://emtechint.com/fixapp/assets/images/profile_pics/"+job.getProfile_pic())
              .thumbnail(0.5f)
              .transition(withCrossFade())
              .apply(new RequestOptions().fitCenter()
                      .transform(new CircleTransform(context)).diskCacheStrategy(DiskCacheStrategy.ALL))
              .into(holder.imgProfile);
      holder.imgProfile.setColorFilter(null);
      holder.iconText.setVisibility(View.INVISIBLE);
    } else {
      holder.imgProfile.setImageResource(R.drawable.bg_circle);
      holder.imgProfile.setColorFilter(job.getColor());
      // displaying the first letter of From in icon text
      //job.getUserName()
      //holder.iconText.setText("username".substring(0, 1));
      holder.iconText.setVisibility(View.VISIBLE);
    }
  }

  public void setList(List<Job> job) {
    this.jobList = job;
    notifyDataSetChanged();
  }

  @Override
  public long getItemId(int position) {
    return jobList.get(position).getJob_id();
  }

  @Override
  public int getItemCount() {
    return jobList.size();
  }

  public void toggleSelection(int pos) {
    currentSelectedIndex = pos;
    if (selectedItems.get(pos, false)) {
      selectedItems.delete(pos);
      animationItemsIndex.delete(pos);
    } else {
      selectedItems.put(pos, true);
      animationItemsIndex.put(pos, true);
    }
    notifyItemChanged(pos);
  }

  public void clearSelections() {
    reverseAllAnimations = true;
    selectedItems.clear();
    notifyDataSetChanged();
  }

  public int getSelectedItemCount() {
    return selectedItems.size();
  }

  public List<Integer> getSelectedItems() {
    List<Integer> items =
            new ArrayList<>(selectedItems.size());
    for (int i = 0; i < selectedItems.size(); i++) {
      items.add(selectedItems.keyAt(i));
    }
    return items;
  }

  public void removeData(int position) {
    jobList.remove(position);
    resetCurrentIndex();
  }

  private void resetCurrentIndex() {
    currentSelectedIndex = -1;
  }

  public interface MyJobsListAdapterListener {

    void onJobRowClicked(int position);

  }

}
