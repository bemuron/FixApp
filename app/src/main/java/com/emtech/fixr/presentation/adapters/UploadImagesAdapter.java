package com.emtech.fixr.presentation.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.emtech.fixr.R;
import com.emtech.fixr.models.UploadImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadImagesAdapter extends RecyclerView.Adapter<UploadImagesAdapter.ImageUploadViewHolder> {
  private static final String TAG = UploadImagesAdapter.class.getSimpleName();
  //private ArrayList<Uri> uriArrayList;
  private ArrayList<UploadImage> uploadImages;
  private LayoutInflater inflater;
  Context context;
  private Bitmap bitmap;
  private UploadImagesAdapterListener listener;
  private SparseBooleanArray selectedItems;

  // array used to perform multiple animation at once
  private SparseBooleanArray animationItemsIndex;
  private boolean reverseAllAnimations = false;

  // index is used to animate only the selected row
// dirty fix, find a better solution
  private static int currentSelectedIndex = -1;

  public class ImageUploadViewHolder extends RecyclerView.ViewHolder {
    public TextView title, content, date, iconText, issueName;
    public ImageView iconDel, uploadImg;
    public LinearLayout mustHaveContainer;
    public RelativeLayout iconContainer, iconBack, iconFront, imgContainer;

    public ImageUploadViewHolder(View view) {
      super(view);
      //title = view.findViewById(R.id.text_view_must_have_desc);
      //iconDel = view.findViewById(R.id.icon_delete);
      uploadImg = view.findViewById(R.id.uploadJobImage);
      imgContainer = view.findViewById(R.id.uploadImageContainer);
      //view.setOnLongClickListener(this);
    }
  }

  public UploadImagesAdapter(Context context, ArrayList<UploadImage> uploadImages, UploadImagesAdapterListener listener) {
    inflater = LayoutInflater.from(context);
    this.context = context;
    this.uploadImages = uploadImages;
    this.listener = listener;
    selectedItems = new SparseBooleanArray();
    animationItemsIndex = new SparseBooleanArray();
  }

  @Override
  public ImageUploadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.upload_image_list_item, parent, false);

    return new ImageUploadViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(final ImageUploadViewHolder holder, int position) {
    UploadImage uploadImage = uploadImages.get(position);

    //display image in grid
    try {
      bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uploadImage.getImagePath());

      Log.e(TAG, "Image path "+bitmap);

      //holder.uploadImg.setImageBitmap(bitmap);

      Glide.with(context).load(uploadImage.getImage())
              .thumbnail(0.5f)
              /*.placeholder(R.color.qu_grey_600)
              .centerCrop()
              .transition(DrawableTransitionOptions.withCrossFade(500))*/
              .into(holder.uploadImg);
      holder.uploadImg.setColorFilter(null);

    } catch (IOException e) {
      e.printStackTrace();
    }

    // handle delete icon
    applyDeleteItem(holder, position);

    // apply click events
    //applyClickEvents(holder, position);

  }

  //handling different click events
  private void applyClickEvents(ImageUploadViewHolder holder, final int position) {

    holder.iconDel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onDeleteImageClicked(position);
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

  public void refreshImageList(final ArrayList<UploadImage> newImage){
    if (uploadImages == null) {
      uploadImages = newImage;
      notifyDataSetChanged();
    }
  }


  @Override
  public long getItemId(int position) {
    return uploadImages.get(position).getId();
  }

  private void applyDeleteItem(ImageUploadViewHolder holder, int position) {
    removeData(position);
  }

  @Override
  public int getItemCount() {
    return uploadImages.size();
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
    uploadImages.remove(position);
    resetCurrentIndex();
  }

  private void resetCurrentIndex() {
    currentSelectedIndex = -1;
  }

  public interface UploadImagesAdapterListener {

    void onDeleteImageClicked(int position);

    void onImageClicked(int position);

  }

}
