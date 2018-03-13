package com.nxus.measurement.tracking;

import java.io.IOException;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.nxus.measurement.dto.DataContainer;
import com.nxus.measurement.dto.DataKeys;
import com.nxus.measurement.logging.Logger;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Reading GoogleAdvertiserId from AdvertisingIdClient. Only when Play Store is installed on device - Google Play services available.
 * @author TechMpire Ltd.
 */
public class GetAsyncGoogleAdvertiserId extends AsyncTask<Void, Void, String> {

    public static final Logger log = Logger.getLog(GetAsyncGoogleAdvertiserId.class);

    final Context ctx;
    private GoogleAdvertisingTaskPlayReferrerDelegate delegate;

    public GetAsyncGoogleAdvertiserId(GoogleAdvertisingTaskPlayReferrerDelegate taskDelegate, Context context) {
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
     * Calling onTaskCompletionResult method on GoogleAdvertisingTaskPlayReferrerDelegate (i.e. TrackingWorker) to continue with sending event after ID is resolved.
     * @param advert
     */
    @Override
    protected void onPostExecute (String advert) {
        log.debug("onPostExecute: " + advert);
        DataContainer.getInstance().storeValueString(DataKeys.GOOGLE_ADVERTISER_ID, advert, ctx);
        DataContainer.getInstance().storeValueBoolean(DataKeys.GOOGLE_AAID_FETCHED, true, ctx);
        delegate.onTaskCompletionResult();
    }
}