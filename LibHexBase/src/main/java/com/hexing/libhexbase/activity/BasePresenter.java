package com.hexing.libhexbase.activity;

import android.support.annotation.Nullable;

import com.hexing.libhexbase.log.HexLog;

import java.lang.ref.WeakReference;

/**
 * @author caibinglong
 *         date 2018/4/24.
 *         desc desc
 */

public class BasePresenter<V> {

    /**
     * 当内存不足释放内存
     */
    protected WeakReference<V> mvpView; // view 的弱引用

    /**
     * bind p with v
     *
     * @param view view
     */
    public void attachView(V view) {
        mvpView = new WeakReference<>(view);
    }

    /**
     * 释放
     */
    public void detachView() {
        if (mvpView != null) {
            mvpView.clear();
            mvpView = null;
            HexLog.i("RxBasePresenter", "已经GC...");
        }
    }

    /**
     * 是否已经存在
     * @return bool
     */
    public boolean isViewAttached() {
        return mvpView != null && mvpView.get() != null;
    }

    /**
     * 获取view的方法
     *
     * @return 当前关联的view
     */
    @Nullable
    public V getView() {
        if (mvpView != null) {
            return mvpView.get();
        }
        return null;
    }
}
