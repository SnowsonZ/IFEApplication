package com.fairlink.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

public class WifiChangeBroadcastReceiver extends BroadcastReceiver {

    private Logger logger = new Logger(this, "wifi");

    static enum WifiCipherType {
        NONE, IEEE8021XEAP, WEP, WPA, WPA2, WPAWPA2;
    }

    private static String SSID = "Fairlink_9527";
    private static String PASSWORD = "8046FLC0146";
    private static final WifiCipherType WIFI_CIPHER_TYPE = WifiCipherType.WPA2;

    private Context mContext;
	private String origin="";//AnalyticsType.ORIGIN_DEVICE;

    static final int MAX_ACCUMULATOR = 60;
    private int accumulator;

    private static WifiChangeBroadcastReceiver instance;
    private OnWifiChangedListener mListner;
    private OnWifiScanListener mScanListener;
    private boolean isConnected = false;
    private String ssid = "";
    private int signalLevel = 0;
    private int speed = 0;
    private boolean warningEnabled = true;

    private static final int WIFI_CONNECTED = 0x1;
    private static final int WIFI_DISCONNECTED = 0x2;
    private static final int WIFI_CHANGED = 0x3;
    
    public interface OnWifiScanListener{
        boolean onExists();
        boolean onNotExists();
    }
    
    public void setOnWifiScanListener(OnWifiScanListener listener){
        mScanListener = listener;
    }

    private final Handler wifiStatusHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case WIFI_CONNECTED: {
                mListner.onConnected();
                break;
            }

            case WIFI_DISCONNECTED: {
                mListner.onDisConnected();
                break;
            }
            
