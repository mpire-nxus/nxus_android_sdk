package com.nxus.dsp.tracking;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nxus.dsp.utils.DeviceInformationUtils;
import com.nxus.dsp.utils.Utils;
import com.nxus.dsp.dto.DataContainer;
import com.nxus.dsp.dto.DataKeys;
import com.nxus.dsp.dto.IConstants;
import com.nxus.dsp.logging.Logger;
import com.nxus.dsp.receivers.InstallReceiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;

import javax.net.ssl.HttpsURLConnection;


/**
 * Main Tracking Worker
 * @author TechMpire ltd
 *
 */
public class TrackingWorker implements Runnable, GoogleAdvertisingTaskDelegate {
    public static final Logger log = Logger.getLog(TrackingWorker.class);

    private static final String TRACKING_PREFS_STORAGE  = DataKeys.SHP_TRACKING_PREFS_STORAGE;
    private static final String TRACKING_EVENTS_STORAGE = DataKeys.SHP_TRACKING_EVENTS_STORAGE;

    private DeviceInformationUtils deviceInformationUtils;
    private JSONObject deviceTrackingObjectDefault = null;

    private static TrackingWorker singleton = null;

    private final Thread worker;

    public static String trackingType = "post";
    public static String postEndpoint = "";

    private static Context context;

    private TrackingWorker(Context ctx) {
        context = ctx;

        deviceInformationUtils = new DeviceInformationUtils(context);
        deviceInformationUtils.prepareInformations();
        deviceInformationUtils.debugData();

        worker = new Thread(this, TRACKING_PREFS_STORAGE);
        worker.start();
    }

    /**
     * Sending tracking data.
     */
    @Override
    public void run() {
        log.info(worker + " worker started. (" + trackingType + ")");

        final String apiKey = DataContainer.getInstance().pullValueString(DataKeys.DSP_API_KEY, context);

        while(true) {
            try {
                ArrayList<JSONObject> currentTrackingObjects = loadCurrentTrackingObjects();

                if (currentTrackingObjects != null && currentTrackingObjects.size() > 0) {
                    for (int i = 0; i < currentTrackingObjects.size(); i++) {
                        JSONObject next = currentTrackingObjects.get(i);
                        String serverUrl = IConstants.SERVER_BASE_URL_EVENT;

                        if (String.valueOf(next.get(DataKeys.TRACK_EVENT_NAME)).equalsIgnoreCase(TrackingEvents.FIRST_APP_LAUNCH)) {
                            log.debug("Sending attribution event: " + next.get(DataKeys.TRACK_EVENT_NAME) + " : " + next.toString());

                            serverUrl = IConstants.SERVER_BASE_URL_ATTRIBUTION;

                            URL url = new URL(serverUrl);
                            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                            urlConnection.setDoOutput(true);
                            urlConnection.setUseCaches(false);
                            urlConnection.setRequestMethod("POST");
                            urlConnection.setRequestProperty("Content-Type", "application/json");
                            urlConnection.setRequestProperty("charset", "utf-8");
                            urlConnection.setRequestProperty(DataKeys.REQ_DSP_TOKEN, apiKey);
                            urlConnection.connect();

                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                            outputStreamWriter.write(next.toString());
                            outputStreamWriter.close();

                            int httpResult = urlConnection.getResponseCode();
                            if (httpResult == HttpURLConnection.HTTP_OK) {
                                sendEventToS3(next, apiKey);
                                deleteTrackingObject(next.getString(DataKeys.TRACK_EVENT_TIME_EPOCH));
                            } else {
                                log.error("onFailure() Response was: " + httpResult);
                            }
                        } else {
                            sendEventToS3(next, apiKey);
                        }
                    }
                }
            } catch(Throwable e) {
                log.error("Tracking error happened", e);
            }

            if (singleton == null) {
                return;
            }

            synchronized(singleton) {
                try {
                    singleton.wait(ITrackingConstants.SLEEP);
                } catch (InterruptedException e) {
                    log.error("Library initialization broke", e);
                }
            }
        }
    }

