/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emtech.fixr.utilities;

import android.content.Context;

import com.emtech.fixr.data.FixAppRepository;
import com.emtech.fixr.data.database.FixAppDatabase;
import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.network.BrowsedJobsDataSource;
import com.emtech.fixr.data.network.FetchCategories;
import com.emtech.fixr.data.network.FixAppNetworkDataSource;
import com.emtech.fixr.data.network.GetMyJobs;
import com.emtech.fixr.data.network.LoginUser;
import com.emtech.fixr.data.network.MakePayments;
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.data.network.RegisterUser;
import com.emtech.fixr.data.network.SearchJobsDataSource;
import com.emtech.fixr.models.UserJobs;
import com.emtech.fixr.presentation.viewmodels.BrowseJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.HomeViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.LoginRegistrationViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.MyJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PaymentViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.SearchJobsViewModelFactory;
import com.emtech.fixr.presentation.viewmodels.UserProfileActivityViewModelFactory;

import java.util.Date;

/**
 * Provides static methods to inject the various classes needed for Sunshine i.e
 * The purpose of InjectorUtils is to provide static methods for dependency injection.
 * Dependency injection is the idea that you should make required components available for a class,
 * instead of creating them within the class itself.
 */
public class InjectorUtils {

    public static FixAppRepository provideRepository(Context context) {
        FixAppDatabase database = FixAppDatabase.getInstance(context.getApplicationContext());

        AppExecutors executors = AppExecutors.getInstance();

        PostFixAppJob fixAppJob =
                PostFixAppJob.getInstance(context.getApplicationContext(), executors);

        RegisterUser registerUser =
                RegisterUser.getInstance(context.getApplicationContext(), executors);

        LoginUser loginUser =
                LoginUser.getInstance(context.getApplicationContext(), executors);

        FetchCategories fetchCategories =
                FetchCategories.getInstance(context.getApplicationContext(), executors);

        GetMyJobs getMyJobs = GetMyJobs.getInstance(context.getApplicationContext(), executors);

        MakePayments makePayments = MakePayments.getInstance(context.getApplicationContext(), executors);

        BrowsedJobsDataSource browsedJobsDataSource =
                BrowsedJobsDataSource.getInstance();

        //SearchJobsDataSource searchJobsDataSource =
        //      SearchJobsDataSource.getInstance();

        return FixAppRepository.getInstance(database.categoriesDao(), database.usersDao(), fetchCategories,
                fixAppJob, registerUser, loginUser, getMyJobs, makePayments, browsedJobsDataSource, executors);
    }

    public static PostFixAppJob providePostFixAppJob(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return PostFixAppJob.getInstance(context.getApplicationContext(), executors);
    }

    public static LoginUser provideLoginUser(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return LoginUser.getInstance(context.getApplicationContext(), executors);
    }

    public static RegisterUser provideRegisterUser(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return RegisterUser.getInstance(context.getApplicationContext(), executors);
    }

    public static FetchCategories provideFetchCategories(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return FetchCategories.getInstance(context.getApplicationContext(), executors);
    }

    public static GetMyJobs provideGetMyJobs(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return GetMyJobs.getInstance(context.getApplicationContext(), executors);
    }

//    public static DetailViewModelFactory provideDetailViewModelFactory(Context context, Date date) {
//        SunshineRepository repository = provideRepository(context.getApplicationContext());
//        return new DetailViewModelFactory(repository, date);
//    }

    public static HomeViewModelFactory provideMainActivityViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new HomeViewModelFactory(repository);
    }

    public static LoginRegistrationViewModelFactory provideLoginRegistrationViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new LoginRegistrationViewModelFactory(repository);
    }

    public static PostJobViewModelFactory providePostJobActivityViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new PostJobViewModelFactory(repository);
    }

    public static PaymentViewModelFactory providePaymentActivityViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new PaymentViewModelFactory(repository);
    }

    public static MyJobsViewModelFactory provideMyJobsViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new MyJobsViewModelFactory(repository);
    }

    public static BrowseJobsViewModelFactory provideBrowseJobsViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new BrowseJobsViewModelFactory(repository);
    }

    public static SearchJobsViewModelFactory provideSearchJobsViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new SearchJobsViewModelFactory(repository);
    }

    public static UserProfileActivityViewModelFactory provideUserProfileViewModelFactory(Context context) {
        FixAppRepository repository = provideRepository(context.getApplicationContext());
        return new UserProfileActivityViewModelFactory(repository);
    }

    /*
     * Dependency injection is the idea that you should make required components available
     * for a class, instead of creating them within the class itself. An example of how the
     * Sunshine code does this is that instead of constructing the WeatherNetworkDatasource
     * within the SunshineRepository, the WeatherNetworkDatasource is created via InjectorUtilis
     * and passed into the SunshineRepository constructor. One of the benefits of this is that
     * components are easier to replace when you're testing. You can learn more about dependency
     * injection here. For now, know that the methods in InjectorUtils create the classes you
     * need, so they can be passed into constructors.
     * */
}