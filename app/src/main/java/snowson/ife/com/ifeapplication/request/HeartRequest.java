package snowson.ife.com.ifeapplication.request;


import com.fairlink.common.BaseHttpGetTask;

import snowson.ife.com.ifeapplication.utils.HttpUtil;

public class HeartRequest extends BaseHttpGetTask{

	public HeartRequest(HttpTaskCallback callback) {
		super(HEART_API, HttpUtil.getHeart(), null, callback);
	}

	@Override
	protected Object parseJSON(String json) {
		return null;
	}

}
