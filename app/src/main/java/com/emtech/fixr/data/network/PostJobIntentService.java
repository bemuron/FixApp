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
import android.os.Bundle;
import android.util.Log;

import com.emtech.fixr.utilities.InjectorUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for immediately scheduling a sync with the server off of the
 * main thread. This should only be called when the application is on the
 * screen.
 */
public class PostJobIntentService extends IntentService {
  private static final String LOG_TAG = PostJobIntentService.class.getSimpleName();

  public PostJobIntentService() {
    super("PostJobIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    Bundle jobBundle = intent.getExtras();

    Log.d(LOG_TAG, "Post job intent service started");
    PostFixAppJob postFixAppJob = InjectorUtils.providePostFixAppJob(this.getApplicationContext());
    if (jobBundle != null){
      Log.d(LOG_TAG, "Job details not empty");
      int userId = jobBundle.getInt("userId");
      String jobTitle = jobBundle.getString("jobTitle");
      String jobDesc = jobBundle.getString("jobDesc");
      String jobLocation = jobBundle.getString("jobLocation");
      String mustHaveOne = jobBundle.getString("mustHaveOne");
      String mustHaveTwo = jobBundle.getString("mustHaveTwo");
      String mustHaveThree = jobBundle.getString("mustHaveThree");
      int isJobRemote = jobBundle.getInt("isJobRemote");
      ArrayList<File> imageFilesList = (ArrayList<File>) jobBundle.getSerializable("filePath");
      int categoryId = jobBundle.getInt("categoryId");

      //check if the user didn't add an image
      if (imageFilesList == null){
        //pass the job details to the method to be posted to the server: finally
        postFixAppJob.postJobDetailsWithoutImage(userId, jobTitle, jobDesc, jobLocation,
                mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, categoryId);
      }else {
        //pass the job details to the method to be posted to the server: finally
        postFixAppJob.postJobDetails(userId, jobTitle, jobDesc, jobLocation,
                mustHaveOne, mustHaveTwo,
                mustHaveThree, isJobRemote, imageFilesList, categoryId);
      }
    }else{
      Log.e(LOG_TAG, "Job details empty");
    }

  }
}