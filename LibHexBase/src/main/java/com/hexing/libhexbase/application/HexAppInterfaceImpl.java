package com.hexing.libhexbase.application;

import android.app.Service;
import android.content.Context;
import android.content.Intent;


import com.hexing.libhexbase.thread.HexThreadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * app 初始化 接口实现
 */
public class HexAppInterfaceImpl implements HexAppInterface {

    @Override
    public void initThirdPlugin(final Context context) {
        HexThreadManager.fixedThreadPoolRun(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void initDir(List<String> dirs) {
        for (String dir : dirs) {
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
        }
    }

    @Override
    public void destroy() {
    }

    public void initService(Context context, Class<? extends Service> service, String action) {
        Intent serviceIntent = new Intent(context, service);
        serviceIntent.setAction(action);
        context.startService(serviceIntent);
    }

    @Override
    public void initDB(Context context, String DBName, int DBVersion) {
    }

}