            case WIFI_CHANGED:{
                mListner.onWifiChange(msg.arg1);
            }
            }
        }
    };
    
    static final int HIDE_SHUTDOWN_HINT = 0;
    static final int SHOW_SHUTDOWN_HINT = 1;
    
    public static WifiChangeBroadcastReceiver getInstance(Context context,String org) {
        if (instance == null) {
            instance = new WifiChangeBroadcastReceiver(context,org);
        }
        return instance;
    }

    WifiChangeBroadcastReceiver(Context context,String org) {
    	this.mContext = context;
        this.origin=org;
    }
    
    public void setWifiConnector(String ssid, String password) {
      if (ssid != null && !ssid.isEmpty()) {
          SSID = ssid;
      }
      if (password != null && !password.isEmpty()) {
          PASSWORD = password;
      }
    }

    public void start() {
        addNewWifi(createWifiConfiguration(SSID, PASSWORD, WIFI_CIPHER_TYPE));
        mContext.registerReceiver(this, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        new Timer().schedule(new TimerTask() {
            public void run() {
                getWifiInfo();
            }
        }, 0, 1 * 1000);

    }

    public void stop() {
        mContext.unregisterReceiver(this);
        mContext.unregisterReceiver(wifiScanReceiver);
    }

    private void logWifiChange() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getBSSID() != null) {
            // wifi名称
            String ssid = wifiInfo.getSSID();
            // wifi信号强度
            int signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
            // wifi速度
            int speed = wifiInfo.getLinkSpeed();
            // wifi速度单位
            String units = WifiInfo.LINK_SPEED_UNITS;
            
            if (mListner != null && isConnected) {
                wifiStatusHandler.sendMessage(wifiStatusHandler.obtainMessage(WIFI_CHANGED, signalLevel, 0));
//            	mListner.onWifiChange(signalLevel);
            }

            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("signalLevel", signalLevel);
            dataMap.put("speed", speed);
            dataMap.put("units", units);
            Analytics.logEvent(mContext, AnalyticsType.getOperationDevice(2), origin, dataMap);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        getWifiInfo();
        logWifiChange();
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case HIDE_SHUTDOWN_HINT:
                if (ShutDownHint.instance != null) {
                    ShutDownHint.instance.finish();
                    Intent controlHomeButton = new Intent();
                    controlHomeButton.setAction("customer.control.homeButton");
                    controlHomeButton.putExtra("lock", false);
                    mContext.sendBroadcast(controlHomeButton);
                }
                break;
            case SHOW_SHUTDOWN_HINT:
                Intent intent = new Intent(mContext, com.fairlink.common.ShutDownHint.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
            }
            super.handleMessage(msg);
        }

    };

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            boolean wifiExist = false;
            
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanList = wifiManager.getScanResults();
            for (ScanResult scanResult : scanList) {
                if (SSID.equals(scanResult.SSID)) {
                    wifiExist = true;
                    break;
                }
            }

            Message msg = new Message();
            if (wifiExist) {
                if (mScanListener != null) {
                    mScanListener.onExists();
                }
                accumulator = 0;
                msg.what = HIDE_SHUTDOWN_HINT;
            } else {
                if (mScanListener != null) {
                    mScanListener.onNotExists();
                }
                msg.what = SHOW_SHUTDOWN_HINT;

            }
            if(warningEnabled){
                handler.sendMessage(msg);
                
            }
            
            if(!isConnected){
                wifiManager.startScan();
            }
        
        }
    };

    private void getWifiInfo() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getBSSID() != null) {
            // wifi名称
            String ssid = wifiInfo.getSSID();
            // wifi信号强度
            int signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
            // wifi速度
            int speed = wifiInfo.getLinkSpeed();

            if (!this.ssid.equals(ssid) || this.signalLevel != signalLevel || this.speed != speed) {
                logger.debug("signalLevel=" + signalLevel + ",speed=" + speed + ",units=" + WifiInfo.LINK_SPEED_UNITS);
                this.ssid = ssid;
                this.signalLevel = signalLevel;
                this.speed = speed;
            }

            if (ssid.equals("\"" + SSID + "\"") && (signalLevel > 0)) {
                accumulator = 0;
                if (mListner != null && isConnected == false) {
                    wifiStatusHandler.sendMessage(wifiStatusHandler.obtainMessage(WIFI_CONNECTED));
//                    mListner.onConnected();
                }
                Message msg = new Message();
                msg.what = HIDE_SHUTDOWN_HINT;
                handler.sendMessage(msg);
                isConnected = true;
            } else {

                ++accumulator;
                if (accumulator > MAX_ACCUMULATOR) {
                    if (mListner != null) {
                        wifiStatusHandler.sendMessage(wifiStatusHandler.obtainMessage(WIFI_DISCONNECTED));
//                        mListner.onDisConnected();
                    }
                } else {
                    addNewWifi(createWifiConfiguration(SSID, PASSWORD, WIFI_CIPHER_TYPE));
                }

                if(isConnected){
                    wifiManager.startScan();
                }
                isConnected = false;
            }
        }
    }

    private WifiConfiguration createWifiConfiguration(String ssid, String password, WifiCipherType type) {
        WifiConfiguration newWifiConfiguration = new WifiConfiguration();
        newWifiConfiguration.allowedAuthAlgorithms.clear();
        newWifiConfiguration.allowedGroupCiphers.clear();
        newWifiConfiguration.allowedKeyManagement.clear();
        newWifiConfiguration.allowedPairwiseCiphers.clear();
        newWifiConfiguration.allowedProtocols.clear();
        newWifiConfiguration.SSID = "\"" + ssid + "\"";
        switch (type) {
        case NONE:
            newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            break;
        case IEEE8021XEAP:
            break;
        case WEP:
            break;
        case WPA:
            newWifiConfiguration.preSharedKey = "\"" + password + "\"";
            newWifiConfiguration.hiddenSSID = true;
            newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            newWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            newWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            newWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            break;
        case WPA2:
            newWifiConfiguration.preSharedKey = "\"" + password + "\"";
            newWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            newWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            newWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            newWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            newWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            newWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            newWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            newWifiConfiguration.hiddenSSID = true;
            break;
        default:
            return null;
        }
        return newWifiConfiguration;
    }

    boolean addNewWifi(WifiConfiguration newConfig) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        int netID = wifiManager.addNetwork(newConfig);
        boolean bRet = wifiManager.enableNetwork(netID, true);
        return bRet;
    }

    public void setOnWifiChangedLisner(OnWifiChangedListener listner) {
        mListner = listner;
    }
    
    public boolean isConnected() {
    	return isConnected;
    }

    public void setWarningEnabled(boolean enable){
        this.warningEnabled = enable;
    }
}
