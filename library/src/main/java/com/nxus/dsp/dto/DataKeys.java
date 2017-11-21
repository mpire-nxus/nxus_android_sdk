package com.nxus.dsp.dto;

/**
 * Different keys used across the SDK.
 */
public class DataKeys {
	
	public static final String APP_FIRST_RUN 				= "app-first-run";
	public static final String FINGERPRINT 					= "fingerprint";
	
	public static final String GOOGLE_ADVERTISER_ID 		= "gaid";
	
	public static final String DSP_API_KEY					= "dsp.api.key";
	
	// SharedPreferences files
	public static final String SHP_DSP_STORAGE				= "dsp-storage";
	public static final String SHP_TRACKING_PREFS_STORAGE	= "tracking_prefs_storage";
	public static final String SHP_TRACKING_EVENTS_STORAGE	= "tracking_events_storage";
	
	// Manifest keys
	public static final String NXUS_DSP_TOKEN				= "nxus.dsp.token";
	
	// Device information keys
	public static final String DI_TRUST_DEVICE_ID			= "app_install_trust_key";
	public static final String DI_APP_USER_UUID				= "app_user_uuid";
	public static final String DI_DEVICE_FINGERPRINT_ID 	= "device_fingerprint_id";
	public static final String DI_DEVICE_GOOGLE_ADVERT_ID 	= "device_google_advert_id";
	public static final String DI_AAID 						= "aaid";
	public static final String DI_NETWORK_CONNECTION_TYPE	= "network_connection_type";
	public static final String DI_NETWORK_IP				= "network_ip";
	public static final String DI_NETWORK_SIM_OPERATOR		= "network_sim_operator";
	public static final String DI_NETWORK_SIM_COUNTRY		= "network_sim_country";
	public static final String DI_DEVICE_TYPE				= "device_type";
	public static final String DI_DEVICE_OS					= "device_os";
	public static final String DI_DEVICE_OS_VERSION			= "device_os_version";
	public static final String DI_DEVICE_API_LEVEL			= "device_api_level";
	public static final String DI_DEVICE_MODEL				= "device_model";
	public static final String DI_DEVICE_MANUFACTURER		= "device_manufacturer";
	public static final String DI_DEVICE_HARDWARE_NAME		= "device_hardware_name";
	public static final String DI_DEVICE_SCREEN_SIZE		= "device_screen_size";
	public static final String DI_DEVICE_SCREEN_FORMAT		= "device_screen_format";
	public static final String DI_DEVICE_SCREEN_DPI			= "device_screen_dpi";
	public static final String DI_DEVICE_SCREEN_WIDTH		= "device_screen_width";
	public static final String DI_DEVICE_SCREEN_HEIGHT		= "device_screen_height";
	public static final String DI_DEVICE_LANG				= "device_lang";
	public static final String DI_DEVICE_COUNTRY			= "device_country";
	public static final String DI_DEVICE_USER_AGENT			= "device_user_agent";
	public static final String DI_DEVICE_ABI				= "device_abi";
	public static final String DI_APP_PACKAGE_NAME			= "app_package_name";
	public static final String DI_APP_PACKAGE_VERSION		= "app_package_version";
	public static final String DI_APP_PACKAGE_VERSION_CODE	= "app_package_version_code";
	public static final String DI_APP_INSTALL_TIME			= "app_install_time";
	public static final String DI_APP_FIRST_LAUNCH			= "app_first_launch";
	public static final String DI_SDK_VERSION				= "sdk_version";
	public static final String DI_SDK_PLATFORM				= "sdk_platform";
	
	// Tracking keys
//	public static final String TRACK_APPLICATION_STATS		= "application_stats";
	public static final String TRACK_EVENT_NAME				= "event_name";
	public static final String TRACK_EVENT_PARAM			= "event_param";
	public static final String TRACK_EVENT_TIME				= "event_time";
	public static final String TRACK_EVENT_TIME_EPOCH		= "event_time_epoch";
	public static final String TRACK_ATD_CLICK_ID			= "click_id";
	public static final String TRACK_ATD_AFFILIATE_ID		= "affiliate_id";
	public static final String TRACK_ATD_CAMPAIGN_ID		= "campaign_id";
	public static final String TRACK_ATTRIBUTION_DATA		= "attribution_data";
	
	// Request header keys
	public static final String REQ_DSP_TOKEN				= "dsp-token";

}
