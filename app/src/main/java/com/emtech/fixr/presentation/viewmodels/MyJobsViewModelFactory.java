package com.emtech.fixr.presentation.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.emtech.fixr.data.FixAppRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link FixAppRepository}
 */
public class MyJobsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final FixAppRepository mRepository;

    public MyJobsViewModelFactory(FixAppRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MyJobsActivityViewModel(mRepository);
    }
}
