package com.nxus.dsp;

import com.nxus.dsp.app.NxusActivityLifecycleCallbacks;
import com.nxus.dsp.logging.LogLevel;
import com.nxus.dsp.tracking.ITrackingConstants;
import com.nxus.dsp.utils.Utils;
import com.nxus.dsp.dto.IConstants;
import com.nxus.dsp.logging.Logger;
import com.nxus.dsp.tracking.TrackingParams;
import com.nxus.dsp.tracking.TrackingWorker;

import android.app.Application;
import android.content.Context;

/**
 * 
 * NxusDSPTracker Library for TechMpire Advertising Network
 * @author TechMpire Ltd.
 *
 */
public class NxusDSPTracker {
	
	public static final Logger log = Logger.getLog(NxusDSPTracker.class);
    
    private static NxusDSPTracker instance; 
    
    /**
     * Initializing the library. Also, auto-sends the first_app_launch/app_start event.
     * @param application
     */

    public static void initializeLibrary(Application application) {
        initializeLibrary(application, true);
    }

    public static void initializeLibrary(Application application, boolean trackActivities) {
        log.info("Starting DSP Tracking: " + IConstants.SDK_PLATFORM + ": " + BuildConfig.VERSION_NAME);
        
        if (instance == null) {
            TrackingWorker.storeValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, false, application.getApplicationContext());
            instance = new NxusDSPTracker(application.getApplicationContext());
            if (trackActivities) {
                application.registerActivityLifecycleCallbacks(new NxusActivityLifecycleCallbacks());
            }
        }
    }

    /**
     * Reading the API key from AndroidManifest.xml file and sending first_app_launch/app_start event.
     * @param context
     */
    private NxusDSPTracker(Context context) {
    	log.info("processInitialization");
    	
        Utils.updateApiKeyFromManifest(context);
        TrackingWorker.trackLaunch(context);
    }
    
    /**
     * Tracking an event.
     * @param event Event name to be sent.
     */
    public static void trackEvent(String event) {
        trackEvent(event, null);
    }

    /**
     * Tracking an event with parameters.
     * @param event Event name to be sent.
     * @param params Map of key-value parameters.
     */
    public static void trackEvent(String event, TrackingParams params) {
    	if (instance == null) {
            log.error("You have to call NxusDSPTracker.initializeLibrary(Context context) first!");
        } else {
        	TrackingWorker.track(event, params);
        }
    }

    public static void setLogLevel(LogLevel logLevel) {
        Logger.setLevel(logLevel);
    }
}
