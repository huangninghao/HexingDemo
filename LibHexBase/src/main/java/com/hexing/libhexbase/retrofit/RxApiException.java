package com.hexing.libhexbase.retrofit;


/*
 * 文件名:    RxApiException
 * 创建者:    long
 * 创建时间:  2017/5/19 on 20:45
 * 描述:     TODO 自定义exception 用于访问是是
 */
public class RxApiException extends RuntimeException {
    private int code;

    public RxApiException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }

    public RxApiException(String message) {
        super(new Throwable(message));
    }
}