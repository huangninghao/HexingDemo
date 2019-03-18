package com.hexing.libhexbase.cache;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Base64;

import com.hexing.libhexbase.application.HexApplication;
import com.hexing.libhexbase.tools.PackageUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * 缓存管理(基于SharedPreferences封装)
 *
 * @author long
 *         2015年8月3日 上午10:55:50
 */
public class HexCache {

    private static SharedPreferences sharedPreferences;

    protected static SharedPreferences cache() {
        if (sharedPreferences == null) {
            sharedPreferences = HexApplication.getInstance().getContext().getSharedPreferences(PackageUtils.getPackage(),
                    HexApplication.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    /**
     * writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
     * 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
     *
     * @param object 待加密的转换为String的对象
     * @return String   加密后的String
     */
    private static String Object2String(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            String string = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            objectOutputStream.close();
            return string;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用Base64解密String，返回Object对象
     *
     * @param objectString 待解密的String
     * @return object      解密后的object
     */
    private static Object String2Object(String objectString) {
        byte[] mobileBytes = Base64.decode(objectString.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 使用SharedPreference保存对象
     *
     * @param key        储存对象的key
     * @param saveObject 储存的对象
     */
    public static void saveObject(String key, Object saveObject) {
        String string = Object2String(saveObject);
        cache().edit().putString(key, string).apply();
    }

    /**
     * 获取SharedPreference保存的对象
     *
     * @param key 储存对象的key
     * @return object 返回根据key得到的对象
     */
    public static Object getObject(String key, String defaultVal) {
        String string = cache().getString(key, defaultVal);
        if (string != null) {
            return String2Object(string);
        } else {
            return null;
        }
    }

}
