package com.emtech.fixr.presentation.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.emtech.fixr.R;
import com.emtech.fixr.presentation.ui.fragment.PostJobBudgetFragment;
import com.emtech.fixr.presentation.ui.fragment.PostJobDateFragment;
import com.emtech.fixr.presentation.ui.fragment.PostJobDetailsFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.job_details_tab,
            R.string.job_date_tab, R.string.job_budget_tab};
    private final Context mContext;
    private int mUserId, mCategoryId, mJobId, mIsJobRemote;
    private String mCategoryName,mJobName, mJobDescription, mMustHaveOne,
            mMustHaveTwo, mMustHaveThree, mJobImage, mJobDate, mJobTime,
            mEstTotBudget, mTotalBudget;

    public SectionsPagerAdapter(Context context, FragmentManager fm, int userId,
                                int categoryId, String categoryName, int jobId,
                                String jobName, String jobDescription, String mustHaveOne,
                                String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                String jobImage, String jobDate, String jobTime,
                                String estTotBudget, String totalBudget) {
        super(fm);
        mContext = context;
        mUserId = userId;
        mCategoryId = categoryId;
        mCategoryName = categoryName;
        mJobName = jobName;
        mJobDescription = jobDescription;
        mMustHaveOne = mustHaveOne;
        mMustHaveTwo = mustHaveTwo;
        mMustHaveThree = mustHaveThree;
        mJobId =jobId;
        mIsJobRemote = isJobRemote;
        mJobImage = jobImage;
        mJobDate = jobDate;
        mJobTime = jobTime;
        mEstTotBudget = estTotBudget;
        mTotalBudget = totalBudget;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return PostJobDetailsFragment.newInstance(mUserId, mCategoryId, mCategoryName,
                        mJobId, mJobName, mJobDescription, mMustHaveOne, mMustHaveTwo,
                        mMustHaveThree, mIsJobRemote, mJobImage);
            case 1:
                return PostJobDateFragment.newInstance(mJobId, mJobDate, mJobTime);
            case 2:
                return PostJobBudgetFragment.newInstance(mJobId, mEstTotBudget, mTotalBudget);
            /*default:
                return PostJobBudgetFragment.newInstance();*/
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}