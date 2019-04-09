package com.hexing.libhexbase.tools;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 *
 * @author wanglei 2015年8月1日 上午7:03:28
 */
public class TimeUtil {
    private final static String TAG = TimeUtil.class.getSimpleName();
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * yyyy-MM-dd HH:mm
     */
    public static String DEFAULT_HOUR_MINUTE_FORMAT = "yyyy-MM-dd HH:mm";
    /**
     * MM月dd日 HH:mm
     */
    public static String HOUR_MINUTE_FORMAT = "MM月dd日 HH:mm";
    /**
     * HH:mm
     */
    public static String HOUR = "HH:mm";
    /**
     * yyyy-MM-dd
     */
    public static String DEFAULT_DAY_FORMAT = "yyyy-MM-dd";
    /**
     * yyyy-MM
     */
    public static String DEFAULT_MONTH_FORMAT = "yyyy-MM";
    /**
     * yyyyMMddHHmmss
     */
    public static String DEFAULT_DATE_NO_SEPRATOR_FORMAT = "yyyyMMddHHmmss";
    /**
     * yyyyMMdd
     */
    public static final String DEFAULT_DAY_NO_SEPRATOR_FORMAT = "yyyyMMdd";
    /**
     * yyyyMMdd
     */
    public static final String DEFAULT_DAY_NO_SEPRATOR = "yyyy.MM.dd";
    /**
     * dd/MM/yyyy
     */
    public static final String DEFAULT_SLASH_FORMAT = "dd/MM/yyyy";
    /**
     * dd/MM/yyyy
     */
    public static final String DEFAULT_FORMAT = "yyyy/MM/dd/";

    public static final String DEFAULT_HX = "yyyy/MM/dd HH:mm:ss";

