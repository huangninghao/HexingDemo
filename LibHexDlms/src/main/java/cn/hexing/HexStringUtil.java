package cn.hexing;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * @author caibinglong
 *         date 2018/7/20.
 *         desc desc
 */

public class HexStringUtil {

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
     * 字符串转义
     *
     * @param str 原字符串
     * @return 转义
     */
    public static String toEscape(String str) {
//        点的转义：. ==> u002E
//        美元符号的转义：$ ==> u0024
//        乘方符号的转义：^ ==> u005E
//        左大括号的转义：{ ==> u007B
//            左方括号的转义：[ ==> u005B
//            左圆括号的转义：( ==> u0028
//            竖线的转义：| ==> u007C
//            右圆括号的转义：) ==> u0029
//            星号的转义：* ==> u002A
//            加号的转义：+ ==> u002B
//            问号的转义：? ==> u003F
//            反斜杠的转义： ==> u005C
        String result = str;
        switch (str) {
            case ".":
                result = "\\.";
                break;
            case "$":
                result = "\\$";
                break;
            case "^":
                result = "\\^";
                break;
            case "{":
                result = "\\{";
                break;
            case "[":
                result = "\\[";
                break;
            case "|":
                result = "\\|";
                break;
            case "*":
                result = "\\*";
                break;
            case ":":
                result = "\\:";
                break;
        }
        return result;
    }

    /**
     * 16进制 数据转换 对应的 图形字符串 46 转换 F
     * ascii 转换 字符串  如 31 转换成 1  30 转换 0
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
            if (decimal == 0) {
                stringBuilder.append(String.valueOf(decimal));
            } else {
                stringBuilder.append((char) decimal);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * byte[] 转换 16进制数据
     *
     * @param bytes byte[]
     * @return String
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(byteToString(bytes[i]));
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 转换16进制数据
     *
     * @param src byte
     * @return 16进制字符
     */
    public static String byteToString(byte src) {
        return String.format("%02x", src & 0xFF).toUpperCase();
    }

    /**
     * 转换16进制数据
     *
     * @param str ascii 字符串
     * @return 16进制字符
     */
    public static String asciiToHexString(String str) {
        char[] chars = str.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }


    /**
     *  *
     * 16进制直接转换成为字符串(无需Unicode解码)
     * <p>
     *  *
     *
     * @param hexStr  *
     * @return  
     */

