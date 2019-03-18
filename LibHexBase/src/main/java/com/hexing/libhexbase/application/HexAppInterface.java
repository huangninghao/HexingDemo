package com.hexing.libhexbase.application;

import android.app.Service;
import android.content.Context;

import java.util.List;

/**
 * Created by caibinglong
 * on 16-2-29.
 * app 初始化接口
 */
public interface HexAppInterface {

    void initThirdPlugin(Context context);

    void initDir(List<String> dirs);

    void initService(Context context, Class<? extends Service> service, String action);

    void initDB(Context context, String DBName, int DBVersion);

    void destroy();

}
