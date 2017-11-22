package com.nxus.dsp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nxus.dsp.BuildConfig;
import com.nxus.dsp.dto.DataContainer;
import com.nxus.dsp.dto.DataKeys;
import com.nxus.dsp.dto.IConstants;
import com.nxus.dsp.logging.Logger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;

/**
 * 
 * TrackingDeviceInformation
 * Basic hardware/software info for users device
 * 
 * @author Zeljko Drascic
 *
 */
public class DeviceInformationUtils {
    public static final Logger log = Logger.getLog(DeviceInformationUtils.class);
    
    String networkMacSha1;
    String networkMacShortMd5; // device_fingerprint_id
    
    String networkIpAddress;
    String networkConnectionType; // network_connection_type
    String networkSimOperator; // network_sim_operator
    String networkSimCountry; // network_sim_country

    String trustDeviceAndroidId;

    String deviceType; // device_type
    String deviceOsName; // device_os
    String deviceOsVersion; // device_os_version
    String deviceApiLevel; // device_api_level
    String deviceModel; // device_model
    String deviceManufacturer; // device_manufacturer;
    String deviceHardwareName; // device_hardvare_name
    
    String deviceScreenSize; // device_screen_size
    String deviceScreenFormat; // device_screen_format
    String deviceScreenDensity; // device_screen_dpi
    String deviceDisplayWidth; // device_screen_width
    String deviceDisplayHeight; // device_screen_height
    
    String deviceLanguage; // device_lang
    String deviceCountry; // device_country
    
    String deviceUserAgent; // device_user_agent
    String deviceGooglePlayStoreAdvertId = "";
    String deviceFingerPrint = "";
    String deviceABI; // device_abi

    String playReferrer;
    long playClickTimestamp;
    long playInstallBeginTimestamp;

    String applicationUserUuid; // unique id for tracking a combination of device + app install
    
    String applicationPackageName; // app_package_name
    String applicationPackageVersion; // app_package_version
    String applicationPackageVersionCode; // app_package_version_code
    String applicationInstallTime;
    String applicationFirstRunTime;
    
    String sdkUserId;
    String sdkVersion;
    String sdkPlatform;
    
    TreeMap<String, String> trackingDeviceInfo;
//    List<String> deviceInstalledApplications;
    
    private boolean isGooglePlayServicesAvailable;
    
    Context context;

    /**
     * Initialization of DeviceInformationUtils.
     * @param context
     */
    public DeviceInformationUtils(Context context) {
        this.context = context;
        
        log.info("device information pickup");
        
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        Locale locale = configuration.locale;
        int screenLayout = configuration.screenLayout;
        
        trackingDeviceInfo = new TreeMap<String, String>();

        trustDeviceAndroidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        trustDeviceAndroidId = Utils.hash(trustDeviceAndroidId, IConstants.MD5);
        
        /*
        String macAddress = NetworkUtils.getDeviceMacAddress(context);        
        networkMacSha1 = getMacSha1(macAddress);*/
        
        isGooglePlayServicesAvailable = (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS);
        String macAddress = "";
        
        if (!isGooglePlayServicesAvailable) {
            macAddress = NetworkUtils.getDeviceMacAddress(context);
            networkMacShortMd5 = getMacShortMd5(macAddress);            
        }
                
        networkConnectionType = NetworkUtils.getCurrentConnectionType(context);
        networkIpAddress = NetworkUtils.getDeviceIpAddress();
        
        deviceOsName = getDeviceOsName();
        deviceOsVersion = getDeviceOsVersion();
        deviceApiLevel = getDeviceApiLevel();
        deviceType = getDeviceType(screenLayout);
        deviceModel = getDeviceModel();
        deviceManufacturer = getDeviceManufacturer(); 
        deviceHardwareName = getDeviceHardwareName();
        deviceUserAgent = getDeviceUserAgent(context);
        
        deviceLanguage = getDeviceLanguage(locale);
        deviceCountry = getDeviceCountry(locale);
        
        applicationPackageName = getApplicationPackageName(context);
        applicationPackageVersion = getApplicationVersion(context);
        applicationPackageVersionCode = getApplicationVersionCode(context); 
        applicationInstallTime = getApplicationInstallTime(context, applicationPackageName);
        applicationFirstRunTime = getApplicationFirstRunTime(context);
//        deviceInstalledApplications = getInstalledApplications(context);
        
        sdkVersion = BuildConfig.VERSION_NAME;
        sdkPlatform = IConstants.SDK_PLATFORM;
        
        deviceScreenSize = getScreenSize(screenLayout);
        deviceScreenFormat = getScreenFormat(screenLayout);
        deviceScreenDensity = getScreenDensity(displayMetrics);
        deviceDisplayWidth = getDisplayWidth(displayMetrics);
        deviceDisplayHeight = getDisplayHeight(displayMetrics);
        
        deviceABI = getDeviceABICapabilities();
    }

