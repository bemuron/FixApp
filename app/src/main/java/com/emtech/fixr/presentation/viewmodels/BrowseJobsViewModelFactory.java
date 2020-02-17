package com.emtech.fixr.presentation.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;

import com.emtech.fixr.data.FixAppRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link FixAppRepository}
 */
public class BrowseJobsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final FixAppRepository mRepository;
    private Context context;

    public BrowseJobsViewModelFactory(FixAppRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new BrowseJobsActivityViewModel(mRepository);
    }
}
