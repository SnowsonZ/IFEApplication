package snowson.ife.com.ifeapplication.application;

import android.app.Application;
import android.os.PowerManager.WakeLock;

import com.fairlink.common.Analytics;
import com.fairlink.common.AnalyticsType;
import com.fairlink.common.GlobalStorage;
import com.fairlink.common.GlobalStorage.OnRedirectEvent;
import com.fairlink.common.PhotoManager;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import snowson.ife.com.ifeapplication.utils.ComUtil;

/**
 * @ClassName ： IFEApplication
 * @Description: 全局应用程序类：用于保存和调用全局应用配置
 */

public class IFEApplication extends Application implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;

    private static IFEApplication instance;

    private static final String REDIRECT_KEY_WORD = "redirect";

    public synchronized static IFEApplication getInstance() {

        return instance;
    }
    
    private String passengerSeatNo;// 座位号
    private HashMap<String, Long> videoPosition = new HashMap<String, Long>();
    private int ordersCount;
    public String saytext;
    private boolean isCarReservationShown = false;
    private OnRedirectEvent onRedirect;
    private String versionName;
    private boolean isVisitor = true;

    public String getPassengerSeatNo() {
		return passengerSeatNo;
	}

	public void setPassengerSeatNo(String passengerSeatNo) {
		this.passengerSeatNo = passengerSeatNo;
	}

	public boolean isVisitor() {
        return isVisitor;
    }

    public void setVisitor(boolean isVisitor) {
        this.isVisitor = isVisitor;
    }

    WakeLock wakeLock;

    private static long lastClickTime = System.currentTimeMillis();

    public void setLastClickTime() {
        long now = System.currentTimeMillis();

        if (now - lastClickTime > 5 * 60 * 1000) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("duration", (now - lastClickTime) / 1000);
            Analytics.logEvent(instance, "no_operation", AnalyticsType.ORIGIN_USER, dataMap);
        }

        lastClickTime = now;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        instance = this;

        GlobalStorage.getInstance().setBaseContext(IFEApplication.getInstance());
        GlobalStorage.getInstance().initProperty();

        onRedirect = new GlobalStorage.OnRedirectEvent() {
            public void onRedirect() {
            	ComUtil.redirect();
            }

        };
        GlobalStorage.getInstance().setOnRedirectEvent(onRedirect);

        PhotoManager.getInstance().init();
    }

    public String getValue(String key) {
        return GlobalStorage.getInstance().prop.getProperty(key);

    }


    // 电影进度
    public void setVideoPosition(String url, long positin) {
        videoPosition.put(url, positin);
    }

    public long getVideoPosition(String url) {
        long position = 0;
        if (videoPosition.containsKey(url)) {
            position = videoPosition.get(url);
        }
        return position;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        defaultUEH.uncaughtException(thread, ex);
    }


}
