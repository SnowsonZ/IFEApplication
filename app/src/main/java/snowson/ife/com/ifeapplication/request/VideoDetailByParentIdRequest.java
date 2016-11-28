package snowson.ife.com.ifeapplication.request;

import com.fairlink.common.BaseHttpGetTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;

import snowson.ife.com.ifeapplication.request.MovieDetailRequest.MovieDetail;
import snowson.ife.com.ifeapplication.request.MovieDetailRequest.MoviePlayerItem;
import snowson.ife.com.ifeapplication.utils.HttpUtil;

public class VideoDetailByParentIdRequest extends BaseHttpGetTask {
	
	public VideoDetailByParentIdRequest(int id, HttpTaskCallback callback) {
		super(VIDEO_DETAIL_BY_PARENT_ID_API, HttpUtil.getVideoDetailByParentId(id), new HashMap<String, String>(), callback);
		mParam.put("tags", "mobile");
	}

	@Override
	protected Object parseJSON(String json) {
		if(json == null) {
			return null;
		}

		JSONTokener parser = new JSONTokener(json);
		MovieDetail tmp = new MovieDetail();
		try {
			JSONObject menu = (JSONObject) parser.nextValue();

			int code = menu.getInt("code");

			if (code != 0) {
				return null;
			}

			JSONObject data = menu.getJSONObject("data");

			tmp.id = data.getString("videoId");
			tmp.name = data.getString("videoName");
			tmp.director = data.getString("videoDirector");
			tmp.actor = data.getString("videoActors");
			tmp.content = data.getString("videoDesc");
			tmp.image = data.getString("videoPoster");
			tmp.isMovice = data.getString("videoIsMovie");
			tmp.area = data.getString("videoArea");
			tmp.canRating = data.getBoolean("canRating");
			tmp.curUserRatingMsg = data.getString("curUserRatingMsg");
	        tmp.positiveCount = data.getInt("positiveCount");
	        tmp.negativeCount = data.getInt("negativeCount");
			tmp.items = new ArrayList<MoviePlayerItem>();
			JSONArray items = data.getJSONArray("itemList");
			int len = items.length();
			MoviePlayerItem item;
			JSONObject index;
			for (int i = 0; i < len; i++) {
				item = new MoviePlayerItem();
				index = (JSONObject) items.get(i);
				item.no = index.getString("videoItemNo");
				item.location = index.getString("videoItemLocation");
				tmp.items.add(item);
			}

			return tmp;
		} catch (JSONException e) {
			logger.error("MovieDetailRequest parse failed with error:" + e.getMessage());
			return null;
		}
	}

}
