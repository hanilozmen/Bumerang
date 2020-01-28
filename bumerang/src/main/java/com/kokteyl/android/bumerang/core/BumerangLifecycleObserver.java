package com.kokteyl.android.bumerang.core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class BumerangLifecycleObserver implements Application.ActivityLifecycleCallbacks {
    private Listener mListener;

    private BumerangLifecycleObserver() {
    }

    public BumerangLifecycleObserver(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mListener.onActivityCreated(activity, savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mListener.onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mListener.onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mListener.onActivityPaused(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mListener.onActivityStopped(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        mListener.onActivitySaveInstanceState(activity, outState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mListener.onActivityDestroyed(activity);
    }

    public interface Listener {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);

        void onActivityStarted(Activity activity);

        void onActivityResumed(Activity activity);

        void onActivityPaused(Activity activity);

        void onActivityStopped(Activity activity);

        void onActivitySaveInstanceState(Activity activity, Bundle outState);

        void onActivityDestroyed(Activity activity);
    }
}
