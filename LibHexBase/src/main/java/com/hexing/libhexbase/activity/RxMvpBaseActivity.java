package com.hexing.libhexbase.activity;

import android.os.Bundle;

import com.hexing.libhexbase.inter.RxBasePresenter;


/*
 * 项目名:    BaseLib

 * 文件名:    MvpBaseActivity
 * 创建者:    long
 * 创建时间:  2017/9/7 on 14:17
 * 描述:     TODO 基类Activity
 */
public abstract class RxMvpBaseActivity<P extends RxBasePresenter> extends HexBaseActivity {

    protected P mvpPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mvpPresenter = createPresenter();
    }

    @Override
    protected void onDestroy() {
        if (mvpPresenter != null) {
            mvpPresenter.detach();//在presenter中解绑释放view
            mvpPresenter = null;
        }
        super.onDestroy();
    }

    /**
     * 在子类中初始化对应的presenter
     *
     * @return 相应的presenter
     */
    public abstract P createPresenter();

}
