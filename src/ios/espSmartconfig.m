#import "espSmartconfig.h"

@interface EspTouchDelegateImpl : NSObject<ESPTouchDelegate>
@property (nonatomic, strong) CDVInvokedUrlCommand *command;
@property (nonatomic, weak) id <CDVCommandDelegate> commandDelegate;

@end

@implementation EspTouchDelegateImpl

-(void) onEsptouchResultAddedWithResult: (ESPTouchResult *) result
{
    NSString *InetAddress=[ESP_NetUtil descriptionInetAddr4ByData:result.ipAddrData];
    NSString *text=[NSString stringWithFormat:@"bssid=%@,InetAddress=%@",result.bssid,InetAddress];
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: text];
    [pluginResult setKeepCallbackAsBool:true];
    //[self.commandDelegate sendPluginResult:pluginResult callbackId:self.command.callbackId];  //add by lianghuiyuan
}
@end


@implementation espSmartconfig

- (void) startConfig:(CDVInvokedUrlCommand *)command{
    [self.commandDelegate runInBackground:^{
        dispatch_queue_t  queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
        
        [self._condition lock];
        NSString *apSsid = (NSString *)[command.arguments objectAtIndex:0];
        NSString *apBssid = (NSString *)[command.arguments objectAtIndex:1];
        NSString *apPwd = (NSString *)[command.arguments objectAtIndex:2];
        NSString *isSsidHiddenStr=(NSString *)[command.arguments objectAtIndex:3];
        
        BOOL isSsidHidden = true;
        if([isSsidHiddenStr compare:@"NO"]==NSOrderedSame){
            isSsidHidden=false;
        }
        int taskCount = [[command.arguments objectAtIndex:4] intValue];
        
        NSLog(@"ssid: %@, bssid: %@, apPwd: %@", apSsid, apBssid, apPwd);
        //        self._esptouchTask =
        //        [[ESPTouchTask alloc]initWithApSsid:apSsid andApBssid:apBssid andApPwd:apPwd andIsSsidHiden:isSsidHidden]; // deprecated
        self._esptouchTask =
        [[ESPTouchTask alloc]initWithApSsid:apSsid andApBssid:apBssid andApPwd:apPwd];
        EspTouchDelegateImpl *esptouchDelegate=[[EspTouchDelegateImpl alloc]init];
        esptouchDelegate.command=command;
        esptouchDelegate.commandDelegate=self.commandDelegate;
        [self._esptouchTask setEsptouchDelegate:esptouchDelegate];
        [self._condition unlock];
        NSArray * esptouchResultArray = [self._esptouchTask executeForResults:taskCount];
        
        dispatch_async(queue, ^{
            // show the result to the user in UI Main Thread
            dispatch_async(dispatch_get_main_queue(), ^{
                
                
                ESPTouchResult *firstResult = [esptouchResultArray objectAtIndex:0];
                // check whether the task is cancelled and no results received
                if (!firstResult.isCancelled)
                {
                    //NSMutableString *mutableStr = [[NSMutableString alloc]init];
                    //NSUInteger count = 0;
                    // max results to be displayed, if it is more than maxDisplayCount,
                    // just show the count of redundant ones
                    //const int maxDisplayCount = 5;
                    if ([firstResult isSuc])
                    {
                        
                        //                    for (int i = 0; i < [esptouchResultArray count]; ++i)
                        //                    {
                        //                        ESPTouchResult *resultInArray = [esptouchResultArray objectAtIndex:i];
                        //                        [mutableStr appendString:[resultInArray description]];
                        //                        [mutableStr appendString:@"\n"];
                        //                        count++;
                        //                        if (count >= maxDisplayCount)
                        //                        {
                        //                            break;
                        //                        }
                        //                    }
                        //
                        //                    if (count < [esptouchResultArray count])
                        //                    {
                        //                        [mutableStr appendString:[NSString stringWithFormat:@"\nthere's %lu more result(s) without showing\n",(unsigned long)([esptouchResultArray count] - count)]];
                        //                    }
                        
                        ESPTouchResult *resultInArray = [esptouchResultArray objectAtIndex:0];
                        NSString *ipaddr = [ESP_NetUtil descriptionInetAddr4ByData:resultInArray.ipAddrData];
                        // device0 I think is suppose to be the index
                        NSString *result = [NSString stringWithFormat:@"Finished: device0,bssid=%@,InetAddress=%@.", resultInArray.bssid, ipaddr];
                        
                        //                        NSDictionary* returnObj = @{@"ipaddr": ipaddr};
                        CDVPluginResult* pluginResult = nil;
                        //                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:result];
                        
                        [pluginResult setKeepCallbackAsBool:true];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    }
                    
                    else
                    {
                        CDVPluginResult* pluginResult = nil;
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"Esptouch fail"];
                        [pluginResult setKeepCallbackAsBool:true];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    }
                }
                
            });
        });
    }];
}


- (void) stopConfig:(CDVInvokedUrlCommand *)command{
    [self._condition lock];
    if (self._esptouchTask != nil)
    {
        [self._esptouchTask interrupt];
    }
    [self._condition unlock];
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"cancel success"];
    [pluginResult setKeepCallbackAsBool:true];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getNetworklist:(CDVInvokedUrlCommand*)command {
    CDVPluginResult *pluginResult = nil;
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Not supported"];
    
    [self.commandDelegate sendPluginResult:pluginResult
                                callbackId:command.callbackId];
}

@end

