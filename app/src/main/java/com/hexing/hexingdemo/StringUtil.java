package com.hexing.hexingdemo;

/**
 * @author caibinglong
 *         date 2018/3/5.
 *         desc desc
 */

public class StringUtil {
    /**
     * 发送给固件数据
     * @param bytes 数组
     * @return String
     */
    public static String bytesToHexString2(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0XFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 校验和
     *
     * @param data 原字符串
     * @return 返回校验和
     */
    public static String checkSum(String data) {
        int i = 0;
        int res = 0;
        while (i < data.length() / 2) {
            res += Integer.parseInt(data.substring(i * 2, 2 + i * 2), 16);
            i++;
        }
        if (res > 0xFF) {
            res -= 0x100;
        }
        String str = Integer.toHexString(res);
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str.substring(str.length() - 2, str.length()).toUpperCase();
    }
}