    private void sendEventToS3(JSONObject eventObject, String apiKey) {
        try {
            log.debug("Sending tracking event: " + eventObject.get(DataKeys.TRACK_EVENT_NAME) + " : " + eventObject.toString());

            String requestString = buildFullDataUrl(eventObject);
            log.debug("requestString: " + requestString);

            try {
                URL url = new URL(requestString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("User-Agent","AndroidSDK/1.0");
                urlConnection.setRequestProperty("Accept","*/*");
                urlConnection.setRequestProperty("Content-Type", "");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty(DataKeys.REQ_DSP_TOKEN, apiKey);

                int httpResult = urlConnection.getResponseCode();
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    deleteTrackingObject(eventObject.getString(DataKeys.TRACK_EVENT_TIME_EPOCH));
                } else {
                    log.error("onFailure() Response was: " + httpResult);
                }
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        } catch (JSONException e) {
            log.error("Tracking error happened", e);
        }
    }

    /**
     * Save build GET url formated for s3 service
     * replacement of problematic chars
     * REMOVE THIS in future and replace event objet builder
     * @param job
     */
    private String buildFullDataUrl(JSONObject job) {
        String responseUrl = IConstants.SERVER_BASE_URL_EVENT;
        String delimiter = "?";
        Iterator<?> keys = job.keys();

        String prefixedPackageName = "android." + context.getPackageName();
        responseUrl += Utils.hash(prefixedPackageName,"SHA-1"); // add app endpoint

        try {
            while( keys.hasNext() ) {
                String key = (String) keys.next();
                if (!key.equalsIgnoreCase(DataKeys.TRACK_EVENT_TIME_EPOCH) && !key.equalsIgnoreCase(DataKeys.TRACK_ATTRIBUTION_DATA)) { // remove debug timecode
                    responseUrl += delimiter + key + "=" + URLEncoder.encode((String)job.get(key), "UTF-8");
                    delimiter = "&";
                }
            }

            // add attribution data
            JSONObject joba = (JSONObject) job.get(DataKeys.TRACK_ATTRIBUTION_DATA);
            if (joba != null) {
                Iterator<?> akeys = joba.keys();
                while(akeys.hasNext()) {
                    String akey = (String) akeys.next();
                    responseUrl += delimiter + akey + "=" + URLEncoder.encode((String)joba.get(akey), "UTF-8");
                    delimiter = "&";
                }

            }
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }

        return responseUrl;
    }

    /**
     * Save build GET url formated for s3 service
     * with base64encoded data + replacement of problematic charas
     * @param jsonData
     */
    /*private String buildDataUrl(String jsonData) {

        String responseUrl = IConstants.SERVER_BASE_URL_EVENT;
        responseUrl += Utils.hash(context.getPackageName(),"SHA-1");
        responseUrl += "?x=" + Base64.encodeToString(jsonData.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);
        //responseUrl += "?x=test";

        //System.out.println("responseUrl: " + responseUrl);

        return responseUrl;
    }*/

    /**
     * Save tracking item and notify tracker thread to send it.
     * @param event
     * @param params
     */
    public static void track(String event, TrackingParams params) {
        TrackingItem ti = new TrackingItem(event, params);
        save(ti, context);

        if (singleton == null) {
            return;
        }

        synchronized (singleton) {
            singleton.notifyAll();
        }
    }

    /**
     * Save launch tracking item and notify tracker thread to send it.
     * @param ctx
     */
    public static void trackLaunch(Context ctx) {
        log.debug("ref-extras: Tracking launcher.trackLaunch");

        if (singleton == null) {
            singleton = new TrackingWorker(ctx);
        }

        long lastLaunch = pullValueLong(ITrackingConstants.CONF_LAST_LAUNCH_INTERNAL, context);

        if (lastLaunch == 0) {
            boolean isGooglePlayServicesAvailable = (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
            if (isGooglePlayServicesAvailable) {
                new GetAsyncGoogleAdvertiserId(singleton, context).execute(); // get Android Advertising ID
            } else {
                trackLaunchHandler(lastLaunch);
            }
        } else {
            trackLaunchHandler(lastLaunch);
        }
    }

    /**
     *
     * Handle tracking event
     * @param lastLaunch
     */
    public static void trackLaunchHandler(long lastLaunch) {
        storeValueLong(ITrackingConstants.CONF_LAST_LAUNCH_INTERNAL, System.currentTimeMillis(), context);
        storeValueBoolean(ITrackingConstants.CONF_LAUNCH_TRACKED_INTERNAL, true, context);

        if (lastLaunch == 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    track(TrackingEvents.FIRST_APP_LAUNCH, null);
                }
            }, 100);
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    track(TrackingEvents.APP_LAUNCH, null);
                }
            }, 100);
        }
    }

    /**
     * @param storeName: DataKeys.APP_FIRST_RUN > store value of applications first runtime
     * @param storeValue
     * @param ctx
     */
    public static void storeValueLong(String storeName, long storeValue, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(TRACKING_PREFS_STORAGE, Context.MODE_PRIVATE);
        prefs.edit().putLong(storeName, storeValue).apply();
    }

    /**
     * @param storeName: DataKeys.APP_FIRST_RUN > store value of applications first runtime
     * @param ctx
     * @return
     */
    public static long pullValueLong(String storeName, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(TRACKING_PREFS_STORAGE, Context.MODE_PRIVATE);
        return (prefs.getLong(storeName, 0));
    }

    public static void storeValueBoolean(String storeName, boolean storeValue, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(TRACKING_PREFS_STORAGE, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(storeName, storeValue).apply();
    }

    public static boolean getValueBoolean(String storeName, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(TRACKING_PREFS_STORAGE, Context.MODE_PRIVATE);
        return (prefs.getBoolean(storeName, false));
    }

    /**
     * @param context
     */
    public static void trackServiceLaunch(Context context) {
        if (singleton == null) {
            singleton = new TrackingWorker(context);
        }
    }

    /**
     * Saving tracking item to shared preferences as String.
     * @param i
     * @param context
     * @return
     */
    //context is passed to ensure item save
    private static synchronized boolean save(TrackingItem i, Context context) {
        SharedPreferences p = context.getSharedPreferences(TRACKING_EVENTS_STORAGE, Context.MODE_PRIVATE); // MODE_PRIVATE -> check for api level 11 > multi!
        SharedPreferences.Editor e = p.edit();
        e.putString(Long.toString(i.getTime()), i.getTrack());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return e.commit();
        } else {
            e.apply();
            return true;
        }
    }

    /**
     * Deleting tracking item from shared preferences after it is successfully sent to server side.
     * @param key
     * @return
     */
    private boolean deleteTrackingObject(String key) {
        SharedPreferences p = context.getSharedPreferences(TRACKING_EVENTS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        e.remove(key);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return e.commit();
        } else {
            e.apply();
            return true;
        }
    }

    /**
     * Returns tracking items for sending.
     * @return
     */
    private ArrayList<JSONObject> loadCurrentTrackingObjects() {
        ArrayList<JSONObject> currentTrackingObjects = new ArrayList<JSONObject>();

        SharedPreferences prefs = context.getSharedPreferences(TRACKING_EVENTS_STORAGE, Context.MODE_PRIVATE);
        if (prefs.getAll().isEmpty()) {
            return null;
        }

        TreeMap<String, ?> keys = new TreeMap<String, Object>(prefs.getAll());
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String data[] = ((String)entry.getValue()).split(";");
            currentTrackingObjects.add(buildTrackingItemObject(data[0], data[1], Long.parseLong(data[2])));
        }

        return currentTrackingObjects;
    }

    /**
     * Build JSON Object for sending to tracking system
     * @param event
     * @param params
     * @param time
     * @return
     */
    private JSONObject buildTrackingItemObject(String event, String params, long time){
        JSONObject trackingObject = null;

        if (deviceTrackingObjectDefault == null) {
            trackingObject = new JSONObject();
            deviceTrackingObjectDefault = new JSONObject();

            for (Entry<String, String> entry : deviceInformationUtils.getTrackingDeviceInfo().entrySet()) {
                try {
                    trackingObject.put(entry.getKey() , entry.getValue());
                    deviceTrackingObjectDefault.put(entry.getKey() , entry.getValue());
                } catch (JSONException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            try {
                trackingObject = new JSONObject(deviceTrackingObjectDefault.toString());
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
        }

//        if (event.equalsIgnoreCase(TrackingEvents.APP_LAUNCH) || event.equalsIgnoreCase(TrackingEvents.FIRST_APP_LAUNCH)) {
//            List<String> applicationData = deviceInformationUtils.getDeviceInstalledApplications();
//            if (applicationData.size() > 0) {
//                String apps = "";
//                String delimiter = "";
//
//                for (int i = 0; i < applicationData.size(); i++) {
//                    apps = apps + delimiter + applicationData.get(i);
//                    delimiter = ";";
//                }
//
//                applicationData = null;
//
//                try {
//                    trackingObject.put(DataKeys.TRACK_APPLICATION_STATS, apps);
//                } catch (JSONException e) {
//                    log.error(e.getMessage(), e);
//                }
//
//            }
//        }

        try {
            trackingObject.put(DataKeys.TRACK_EVENT_NAME, event);
            trackingObject.put(DataKeys.TRACK_EVENT_PARAM, params);
            trackingObject.put(DataKeys.TRACK_EVENT_TIME, Utils.convertMillisAndFormatDate(time) + "");
            trackingObject.put(DataKeys.TRACK_EVENT_TIME_EPOCH, time); // debug!
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }

        try {
            JSONObject attributionObject = new JSONObject();

            if (event.equals(TrackingEvents.FIRST_APP_LAUNCH)) {
                Thread.sleep(5000);
            }

            attributionObject.put(DataKeys.TRACK_ATD_CLICK_ID, DataContainer.getInstance().pullValueString(InstallReceiver.UTM_CLICK_ID, context));
            attributionObject.put(DataKeys.TRACK_ATD_AFFILIATE_ID, DataContainer.getInstance().pullValueString(InstallReceiver.UTM_AFFILIATE_ID, context));
            attributionObject.put(DataKeys.TRACK_ATD_CAMPAIGN_ID, DataContainer.getInstance().pullValueString(InstallReceiver.UTM_CAMPAIGN_ID, context));

            trackingObject.put(DataKeys.TRACK_ATTRIBUTION_DATA, attributionObject);

        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        return trackingObject;
    }

    /**
     * Helper class for getting formatted TrackingItem as delimited string value.
     */
    static class TrackingItem {
        private String event;
        private TrackingParams params;
        private long time;

        /**
         * @param event
         * @param params
         */
        TrackingItem (String event, TrackingParams params) {
            this.event = event;
            this.params = params;
            this.time = System.currentTimeMillis();
        }

        /**
         * @param event
         * @param params
         * @param time
         */
        TrackingItem (String event, TrackingParams params, long time) {
            this.event = event;
            this.params = params;
            this.time = time;
        }

        public String getEvent() {
            return event;
        }

        public TrackingParams getParams() {
            return params;
        }

        public long getTime() {
            return time;
        }

        public String getTrack() {
            String tempParams = "";
            if (params != null) {
                String concatenator = "";
                StringBuilder builder = new StringBuilder();
                for (Entry<String, String> entry : params.entrySet()) {
                    builder.append(concatenator);
                    builder.append(entry.getKey());
                    builder.append("=");
                    builder.append(entry.getValue());

                    concatenator = "&";
                }
                tempParams = builder.toString();
            }
            return event + ";" + tempParams + ";" + time;
        }
    }


    // this is needed mostly for first_app_launch event
    // this event is not supposed to be sent before google_advert_id is read
    // since it must be read in background task... gee thanks google for that 
    @Override
    public void onTaskCompletionResult(String result) {
        deviceInformationUtils.prepareInformations();
        deviceInformationUtils.debugData();
        long lastLaunch = pullValueLong(ITrackingConstants.CONF_LAST_LAUNCH_INTERNAL, context);
        trackLaunchHandler(lastLaunch);
    }

}