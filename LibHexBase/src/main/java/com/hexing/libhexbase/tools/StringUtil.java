package com.hexing.libhexbase.tools;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author by HEC271
 *         on 2018/1/26.
 */

public class StringUtil {

    /**
     * 字符串是否为空
     *
     * @param str 字符串
     * @return bool
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 转换 ascii 码
     * 16进制 转换ascii字符串
     *
     * @param data 原字符串
     * @return 新字符串
     */
    public static String convertHexToString(String data) {
        StringBuilder stringBuilder = new StringBuilder();
        if (TextUtils.isEmpty(data)) {
            return stringBuilder.toString();
        }
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < data.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = data.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            stringBuilder.append((char) decimal);
        }
        return stringBuilder.toString();
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    /**
     * 转换16进制数据
     *
     * @param str ascii 字符串
     * @return 16进制字符
     */
    public String hexToString(String str) {
        char[] chars = str.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    public static byte[] hexToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] aChar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(aChar[pos]) << 4 | toByte(aChar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * String左对齐
     */
    public static String padLeft(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }
        char[] chars = new char[len];
        System.arraycopy(src.toCharArray(), 0, chars, 0, src.length());
        for (int i = src.length(); i < len; i++) {
            chars[i] = ch;
        }
        return new String(chars).toUpperCase();
    }

    /**
     * String右对齐
     */
    public static String padRight(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }
        char[] chars = new char[len];
        System.arraycopy(src.toCharArray(), 0, chars, diff, src.length());
        for (int i = 0; i < diff; i++) {
            chars[i] = ch;
        }
        return new String(chars).toUpperCase();
    }

    /**
     * 是否数字开头
     *
     * @param str 字符串
     * @return bool
     */
    public static boolean isNumeric(String str) {
        if (str != null && !"".equals(str.trim())) {
            return str.matches("^[0-9]*$");
        }
        return false;
    }

    /**
     * 转换 ascii
     *
     * @param str 字符串
     * @return ascii
     */
    public static String parseAscii(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        for (int i = 0; i < bs.length; i++) {
            sb.append(toHex(bs[i]));
        }
        return sb.toString();
    }

    /**
     * 转换 ascii
     *
     * @param bytes byte[]
     * @return ascii
     */
    public static String parseAscii(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(toHex(bytes[i]));
        }
        return sb.toString();
    }

    private static String toHexUtil(int n) {
        String rt = "";
        switch (n) {
            case 10:
                rt += "A";
                break;
            case 11:
                rt += "B";
                break;
            case 12:
                rt += "C";
                break;
            case 13:
                rt += "D";
                break;
            case 14:
                rt += "E";
                break;
            case 15:
                rt += "F";
                break;
            default:
                rt += n;
        }
        return rt;
    }

    public static String toHex(int n) {
        StringBuilder sb = new StringBuilder();
        if (n / 16 == 0) {
            return toHexUtil(n);
        } else {
            String t = toHex(n / 16);
            int nn = n % 16;
            sb.append(t).append(toHexUtil(nn));
        }
        return sb.toString();
    }

    /**
     * 反序
     *
     * @param bytes byte[]
     * @return byte[]
     */
    public static byte[] reverse(byte[] bytes) {
        int k = 0;
        byte[] newByte = new byte[bytes.length];
        for (int j = bytes.length - 1; j >= 0; j--) {
            newByte[k] = bytes[j];
            k++;
        }
        return newByte;
    }


    /**
     * 该方法主要使用正则表达式来判断字符串中是否包含字母
     * @author fenggaopan 2015年7月21日 上午9:49:40
     * @param cardNum 待检验的原始卡号
     * @return 返回是否包含
     */
    public static boolean judgeContainsStr(String cardNum) {
        String regex=".*[a-zA-Z]+.*";
        Matcher m= Pattern.compile(regex).matcher(cardNum);
        return m.matches();
    }


    /**
     * byte[] add
     *
     * @param source   原数组
     * @param addBytes 新加
     * @return 最终
     */
    public static byte[] addBytes(byte[] source, byte[] addBytes) {
        byte[] result = new byte[source.length + addBytes.length];
        byte[] temp = source;
        System.arraycopy(temp, 0, result, 0, temp.length);
        temp = addBytes;
        System.arraycopy(temp, 0, result, source.length, temp.length);
        return result;
    }

    /**
     * byte[] add
     *
     * @param source  原数组
     * @param addByte 新加
     * @return 最终
     */
    public static byte[] addBytes(byte[] source, byte addByte) {
        byte[] result = new byte[source.length + 1];
        byte[] temp = Arrays.copyOf(source, source.length);
        System.arraycopy(temp, 0, result, 0, source.length);
        result[result.length - 1] = addByte;
        return result;
    }

    /**
     * byte[] remove
     *
     * @param source 数据源
     * @param sPos   开始位置
     * @param ePos   结束位置
     * @return byte[]
     */
    public static byte[] removeBytes(byte[] source, int sPos, int ePos) {
        if (ePos >= source.length || ePos < sPos) {
            return source;
        }
        int pos = 0;
        byte[] result = new byte[source.length - (ePos - sPos + 1)];
        for (int m = 0; m < source.length; m++) {
            if (m < sPos || m > ePos) {
                result[pos] = source[m];
                pos++;
            }
        }
        return result;
    }

    /**
     * byte[] 获取
     *
     * @param source 数据源
     * @param pos    起始位置
     * @param len    取多少个字节
     * @return 新的byte[]
     */
    public static byte[] getBytes(byte[] source, int pos, int len) {
        if (pos >= source.length || len >= source.length) {
            return source;
        }
        byte[] result = new byte[len];
        System.arraycopy(source, pos, result, 0, result.length);
        return result;
    }
}
