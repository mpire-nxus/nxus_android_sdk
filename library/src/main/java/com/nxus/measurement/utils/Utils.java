package com.nxus.measurement.utils;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.nxus.measurement.dto.DataContainer;
import com.nxus.measurement.dto.DataKeys;
import com.nxus.measurement.dto.IConstants;
import com.nxus.measurement.logging.Logger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;

/**
 * DSP Tracking Library Utility Class
 * @author TechMpire Ltd.
 */
public class Utils {

    public static final Logger log = Logger.getLog(Utils.class);

    private static final String DATE_FORMAT_TEMPLATE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_TEMPLATE);

    /**
     * Get supported CPU ABIs
     * @return
     */
    public static String[] getSupportedAbis() {
        String[] supportedAbis = null;
        try {
            Class buildClass = forName("android.os.Build");
            Field supportedAbisField = buildClass.getField("SUPPORTED_ABIS");
            Object supportedAbisObject = supportedAbisField.get(null);

            if (supportedAbisObject instanceof String[]) {
                supportedAbis = (String[]) supportedAbisObject;
            }

        } catch (Exception e) {}

        return supportedAbis;
    }

    /**
     * Get supported CPU ABIs (deprecated from v21)
     * @return
     */
    public static String getCpuAbi() {
        String cpuAbi = null;
        try {
            Class buildClass = forName("android.os.Build");
            Field cpuAbiField = buildClass.getField("CPU_ABI");
            Object cpuAbiObject = cpuAbiField.get(null);
            if (cpuAbiObject instanceof String) {
                cpuAbi = (String) cpuAbiObject;
            }
        } catch (Exception e) {}

        return cpuAbi;
    }

    /**
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * Hash a sent string value with selected method.
     * @param text
     * @param method SHA-1/MD5
     * @return
     */
    public static String hash(final String text, final String method) {
        String hashString = null;
        try {
            final byte[] bytes = text.getBytes(IConstants.DEFAULT_ENCODING);
            final MessageDigest mesd = MessageDigest.getInstance(method);
            mesd.update(bytes, 0, bytes.length);
            final byte[] hash = mesd.digest();
            hashString = StringUtils.convertToHex(hash);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return hashString;
    }

    @SuppressWarnings("rawtypes")
    public static Class forName(String className) {
        try {
            Class classObject = Class.forName(className);
            return classObject;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return null;
        }
    }

    /**
     * Check if permission is given to app.
     * @param context
     * @param permission
     * @return true/false
     */
    public static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @param timeInMillis
     * @return
     */
    public static String convertMillisAndFormatDate(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Prepare hashed device id in readable format
     * @param deviceGooglePlayStoreAdvertId
     * @return
     */
    public static String prepareFingerprint(String deviceGooglePlayStoreAdvertId) {
        String response = "";
        if ((deviceGooglePlayStoreAdvertId != null) && !deviceGooglePlayStoreAdvertId.equals("")) {
            String initFingerPrint = hash(deviceGooglePlayStoreAdvertId, IConstants.MD5);
            String chunks[] = StringUtils.splitByNumber(initFingerPrint, 4);
            String delimiter = "";

            for (String st : chunks) {
                response = response + delimiter + st;
                delimiter = "-";
            }
        }

        return response;
    }

    /**
     * Reads DSP API key from AndroidManifest.xml and stores it in SharedPreferences for easier access from the app.
     * @param context
     */
    public static void updateApiKeyFromManifest(Context context) {
        String dspToken = getValueFromManifest(context, DataKeys.NXUS_DSP_TOKEN);

        if ((dspToken == null) || dspToken.equals("none")) {
            log.error(DataKeys.NXUS_DSP_TOKEN + " not found in AndroidManifest.xml");
        } else {
            DataContainer.getInstance().storeValueString(DataKeys.DSP_API_KEY, dspToken, context);
        }
    }

    /**
     * Reads DSP API key from AndroidManifest.xml.
     * @param applicationContext
     * @return
     */
    public static String getValueFromManifest(Context applicationContext, String manifestValue) {
        ApplicationInfo ai;
        String value = "none";
        try {
            ai = applicationContext.getPackageManager().getApplicationInfo(applicationContext.getPackageName(), PackageManager.GET_META_DATA);
            value = (String) ai.metaData.get(manifestValue);
        } catch (NameNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return value;
    }



}
