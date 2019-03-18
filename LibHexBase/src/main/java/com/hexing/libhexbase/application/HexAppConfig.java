package com.hexing.libhexbase.application;

import android.app.Application;

/**
 * Author: long
 * Date: 2017-09-8 10:06
 * FIXME:
 * DESC: 应用配置常量
 */
public class HexAppConfig {

    //系统初始化生成目录名字常量
    public static final String FILEPATH_ROOT_NAME = "HexBase";
    public static final String FILEPATH_CACHE_NAME = "cache";
    public static final String FILEPATH_BASE_CONFIG_NAME = "baseConfig";
    public static final String FILEPATH_UPAPK_NAME = "upapk";
    public static final String FILEPATH_CAMERA_NAME = "camera";
    public static final String FILEPATH_RECORD_NAME = "record";

    public void add(Application application) {
        AppManager.add(application);
    }
}
