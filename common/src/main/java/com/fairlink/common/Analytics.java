package com.fairlink.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.fairlink.common.BaseHttpTask.HttpTaskCallback;

public class Analytics {
    static final int ANALYTICS_COUNT=99;
    private static Logger logger = new Logger(null, "Analytics");
    static List<JSONObject> logEventList = new ArrayList<JSONObject>();
   
    static {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (Analytics.class) {
                    if (logEventList != null && logEventList.size() > 0) {
                        sendLogEvent(logEventList);
                        logEventList = null;
                        logEventList = new ArrayList<JSONObject>();
                    }
                }
            }
        }, 60 * 1000, 60 * 1000);

    };
      
       
    static public void sendLogEvent(List<JSONObject> logEventList) {
        HashMap<String, Object> parma = new HashMap<String, Object>();

        parma.put("analyticsEvent", logEventList);
        String jsonString = parma.toString();
        HttpTaskCallback callback = new HttpTaskCallback() {

            @Override
            public void onGetResult(int requestType, Object result) {
            }

            @Override
            public void onError(int requestType) {
                logger.error("Analytics result:" + "服务器出错");
            }
        };

        logger.debug("send json : " + jsonString);
        new AnalyticsPostTask(callback, jsonString).execute((String) null);
    }

    static public void logEvent(Context ctx, String type, String origin, JSONObject data) {
        JSONObject object = new JSONObject();

        object.put("type", type);
        object.put("origin", origin);
        object.put("platform", "android" + android.os.Build.VERSION.RELEASE + " " + android.os.Build.MANUFACTURER + " "
                + android.os.Build.MODEL);
        object.put("deviceId", DeviceUtil.getUID(ctx));
        object.put("serial_number", DeviceUtil.getSerialNumber());
        if (data != null) {
            object.put("data", data.toString().replace("\\", "\\\\").replace("\"", "\\\""));
        }
        object.put("ts", (new Date().getTime()+GlobalStorage.time_variation));
        synchronized (Analytics.class) {
            logEventList.add(object);

            if (logEventList.size() > ANALYTICS_COUNT) {
                sendLogEvent(logEventList);
                logEventList = null;
                logEventList = new ArrayList<JSONObject>();
            } 
        }
    }

    static public void logEvent(Context ctx, String type, String origin, Map<String, Object> data) {
        logEvent(ctx, type, origin, new JSONObject(data));

    }
}
