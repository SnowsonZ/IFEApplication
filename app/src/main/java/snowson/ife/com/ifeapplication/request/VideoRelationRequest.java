package snowson.ife.com.ifeapplication.request;

import com.fairlink.common.BaseHttpGetTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;

import snowson.ife.com.ifeapplication.bean.VideoListInfo;
import snowson.ife.com.ifeapplication.utils.HttpUtil;

public class VideoRelationRequest extends BaseHttpGetTask {

	public VideoRelationRequest(long albumId, long id, HttpTaskCallback callback) {
		super(VIDEO_LIST, HttpUtil.getVideoList(), new HashMap<String, String>(), callback);
		mParam.put("albumId", String.valueOf(albumId));
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

			JSONArray datas = (JSONArray) parser.nextValue();
			for (int i = 0; i < datas.length(); i++) {
				VideoListInfo info = new VideoListInfo();
				info.setVideoPoster(datas.getJSONObject(i).getString("videoPoster"));
				info.setVideoId(datas.getJSONObject(i).getString("videoId"));
				info.setVideoName(datas.getJSONObject(i).getString("videoName"));
				listInfos.add(info);
			}

			return listInfos;
		} catch (JSONException e) {
			logger.error("VideoDetailByVideoIdRequest parse failed with error:" + e.getMessage());
			return null;
		}
	}

}
