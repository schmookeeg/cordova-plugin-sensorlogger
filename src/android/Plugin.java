/**
 * Sensor logger by Dario Salvi </dariosalvi78@gmail.com>
 * License: MIT
 */
 package org.apache.cordova.sensorlogger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Plugin extends CordovaPlugin {

    private static final String LOG_NAME = Plugin.class.getName();
    private SensorsLogger logger;

    /**
     * Sets the context of the Command.
     *
     * @param cordova the context of the main Activity.
     * @param webView the associated CordovaWebView.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.activity = cordova.getActivity();
    }

    /**
     * Executes the request.
     *
     * @param action the action to execute.
     * @param args the exec() arguments.
     * @param callbackContext the callback context used when calling back into JavaScript.
     * @return whether the action was valid.
     */
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
    
        if (action.equalsIgnoreCase("init")) {
			if(logger != null){
				logger.stop();
			}
			logger = new SensorsLogger();
			
            
            callbackContext.success();
            return true;
        } else if (action.equalsIgnoreCase("start")) {
            

            return true;
        } else if (action.equalsIgnoreCase("stop")) {
            
           return true;
        }
    }
}
