package snowson.ife.com.ifeapplication.request;

import com.fairlink.common.BaseHttpGetTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;

import snowson.ife.com.ifeapplication.bean.VideoListInfo;
import snowson.ife.com.ifeapplication.utils.HttpUtil;

public class VideoListDetailByParentIdRequest extends BaseHttpGetTask {

	public VideoListDetailByParentIdRequest(int id, HttpTaskCallback callback) {
		super(VIDEO_LIST, HttpUtil.getVideoList(), new HashMap<String, String>(), callback);
		mParam.put("videoId", String.valueOf(id));
		mParam.put("length", String.valueOf(10));
		mParam.put("tags", "mobile");
	}

	@Override
	protected Object parseJSON(String json) {
		if(json == null) {
			return null;
		}

		JSONTokener parser = new JSONTokener(json);
		ArrayList<VideoListInfo> listInfos = new ArrayList<VideoListInfo>();
		try {
			JSONObject menu = (JSONObject) parser.nextValue();

			int code = menu.getInt("code");

			if (code != 0) {
				return null;
			}

			JSONArray datas = menu.getJSONArray("data");
			for (int i = 0; i < datas.length(); i++) {
				VideoListInfo info = new VideoListInfo();
				info.videoPoster = datas.getJSONObject(i).getString("videoPoster");
				info.videoId = datas.getJSONObject(i).getString("videoId");
				info.videoName = datas.getJSONObject(i).getString("videoName");
				listInfos.add(info);
			}

			return listInfos;
		} catch (JSONException e) {
			logger.error("MovieDetailRequest parse failed with error:" + e.getMessage());
			return null;
		}
	}

}
