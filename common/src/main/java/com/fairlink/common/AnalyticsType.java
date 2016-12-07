package com.fairlink.common;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;


/**
 * @ClassName ： AnalyticsType
 * @Description: 操作日志类型规范
 */
public class AnalyticsType {

	// 记录所在的页面
	public final static String ORIGIN_CART = "cart";
	public final static String ORIGIN_VIDEO = "video";
	public final static String ORIGIN_CHILD_VIDEO = "child_video";
	public final static String ORIGIN_NEWS = "news";
	public final static String ORIGIN_CLUB = "club";
	public final static String ORIGIN_MALL_SSS = "mall_sss";
	public final static String ORIGIN_MUSIC = "music";
	public final static String ORIGIN_KID = "children";
	public final static String ORIGIN_VOICE_CUSTOMER = "voice_customer";
	public final static String ORIGIN_GAME = "game";
	public final static String ORIGIN_EBOOK = "ebook";
	public final static String ORIGIN_MALL_FLC = "mall_flc";
	public final static String ORIGIN_ABOUT_US = "about_us";
	public final static String ORIGIN_DEVICE = "device";
	public final static String ORIGIN_USER = "user";
	public final static String ORIGIN_MAIN_INTERFACE = "main_interface";
    public final static String ORIGIN_STEWARD = "steward";
    public final static String ORIGIN_MAINTENANCE = "maintenance";
    public final static String ORIGIN_PLANE_INFO = "plane_info";
    public final static String ORIGIN_CAR = "car_reservation";
    public final static String ORIGIN_ORDER = "order";
    public final static String ORIGIN_SETTING = "setting";
    
    public final static String ORIGIN_MAININTERFACE = "mainInterface";
    public final static String ORIGIN_MODULE = "module";
    public final static String ORIGIN_CATEGORY = "category";
    public final static String ORIGIN_SUBCATEGORY = "subCategory";
    public final static String ORIGIN_DETAIL = "detail";
    public final static String ORIGIN_RESOURCE = "resource";
    public final static String ORIGIN_CREW_DEVICE = "crew_device";
    
    public final static String RESOURCE_TYPE_GOODS = "goods";
    public final static String RESOURCE_TYPE_OTHER = "resource";
    
    public static String getOperationDynamic(int type) {
        switch (type) {
        case 1:
            return "enter";
        case 2:
            return "out";
        case 3:
            return "click";
        case 4:
            return "recommend";
        case 5:
            return "ad";
        case 6:
            return "imagine";
        case 7:
            return "autoDisplay";
        case 8:
            return "submit";
        case 9:
        	return "recJump";
        case 10:
        	return "submitResult";
        case 11:
            return "adShow";
        case 12:
            return "adClose";
        default:
            return "unkown";
        }
    }

	public static String getStewardOrder(int type)
	{
		switch (type) {
		case 1:
			return "gathering";
		case 2:
			return "cashOrderRefuse";
		case 3:
			return "confirmRefund";
		case 4:
			return "cancelOrder";	
		default:
			return "unkown";
		}
	}
	
	public static String getOperationShop(int type) {

		switch (type) {
		case 1:
			return "enter";
		case 2:
			return "out";
		case 3:
			return "detail_in";
		case 4:
		    return "detail_out";
		case 5:
		    return "category";
		case 6:
		    return "buy_goods";
		case 7:
			return "cart_add";
		case 8:
			return "delete";
		case 9:
		    return "image_detail";
		case 10:
		    return "after_service";
		case 11:
			return "pay";
		default:
			return "unknow";
		}

	}

	public static String getOperationVideoMus(int type) {

		switch (type) {
		case 1:
			return "enter";
		case 2:
			return "out";
		case 3:
			return "detail_in";
		case 4:
			return "play";
		case 5:
		    return "exit";
		case 6:
			return "pause";
		case 7:
		    return "continue";
		case 8:
		    return "category";
        case 11:
            return "video_delay";
        case 12:
            return "detail_out";
        case 13:
            return "complete";
		default:
			return "unknow";
		}

	}

	public static String getOperationEbook(int type,int page) {
		
		switch (type) {
		case 1:
			return "enter";
		case 2:
			return "out";
		case 3:
			return "detail_in";
		case 4:
		    return "detail_out";
		case 5:
			return "jump to page:"+page;
		default:
			return "unknow";
		}
	}

	public static String getOperationAD(int type) {
		switch (type) {
		case 1:
			return "open";
		case 2:
			return "jump";
		case 3:
		    return "ad_video";
		default:
			return "unknow";
		}
	}

	public static String getOperationNews(int type) {
		switch (type) {
		case 1:
			return "enter";
		case 2:
			return "out";
		default:
			return "unknow";
		}
	}