    /**
     * Returns device model.
     * @return device model
     */
    private String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * Returns device manufacturer.
     * @return device manufacturer
     */
    private String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * Returns device OS.
     * @return "android"
     */
    private String getDeviceOsName() {
        return "android";
    }

    /**
     * Returns device OS version.
     * @return device OS version
     */
    private String getDeviceOsVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Returns device API level.
     * @return device API level/version of Android SDK
     */
    private String getDeviceApiLevel() {
        return Integer.toString(Build.VERSION.SDK_INT);
    }
    

    /**
     * Resolve the application package name.
     * @param context
     * @return application package name
     */
    public String getApplicationPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * Returns application version.
     * @param context
     * @return application version name
     */
    private String getApplicationVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            return info.versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Returns application version code.
     * @param context
     * @return application version code
     */
    private String getApplicationVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            return String.valueOf(info.versionCode);
        } catch (NameNotFoundException e) {
            return null;
        }
    }    

    /**
     * Based on SCREENLAYOUT_SIZE_* returns device type
     * @param screenLayout
     * @return phone / tablet
     */
    private String getDeviceType(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "phone";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "tablet";
            default:
                return null;
        }
    }


    /**
     * Returns device language.
     * @param locale
     * @return 2-letter language code
     */
    private String getDeviceLanguage(Locale locale) {
        return locale.getLanguage();
    }

    /**
     * Returns device country.
     * @param locale
     * @return 2-letter country code based on set locale
     */
    private String getDeviceCountry(Locale locale) {
        return locale.getCountry();
    }

    /**
     * Returns device hardware name.
     * @return device hardware name
     */
    private String getDeviceHardwareName() {
        return Build.DISPLAY;
    }

    /**
     * Returns size of screen.
     * @param screenLayout
     * @return small/normal/large/xlarge
     */
    private String getScreenSize(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return IConstants.SIZE_SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return IConstants.SIZE_NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return IConstants.SIZE_LARGE;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return IConstants.SIZE_XLARGE;
            default:
                return null;
        }
    }

    /**
     * Return Android Screen format.
     * @param screenLayout
     * @return normal/long
     */
    private String getScreenFormat(int screenLayout) {
        int screenFormat = screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;

        switch (screenFormat) {
            case Configuration.SCREENLAYOUT_LONG_YES:
                return IConstants.SIZE_LONG;
            case Configuration.SCREENLAYOUT_LONG_NO:
                return IConstants.SIZE_NORMAL;
            default:
                return null;
        }
    }

    /**
     * Return Android Screen DPI.
     * @param displayMetrics
     * @return low/high/medium
     */
    private String getScreenDensity(DisplayMetrics displayMetrics) {
        int density = displayMetrics.densityDpi;
        
        int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;

        if (density == 0) {
            return null;
        } else if (density < low) {
            return IConstants.DPI_LOW;
        } else if (density > high) {
            return IConstants.DPI_HIGH;
        }
        return IConstants.DPI_MEDIUM;
    }

    
    /**
     * Return Android Screen Resolution Width.
     * @param displayMetrics
     * @return width in pixels
     */
    private String getDisplayWidth(DisplayMetrics displayMetrics) {
        return String.valueOf(displayMetrics.widthPixels);
    }

    /**
     * Return Android Screen Resolution Height
     * @param displayMetrics
     * @return height in pixels
     */
    private String getDisplayHeight(DisplayMetrics displayMetrics) {
        return String.valueOf(displayMetrics.heightPixels);
    }


    /**
     * Hash device mac address. For science.
     * @param macAddress
     * @return hashed value of passed in macAddress
     */
    private String getMacSha1(String macAddress) {
        
        if (macAddress == null) {
            return null;
        }
        
        return Utils.hash(macAddress, IConstants.SHA1);
    }

    /**
     * MD5 Hash version of MAC address.
     * @param macAddress
     * @return hashed value of passed in macAddress
     */
    private String getMacShortMd5(String macAddress) {
        
        if (macAddress == null) {
            return null;
        }
        
        return Utils.hash(macAddress.replaceAll(":", ""), IConstants.MD5);        
    }

    
    /**
     * @param context
     * @param isGooglePlayServicesAvailable // replace this!
     * @return
     */
    private String getAndroidId(Context context, boolean isGooglePlayServicesAvailable) {
        if (!isGooglePlayServicesAvailable) {
            return Utils.getAndroidId(context);
        } else {
            return null;
        }
    }

    /**
     * Get Application Binary Interface for current device
     * @return
     */
    private String getDeviceABICapabilities() {
        // use SUPPORTED_ABIS for API 21+
        String[] SupportedABIS = Utils.getSupportedAbis();

        // use deprecated CPU_ABI
        if (SupportedABIS == null || SupportedABIS.length == 0) {
            return Utils.getCpuAbi();
        }

        return SupportedABIS[0];
    }
    
    /**
     * Create a user agent from device data.
     * @param context
     * @return created user-agent
     */
    private static String getDeviceUserAgent(Context context) {
        try {
            StringBuffer buffer = new StringBuffer();

            final String version = Build.VERSION.RELEASE;
            
            if (version.length() > 0) {
                buffer.append(version);
            } else {
                // default to "1.0"
                buffer.append("1.0");
            }
            
            buffer.append("; ");
            buffer.append("en");
            buffer.append(";");
            
            // add the model for the release build
            if ("REL".equals(Build.VERSION.CODENAME)) {
                final String model = Build.MODEL;
                if (model.length() > 0) {
                    buffer.append(" ");
                    buffer.append(model);
                }
            }
            final String id = Build.ID;
            
            if (id.length() > 0) {
                buffer.append(" Build/");
                buffer.append(id);
            }
            
            int webUserAgentTargetContentResourceId = Resources.getSystem().getIdentifier("web_user_agent_target_content", "string", "android");           
            String mobile = "";
            if (webUserAgentTargetContentResourceId > 0) {
                mobile = context.getResources().getText(webUserAgentTargetContentResourceId).toString();
            }
            
            int webUserAgentResourceId = Resources.getSystem().getIdentifier("web_user_agent", "string", "android");
            final String base = context.getResources().getText(webUserAgentResourceId).toString();
            final String userAgent = String.format(base, buffer, mobile);

            return userAgent;
        }
        catch (Throwable t) {
        	log.error("Exception caught while generating user agent: " + t.getMessage());
            return "Android";
        }    
    }

    /**
     * Get first run time from SharedPreferences.
     * @param context
     * @return firstRunTime
     */
    private String getApplicationFirstRunTime(Context context) {
        long firstRun = DataContainer.getInstance().pullValueLong(DataKeys.APP_FIRST_RUN, context);
        if (firstRun == 0) {            
            firstRun = System.currentTimeMillis();
            DataContainer.getInstance().storeValueLong(DataKeys.APP_FIRST_RUN, firstRun, context);
        }
        
        return Utils.convertMillisAndFormatDate(firstRun);
    }

    /**
     * Get application install time from PackageManager.
     * @param context
     * @param packageName
     * @return appInstallTime
     */
    private String getApplicationInstallTime(Context context, String packageName) {
        long time = 0;
        
        try {
            time = context.getPackageManager().getPackageInfo(packageName, 0).firstInstallTime;
        } catch (NameNotFoundException e) {
        	log.error(e.getMessage(), e);
        }

        return Utils.convertMillisAndFormatDate(time);
    }
    

    /**
     * List of installed application on device to be sent with first_app_launch/app_start events
     * @param context
     * @return list of application package names
     */