    public static String hexToCharStr(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();

        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    /**
     * 字符串转换 byte[]
     *
     * @param hex 16进制 字符串
     * @return byte[]
     */
    public static byte[] hexToByte(String hex) {
        if (TextUtils.isEmpty(hex)) {
            return new byte[0];
        }
        hex = hex.replace(" ", "");
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
        if (TextUtils.isEmpty(str)) return "";
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
     * 8位 数据 位移计算  7位 发送数据帧
     * 用于 7数据位 E偶校验  1停止位
     *
     * @param data 数据帧
     * @return 计算后的数据帧
     */
    public static String getDisplacement(String data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length() / 2; i++) {
            String str = data.substring(i * 2, 2 + i * 2);
            String str1 = Integer.toBinaryString(Integer.parseInt(str, 16));
            int len1 = str1.length();//获取原来的字符串长度
            String s1 = str1.replaceAll("1", "");
            int len2 = s1.length();
            int lenTimes = len1 - len2;//出现的次数
            if (lenTimes % 2 != 0) {
                str1 = HexStringUtil.padRight(Integer.toHexString((Integer.valueOf(str1, 2)) | 0x80), 2, '0');
            } else {
                str1 = str;
            }
            stringBuilder.append(str1);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 解密 硬件返回字符串
     *
     * @param encryptData 加密字符串
     * @return 返回解密字符串 ascii
     */
    public static boolean decryptUpgrade(String encryptData) {
        //String data = "09 03 03 03 03 03 03 76 75 02 02 3E 39";
        if (TextUtils.isEmpty(encryptData)) {
            return false;
        }
        System.out.println("rec data=" + encryptData);
        String data = encryptData.replace(" ", "");
        data = exclusiveOrOperation(data);
        data = convertHexToString(data.toUpperCase());
        data = data.replace(" ", "").toUpperCase();
        System.out.println("rec 解析=" + data);
        if (data.contains("OK") || data.contains("RDY") || data.contains("DY")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 收到数据 &7F 做处理
     *
     * @param source 收到数据
     * @return String
     */
    public static String moveRecData(String source) {
        if (TextUtils.isEmpty(source)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        int j = source.length() / 2;
        int num;
        while (i < j) {
            num = Integer.parseInt(source.substring(i * 2, 2 + i * 2), 16);
            num = num & 127;
            stringBuilder.append(String.format("%02x", num));
            i++;
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 固件升级 加密 算法
     *
     * @param sourceData 原字符串
     */

    public static String upgradeEncrypt(String sourceData) {
        String last = "0D0A";
        //去除第一个字符冒号
        String tempData = sourceData.substring(1, sourceData.length());
        tempData = tempData.substring(0, tempData.length() - 2);
        //LL 部分
        String byteLen = tempData.substring(0, 2);
        // TT部分：00－代表数据记录、01－代表结束记录、02－代表扩展段地址记录、04－代表扩展线性地址记录（TDK•654xG使用此种地址扩展）
        String tt = tempData.substring(6, 8);
        //数据 第4位开始 取2个字节 是目标地址 AAAA部分 //第4位开始 取1个字节 是目标地址 TT部分
        String dataAddress = tempData.substring(2, 6);
        String hAddress = tempData.substring(2, 4);
        String sendData;
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sendBuilder;

        sendBuilder = new StringBuilder();
        //数据字节大于0  内容区域DD模块 每个字节需要加上0x20
        String context = tempData.substring(8, tempData.length());
        sendBuilder.append(byteLen);
        sendBuilder.append(dataAddress);
        sendBuilder.append(tt);
        for (int i = 0; i < context.length() / 2; i++) {
            int num = Integer.valueOf(context.substring(i * 2, i * 2 + 2), 16);
            num = num + Integer.valueOf(hAddress, 16);
            if (num > 0xFF) {
                num -= 0x100;
            }
            sendBuilder.append(String.format("%02x", num));
        }
        String cc = checkSum(sendBuilder.toString());
        sendBuilder.append(cc);
        //Log.e(TAG, "待发送数据=" + sendBuilder.toString());

        //取冒号 转Ascii
        sendData = sendBuilder.toString().toUpperCase();
        //Log.d(TAG, "待发送数据=" + sendData);
        stringBuilder.append(parseAscii(sourceData.substring(0, 1)));
        for (int i = 0; i < sendData.length(); i++) {
            stringBuilder.append(parseAscii(sendData.substring(i, i + 1)));
        }
        stringBuilder.append(last);
        sendData = stringBuilder.toString();
        //Log.d(TAG, "待发送Ascii数据=" + sendData);

        stringBuilder = new StringBuilder();
        for (int i = 0, k = sendData.length() / 2; i < k; i++) {
            //进行异或运算 0x33
            int num = Integer.parseInt(sendData.substring(i * 2, i * 2 + 2), 16);
            num = num ^ 0x33;
            stringBuilder.append(String.format("%02x", num));
        }
        if (":00000001FF".equals(sourceData)) {
            stringBuilder.append("3E39");
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 异或运算
     *
     * @param data 原字符串
     * @return 运算之后字符串
     */
    public static String exclusiveOrOperation(String data) {
        StringBuilder stringBuilder = new StringBuilder();
        if (TextUtils.isEmpty(data)) {
            return stringBuilder.toString();
        }
        int i = 0;
        int j = data.length() / 2;
        while (i < j) {
            String res = String.format("%02x", 0x33 ^ Integer.parseInt(data.substring(i * 2, 2 + i * 2), 16));
            stringBuilder.append(res);
            i++;
        }
        return stringBuilder.toString();
    }

    /**
     * 校验和 取补码
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
        res = 0xFF - res + 1;
        String str = Integer.toHexString(res);
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str.substring(str.length() - 2, str.length()).toUpperCase();
    }

    /**
     * 校验和
     *
     * @param bytes [] 原字符串
     * @return 返回校验和
     */
    public static String checkSum2(byte[] bytes) {
        int sum = 0;
        for (int i = 0; i < bytes.length; i++) {
            sum += bytes[i] & 0xff;
        }
        if (sum > 0xff) {
            sum -= 0x100;
        }
        sum = 0xFF - sum;
        String str = Integer.toHexString(sum);
        if (str.length() == 1) {
            str = "0" + str;
        }
        return str.substring(str.length() - 2, str.length()).toUpperCase();
    }

    /**
     * 高低字节位互换
     *
     * @param dataInteger int
     * @param byteLength  byte len
     * @return byte[]
     */
    public static byte[] GetIntegerBytes(int dataInteger, int byteLength) {
        List<Byte> list = new ArrayList<>();
        while (dataInteger > 0) {
            list.add((byte) (dataInteger % 256));
            dataInteger >>= 8;
        }
        if (list.size() > byteLength) {
            return new byte[0];
        } else {
            int listCount = list.size();
            for (int i = 0; i < byteLength - listCount; i++) {
                list.add((byte) 0x00);
            }
        }

        byte[] bytResult = new byte[list.size()];
        for (int i = 0; i < bytResult.length; i++) {
            bytResult[i] = list.get(i);
        }
        return bytResult;
    }

    public static String makeCheckSum(byte[] bytes) {
        return makeCheckSum(bytesToHexString(bytes));
    }

    /**
     * 累加和校验，并取反
     */
    public static String makeCheckSum(String data) {

        if (data == null || data.equals("")) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }

        //用256求余最大是255，即16进制的FF
        int mod = total % 256;
        if (mod == 0) {
            return "FF";
        } else {
            return String.format("%02X", mod);
        }
    }

    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return sdf.format(curDate);
    }
}