	public static String getOperationGame(int type) {
		switch (type) {
		case 1:
			return "enter";
		case 2:
			return "out";
		case 3:
			return "detail_in";
		case 4:
			return "detail_out";
		case 5:
		    return "game_activity";
		default:
			return "unknow";
		}
	}

	public static String getOperationOrder(int type) {
		switch (type) {
		case 1:
            return "enter";
		case 2:
		    return "out";
		case 3:
			return "create";
		case 4:
			return "cancel";
		case 5:
			return "pay";
		case 6:
		    return "confirm_pay";
		default:
			return "unknow";
		}
	}

    public static String getOperationAboutUs(int type) {

        switch (type) {
        case 1:
            return "enter";
        case 2:
            return "out";
        case 3:
            return "spring_airlines";
        case 4:
            return "fairlink_century";
        default:
            return "unknow";
        }

    }

    public static String getOperationCar(int type) {

        switch (type) {
        case 1:
            return "enter";
        case 2:
            return "out";
        case 3:
            return "submit_order";
        case 4:
            return "cancel";
        case 5:
            return "change_order";
        case 6:
            return "cancel_order";
        default:
            return "unknow";
        }

    }

    public static String getOperationCustomer(int type) {

        switch (type) {
        case 1:
            return "enter";
        case 2:
            return "out";
        case 3:
            return "customer_voice";
        default:
            return "unknow";
        }

    }
    
	public static String getOperationDevice(int type) {

		switch (type) {
		case 1:
			return "battery_change";
		case 2:
			return "wifi_change";
		case 3:
			return "device_space";
		case 4:
		    return "device_boot";
		case 5:
		    return "device_shutdown";
		case 6:
		    return "main_interface";
		case 7:
		    return "login";
		case 8:
		    return "login_result";
		case 9:
		    return "register";
		case 10:
		    return "register_result";
		case 11:
        	return "warning";
		case 12:
		    return "maintenance_event";
		default:
			return "unknow";
		}

	}

	/**
	 * 离线登录
	 * 
	 * @return
	 */
    public static Map<String, Object> cofflinelogin(String account,String usrNo) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("account", account);
		dataMap.put("userno", usrNo);
		return dataMap;
	};
	
    public static JSONObject getSingleData(Object id) {
        JSONObject data = new JSONObject();
        data.put("resId", String.valueOf(id));
        return data;
    };
	
	public static JSONObject getAnalyticsData(Object id) {
        JSONObject data = new JSONObject();
        data.put("resId", String.valueOf(id));
        data.put("uid", 0);
        return data;
    }
	
	public static JSONObject getAnalyticsData(Object id,String resType) {
        JSONObject data = new JSONObject();
        data.put("resId", String.valueOf(id));
        data.put("uid", 0);
        data.put("resType", resType);
        return data;
    }
	
	public static JSONObject getSubmitData(Object id, String infoType, JSONObject info) {
	    JSONObject data = new JSONObject();
        data.put("resId", String.valueOf(id));
        data.put("uid", 0);
        data.put(infoType, info);
        return data;
	}
    
    public static JSONObject getComplexData(Object srcResId, Object destResId, String extraKey, JSONObject extraData) {
        JSONObject data = new JSONObject();
        JSONObject src = new JSONObject();
        src.put("resId", String.valueOf(srcResId));
        src.put("uid", 0);
        data.put("src", src);
        JSONObject dest = new JSONObject();
        dest.put("resId", String.valueOf(destResId));
        data.put("dest", dest);
        data.put(extraKey, extraData);
        return data;
    }
    
    public static JSONObject getComplexData(Object srcResId, Object destResId, String extraKey, JSONObject extraData,String resType) {
        JSONObject data = new JSONObject();
        JSONObject src = new JSONObject();
        src.put("resId", String.valueOf(srcResId));
        src.put("uid", 0);
        src.put("resType", resType);
        data.put("src", src);
        JSONObject dest = new JSONObject();
        dest.put("resId", String.valueOf(destResId));
        dest.put("resType", resType);
        data.put("dest", dest);
        data.put(extraKey, extraData);
        return data;
    }
    
    public static JSONObject getComplexData(Object srcResId, Object destResId, String extraKey, JSONObject extraData,String srcResType,String destResType) {
        JSONObject data = new JSONObject();
        JSONObject src = new JSONObject();
        src.put("resId", String.valueOf(srcResId));
        src.put("uid", 0);
        src.put("resType", srcResType);
        data.put("src", src);
        JSONObject dest = new JSONObject();
        dest.put("resId", String.valueOf(destResId));
        dest.put("resType", destResType);
        data.put("dest", dest);
        data.put(extraKey, extraData);
        return data;
    }
	
}