//    private List<String> getInstalledApplications(Context context) {
//        final PackageManager pm = context.getPackageManager();
//        List<String> response = new ArrayList<String>();
//        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
//
//        for (ApplicationInfo packageInfo : packages) {
//            if (!isSystemPackage(packageInfo)) {
//                response.add(packageInfo.packageName);
//            }
//        }
//
//        return response;
//    }
    
    /**
     * Check if application is a system app. Used for filtering out system apps from list of all installed apps.
     * @param packageInfo
     * @return true/false
     */
    private boolean isSystemPackage(ApplicationInfo packageInfo) {
        return ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }

    
    /**
     * @param context
     * @return
     */
    public static String getGooglePlaystoreAdId(Context context) {
        String googlePlaystoreAdId = DataContainer.getInstance().pullValueString(DataKeys.GOOGLE_ADVERTISER_ID, context);
        log.trace("1: ", googlePlaystoreAdId);
        return googlePlaystoreAdId;
    }
    
    /**
     * Debug device stored data
     * @return
     */
    public void debugData() {
    	log.debug("... debug data ...");
        for (Map.Entry<String, String> entry : trackingDeviceInfo.entrySet()) {
        	log.debug(entry.getKey() + " : " + entry.getValue());
        } 
    }

    private String getApplicationUserUuid() {
        String combination = deviceFingerPrint + applicationPackageName + applicationInstallTime;
        String hashedCombo = Utils.hash(combination, IConstants.MD5);

        String response = "";
        String chunks[] = StringUtils.splitByNumber(hashedCombo, 4);
        String delimiter = "";
        for (String st : chunks) {
            response = response + delimiter + st;
            delimiter = "-";
        }

        return response;
    }
    
    /**
     * Prepairs device data for later use from TrackingWorker.
     */
    public void prepareInformations() {
        playReferrer = DataContainer.getInstance().pullValueString(DataKeys.PLAY_INSTALL_REFERRER, this.context);
        playClickTimestamp = DataContainer.getInstance().pullValueLong(DataKeys.PLAY_REF_CLICK_TIMESTAMP, this.context);
        playInstallBeginTimestamp = DataContainer.getInstance().pullValueLong(DataKeys.PLAY_INSTALL_BEGIN_TIMESTAMP, this.context);

        deviceGooglePlayStoreAdvertId = DataContainer.getInstance().pullValueString(DataKeys.GOOGLE_ADVERTISER_ID, this.context);
        deviceFingerPrint = DataContainer.getInstance().pullValueString(DataKeys.FINGERPRINT, context);
        if (deviceFingerPrint.equalsIgnoreCase("")) {
            deviceFingerPrint = Utils.prepareFingerprint(isGooglePlayServicesAvailable ? deviceGooglePlayStoreAdvertId : networkMacShortMd5);
            DataContainer.getInstance().storeValueString(DataKeys.FINGERPRINT, deviceFingerPrint, context);
        }

        applicationUserUuid = getApplicationUserUuid();

        trackingDeviceInfo.put(DataKeys.DI_TRUST_DEVICE_ID, trustDeviceAndroidId);
        trackingDeviceInfo.put(DataKeys.DI_APP_USER_UUID, applicationUserUuid);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_FINGERPRINT_ID, deviceFingerPrint);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_GOOGLE_ADVERT_ID, deviceGooglePlayStoreAdvertId);
        trackingDeviceInfo.put(DataKeys.DI_AAID, deviceGooglePlayStoreAdvertId);

        trackingDeviceInfo.put(DataKeys.DI_PLAY_REFERRER, playReferrer);
        trackingDeviceInfo.put(DataKeys.DI_PLAY_REF_CLICK_TIMESTAMP, Long.toString(playClickTimestamp));
        trackingDeviceInfo.put(DataKeys.DI_PLAY_INSTALL_BEGIN_TIME, Long.toString(playInstallBeginTimestamp));

        trackingDeviceInfo.put(DataKeys.DI_NETWORK_CONNECTION_TYPE, networkConnectionType);
        trackingDeviceInfo.put(DataKeys.DI_NETWORK_IP, networkIpAddress);
        
        trackingDeviceInfo.put(DataKeys.DI_NETWORK_SIM_OPERATOR, networkSimOperator);
        trackingDeviceInfo.put(DataKeys.DI_NETWORK_SIM_COUNTRY, networkSimCountry);
        
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_TYPE, deviceType);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_OS, deviceOsName);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_OS_VERSION, deviceOsVersion);

        trackingDeviceInfo.put(DataKeys.DI_DEVICE_API_LEVEL, deviceApiLevel);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_MODEL, deviceModel);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_MANUFACTURER, deviceManufacturer);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_HARDWARE_NAME, deviceHardwareName);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_SCREEN_SIZE, deviceScreenSize);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_SCREEN_FORMAT, deviceScreenFormat);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_SCREEN_DPI, deviceScreenDensity);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_SCREEN_WIDTH, deviceDisplayWidth);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_SCREEN_HEIGHT, deviceDisplayHeight);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_LANG, deviceLanguage);        
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_COUNTRY, deviceCountry);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_USER_AGENT, deviceUserAgent);
        trackingDeviceInfo.put(DataKeys.DI_DEVICE_ABI, deviceABI);
        
        trackingDeviceInfo.put(DataKeys.DI_APP_PACKAGE_NAME, applicationPackageName);
        trackingDeviceInfo.put(DataKeys.DI_APP_PACKAGE_VERSION, applicationPackageVersion);
        trackingDeviceInfo.put(DataKeys.DI_APP_PACKAGE_VERSION_CODE, applicationPackageVersionCode);
        trackingDeviceInfo.put(DataKeys.DI_APP_INSTALL_TIME, applicationInstallTime);        
        trackingDeviceInfo.put(DataKeys.DI_APP_FIRST_LAUNCH, applicationFirstRunTime);
        
        trackingDeviceInfo.put(DataKeys.DI_SDK_VERSION, sdkVersion);
        trackingDeviceInfo.put(DataKeys.DI_SDK_PLATFORM, sdkPlatform);
    }

    /**
     * @return the deviceInstalledApplications
     */
//    public List<String> getDeviceInstalledApplications() {
//        return deviceInstalledApplications;
//    }

    /**
     * @param deviceInstalledApplications the deviceInstalledApplications to set
     */
//    public void setDeviceInstalledApplications(List<String> deviceInstalledApplications) {
//        this.deviceInstalledApplications = deviceInstalledApplications;
//    }

    /**
     * @return the trackingDeviceInfo
     */
    public TreeMap<String, String> getTrackingDeviceInfo() {
        return trackingDeviceInfo;
    }

    /**
     * @param trackingDeviceInfo the trackingDeviceInfo to set
     */
    public void setTrackingDeviceInfo(TreeMap<String, String> trackingDeviceInfo) {
        this.trackingDeviceInfo = trackingDeviceInfo;
    }
        
    
}

