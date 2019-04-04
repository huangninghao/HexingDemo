package cn.hexing.dlms.protocol.bll;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.hexing.dlms.HexDataFormat;
import cn.hexing.HexStringUtil;
import cn.hexing.model.TranXADRAssist;


public class dlmsService {

    private final static String TAG = dlmsService.class.getSimpleName();
//
//    public enum DataType {
//        bool,
//        octet_string_origal,
//        Octs_string,
//        Octs_hex,
//        Octs_ascii,
//        Ascs,
//        yyyy_mm_dd,
//        HH_mm_ss,
//        Octs_datetime,
//        Int32,
//        U32_hex,
//        unsigned32_decimal,
//        unsigned32_decimal2,
//        U32,
//        U8_hex,
//        U8,
//        unsigned8_workMode,
//        unsigned8_limiterActive,
//        unsigned16_hex,
//        Int64,
//        float32,
//        U16,
//        BCD,
//        Enum,
//        time,
//        mm_dd,
//        yy_mm_dd,
//        Array_dd,
//        Struct_Billing,
//        Array_Upgrade,
//        Bit_String,
//        Ascs_String,
//        Indonesia_Struct,
//        Null
//    }
//

    public enum DateType {
        YYMMDDhhmmssYYMMDDhhmmss, YYMMDDhhmmss, YYMMDDhhmm, YYYYMMDD, YYYYMMDDhhmmss, YYYYMMDDhhmm, MMDDhhmm, HHmmss, YYMMDDhhmmssNNNN, NNNNNNNN, NNNN, DDHHmmss,

    }

    public enum DtFormat {
        dd_MM_yyyy, MM_dd_yyyy, dd_yyyy_MM, yyyy_dd_MM, MM_yyyy_dd, yyyy_MM_dd,

    }

    private static DtFormat dtFormat = DtFormat.yyyy_MM_dd;

    /***
     * 解析函数
     *
     * @param XADRcodeStr byte[]
     * @param assist TranXADRAssist
     * @return String
     */
    public static TranXADRAssist tranXADRCode(byte[] XADRcodeStr, TranXADRAssist assist) {
        String RtnStr = "";// = HexStringUtil.convertHexToString(HexStringUtil.bytesToHexString(XADRcodeStr));
        switch (assist.dataType) {
            case HexDataFormat.ARRAY:
                RtnStr = String.format("%02X", (XADRcodeStr[15] & 0xff));
                break;
            case HexDataFormat.BOOL:
                if (XADRcodeStr[1] == 0x00) {
                    RtnStr = "00";
                } else {
                    RtnStr = "01";
                }
                break;
            case HexDataFormat.BIT_STRING:
                break;
            case HexDataFormat.OCTET_STRING:// octet_string
                int size = (XADRcodeStr[1] & 0xff);
                for (int i = 0; i < size; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
                }

                if (assist.getCodingType() == TranXADRAssist.CodingType.ASCS) {
                    //ascs 转换
                    RtnStr = HexStringUtil.convertHexToString(RtnStr);
                }

                break;
            case HexDataFormat.VISIBLE_STRING:
                //Ascs  0A
                size = (XADRcodeStr[1] & 0xff);
                for (int i = 0; i < size; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
                }
                RtnStr = HexStringUtil.convertHexToString(RtnStr);
                break;
            case HexDataFormat.DATE_TIME:
                //090C
                RtnStr = String.format(Locale.ENGLISH, "%04d", (XADRcodeStr[2] & 0xff) * 256
                        + (XADRcodeStr[3] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[4] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[5] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[7] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[8] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[9] & 0xff));
                RtnStr = FormatValue(RtnStr, DateType.YYYYMMDDhhmmss);
                break;
            case HexDataFormat.BCD:// BCD
                for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
                }
                if (!TextUtils.isEmpty(assist.format) && RtnStr.length() >= assist.format.length() - 1) {
                    StringBuilder str = new StringBuilder(RtnStr.substring(RtnStr.length() - assist.format.length() + 1));
                    int index = assist.format.indexOf(".");
                    if (index > 0) {
                        RtnStr = str.insert(index, ".").toString();
                    }
                }
                break;
            case HexDataFormat.DOUBLE_LONG:// int32
                for (int i = 0; i < 4; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
                }
                long res = parseInteger(RtnStr, HexDataFormat.DOUBLE_LONG);
                RtnStr = parseScale(res, assist.scale);
                break;
            case HexDataFormat.DOUBLE_LONG_UNSIGNED:// unsigned32
                if (TextUtils.isEmpty(assist.coding)) {
                    byte[] copy = new byte[4];
                    System.arraycopy(XADRcodeStr, XADRcodeStr.length - copy.length, copy, 0, copy.length);
                    RtnStr = Integer.toString((copy[0] & 0xff) * 0x1000000
                            + (copy[1] & 0xff) * 0x10000
                            + (copy[2] & 0xff) * 0x100
                            + (copy[3] & 0xff));
                    res = Long.parseLong(RtnStr);
                    RtnStr = parseScale(res, assist.scale);
                } else {
                    if (assist.coding.contains("Hex")){
                        for (int i = 0; i < 4; i++) {
                            RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
                        }
                    }
                }
                break;

            case HexDataFormat.UNSIGNED:// unsigned8
                RtnStr += Integer.toString((XADRcodeStr[1] & 0xff));
                res = parseInteger(RtnStr, HexDataFormat.UNSIGNED);
                RtnStr = parseScale(res, assist.scale);
                break;
            case HexDataFormat.LONG_UNSIGNED:
                int u16 = (XADRcodeStr[1] & 0xff) * 0x100 + (XADRcodeStr[2] & 0xff);
                if (TextUtils.isEmpty(assist.comments)) {
                    RtnStr = parseScale(u16, assist.scale);
                } else {
                    if (assist.comments.contains("bit")) {
                        RtnStr = Integer.toBinaryString(u16);
                        RtnStr = HexStringUtil.padRight(RtnStr, 16, '0');
                        StringBuilder builder = new StringBuilder(RtnStr);
                        builder.reverse();
                        RtnStr = builder.toString();
                    }
                }
                break;
            case HexDataFormat.LONG64:// int64
                for (int i = 0; i < 8; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
                }
                res = parseInteger(RtnStr, HexDataFormat.LONG64);
                RtnStr = parseScale(res, assist.scale);
                break;
            case HexDataFormat.ENUM:
                int Tmp = ((XADRcodeStr[1] & 0xff));
                RtnStr = String.format(Locale.ENGLISH, "%02d", Tmp);
                break;
            case HexDataFormat.FLOAT32:// float32
                byte[] tmpbytes = new byte[4];
                tmpbytes[0] = XADRcodeStr[4];
                tmpbytes[1] = XADRcodeStr[3];
                tmpbytes[2] = XADRcodeStr[2];
                tmpbytes[3] = XADRcodeStr[1];
                RtnStr = Float.toString(getFloat(tmpbytes));
                if (RtnStr.indexOf('.') > 0) {
                    RtnStr = RtnStr + "00";
                    RtnStr = RtnStr.substring(0, RtnStr.indexOf('.') + 3);
                }
                break;
            case HexDataFormat.INTEGER:
                RtnStr = Integer.toString((XADRcodeStr[1] & 0xff));
                break;
            case HexDataFormat.STRUCTURE:

                size = (XADRcodeStr[1] & 0xff);
                //成员个数
                int start = 2;
                if (assist.structList == null || assist.structList.size() == 0) {
                    StringBuilder str = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        switch (XADRcodeStr[start++] & 0xff) {
                            case 6:
                                for (int m = 0; m < 6; m++) {
                                    str.append(XADRcodeStr[start++] & 0xff);
                                    str.append("|");
                                }
                                break;
                            case 12:
                                str.append(XADRcodeStr[start++] & 0xff);
                                str.append("|");
                                str.append(XADRcodeStr[start++] & 0xff);
                                str.append("|");
                                break;
                            case 9:
                                int len = XADRcodeStr[start++] & 0xff;
                                for (int n = 0; n < len; n++) {
                                    str.append(XADRcodeStr[start++] & 0xff);
                                    str.append("|");
                                }
                                break;
                        }

                    }
                    RtnStr = str.toString();
                }
                if (assist.structList != null && assist.structList.size() > 0) {
                    start = 2;
                    TranXADRAssist.StructBean structBean;
                    String tmpStr;
                    long intTemp;

                    for (int i = 0; i < size; i++) {
                        structBean = assist.structList.get(i);
                        int nowDataType = XADRcodeStr[start++] & 0xff;
                        tmpStr = "";
                        switch (nowDataType) {
                            case HexDataFormat.OCTET_STRING:
                                int len = XADRcodeStr[start++] & 0xff;
                                for (int n = 0; n < len; n++) {
                                    tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                }
                                if (!TextUtils.isEmpty(structBean.coding)) {
                                    if (structBean.getCodingType() == TranXADRAssist.CodingType.ASCS) {
                                        //ascs 转换
                                        tmpStr = HexStringUtil.convertHexToString(tmpStr);
                                    }
                                }
                                structBean.value = tmpStr;
                                break;
                            case HexDataFormat.BOOL:
                                tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                structBean.value = tmpStr;
                                break;
                            case HexDataFormat.ENUM:
                                tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                structBean.value = tmpStr;
                                break;
                            case HexDataFormat.LONG64://int64
                                for (int m = 0; m < 8; m++) {
                                    tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                }
                                intTemp = parseInteger(tmpStr, HexDataFormat.LONG64);
                                tmpStr = parseScale(intTemp, structBean.scale);
                                structBean.value = tmpStr;
                                break;
                            case HexDataFormat.DOUBLE_LONG_UNSIGNED://unsigned32
                            case HexDataFormat.DOUBLE_LONG:
                                if (structBean.dataType == HexDataFormat.GPRS_IP) {
                                    StringBuilder str = new StringBuilder();
                                    len = XADRcodeStr[start++] & 0xff;
                                    for (int n = 0; n < len; n++) {
                                        String tem = String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                        //16 进制字符串
                                        str.append(HexStringUtil.convertHexToString(tem));
                                        //转换 图形码
                                    }
                                    structBean.value = str.toString();
                                    if (!structBean.visible) {
                                        assist.structList.remove(structBean);
                                        i--;
                                    }
                                } else {
                                    for (int m = 0; m < 4; m++) {
                                        tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                    }
                                    if (tmpStr.startsWith("FF") && tmpStr.endsWith("FF")) {
                                        tmpStr = "";
                                    } else {
                                        intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG);
                                        tmpStr = parseScale(intTemp, structBean.scale);
                                    }
                                    structBean.value = tmpStr;
                                }
                                break;
                            case HexDataFormat.LONG:
                                //long
                                for (int m = 0; m < 2; m++) {
                                    tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                }
                                intTemp = parseInteger(tmpStr, HexDataFormat.LONG);
                                tmpStr = parseScale(intTemp, structBean.scale);
                                structBean.value = tmpStr;
                                break;
                            //unsigned8
                            case HexDataFormat.UNSIGNED:
                                tmpStr = String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                structBean.value = tmpStr;
                                break;
                            //unsigned16
                            case HexDataFormat.LONG_UNSIGNED:
                                intTemp = (XADRcodeStr[start++] & 0xff) * 0x100 + (XADRcodeStr[start++] & 0xff);
                                tmpStr = parseScale(intTemp, structBean.scale);
                                structBean.value = tmpStr;
                                break;
                            case HexDataFormat.FLOAT32://float32
                                byte[] tmpBytes = new byte[4];
                                tmpBytes[3] = XADRcodeStr[start++];
                                tmpBytes[2] = XADRcodeStr[start++];
                                tmpBytes[1] = XADRcodeStr[start++];
                                tmpBytes[0] = XADRcodeStr[start++];

                                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmpBytes));
                                float valCPT = 0;
                                try {
                                    valCPT = dis.readFloat();
                                    dis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                tmpStr = parseScale((long) valCPT, structBean.scale);
                                structBean.value = tmpStr;
                                break;
                            case HexDataFormat.VISIBLE_STRING:
                                len = XADRcodeStr[start++] & 0xff;
                                for (int m = 0; m < len; m++) {
                                    tmpStr += String.format("%02X", (XADRcodeStr[m + 2] & 0xff));
                                }
                                tmpStr = HexStringUtil.convertHexToString(tmpStr);
                                structBean.value = tmpStr;
                                break;
                            default:
                                i--;
                                System.out.println("数据类型=" + nowDataType + "||下标=" + start);
                                break;
                        }
                    }
                    for (int j = 0; j < assist.structList.size(); j++) {
                        if (!assist.structList.get(j).visible) {
                            assist.structList.remove(j);
                        }
                    }
                }
                break;
            default:
                System.out.println("数据类型=" + assist.dataType + "||data=" + HexStringUtil.bytesToHexString(XADRcodeStr));

                break;
        }
        assist.value = RtnStr.toUpperCase();
        return assist;
    }

    /**
     * 量纲转换
     *
     * @param val   long
     * @param scale 量纲
     * @return String
     */
    public static String parseScale(long val, double scale) {
        String result;
        if (scale < 0) {
            scale = Math.abs(scale);
            String str = String.valueOf(val);
            String temp = "";
            if (str.startsWith("-")) {
                //有负数的情况
                temp = "-";
                str = str.substring(1);
            }
            if (str.length() <= scale) {
                str = HexStringUtil.padRight(str, (int) scale + 1, '0');
                str = temp + str;
            }
            result = new StringBuffer(str).insert(str.length() - (int) scale, ".").toString();
        } else {
            result = String.valueOf((int) (val * Math.pow(10, scale)));
        }
        return result;
    }

    /***
     * 解析函数
     *
     * @param XADRcodeStr byte[]
     * @param tranXADRAssist 数据对象
     * @return
     */
