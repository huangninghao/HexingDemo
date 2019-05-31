package cn.hexing.iec21.services;

import android.text.TextUtils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.hexing.HexStringUtil;
import cn.hexing.dlms.HexDataFormat;
import cn.hexing.model.TranXADRAssist;


public class AnalysisService {

    private final static String TAG = AnalysisService.class.getSimpleName();

    /***
     * 解析函数
     *
     * @param XADRcodeStr byte[]
     * @param assist TranXADRAssist
     * @return String
     */
    public static String tranXADRCode(byte[] XADRcodeStr, TranXADRAssist assist) {
        StringBuilder rtnStr = new StringBuilder();
        int index;
        try {
            for (int i = 0; i < XADRcodeStr.length; i++) {
                rtnStr.append(HexStringUtil.convertHexToString(String.format("%02x", (XADRcodeStr[i] & 0xff))));
            }
            if (!TextUtils.isEmpty(assist.format) && rtnStr.length() >= assist.format.length() - 1) {
                rtnStr = new StringBuilder(rtnStr.substring(rtnStr.length() - assist.format.length() + 1));
                index = assist.format.indexOf(".");
                if (index > 0) {
                    rtnStr = rtnStr.insert(index, ".");
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return rtnStr.toString();
    }

    public static String GetXADRCode(TranXADRAssist assist) {
        StringBuilder stringBuilder = new StringBuilder();
        if (assist.dataType == HexDataFormat.DATE_TIME) {
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
            stringBuilder.append(String.valueOf(year).substring(2));
            stringBuilder.append(HexStringUtil.padRight(String.valueOf(month),2,'0'));
            stringBuilder.append(HexStringUtil.padRight(String.valueOf(day),2,'0'));
            stringBuilder.append(getWeekOfDate(dt));
            stringBuilder.append(HexStringUtil.padRight(String.valueOf(hour),2,'0'));
            stringBuilder.append(HexStringUtil.padRight(String.valueOf(minute),2,'0'));
            stringBuilder.append(HexStringUtil.padRight(String.valueOf(second),2,'0'));
        }
        else{stringBuilder.append(assist.writeData);}

        return stringBuilder.toString().toUpperCase();
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


}
