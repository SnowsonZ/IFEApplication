package snowson.ife.com.ifeapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.fairlink.common.Logger;

import java.util.ArrayList;
import java.util.List;

import snowson.ife.com.ifeapplication.application.IFEApplication;

public class BaseActivity extends Activity {
	private static List<Activity> activityList = new ArrayList<Activity>();
	protected Logger logger = new Logger(this, "activity");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityList.add(this);
		logger.info("onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		logger.debug("onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		logger.debug("onPause");
	}

	@Override
	protected void onDestroy() {
		activityList.remove(this);
		logger.info("onDestroy");
		super.onDestroy();
	}
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        IFEApplication.getInstance().setLastClickTime();
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        IFEApplication.getInstance().setLastClickTime();
        return super.dispatchKeyEvent(event);
    }

	public static void clearAllActivity() {
		for (Activity activity : activityList) {
			if (activity instanceof MainActivity == false) {
				activity.finish();
			}
		}
	}
	
	public void onBackMainListener(View v) {
		clearAllActivity();
	}
}