//    public static TranXADRAssist tranXADRCode(byte[] XADRcodeStr, TranXADRAssist tranXADRAssist) {
//        String RtnStr = "";
//        switch (XADRcodeStr[0] & 0xff) {
//            case HexDataFormat.ARRAY:
//                RtnStr = String.format("%02X", (XADRcodeStr[15] & 0xff));
//                break;
//            break;
//            case HexDataFormat.STRUCTURE:
//                int size = (XADRcodeStr[1] & 0xff);
//                //成员个数
//                int start = 0;
//                StringBuilder str = new StringBuilder();
//                switch (tranXADRAssist.dataType) {
//                    case HexDataFormat.GPRS_PDP_NAME_PASSWORD_STRUCT:
//                        start = 2;
//                        for (int i = 0; i < size; i++) {
//                            switch (XADRcodeStr[start++] & 0xff) {
//                                case 9:
//                                    // pdp name password 313233  转换 123
//                                    str = new StringBuilder();
//                                    int len = XADRcodeStr[start++] & 0xff;
//                                    for (int n = 0; n < len; n++) {
//                                        String tem = String.format("%02X", (XADRcodeStr[start++] & 0xff));
//                                        //16 进制字符串
//                                        str.append(HexStringUtil.convertHexToString(tem));
//                                        //转换 图形码
//                                    }
//                                    tranXADRAssist.structList.get(i).value = str.toString();
//                                    break;
//                            }
//
//                        }
//                        break;
//                    case HexDataFormat.GPRS_IP_PORT_APN_STRUCT:
//                        //成员个数
//                        start = 2;
//
//                        for (int i = 0; i < size; i++) {
//                            switch (XADRcodeStr[start++] & 0xff) {
//                                case 6:
//                                    // stationIP  u32
//                                    str = new StringBuilder();
//                                    for (int m = 0; m < 4; m++) {
//                                        str.append(XADRcodeStr[start++] & 0xff);
//                                        if (m < 3) {
//                                            str.append(".");
//                                        }
//                                    }
//                                    tranXADRAssist.structList.get(i).value = str.toString();
//                                    break;
//                                case 18:
//                                    //port
//                                    str = new StringBuilder();
//                                    str.append(String.format("%02X", (XADRcodeStr[start++] & 0xff)));
//                                    str.append(String.format("%02X", (XADRcodeStr[start++] & 0xff)));
//                                    tranXADRAssist.structList.get(i).value = String.valueOf(Integer.parseInt(str.toString(), 16));
//                                    break;
//                                case 9:
//                                    //apn
//                                    str = new StringBuilder();
//                                    int len = XADRcodeStr[start++] & 0xff;
//                                    for (int n = 0; n < len; n++) {
//                                        str.append(String.format("%02X", (XADRcodeStr[start++] & 0xff)));
//                                    }
//                                    tranXADRAssist.structList.get(i).value = HexStringUtil.convertHexToString(str.toString());
//                                    break;
//                            }
//
//                        }
//                        break;
//                }
//
//                RtnStr = str.toString();
//                break;
//            case 3:
//                switch (tranXADRAssist.decType) {
//                    case bool:
//                        if (XADRcodeStr[1] == 0x00) {
//                            RtnStr = "00";
//                        } else {
//                            RtnStr = "01";
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            case 4:
//
//                break;
//            case 9:// octet_string
//                switch (tranXADRAssist.decType) {
//                    case octet_string_origal:
//                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
//                            RtnStr += ((XADRcodeStr[i + 2] & 0xff));
//                        }
//                        break;
//                    case Octs_string:
//                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
//                            RtnStr += String
//                                    .format("%02X", (XADRcodeStr[i + 2] & 0xff));
//                        }
//                        break;
//                    case Octs_ascii:
//                    case Octs_hex:
//                        if ((XADRcodeStr[1] & 0xff) >= 0x80) {
//                            int lenofLen = (XADRcodeStr[1] & 0xff) - 0x80;
//                            int len = 0;
//                            for (int i = 0; i < lenofLen; i++) {
//                                len += (((XADRcodeStr[2 + i] & 0xff) * Math.pow(0x100,
//                                        lenofLen - 1 - i)));
//                            }
//                            String temp = null;
//                            try {
//                                temp = new String(XADRcodeStr, 2 + lenofLen, len,
//                                        "ASCII");
//                            } catch (UnsupportedEncodingException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                            RtnStr += temp;
//                        } else {
//                            String temp = null;
//                            try {
//                                temp = new String(XADRcodeStr, 2,
//                                        (XADRcodeStr[1] & 0xff), "ASCII");
//                            } catch (UnsupportedEncodingException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                            RtnStr += temp;
//                        }
//                        break;
//                    case Ascs:
//                        RtnStr += byteToString(XADRcodeStr, 2, XADRcodeStr.length - 2);
//                        break;
//                    case yyyy_mm_dd:
//                        RtnStr = String.format("%04d", (XADRcodeStr[2] & 0xff) * 256
//                                + (XADRcodeStr[3] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[4] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[5] & 0xff));
//                        RtnStr = FormatValue(RtnStr, 1, 1, 1, 1, DateType.YYYYMMDD);
//                        break;
//                    case HH_mm_ss:
//                        RtnStr = String.format("%02d", (XADRcodeStr[2] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[3] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[4] & 0xff));
//                        RtnStr = FormatValue(RtnStr, 1, 1, 1, 1, DateType.HHmmss);
//                        break;
//                    case Octs_datetime:
//                        RtnStr = String.format("%04d", (XADRcodeStr[2] & 0xff) * 256
//                                + (XADRcodeStr[3] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[4] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[5] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[7] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[8] & 0xff))
//                                + String.format("%02d", (XADRcodeStr[9] & 0xff));
//                        RtnStr = FormatValue(RtnStr, 1, 1, 1, 1,
//                                DateType.YYYYMMDDhhmmss);
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 10:
//                switch (tranXADRAssist.decType) {
//                    case Ascs:
//                        RtnStr += byteToString(XADRcodeStr, 2, XADRcodeStr.length - 2).replace(" ", "");
//                        break;
//                    case Ascs_String:
//                        for (int i = 0; i < (XADRcodeStr[1] & 0xFF); i++) {
//                            RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            case 13:// BCD
//                switch (tranXADRAssist.decType) {
//                    case BCD:
//                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
//                            RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            case 5:// int32
//                switch (tranXADRAssist.decType) {
//                    case Int32:
//                        for (int i = 0; i < 4; i++) {
//                            RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
//                        }
//
//                        Integer int32 = Integer.parseInt(RtnStr, 16);
//                        Double dlResult = int32 * Math.pow(10, tranXADRAssist.scale);
//                        RtnStr = getDecimalValue(dlResult, ((Double) tranXADRAssist.scale).intValue());
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 6:// unsigned32
//                switch (tranXADRAssist.decType) {
//                    case U32_hex:
//                        for (int i = 0; i < 4; i++) {
//                            RtnStr += String
//                                    .format("%02X", (XADRcodeStr[i + 1] & 0xff));
//                        }
//                        break;
//                    case unsigned32_decimal:
//                        RtnStr = Integer.toString((XADRcodeStr[1] & 0xff) * 0x1000000
//                                + (XADRcodeStr[2] & 0xff) * 0x10000
//                                + (XADRcodeStr[3] & 0xff) * 0x100
//                                + (XADRcodeStr[4] & 0xff));
//                        break;
//                    case unsigned32_decimal2:
//                        RtnStr = Integer
//                                .toString(((XADRcodeStr[1] & 0xff) * 0x1000000
//                                        + (XADRcodeStr[2] & 0xff) * 0x10000
//                                        + (XADRcodeStr[3] & 0xff) * 0x100 + (XADRcodeStr[4] & 0xff)) / 100);
//                        break;
//                    case U16:
//                    case U32:// 增加U32的解析lyh
//                        Integer inResult = (XADRcodeStr[1] & 0xff) * 0x1000000
//                                + (XADRcodeStr[2] & 0xff) * 0x10000
//                                + (XADRcodeStr[3] & 0xff) * 0x100
//                                + (XADRcodeStr[4] & 0xff);
//                        Double dlResult = inResult * Math.pow(10, tranXADRAssist.scale);
//                        RtnStr = getDecimalValue(dlResult, ((Double) tranXADRAssist.scale).intValue());
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 17:// unsigned8
//                switch (tranXADRAssist.decType) {
//                    case U8_hex:
//                        RtnStr += String.format("%02X", (XADRcodeStr[1] & 0xff));
//                        break;
//                    case U8:
//                        RtnStr += Integer.toString((XADRcodeStr[1] & 0xff));
//                        break;
//                    case unsigned8_workMode:
//                        switch ((XADRcodeStr[1] & 0xff)) {
//                            case 0x00:
//                                RtnStr = "Normal mode";
//                                break;
//                            case 0x01:
//                                RtnStr = "Pre-payment";
//                                break;
//                            default:
//                                break;
//                        }
//
//                        break;
//                    case unsigned8_limiterActive:
//                        switch ((XADRcodeStr[1] & 0xff)) {
//                            case 0x00:
//                                RtnStr = "Deactive";
//                                break;
//                            case 0x01:
//                                RtnStr = "Active";
//                                break;
//                            case (byte) 0xff:
//                                RtnStr = "Not valid";
//                                break;
//                            default:
//                                break;
//                        }
//                        break;
//                    default:
//                        RtnStr += String.format("%02X", (XADRcodeStr[1] & 0xff), 16);
//                        break;
//                }
//                break;
//            case 18:
//                switch (tranXADRAssist.decType) {
//                    case U16:
//                        int u16 = (XADRcodeStr[1] & 0xff) * 0x100 + (XADRcodeStr[2] & 0xff);
//                        Double dlResult = u16 * Math.pow(10, tranXADRAssist.scale);
//                        RtnStr = getDecimalValue(dlResult, ((Double) tranXADRAssist.scale).intValue());
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 20:// int64
//                switch (tranXADRAssist.decType) {
//                    case Int64:
//                        for (int i = 0; i < 8; i++) {
//                            RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
//                        }
//                        long temp = parseInteger(RtnStr, HexDataFormat.LONG64);
//                        Double dlResult = temp * Math.pow(10, tranXADRAssist.scale);
//                        RtnStr = getDecimalValue(dlResult, ((Double) tranXADRAssist.scale).intValue());
//                        break;
//                    default:
//                        for (int i = 0; i < 8; i++) {
//                            RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
//                        }
//                        break;
//                }
//                break;
//            case 22:
//                switch (tranXADRAssist.decType) {
//                    case Enum:
//                        int Tmp = ((XADRcodeStr[1] & 0xff));
//                        RtnStr = String.valueOf(Tmp);
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 23:// float32
//                switch (tranXADRAssist.decType) {
//                    case Enum:
//                        byte[] tmpbytes = new byte[4];
//                        tmpbytes[0] = XADRcodeStr[4];
//                        tmpbytes[1] = XADRcodeStr[3];
//                        tmpbytes[2] = XADRcodeStr[2];
//                        tmpbytes[3] = XADRcodeStr[1];
//                        RtnStr = Float.toString(getFloat(tmpbytes));
//                        if (RtnStr.indexOf('.') > 0) {
//                            RtnStr = RtnStr + "00";
//                            RtnStr = RtnStr.substring(0, RtnStr.indexOf('.') + 3);
//                        }
//                        break;
//                    default:
//                        break;
//                }
//                break;
//            case 15:
//                RtnStr = Integer.toString((XADRcodeStr[1] & 0xff));
//                break;
//            default:
//                break;
//        }
//        tranXADRAssist.value = RtnStr;
//        return tranXADRAssist;
//    }

    /***
     * 解析函数
     *
     * @param XADRcodeStr byte[]
     * @param tranXADRAssist 数据对象
     * @return 固件返回数据 解析完的对象
     */
    public static TranXADRAssist tranXADRCode2(byte[] XADRcodeStr, TranXADRAssist tranXADRAssist) {
        String RtnStr = "";
        switch (XADRcodeStr[0] & 0xff) {
            case HexDataFormat.ARRAY:
                switch (tranXADRAssist.dataType) {
                    case HexDataFormat.ARRAY:// Array_dd
                        RtnStr = String.format("%02X", (XADRcodeStr[15] & 0xff));
                        break;
                }
                break;
            case HexDataFormat.STRUCTURE:
                int size = (XADRcodeStr[1] & 0xff);
                //成员个数
                int start;
                if (tranXADRAssist.structList != null && tranXADRAssist.structList.size() > 0) {
                    List<TranXADRAssist.StructBean> structBeans = new ArrayList<>();
                    start = 2;
                    TranXADRAssist.StructBean assist;
                    String tmpStr = "";
                    long intTemp;
                    for (int i = 0; i < size; i++) {
                        assist = new TranXADRAssist.StructBean();
                        assist.name = tranXADRAssist.structList.get(i).name;
                        assist.scale = tranXADRAssist.structList.get(i).scale;
                        assist.dataType = tranXADRAssist.structList.get(i).dataType;
                        assist.unit = tranXADRAssist.structList.get(i).unit;
                        assist.size = tranXADRAssist.structList.get(i).size;
                        assist.visible = tranXADRAssist.structList.get(i).visible;
                        int nowDataType = XADRcodeStr[start++] & 0xff;
                        switch (nowDataType) {
                            case HexDataFormat.BOOL:
                                tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            case HexDataFormat.ENUM:
                                tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            case HexDataFormat.LONG64://int64
                                for (int m = 0; m < 8; m++) {
                                    tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                }
                                intTemp = parseInteger(tmpStr, HexDataFormat.LONG64);
                                tmpStr = parseScale(intTemp, assist.scale);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            case HexDataFormat.DOUBLE_LONG_UNSIGNED://unsigned32
                            case HexDataFormat.DOUBLE_LONG:
                                if (assist.dataType == HexDataFormat.GPRS_IP) {
                                    StringBuilder str = new StringBuilder();
                                    int len = XADRcodeStr[start++] & 0xff;
                                    for (int n = 0; n < len; n++) {
                                        String tem = String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                        //16 进制字符串
                                        str.append(HexStringUtil.convertHexToString(tem));
                                        //转换 图形码
                                    }
                                    if (assist.visible) {
                                        assist.value = str.toString();
                                        structBeans.add(assist);
                                    }
                                } else {
                                    for (int m = 0; m < 4; m++) {
                                        tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                    }
                                    intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG);
                                    tmpStr = parseScale(intTemp, assist.scale);
                                    if (assist.visible) {
                                        assist.value = tmpStr;
                                        structBeans.add(assist);
                                    }
                                }
                                break;
                            case HexDataFormat.LONG:
                                //long
                                for (int m = 0; m < 2; m++) {
                                    tmpStr += String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                }
                                intTemp = parseInteger(tmpStr, HexDataFormat.LONG);
                                tmpStr = parseScale(intTemp, assist.scale);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            //unsigned8
                            case HexDataFormat.UNSIGNED:
                                tmpStr = String.format("%02X", (XADRcodeStr[start++] & 0xff));
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            //unsigned16
                            case HexDataFormat.LONG_UNSIGNED:
                                intTemp = (XADRcodeStr[start++] & 0xff) * 0x100 + (XADRcodeStr[start++] & 0xff);
                                tmpStr = parseScale(intTemp, assist.scale);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            case HexDataFormat.FLOAT32://float32
                                byte[] tmpBytes = new byte[4];
                                tmpBytes[3] = XADRcodeStr[start++];
                                tmpBytes[2] = XADRcodeStr[start++];
                                tmpBytes[1] = XADRcodeStr[start++];
                                tmpBytes[0] = XADRcodeStr[start++];

                                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmpBytes));
                                float valCPT = 0;
                                try {
                                    valCPT = dis.readFloat();
                                    dis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                tmpStr = parseScale((long) valCPT, assist.scale);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    structBeans.add(assist);
                                }
                                break;
                            default:
                                i--;
                                System.out.println("数据类型=" + nowDataType + "||下标=" + start);
                                break;

                        }
                    }
                    tranXADRAssist.structList = structBeans;
                }
