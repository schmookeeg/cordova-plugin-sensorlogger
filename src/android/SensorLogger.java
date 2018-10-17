/**
 * Sensor logger by Dario Salvi </dariosalvi78@gmail.com>
 * License: MIT
 */
package org.apache.cordova.sensorlogger;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.Call;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public class SensorLogger extends CordovaPlugin {

    private static final String LOG_NAME = SensorLogger.class.getName();

    boolean logging = false;

    List<OutputStreamWriter> writers = new LinkedList<OutputStreamWriter>();

    //standard sensors stuff
    SensorManager sensorMng;
    List<String> sensortypes = new LinkedList<String>();
    List<Sensor> sensors = new LinkedList<Sensor>();
    List<SensorEventListener> sensorListeners = new LinkedList<SensorEventListener>();

    //orientation stuff
    float[] mGravity;
    float[] mGeomagnetic;
    SensorEventListener orientationListener;

    //location stuff
    LocationManager locationMng;
    LocationListener locListener = null;

    //permissions stuff
    List<String> perms = new LinkedList<String>();
    private static final int REQUEST_DYN_PERMS = 55;
    CallbackContext authReqCallbackCtx;

    CallbackContext cb;

    /**
     * Sets the context of the Command.
     *
     * @param cordova the context of the main Activity.
     * @param webView the associated CordovaWebView.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        locationMng = (LocationManager) webView.getContext().getSystemService(Context.LOCATION_SERVICE);
        sensorMng = (SensorManager) webView.getContext().getSystemService(Context.SENSOR_SERVICE);
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
        cb = callbackContext;

        if (action.equalsIgnoreCase("init")) {
			if(logging){
				stop();
			}
            sensortypes.clear();
            try{
                JSONArray strings = args.getJSONArray(0);
                for(int i=0; i<strings.length(); i++)
                    sensortypes.add(strings.getString(i));
                callbackContext.success(strings);
            } catch (JSONException ex){
                callbackContext.error(ex.getMessage());
                return true;
            }
            requestPermissions(callbackContext, sensortypes);
            return true;
        } else if (action.equalsIgnoreCase("start")) {
            sensors.clear();
            sensorListeners.clear();
            writers.clear();
            start(callbackContext);
            return true;
        } else if (action.equalsIgnoreCase("stop")) {
            stop();
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void requestPermissions(final CallbackContext callbackContext, List<String> sensortypes) {
        if (!cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        for (String type : sensortypes) {
            if(type.equalsIgnoreCase("location")){
                if (!cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                }
                if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else if (type.equals("heartrate")) {
                if (!cordova.hasPermission(Manifest.permission.BODY_SENSORS)) {
                    perms.add(Manifest.permission.BODY_SENSORS);
                }
            }
        }
        if (perms.isEmpty()) {
            // nothing to be done
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
        } else {
            authReqCallbackCtx = callbackContext;
            cordova.requestPermissions(this, REQUEST_DYN_PERMS, perms.toArray(new String[perms.size()]));
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (requestCode == REQUEST_DYN_PERMS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    String errmsg = "Permission denied ";
                    for (String perm : permissions) {
                        errmsg += " " + perm;
                    }
                    authReqCallbackCtx.error("Permission denied: " + permissions[i]);
                    return;
                }
            }
            //all accepted!
            authReqCallbackCtx.success();
        }
    }

    public void start(final CallbackContext callbackContext) {
        if(logging)
            return;

        for (String type : sensortypes) {
            //start file
            /*
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                Log.e(LOG_NAME, "SD card not writeable");
                callbackContext.error("External storage is not writeable");
            }

            File file = new File(Environment.getExternalStorageDirectory() + "/" + type + ".csv");
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                Log.e(LOG_NAME, "SD card not writeable");
                e.printStackTrace();
                callbackContext.error("External storage is not writeable");
            }
            final OutputStreamWriter out = new OutputStreamWriter(outputStream);
            writers.add(out);
            */

            if(type.equalsIgnoreCase("location")){
                locListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        long ts = location.getTime();
                        float acc = location.getAccuracy();
                        
                        String line = "-2,Location," + ts + "," + acc + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getSpeed() + "," + location.getBearing();
                        try {
                            //cb.success(line);
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, line);
                            pluginResult.setKeepCallback(true);
                            cb.sendPluginResult(pluginResult);
                            //out.flush();
                        } catch  (Exception ex) {
                            Log.e(SensorLogger.class.getName(), "Error while writing log on file", ex);
                        }
                    }
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        //nothing to do
                    }
                    @Override
                    public void onProviderEnabled(String provider) {
                        //nothing to do
                    }
                    @Override
                    public void onProviderDisabled(String provider) {
                        //nothing to do
                    }
                };
            } else if (type.equalsIgnoreCase("orientation")){
                orientationListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                            mGravity = event.values;
                        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                            mGeomagnetic = event.values;
                        if (mGravity != null && mGeomagnetic != null) {
                            float R[] = new float[9];
                            float I[] = new float[9];
                            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                            if (success && (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)) {
                                float orientation[] = new float[3];
                                SensorManager.getOrientation(R, orientation);
                                long ts = event.timestamp;
                                int acc = event.accuracy;
                                String line = "-1,Orientation," + ts + "," + acc;
                                for (int j = 0; j < orientation.length; j++) {
                                    line += "," + orientation[j];
                                }
                                //line += "\n";
                                try {
                                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, line);
                                    pluginResult.setKeepCallback(true);
                                    cb.sendPluginResult(pluginResult);
                                    //cb.success(line);
                                } catch (Exception ex) {
                                    Log.e(SensorLogger.class.getName(), "Error while writing log on file", ex);
                                }
                            }
                        }
                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
            } else {
                if (type.equals("accelerometer")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                } else if (type.equals("gravity")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_GRAVITY));
                } else if (type.equals("gyroscope")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
                } else if (type.equals("rotation")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
                } else if (type.equals("magnetometer")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
                } else if (type.equals("light")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_LIGHT));
                } else if (type.equals("setpcounter")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));
                } else if (type.equals("heartrate")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_HEART_RATE));
                } else if (type.equals("temperature")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));
                } else if (type.equals("pressure")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_PRESSURE));
                } else if (type.equals("proximity")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_PROXIMITY));
                } else if (type.equals("humidity")) {
                    sensors.add(sensorMng.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY));
                } else {
                    throw new IllegalArgumentException("Unknown sensor type " + type);
                }

                sensorListeners.add(new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        long ts = event.timestamp;
                        int acc = event.accuracy;
                        String name = event.sensor.getName();
                        String tname = String.valueOf(event.sensor.getType());
                        String line = tname + "," + name + "," + ts + "," + acc;
                        for (int j = 0; j < event.values.length; j++) {
                            line += "," + event.values[j];
                        }
                        //line += "\n";
                        try {
                            //cb.success(line);
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, line);
                            pluginResult.setKeepCallback(true);
                            cb.sendPluginResult(pluginResult);
                        } catch (Exception ex) {
                            Log.e(SensorLogger.class.getName(), "Error while writing log on file", ex);
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        //nothing to do here
                    }
                });
            }
        }

        for (int i = 0; i < sensors.size(); i++) {
            sensorMng.registerListener(sensorListeners.get(i), sensors.get(i), SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(orientationListener != null){
            sensorMng.registerListener(orientationListener, sensorMng.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
            sensorMng.registerListener(orientationListener, sensorMng.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (locListener != null) {
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            try {
                locationMng.requestLocationUpdates(500, 0, crit, locListener, null);
            } catch (SecurityException ex) {
                Log.e(LOG_NAME, "Cannot access location, permission denied", ex);
            }
        }
        logging = true;

        //callbackContext.success();
    }

    private void stop() {
        if(!logging)
            return;

        for (int i = 0; i < sensors.size(); i++) {
            sensorMng.unregisterListener(sensorListeners.get(i));
        }
        if(orientationListener != null){
            sensorMng.unregisterListener(orientationListener);
            orientationListener = null;
        }
        if (locListener != null) {
            try{
                locationMng.removeUpdates(locListener);
            } catch (SecurityException ex){
                Log.e(SensorLogger.class.getName(), "Cannot access location, permission denied", ex);
            }
            locListener = null;
        }

        for(OutputStreamWriter writer : writers){
            try {
                writer.close();
            } catch (IOException ex) {
                Log.e(SensorLogger.class.getName(), "Error while closing log file", ex);
            }
        }
        logging = false;
    }
}
