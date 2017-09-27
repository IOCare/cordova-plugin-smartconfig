var exec = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	startConfig:function(apSsid,apBssid,apPassword,isSsidHiddenStr,taskResultCountStr,successCallback, errorCallback){
		exec(successCallback, errorCallback, "espSmartconfig", "startConfig", [apSsid,apBssid,apPassword,isSsidHiddenStr,taskResultCountStr]);
	},
	
	stopConfig:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "espSmartconfig", "stopConfig", []);
	},
    getNetworklist: function(options, win, fail) {
        if (typeof options === 'function') {
            fail = win;
            win = options;
            options = {};
        }

        if (typeof win != "function") {
            console.log("getNetworklist first parameter must be a function to handle list.");
            return;
        }

        cordova.exec(win, fail, 'espSmartconfig', 'getNetworklist', [options]);
    }


}