package com.emtech.fixr.app;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.util.Log;

import com.emtech.fixr.helpers.MyPreferenceManager;


public class MyApplication extends Application
        implements LifecycleObserver {

        public static final String TAG = MyApplication.class
                .getSimpleName();

        public boolean myApplicationStatus;

        private static MyApplication mInstance;

        private MyPreferenceManager pref;

        @Override
        public void onCreate() {
            super.onCreate();

            ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

            mInstance = this;
        }

        public static synchronized MyApplication getInstance() {
            return mInstance;
        }


        public MyPreferenceManager getPrefManager() {
            if (pref == null) {
                pref = new MyPreferenceManager(this);
            }

            return pref;
        }

    public void logout() {
        //pref.clear();
        //Intent intent = new Intent(this, LoginActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //startActivity(intent);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connectListener() {
        Log.d(TAG, "resumed observing lifecycle.");
        mInstance.myApplicationStatus = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disconnectListener() {
        Log.d(TAG, "paused observing lifecycle.");
        mInstance.myApplicationStatus = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        Log.d(TAG, "Returning to foreground…");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        Log.d(TAG, "Moving to background…");
    }

}
