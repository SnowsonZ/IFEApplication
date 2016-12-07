package com.fairlink.common;

import android.content.Context;
import android.os.Handler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public abstract class BaseHttpTask implements NetworkRequestAPI {

	private static final String REDIRECT_KEY_WORD = "redirect";
	protected Map<String, String> mParam;
	protected String mUrl;
	protected Logger logger;

	private HttpTaskCallback mCallback;
	private HttpTaskCallbackNew newCallback;
	private int mRequestType;
	private static final Handler sHandler = new Handler();
    private static class HttpResponseData{
        public String text;
        public int statusCode;
        public HttpResponseData(int statusCode, String text){
            this.text = text;
            this.statusCode = statusCode;
        }
    }
    
	public static interface HttpTaskCallback {
		public void onGetResult(int requestType, Object result);

		public void onError(int requestType);
	}
	
	public static interface HttpTaskCallbackNew{
	    public void onGetResult(int requestType, Object result);
	    public void onError(int requestType, int statusCode);
	}
	
	public void cancel() {
	    mCallback = null;
	}

	public BaseHttpTask(int requestType, String url, HttpTaskCallback callback) {
		mRequestType = requestType;
		mUrl = url;
		mCallback = callback;
	}
	
	public BaseHttpTask(int requestType, String url, HttpTaskCallbackNew callback) {
        mRequestType = requestType;
        mUrl = url;
        newCallback = callback;
    }

	protected abstract HttpRequestBase getRequest();

	protected HttpResponseData doInBackground(String... params) {
		DefaultHttpClient httpclient = new DefaultHttpClient();

		HttpRequestBase httpRequest = getRequest();
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);

		String sessionId = GlobalStorage.getInstance().getSessionId();
		if (sessionId != null) {
			httpRequest.setHeader("Cookie", "JSESSIONID=" + sessionId);
		}

		try {
			final long sendTime=new Date().getTime();
			HttpResponse response = httpclient.execute(httpRequest);
			Header[] headers=response.getHeaders("Date");
			if(headers.length>0&&!headers[0].getValue().equals("")){
				try {
					long recvTime=new Date().getTime();
					long cmp=recvTime-sendTime;
					if(GlobalStorage.time_variation_comp==-1||GlobalStorage.time_variation_comp>cmp){
						GlobalStorage.time_variation_comp=cmp;
						
						DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);//Tue, 03 Mar 2009 04:58:40 GMT
						httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
						Date date=httpDateFormat.parse(headers[0].getValue());
						long re=date.getTime()-(sendTime+recvTime)/2;
						GlobalStorage.time_variation=re;
					}else{
						DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);//Tue, 03 Mar 2009 04:58:40 GMT
						httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
						Date date=httpDateFormat.parse(headers[0].getValue());
						long re=date.getTime()-(sendTime+recvTime)/2;
						
						long val=GlobalStorage.time_variation-re;
						if(val<0){
							val=0-val;
						}
						if(val>300000){//5 min
							GlobalStorage.time_variation_comp=cmp;
							GlobalStorage.time_variation=re;
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.debug("send http request:" + httpRequest.getURI() + "[" + mParam + "]");
			int statusCode = response.getStatusLine().getStatusCode();
			String getString = "";
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				getString = EntityUtils.toString(entity, "utf-8");
				logger.debug("receive http repond:" + getString);
				CookieStore mCookieStore = httpclient.getCookieStore();
				List<Cookie> cookies = mCookieStore.getCookies();
				for (int i = 0; i < cookies.size(); i++) {
                    // 这里是读取Cookie['SESSIONID']的值存在静态变量中，保证每次都是同一个值
					if ("JSESSIONID".equals(cookies.get(i).getName())) {
						GlobalStorage.getInstance().setSessionId(cookies.get(i).getValue());
						break;
					}
				}

			} else {
				logger.error("get error status:" + statusCode + "when request url:" + mUrl);
			}
			return new HttpResponseData(statusCode, getString);
		} catch (UnsupportedEncodingException e) {
			logger.error("receive UnsupportedEncodingException [" + e.getMessage() + "] when request url:" + this.mUrl);
			return null;
		} catch (ClientProtocolException e) {
			logger.error("receive ClientProtocolException [" + e.getMessage() + "] when request url:" + mUrl);
			return null;
		} catch (IOException e) {
			logger.error("receive IOException [" + e.getMessage() + "] when request url:" + mUrl);
			return null;
		}
	}

	protected abstract Object parseJSON(String json);

	public final void execute(final String... params) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final HttpResponseData result = doInBackground(params);

				sHandler.post(new Runnable() {

					@Override
					public void run() {
						onPostExecute(result);
					}
				});
			}

		}, "HttpTask").start();
	}

	protected void onPostExecute(HttpResponseData result) {
		if (result == null || (result.statusCode != 200 && result.text.isEmpty())) {
			logger.error("send request failed");
			if(mCallback != null){
			    mCallback.onError(mRequestType);
			}else if(newCallback != null){
			    newCallback.onError(mRequestType, result != null ? result.statusCode : -1);
			}
		} else {
			if (isRedirect(result.text))
				return;
			if(mCallback != null){
			    mCallback.onGetResult(mRequestType, parseJSON(result.text));
			}else if(newCallback != null){
			    newCallback.onGetResult(mRequestType, parseJSON(result.text));
			}
		}
	}	

	private boolean isRedirect(String result) {
		try {
			JSONTokener parser = new JSONTokener(result);
			JSONObject menu = (JSONObject) parser.nextValue();
			int code = menu.optInt("code", -1);
			if (code != 0) {
				return false;
			}

			JSONObject item = menu.optJSONObject("data");
			if (item == null)
				return false;

			if (item.optBoolean(REDIRECT_KEY_WORD, false)) {
				logger.error("receive redirect, offline is " + item.getBoolean("offline"));
                if(mCallback != null){
                    mCallback.onError(REDIRECT_API);
                }else if(newCallback != null){
                    newCallback.onError(REDIRECT_API, 200);
                }
				backToLogin(item.getBoolean("offline"));
				return true;
			}
		} catch (JSONException e) {
			return false;
		} catch (Exception e){
		    return false;
		}

		return false;
	}

	private void backToLogin(boolean isOffline) throws JSONException {
		Context context = GlobalStorage.getInstance().getBaseContext();
		if (context != null) {
			GlobalStorage.getInstance().setSessionId(null);
			
			GlobalStorage.getInstance().setOfflineStatus(isOffline);
			GlobalStorage.getInstance().getOnRedirectEvent().onRedirect();
		}
	}
}
