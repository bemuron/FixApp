package com.emtech.fixr.presentation.adapters;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.emtech.fixr.R;
import com.emtech.fixr.presentation.ui.fragment.IntroFragment;

public class IntroAdapter extends FragmentStateAdapter {

    public IntroAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return IntroFragment.newInstance(Color.parseColor("#f64c73"), position);
            case 1:
                return IntroFragment.newInstance(Color.parseColor("#20d2bb"), position);
            case 2:
                return IntroFragment.newInstance(Color.parseColor("#3395ff"), position);
            //case 3:
                //return IntroFragment.newInstance(Color.parseColor("#c873f4"), position);
            default:
                return IntroFragment.newInstance(Color.parseColor("#c873f4"), position);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
