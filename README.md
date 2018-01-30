# cordova-plugin-smartconfig

A cordova plugin for Expressif Esptouch protocol which is used to smartconfig esp8266 & ESP32.
It's a modified version of original code at https://github.com/xumingxin7398/cordovaEsptouch

# Install

cordova plugin add https://github.com/IOCare/cordova-plugin-smartconfig.git

# Usage
1. espSmartconfig.startConfig 

```
//@apSsid,ssid of the wifi,for example: "wifiName"
//@apBssid,bssid of the wifi,for example "b2:05:2f:92" 
//@apPassword,password of the wifi,for example: "wifiPassword" 
//@isSsidHiddenStr,default "NO"
//@taskResultCountStr,the count of device you want to config,for example:1


	espSmartconfig.startConfig(apSsid,apBssid,apPassword,isSsidHiddenStr,taskResultCountStr, function(res) {
	  alert(res);
	},function(error){
	  console.log(error);
	});
```

2. espSmartconfig.stopConfig


```
	espSmartconfig.stopConfig(function(res) {
		console.log(res);
	}, function(error) {
		console.log(error);
	});
```
3. espSmartconfig.getNetworklist([options], listHandler, fail);

Retrieves a list of the available networks as an array of objects and passes them to the function listHandler. The format of the array is:

```
networks = [
    {   "level": signal_level, // raw RSSI value
        "SSID": ssid, // SSID as string, with escaped double quotes: "\"ssid name\""
        "BSSID": bssid // MAC address of WiFi router as string
        "frequency": frequency of the access point channel in MHz
        "capabilities": capabilities // Describes the authentication, key management, and encryption schemes supported by the access point.
    }
]
```
Example usage:

```
	espSmartconfig.getNetworklist({numLevels: false}, $scope.listHandler, $scope.fail);


	$scope.listHandler = function(ssids) {
		console.log(ssids.SSID);
		console.log(ssids.BSSID);
	};

	$scope.fail = function(e) {
		console.log(e);
	};


```
An options object may be passed. Currently, the only supported option is numLevels, and it has the following behavior:

if (n == true || n < 2), *.getNetworklist({numLevels: n}) will return data as before, split in 5 levels;
if (n > 1), *.getNetworklist({numLevels: n}) will calculate the signal level, split in n levels;
if (n == false), *.getNetworklist({numLevels: n}) will use the raw signal level;

#Warning 

You must call "espSmartconfig.stopConfig" when you want to stop the config,if not "espSmartconfig.startConfig" won't work if called next time.
