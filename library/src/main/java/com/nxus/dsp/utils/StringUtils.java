package com.nxus.dsp.utils;

import java.math.BigInteger;
import java.util.Locale;

import com.nxus.dsp.dto.IConstants;

import android.text.TextUtils;

/**
 * 
 * String utilities from DSP library
 * @author Zeljko Drascic
 *
 */
public class StringUtils {
    

    /**
     * Remove space character from string.. cleanup
     * @param dirtyString
     * @return
     */
    public static String cleanSpaceCharacterFromString(final String dirtyString) {
        if (dirtyString == null) {
            return null;
        }
        
        String cleanString = dirtyString.replaceAll("\\s", "");
        
        if (TextUtils.isEmpty(cleanString)) {
            return null;
        }

        return cleanString;
    }
    
    /**
     * @param bytes
     * @return
     */
    public static String convertToHex(final byte[] bytes) {
        final BigInteger bigInt = new BigInteger(1, bytes);
        final String formatString = "%0" + (bytes.length << 1) + "x";
        return String.format(Locale.US, formatString, bigInt);
    }

    /**
     * @param macAddress
     * @return
     */
    private String getMacAddressShortMd5(String macAddress) {
        
        if (macAddress == null) {
            return null;
        }
        
        return Utils.hash(macAddress.replaceAll(":", ""), IConstants.MD5);
    }
    
    /**
     * @param s
     * @param chunkSize
     * @return
     */
    public static String[] splitByNumber(String s, int chunkSize){
        if (chunkSize == 0 ) chunkSize++; // o
        int chunkCount = (s.length() / chunkSize) + (s.length() % chunkSize == 0 ? 0 : 1);
        String[] returnVal = new String[chunkCount];
        
        for(int i = 0; i < chunkCount; i++){
            returnVal[i] = s.substring(i*chunkSize, Math.min((i+1)*chunkSize, s.length()));
        }
        
        return returnVal;
    }
}
