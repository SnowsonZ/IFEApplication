package com.fairlink.common;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
public class GlobalStorage{
	private static final String ANALYTICS_LOG_EVENT = "analytics/logEvent";
	private final static String installCMD = "application/vnd.android.package-archive";
	private static final String LOG_EVENT = "logResource/logEvent";
	
	public static long time_variation=0;//与服务器校正后增量
	public static long time_variation_comp=-1;//是否趋近判断值
	
	public static final int INSTALL_SUCCEEDED                              = 1;
	
	public String userName; 
	
	private boolean offlineStatus = false;

	private static GlobalStorage instance;

	private String JSESSIONID = null;
	
	private Logger logger = new Logger(this, "");
	
	Context context;
	
	private OnRedirectEvent onRedirect;
	
	public Properties prop;
	
	public String getValue(String key) {
		return prop.getProperty(key);
	}
	
	public OnRedirectEvent getOnRedirectEvent(){
		return onRedirect;
	}
	public void setOnRedirectEvent(OnRedirectEvent onRedirect){
		this.onRedirect=onRedirect;
	}
	
	
	public static int installSilent(Context context, String filePath) {
        Intent silent_install = new Intent(Intent.ACTION_VIEW);
        silent_install.setDataAndType(Uri.parse("file://" + filePath), installCMD);
        context.startActivity(silent_install);
        
        return INSTALL_SUCCEEDED;
    }
	
	public static String getAppVersionCode(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(context.getPackageName(), 0);
                    if (pi != null) {
                        return pi.versionName;
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

	
	public interface OnRedirectEvent{
		
		public void onRedirect();
	}
	
	
	public synchronized static GlobalStorage getInstance() {
		if(instance == null){
			instance = new GlobalStorage();
		}

		return instance;
	}

	public synchronized void setSessionId(String id) {
		if (JSESSIONID == null) {
			logger.info("set session id to " + id);
		} else if (!JSESSIONID.equals(id)) {
			logger.warn("session id changed from " + JSESSIONID + " to " + id);
		} else {
			return;
		}

		JSESSIONID = id;
	}
	

	public synchronized String getSessionId() {
		return JSESSIONID;
	}
	public void setOfflineStatus(boolean offline) {
		offlineStatus = offline;
	}
	public Context getBaseContext() {
        return context;
    }
	
	public void setBaseContext(Context context) {//This function is used to call in IFEApplycation to set up context
		this.context=context;
    }
	
	public static String getAnalyticsLogEvent() {
		return getAbsoluteUrl(ANALYTICS_LOG_EVENT);
	}
	
	private static String getAbsoluteUrl(String relativeUrl) {
		return instance.getValue("BASE_URL") + relativeUrl;
	}
	
	public static String getLogEvent(){
		return getAbsoluteUrl(LOG_EVENT);
	}
	public void initProperty(){
	    if (prop != null) {
            return;
        }
		InputStream in = null;
		try {
			prop = new Properties();
			File f = new File(Environment.getExternalStorageDirectory().getPath() + "/config.properties");
			if (f.exists()) {
				in = new FileInputStream(f);
			} else {
				in = context.getResources().getAssets().open("config.properties");

			}
			prop.load(in);
		} catch (IOException e) {
			logger.error("can't find config.properties " + e.getMessage());
			throw new RuntimeException("A error happens when reading config.properties", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
		
	}
	
	public void initProperties(){
	    if (prop == null) {
            prop = new Properties();
            prop.setProperty("BASE_IP", "http://wifi.ch.com");
            prop.setProperty("BASE_URL", "http://ife.fairlink.com/ife_services/");
            prop.setProperty("BASE_SECURITY_URL", "https://ife.fairlink.com/ife_services/");
            prop.setProperty("SSID", "CH_DEVTest1.COM");
            prop.setProperty("PASSWORD", "fairlink");
            prop.setProperty("wifi_detection", "true");
            prop.setProperty("SpringMall", "SpringMall");
            prop.setProperty("MAP_URL", "http://wifi.ch.com/map/");
            prop.setProperty("APACHE_URL", "http://ife.fairlink.com:8080/data/source");
        }
	}
	public boolean isOffline() {
		return offlineStatus;
	}
	
	
	
	
}