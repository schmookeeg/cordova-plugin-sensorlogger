cordova.define("cordova-plugin-sensorlogger.SensorLogger", function(require, exports, module) {

var exec = require("cordova/exec");

var SensorLogger = function () {
    this.name = "SensorLogger";
};

SensorLogger.prototype.init = function (sensors, onSuccess, onError) {
    exec(onSuccess, onError, "SensorLogger", "init", [sensors]);
};

SensorLogger.prototype.start = function (onSuccess, onError) {
    exec(onSuccess, onError, "SensorLogger", "start", []);
};

SensorLogger.prototype.stop = function (onSuccess, onError) {
    exec(onSuccess, onError, "SensorLogger", "stop", []);
};


module.exports = new SensorLogger();

});
