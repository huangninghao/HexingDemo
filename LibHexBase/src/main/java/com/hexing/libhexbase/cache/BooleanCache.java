package com.hexing.libhexbase.cache;

/**
 * 布尔类型缓存
 */
public class BooleanCache extends HexCache {

    /**
     * 写入缓存
     *
     * @param key   key
     * @param value value
     */
    public static void put(String key, boolean value) {
        cache().edit().putBoolean(key, value).apply();
    }

    /**
     * 获取缓存
     */
    public static boolean get(String key, boolean defValue) {
        return cache().getBoolean(key, defValue);
    }
}
