package com.nxus.measurement.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.nxus.measurement.MpireNxusMeasurement;
import com.nxus.measurement.logging.Logger;
import com.nxus.measurement.tracking.ITrackingConstants;
import com.nxus.measurement.tracking.TrackingEvents;
import com.nxus.measurement.tracking.TrackingParams;
import com.nxus.measurement.tracking.TrackingWorker;

/**
 * @author TechMpire Ltd.
 */
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
            MpireNxusMeasurement.trackEvent(TrackingEvents.ACTIVITY_STARTED, params);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        log.debug("ACTIVITY RESUMED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            MpireNxusMeasurement.trackEvent(TrackingEvents.ACTIVITY_RESUMED, params);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        log.debug("ACTIVITY PAUSED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            MpireNxusMeasurement.trackEvent(TrackingEvents.ACTIVITY_PAUSED, params);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        log.debug("ACTIVITY STOPPED: " + activity.getClass().getName());

        if (TrackingWorker.getValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, activity.getApplicationContext())) {
            TrackingParams params = new TrackingParams();
            params.put(TRACKING_PARAMS_ACTIVITY, activity.getClass().getName());
            MpireNxusMeasurement.trackEvent(TrackingEvents.ACTIVITY_STOPPED, params);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

}
