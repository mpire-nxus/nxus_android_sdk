package com.nxus.dsp.app;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.nxus.dsp.NxusDSPTracker;
import com.nxus.dsp.logging.Logger;
import com.nxus.dsp.tracking.ITrackingConstants;
import com.nxus.dsp.tracking.TrackingEvents;
import com.nxus.dsp.tracking.TrackingParams;
import com.nxus.dsp.tracking.TrackingWorker;

public class NxusActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static final Logger log = Logger.getLog(NxusActivityLifecycleCallbacks.class);

    private static final String TRACKING_PARAMS_ACTIVITY = "activity";

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        log.debug("ACTIVITY STARTED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            NxusDSPTracker.trackEvent(TrackingEvents.ACTIVITY_STARTED, params);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        log.debug("ACTIVITY RESUMED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            NxusDSPTracker.trackEvent(TrackingEvents.ACTIVITY_RESUMED, params);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        log.debug("ACTIVITY PAUSED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            NxusDSPTracker.trackEvent(TrackingEvents.ACTIVITY_PAUSED, params);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        log.debug("ACTIVITY STOPPED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            NxusDSPTracker.trackEvent(TrackingEvents.ACTIVITY_STOPPED, params);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

}
