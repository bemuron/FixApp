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
package com.emtech.fixr.data.network;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.emtech.fixr.utilities.InjectorUtils;

/**
 * An {@link IntentService} subclass for immediately scheduling a sync with the server off of the
 * main thread. This is necessary because {com.firebase.jobdispatcher.FirebaseJobDispatcher}
 * will not trigger a job immediately. This should only be called when the application is on the
 * screen.
 */
public class FixAppSyncIntentService extends IntentService {
    private static final String LOG_TAG = FixAppSyncIntentService.class.getSimpleName();

    public FixAppSyncIntentService() {
        super("FixAppSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Intent service started");
        /*
        FixAppNetworkDataSource networkDataSource = InjectorUtils.
                provideNetworkDataSource(this.getApplicationContext());
        networkDataSource.fetchCategories();
*/
        FetchCategories fetchCategories = InjectorUtils.provideFetchCategories(this.getApplicationContext());
        fetchCategories.GetAppCategories();
    }
}