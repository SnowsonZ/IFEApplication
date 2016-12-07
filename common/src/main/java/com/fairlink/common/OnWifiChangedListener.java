package com.fairlink.common;

public interface OnWifiChangedListener {
	abstract void onConnected();	
	abstract void onDisConnected();
	abstract void onWifiChange(int level);
}
