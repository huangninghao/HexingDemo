package com.hexing.libhexbase.tools;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by long
 * on 2018/2/26.
 */

public class MD5Utils {
    /**
     * 1.对文本进行32位小写MD5加密
     *
     * @param plainText 要进行加密的文本
     * @return 加密后的内容
     */
    public static String toMD5L32(String plainText) {
        String result = null;
        //首先判断是否为空
        if (TextUtils.isEmpty(plainText)) {
            return null;
        }
        try {
            //首先进行实例化和初始化
            MessageDigest md = MessageDigest.getInstance("MD5");
            //得到一个操作系统默认的字节编码格式的字节数组
            byte[] btInput = plainText.getBytes();
            //对得到的字节数组进行处理
            md.update(btInput);
            //进行哈希计算并返回结果
            byte[] btResult = md.digest();
            //进行哈希计算后得到的数据的长度
            StringBuffer sb = new StringBuffer();
            for (byte b : btResult) {
                int bt = b & 0xff;
                if (bt < 16) {
                    sb.append(0);
                }
                sb.append(Integer.toHexString(bt));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 2.对文本进行32位MD5大写加密
     *
     * @param plainText 要进行加密的文本
     * @return 加密后的内容
     */
    public static String toMD5U32(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return null;
        }
        String result = toMD5L32(plainText);
        return result != null ? result.toUpperCase() : "";
    }

    /**
     * 3.对文本进行16位MD5小写加密
     *
     * @param plainText 需要进行加密的文本
     */
    public static String toMD5L16(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return null;
        }
        String result = toMD5L32(plainText);
        if (result == null) return null;
        return result.substring(8, 24);
    }

    /**
     * 4.对文本进行16位MD5大写加密
     *
     * @param plainText 需要进行加密的文本
     * @return
     */
    public static String toMD5U16(String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return null;
        }
        String result = toMD5L32(plainText);
        return result != null ? result.toUpperCase().substring(8, 24) : "";
    }

}
