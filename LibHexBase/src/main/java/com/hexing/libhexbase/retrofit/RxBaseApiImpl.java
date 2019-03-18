package com.hexing.libhexbase.retrofit;


//import com.google.gson.GsonBuilder;
//
//import okhttp3.Interceptor;
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Converter;
//import retrofit2.Retrofit;
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.converter.scalars.ScalarsConverterFactory;


/*
 * 项目名:    BaseFrame
 * 文件名:    RxBaseApiImpl
 * 创建者:    long
 * 创建时间:  2017/9/7 on 10:12
 * 描述:     TODO
 */
public class RxBaseApiImpl implements RxBaseApi {
//    private volatile static Retrofit retrofit = null;
//    protected Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
//    protected OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
//
//    public RxBaseApiImpl(String baseUrl) {
//        retrofitBuilder.addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
//                        .setLenient()
//                        .create()
//                ))
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .client(httpBuilder.addInterceptor(getLoggerInterceptor()).build())
//                .baseUrl(baseUrl);
//    }
//
//    /**
//     * 构建retroft
//     *
//     * @return Retrofit对象
//     */
//    @Override
//    public Retrofit getRetrofit() {
//        if (retrofit == null) {
//            //锁定代码块
//            synchronized (RxBaseApiImpl.class) {
//                if (retrofit == null) {
//                    retrofit = retrofitBuilder.build(); //创建retrofit对象
//                }
//            }
//        }
//        return retrofit;
//
//    }
//
//
//    @Override
//    public OkHttpClient.Builder setInterceptor(Interceptor interceptor) {
//        return httpBuilder.addInterceptor(interceptor);
//    }
//
//    @Override
//    public Retrofit.Builder setConverterFactory(Converter.Factory factory) {
//        return retrofitBuilder.addConverterFactory(factory);
//    }
//
//    /**
//     * 日志拦截器
//     * 将你访问的接口信息
//     *
//     * @return 拦截器
//     */
//    public HttpLoggingInterceptor getLoggerInterceptor() {
//        //日志显示级别
//        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.HEADERS;
//        //新建log拦截器
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
//            @Override
//            public void log(String message) {
//                Log.d("ApiUrl", "--->" + message);
//            }
//        });
//        loggingInterceptor.setLevel(level);
//        return loggingInterceptor;
//    }
}
