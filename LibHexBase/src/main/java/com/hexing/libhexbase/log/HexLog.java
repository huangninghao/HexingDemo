package com.hexing.libhexbase.log;


import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This is a Log tool，with this you can the following
 * <ol>
 * <li>use HexLog.d(),you could print whether the method execute,and the default tag is current class's name</li>
 * <li>use HexLog.d(msg),you could print log as before,and you could location the method with a click in Android Studio Logcat</li>
 * <li>use HexLog.json(),you could print json string with well format automatic</li>
 * </ol>
 * 15/11/17 扩展功能，添加对文件的支持
 * 15/11/18 扩展功能，增加对XML的支持，修复BUG
 * 15/12/8  扩展功能，添加对任意参数的支持
 * 15/12/11 扩展功能，增加对无限长字符串支持
 * 16/6/13  扩展功能，添加对自定义全局Tag的支持,修复内部类不能点击跳转的BUG
 * 16/6/15  扩展功能，添加不能关闭的KLog.debug(),用于发布版本的Log打印,优化部分代码
 * 16/6/20  扩展功能，添加堆栈跟踪功能KLog.trace()
 */
public final class HexLog {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String NULL_TIPS = "Log with null object";

    private static final String DEFAULT_MESSAGE = "execute";
    private static final String PARAM = "Param";
    private static final String NULL = "null";
    private static final String TAG_DEFAULT = "HexLog";
    private static final String SUFFIX = ".java";

