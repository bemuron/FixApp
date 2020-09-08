package com.emtech.fixr.presentation.adapters;

import android.content.Context;
import android.graphics.Typeface;
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

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Job;
import com.emtech.fixr.helpers.CircleTransform;
import com.emtech.fixr.models.Offer;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


public class OffersListAdapter extends RecyclerView.Adapter<OffersListAdapter.MyViewHolder> {

    private List<Offer> offerList;
    private LayoutInflater inflater;
    Context context;
    private OffersListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView job_name, job_location, job_date,
                job_posted_on, job_amount, job_status, iconText;
        private ImageView imgProfile;
        private LinearLayout jobContainer;
        private RelativeLayout iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
            job_name = view.findViewById(R.id.offer_jobs_job_title);
            job_date = view.findViewById(R.id.offer_job_date);
            job_location = view.findViewById(R.id.offer_job_location);
            job_posted_on = view.findViewById(R.id.offer_job_posted_on);
            job_amount = view.findViewById(R.id.offer_amount);
            job_status = view.findViewById(R.id.offer_status);
            //iconImp = view.findViewById(R.id.icon_star);
            imgProfile = view.findViewById(R.id.icon_profile);
            iconText = view.findViewById(R.id.icon_text);
            jobContainer = view.findViewById(R.id.offer_job_container);
            iconContainer = view.findViewById(R.id.offer_icon_container);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    public OffersListAdapter(Context context, List<Offer> offers, OffersListAdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.offerList = offers;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offers_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Offer offer = offerList.get(position);
        int offerStatus;

        //make these texts bold
        SpannableStringBuilder location = new SpannableStringBuilder("Location: ");
        SpannableStringBuilder date = new SpannableStringBuilder("Posted On: ");
        SpannableStringBuilder yourOffer = new SpannableStringBuilder("Your offer UGX. ");
        SpannableStringBuilder postedOn = new SpannableStringBuilder("Posted On: ");

        location.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, location.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        date.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, date.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        yourOffer.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, yourOffer.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        postedOn.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, postedOn.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // displaying text view data
        holder.job_name.setText(offer.getName());
        holder.job_date.setText(date + offer.getJob_date());
        holder.job_posted_on.setText(postedOn + offer.getPosted_on());
        holder.job_amount.setText(yourOffer + offer.getOffer_amount());

        // displaying the first letter of From in icon text
        //holder.iconText.setText(tutor.getName().substring(0, 1));

        offerStatus = offer.getSeen_by_poster();
        if (offerStatus == 0){
            holder.job_status.setText("Not yet seen");
            holder.job_status.setTextColor(context.getResources().getColor(R.color.draft_job));
        }else if(offerStatus == 1){
            holder.job_status.setText("Seen by poster");
            holder.job_status.setTextColor(context.getResources().getColor(R.color.completed_job));
        }

        // change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        // apply click events
        applyClickEvents(holder, offer.getName(), position);

        // display profile image
        applyProfilePicture(holder, offer);

    }

    //handling different click events
    private void applyClickEvents(MyViewHolder holder, String jobName, final int position) {
        holder.jobContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOfferRowClicked(jobName, position);
            }
        });
    }

    private void applyProfilePicture(MyViewHolder holder, Offer offer) {
        if (!TextUtils.isEmpty(offer.getProfile_pic())) {
            Glide.with(context).load("http://emtechint.com/fixapp/assets/images/profile_pics/"+offer.getProfile_pic())
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(new RequestOptions().fitCenter()
                            .transform(new CircleTransform(context)).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.imgProfile);
            holder.imgProfile.setColorFilter(null);
            holder.iconText.setVisibility(View.INVISIBLE);
        } else {
            holder.imgProfile.setImageResource(R.drawable.bg_circle);
            holder.imgProfile.setColorFilter(offer.getColor());
            // displaying the first letter of From in icon text
            holder.iconText.setText(offer.getUser_name().substring(0, 1));
            holder.iconText.setVisibility(View.VISIBLE);
        }
    }

    public void setList(List<Offer> offer) {
        this.offerList = offer;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return offerList.get(position).getJob_id();
    }

    @Override
    public int getItemCount() {
        if (offerList != null){
            return offerList.size();
        }
        return 0;
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

    public void clearData(){
        offerList.clear();
    }

    public void removeData(int position) {
        offerList.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface OffersListAdapterListener {

        void onOfferRowClicked(String jobName, int position);

    }

}
