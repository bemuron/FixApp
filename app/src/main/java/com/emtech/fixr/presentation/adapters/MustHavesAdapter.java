package com.emtech.fixr.presentation.adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emtech.fixr.R;
import com.emtech.fixr.models.JobMustHave;

import java.util.ArrayList;
import java.util.List;

public class MustHavesAdapter extends RecyclerView.Adapter<MustHavesAdapter.MyViewHolder> {

private ArrayList<JobMustHave> jobMustHaves;
private LayoutInflater inflater;
        Context context;
private MustHavesAdapterListener listener;
private SparseBooleanArray selectedItems;

// array used to perform multiple animation at once
private SparseBooleanArray animationItemsIndex;
private boolean reverseAllAnimations = false;

// index is used to animate only the selected row
// dirty fix, find a better solution
private static int currentSelectedIndex = -1;

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView title, content, date, iconText, issueName;
    public ImageView iconDel, imgProfile;
    public LinearLayout mustHaveContainer;
    public RelativeLayout iconContainer, iconBack, iconFront;

    public MyViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.text_view_must_have_desc);
        iconDel = view.findViewById(R.id.icon_delete);
        mustHaveContainer = view.findViewById(R.id.must_have_item_container);
        //view.setOnLongClickListener(this);
    }
}

    public MustHavesAdapter(Context context, ArrayList<JobMustHave> mustHaves, MustHavesAdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.jobMustHaves = mustHaves;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.must_have_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        JobMustHave mustHave = jobMustHaves.get(position);

        // displaying text view data
        //holder.title.setText(mustHave.getMustHaveDescription());
        if (mustHave != null)
        holder.title.append("\u2713 " + mustHave.getMustHaveDescription());

        // handle delete icon
        applyDeleteItem(holder, position);

        // apply click events
        applyClickEvents(holder, position);

    }

    //handling different click events
    private void applyClickEvents(MyViewHolder holder, final int position) {

        holder.iconDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconDeleteClicked(position);
            }
        });
/*
        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
        */
    }


    @Override
    public long getItemId(int position) {
        return jobMustHaves.get(position).getId();
    }

    private void applyDeleteItem(MyViewHolder holder, int position) {
        removeData(position);
    }

    @Override
    public int getItemCount() {
        return jobMustHaves.size();
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
        jobMustHaves.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

public interface MustHavesAdapterListener {

    void onIconDeleteClicked(int position);

}

}
