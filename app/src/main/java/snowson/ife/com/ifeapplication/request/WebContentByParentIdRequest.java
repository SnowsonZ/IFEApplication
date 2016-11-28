package snowson.ife.com.ifeapplication.request;

import com.fairlink.common.BaseHttpGetTask;

import java.util.HashMap;
import java.util.List;

import snowson.ife.com.ifeapplication.application.IFEApplication;
import snowson.ife.com.ifeapplication.bean.WebContent;
import snowson.ife.com.ifeapplication.utils.HttpUtil;
import snowson.ife.com.ifeapplication.utils.JsonUtil;

public class WebContentByParentIdRequest extends BaseHttpGetTask {

    public WebContentByParentIdRequest(int parentId, HttpTaskCallback callback) {
        super(WEB_CONTENT_BY_PARENT_ID_API, HttpUtil.getWebContentByParentId(parentId), new HashMap<String, String>(), callback);
    }

    @Override
    protected Object parseJSON(String json) {
        if (json == null) {
            return null;
        }
        List<WebContent> ret = JsonUtil.parseJsonArray(json, WebContent.class);
        if (ret != null) {
            for(WebContent t : ret) {
                if(t.getPicture() != null) {
                    t.setPicture(IFEApplication.getInstance().getValue("APACHE_URL") + t.getPicture());
                }
            }
        }
        return ret;
    }
}
