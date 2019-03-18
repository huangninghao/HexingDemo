package com.hexing.libhexbase.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by long
 * on 2017/12/22.
 */

public abstract class HexApplication extends Application {
    private static List<Activity> activityList = new ArrayList<>();
    private static String packageName;
    private static HexApplication instance = null;
    private HexAppInterface mAppInterface = new HexAppInterfaceImpl();
    public static String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String FILEPATH_ROOT = SDCARD_ROOT + File.separator + HexAppConfig.FILEPATH_ROOT_NAME;
    public static final String FILEPATH_CACHE = FILEPATH_ROOT + File.separator + HexAppConfig.FILEPATH_CACHE_NAME;
    public static final String FILEPATH_BASE_CONFIG = FILEPATH_ROOT + File.separator + HexAppConfig.FILEPATH_BASE_CONFIG_NAME;
    public static final String FILEPATH_UPDATE_APK = FILEPATH_ROOT + File.separator + HexAppConfig.FILEPATH_UPAPK_NAME;
    public static final String FILEPATH_CAMERA = FILEPATH_ROOT + File.separator + HexAppConfig.FILEPATH_CAMERA_NAME;
    public static final String FILEPATH_RECORD = FILEPATH_ROOT + File.separator + HexAppConfig.FILEPATH_RECORD_NAME;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppManager.onCreate();
        instance = this;
        packageName = getPackageName();

//        List<String> dirs = new ArrayList<>();
//        {
//            dirs.add(FILEPATH_CACHE);
//            dirs.add(FILEPATH_BASE_CONFIG);
//            dirs.add(FILEPATH_UPDATE_APK);
//            dirs.add(FILEPATH_CAMERA);
//            dirs.add(FILEPATH_RECORD);
//        }
        instance = this;
        //mAppInterface.initDir(dirs);
        mAppInterface.initThirdPlugin(this);
    }

    /**
     * 创建文件夹
     *
     * @param dirs 文件夹
     */
    public void createDir(List<String> dirs) {
        mAppInterface.initDir(dirs);
    }

    public synchronized void register(Activity activity) {
        activityList.add(activity);
    }

    /**
     * Activity被销毁时，从Activities中移除
     */
    public synchronized void unregister(Activity activity) {
        if (activityList != null && activityList.size() != 0) {
            activityList.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }


    //注销是销毁所有的Activity
    public synchronized void loginOut() {
        for (Activity activity : activityList) {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    // 单例模式获取唯一的MyApplication实例
    public static HexApplication getInstance() {
        return instance;
    }

    /**
     * 全局上下文对象
     */
    public Context getContext() {
        return instance.getApplicationContext();
    }

}
