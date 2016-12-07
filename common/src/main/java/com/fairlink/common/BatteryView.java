package com.fairlink.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BatteryView extends ImageView {
	private int mPower = 100;
	int battery_left = 0;
	int battery_top = 0;
	int battery_width = 27;
	int battery_height = 18;

	int battery_head_width = 3;
	int battery_head_height = 5;

	int battery_inside_margin = 3;
	
	String origin;
	
	public BatteryView(Context context) {
		super(context);
	}

	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.battery_view);

        origin = typedArray.getString(R.styleable.battery_view_origin);

        typedArray.recycle();

	
	}
	public void setPower(int power) {
		mPower = power;
		if (mPower < 0) {
			mPower = 0;
		}
		invalidate();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getContext().registerReceiver(batteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getContext().unregisterReceiver(batteryChangedReceiver);
	}
	
	private BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 100);
				int power = level * 100 / scale;
				setPower(power);
			}
			
			if (mPower <= 10) {
				setBackgroundResource(R.drawable.tenpercent);
				return;
			} else if (mPower <= 25) {
				setBackgroundResource(R.drawable.twentyfivepercent);
				return;
			} else if (mPower <= 50) {
				setBackgroundResource(R.drawable.fiftypercent);
				return;
			} else if (mPower <= 75) {
				setBackgroundResource(R.drawable.seventyfivepercent);
				return;
			} else if (mPower <= 100) {
				setBackgroundResource(R.drawable.hundredpercent);
				return;
			}}
	};
}
