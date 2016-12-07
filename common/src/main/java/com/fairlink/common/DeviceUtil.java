package com.fairlink.common;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * @ClassName ： DeviceUtil
 * @Description: 公共工具类
 * @author ： John
 * @date ： 2015-02-03 下午11:53:00
 */

public class DeviceUtil {

	public static String getUID(Context ctx) {

		String imei = ((TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if (imei == null) {
			return Secure
					.getString(ctx.getContentResolver(), Secure.ANDROID_ID);

		}
		return imei;
	}
	
	public static String getSerialNumber() {
		String serial = "";
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.lenovosn2");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serial;
	}
	
	public static String getModel(){
	    Build build = new Build();
	    String model = build.MODEL;
	    return model;
	}

}
