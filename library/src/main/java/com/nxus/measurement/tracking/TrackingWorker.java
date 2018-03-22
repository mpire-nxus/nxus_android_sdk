package com.nxus.measurement.tracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nxus.measurement.dto.DataContainer;
import com.nxus.measurement.dto.DataKeys;
import com.nxus.measurement.dto.IConstants;
import com.nxus.measurement.logging.Logger;
import com.nxus.measurement.receivers.InstallReceiver;
import com.nxus.measurement.utils.DeviceInformationUtils;
import com.nxus.measurement.utils.StringUtils;
import com.nxus.measurement.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

import static com.android.installreferrer.api.InstallReferrerClient.newBuilder;


/**
 * Main Tracking Worker
 * @author TechMpire ltd
 *
 */
public class TrackingWorker implements Runnable, GoogleAdvertisingTaskPlayReferrerDelegate {
    public static final Logger log = Logger.getLog(TrackingWorker.class);

    private static final String TRACKING_PREFS_STORAGE  = DataKeys.SHP_TRACKING_PREFS_STORAGE;
    private static final String TRACKING_EVENTS_STORAGE = DataKeys.SHP_TRACKING_EVENTS_STORAGE;

    private DeviceInformationUtils deviceInformationUtils;
    private JSONObject deviceTrackingObjectDefault = null;

    private static TrackingWorker singleton = null;

    private final Thread worker;

    public static String trackingType = "post";
    public static String postEndpoint = "";

    private static InstallReferrerClient mReferrerClient;

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

                        sendEventToPostback(next, apiKey);
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

