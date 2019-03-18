package com.hexing.libhexbase.tools;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author caibinglong
 *         date 2018/3/26.
 *         desc AES 加密
 */

public class AESEncrypt {

    private static String mSecretKey = "HEXINGGROUP12345";
    public final static String PKCS5PADDING = "PKCS5Padding";
    public final static String PKCS7PADDING = "PKCS7Padding";
    public static String defaultPKCS = PKCS5PADDING;

    public static void setSecretKey(String secretKey) {
        mSecretKey = secretKey;
    }

    public static void setDefaultPKCS(String PKCS) {
        defaultPKCS = PKCS;
    }

    public static String encrypt(String source) {
        if (mSecretKey == null) {
            return null;
        }
        return encrypt(encryptKey(source));
    }

    public static String encrypt(byte[] arr) {
        return encrypt(arr, Base64.NO_WRAP);
    }

    private static String encrypt(byte[] arr, int wrapAt) {
        try {
            return new String(Base64.encode(arr, wrapAt), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    private static byte[] encryptKey(String source) {
        byte[] arr = null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(mSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/" + defaultPKCS + "", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            arr = cipher.doFinal(source.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static String decrypt(String encryptStr) {
        byte[] arr = Base64.decode(encryptStr.getBytes(), Base64.NO_WRAP);
        return new String(decryptKey(arr));
    }

    private static byte[] decryptKey(byte[] source) {
        byte[] arr = null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(mSecretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/" + defaultPKCS + "", "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            arr = cipher.doFinal(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
}