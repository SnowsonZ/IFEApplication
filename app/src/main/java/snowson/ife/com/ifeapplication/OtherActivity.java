package snowson.ife.com.ifeapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.fairlink.common.GlobalStorage;

public class OtherActivity extends BaseActivity {

    private WebView webview;
    private String loadUrl;
    private OtherActivity mContext;
    private String cookieUrl;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_other);
        Bundle bundle = getIntent().getExtras();
        loadUrl = bundle.getString("url");
        cookieUrl = bundle.getString("cookieUrl");
        webview = (WebView) findViewById(R.id.webviewother);

        WebSettings settings = webview.getSettings();
        settings.setSupportZoom(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.addJavascriptInterface(new BackMainPageInterface(), "appContext");
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(OtherActivity.this, "页面出错,请退出重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url, null);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webLoad();
    }

    @Override
        public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void webLoad() {

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(cookieUrl, "JSESSIONID=" + GlobalStorage.getInstance().getSessionId());
        CookieSyncManager.getInstance().sync();
        webview.loadUrl(loadUrl);
    }

    public class BackMainPageInterface {

        @JavascriptInterface
        public void backToMain() {
            mContext.finish();
    }

    }

}
