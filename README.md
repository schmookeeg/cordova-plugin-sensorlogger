# Cordova sensors logging plugin
A cordova plugin that logs sensors on file.
Useful for research purposes.

**only works with Android (for the moment)**

## Install

```
cordova plugin add https://github.com/dariosalvi78/cordova-plugin-sensorlogger.git
```

## Usage
Files are saved on the external storage (the SD card) on its root.
The format of the file is CSV, the first column always being the Unix timestamp of when the sample was received.
Each sensor log is stored into a different file with the name of the sensor as the file name.

#### init
Initialises the logging plugin.

```js
sensorlogger.init(sensors, successCallback, failureCallback);
```
- => `sensors` an array of names of the sensors to be logged, supported sensors are: accelerometer, location, orientation, gravity, gyroscope, rotation, magnetometer, light, setpcounter, heartrate
- => `successCallback` is called if OK
- => `failureCallback` is called if there was an error (eg wrong format of arguments, or file not writeable)

#### start
Starts logging.

```js
sensorlogger.start(successCallback, failureCallback);
```
- => `successCallback` is called if OK
- => `failureCallback` is called if there was an error

#### stop
Stops logging.

```js
sensorlogger.stop(successCallback, failureCallback);
```
- => `successCallback` is called if OK
- => `failureCallback` is called if there was an error

## HW support

At the moment the plugin only works on Android phones.
It uses the localisation API and the Sensor API, so all sensors exposed are potentially accessible, though the following ahve been implemented until now: accelerometer, location, orientation, gravity, gyroscope, rotation, magnetometer, light, setpcounter, heartrate