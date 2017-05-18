package snowson.ife.com.ifeapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.fairlink.common.GlobalStorage;

import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import snowson.ife.com.ifeapplication.utils.Constant;

public class MainActivity extends BaseActivity{

    public XWalkView xWebView;
    private MainActivity mContext;
    private int retry_count = 0;
    private boolean isExists = true;
    private Handler handler = null;
    private JsInterface mPageObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        mContext = this;
        mPageObject = new JsInterface();
        handler = new Handler();
        xWebView = (XWalkView) findViewById(R.id.webView);
        xWebView.setResourceClient(new XWalkResourceClient(xWebView) {

            @Override
            public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
                view.load(url, null);
                return true;
            }

            @Override
            public void onReceivedLoadError(final XWalkView view, int errorCode, String description, final String failingUrl) {
                super.onReceivedLoadError(view, errorCode, description, failingUrl);
                if(retry_count < 5) {
                    retry_count++;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.load(failingUrl, null);
                        }
                    }, 2000);
                }else {
                    Toast.makeText(mContext, "网络错误,请退出重试", Toast.LENGTH_SHORT).show();
                    isExists = true;
                }
            }

            @Override
            public void onLoadFinished(XWalkView view, String url) {
                super.onLoadFinished(view, url);
                if(url.endsWith("index")) {
                    xWebView.getNavigationHistory().clear();
                }
                if(GlobalStorage.getInstance().getSessionId() == null && url.endsWith("index")) {
                    XWalkCookieManager cManager = new XWalkCookieManager();
                    String cookies = cManager.getCookie(url);
                    if(TextUtils.isEmpty(cookies)) {
                        return;
                    }
                    for (String item : cookies.split(";")) {
                        if (item.contains("JSESSIONID")) {
                            isExists = false;
                            GlobalStorage.getInstance().setSessionId(item.split("=")[1]);
                        }
                    }
                }
                if (url.endsWith("login")) {
                    GlobalStorage.getInstance().setSessionId(null);
                }
            }

            @Override
            public void onLoadStarted(XWalkView view, String url) {
                super.onLoadStarted(view, url);
            }
        });
        xWebView.addJavascriptInterface(mPageObject, "appContext");
        xWebView.load(Constant.BASE_URL, null);
    }

    public class JsInterface {

        public JsInterface() {
        }

        @JavascriptInterface
        public void openDetailVideo(String params) {
            Intent intent = new Intent(mContext,VideoDetailActivity2.class);
            Bundle bundle = new Bundle();
            bundle.putString("params", params);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
            mContext.overridePendingTransition(0, 0);
        }

        @JavascriptInterface
        public void openOtherPage(String url, String cookieUrl) {
            Intent intent = new Intent(mContext, OtherActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            bundle.putString("cookieUrl", cookieUrl);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(isExists) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (xWebView.getUrl().endsWith("index")) {
                return true;
            }else {
                super.onKeyDown(keyCode, event);
                return true;
            }
        }

        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (xWebView != null) {
            xWebView.pauseTimers();
            xWebView.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (xWebView != null) {
            xWebView.resumeTimers();
            xWebView.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHomeKeyEventReceiver != null) {
            mContext.unregisterReceiver(mHomeKeyEventReceiver);
        }
        if (xWebView != null) {
            xWebView.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (xWebView != null) {
            xWebView.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (xWebView != null) {
            xWebView.onNewIntent(intent);
        }
    }

    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    MainActivity.clearAllActivity();
                    if(xWebView == null) {
                        xWebView = (XWalkView) findViewById(R.id.webView);
                    }else {
                        if(!xWebView.getUrl().endsWith("index")) {
                            xWebView.getNavigationHistory().clear();
                            xWebView.load(Constant.BASE_URL, null);
                        }
                    }
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
                    //表示长按home键,显示最近使用的程序列表
                }
            }
        }
    };
}
