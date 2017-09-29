package com.iocare.smartconfig;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.List;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;

//import org.apache.cordova.CallbackContext;
//import org.apache.cordova.CordovaPlugin;
//import org.apache.cordova.PluginResult;
import org.apache.cordova.*;

import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.SupplicantState;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class espSmartconfig extends CordovaPlugin {
	
	private WifiManager wifiManager;
	CallbackContext receivingCallbackContext = null;
	IEsptouchTask mEsptouchTask;
	private static final String TAG = "espSmartconfig";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.wifiManager = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);
    }

	@Override
    public boolean execute(String action, final JSONArray args,final CallbackContext callbackContext) throws JSONException{
        receivingCallbackContext = callbackContext;    //modified by lianghuiyuan
        if (action.equals("startConfig")) {
            final String apSsid = args.getString(0);
            final String apBssid = args.getString(1);
            final String apPassword = args.getString(2);
            final String isSsidHiddenStr = args.getString(3);
            final String taskResultCountStr = args.getString(4);
            final int taskResultCount = Integer.parseInt(taskResultCountStr);
            final Object mLock = new Object();
            cordova.getThreadPool().execute(
            new Runnable() {
                public void run() {
                    synchronized (mLock) {
                        boolean isSsidHidden = false;
                        if (isSsidHiddenStr.equals("YES")) {
                            isSsidHidden = true;
                        }
                        mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, cordova.getActivity());
                        mEsptouchTask.setEsptouchListener(myListener);
                    }
                    List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
                    IEsptouchResult firstResult = resultList.get(0);
                    if (!firstResult.isCancelled()) {
                        int count = 0;
                        final int maxDisplayCount = taskResultCount;
                        if (firstResult.isSuc()) {
                             StringBuilder sb = new StringBuilder();
                             for (IEsptouchResult resultInList : resultList) {
                            	 sb.append("device"+count+",bssid="
                            			 + resultInList.getBssid()
                            			 + ",InetAddress="
                            			 + resultInList.getInetAddress()
                            					 .getHostAddress() + ".");
                            	 count++;
                            	 if (count >= maxDisplayCount) {
                            		 break;
                            	 }
                             }
                             if (count < resultList.size()) {
                            	 sb.append("\nthere's " + (resultList.size() - count)
                            			 + " more resultList(s) without showing\n");
                             }
                            PluginResult result = new PluginResult(PluginResult.Status.OK, "Finished: "+sb);
                            result.setKeepCallback(true);           // keep callback after this call
                            receivingCallbackContext.sendPluginResult(result);
                            //receivingCallbackContext.success("finished");
                        } else {
                            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "No Device Found!");
                            result.setKeepCallback(true);           // keep callback after this call
                            receivingCallbackContext.sendPluginResult(result);
                        }
                    }
                }
            }//end runnable
            );
            return true;
        }
        else if (action.equals("stopConfig")) {
            mEsptouchTask.interrupt();
            PluginResult result = new PluginResult(PluginResult.Status.OK, "Cancel Success");
            result.setKeepCallback(true);           // keep callback after this call
            receivingCallbackContext.sendPluginResult(result);
            return true;
        }
        else if (action.equals("getNetworklist")) {
			return this.getNetworklist(callbackContext, args);
        }
        else{
            callbackContext.error("can not find the function "+action);
            return false;
        }
    }


    /** Code cobtained from WiFiWizard by hoerresb
       *    This method uses the callbackContext.success method to send a JSONArray
       *    of the scanned networks.
       *
       *    @param    callbackContext        A Cordova callback context
       *    @param    data                   JSONArray with [0] == JSONObject
       *    @return    true
       */
    private boolean getNetworklist(CallbackContext callbackContext, JSONArray data) {
        List<ScanResult> scanResults = wifiManager.getScanResults();

        JSONArray returnList = new JSONArray();

        Integer numLevels = null;

        if(!validateData(data)) {
            callbackContext.error("espSmartconfig: disconnectNetwork invalid data");
            Log.d(TAG, "espSmartconfig: disconnectNetwork invalid data");
            return false;
        }else if (!data.isNull(0)) {
            try {
                JSONObject options = data.getJSONObject(0);

                if (options.has("numLevels")) {
                    Integer levels = options.optInt("numLevels");

                    if (levels > 0) {
                        numLevels = levels;
                    } else if (options.optBoolean("numLevels", false)) {
                        // use previous default for {numLevels: true}
                        numLevels = 5;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error(e.toString());
                return false;
            }
        }

        for (ScanResult scan : scanResults) {
            /*
             * @todo - breaking change, remove this notice when tidying new release and explain changes, e.g.:
             *   0.y.z includes a breaking change to espSmartconfig.getNetworklist().
             *   Earlier versions set scans' level attributes to a number derived from wifiManager.calculateSignalLevel.
             *   This update returns scans' raw RSSI value as the level, per Android spec / APIs.
             *   If your application depends on the previous behaviour, we have added an options object that will modify behaviour:
             *   - if `(n == true || n < 2)`, `*.getNetworklist({numLevels: n})` will return data as before, split in 5 levels;
             *   - if `(n > 1)`, `*.getNetworklist({numLevels: n})` will calculate the signal level, split in n levels;
             *   - if `(n == false)`, `*.getNetworklist({numLevels: n})` will use the raw signal level;
             */

            int level;

            if (numLevels == null) {
              level = scan.level;
            } else {
              level = wifiManager.calculateSignalLevel(scan.level, numLevels);
            }

            JSONObject lvl = new JSONObject();
            try {
                lvl.put("level", level);
                lvl.put("SSID", scan.SSID);
                lvl.put("BSSID", scan.BSSID);
                lvl.put("frequency", scan.frequency);
                lvl.put("capabilities", scan.capabilities);
               // lvl.put("timestamp", scan.timestamp);
                returnList.put(lvl);
            } catch (JSONException e) {
                e.printStackTrace();
                callbackContext.error(e.toString());
                return false;
            }
        }

        callbackContext.success(returnList);
        return true;
    }
	
	//Code cobtained from WiFiWizard by hoerresb
    private boolean validateData(JSONArray data) {
        try {
            if (data == null || data.get(0) == null) {
                receivingCallbackContext.error("Data is null.");
                return false;
            }
            return true;
        }
        catch (Exception e) {
            receivingCallbackContext.error(e.getMessage());
        }
        return false;
    }

    //listener to get result
    private IEsptouchListener myListener = new IEsptouchListener() {
        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            String text = "bssid="+ result.getBssid()+",InetAddress="+result.getInetAddress().getHostAddress();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, text);
            pluginResult.setKeepCallback(true);           // keep callback after this call
            //receivingCallbackContext.sendPluginResult(pluginResult);    //modified by lianghuiyuan
        }
    };
}