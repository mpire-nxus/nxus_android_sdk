package com.nxus.measurement.utils;

import java.math.BigInteger;
import java.util.Locale;

import com.nxus.measurement.dto.IConstants;

import android.text.TextUtils;

/**
 * String utilities from DSP library
 * @author TechMpire Ltd.
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

    public static String testAndConvertArabDate(String input) {
        if (isProbablyArabic(input)) {
            return (input.replace("١", "1").replace("٢", "2").replace("٣", "3").replace("٤", "4").replace("٥", "5").replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9").replace("٠", "0"));
        }
        return input;
    }

    private static boolean isProbablyArabic(String s) {
        for (int i = 0; i < s.length();) {
            int c = s.codePointAt(i);
            if (c >= 0x0600 && c <= 0x06E0) return true;
            i += Character.charCount(c);
        }
        return false;
    }
}
