package com.hexing.libhexbase.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hexing.libhexbase.activity.BasePresenter;

/**
 * @author caibinglong
 *         date 2018/4/24.
 *         desc desc
 */

public abstract class HexMVPBaseFragment<P extends BasePresenter> extends HexBaseFragment {
    protected P mvpPresenter;
    private boolean isViewCreate = false;//view是否创建
    private boolean isViewVisible = false;//view是否可见
    protected Context context;
    private boolean isFirst = true;//是否第一次加载

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isViewCreate = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mvpPresenter == null) {
            mvpPresenter = createPresenter();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected abstract P createPresenter();

    @Override
    public void onResume() {
        super.onResume();
        if (isViewVisible) {
            visibleToUser();
        }
    }


    /**
     * 懒加载
     * 让用户可见
     * 第一次加载
     */
    protected void firstLoad() {

    }

    /**
     * 懒加载
     * 让用户可见
     */
    protected void visibleToUser() {
        if (isFirst) {
            firstLoad();
            isFirst = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isViewVisible = isVisibleToUser;
        if (isVisibleToUser && isViewCreate) {
            visibleToUser();
        }
    }

    @Override
    public void onDestroy() {
        if (mvpPresenter != null) {
            mvpPresenter.detachView();
        }
        super.onDestroy();
    }
}
