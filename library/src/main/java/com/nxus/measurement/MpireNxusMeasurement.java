package com.nxus.measurement;

import com.nxus.measurement.app.NxusActivityLifecycleCallbacks;
import com.nxus.measurement.logging.LogLevel;
import com.nxus.measurement.tracking.CustomTrackingEvents;
import com.nxus.measurement.tracking.ITrackingConstants;
import com.nxus.measurement.tracking.TrackingItem;
import com.nxus.measurement.utils.Utils;
import com.nxus.measurement.dto.IConstants;
import com.nxus.measurement.logging.Logger;
import com.nxus.measurement.tracking.TrackingParams;
import com.nxus.measurement.tracking.TrackingWorker;

import android.app.Application;
import android.content.Context;

/**
 *
 * MpireNxusMeasurement Library for TechMpire Advertising Network
 * @author TechMpire Ltd.
 *
 */
public class MpireNxusMeasurement {

    public static final Logger log = Logger.getLog(MpireNxusMeasurement.class);

    private static MpireNxusMeasurement instance;

    /**
     * Initializing the library. Also, auto-sends the first_app_launch/app_start event.
     * @param application
     */

    public static void initializeLibrary(Application application) {
        initializeLibrary(application, true);
    }

    public static void initializeLibrary(Application application, boolean trackActivities) {
        log.info("Starting tracking: " + IConstants.SDK_PLATFORM + ": " + BuildConfig.VERSION_NAME);

        if (instance == null) {
            TrackingWorker.storeValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, false, application.getApplicationContext());
            instance = new MpireNxusMeasurement(application.getApplicationContext());
            if (trackActivities) {
                application.registerActivityLifecycleCallbacks(new NxusActivityLifecycleCallbacks());
            }
        }
    }

    /**
     * Reading the API key from AndroidManifest.xml file and sending first_app_launch/app_start event.
     * @param context
     */
    private MpireNxusMeasurement(Context context) {
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
            log.error(getErrorInitializeMessage());
        } else {
            TrackingWorker.track(event, params);
        }
    }

    /**
     * Tracking an Install event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventInstall(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.INSTALL_INDEX, CustomTrackingEvents.INSTALL_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking an Open event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventOpen(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.OPEN_INDEX, CustomTrackingEvents.OPEN_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking a Registration event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventRegistration(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.REGISTRATION_INDEX, CustomTrackingEvents.REGISTRATION_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking a Purchase event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventPurchase(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.PURCHASE_INDEX, CustomTrackingEvents.PURCHASE_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking a Level event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventLevel(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.LEVEL_INDEX, CustomTrackingEvents.LEVEL_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking a Tutorial event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventTutorial(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.TUTORIAL_INDEX, CustomTrackingEvents.TUTORIAL_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking an Add to Cart event
     * @param params Map of key-value parameters.
     */
    public static void trackEventAddToCart(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.ADD_TO_CART_INDEX, CustomTrackingEvents.ADD_TO_CART_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking a Checkout event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventCheckout(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.CHECKOUT_INDEX, CustomTrackingEvents.CHECKOUT_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking an Invite event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventInvite(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.INVITE_INDEX, CustomTrackingEvents.INVITE_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    /**
     * Tracking an Achievement event.
     * @param params Map of key-value parameters.
     */
    public static void trackEventAchievement(TrackingParams params) {
        if (instance == null) {
            log.error(getErrorInitializeMessage());
        } else {
            TrackingItem trackingItem = new TrackingItem(CustomTrackingEvents.ACHIEVEMENT_INDEX, CustomTrackingEvents.ACHIEVEMENT_NAME, params);
            TrackingWorker.track(trackingItem);
        }
    }

    public static void setLogLevel(LogLevel logLevel) {
        Logger.setLevel(logLevel);
    }

    private static String getErrorInitializeMessage() {
        return "You have to call MpireNxusMeasurement.initializeLibrary(Context context) first!";
    }

}