//
//
//                switch (tranXADRAssist.dataType) {
//                    case HexDataFormat.GPRS_PDP_NAME_PASSWORD_STRUCT:
//                        start = 2;
//                        for (int i = 0; i < size; i++) {
//                            switch (XADRcodeStr[start++] & 0xff) {
//                                case 9:
//                                    // pdp name password 313233  转换 123
//                                    str = new StringBuilder();
//                                    int len = XADRcodeStr[start++] & 0xff;
//                                    for (int n = 0; n < len; n++) {
//                                        String tem = String.format("%02X", (XADRcodeStr[start++] & 0xff));
//                                        //16 进制字符串
//                                        str.append(HexStringUtil.convertHexToString(tem));
//                                        //转换 图形码
//                                    }
//                                    tranXADRAssist.structList.get(i).value = str.toString();
//                                    break;
//                            }
//
//                        }
//                        break;
//                    case HexDataFormat.GPRS_IP_PORT_APN_STRUCT:
//                        //成员个数
//                        start = 2;
//
//                        for (int i = 0; i < size; i++) {
//                            switch (XADRcodeStr[start++] & 0xff) {
//                                case 6:
//                                    // stationIP  u32
//                                    str = new StringBuilder();
//                                    for (int m = 0; m < 4; m++) {
//                                        str.append(XADRcodeStr[start++] & 0xff);
//                                        if (m < 3) {
//                                            str.append(".");
//                                        }
//                                    }
//                                    tranXADRAssist.structList.get(i).value = str.toString();
//                                    break;
//                                case 18:
//                                    //port
//                                    str = new StringBuilder();
//                                    str.append(String.format("%02X", (XADRcodeStr[start++] & 0xff)));
//                                    str.append(String.format("%02X", (XADRcodeStr[start++] & 0xff)));
//                                    tranXADRAssist.structList.get(i).value = String.valueOf(Integer.parseInt(str.toString(), 16));
//                                    break;
//                                case 9:
//                                    //apn
//                                    str = new StringBuilder();
//                                    int len = XADRcodeStr[start++] & 0xff;
//                                    for (int n = 0; n < len; n++) {
//                                        str.append(String.format("%02X", (XADRcodeStr[start++] & 0xff)));
//                                    }
//                                    tranXADRAssist.structList.get(i).value = HexStringUtil.convertHexToString(str.toString());
//                                    break;
//                            }
//
//                        }
//                        break;
//                }
                break;
            case HexDataFormat.BOOL:
                // bool:
                if (XADRcodeStr[1] == 0x00) {
                    RtnStr = "00";
                } else {
                    RtnStr = "01";
                }
                break;

            case HexDataFormat.BIT_STRING:

                break;
            case HexDataFormat.OCTET_STRING:// octet_string
                switch (tranXADRAssist.dataType) {
                    case HexDataFormat.OCTET_STRING:// Octs_string:
                        for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                            RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
                        }
                        break;
                    case HexDataFormat.OCTET_STRING_ASCS://Octs_ascii: //Octs_hex
                        if ((XADRcodeStr[1] & 0xff) >= 0x80) {
                            int lenofLen = (XADRcodeStr[1] & 0xff) - 0x80;
                            int len = 0;
                            for (int i = 0; i < lenofLen; i++) {
                                len += (((XADRcodeStr[2 + i] & 0xff) * Math.pow(0x100,
                                        lenofLen - 1 - i)));
                            }
                            String temp = null;
                            try {
                                temp = new String(XADRcodeStr, 2 + lenofLen, len, "ASCII");
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            RtnStr += temp;
                        } else {
                            String temp = null;
                            try {
                                temp = new String(XADRcodeStr, 2,
                                        (XADRcodeStr[1] & 0xff), "ASCII");
                            } catch (UnsupportedEncodingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            RtnStr += temp;
                        }
                        break;
                    case HexDataFormat.DATE_TIME://Octs_datetime:
                        RtnStr = String.format(Locale.ENGLISH, "%04d", (XADRcodeStr[2] & 0xff) * 256
                                + (XADRcodeStr[3] & 0xff))
                                + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[4] & 0xff))
                                + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[5] & 0xff))
                                + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[7] & 0xff))
                                + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[8] & 0xff))
                                + String.format(Locale.ENGLISH, "%02d", (XADRcodeStr[9] & 0xff));
                        RtnStr = FormatValue(RtnStr, DateType.YYYYMMDDhhmmss);
                        break;
                    default:
                        break;
                }
                break;
            case HexDataFormat.VISIBLE_STRING:
                for (int i = 0; i < (XADRcodeStr[1] & 0xFF); i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
                }
                RtnStr = HexStringUtil.convertHexToString(RtnStr);
                break;
            case HexDataFormat.BCD:// BCD
                for (int i = 0; i < (XADRcodeStr[1] & 0xff); i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 2] & 0xff));
                }
                break;
            case HexDataFormat.DOUBLE_LONG:// int32
                for (int i = 0; i < 4; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
                }
                long res = parseInteger(RtnStr, HexDataFormat.DOUBLE_LONG);
                res = (long) (res * Math.pow(10, tranXADRAssist.scale));
                RtnStr = String.valueOf(res);
                break;
            case HexDataFormat.DOUBLE_LONG_UNSIGNED:// unsigned32
                for (int i = 0; i < 4; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
                }
                res = parseInteger(RtnStr, HexDataFormat.DOUBLE_LONG_UNSIGNED);
                res = (long) (res * Math.pow(10, tranXADRAssist.scale));
                RtnStr = String.valueOf(res);
                break;
            case HexDataFormat.UNSIGNED:// unsigned8
                switch (tranXADRAssist.dataType) {
                    case HexDataFormat.UNSIGNED:
                        RtnStr += Integer.toString((XADRcodeStr[1] & 0xff));
                        break;
                    default:
                        RtnStr += String.format("%02X", (XADRcodeStr[1] & 0xff));
                        break;
                }
                break;
            case HexDataFormat.LONG_UNSIGNED:
                //u16
                int u16 = (XADRcodeStr[1] & 0xff) * 0x100 + (XADRcodeStr[2] & 0xff);
                res = parseInteger(String.valueOf(u16), HexDataFormat.LONG_UNSIGNED);
                res = (long) (res * Math.pow(10, tranXADRAssist.scale));
                RtnStr = String.valueOf(res);
                break;
            case HexDataFormat.LONG64:// int64
                for (int i = 0; i < 8; i++) {
                    RtnStr += String.format("%02X", (XADRcodeStr[i + 1] & 0xff));
                }
                res = parseInteger(RtnStr, HexDataFormat.LONG64);
                res = (long) (res * Math.pow(10, tranXADRAssist.scale));
                RtnStr = String.valueOf(res);
                break;
            case HexDataFormat.ENUM:
                int Tmp = ((XADRcodeStr[1] & 0xff));
                RtnStr = String.valueOf(Tmp);
                break;
            case HexDataFormat.FLOAT32:// float32
                byte[] tmpbytes = new byte[4];
                tmpbytes[0] = XADRcodeStr[4];
                tmpbytes[1] = XADRcodeStr[3];
                tmpbytes[2] = XADRcodeStr[2];
                tmpbytes[3] = XADRcodeStr[1];
                RtnStr = Float.toString(getFloat(tmpbytes));
                if (RtnStr.indexOf('.') > 0) {
                    RtnStr = RtnStr + "00";
                    RtnStr = RtnStr.substring(0, RtnStr.indexOf('.') + 3);
                }
                break;
            case HexDataFormat.INTEGER: //int8
                RtnStr = Integer.toString((XADRcodeStr[1] & 0xff));
                break;
            default:
                break;
        }

        tranXADRAssist.value = RtnStr;
        return tranXADRAssist;
    }

    /**
     * 数据块解析
     *
     * @param XADRCodeStr        byte[]
     * @param listTranXADRAssist List<TranXADRAssist>
     * @return List<String>
     */
    public static List<List<TranXADRAssist>> TranBillingList(byte[] XADRCodeStr, List<TranXADRAssist> listTranXADRAssist) {
        Integer IData;
        long intTemp;
        int dataStartIndex = 0;
        if ((XADRCodeStr[0] & 0xff) == 0x01) {
            //数组 结构体
            dataStartIndex = 2;
        }
        //冻结配置项个数
        int ItemCount = listTranXADRAssist.size();
        String tmpStr;
        ////冻结记录条数
        int rowSize = XADRCodeStr[1];
        List<List<TranXADRAssist>> dataList = new ArrayList<>();
        TranXADRAssist assist;
        List<TranXADRAssist> itemList;
        try {
            for (int i = 0; i < rowSize; i++) {
                itemList = new ArrayList<>();
                dataStartIndex = dataStartIndex + 2;
                if (ItemCount > 0x80) {
                    dataStartIndex++;
                }
                //每条记录的项数
                for (int j = 0; j < ItemCount; j++) {
                    assist = listTranXADRAssist.get(j).clone();
                    tmpStr = "";
                    int nowDataType = XADRCodeStr[dataStartIndex++] & 0xff;
                    switch (nowDataType) {
                        case HexDataFormat.OCTET_STRING:
                            int tem = XADRCodeStr[dataStartIndex++];
                            if (tem == 0x0c)//date_time
                            {
                                if (TextUtils.isEmpty(assist.coding)
                                        || assist.coding.toUpperCase().equals("HEX")) {
                                    int year = XADRCodeStr[dataStartIndex++] & 0xff;
                                    if (year != 255) {
                                        tmpStr = String.format(Locale.ENGLISH, "%04d", year * 256 + (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff));
                                        dataStartIndex++;
                                        tmpStr += String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff));
                                        tmpStr = FormatValue(tmpStr, DateType.YYYYMMDDhhmmss);
                                        dataStartIndex = dataStartIndex + 4;
                                        if (assist.visible) {
                                            assist.value = tmpStr;
                                            itemList.add(assist);
                                        }
                                    } else {
                                        dataStartIndex += 11;
                                        tmpStr = "FFFF-FF-FF FF:FF:FF";
                                        if (assist.visible) {
                                            assist.value = tmpStr;
                                            itemList.add(assist);
                                        }
                                    }

                                } else if ((assist.coding != null && assist.coding.toUpperCase().equals("ASCII"))
                                        || assist.dataType == HexDataFormat.OCTET_STRING_ASCS) {
                                    for (int m = 0; m < tem; m++) {
                                        tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                                    }
                                    tmpStr = HexStringUtil.convertHexToString(tmpStr);
                                    if (assist.visible) {
                                        assist.value = tmpStr;
                                        itemList.add(assist);
                                    }
                                }
                            } else if (tem == HexDataFormat.DOUBLE_LONG_UNSIGNED) {
                                for (int m = 0; m < 4; m++) {
                                    tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                                }
                                intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG_UNSIGNED);
                                tmpStr = parseScale(intTemp, assist.scale);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    itemList.add(assist);
                                }
                            } else if (assist.coding != null && assist.coding.toUpperCase().equals("ASCII")) {
                                for (int m = 0; m < tem; m++) {
                                    tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                                }
                                tmpStr = HexStringUtil.convertHexToString(tmpStr);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    itemList.add(assist);
                                }
                            }
                            break;
                        case HexDataFormat.BOOL:
                            tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        case HexDataFormat.ENUM:
                            tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        case HexDataFormat.LONG64://int64
                            for (int m = 0; m < 8; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.LONG64);
                            tmpStr = parseScale(intTemp, assist.scale);
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        case HexDataFormat.DOUBLE_LONG_UNSIGNED://unsigned32
                        case HexDataFormat.DOUBLE_LONG:
                            for (int m = 0; m < 4; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG);
                            tmpStr = parseScale(intTemp, assist.scale);
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        case HexDataFormat.BCD://bcd
                            int forCount = XADRCodeStr[dataStartIndex++];
                            if (assist.structList != null && assist.structList.size() > 0) {
                                for (int m = 0; m < assist.structList.size(); m++) {
                                    tmpStr = "";
                                    for (int k = 0; k < assist.structList.get(m).size; k++) {
                                        tmpStr += String.format("%02X", XADRCodeStr[dataStartIndex++] & 0xff);
                                    }
                                    if (assist.structList.get(m).dataType == HexDataFormat.INTEGER) {
                                        //直接转换int
                                        try {
                                            intTemp = Integer.parseInt(tmpStr);
                                            tmpStr = parseScale(intTemp, assist.structList.get(m).scale);
                                        } catch (Exception ex) {
                                            System.out.println("BCD8转换int 失败");
                                        }
                                    }
                                    assist.structList.get(m).value = tmpStr;
                                }
                                if (assist.visible) {
                                    itemList.add(assist);
                                }
                            } else {
                                long lResult = 0;
                                for (int k = 0; k < forCount; k++) {
                                    lResult = (XADRCodeStr[dataStartIndex++] & 0xff);
                                    lResult = (long) (lResult * Math.pow(100, forCount - k - 1));
                                }
                                tmpStr = parseScale(lResult, assist.scale);
                                if (assist.visible) {
                                    assist.value = tmpStr;
                                    itemList.add(assist);
                                }
                            }
                            break;
                        case HexDataFormat.LONG:
                            //long
                            for (int m = 0; m < 2; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.LONG);
                            tmpStr = parseScale(intTemp, assist.scale);
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        //unsigned8
                        case HexDataFormat.UNSIGNED:
                            tmpStr = String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        //unsigned16
                        case HexDataFormat.LONG_UNSIGNED:
                            IData = (XADRCodeStr[dataStartIndex++] & 0xff) * 0x100 + (XADRCodeStr[dataStartIndex++] & 0xff);
                            tmpStr = parseScale(IData, assist.scale);
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        case HexDataFormat.FLOAT32://float32
                            byte[] tmpbytes = new byte[4];
                            tmpbytes[3] = XADRCodeStr[dataStartIndex++];
                            tmpbytes[2] = XADRCodeStr[dataStartIndex++];
                            tmpbytes[1] = XADRCodeStr[dataStartIndex++];
                            tmpbytes[0] = XADRCodeStr[dataStartIndex++];

                            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmpbytes));
                            float valCPT = dis.readFloat();
                            dis.close();
                            tmpStr = parseScale((long) valCPT, assist.scale);
                            if (assist.visible) {
                                assist.value = tmpStr;
                                itemList.add(assist);
                            }
                            break;
                        case HexDataFormat.STRUCTURE:
                            dataStartIndex++;
                            if (ItemCount > 0x80) {
                                dataStartIndex++;
                            }
                            break;
                        default:
                            j--;
                            System.out.println("数据类型=" + nowDataType + "||下标=" + dataStartIndex);
                            break;
                    }

                }
                dataList.add(itemList);
            }

        } catch (Exception ex) {
            System.out.println("错误=" + ex.getMessage());
        }
        return dataList;
    }

    /**
     * 数据块解析
     *
     * @param XADRCodeStr byte[]
     * @param assist      TranXADRAssist
     * @return List<String>
     */
    public static List<TranXADRAssist> TranBillingList(byte[] XADRCodeStr, TranXADRAssist assist) {
        Integer IData;
        long intTemp;
        int dataStartIndex = 0;
        if ((XADRCodeStr[0] & 0xff) == 0x01) {
            //数组 结构体
            dataStartIndex = 2;
        }
        if (assist.structList == null || assist.structList.size() == 0) {
            System.out.println("冻结数据解析=子项数据未配置");
            return new ArrayList<>();
        }
        //冻结配置项个数
        int ItemCount = assist.structList.size();
        String tmpStr;
        ////冻结记录条数
        int rowSize = XADRCodeStr[1];
        List<TranXADRAssist> dataList = new ArrayList<>();
        TranXADRAssist.StructBean structBean;
        List<TranXADRAssist.StructBean> itemList;
        TranXADRAssist tranXADRAssist;
        try {
            for (int i = 0; i < rowSize; i++) {
                tranXADRAssist = new TranXADRAssist();
                tranXADRAssist.obis = assist.obis;
                tranXADRAssist.markNo = assist.markNo;
                tranXADRAssist.readType = assist.readType;
                itemList = new ArrayList<>();
                tranXADRAssist.structList = itemList;
                dataList.add(tranXADRAssist);
                dataStartIndex = dataStartIndex + 2;
                if (ItemCount > 0x80) {
                    dataStartIndex++;
                }
                //每条记录的项数
                for (int j = 0; j < ItemCount; j++) {
                    structBean = assist.structList.get(j).clone();
                    tmpStr = "";
                    int nowDataType = XADRCodeStr[dataStartIndex++] & 0xff;
                    switch (nowDataType) {
                        case HexDataFormat.OCTET_STRING:
                            int tem = XADRCodeStr[dataStartIndex++];
                            if (tem == 0x0c)//date_time
                            {
                                if (TextUtils.isEmpty(structBean.coding)
                                        || structBean.coding.toUpperCase().equals("HEX")) {
                                    int year = XADRCodeStr[dataStartIndex++] & 0xff;
                                    if (year != 255) {
                                        tmpStr = String.format(Locale.ENGLISH, "%04d", year * 256 + (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff));
                                        dataStartIndex++;
                                        tmpStr += String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff))
                                                + String.format(Locale.ENGLISH, "%02d", (XADRCodeStr[dataStartIndex++] & 0xff));
                                        tmpStr = FormatValue(tmpStr, DateType.YYYYMMDDhhmmss);
                                        dataStartIndex = dataStartIndex + 4;
                                        if (structBean.visible) {
                                            structBean.value = tmpStr;
                                            itemList.add(structBean);
                                        }
                                    } else {
                                        dataStartIndex += 11;
                                        tmpStr = "FFFF-FF-FF FF:FF:FF";
                                        if (structBean.visible) {
                                            structBean.value = tmpStr;
                                            itemList.add(structBean);
                                        }
                                    }

                                } else if ((structBean.coding != null && structBean.coding.toUpperCase().equals("ASCII"))
                                        || structBean.dataType == HexDataFormat.OCTET_STRING_ASCS) {
                                    for (int m = 0; m < tem; m++) {
                                        tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                                    }
                                    tmpStr = HexStringUtil.convertHexToString(tmpStr);
                                    if (structBean.visible) {
                                        structBean.value = tmpStr;
                                        itemList.add(structBean);
                                    }
                                }
                            } else if (tem == HexDataFormat.DOUBLE_LONG_UNSIGNED) {
                                for (int m = 0; m < 4; m++) {
                                    tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                                }
                                intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG_UNSIGNED);
                                tmpStr = parseScale(intTemp, structBean.scale);
                                if (structBean.visible) {
                                    structBean.value = tmpStr;
                                    itemList.add(structBean);
                                }
                            } else if (structBean.coding != null && structBean.coding.toUpperCase().equals("ASCII")) {
                                for (int m = 0; m < tem; m++) {
                                    tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                                }
                                tmpStr = HexStringUtil.convertHexToString(tmpStr);
                                if (structBean.visible) {
                                    structBean.value = tmpStr;
                                    itemList.add(structBean);
                                }
                            }
                            break;
                        case HexDataFormat.BOOL:
                            tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.ENUM:
                            tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.LONG64://int64
                            for (int m = 0; m < 8; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.LONG64);
                            tmpStr = parseScale(intTemp, structBean.scale);
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.DOUBLE_LONG_UNSIGNED://unsigned32
                        case HexDataFormat.DOUBLE_LONG:
                            for (int m = 0; m < 4; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG);
                            tmpStr = parseScale(intTemp, structBean.scale);
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.BCD://bcd
                            int forCount = XADRCodeStr[dataStartIndex++];
                            if (structBean.beanItems != null && structBean.beanItems.size() > 0) {
                                for (int m = 0; m < structBean.beanItems.size(); m++) {
                                    tmpStr = "";
                                    for (int k = 0; k < structBean.beanItems.get(m).size; k++) {
                                        tmpStr += String.format("%02X", XADRCodeStr[dataStartIndex++] & 0xff);
                                    }
                                    if (structBean.beanItems.get(m).dataType == HexDataFormat.INTEGER) {
                                        //直接转换int
                                        try {
                                            intTemp = Integer.parseInt(tmpStr);
                                            tmpStr = parseScale(intTemp, structBean.beanItems.get(m).scale);
                                        } catch (Exception ex) {
                                            System.out.println("BCD8转换int 失败");
                                        }
                                    }
                                    structBean.beanItems.get(m).value = tmpStr;
                                }
                                if (structBean.visible) {
                                    itemList.add(structBean);
                                }
                            } else {
                                long lResult = 0;
                                for (int k = 0; k < forCount; k++) {
                                    lResult = (XADRCodeStr[dataStartIndex++] & 0xff);
                                    lResult = (long) (lResult * Math.pow(100, forCount - k - 1));
                                }
                                tmpStr = parseScale(lResult, structBean.scale);
                                if (structBean.visible) {
                                    structBean.value = tmpStr;
                                    itemList.add(structBean);
                                }
                            }
                            break;
                        case HexDataFormat.LONG:
                            //long
                            for (int m = 0; m < 2; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.LONG);
                            tmpStr = parseScale(intTemp, structBean.scale);
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        //unsigned8
                        case HexDataFormat.UNSIGNED:
                            tmpStr = String.format("%02X", (XADRCodeStr[dataStartIndex++] & 0xff));
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        //unsigned16
                        case HexDataFormat.LONG_UNSIGNED:
                            IData = (XADRCodeStr[dataStartIndex++] & 0xff) * 0x100 + (XADRCodeStr[dataStartIndex++] & 0xff);
                            tmpStr = parseScale(IData, structBean.scale);
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.FLOAT32://float32
                            byte[] tmpbytes = new byte[4];
                            tmpbytes[3] = XADRCodeStr[dataStartIndex++];
                            tmpbytes[2] = XADRCodeStr[dataStartIndex++];
                            tmpbytes[1] = XADRCodeStr[dataStartIndex++];
                            tmpbytes[0] = XADRCodeStr[dataStartIndex++];

                            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmpbytes));
                            float valCPT = dis.readFloat();
                            dis.close();
                            tmpStr = parseScale((long) valCPT, structBean.scale);
                            if (structBean.visible) {
                                structBean.value = tmpStr;
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.NULL_DATA:
                            if (structBean.visible) {
                                structBean.value = "";
                                itemList.add(structBean);
                            }
                            break;
                        case HexDataFormat.STRUCTURE:
                            dataStartIndex++;
                            if (ItemCount > 0x80) {
                                dataStartIndex++;
                            }
                            j--;
                            break;
                        default:
                            j--;
                            dataStartIndex++;
                            System.out.println("数据类型=" + nowDataType + "||下标=" + dataStartIndex);
                            break;
                    }

                }
            }

        } catch (Exception ex) {
            System.out.println("错误=" + ex.getMessage());
        }
        return dataList;
    }

    /**
     * 日费率解析
     *
     * @param XADRCodeStr byte[]
     * @param assist      list
     * @return list
     */
    public static List<TranXADRAssist> getTranRate(byte[] XADRCodeStr, TranXADRAssist assist) {
        //01 03
        //02 02 11 01 01 02 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02
        //02 02 11 02 01 01 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02
        //02 02 11 02 01 01 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02
        List<TranXADRAssist> resultList = new ArrayList<>();
        TranXADRAssist.StructBean beanItem = assist.structList.get(0);
        int dataStartIndex = 0;
        if (XADRCodeStr.length <= 5 ||
                assist.structList == null || assist.structList.size() == 0) {
            //字节不够 直接返回
            return resultList;
        }
        if ((XADRCodeStr[0] & 0xff) == 0x01) {
            //数组 结构体
            dataStartIndex = 2;
        }
        //接收数据的 数组大小
        int size = (XADRCodeStr[1] & 0xff);
        String temp = "";
        long IData;
        int nowType;
        try {
            TranXADRAssist recTran;
            for (int i = 0; i < size; i++) {
                recTran = new TranXADRAssist();
                recTran.obis = assist.obis;
                recTran.structList = new ArrayList<>();
                dataStartIndex += 3;
                String dayId = String.valueOf(XADRCodeStr[dataStartIndex++] & 0xFF);
                resultList.add(recTran);
                dataStartIndex++;
                int timeSize = XADRCodeStr[dataStartIndex++] & 0xFF;
                //每条记录的 子项个数
                int itemCount = beanItem.beanItems.size();
                TranXADRAssist.StructBean struct;
                TranXADRAssist.StructBean.BeanItem bean;
                for (int m = 0; m < timeSize; m++) {
                    //循环 时段
                    struct = new TranXADRAssist.StructBean();
                    recTran.structList.add(struct);
                    recTran.structList.get(m).value = dayId;
                    recTran.structList.get(m).beanItems = new ArrayList<>();
                    for (int j = 0; j < itemCount; j++) {
                        bean = new TranXADRAssist.StructBean.BeanItem();
                        nowType = XADRCodeStr[dataStartIndex++] & 0xFF;
                        switch (nowType) {
                            case HexDataFormat.STRUCTURE:
                                dataStartIndex++;
                                j--;
                                break;
                            case HexDataFormat.ARRAY:
                                dataStartIndex++;
                                j--;
                                break;
                            case HexDataFormat.OCTET_STRING:
                                int len = XADRCodeStr[dataStartIndex++] & 0xFF;
                                temp = "";
                                for (int k = 0; k < len; k++) {
                                    temp += String.format("%02X", XADRCodeStr[dataStartIndex++] & 0xFF);
                                }
                                bean.value = temp;
                                recTran.structList.get(m).beanItems.add(bean);
                                break;
                            case HexDataFormat.LONG_UNSIGNED:
                                IData = (XADRCodeStr[dataStartIndex++] & 0xFF) * 0x100 + (XADRCodeStr[dataStartIndex++] & 0xFF);
                                temp = parseScale(IData, bean.scale);
                                bean.value = temp;
                                recTran.structList.get(m).beanItems.add(bean);
                                break;
                            case HexDataFormat.VISIBLE_STRING:
                                len = XADRCodeStr[dataStartIndex++] & 0xFF;
                                for (int k = 0; k < len; k++) {
                                    temp += String.format("%02X", XADRCodeStr[dataStartIndex++] & 0xFF);
                                }
                                temp = HexStringUtil.convertHexToString(temp);
                                bean.value = temp;
                                recTran.structList.get(m).beanItems.add(bean);
                                break;
                        }

                    }
                }

            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return resultList;
    }

    /**
     * 日费率解析
     *
     * @param XADRCodeStr byte[]
     * @param assist      list
     * @return list
     */
    public static List<TranXADRAssist> getDisplay(byte[] XADRCodeStr, TranXADRAssist assist) {
        //01 03
        //02 02
        // 11 01 01 02 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02
        // 11 02 01 01 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02
        // 11 02 01 01 02 03 09 04 00 00 FF FF 09 06 00 00 0A 00 64 FF 12 00 02
        //0202
        List<TranXADRAssist> resultList = new ArrayList<>();
        int dataStartIndex = 0;
        if (XADRCodeStr.length <= 5 ||
                assist.structList == null || assist.structList.size() == 0) {
            //字节不够 直接返回
            return resultList;
        }
        if ((XADRCodeStr[0] & 0xff) == 0x01) {
            //数组 结构体
            dataStartIndex = 4;
        }
        //接收数据的 数组大小
        int size = (XADRCodeStr[1] & 0xff);
        String temp = "";
        long IData;
        int nowType;
        try {
            TranXADRAssist recTran;
            for (int i = 0; i < size; i++) {
                recTran = assist.clone();
                resultList.add(recTran);
                //每条记录的 子项个数
                TranXADRAssist.StructBean struct;
                for (int m = 0; m < recTran.structList.size(); m++) {
                    //循环 时段
                    struct = recTran.structList.get(m);
                    nowType = XADRCodeStr[dataStartIndex++] & 0xFF;
                    temp = "";
                    switch (nowType) {
                        case HexDataFormat.STRUCTURE:
                            dataStartIndex++;
                            m--;
                            break;
                        case HexDataFormat.ARRAY:
                            dataStartIndex++;
                            m--;
                            break;
                        case HexDataFormat.OCTET_STRING:
                            int len = XADRCodeStr[dataStartIndex++] & 0xFF;
                            for (int k = 0; k < len; k++) {
                                temp += String.format("%02X", XADRCodeStr[dataStartIndex++] & 0xFF);
                            }
                            struct.value = temp;
                            break;
                        case HexDataFormat.LONG_UNSIGNED:
                            IData = (XADRCodeStr[dataStartIndex++] & 0xFF) * 0x100 + (XADRCodeStr[dataStartIndex++] & 0xFF);
                            temp = parseScale(IData, struct.scale);
                            struct.value = temp;
                            break;
                        case HexDataFormat.VISIBLE_STRING:
                            len = XADRCodeStr[dataStartIndex++] & 0xFF;
                            for (int k = 0; k < len; k++) {
                                temp += String.format("%02X", XADRCodeStr[dataStartIndex++] & 0xFF);
                            }
                            temp = HexStringUtil.convertHexToString(temp);
                            struct.value = temp;
                            break;
                        default:
                            m--;
                            dataStartIndex++;
                            break;
                    }
                }

            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return resultList;
    }

    /**
     * 数据块解析
     *
     * @param XADRCodeStr        byte[]
     * @param listTranXADRAssist List<TranXADRAssist>
     * @return List<String>
     */
    public static String TranBillingCode(byte[] XADRCodeStr, List<TranXADRAssist> listTranXADRAssist) {
        StringBuilder stringBuilder = new StringBuilder();
        long intTemp;
        stringBuilder.append("[");
        int TmpIndex = 2;
        int ItemCnt = listTranXADRAssist.size();//冻结配置项个数
        String tmpStr;

        try {
            int pos = XADRCodeStr[1];//冻结记录条数
            for (int i = 0; i < pos; i++) {

                TmpIndex = TmpIndex + 2;
                if (ItemCnt > 0x80) {
                    TmpIndex++;
                }
                stringBuilder.append("{");
                for (int j = 0; j < ItemCnt; j++)//每条记录的项数
                {

                    Double scale = listTranXADRAssist.get(j).scale;
                    String strName = listTranXADRAssist.get(j).name;
                    String strUnit = listTranXADRAssist.get(j).unit;
                    tmpStr = "";
                    stringBuilder.append("\"");
                    stringBuilder.append(strName);
                    stringBuilder.append("\"");
                    stringBuilder.append(":");

                    switch (XADRCodeStr[TmpIndex++] & 0xff) {
                        case 9:
                            if (XADRCodeStr[TmpIndex++] == 0x0c)//date_time
                            {
                                int year = XADRCodeStr[TmpIndex++] & 0xff;
                                if (year != 255) {
                                    tmpStr = String.format("%04d", year * 256 + (XADRCodeStr[TmpIndex++] & 0xff))
                                            + String.format("%02d", (XADRCodeStr[TmpIndex++] & 0xff))
                                            + String.format("%02d", (XADRCodeStr[TmpIndex++] & 0xff))
                                            + String.format("%02d", (XADRCodeStr[TmpIndex + 2] & 0xff))
                                            + String.format("%02d", (XADRCodeStr[TmpIndex++] & 0xff))
                                            + String.format("%02d", (XADRCodeStr[TmpIndex++] & 0xff));
                                    tmpStr = FormatValue(tmpStr, DateType.YYYYMMDDhhmmss);
                                } else {
                                    TmpIndex += 7;
                                    tmpStr = "FFFF-FF-FF FF:FF:FF";
                                }
                                TmpIndex = TmpIndex + 6;
                            }
                            break;
                        case 6://unsigned32
                            Integer inResult = (XADRCodeStr[TmpIndex++] & 0xff) * 0x1000000
                                    + (XADRCodeStr[TmpIndex++] & 0xff) * 0x10000
                                    + (XADRCodeStr[TmpIndex++] & 0xff) * 0x100
                                    + (XADRCodeStr[TmpIndex++] & 0xff);

                            Double dlResult = inResult * Math.pow(10, scale);
                            tmpStr = Double.toString(dlResult) + strUnit;
                            break;
                        case HexDataFormat.DOUBLE_LONG:
                            for (int m = 0; m < 4; m++) {
                                tmpStr += String.format("%02X", (XADRCodeStr[TmpIndex++] & 0xff));
                            }
                            intTemp = parseInteger(tmpStr, HexDataFormat.DOUBLE_LONG);
                            dlResult = intTemp * Math.pow(10, scale);
                            tmpStr = Double.toString(dlResult) + strUnit;
                            break;
                        case 13://bcd
                            int ForCount = XADRCodeStr[TmpIndex++] & 0xff;
                            long lResult = 0;
                            for (int k = 0; k < ForCount; k++) {
                                Integer TmpInt = (XADRCodeStr[TmpIndex++] & 0xff);

                                lResult = (long) (TmpInt * Math.pow(100, ForCount - k - 1));

                            }

                            dlResult = lResult * Math.pow(10, scale);
                            tmpStr = Double.toString(dlResult) + strUnit;
                            break;
                        case 17://unsigned8
                            tmpStr += Integer.valueOf(String.format("%02X", (XADRCodeStr[TmpIndex++] & 0xff)), 16);
                            break;
                        case 18://unsigned16
                            Integer u16 = 0;
                            u16 = (XADRCodeStr[TmpIndex++] & 0xff) * 0x100 + (XADRCodeStr[TmpIndex++] & 0xff);
                            dlResult = u16 * Math.pow(10, scale);
                            tmpStr = Double.toString(dlResult) + strUnit;
                            break;
                        case 20://int64
                            String RtnStr = "";
                            for (int k = 0; k < 8; k++) {
                                RtnStr += String.format("%02X", (XADRCodeStr[TmpIndex++] & 0xff));
                            }
                            intTemp = parseInteger(RtnStr, HexDataFormat.LONG64);
                            dlResult = intTemp * Math.pow(10, scale);
                            tmpStr = Double.toString(dlResult) + strUnit;
                            break;
                        case 23://float32
                            byte[] tmpbytes = new byte[4];
                            tmpbytes[3] = XADRCodeStr[TmpIndex++];
                            tmpbytes[2] = XADRCodeStr[TmpIndex++];
                            tmpbytes[1] = XADRCodeStr[TmpIndex++];
                            tmpbytes[0] = XADRCodeStr[TmpIndex++];

                            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmpbytes));
                            float valCPT = dis.readFloat();
                            dis.close();
                            dlResult = valCPT * Math.pow(10, scale);
                            tmpStr = Double.toString(dlResult) + strUnit;
                            break;
                        default:
                            break;
                    }
                    stringBuilder.append("\"");
                    stringBuilder.append(tmpStr);
                    stringBuilder.append("\"");
                    if (j < ItemCnt - 1) {
                        stringBuilder.append(",");
                    }
                }

                stringBuilder.append("}");
                if (i < pos - 1) {
                    stringBuilder.append(",");
                }
                Log.e(TAG, "数据=" + stringBuilder.toString());
            }
        } catch (Exception ex) {
            System.out.println("错误=" + ex.getMessage());
        }
        return stringBuilder.append("]").toString();
    }

    /**
     * 解析冻结捕获对象
     *
     * @param XADRCodeStr byte[]
     * @return List<TranXADRAssist>
     */
    public static List<TranXADRAssist> TranBillingCode(byte[] XADRCodeStr) {
        List<TranXADRAssist> tranXADRAssists = new ArrayList<>();
        int TmpIndex = 2;
        int itemSize = XADRCodeStr[3];
        int size = XADRCodeStr[1];//冻结项数
        for (int i = 0; i < size; i++) {
            TmpIndex = TmpIndex + 2;
            TranXADRAssist tranXADRAssist = new TranXADRAssist();
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < itemSize; j++) {
                int dataType = XADRCodeStr[TmpIndex++] & 0xff;
                switch (dataType) {
                    case HexDataFormat.LONG_UNSIGNED://unsigned16
                        stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        break;
                    case HexDataFormat.OCTET_STRING: //octet-string
                        int len = XADRCodeStr[TmpIndex++];
                        for (int k = 0; k < len; k++) {
                            stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        }
                        break;
                    case HexDataFormat.INTEGER:
                        stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        break;
                    default:
                        Log.e("TranBillingCode", "TmpIndex=" + TmpIndex + "||" + (XADRCodeStr[5] & 0xFF));
                        break;
                }
            }
            tranXADRAssist.obis = stringBuilder.toString();
            tranXADRAssist.obis = tranXADRAssist.obis.substring(0, tranXADRAssist.obis.length() - 4);
            tranXADRAssists.add(tranXADRAssist);
        }
        return tranXADRAssists;
    }

    /**
     * 解析冻结捕获对象 obis
     *
     * @param XADRCodeStr byte[]
     * @return List<TranXADRAssist>
     */
    public static TranXADRAssist TranBillingCodeNew(byte[] XADRCodeStr, TranXADRAssist assist) {
        assist.structList = new ArrayList<>();
        int TmpIndex = 2;
        int itemSize = XADRCodeStr[3];
        int size = XADRCodeStr[1];//冻结项数
        for (int i = 0; i < size; i++) {
            TmpIndex = TmpIndex + 2;
            TranXADRAssist.StructBean structBean = new TranXADRAssist.StructBean();
            assist.structList.add(structBean);
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < itemSize; j++) {
                int dataType = XADRCodeStr[TmpIndex++] & 0xff;
                switch (dataType) {
                    case HexDataFormat.LONG_UNSIGNED://unsigned16
                        stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        break;
                    case HexDataFormat.OCTET_STRING: //octet-string
                        int len = XADRCodeStr[TmpIndex++];
                        for (int k = 0; k < len; k++) {
                            stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        }
                        break;
                    case HexDataFormat.INTEGER:
                        stringBuilder.append(HexStringUtil.byteToString(XADRCodeStr[TmpIndex++]));
                        break;
                    default:
                        Log.e("TranBillingCode", "TmpIndex=" + TmpIndex + "||" + (XADRCodeStr[5] & 0xFF));
                        break;
                }
            }
            structBean.obis = stringBuilder.toString();
            structBean.obis = structBean.obis.substring(0, structBean.obis.length() - 4);
        }
        return assist;
    }

    /**
     * 数据解析 最新版本
     *
     * @param bytes  byte[]
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist ByteToAnalysis(byte[] bytes, TranXADRAssist assist) {
        switch (assist.dataType) {
            case HexDataFormat.ARRAY:
                assist.value = String.format("%02X", (bytes[15] & 0xff));
                break;
            case HexDataFormat.BOOL:
                if (bytes[1] == 0x00) {
                    assist.value = "00";
                } else {
                    assist.value = "01";
                }
                break;
            case HexDataFormat.BIT_STRING:
                break;
            case HexDataFormat.OCTET_STRING:// octet_string
                int size = (bytes[1] & 0xff);
                for (int i = 0; i < size; i++) {
                    assist.value += String.format("%02X", (bytes[i + 2] & 0xff));
                }
                break;
            case HexDataFormat.VISIBLE_STRING:
                //Ascs  0A
                size = (bytes[1] & 0xff);
                for (int i = 0; i < size; i++) {
                    assist.value += String.format("%02X", (bytes[i + 2] & 0xff));
                }
                assist.value = HexStringUtil.convertHexToString(assist.value);
                break;
            case HexDataFormat.DATE_TIME:
                //090C
                assist.value = String.format(Locale.ENGLISH, "%04d", (bytes[2] & 0xff) * 256
                        + (bytes[3] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (bytes[4] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (bytes[5] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (bytes[7] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (bytes[8] & 0xff))
                        + String.format(Locale.ENGLISH, "%02d", (bytes[9] & 0xff));
                assist.value = FormatValue(assist.value, DateType.YYYYMMDDhhmmss);
                break;
            case HexDataFormat.BCD:// BCD
                for (int i = 0; i < (bytes[1] & 0xff); i++) {
                    assist.value += String.format("%02X", (bytes[i + 2] & 0xff));
                }
                if (!TextUtils.isEmpty(assist.format) && assist.value.length() >= assist.format.length() - 1) {
                    StringBuilder str = new StringBuilder(assist.value.substring(assist.value.length() - assist.format.length() + 1));
                    int index = assist.format.indexOf(".");
                    if (index > 0) {
                        assist.value = str.insert(index, ".").toString();
                    }
                }
                break;
            case HexDataFormat.DOUBLE_LONG:// int32
                for (int i = 0; i < 4; i++) {
                    assist.value += String.format("%02X", (bytes[i + 1] & 0xff));
                }
                long res = parseInteger(assist.value, HexDataFormat.DOUBLE_LONG);
                assist.value = parseScale(res, assist.scale);
                break;
            case HexDataFormat.DOUBLE_LONG_UNSIGNED:// unsigned32
                byte[] copy = new byte[4];
                System.arraycopy(bytes, bytes.length - copy.length, copy, 0, copy.length);
                assist.value = Integer.toString((copy[0] & 0xff) * 0x1000000
                        + (copy[1] & 0xff) * 0x10000
                        + (copy[2] & 0xff) * 0x100
                        + (copy[3] & 0xff));
                res = Long.parseLong(assist.value);
                assist.value = parseScale(res, assist.scale);
                break;

            case HexDataFormat.UNSIGNED:// unsigned8
                assist.value += Integer.toString((bytes[1] & 0xff));
                res = parseInteger(assist.value, HexDataFormat.UNSIGNED);
                assist.value = parseScale(res, assist.scale);
                break;
            case HexDataFormat.LONG_UNSIGNED:
                int u16 = (bytes[1] & 0xff) * 0x100 + (bytes[2] & 0xff);
                if (TextUtils.isEmpty(assist.comments)) {
                    assist.value = parseScale(u16, assist.scale);
                } else {
                    // bit 数据处理
                    if (assist.comments.contains("bit")) {
                        assist.value = Integer.toBinaryString(u16);
                        assist.value = HexStringUtil.padRight(assist.value, 16, '0');
                        StringBuilder builder = new StringBuilder(assist.value);
                        builder.reverse();
                        assist.value = builder.toString();
                    }
                }
                break;
            case HexDataFormat.LONG64:// int64
                for (int i = 0; i < 8; i++) {
                    assist.value += String.format("%02X", (bytes[i + 1] & 0xff));
                }
                res = parseInteger(assist.value, HexDataFormat.LONG64);
                assist.value = parseScale(res, assist.scale);
                break;
            case HexDataFormat.ENUM:
                int Tmp = ((bytes[1] & 0xff));
                assist.value = String.format(Locale.ENGLISH, "%02d", Tmp);
                break;
            case HexDataFormat.FLOAT32:// float32
                byte[] tmpbytes = new byte[4];
                tmpbytes[0] = bytes[4];
                tmpbytes[1] = bytes[3];
                tmpbytes[2] = bytes[2];
                tmpbytes[3] = bytes[1];
                assist.value = Float.toString(getFloat(tmpbytes));
                if (assist.value.indexOf('.') > 0) {
                    assist.value = assist.value + "00";
                    assist.value = assist.value.substring(0, assist.value.indexOf('.') + 3);
                }
                break;
            case HexDataFormat.INTEGER:
                assist.value = Integer.toString((bytes[1] & 0xff));
                break;
            case HexDataFormat.DAY_RATE:
                assist.structList = new ArrayList<>();
                int TmpIndex = 2;
                int itemSize = bytes[3];
                size = bytes[1];//冻结项数
                for (int i = 0; i < size; i++) {
                    TmpIndex = TmpIndex + 2;
                    TranXADRAssist.StructBean structBean = new TranXADRAssist.StructBean();
                    assist.structList.add(structBean);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j < itemSize; j++) {
                        int dataType = bytes[TmpIndex++] & 0xff;
                        switch (dataType) {
                            case HexDataFormat.LONG_UNSIGNED://unsigned16
                                stringBuilder.append(HexStringUtil.byteToString(bytes[TmpIndex++]));
                                stringBuilder.append(HexStringUtil.byteToString(bytes[TmpIndex++]));
                                break;
                            case HexDataFormat.OCTET_STRING: //octet-string
                                int len = bytes[TmpIndex++];
                                for (int k = 0; k < len; k++) {
                                    stringBuilder.append(HexStringUtil.byteToString(bytes[TmpIndex++]));
                                }
                                break;
                            case HexDataFormat.INTEGER:
                                stringBuilder.append(HexStringUtil.byteToString(bytes[TmpIndex++]));
                                break;
                            default:
                                Log.e("TranBillingCode", "TmpIndex=" + TmpIndex + "||" + (bytes[5] & 0xFF));
                                break;
                        }
                    }
                    structBean.obis = stringBuilder.toString();
                    structBean.obis = structBean.obis.substring(0, structBean.obis.length() - 4);
                }
                break;
            case HexDataFormat.FREEZE_BILLING:
                //冻结数据块 解析
                break;
            case HexDataFormat.FREEZE_CAPTURE:
                //冻结项 捕获对象
                break;
        }

        return assist;
    }

    public static int numLen(int len) {
        int rtnNum = 0;

        while (len % 0xff > 0) {
            rtnNum++;
            len = len / 0xff;
        }

        return rtnNum;
    }


    /***
     * 组织数据
     *
     * @param assist 数据对象 数据内容在 StructBean 结构体中
     * @return 字符串
     */
    public static String GetXADRCode(TranXADRAssist assist) {
        List<TranXADRAssist.StructBean> structList = assist.structList;
        StringBuilder stringBuilder = new StringBuilder();
        int size = 0;
        switch (assist.dataType) {
            case HexDataFormat.GPRS_IP_PORT_APN_STRUCT:
                size = structList.size();
                stringBuilder.append("02");
                stringBuilder.append(String.format("%02X", size));
                for (int i = 0; i < size; i++) {
                    switch (structList.get(i).dataType) {
                        case HexDataFormat.GPRS_IP:
                            stringBuilder.append("06");
                            if (structList.get(i).format.length() == 0) {
                                structList.get(i).format = ".";
                                structList.get(i).format = HexStringUtil.toEscape(structList.get(i).format);
                            }
                            String[] str = structList.get(i).value.split(structList.get(i).format);
                            for (int j = 0; j < str.length; j++) {
                                stringBuilder.append(String.format("%02X", Integer.parseInt(str[j])));
                            }
                            break;
                        case HexDataFormat.GPRS_PORT:
                            stringBuilder.append("12");
                            stringBuilder.append(String.format("%04X", Integer.parseInt(structList.get(i).value)));
                            break;
                        case HexDataFormat.GPRS_APN:
                            int len = structList.get(i).value.length();
                            stringBuilder.append("09");
                            stringBuilder.append(String.format("%02X", len));
                            stringBuilder.append(HexStringUtil.bytesToHexString(structList.get(i).value.getBytes()));
                            break;
                    }
                }
                break;
            case HexDataFormat.OCTET_STRING:
                if (!TextUtils.isEmpty(assist.coding)) {
                    switch (assist.coding) {
                        case HexDataFormat.OCTET_CODING_HEX: //16进制
                            stringBuilder.append(String.format("%02X", HexDataFormat.OCTET_STRING));
                            stringBuilder.append(String.format("%02X", assist.writeData.length()));
                            stringBuilder.append(HexStringUtil.bytesToHexString(assist.writeData.getBytes()));
                            break;
                        case HexDataFormat.OCTET_CODING_ASCS://ascs
                            stringBuilder.append(String.format("%02X", HexDataFormat.OCTET_STRING));
                            stringBuilder.append(String.format("%02X", assist.writeData.length()));
                            stringBuilder.append(HexStringUtil.parseAscii(assist.writeData));
                            break;
                        case HexDataFormat.OCTET_CODING_STRING://原始数据下发
                            stringBuilder.append(String.format("%02X", HexDataFormat.OCTET_STRING));
                            stringBuilder.append(String.format("%02X", assist.writeData.length()));
                            stringBuilder.append(assist.writeData);
                            break;
                        case HexDataFormat.OCTET_CODING_TOKEN://原始数据下发
                            stringBuilder.append(String.format("%02X", HexDataFormat.OCTET_STRING));
                            stringBuilder.append(String.format("%02X", assist.writeData.length() / 2));
                            stringBuilder.append(assist.writeData);
                            break;
                    }
                } else {
                    size = structList.size();
                    if (size > 1) {
                        //说明是结构体数据
                        stringBuilder.append("02");
                        stringBuilder.append(String.format("%02X", size));
                        for (int i = 0; i < size; i++) {
                            switch (structList.get(i).dataType) {
                                case HexDataFormat.OCTET_STRING_ASCS:
                                    stringBuilder.append("09");
                                    int len = structList.get(i).value.length();
                                    stringBuilder.append(String.format("%02X", len));
                                    stringBuilder.append(HexStringUtil.parseAscii(structList.get(i).value));
                                    break;
                            }
                        }
                    }
                }
                break;
            case HexDataFormat.UNSIGNED:
                stringBuilder.append(String.format("%02X", HexDataFormat.UNSIGNED));
                stringBuilder.append(String.format("%02X", (Integer.parseInt(assist.writeData))));
                break;
            case HexDataFormat.LONG_UNSIGNED:
                stringBuilder.append(String.format("%02X", HexDataFormat.LONG_UNSIGNED));
                stringBuilder.append(String.format("%04X", (Long.parseLong(assist.writeData))));
                break;
            case HexDataFormat.BOOL:
                stringBuilder.append(String.format("%02X", HexDataFormat.BOOL));
                stringBuilder.append(assist.writeData);
                break;
            case HexDataFormat.DOUBLE_LONG:
                stringBuilder.append(String.format("%02X", HexDataFormat.DOUBLE_LONG));
                stringBuilder.append(String.format("%08x", (Integer.parseInt(assist.writeData))));
                break;
            case HexDataFormat.DOUBLE_LONG_UNSIGNED:
                stringBuilder.append(String.format("%02X", HexDataFormat.DOUBLE_LONG_UNSIGNED));
                stringBuilder.append(String.format("%08x", (Long.parseLong(assist.writeData))));
                break;
            case HexDataFormat.VISIBLE_STRING:
                //ascs
                stringBuilder.append(String.format("%02X", HexDataFormat.VISIBLE_STRING));
                if (assist.writeData.length() < 128) {
                    stringBuilder.append(String.format("%02X", assist.writeData.length()));
                } else {
                    int len = numLen(assist.writeData.length() / 2);
                    stringBuilder.append(Integer.toString((0x80 + len), 16));// "09"
                    stringBuilder.append(String.format("%02X", Integer.parseInt(assist.writeData)));
                }
                stringBuilder.append(HexStringUtil.bytesToHexString(assist.writeData.getBytes()));
                break;
            case HexDataFormat.MONTH_SETT_DATE:
                //设置月结算日期
                stringBuilder.append("0101");
                stringBuilder.append("0202");
                stringBuilder.append("090400000000");
                stringBuilder.append("0905");
                stringBuilder.append("FFFFFF");
                stringBuilder.append(assist.writeData);
                stringBuilder.append("FF");
                break;
            case HexDataFormat.DATE_TIME:
                Date dt = new Date();
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                try {
                    dt = dateFormat2.parse(assist.writeData);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);
                int year = cal.get(Calendar.YEAR);// 获取年份
                int month = cal.get(Calendar.MONTH) + 1;// 获取月份
                int day = cal.get(Calendar.DATE);// 获取日
                int hour = cal.get(Calendar.HOUR_OF_DAY);// 小时
                int minute = cal.get(Calendar.MINUTE);// 分
                int second = cal.get(Calendar.SECOND);// 秒
                stringBuilder.append("090C");
                stringBuilder.append(String.format("%04X", year));
                stringBuilder.append(String.format("%02X", month));
                stringBuilder.append(String.format("%02X", day));
                stringBuilder.append(getWeekOfDate(dt));
                stringBuilder.append(String.format("%02X", hour));
                stringBuilder.append(String.format("%02X", minute));
                stringBuilder.append(String.format("%02X", second));
                stringBuilder.append("FF");
                stringBuilder.append("01E0");
                stringBuilder.append("00");
                break;
            case HexDataFormat.DAY_RATE:
                //日费率

                break;

            case HexDataFormat.FREEZE_CAPTURE:

                break;
            default:
                stringBuilder.append(assist.writeData);
                break;
        }
        Log.v(TAG, "待写入数据=" + stringBuilder.toString().toUpperCase());
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 多条设置拼接
     *
     * @param assists list
     * @return String
     */
    public static String GetXADRCode(List<TranXADRAssist> assists) {
        StringBuilder stringBuilder = new StringBuilder();
        if (assists == null || assists.size() == 0) {
            return "";
        }
        int size = 0;
        stringBuilder.append("01");
        switch (assists.get(0).dataType) {
            case HexDataFormat.DAY_RATE:
                size = assists.size();
                stringBuilder.append(String.format("%02X", size));
                for (int i = 0; i < size; i++) {
                    if (assists.get(i).structList.get(0).beanItems == null ||
                            assists.get(i).structList.get(0).beanItems.size() == 0) {
                        System.out.println("费率设置=子元素数据异常");
                        break;
                    }
                    stringBuilder.append("0202");
                    stringBuilder.append("11");//day id
                    stringBuilder.append(String.format("%02X", Integer.parseInt(assists.get(i).value)));
                    stringBuilder.append("01");
                    stringBuilder.append(String.format("%02X", assists.get(i).structList.size()));
                    for (int j = 0; j < assists.get(i).structList.size(); j++) {
                        TranXADRAssist.StructBean structBean = assists.get(i).structList.get(j);
                        String time = structBean.beanItems.get(0).value;

                        stringBuilder.append("0203");
                        stringBuilder.append("0904");
                        if (time.length() == 8) {
                            stringBuilder.append(time);
                        } else if (time.length() == 4) {
                            stringBuilder.append(String.format("%02X", Integer.parseInt(time.substring(0, 2))));
                            stringBuilder.append(String.format("%02X", Integer.parseInt(time.substring(2, 4))));
                            stringBuilder.append("FFFF");
                        } else {
                            break;
                        }

                        stringBuilder.append("090600000A0064FF");
                        stringBuilder.append("12");
                        stringBuilder.append(String.format("%04X", Integer.parseInt(structBean.beanItems.get(2).value)));
                    }
                }
                break;
            case HexDataFormat.FREEZE_CAPTURE:
                TranXADRAssist tranXADRAssist = assists.get(0);
                int itemSize = tranXADRAssist.structList.size();
                String classId, obis, attr;
                String[] temp;
                stringBuilder.append(String.format("%02X", itemSize));
                for (int k = 0; k < itemSize; k++) {
                    stringBuilder.append("0204");
                    temp = tranXADRAssist.structList.get(k).value.split("#");
                    classId = String.format("%04X", Integer.parseInt(temp[0]));
                    attr = String.format("%02X", Integer.parseInt(temp[2]));
                    stringBuilder.append("12");
                    stringBuilder.append(classId);
                    obis = temp[1].replace(":", ".").replace("-", ".").replace(" ", "");
                    temp = obis.split("\\.");
                    stringBuilder.append("0906");
                    for (int i = 0; i < temp.length; i++) {
                        stringBuilder.append(String.format("%02X", Integer.parseInt(temp[i], 10)));
                    }
                    stringBuilder.append("0F");
                    stringBuilder.append(attr);
                    stringBuilder.append("120000");
                }
                break;
            case HexDataFormat.DISPLAY_ARRAY:
                size = assists.size();
                int dataType;
                TranXADRAssist assist;
                TranXADRAssist.StructBean structBean;
                stringBuilder.append(String.format("%02X", size));
                if (size > 0) {
                    //判断是否有数组
                    stringBuilder.append("02");
                    stringBuilder.append(String.format("%02X", assists.get(0).structList.size()));
                    for (int i = 0; i < size; i++) {
                        assist = assists.get(i);
                        for (int j = 0; j < assist.structList.size(); j++) {
                            structBean = assist.structList.get(j);
                            dataType = structBean.dataType;
                            switch (dataType) {
                                case HexDataFormat.LONG_UNSIGNED:
                                    stringBuilder.append(String.format("%02X", HexDataFormat.LONG_UNSIGNED));
                                    stringBuilder.append(String.format("%04X", (Long.parseLong(structBean.writeData))));
                                    break;
                                case HexDataFormat.BOOL:
                                    stringBuilder.append(String.format("%02X", HexDataFormat.BOOL));
                                    stringBuilder.append(structBean.writeData);
                                    break;
                                case HexDataFormat.DOUBLE_LONG:
                                    stringBuilder.append(String.format("%02X", HexDataFormat.DOUBLE_LONG));
                                    stringBuilder.append(String.format("%08x", (Integer.parseInt(structBean.writeData))));
                                    break;
                                case HexDataFormat.DOUBLE_LONG_UNSIGNED:
                                    stringBuilder.append(String.format("%02X", HexDataFormat.DOUBLE_LONG_UNSIGNED));
                                    stringBuilder.append(String.format("%08x", (Long.parseLong(structBean.writeData))));
                                    break;
                                case HexDataFormat.VISIBLE_STRING:
                                    //ascs
                                    stringBuilder.append(String.format("%02X", HexDataFormat.VISIBLE_STRING));
                                    if (assist.writeData.length() < 128) {
                                        stringBuilder.append(String.format("%02X", structBean.writeData.length()));
                                    } else {
                                        int len = numLen(assist.writeData.length() / 2);
                                        stringBuilder.append(Integer.toString((0x80 + len), 16));// "09"
                                        stringBuilder.append(String.format("%02X", Integer.parseInt(structBean.writeData)));
                                    }
                                    stringBuilder.append(HexStringUtil.bytesToHexString(structBean.writeData.getBytes()));
                                    break;
                            }
                        }
                    }
                }

                break;
        }
        Log.v(TAG, "待写入数据=" + stringBuilder.toString().toUpperCase());
        return stringBuilder.toString().toUpperCase();
    }

//    /***
//     * 组织数据
//     *
//     * @param EncodeStr write data
//     * @param TypeStr 数据类型
//     * @return 字符串
//     */
//
//    public static String GetXADRCode(String EncodeStr, DataType TypeStr) {
//        String RtnStr = "";
//
//        switch (TypeStr) {
//            case Array_dd://010102020904310000000905FFFFFF00FF
//                RtnStr = "010102020904310000000905FFFFFF" + EncodeStr + "FF";
//                break;
//            case Array_Upgrade:
//                RtnStr = "01020206" + EncodeStr;
//                break;
//            case bool:
//                RtnStr = "03" + EncodeStr;
//                break;
//            case Int32:// 1234->06 80 00 04 d2
//
//                RtnStr = "05"
//                        + String.format("%08x", (Integer.parseInt(EncodeStr)));
//                break;
//            //case unsigned32:// "00120F1108"->"0600120F1108"
//            case U32:
//                // RtnStr = "06" + EncodeStr.PadLeft(10, '0');
//                RtnStr = "06"
//                        + String.format("%08x", (Long.parseLong(EncodeStr)));
//                break;
//            case unsigned32_decimal:// 1234->06 00 00 04 d2
//                RtnStr = "06"
//                        + String.format("%08x", (Long.parseLong(EncodeStr)));
//                break;
//            case BCD:// "0012021108"->"0d040012021108"
//                RtnStr = "0d" + String.format("%02X", EncodeStr.length() / 2)
//                        + EncodeStr;
//                break;
//            case Octs_string:
//                if (EncodeStr.length() / 2 < 128) {
//                    RtnStr = "09" + String.format("%02X", EncodeStr.length() / 2);
//                } else {
//                    RtnStr = "09" + Integer.toString(0x80 + numLen(EncodeStr.length() / 2));
//                    RtnStr = "0981" + String.format("%02X", EncodeStr.length() / 2);
//                }
//                for (int i = 0; i < EncodeStr.length() / 2; i++) {
//                    RtnStr += EncodeStr.substring(i * 2, i * 2 + 2);
//                }
//                break;
//            case octet_string_origal:
//                if (EncodeStr.length() < 128) {
//                    RtnStr = "09" + String.format("%02X", EncodeStr.length());
//                } else {
//                    RtnStr = "0981" + String.format("%02X", EncodeStr.length());
//                }
//                for (int i = 0; i < EncodeStr.length(); i++) {
//                    RtnStr += "0" + EncodeStr.substring(i, i + 1);
//                }
//                break;
//            case Ascs:
//            case Octs_ascii:
//                if (EncodeStr.length() < 128) {
//                    RtnStr = "0A" + String.format("%02X", EncodeStr.length());// "09"
//                } else {
//                    int len = numLen(EncodeStr.length() / 2);
//                    RtnStr = "0A" + Integer.toString((0x80 + len), 16);// "09"
//                    RtnStr = RtnStr + String.format("%02X", Integer.parseInt(EncodeStr));
//                }
//                byte[] TmpBytes = EncodeStr.getBytes();
//                for (int i = 0; i < TmpBytes.length; i++) {
//                    RtnStr += String.format("%02X", TmpBytes[i]);
//                }
//                break;
//            case U8:// LYH
//                RtnStr = "11" + String.format("%02X", Integer.parseInt(EncodeStr));
//                break;
//            case U16:
//                RtnStr = "12" + String.format("%04X", Integer.parseInt(EncodeStr));
//                break;
//            case Int64:
//                RtnStr = "14" + String.format("%16x", Long.parseLong(EncodeStr));
//                break;
//            case float32:
//                RtnStr = "17";
//                byte[] byteArray = new byte[4];
//                byteArray = getBytes(Float.parseFloat(EncodeStr));
//                for (int i = 3; i >= 0; i--) {
//                    RtnStr += String.format("%02X", byteArray[i]);
//                }
//                break;
//            case time:
//                RtnStr = "0904";
//                RtnStr += String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(0, 2)))
//                        + String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(2, 4)));
//                RtnStr += "FFFF";
//                break;
//            case HH_mm_ss:
//                RtnStr = "0904";
//                RtnStr += String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(0, 2)))
//                        + String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(2, 4)))
//                        + String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(4, 6)));
//                RtnStr += "FF";
//                break;
//            case mm_dd:
//                RtnStr = "090CFFFF";
//                RtnStr += String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(0, 2)))
//                        + String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(2, 4)));
//                RtnStr += "FFFFFFFF80000000";
//                break;
//            case yy_mm_dd:
//                int Year = 0xFFFF;
//                String YearHigh = "FF";
//                String YearLow = "FF";
//                if (EncodeStr.length() > 4) {
//                    Year = 2000 + Integer.parseInt(EncodeStr.substring(0, 2));
//                    YearHigh = String.format("%02X", Year / 256);
//                    YearLow = String.format("%02X", Year % 256);
//                    EncodeStr = EncodeStr.substring(2);
//                }
//                RtnStr = "0905" + YearHigh + YearLow;
//                RtnStr += String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(0, 2)))
//                        + String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(2, 4)));
//                RtnStr += "FF";
//                break;
//            case yyyy_mm_dd:
//                int year = 0xFFFF;
//                String yearHigh = "FF";
//                String yearLow = "FF";
//                if (EncodeStr.length() > 4) {
//                    year = Integer.parseInt(EncodeStr.substring(0, 4));
//                    yearHigh = String.format("%02X", year / 256);
//                    yearLow = String.format("%02X", year % 256);
//                    EncodeStr = EncodeStr.substring(4);
//                }
//                RtnStr = "0905" + yearHigh + yearLow;
//                RtnStr += String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(0, 2)))
//                        + String.format("%02X",
//                        Integer.parseInt(EncodeStr.substring(2, 4)));
//                RtnStr += "FF";
//                break;
//            case Octs_datetime:
//                Date dt = new Date();
//
//                SimpleDateFormat dateFormat2 = new SimpleDateFormat(
//                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                try {
//                    dt = dateFormat2.parse(EncodeStr);
//                } catch (ParseException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//                String WeekStr = getWeekOfDate(dt);
//                RtnStr = "090C";
//
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(dt);
//                int year1 = cal.get(Calendar.YEAR);// 获取年份
//                int month = cal.get(Calendar.MONTH) + 1;// 获取月份
//                int day = cal.get(Calendar.DATE);// 获取日
//                int hour = cal.get(Calendar.HOUR_OF_DAY);// 小时
//                int minute = cal.get(Calendar.MINUTE);// 分
//                int second = cal.get(Calendar.SECOND);// 秒
//
//                RtnStr += String.format("%04X", year1);
//                RtnStr += String.format("%02X", month);
//                RtnStr += String.format("%02X", day);
//                RtnStr += WeekStr;
//                RtnStr += String.format("%02X", hour);
//                RtnStr += String.format("%02X", minute);
//                RtnStr += String.format("%02X", second);
//                RtnStr += "FF";
//                RtnStr += "01E0";
//                RtnStr += "00";
//                break;
//            // case "clock_manual":
//            // break;
//            default:
//                RtnStr = EncodeStr;
//                break;
//        }
//        return RtnStr;
//    }

//    public static byte[] getBytes(float data) {
//        int intBits = Float.floatToIntBits(data);
//        return getBytes(intBits);
//    }

    /**
     * 解析obis 格式
     *
     * @param strOBIS String
     * @return string 16进制 字符串
     */
    public static String fnChangeOBIS(String strOBIS) {
        String strData = "";
        try {
            strOBIS = strOBIS.replaceAll(":", ".").replaceAll("-", ".");
            String[] str = strOBIS.split("#");
            if (str.length == 3) {
                StringBuilder strB = new StringBuilder();

                strB.append(String.format("%04X", Integer.parseInt(str[0], 10)));
                String[] strMain = str[1].split("\\.");
                for (int i = 0; i < strMain.length; i++) {
                    strB.append(String.format("%02X", Integer.parseInt(strMain[i], 10)));
                }
                strB.append(String.format("%02X", Integer.parseInt(str[2], 10)));
                strB.append("00");
                strData = strB.toString();
            } else {
                strData = strOBIS;
            }

        } catch (Exception ex) {
            strData = strOBIS;
        }
        return strData;
    }

    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"00", "01", "02", "03", "04", "05", "06"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 字节数组转为单精度（符点数） 小端
     *
     * @param bytes 数组
     * @return float
     */
    public static float getFloat(byte[] bytes) {

        return Float.intBitsToFloat(getInt(bytes));
    }

    public static int getInt(byte[] bytes) {
        return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8))
                | (0xff0000 & (bytes[2] << 16))
                | (0xff000000 & (bytes[3] << 24));
    }

    public static String FormatValue(String StrValue, DateType dataType) {
        switch (dataType) {
            case YYMMDDhhmmssYYMMDDhhmmss:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(12, 14) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(12, 14) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(14, 16) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(16, 18) + "-"
                                + StrValue.substring(14, 16) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(16, 18) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                    case yyyy_MM_dd:
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 14) + "-"
                                + StrValue.substring(14, 16) + "-"
                                + StrValue.substring(16, 18) + " "
                                + StrValue.substring(18, 20) + ":"
                                + StrValue.substring(20, 22) + ":"
                                + StrValue.substring(22, 24);
                        break;
                }
                return StrValue;
            case YYMMDDhhmmss:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    case yyyy_MM_dd:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12);
                        break;
                }
                return StrValue;
            case YYMMDDhhmm:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    case yyyy_MM_dd:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                }
                return StrValue;
            case YYYYMMDD:
                StrValue = StrValue.substring(0, 4) + "-"
                        + StrValue.substring(4, 6) + "-" + StrValue.substring(6, 8);
                return StrValue;
            case YYYYMMDDhhmmss:
                StrValue = StrValue.substring(0, 4) + "-"
                        + StrValue.substring(4, 6) + "-" + StrValue.substring(6, 8)
                        + " " + StrValue.substring(8, 10) + ":"
                        + StrValue.substring(10, 12) + ":"
                        + StrValue.substring(12, 14);
                return StrValue;
            case YYYYMMDDhhmm:
                StrValue = StrValue.substring(0, 4) + "-"
                        + StrValue.substring(4, 6) + "-" + StrValue.substring(6, 8)
                        + " " + StrValue.substring(8, 10) + ":"
                        + StrValue.substring(10, 12);
                return StrValue;
            case MMDDhhmm:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(0, 2) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(0, 2) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(2, 4) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(2, 4) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(4, 6) + ":"
                                + StrValue.substring(6, 8);
                        break;
                    case yyyy_MM_dd:
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10);
                        break;
                }
                StrValue = StrValue.substring(0, 2) + "-"
                        + StrValue.substring(2, 4) + " " + StrValue.substring(4, 6)
                        + ":" + StrValue.substring(6, 8);
                return StrValue;
            case HHmmss:
                StrValue = StrValue.substring(0, 2) + ":"
                        + StrValue.substring(2, 4) + ":" + StrValue.substring(4, 6);
                return StrValue;
            case YYMMDDhhmmssNNNN:
                switch (dtFormat) {
                    case dd_MM_yyyy:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case MM_dd_yyyy:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case dd_yyyy_MM:
                        StrValue = StrValue.substring(4, 6) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case yyyy_dd_MM:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + "-"
                                + StrValue.substring(2, 4) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case MM_yyyy_dd:
                        StrValue = StrValue.substring(2, 4) + "-"
                                + StrValue.substring(0, 2) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                    case yyyy_MM_dd:
                    default:
                        StrValue = StrValue.substring(0, 2) + "-"
                                + StrValue.substring(2, 4) + "-"
                                + StrValue.substring(4, 6) + " "
                                + StrValue.substring(6, 8) + ":"
                                + StrValue.substring(8, 10) + ":"
                                + StrValue.substring(10, 12) + "&"
                                + StrValue.substring(12, 16);
                        break;
                }
                return StrValue;
            case NNNNNNNN:
            case NNNN:
                return StrValue;
            default:
                return StrValue;
        }
    }

    /**
     * 大数据 转换 int 有符合
     *
     * @param value    16进制数据
     * @param dataType 数据类型
     * @return int
     */
    public static long parseInteger(String value, int dataType) {
        try {

            return parseInteger(new BigInteger(value, 16), dataType);

        } catch (Exception ex) {
            Log.d("api=parseInteger", ex.getMessage());
        }
        return -1;
    }

    /**
     * 转换 Integer
     *
     * @param integer  BigInteger
     * @param dataType 数据类型
     * @return Integer
     */
    public static long parseInteger(BigInteger integer, int dataType) {

        //4字节 有 负数
        if (dataType == HexDataFormat.DOUBLE_LONG) {
            BigInteger c = new BigInteger("100000000", 16);
            int data = integer.compareTo(c);
            if (data > 0) {
                return data;
            }
        }
        //有负数
        else if (dataType == HexDataFormat.LONG64) {
            BigInteger c = new BigInteger("10000000000000000", 16);
            int data = integer.compareTo(c);
            if (data > 0) {
                return data;
            }
        } else if (dataType == HexDataFormat.DOUBLE_LONG_UNSIGNED) {
            return integer.longValue();
        } else if (dataType == HexDataFormat.LONG64_UNSIGNED) {
            BigInteger c = new BigInteger("10000000000000000", 16);
            return c.longValue();
        }
        return integer.intValue();
    }
}
