package snowson.ife.com.ifeapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fairlink.common.Analytics;
import com.fairlink.common.AnalyticsType;
import com.fairlink.common.BaseHttpTask.HttpTaskCallback;
import com.fairlink.common.GlobalStorage;
import com.fairlink.common.NetworkRequestAPI;

import java.lang.ref.WeakReference;
import java.util.List;

import snowson.ife.com.ifeapplication.application.IFEApplication;
import snowson.ife.com.ifeapplication.bean.WebContent;
import snowson.ife.com.ifeapplication.request.WebContentByParentIdRequest;
import snowson.ife.com.ifeapplication.utils.ComUtil;
import snowson.ife.com.ifeapplication.view.DialogLoading;
import snowson.ife.com.ifeapplication.view.DialogLoading.LoadingListener;

public class WebFragment extends BaseFragment implements NetworkRequestAPI, LoadingListener {

    private class WebContentCallback implements HttpTaskCallback {
        private WeakReference<WebFragment> container;

        public WebContentCallback(WebFragment container) {
            this.container = new WeakReference<WebFragment>(container);
        }

        @Override
        public void onGetResult(int requestType, Object result) {
            WebFragment c = container.get();
            if (c == null) {
                return;
            }
            if (c.loadingDialog != null) {
                c.loadingDialog.hide();
            }

            if (result == null) {
                return;
            }

            List<WebContent> list = (List<WebContent>) result;
            if (list.isEmpty()) {
                return;
            }

            WebContent content = list.get(0);
            if (content == null) {
                return;
            }

            c.textView.setText(content.getName());
            int id = content.getId();
            Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(), AnalyticsType
                            .getOperationDynamic(9), AnalyticsType.ORIGIN_MAININTERFACE,
                            AnalyticsType.getComplexData(parentId, id, null, null,
                                            AnalyticsType.RESOURCE_TYPE_OTHER));
            Analytics.logEvent(IFEApplication.getInstance().getApplicationContext(), AnalyticsType
                            .getOperationDynamic(1), AnalyticsType.ORIGIN_DETAIL, AnalyticsType
                            .getAnalyticsData(id, AnalyticsType.RESOURCE_TYPE_OTHER));
            c.targetUrl = WEB_PATH + id + "/index.html";
            c.webLoad();

        }

        @Override
        public void onError(int requestType) {
            WebFragment c = container.get();
            if (c == null) {
                return;
            }
            if (c.loadingDialog != null) {
                c.loadingDialog.hide();
            }
            if (requestType != REDIRECT_API) {
                ComUtil.toastText("连接服务器出错", Toast.LENGTH_SHORT);
            }
        }
    }

    private WebView webView;
    private LinearLayout linLoad;
    private TextView textView;
    private ImageButton closeBtn;
    private volatile boolean loadedFlag = false;
    private String targetUrl;
    private static final String BLANK_URL = "about:blank";
    private boolean enableTouch = true;
    private int parentId = -1;
    private int resourceId = -1;
    private static final String WEB_PATH = IFEApplication.getInstance().getValue("APACHE_URL") + "/thirdparty_web/";
    private WebContentByParentIdRequest request;
    private DialogLoading loadingDialog;

    public void setParentId(int id) {
        parentId = id;
        sendRequest(getActivity());
    }
    
    public void setResourceId(int id, String title) {
        resourceId = id;
        targetUrl = WEB_PATH + resourceId + "/index.html";
        if(title != null && textView != null) {
            textView.setText(title);
        }
        webLoad();
    }
    
    public boolean goBack() {
        if (webView != null) {
            if (!webView.canGoBack()) {
                return false;
            }
            webView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.web_content, container);
        linLoad = (LinearLayout) view.findViewById(R.id.lin_load);
        webView = (WebView) view.findViewById(R.id.web);
        textView = (TextView) view.findViewById(R.id.title_view);
        closeBtn = (ImageButton) view.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }

        });
        initWebView();
        return view;
    }

    private void initWebView() {
        webView.setHorizontalScrollBarEnabled(true);
        webView.setVerticalScrollBarEnabled(true);
        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(false);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true); 
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    private void sendRequest(Context ct) {
        dismissDialog();
        if (parentId != -1) {
            loadingDialog = new DialogLoading(ct, this);
            loadingDialog.show();
            request = new WebContentByParentIdRequest(parentId, new WebContentCallback(this));
            request.execute((String) null);
        }
    }

    @Override
    public void onDestroy() {
        webView.stopLoading();

        super.onDestroy();
        cancelRequest();
        dismissDialog();
    }

    private void webLoad() {

        webView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (enableTouch) {
                    return webView.onTouchEvent(event);
                }
                if (event.getAction() == (MotionEvent.EDGE_LEFT | MotionEvent.EDGE_RIGHT | MotionEvent.EDGE_TOP | MotionEvent.EDGE_BOTTOM)) {
                    return false;
                } else {
                    return true;
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("mailto:") || url.startsWith("geo:") || url.startsWith("tel:"))) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                logger.error("Cannot load the map: error code = " + errorCode + " description = " + description
                        + " failing Url= " + failingUrl);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadedFlag = false;
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadedFlag = true;
                linLoad.setVisibility(View.GONE);
            }

        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if(title != null && textView != null) {
                    textView.setText(title);
                }
            }
        });

        CookieSyncManager.createInstance(getActivity());    
        CookieManager cookieManager = CookieManager.getInstance();    
        cookieManager.setCookie("http://wifi.ch.com", "JSESSIONID=" + GlobalStorage.getInstance().getSessionId());   
        cookieManager.setCookie("http://wifi.ch.com", "APPUSED=true");
        CookieSyncManager.getInstance().sync(); 
        webView.loadUrl(targetUrl);
    }

    @Override
    public void onCancel() {
        cancelRequest();
    }

    private void cancelRequest() {
        if (loadingDialog != null) {
            loadingDialog.hide();
        }
        if (request != null) {
            request.cancel();
        }
    }

    private void dismissDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
