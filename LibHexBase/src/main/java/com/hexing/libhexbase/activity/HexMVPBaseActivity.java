package com.hexing.libhexbase.activity;

import android.os.Bundle;

/**
 * @author caibinglong
 *         date 2018/4/24.
 *         desc desc
 */

public abstract class HexMVPBaseActivity<P extends BasePresenter> extends HexBaseActivity {
    protected P mvpPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mvpPresenter = createPresenter();
        super.onCreate(savedInstanceState);
    }

    protected abstract P createPresenter();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mvpPresenter != null) {
            mvpPresenter.detachView();
            mvpPresenter = null;
        }
    }
}