    private static final String LOGFILEName = ".txt";// 本类输出的日志文件名称
    private static final SimpleDateFormat LogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());// 日志的输出格式
    private static final SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());// 日志文件格式
    //默认存储 日志文件在sdcard中的路径

    private static final String LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static String LOG_PATH_SDCARD_DIR = LOG_PATH + "HexLog";

    // sd卡中日志文件的最多保存天数
    private static final int SDCARD_LOG_FILE_SAVE_DAYS = 7;

    public static final int JSON_INDENT = 4;

    public static final int V = 0x1;
    public static final int D = 0x2;
    public static final int I = 0x3;
    public static final int W = 0x4;
    public static final int E = 0x5;
    public static final int A = 0x6;

    private static final int JSON = 0x7;
    private static final int XML = 0x8;

    public static final int STACK_TRACE_INDEX_5 = 5 + 1;
    private static final int STACK_TRACE_INDEX_4 = 4;

    private static String mGlobalTag;
    private static boolean mIsGlobalTagEmpty = true;
    private static boolean IS_SHOW_LOG = true;

    public static void init(boolean isShowLog) {
        IS_SHOW_LOG = isShowLog;
    }

    public static void init(boolean isShowLog, @Nullable String tag) {
        IS_SHOW_LOG = isShowLog;
        mGlobalTag = tag;
        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag);
    }

    public static void v() {
        printLog(V, null, DEFAULT_MESSAGE);
    }

    public static void v(Object msg) {
        printLog(V, null, msg);
    }

    public static void v(String tag, Object... objects) {
        printLog(V, tag, objects);
    }

    public static void d() {
        printLog(D, null, DEFAULT_MESSAGE);
    }

    public static void d(Object msg) {
        printLog(D, null, msg);
    }

    public static void d(String tag, Object... objects) {
        printLog(D, tag, objects);
    }

    public static void d(int stackTraceIndex, String tag, Object... objects) {
        printLog(stackTraceIndex, D, tag, objects);
    }

    public static void i() {
        printLog(I, null, DEFAULT_MESSAGE);
    }

    public static void i(Object msg) {
        printLog(I, null, msg);
    }

    public static void i(String tag, Object... objects) {
        printLog(I, tag, objects);
    }

    public static void w() {
        printLog(W, null, DEFAULT_MESSAGE);
    }

    public static void w(Object msg) {
        printLog(W, null, msg);
    }

    public static void w(String tag, Object... objects) {
        printLog(W, tag, objects);
    }

    public static void e() {
        printLog(E, null, DEFAULT_MESSAGE);
    }

    public static void e(Object msg) {
        printLog(E, null, msg);
    }

    public static void e(String tag, Object... objects) {
        printLog(E, tag, objects);
    }

    public static void a() {
        printLog(A, null, DEFAULT_MESSAGE);
    }

    public static void a(Object msg) {
        printLog(A, null, msg);
    }

    public static void a(String tag, Object... objects) {
        printLog(A, tag, objects);
    }

    public static void json(String jsonFormat) {
        printLog(JSON, null, jsonFormat);
    }

    public static void json(String tag, String jsonFormat) {
        printLog(JSON, tag, jsonFormat);
    }

    public static void xml(String xml) {
        printLog(XML, null, xml);
    }

    public static void xml(String tag, String xml) {
        printLog(XML, tag, xml);
    }

    public static void file(File targetDirectory, Object msg) {
        printFile(null, targetDirectory, null, msg);
    }

    public static void file(String tag, File targetDirectory, Object msg) {
        printFile(tag, targetDirectory, null, msg);
    }

    public static void file(String tag, File targetDirectory, String fileName, Object msg) {
        printFile(tag, targetDirectory, fileName, msg);
    }

    public static void debug() {
        printDebug(null, DEFAULT_MESSAGE);
    }

    public static void debug(Object msg) {
        printDebug(null, msg);
    }

    public static void debug(String tag, Object... objects) {
        printDebug(tag, objects);
    }

    public static void trace() {
        printStackTrace();
    }

    private static void printStackTrace() {

        if (!IS_SHOW_LOG) {
            return;
        }

        Throwable tr = new Throwable();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        String message = sw.toString();

        String traceString[] = message.split("\\n\\t");
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (String trace : traceString) {
            if (trace.contains("at com.socks.library.HexLog")) {
                continue;
            }
            sb.append(trace).append("\n");
        }
        String[] contents = wrapperContent(STACK_TRACE_INDEX_4, null, sb.toString());
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        HexBaseLog.printDefault(D, tag, headString + msg);
    }

    private static void printLog(int type, String tagStr, Object... objects) {
        printLog(STACK_TRACE_INDEX_5, type, tagStr, objects);

    }

    private static void printLog(int stackTraceIndex, int type, String tagStr, Object... objects) {

        if (!IS_SHOW_LOG) {
            return;
        }

        String[] contents = wrapperContent(stackTraceIndex, tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];

        switch (type) {
            case V:
            case D:
            case I:
            case W:
            case E:
            case A:
                HexBaseLog.printDefault(type, tag, headString + msg);
                break;
            case JSON:
                HexJsonLog.printJson(tag, msg, headString);
                break;
            case XML:
                HexXmlLog.printXml(tag, msg, headString);
                break;
        }
    }

    private static void printDebug(String tagStr, Object... objects) {
        String[] contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        HexBaseLog.printDefault(D, tag, headString + msg);
    }


    private static void printFile(String tagStr, File targetDirectory, String fileName, Object objectMsg) {

        if (!IS_SHOW_LOG) {
            return;
        }

        String[] contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objectMsg);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];

        HexFileLog.printFile(tag, targetDirectory, fileName, headString, msg);
    }

    private static String[] wrapperContent(int stackTraceIndex, String tagStr, Object... objects) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement targetElement = stackTrace[stackTraceIndex];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }

        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }

        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();

        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String tag = (tagStr == null ? className : tagStr);

        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT;
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag;
        }

        String msg = (objects == null) ? NULL_TIPS : getObjectsString(objects);
        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] ";

        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {

        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(NULL).append("\n");
                } else {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? NULL : object.toString();
        }
    }

    /**
     * 日志文件在sdcard中的路径
     *
     * @param path 路径
     */
    public static void setLogPathSdcardDir(String path) {
        if (!TextUtils.isEmpty(path)) {
            LOG_PATH_SDCARD_DIR = LOG_PATH + path;
        }
    }

    /**
     * 写 日志文件
     *
     * @param log 日志内容
     * @return bool
     */
    public static boolean writeFile(String log) {
        return writeFile(LOG_PATH_SDCARD_DIR, "", log);
    }

    /**
     * 写 日志文件
     *
     * @param tag 标志
     * @param log 日志内容
     * @return bool
     */
    public static boolean writeFile(String tag, String log) {
        return writeFile(LOG_PATH_SDCARD_DIR, tag, log);
    }

    /**
     * 打开日志文件并写入日志
     **/
    public static boolean writeFile(String sdcardPath, String tag, String text) {// 新建或打开日志文件
        Date nowTime = new Date();
        String needWriteFile = logfile.format(nowTime);
        String needWriteMessage = LogSdf.format(nowTime) + " " + tag + " " + text;
        FileWriter filerWriter = null;
        try {
            File dirFile = new File(sdcardPath);
            if (!dirFile.exists()) {
                boolean isSuccess = dirFile.mkdirs();
                System.out.print("文件不存在，创建日志文件" + isSuccess);
                if (!isSuccess) {
                    return false;
                }
            }
            File file = new File(sdcardPath, needWriteFile + LOGFILEName);
            filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (filerWriter != null) {
                try {
                    filerWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }

        return true;
    }

    /**
     * 删除制定的日志文件
     */
    public static boolean delFile() {// 删除日志文件
        String needDelFile = logfile.format(getDateBefore());
        File file = new File(LOG_PATH_SDCARD_DIR, needDelFile + LOGFILEName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getDateBefore() {
        Date nowTime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowTime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }

}
