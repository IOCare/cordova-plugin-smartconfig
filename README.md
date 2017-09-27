# cordova-plugin-smartconfig

A cordova plugin for Expressif Esptouch protocol which is used to smartconfig esp8266 & ESP32
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

#Warning 

You must call "espSmartconfig.stopConfig" when you want to stop the config,if not it will make some mistake when you call
"esptouchPlugin.smartConfig" the other time.


iOS emulator does not support the SmartConfig feature.