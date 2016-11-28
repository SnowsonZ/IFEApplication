package snowson.ife.com.ifeapplication.request;

import com.alibaba.fastjson.JSON;
import com.fairlink.common.BaseHttpGetTask;

import java.util.HashMap;

import snowson.ife.com.ifeapplication.bean.Ads;
import snowson.ife.com.ifeapplication.utils.HttpUtil;

/**
 * @ClassName ： AdsRequest
 */

public class AdsRequest extends BaseHttpGetTask {

    public final static int ADS_TYPE_INDEX = 1;
    public final static int ADS_TYPE_VIDEO_START = 2;
    public final static int ADS_TYPE_VIDEO_PAUSE = 3;
    public final static int ADS_TYPE_BOOTUP = 4;

    public AdsRequest(HttpTaskCallback callback, int adsType) {
        super(ADS_API, HttpUtil.getAds(), new HashMap<String, String>(), callback);
        mParam.put("type", "" + adsType);
        mParam.put("tags", "mobile");
    }

    @Override
    protected Object parseJSON(String json) {
        if (json == null) {
            return null;
        }
        Ads ad = null;
        try {
            ad = JSON.parseObject(json, Ads.class);
        } catch (Exception e) {
            //just ignore this exception.
        }
        return ad;
    }
}
