package com.hexing.libhexbase.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hexing.libhexbase.inter.RxBasePresenter;


/*
 * 项目名:    BaseLib
 * 文件名:    MvpBaseFragment
 * 创建者:    long
 * 创建时间:  2017/9/7 on 14:17
 * 描述:     TODO 基类Fragment
 */
public abstract class RxMvpBaseFragment<P extends RxBasePresenter> extends HexBaseFragment {

    protected P presenter;
    private boolean isViewCreate = false;//view是否创建
    private boolean isViewVisible = false;//view是否可见
    public Context context;
    private boolean isFirst = true;//是否第一次加载

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (presenter == null) {
            presenter = createPresenter();
        }
        isViewCreate = true;
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (presenter == null) {
            presenter = createPresenter();
        }
        isViewCreate = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (presenter != null) {
            presenter.detach();
            presenter = null;
        }
        isViewCreate = false;
        super.onDestroy();
    }

    public abstract P createPresenter();


}
