package com.nxus.dsp.tracking;

import java.io.IOException;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.nxus.dsp.dto.DataContainer;
import com.nxus.dsp.dto.DataKeys;
import com.nxus.dsp.logging.Logger;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Reading GoogleAdvertiserId from AdvertisingIdClient. Only when Play Store is installed on device - Google Play services available.
 */
public class GetAsyncGoogleAdvertiserId extends AsyncTask<Void, Void, String> {
	
	public static final Logger log = Logger.getLog(GetAsyncGoogleAdvertiserId.class);
	
    final Context ctx;
    private GoogleAdvertisingTaskDelegate delegate;

    /**
     * 
     * @param taskDelegate
     * @param context
     */
    public GetAsyncGoogleAdvertiserId(GoogleAdvertisingTaskDelegate taskDelegate, Context context) {            
        super();
        ctx = context;
        delegate = taskDelegate;
    }

    protected String doInBackground(Void... params) {
        AdvertisingIdClient.Info idInfo = null;
        try {
            idInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx);
        } catch (GooglePlayServicesNotAvailableException e) {
        	log.error(e.getMessage(), e);
        } catch (GooglePlayServicesRepairableException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        
        String advertId = "";
        try {
            advertId = idInfo.getId();
        } catch (NullPointerException e){
            log.error(e.getMessage(), e);
        }
        
        return advertId;
    }

    /**
     * Calling onTaskCompletionResult method on GoogleAdvertisingTaskDelegate (i.e. TrackingWorker) to continue with sending event after ID is resolved.
     * @param advert
     */
    @Override
    protected void onPostExecute (String advert) {  
    	log.debug("onPostExecute: " + advert);
        DataContainer.getInstance().storeValueString(DataKeys.GOOGLE_ADVERTISER_ID, advert, ctx);
        delegate.onTaskCompletionResult(advert);
    }
}