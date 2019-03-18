package com.hexing.libhexbase.inter;

import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;


/*
 * 项目名:    BaseLib
 * 文件名:    RxBasePresenterImpl
 * 创建者:    long
 * 创建时间:  2017/9/7 on 14:17
 * 描述:     TODO
 */
public abstract class RxBasePresenterImpl<V extends HexBaseView> implements RxBasePresenter {

    /**
     * 当内存不足释放内存
     */
    protected WeakReference<V> mvpView; // view 的弱引用

    private V view;

    public RxBasePresenterImpl(V view) {
        this.view = view;
        attach();
    }


    @Override
    public void detach() {
        this.view = null;
        if (mvpView != null) {
            mvpView.clear();
            mvpView = null;
        }
        unDisposable();
    }

    @Override
    public void attach() {
        mvpView = new WeakReference<>(view);
    }

    /**
     * 是否已经存在
     *
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
        if (this.view != null) {
            return this.view;
        }
        return null;
    }

    //将所有正在处理的Subscription都添加到CompositeSubscription中。统一退出的时候注销观察
    //private CompositeDisposable mCompositeDisposable;

    /**
     * 将Disposable添加
     *
     * @param subscription Disposable
     */
//    @Override
//    public void addDisposable(Disposable subscription) {
//        //csb 如果解绑了的话添加 sb 需要新的实例否则绑定时无效的
//        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
//            mCompositeDisposable = new CompositeDisposable();
//        }
//        mCompositeDisposable.add(subscription);
//    }

    /**
     * 在界面退出等需要解绑观察者的情况下调用此方法统一解绑，防止Rx造成的内存泄漏
     */
    @Override
    public void unDisposable() {
//        if (mCompositeDisposable != null) {
//            mCompositeDisposable.dispose();
//        }
    }

}