    /**
     * 指定日期格式，转化时间字符串为Date对象
     *
     * @param pattern
     * @param dateString
     * @return
     */
    public static Date parseDate(String pattern, String dateString) {
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            return df.parse(dateString);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * 指定日期格式，转化时间字符串为Date对象
     *
     * @return Date 对象
     */
    public static Date parseDate(String dateString) {
        return parseDate(DEFAULT_DATE_FORMAT, dateString);
    }

    /**
     * 指定日期格式，转化Date对象为时间字符串
     *
     * @param pattern 日期格式
     * @param date    Date
     * @return string
     */
    public static String parseString(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 指定日期格式，转化Date对象为时间字符串
     *
     * @param date Date
     * @return 字符串
     */
    public static String parseString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }


    /**
     * 时间格式转换
     *
     * @param dateStr 日期参数
     * @return 字符串 默认格式
     */
    public static String parseString(String dateStr) {
        Date date = parseDate(dateStr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Date转化为时间戳
     *
     * @return long
     */
    public static Long dateToLong(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 时间戳转换成字符窜
     */
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(HOUR_MINUTE_FORMAT, Locale.getDefault());
        return sf.format(d);
    }

    /* 时间戳转换成字符窜 */
    public static String getDateToStringss(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault());
        return sf.format(d);
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param time    long
     * @param pattern 时间格式
     * @return 字符串
     */
    public static String getDateToString(long time, String pattern) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sf.format(d);
    }

    /**
     * 时间戳转换成字符窜
     *
     * @param dateTime String
     * @param pattern  时间格式
     * @return 字符串
     */
    public static String getTimeFormat(String dateTime, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            Date d1 = format.parse(dateTime);
            return format.format(d1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    /**
     * 时间戳转换成字符窜
     */
    public static String getDateToStringOrder(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(DEFAULT_HOUR_MINUTE_FORMAT, Locale.getDefault());
        return sf.format(d);
    }

    /**
     * 时间戳转换成字符窜
     */
    public static String getDateToStringday(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(DEFAULT_DAY_NO_SEPRATOR, Locale.getDefault());
        return sf.format(d);
    }

    /**
     * 时间戳转换成字符窜
     */
    public static String getDateToStringHour(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat(HOUR, Locale.getDefault());
        return sf.format(d);
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前时间
     *
     * @return string
     */
    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return sdf.format(curDate);
    }

    /**
     * 获取当前时间
     *
     * @param format 时间格式
     * @return string
     */
    public static String getNowTime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return sdf.format(curDate);
    }

    /**
     * 获取当前时间
     *
     * @return long
     */
    public static long getNowLongTime() {
        return new Date(System.currentTimeMillis()).getTime();
    }

    /**
     * 获取时间差
     *
     * @param startTime 开始时间
     * @param endTime   比较的第二个时间
     * @return long
     */
    public static long getTimeTimestamp(String startTime, String endTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault());

            Date d1 = format.parse(startTime);
            Date d2 = format.parse(endTime);
            return d2.getTime() - d1.getTime();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return 0;
    }

    /**
     * 根据当前选择的年月判断最大天数
     */
    public static int getMaxDayOfMonth(int cYear, int cMonth) {
        Calendar cld = Calendar.getInstance();
        cld.set(Calendar.YEAR, cYear);
        cld.set(Calendar.MONTH, cMonth);
        return cld.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    /**
     * 获取本月最后一天
     */
    public static String getLastDayInMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DAY_FORMAT, Locale.getDefault());
        return format.format(calendar.getTime());
    }

    /**
     * 获取时间戳 当前时间 到月底的 差值
     *
     * @return long
     */
    public static long getLastTimeInMonth() {
        String last = getLastDayInMonth() + " 00:00:00";
        return date2TimeStamp(last, DEFAULT_DATE_FORMAT) * 1000 - System.currentTimeMillis();
    }

    /**
     * 时间加减小时
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param hours     加减的小时
     * @return Date
     */
    public static Date dateAddHours(Date startDate, int hours) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.HOUR, c.get(Calendar.HOUR) + hours);
        return c.getTime();
    }

    /**
     * 时间加减分钟
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param minutes   加减的分钟
     * @return
     */
    public static Date dateAddMinutes(Date startDate, int minutes) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + minutes);
        return c.getTime();
    }

    /**
     * 时间加减秒数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param seconds   加减的秒数
     * @return Date
     */
    public static Date dateAddSeconds(Date startDate, int seconds) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.SECOND, c.get(Calendar.SECOND) + seconds);
        return c.getTime();
    }

    /**
     * 计算日期相差 天数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param format 时间格式
     * @return long
     */
    public static long dateDiff(String startTime, String endTime, String format) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sd = new SimpleDateFormat(format, Locale.getDefault());
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
//        long nh = 1000 * 60 * 60;// 一小时的毫秒数
//        long nm = 1000 * 60;// 一分钟的毫秒数
//        long ns = 1000;// 一秒钟的毫秒数
        long diff;
        long day = 0;
        try {
            // 获得两个时间的毫秒时间差异
            diff = sd.parse(endTime).getTime()
                    - sd.parse(startTime).getTime();
            day = diff / nd;// 计算差多少天
            //long hour = diff % nd / nh;// 计算差多少小时
            //long min = diff % nd % nh / nm;// 计算差多少分钟
           // long sec = diff % nd % nh % nm / ns;// 计算差多少秒
            // 输出结果
//            System.out.println("时间相差：" + day + "天" + hour + "小时" + min
//                    + "分钟" + sec + "秒。");
            if (day >= 1) {
                return day;
            } else {
                if (day == 0) {
                    return 1;
                } else {
                    return 0;
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

    /**
     * 计算日期 月份相差
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return int
     */
    public static int dateDiffMonth(String startTime, String endTime) {
        // 按照传入的格式生成一个simpledateformate对象
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_MONTH_FORMAT, Locale.getDefault());
        Calendar bef = Calendar.getInstance();
        Calendar aft = Calendar.getInstance();
        try {
            bef.setTime(sdf.parse(startTime));
            aft.setTime(sdf.parse(endTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
        int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
        return Math.abs(month + result);
    }

    /**
     * 时间加减天数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param days      加减的天数
     * @return Date
     */
    public static Date dateAddDays(Date startDate, int days) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.DATE, c.get(Calendar.DATE) + days);
        return c.getTime();
    }

    /**
     * 时间加减月数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param months    加减的月数
     * @return Date
     */
    public static Date dateAddMonths(Date startDate, int months) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + months);
        return c.getTime();
    }

    /**
     * 时间加减年数
     *
     * @param startDate 要处理的时间，Null则为当前时间
     * @param years     加减的年数
     * @return Date
     */
    public static Date dateAddYears(Date startDate, int years) {
        if (startDate == null) {
            startDate = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) + years);
        return c.getTime();
    }
}
