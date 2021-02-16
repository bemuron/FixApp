package com.emtech.fixr.presentation.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.emtech.fixr.data.FixAppRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link FixAppRepository}
 */
public class PaymentViewModelFactory extends ViewModelProvider.NewInstanceFactory {

  private final FixAppRepository mRepository;

  public PaymentViewModelFactory(FixAppRepository repository) {
    this.mRepository = repository;
  }

  @Override
  public <T extends ViewModel> T create(Class<T> modelClass) {
    //noinspection unchecked
    return (T) new PaymentActivityViewModel(mRepository);
  }
}
