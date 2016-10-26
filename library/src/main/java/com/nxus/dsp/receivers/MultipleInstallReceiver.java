package com.nxus.dsp.receivers;

import java.util.List;

import com.nxus.dsp.logging.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

/**
 * Receiver for passing the INSTALL_REFERRER broadcast to all registered receivers. 
 */
public class MultipleInstallReceiver extends BroadcastReceiver {
	
    public static final Logger log = Logger.getLog(MultipleInstallReceiver.class);
    private static final String INTENT_FILTER_ACTION = "com.android.vending.INSTALL_REFERRER";
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	String action = intent.getAction();
    	
    	List<ResolveInfo> receivers = context.getPackageManager().queryBroadcastReceivers(new Intent(INTENT_FILTER_ACTION), 0);
    	for (ResolveInfo resolveInfo : receivers) {
			if ((resolveInfo.activityInfo.packageName.equals(context.getPackageName())) && (INTENT_FILTER_ACTION.equals(action)) && (!getClass().getName().equals(resolveInfo.activityInfo.name))) {
				try {
					BroadcastReceiver broadcastReceiver = (BroadcastReceiver)Class.forName(resolveInfo.activityInfo.name).newInstance();
					broadcastReceiver.onReceive(context, intent);
					log.debug("FOUND_BROADCAST_RECEIVER", resolveInfo.activityInfo.name);
		        } catch (Throwable e) {
		        	log.error("error in BroadcastReceiver " + resolveInfo.activityInfo.name, e);
		        }
			}
		}
    	
    	InstallReceiver installReceiver = new InstallReceiver();
    	installReceiver.onReceive(context, intent);
    }
    
}
