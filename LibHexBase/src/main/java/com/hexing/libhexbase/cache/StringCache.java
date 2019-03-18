package com.hexing.libhexbase.cache;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hexing.libhexbase.tools.GJsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字符串类型缓存
 *
 * @author long
 *         2015年8月3日 上午11:26:55
 */
public class StringCache extends HexCache {

    /**
     * 插入缓存
     */
    public static void put(String key, String value) {
        cache().edit().putString(key, value).apply();
    }

    /**
     * 获取缓存
     */
    public static String get(@NonNull String key, String defValue) {
        return cache().getString(key, defValue);
    }

    /**
     * 获取缓存
     */
    public static String get(@NonNull String key) {
        return get(key, "");
    }

    /**
     * 判断缓存是否存在
     *
     * @return true is null
     */
    public static boolean isValue(@NonNull String key) {
        return TextUtils.isEmpty(get(key));

    }

    /**
     * 删除缓存
     *
     * @return true is success
     */
    public static boolean remove(@NonNull String key) {
        if (!isValue(key)) {
            cache().edit().remove(key).apply();
            return true;
        }
        return false;
    }

    public static void putArrayString(@NonNull String key, List<String> list) {
        put(key, GJsonUtil.toJson(list));
    }

    public static List<String> getArrayString(@NonNull String key) {
        return GJsonUtil.fromJsonList(StringCache.get(key), String.class);
    }

    public static void refreshArrayString(@NonNull String key, String value) {
        List<String> list = getArrayString(key);
        boolean isNew = true;
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(value);
            putArrayString(key, list);
            return;
        }
        for (String s : list) {
            if (!TextUtils.isEmpty(value) && value.equals(s)) {
                return;
            }
        }
        list.add(value);
        putArrayString(key, list);
    }

    /**
     * 获取对象
     *
     * @param key key
     * @return T
     */
    public static Object getObject(@NonNull String key) {
        return getObject(key, null);
    }

    public static void putObject(@NonNull String key, @NonNull Object ob) {
        saveObject(key, ob);
    }

    /**
     * 加密处理
     * 保存对象 * * @param context 上下文 * @param key 键 * @param obj 要保存的对象（Serializable的子类） * @param <T> 泛型定义
     */
    public static <T extends Serializable> void putJavaBean(@NonNull String key, @NonNull T obj) {
        try {
            saveObject(key, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 有解密操作
     *
     * @param key key
     * @param <T> T
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getJavaBean(@NonNull String key) {
        try {
            Object object = getObject(key);
            return (T) object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存List * @param key
     * * @param dataList
     */
    public static <T> void putJavaBeanList(@NonNull String key, List<T> dataList) {
        if (null == dataList || dataList.size() <= 0)
            return;
        //转换成json数据，再保存
        String strJson = GJsonUtil.toJsonString(dataList);
        saveObject(key, strJson);
    }

    /**
     * @param key key
     * @param cls bean
     * @param <T> bean
     * @return list
     */
    public static <T> List<T> getJavaBeanList(@NonNull String key, Class<T> cls) {
        List<T> dataList = new ArrayList<>();
        Object cache = getObject(key);
        if (cache == null || cache.toString().length() == 0) {
            return dataList;
        }
        dataList = GJsonUtil.fromJsonList(cache.toString(), cls);
        return dataList;
    }

    public static void putMapCache(@NonNull String key, @NonNull String mapKey, @NonNull Object object) {
        Map<String, Object> map = new HashMap<>();
        map.put(mapKey, object);
        put(key, GJsonUtil.toJson(map));
    }

    public static void putMapCache(@NonNull String key, @NonNull String mapKey, @NonNull String object) {
        Map<String, String> map = new HashMap<>();
        map.put(mapKey, object);
        put(key, GJsonUtil.toJson(map));
    }
}
