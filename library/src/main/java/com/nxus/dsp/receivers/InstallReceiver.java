package com.nxus.dsp.receivers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.nxus.dsp.dto.DataKeys;
import com.nxus.dsp.dto.IConstants;
import com.nxus.dsp.logging.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

/**
 * Receiver for parsing referrer data after install from Google Play.
 */
public class InstallReceiver extends BroadcastReceiver {
	
    public static final Logger log = Logger.getLog(InstallReceiver.class);
    
    public static final String SHARED_PREFS_FILENAME = DataKeys.SHP_DSP_STORAGE;

    /**
     *  Keep these three constants here just as a reminder
     *  that they are available in referrer parameter after parsing. 
     */
    public static final String UTM_CLICK_ID = "dsp_clickid";
    public static final String UTM_AFFILIATE_ID = "dsp_affiliateid";
    public static final String UTM_CAMPAIGN_ID = "dsp_campaignid";
   
    @Override
    public void onReceive(Context context, Intent intent) {        
        Bundle extras = intent.getExtras();
        String referrerString = extras.getString(IConstants.INSTALL_REFERRER);
        
        if (!referrerString.equalsIgnoreCase("")) {
            try {
                Map<String, String> getParams = getHashMapFromQuery(referrerString);
                SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor preferencesEditor = preferences.edit();

                for (Map.Entry<String,String> entry : getParams.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value != null) {
                        preferencesEditor.putString(key, value);
                        log.debug("ref: " + key + " - " + value);
                    }                   
                }
           
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    preferencesEditor.commit();
                } else {
                    preferencesEditor.apply();
                }

            } catch (UnsupportedEncodingException e) {  
            	log.error("Referrer Error: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Parsing key-values from referrer string.
     * @param query
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> getHashMapFromQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> responsePairs = new LinkedHashMap<String, String>();  
        String[] pairs = query.split("&");
        for (String pair : pairs) {
        	log.debug("pair: " + pair);
            if (!pair.equalsIgnoreCase("")) {               
                int idx = pair.indexOf("=");
                responsePairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),  URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        
        return responsePairs;
    }    
}
