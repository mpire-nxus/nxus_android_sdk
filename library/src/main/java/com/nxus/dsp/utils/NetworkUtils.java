package com.nxus.dsp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

import com.nxus.dsp.logging.Logger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

/**
 * 
 * Network information utilities
 * @author Zeljko Drascic
 *
 */
public class NetworkUtils {
    
	public static final Logger log = Logger.getLog(NetworkUtils.class);
	
    /**
     * Read Device Mac Address
     * @param context
     * @return
     */
    public static String getDeviceMacAddress(Context context) {
        final String macAddress = getMacAddress(context);
        
        if (macAddress == null) {
            return "";
        }
        
        log.debug("macAddress: " + macAddress);
        
        return StringUtils.cleanSpaceCharacterFromString(macAddress.toUpperCase(Locale.UK));
    }

    /**
     * Read MAC address without requiring the ACCESS_WIFI_STATE permission.
     * @param context
     * @return
     */
    private static String getMacAddress(Context context) {
        // android devices should have a wlan address
        final String wlanAddress = readAddressFromInterface("wlan0");
        
        if (wlanAddress != null) {
            return wlanAddress.replace("\n", "").toUpperCase();
        }

        final String ethAddress = readAddressFromInterface("eth0");
        if (ethAddress != null) {
            return ethAddress.replace("\n", "").toUpperCase();
        }

        // backup -> read from Wifi!
        if (!Utils.checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
        	return "";
        } else {
        	try {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                @SuppressWarnings("MissingPermission") final String wifiAddress = wifiManager.getConnectionInfo().getMacAddress();
                
                if (wifiAddress != null) {
                    return wifiAddress;
                }
            } catch (Exception e) {
            	log.debug(e.getMessage(), e);
            }	
        }

        return null;
    }

    /**
     * Read MAC address without requiring the ACCESS_WIFI_STATE permission.
     * @param interfaceName
     * @return
     */
    private static String readAddressFromInterface(final String interfaceName) {
        try {
            final String filePath = "/sys/class/net/" + interfaceName + "/address";
            final StringBuilder fileData = new StringBuilder(1000);
            final BufferedReader reader = new BufferedReader(new FileReader(filePath), 1024);
            final char[] buf = new char[1024];
            int numRead;

            String readData;
            while ((numRead = reader.read(buf)) != -1) {
                readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }

            reader.close();
            return fileData.toString();
        } catch (IOException e) {
            return null;
        }
    }
    
    
    /**
     * Returns current connection type from TelephonyManager.
     * @param context
     * @return 2G/3G/4G/Unknown
     */
    public static String getCurrentConnectionType(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);      
            NetworkInfo info = cm.getActiveNetworkInfo();
           
            if (info == null || !info.isConnected()) return "-"; //not connected            
            if (info.getType() == ConnectivityManager.TYPE_WIFI) return "WIFI";
            
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int networkType = info.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        return "2G";
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        return "3G";
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        return "4G";
                    default:
                        return "Unknown";
                 }
            }
            return "Unknown";
    }
    
    /**
     * IP address from currently active interface.
     * @return
     */
    public static String getDeviceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    
                    log.debug("inetAddress: " + inetAddress);
                    
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
        	log.error(ex.getMessage(), ex);
        }
        
        return null;
    }

}
