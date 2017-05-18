package net.kimhou.playersdk.exoplayerwrapper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.android.exoplayer.ExoPlayerLibraryInfo;

import java.util.Locale;

/**
 * Created by wxtry on 15/5/10.
 */
public class Util {
    public static String getUserAgent(Context context) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        return "ExoPlayerDemo/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE +
                ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
    }

    public static  String getContentId(String uri) {
        return uri.toLowerCase(Locale.US).replaceAll("\\s", "");
    }
}
