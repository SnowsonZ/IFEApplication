package snowson.ife.com.ifeapplication.utils;

import com.alibaba.fastjson.JSON;
import com.fairlink.common.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

public class JsonUtil {

	private static Logger logger = new Logger(null, "JsonUtil");

	public static <T> List<T> parseJsonArray(String json, Class<T> clazz) {
		try {
			return (List<T>) JSON.parseArray(getDataField(json, clazz).getJSONArray("data").toString(), clazz);
		} catch (JSONException e) {
			logger.error("parse json array with error " + e.getMessage() + ". when parse class [" + clazz.getName()
					+ "], json string [" + json + "]");
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T parseJsonObject(String json, Class<T> clazz) {
		try {
			return JSON.parseObject(getDataField(json, clazz).getJSONObject("data").toString(), clazz);
		} catch (JSONException e) {
			logger.error("parse json objcet with error " + e.getMessage() + ". when parse class [" + clazz.getName()
					+ "], json string [" + json + "]");
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private static <T> JSONObject getDataField(String json, Class<T> clazz) throws Exception {
		if (json == null || json.isEmpty()) {
			logger.error("json string is null");
			throw new Exception();
		}

		JSONTokener parser = new JSONTokener(json);
		JSONObject menu = (JSONObject) parser.nextValue();
		int code = menu.getInt("code");
		if (code != 0) {
			logger.error("receive error code " + code + " when parse class [" + clazz.getName() + "], json string ["
					+ json + "]");
			throw new Exception();
		}

		return menu;
	}

	public static int getCode(String json) {
		try {
			JSONTokener parser = new JSONTokener(json);
			JSONObject menu = (JSONObject) parser.nextValue();
			return menu.getInt("code");
		} catch (JSONException e) {
			logger.error("can't get code from json string " + json);
			return 1;
		}
	}
}
