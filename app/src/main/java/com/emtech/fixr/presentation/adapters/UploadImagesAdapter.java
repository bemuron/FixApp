package com.emtech.fixr.presentation.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.emtech.fixr.R;
import com.emtech.fixr.models.UploadImage;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class UploadImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final String TAG = UploadImagesAdapter.class.getSimpleName();
  //private ArrayList<Uri> uriArrayList;
  private ArrayList<UploadImage> uploadImages;
  private LayoutInflater inflater;
  Context context;
  private OnItemClickListener onItemClickListener;
  private final static int IMAGE_LIST = 0;
  private final static int IMAGE_PICKER = 1;
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
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == IMAGE_LIST) {;
    //layout to show the images selected
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_image_list_item, parent, false);
      return new ImageListViewHolder(view);
    } else {
      //layout to show the camera and folder options
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_image_picker, parent, false);
      return new ImagePickerViewHolder(view);
    }
  }

  @Override
  public int getItemViewType(int position) {
    return position < 2 ? IMAGE_PICKER : IMAGE_LIST;
  }

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
    UploadImage uploadImage = uploadImages.get(position);

    Log.e(TAG,"Image added is "+uploadImage.getImage());

    if (holder.getItemViewType() == IMAGE_LIST) {
      Glide.with(context)
              .load(uploadImage.getImage())
              //.placeholder(R.color.codeGray)
              //.centerCrop()
              .transition(DrawableTransitionOptions.withCrossFade(500))
              .into(((ImageListViewHolder) holder).image);

      if (uploadImage.isSelected()) {
        ((ImageListViewHolder) holder).checkBox.setChecked(true);
      } else {
        ((ImageListViewHolder) holder).checkBox.setChecked(false);
      }
    } else {
      ImagePickerViewHolder viewHolder = (ImagePickerViewHolder) holder;
      viewHolder.image.setImageResource(uploadImage.getResImg());
      viewHolder.title.setText(uploadImage.getTitle());
    }

    // apply click events
    applyClickEvents(holder, position, uploadImage.getImage());

  }

  //handling different click events
  private void applyClickEvents(RecyclerView.ViewHolder holder, final int position, String imagePath) {

    if (holder.getItemViewType() == IMAGE_LIST) {
      ((ImageListViewHolder) holder).imgContainer.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          listener.onImageLongClicked(position);
          view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
          return true;
        }
      });

      ((ImageListViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          listener.onImageClicked(position, imagePath);
        }
      });

    }else{
      ((ImagePickerViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          listener.onImageClicked(position, imagePath);
        }
      });
    }

    /*holder.iconDel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        listener.onDeleteImageClicked(position);
      }
    });*/
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
    //if (uploadImages == null) {
      this.uploadImages = newImage;
      notifyDataSetChanged();
      Log.e(TAG,"Size of list is "+uploadImages.size());
    //}
  }

  public void addNewImageToList(String filePath){
    UploadImage imageModel = new UploadImage();
    imageModel.setImage(filePath);
    imageModel.setSelected(false);
    uploadImages.add(imageModel);
    notifyDataSetChanged();
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

  public class ImageListViewHolder extends RecyclerView.ViewHolder {
    ImageView image;
    CheckBox checkBox;
    public RelativeLayout imgContainer;

    public ImageListViewHolder(View itemView) {
      super(itemView);
      image = itemView.findViewById(R.id.uploadJobImage);
      checkBox = itemView.findViewById(R.id.circle);
      imgContainer = itemView.findViewById(R.id.uploadImageContainer);
      /*imgContainer.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          listener.onImageLongClicked(getAdapterPosition());
          view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
          return true;
        }
      });*/
      /*itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onItemClickListener.onItemClick(getAdapterPosition(), v);
        }
      });*/
    }
  }

  public class ImagePickerViewHolder extends RecyclerView.ViewHolder {
    ImageView image;
    TextView title;

    public ImagePickerViewHolder(View itemView) {
      super(itemView);
      image = itemView.findViewById(R.id.image);
      title = itemView.findViewById(R.id.title);
      /*itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onItemClickListener.onItemClick(getAdapterPosition(), v);
        }
      });*/
    }
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

    void onImageClicked(int position, String imagePath);

    void onImageLongClicked(int position);

  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public interface OnItemClickListener {
    void onItemClick(int position, View v);
  }

}
