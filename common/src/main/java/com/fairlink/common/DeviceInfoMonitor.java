package com.fairlink.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.StatFs;

import com.fairlink.common.Analytics;
import com.fairlink.common.AnalyticsType;

public class DeviceInfoMonitor {
	private Context mContext ;
	private String origin="";//AnalyticsType.ORIGIN_DEVICE;
	private int lastLevel = 0;
	private int lastScale = 0;

	static private DeviceInfoMonitor instance ;

	class BatteryReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateBatteryInfo(intent);
		}

	}
	
	public static DeviceInfoMonitor getInstance(Context cxt,String org) {
		if(instance == null){
			instance = new DeviceInfoMonitor(cxt,org);
			
		}
		return instance;
	}

	DeviceInfoMonitor(Context cxt,String org) {
		this.mContext = cxt;
		this.origin=org;
		
	}

	public void start(){
	    IntentFilter batteryChangedReceiverFilter = new IntentFilter();  
	    batteryChangedReceiverFilter.addAction(Intent.ACTION_BATTERY_CHANGED);  
	       
	    // 向系统注册batteryChangedReceiver接收器，本接收器的实现见代码字段处  
	    mContext.registerReceiver(new BatteryReceiver(), batteryChangedReceiverFilter);

		//记录pad剩余空间
		long avairableSize = getAvailableInternalSpaceSize();
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("device_space", avairableSize);

		Analytics.logEvent(mContext, AnalyticsType.getOperationDevice(3),origin,dataMap);
	}
	
	void updateBatteryInfo(Intent intent) {
		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
			// 获取当前电量
			int level = intent.getIntExtra("level", 0);
			// 电量的总刻度
			int scale = intent.getIntExtra("scale", 100);

			if (lastLevel == level && lastScale == scale)
				return;

			lastLevel = level;
			lastScale = scale;

			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("battery", (level * 100) / scale);

			// 把它转成百分比
			Analytics.logEvent(mContext, AnalyticsType.getOperationDevice(1), origin, dataMap);
		}
   }
	
	private long getAvailableInternalSpaceSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlock = stat.getAvailableBlocks();
		return availableBlock * blockSize;
	}
}
