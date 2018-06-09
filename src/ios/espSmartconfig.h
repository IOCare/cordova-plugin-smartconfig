#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVPluginResult.h>
#import <Esptouch/ESPTouchTask.h>
#import <Esptouch/ESPTouchResult.h>
#import <Esptouch/ESP_NetUtil.h>
#import <Esptouch/ESPTouchDelegate.h>



@interface espSmartconfig : CDVPlugin
@property (nonatomic, strong) NSCondition *_condition;
@property (atomic, strong) ESPTouchTask *_esptouchTask;

- (void)startConfig:(CDVInvokedUrlCommand*)command;

- (void)stopConfig:(CDVInvokedUrlCommand*)command;
- (void)getNetworklist:(CDVInvokedUrlCommand*)command;

@end