    private void sendEventToPostback(JSONObject eventObject, String apiKey) {
        String serverUrl = IConstants.SERVER_BASE_URL_POSTBACK;

        try {
            if (String.valueOf(eventObject.get(DataKeys.TRACK_EVENT_NAME)).equalsIgnoreCase(TrackingEvents.FIRST_APP_LAUNCH)) {
                eventObject.put(DataKeys.TRACK_EVENT_INDEX, CustomTrackingEvents.INSTALL_INDEX);
                eventObject.put(DataKeys.TRACK_EVENT_NAME, CustomTrackingEvents.INSTALL_NAME);
            }

            log.debug("Sending tracking event: " + eventObject.get(DataKeys.TRACK_EVENT_NAME) + " : " + eventObject.toString());

            StringBuilder paramsUri = new StringBuilder();
            paramsUri.append(DataKeys.REQ_APP_KEY + "=");
            paramsUri.append(URLEncoder.encode(apiKey, "UTF-8"));
            paramsUri.append("&" + DataKeys.TRACK_EVENT_INDEX + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.TRACK_EVENT_INDEX), "UTF-8"));
            paramsUri.append("&" + DataKeys.TRACK_EVENT_NAME + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.TRACK_EVENT_NAME), "UTF-8"));
            paramsUri.append("&" + DataKeys.TRACK_EVENT_TIME + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.TRACK_EVENT_TIME), "UTF-8"));
            paramsUri.append("&" + DataKeys.TRACK_EVENT_REVENUE_USD + "=");
            paramsUri.append(URLEncoder.encode("", "UTF-8")); // TODO set event_revenue_usd

            JSONObject eventParams = new JSONObject();
            String eventParamsString = eventObject.getString(DataKeys.TRACK_EVENT_PARAM);
            if ((eventParamsString != null) && !eventParamsString.equals("")) {
                String[] eventParamsSplit = eventParamsString.split("&");
                for (String param : eventParamsSplit) {
                    String[] paramSplit = param.split("=");
                    eventParams.put(paramSplit[0], paramSplit[1]);
                }
            }
            paramsUri.append("&" + DataKeys.TRACK_EVENT_PARAM + "=");
            paramsUri.append(URLEncoder.encode(eventParams.toString(), "UTF-8"));

            JSONObject attributionData = eventObject.getJSONObject(DataKeys.TRACK_ATTRIBUTION_DATA);
            paramsUri.append("&" + DataKeys.TRACK_ATD_CLICK_ID + "=");
            paramsUri.append(URLEncoder.encode(attributionData.getString(DataKeys.TRACK_ATD_CLICK_ID), "UTF-8"));
            paramsUri.append("&" + DataKeys.TRACK_ATD_CAMPAIGN_ID + "=");
            paramsUri.append(URLEncoder.encode(attributionData.getString(DataKeys.TRACK_ATD_CAMPAIGN_ID), "UTF-8"));
            paramsUri.append("&" + DataKeys.TRACK_ATD_AFFILIATE_ID + "=");
            paramsUri.append(URLEncoder.encode(attributionData.getString(DataKeys.TRACK_ATD_AFFILIATE_ID), "UTF-8"));

            paramsUri.append("&" + DataKeys.DI_DEVICE_USER_AGENT + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_USER_AGENT), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_ABI + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_ABI), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_ACCEPT_LANGUAGE + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_LANG), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_COUNTRY + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_COUNTRY), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_FINGERPRINT_ID + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_FINGERPRINT_ID), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_HARDWARE_NAME + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_HARDWARE_NAME), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_LANG + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_LANG), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_MANUFACTURER + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_MANUFACTURER), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_MODEL + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_MODEL), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_OS + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_OS), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_OS_VERSION + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_OS_VERSION), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_SCREEN_DPI + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_SCREEN_DPI), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_SCREEN_HEIGHT + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_SCREEN_HEIGHT), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_SCREEN_WIDTH + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_SCREEN_WIDTH), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_TYPE + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_TYPE), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_APP_INSTALL_TIME + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_APP_INSTALL_TIME), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_APP_PACKAGE_NAME + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_APP_PACKAGE_NAME), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_APP_PACKAGE_VERSION + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_APP_PACKAGE_VERSION), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_NETWORK_CONNECTION_TYPE + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_NETWORK_CONNECTION_TYPE), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_AAID + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_AAID), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_USER_IP + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_NETWORK_IP), "UTF-8"));

            paramsUri.append("&" + DataKeys.DI_DEVICE_API_LEVEL + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_API_LEVEL), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_SCREEN_FORMAT + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_SCREEN_FORMAT), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_DEVICE_SCREEN_SIZE + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_DEVICE_SCREEN_SIZE), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_SDK_PLATFORM + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_SDK_PLATFORM), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_SDK_VERSION + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_SDK_VERSION), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_APP_PACKAGE_VERSION_CODE + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_APP_PACKAGE_VERSION_CODE), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_APP_FIRST_LAUNCH + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_APP_FIRST_LAUNCH), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_TRUST_DEVICE_ID + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_TRUST_DEVICE_ID), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_APP_USER_UUID + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_APP_USER_UUID), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_PLAY_REF_CLICK_TIMESTAMP + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_PLAY_REF_CLICK_TIMESTAMP), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_PLAY_INSTALL_BEGIN_TIME + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_PLAY_INSTALL_BEGIN_TIME), "UTF-8"));
            paramsUri.append("&" + DataKeys.DI_PLAY_REFERRER + "=");
            paramsUri.append(URLEncoder.encode(eventObject.getString(DataKeys.DI_PLAY_REFERRER), "UTF-8"));

            byte[] postData = paramsUri.toString().getBytes();
            int postDataLength = postData.length;

            URL url = new URL(serverUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            urlConnection.connect();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write(paramsUri.toString());
            outputStreamWriter.flush();

            int httpResult = urlConnection.getResponseCode();

            log.debug("Sending Event To Postback: " + serverUrl);
            log.debug("POST params: " + paramsUri.toString());
            log.debug("Response code: " + httpResult);

            if (httpResult == HttpURLConnection.HTTP_OK) {
                deleteTrackingObject(eventObject.getString(DataKeys.TRACK_EVENT_TIME_EPOCH));
            } else {
                log.error("onFailure() Response was: " + httpResult);
            }

        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        } catch (ProtocolException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

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

    public static void track(TrackingItem trackingItem) {
        save(trackingItem, context);

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
    public static void trackLaunch(final Context ctx) {
        log.debug("ref-extras: Tracking launcher.trackLaunch");

        if (singleton == null) {
            singleton = new TrackingWorker(ctx);
        }

        long lastLaunch = pullValueLong(ITrackingConstants.CONF_LAST_LAUNCH_INTERNAL, context);

        if (lastLaunch == 0) {
            boolean isGooglePlayServicesAvailable = (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
            if (isGooglePlayServicesAvailable) {
                new GetAsyncGoogleAdvertiserId(singleton, context).execute();

                mReferrerClient = newBuilder(ctx).build();
                mReferrerClient.startConnection(new InstallReferrerStateListener() {
                    @Override
                    public void onInstallReferrerSetupFinished(int responseCode) {
                        switch (responseCode) {
                            case InstallReferrerClient.InstallReferrerResponse.OK:
                                try {
                                    ReferrerDetails response = mReferrerClient.getInstallReferrer();
                                    String installReferrer = response.getInstallReferrer();
                                    long referrerClickTimestampSeconds = response.getReferrerClickTimestampSeconds();
                                    long installBeginTimestampSeconds = response.getInstallBeginTimestampSeconds();

                                    DataContainer.getInstance().storeValueString(DataKeys.PLAY_INSTALL_REFERRER, installReferrer, ctx);
                                    DataContainer.getInstance().storeValueLong(DataKeys.PLAY_REF_CLICK_TIMESTAMP, referrerClickTimestampSeconds, ctx);
                                    DataContainer.getInstance().storeValueLong(DataKeys.PLAY_INSTALL_BEGIN_TIMESTAMP, installBeginTimestampSeconds, ctx);
                                } catch (RemoteException e) {
                                    log.error(e.getMessage(), e);
                                }
                                break;
                            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                                break;
                            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                                break;
                        }
                        DataContainer.getInstance().storeValueBoolean(DataKeys.PLAY_REFERRER_FETCHED, true, ctx);
                        singleton.onTaskCompletionResult();
                    }

                    @Override
                    public void onInstallReferrerServiceDisconnected() {
                        DataContainer.getInstance().storeValueBoolean(DataKeys.PLAY_REFERRER_FETCHED, true, ctx);
                        singleton.onTaskCompletionResult();
                    }
                });
            } else {
                trackLaunchHandler(lastLaunch);
            }
        } else {
            trackLaunchHandler(lastLaunch);
        }
    }

    /**
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

    public static void storeValueLong(String storeName, long storeValue, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(TRACKING_PREFS_STORAGE, Context.MODE_PRIVATE);
        prefs.edit().putLong(storeName, storeValue).apply();
    }

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
            currentTrackingObjects.add(buildTrackingItemObject(data[0], data[1], data[2], Long.parseLong(data[3])));
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
    private JSONObject buildTrackingItemObject(String eventIndex, String event, String params, long time){
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

        try {
            trackingObject.put(DataKeys.TRACK_EVENT_INDEX, eventIndex);
            trackingObject.put(DataKeys.TRACK_EVENT_NAME, event);
            trackingObject.put(DataKeys.TRACK_EVENT_PARAM, params);
            trackingObject.put(DataKeys.TRACK_EVENT_TIME, Utils.convertMillisAndFormatDate(time) + "");
            trackingObject.put(DataKeys.TRACK_EVENT_TIME_EPOCH, time);
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

        trackingObject = trackingObjectCleanup(trackingObject);

        return trackingObject;
    }

    private JSONObject trackingObjectCleanup(JSONObject jsonObject) {
        try {
            jsonObject.put(DataKeys.DI_APP_FIRST_LAUNCH, StringUtils.testAndConvertArabDate(jsonObject.getString(DataKeys.DI_APP_FIRST_LAUNCH)));
            jsonObject.put(DataKeys.DI_APP_INSTALL_TIME, StringUtils.testAndConvertArabDate(jsonObject.getString(DataKeys.DI_APP_INSTALL_TIME)));
            jsonObject.put(DataKeys.TRACK_EVENT_TIME, StringUtils.testAndConvertArabDate(jsonObject.getString(DataKeys.TRACK_EVENT_TIME)));
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }

        return jsonObject;
    }

    // This is needed mostly for first_app_launch event.
    // This event is not supposed to be sent before google_advert_id is read
    // since it must be read in background task.
    @Override
    synchronized public void onTaskCompletionResult() {
        if (DataContainer.getInstance().pullValueBoolean(DataKeys.PLAY_REFERRER_FETCHED, context) && DataContainer.getInstance().pullValueBoolean(DataKeys.GOOGLE_AAID_FETCHED, context)) {
            deviceInformationUtils.prepareInformations();
            deviceInformationUtils.debugData();
            long lastLaunch = pullValueLong(ITrackingConstants.CONF_LAST_LAUNCH_INTERNAL, context);
            trackLaunchHandler(lastLaunch);
        }
    }

}