package com.hexing.libhexbase.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by HEC271
 * on 2017/12/1.
 *
 * @author HEC271
 *         线程池
 */

public class HexThreadManager {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;

    static ExecutorService cachedThreadPool;

    static ExecutorService fixedThreadPool;

    static ScheduledExecutorService scheduledThreadPool;

    static ExecutorService singleThreadPool;

    //通过ThreadPoolExecutor的代理类来对线程池的管理
    private static ThreadPollProxy mThreadPollProxy;

    //单列对象
    public static ThreadPollProxy getThreadPollProxy() {
        synchronized (ThreadPollProxy.class) {
            if (mThreadPollProxy == null) {
                mThreadPollProxy = new ThreadPollProxy(CORE_POOL_SIZE, CORE_POOL_SIZE * 2, KEEP_ALIVE_TIME);
            }
        }
        return mThreadPollProxy;
    }

    //通过ThreadPoolExecutor的代理类来对线程池的管理
    public static class ThreadPollProxy {
        private ThreadPoolExecutor poolExecutor;//线程池执行者 ，java内部通过该api实现对线程池管理
        private int corePoolSize;
        private int maximumPoolSize;
        private long keepAliveTime;

        public ThreadPollProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
        }

        //对外提供一个执行任务的方法
        public void execute(Runnable r) {
            if (poolExecutor == null || poolExecutor.isShutdown()) {
                poolExecutor = new ThreadPoolExecutor(
                        //核心线程数量
                        corePoolSize,
                        //最大线程数量
                        maximumPoolSize,
                        //当线程空闲时，保持活跃的时间
                        keepAliveTime,
                        //时间单元 ，秒级
                        TimeUnit.SECONDS,
                        //线程任务队列
                        new LinkedBlockingQueue<Runnable>(),
                        //创建线程的工厂
                        Executors.defaultThreadFactory());
            }
            poolExecutor.execute(r);
        }
    }

    /**
     * 创建一个可缓存的线程池。如果线程池的大小超过了处理任务所需要的线程，
     * 那么就会回收部分空闲（60秒不执行任务）的线程，当任务数增加时，
     * 此线程池又可以智能的添加新线程来处理任务。
     * 此线程池不会对线程池大小做限制，
     * 线程池大小完全依赖于操作系统（或者说JVM）能够创建的最大线程大小
     *
     * @param runnable run
     * @return ExecutorService
     */
    public static ExecutorService cachedThreadPoolRun(Runnable runnable) {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
        cachedThreadPool.submit(runnable);
        return cachedThreadPool;
    }

    /**
     * 创建固定大小的线程池。每次提交一个任务就创建一个线程，
     * 直到线程达到线程池的最大大小。
     * 线程池的大小一旦达到最大值就会保持不变，
     * 如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。
     *
     * @param runnable run
     * @return ExecutorService
     */
    public static ExecutorService fixedThreadPoolRun(Runnable runnable) {
        if (fixedThreadPool == null) {
            fixedThreadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        }
        fixedThreadPool.submit(runnable);
        return fixedThreadPool;
    }

    /**
     * 创建一个大小无限的线程池。此线程池支持定时以及周期性执行任务的需求。
     *
     * @param runnable run
     * @param delay    延迟时间
     * @param unit     单位
     * @return ExecutorService
     */
    public static ExecutorService scheduledThreadPoolRun(Runnable runnable, long delay, TimeUnit unit) {
        if (scheduledThreadPool == null) {
            scheduledThreadPool = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
        }
        scheduledThreadPool.schedule(runnable, delay, unit);
        return scheduledThreadPool;
    }

    /**
     * 定时器
     * @param runnable run
     * @param delay    延迟时间
     * @param period 间隔
     * @param unit     单位
     * @return ExecutorService
     */
    public static ExecutorService scheduledRateThreadPoolRun(Runnable runnable, long delay, long period, TimeUnit unit) {
        if (scheduledThreadPool == null) {
            scheduledThreadPool = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
        }
        scheduledThreadPool.scheduleAtFixedRate(runnable, delay, period, unit);
        return scheduledThreadPool;
    }

    /**
     * 创建一个单线程的线程池。这个线程池只有一个线程在工作
     *
     * @param runnable run
     * @return ExecutorService
     */
    public static ExecutorService singleThreadPoolRun(Runnable runnable) {
        if (singleThreadPool == null) {
            singleThreadPool = Executors.newSingleThreadExecutor();
        }
        singleThreadPool.submit(runnable);
        return singleThreadPool;
    }

    //在UI中执行
    static Handler handler;

    /**
     * UI 中执行
     *
     * @param runnable run
     */
    public static void runTaskOnMainThread(Runnable runnable) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.post(runnable);
    }

}
