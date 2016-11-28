package snowson.ife.com.ifeapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.xwalk.core.JavascriptInterface;

import snowson.ife.com.ifeapplication.utils.Constant;

public class OtherActivity extends BaseActivity {

    private WebView webview;
    private String loadUrl;
    private OtherActivity mContext;
    private String moduleName;
    private String flightServiceModulePath = "";

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_other);
        Bundle bundle = getIntent().getExtras();
        loadUrl = bundle.getString("url");
        moduleName = bundle.getString("moudleName");
        if(moduleName.equals(Constant.FLIGHT_SERVICE_MODULE_NAME) && TextUtils.isEmpty(flightServiceModulePath)) {
            flightServiceModulePath = loadUrl;
        }
        webview = (WebView) findViewById(R.id.webviewother);

        WebSettings settings = webview.getSettings();
        settings.setSupportZoom(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.addJavascriptInterface(new OtherPageInterface(), "otherPage");
        webLoad();
    }

    private void webLoad() {

        webview.loadUrl(loadUrl);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(OtherActivity.this, "页面出错,请退出重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(flightServiceModulePath.equals(loadUrl) || moduleName.equals(Constant.GAME_NAME)) {
                    webview.loadUrl(loadUrl);
                }else {
                    Intent intent = new Intent();
                    intent.putExtra("url", loadUrl);
                    mContext.sendBroadcast(intent);
                    finish();
                }
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

    }

    public class OtherPageInterface {

        @JavascriptInterface
        public void finishPage() {
            mContext.finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.sendBroadcast(new Intent(Constant.STATE_FLAG));
    }

}
