package snowson.ife.com.ifeapplication.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.fairlink.common.Logger;

import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import snowson.ife.com.ifeapplication.application.IFEApplication;

/**
 * @ClassName ： StringUtil
 * @Description: 公共工具类
 */

public class ComUtil {

    private static Logger logger = new Logger(null, "ComUtil");
    private static String sCookie;

    public static String getNoToString(int num) {

        if (num <= 0)
            return "0";
        if (num > 0 && num < 10) {
            return "0" + num;
        }

        return "" + num;
    }

    private static long lastClickTime;

    /** 3s内防止短时间内按钮多次点击 */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;

        if (0 < timeD && timeD < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    private static final String ENCODING = "UTF-8";

    /** assets获取管理 */
    public static String getFromAssets(Context context, String fileName) {

        String result = "";
        if (context == null || fileName == null || fileName.length() == 0) {
            return result;
        }

        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            int lenght = in.available();
            byte[] buffer = new byte[lenght];
            in.read(buffer);
            result = EncodingUtils.getString(buffer, ENCODING);
        } catch (Exception e) {
            logger.error("can't read file [" + fileName + "] from asset folder with error:" + e.getMessage());
        }
        return result;
    }

    /** 判断string是否为空 */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input) || "null".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };

    /** 获取当前时间字符串 */
    public static String DateToString(Date date) {
        return dateFormater.get().format(date);
    }

    /** 获取图片列表 */
    public static List<String> getPicList(String pics) {
        if (isEmpty(pics))
            return null;

        String[] temp = pics.split(",");
        List<String> picList = new ArrayList<String>();
        for (int i = 0; i < temp.length; i++) {
            picList.add(temp[i]);
        }
        return picList;
    }

    /** 从图片列表中获取第一张图片 */
    public static String getPic(String pics) {

        if (isEmpty(pics))
            return null;

        String[] temp = pics.split(",");

        return temp[0];
    }

    /** 从图片列表中获取图片总量 */
    public static int getPicNum(String pics) {
        if (isEmpty(pics))
            return 0;

        String[] temp = pics.split(",");
        return temp.length;
    }

    /** 判断手机格式是否正确 */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^1[3|4|5|6|7|8|][0-9]{9}$");

        Matcher m = p.matcher(mobiles);

        return m.matches();
    }

    /** 判断email格式是否正确 */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    /** 毫秒数转化成时分秒格式 */
    public static String Time(int sec) {

        sec /= 1000;
        StringBuilder text = new StringBuilder();
        int hour = sec / 3600;
        int minute = sec / 60 - hour * 60;
        int secs = sec % 60;
        if (hour > 0) {
            if (hour < 10) {
                text.append("0" + hour).append(":");
            } else {
                text.append(hour).append(":");
            }
        }
        if (hour == 0)
            text.append("00:");
        if (minute == 0)
            text.append("00:");

        if (minute > 0 || hour > 0) {
            if (minute < 10) {
                text.append("0" + minute).append(":");
            } else {
                text.append(minute).append(":");
            }
        }
        if (secs < 10) {
            text.append("0" + secs);
        } else {
            text.append(secs);
        }

        return text.toString();

    }

    /** 获取音乐类型 */
    public static String getMusType(int type) {
        switch (type) {
        case 1:
            return "中国风";
        case 2:
            return "流行地带";
        case 3:
            return "轻音乐谷";
        case 4:
            return "爵士乡村";
        case 5:
            return "古典情怀";
        case 6:
            return "岁月留声";
        case 7:
            return "特别推荐";
        default:
            return "unkown";
        }
    }

    /** 获取视频类型 */
    public static String getVideoType(int type) {
        switch (type) {
        case 1:
            return "动画喜剧";
        case 2:
            return "爱情家庭";
        case 3:
            return "剧情悬疑";
        case 4:
            return "动作冒险";
        default:
            return "unkown";
        }
    }

    public static boolean cleanFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            return false;
        }

        if (!folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            }
            if (file.isDirectory()) {
                cleanFolder(file.getPath());
            }
        }

        return true;
    }   
    private static Toast mToast;
    public static void toastText(String text, int duration) {
    	if (mToast==null) {
    		mToast=Toast.makeText(IFEApplication.getInstance().getApplicationContext(), text, duration);
		}else{
			mToast.setText(text);
			mToast.setDuration(duration);
		}
        mToast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
    	mToast.show();
    }
    
    public static void toastTextForList(String text, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(IFEApplication.getInstance().getApplicationContext(), text,
                            duration);
        } else {
            mToast.setText(text);
            mToast.setDuration(duration);
        }
        mToast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 120, 0);
        mToast.show();
    }
    
    public static void hideToast() {
        if(mToast != null) {
            mToast.cancel();
        }
    }
    
    /** 判断是否为证件 后6位*/
    public static boolean isValidCradNo(String input) {
        if (input == null || input.length() != 6)
            return false;

        Matcher isCradNo = Pattern.compile("[0-9A-Za-z]{6}").matcher(input);
        return isCradNo.matches();
    }
    
    public static void redirect() {
//    	GlobalStorage.getInstance().setSessionId(null);
//        Context context = GlobalStorage.getInstance().getBaseContext();
////        Intent intent = new Intent(context, LoginMain.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("redirect", true);
//        context.startActivity(intent);
    }

    public static void setCookie(String cookie) {
        sCookie = cookie;
    }

    public static String getCookie() {
        if(sCookie != null) {
            return sCookie;
        }
        return null;
    }

}
