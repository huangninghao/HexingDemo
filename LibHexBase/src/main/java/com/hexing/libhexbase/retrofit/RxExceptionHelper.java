package com.hexing.libhexbase.retrofit;

//import android.net.ParseException;
//
//import com.google.gson.JsonParseException;
//import com.hexing.libhexbase.log.HexLog;
//
//import org.json.JSONException;
//
//import java.net.ConnectException;
//import java.net.SocketTimeoutException;
//import java.net.UnknownHostException;

/*
 * 文件名:    long
 * 创建者:    ZJB
 * 创建时间:  2017/5/19 on 20:45
 * 描述:     TODO 异常抛出帮助类
 */
public class RxExceptionHelper {
//    public static String handleException(Throwable e) {
//        e.printStackTrace();
//        String error;
//        if (e instanceof SocketTimeoutException) {//网络超时
//            HexLog.e("TAG", "网络连接异常: " + e.getMessage());
//            error = "网络连接异常";
//        } else if (e instanceof ConnectException) { //均视为网络错误
//            HexLog.e("TAG", "网络连接异常: " + e.getMessage());
//
//            error = "网络连接异常";
//        } else if (e instanceof JsonParseException
//                || e instanceof JSONException
//                || e instanceof ParseException) {   //均视为解析错误
//            HexLog.e("TAG", "数据解析异常: " + e.getMessage());
//            error = "数据解析异常";
//        } else if (e instanceof RxApiException) {//服务器返回的错误信息
//            error = e.getCause().getMessage();
//        } else if (e instanceof UnknownHostException) {
//            HexLog.e("TAG", "网络连接异常: " + e.getMessage());
//            error = "网络连接异常";
//        } else if (e instanceof IllegalArgumentException) {
//            HexLog.e("TAG", "下载文件已存在: " + e.getMessage());
//            error = "下载文件已存在";
//        } else {//未知错误
//            try {
//                HexLog.e("TAG", "错误: " + e.getMessage());
//            } catch (Exception e1) {
//                HexLog.e("TAG", "未知错误Debug调试 ");
//            }
//            error = "错误";
//        }
//        return error;
//    }
}