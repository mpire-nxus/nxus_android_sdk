package com.nxus.measurement.dto;

import java.util.Iterator;
import java.util.TreeMap;

import org.json.JSONObject;

import com.nxus.measurement.logging.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * Class for easier access to SharedPreferences and saving/pulling values to/from it.
 * @author TechMpire Ltd.
 *
 */
public class DataContainer {
    
	public static final Logger log = Logger.getLog(DataContainer.class);
	
	private static final String SHARED_PREFS_FILENAME = DataKeys.SHP_DSP_STORAGE;
    
    private static DataContainer instance = null;
    
    public static DataContainer getInstance() {
        if (instance == null){
            instance = new DataContainer();
        }
        
        return instance;
    }

    public void storeValueBoolean(String storeName, boolean storeValue, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(storeName, storeValue).apply();
    }

    public boolean pullValueBoolean(String storeName, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(storeName, false);
    }

    public void storeValueLong(String storeName, long storeValue, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(storeName, storeValue).apply();
    }

    public long pullValueLong(String storeName, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        return (prefs.getLong(storeName, 0));        
    }

    public void storeValueString(String storeName, String storeValue, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        prefs.edit().putString(storeName, storeValue).apply();
    }

    public String pullValueString(String storeName, Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        return (prefs.getString(storeName, ""));        
    }

    private void storeValueMap(String storeName, TreeMap<String, String> inputMap, Context ctx){
        SharedPreferences pSharedPref = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            Editor editor = pSharedPref.edit();
            editor.remove(storeName).commit();
            editor.putString(storeName, jsonString);
            editor.commit();
        }
    }

    private TreeMap<String, String> pullValueMap(String storeName, Context ctx){
        TreeMap<String, String> outputMap = new TreeMap<String, String>();
        SharedPreferences pSharedPref = ctx.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE);
        
        try{
            if (pSharedPref != null){       
                String jsonString = pSharedPref.getString(storeName, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = (String) jsonObject.get(key);  
                    outputMap.put(key, value);
                }
            }
        } catch(Exception e){
            log.error(e.getMessage(), e);
        }
        
        return outputMap;
    }

}
