package com.hexing.libhexbase.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hexing.libhexbase.application.HexApplication;

/**
 * @author caibinglong
 *         date 2018/3/28.
 *         desc desc
 */

public class PackageUtils {
    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * [获取应用程序build称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return String.valueOf(packageInfo.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 项目包名
     */
    public static String getPackage() {
        return HexApplication.getInstance().getPackageName();
    }
}
