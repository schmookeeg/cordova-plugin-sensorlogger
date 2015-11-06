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
    private SensorLogger logger;
    CordovaWebView view;

    /**
     * Sets the context of the Command.
     *
     * @param cordova the context of the main Activity.
     * @param webView the associated CordovaWebView.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.view = webView;
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
            String[] sensors;
            try{
                JSONArray strings = args.getJSONArray(0);
                sensors = new String[strings.length()];
                for(int i=0; i<strings.length(); i++)
                    sensors[i] = strings.getString(i);
            }catch (JSONException ex){
                callbackContext.error(ex.getMessage());
                return true;
            }
            try{
                logger = new SensorLogger(this.view.getContext(), sensors);
            } catch (IOException ex){
                callbackContext.error(ex.getMessage());
                return true;
            }

            callbackContext.success();
            return true;
        } else if (action.equalsIgnoreCase("start")) {
            logger.start();
            callbackContext.success();
            return true;
        } else if (action.equalsIgnoreCase("stop")) {
            logger.stop();
            callbackContext.success();
            return true;
        }
        return false;
    }
}
