var exec = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	startConfig:function(apSsid,apBssid,apPassword,isSsidHiddenStr,taskResultCountStr,successCallback, errorCallback){
		exec(successCallback, errorCallback, "espSmartconfig", "startConfig", [apSsid,apBssid,apPassword,isSsidHiddenStr,taskResultCountStr]);
	},
	
	stopConfig:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "espSmartconfig", "stopConfig", []);
	}
}