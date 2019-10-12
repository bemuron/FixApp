package com.emtech.fixr.presentation.ui.activity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emtech.fixr.R;
import com.emtech.fixr.data.database.Category;

import java.util.Date;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesAdapterViewHolder>{

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // The context we use to utility methods, app resources and layout inflaters
    private final Context mContext;

    /*
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onItemClick method whenever
     * an item is clicked in the list.
     */
    private final CategoriesAdapterOnItemClickHandler mClickHandler;
    /*
     * Flag to determine if we want to use a separate view for the list item that represents
     * today. This flag will be true when the phone is in portrait mode and false when the phone
     * is in landscape. This flag will be set in the constructor of the adapter by accessing
     * boolean resources.
     */
    //private final boolean mUseTodayLayout;
    private List<Category> mCategory;

    /**
     * Creates a CategoriesAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    CategoriesAdapter(@NonNull Context context, CategoriesAdapterOnItemClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        //mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public CategoriesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        //int layoutId = getLayoutIdByType(viewType);
        View view = LayoutInflater.from(mContext).inflate(R.layout.categories_grid_item, viewGroup, false);
        view.setFocusable(true);
        return new CategoriesAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param categoriesAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapterViewHolder categoriesAdapterViewHolder, int position) {
        Category currentCategory = mCategory.get(position);

        String catName = currentCategory.getName();
        categoriesAdapterViewHolder.categoryName.setText(catName);

        currentCategory.setColor(getRandomMaterialColor("400"));

        //displaying the category pic or showing the first letter of the name
        applyProfilePicture(categoriesAdapterViewHolder, currentCategory);

    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of categories
     */
    @Override
    public int getItemCount() {
        if (null == mCategory) return 0;
        return mCategory.size();
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and list
     * @return the view type (today or future day)
     */
    /*
    @Override
    public int getItemViewType(int position) {
        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }
    */

    /**
     * Swaps the list used by the CategoriesAdapter for its categories data. This method is called by
     * {@link HomeActivity} after a load has finished. When this method is called, we assume we have
     * a new set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCategories the new list of categories to use as CategoriesAdapter's data source
     */
    void swapForecast(final List<Category> newCategories) {
        // If there was no forecast data, then recreate all of the list
        if (mCategory == null) {
            mCategory = newCategories;
            notifyDataSetChanged();
        }
            /*
        } else {
            /*
             * Otherwise we use DiffUtil to calculate the changes and update accordingly. This
             * shows the four methods you need to override to return a DiffUtil callback. The
             * old list is the current list stored in mForecast, while the new list is the new
             * values passed in from observing the database.
             */

            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mCategory.size();
                }

                @Override
                public int getNewListSize() {
                    return newCategories.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mCategory.get(oldItemPosition).getCategory_id() ==
                            newCategories.get(newItemPosition).getCategory_id();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Category newCategory = newCategories.get(newItemPosition);
                    Category oldCategory = mCategory.get(oldItemPosition);
                    return newCategory.getCategory_id() == oldCategory.getCategory_id();
                           // && newCategory.getCategoryName().equals(oldCategory.getCategoryName());
                }
            });
            mCategory = newCategories;
            result.dispatchUpdatesTo(this);
        }

    /**
     * get the category picture if available otherwise set a random color
     * */
    private void applyProfilePicture(CategoriesAdapterViewHolder holder, Category category) {
            if (!TextUtils.isEmpty(category.getImageName())) {
                Glide.with(mContext).load("http://www.emtechint.com/fixapp/assets/images/category_pics/"+category.getImageName())
                        .thumbnail(0.5f)
                        //.crossFade()
                        //.transform(new CircleTransform(context))
                        //.diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.categoryImage);
                holder.categoryImage.setColorFilter(null);
            }
        else {
            holder.categoryImage.setImageResource(R.drawable.bg_square);
            holder.categoryImage.setColorFilter(category.getColor());
            // displaying the first letter of From in icon text
            //holder.categoryName.setText(category.getName().substring(0, 1));
        }
    }

    /**
     * The interface that receives onItemClick messages.
     */
    public interface CategoriesAdapterOnItemClickHandler {
        void onItemClick(int category_id, String categoryName);
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class CategoriesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView categoryImage;
        final TextView categoryName;

        CategoriesAdapterViewHolder(View view) {
            super(view);

            categoryImage = view.findViewById(R.id.category_pic);
            categoryName = view.findViewById(R.id.category_name);

            //view.setOnClickListener(this);
            categoryImage.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onItemClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            int categoryId = mCategory.get(adapterPosition).getCategory_id();
            String categoryName = mCategory.get(adapterPosition).getName();
            mClickHandler.onItemClick(categoryId, categoryName);
        }
    }

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = mContext.getResources().getIdentifier("mdcolor_" + typeColor, "array", mContext.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = mContext.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }
}
