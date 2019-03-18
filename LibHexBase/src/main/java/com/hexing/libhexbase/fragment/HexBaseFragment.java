package com.hexing.libhexbase.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hexing.libhexbase.activity.HexBaseActivity;
import com.hexing.libhexbase.application.HexApplication;
import com.hexing.libhexbase.handler.CommonDoHandler;
import com.hexing.libhexbase.handler.CommonHandler;
import com.hexing.libhexbase.inter.InterBaseActivity;
import com.hexing.libhexbase.inter.InterBaseView;
import com.hexing.libhexbase.tools.ToastUtils;
import com.hexing.libhexbase.view.InjectedView;
import com.hexing.libhexbase.view.LoadingDialog;

/**
 * Created by caibinglong
 * on 2017/9/25.
 */

public class HexBaseFragment extends Fragment implements View.OnClickListener, CommonDoHandler,
        InterBaseActivity, InterBaseView {
    protected HexBaseActivity mActivity;
    public LayoutInflater mInflater;
    public Bundle mBundle;
    public Fragment mFragment;
    @SuppressWarnings("unchecked")
    protected CommonHandler<HexBaseFragment> fragmentHandler = new CommonHandler(this);

    @Override
    public void doHandler(Message msg) {
        fragmentHandler.handleMessage(msg);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = (HexBaseActivity) getActivity();
        this.mBundle = getArguments();
        this.mFragment = this;
    }

    public HexBaseActivity getBaseActivity() {
        return this.mActivity;
    }

    public CommonHandler<HexBaseFragment> getFragmentHandler() {
        return fragmentHandler;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void showLoading(String msg) {
        LoadingDialog.showSysLoadingDialog(mActivity, msg);
    }

    @Override
    public void showLoading() {
        LoadingDialog.showSysLoadingDialog(mActivity, "");
    }

    @Override
    public void hideLoading() {
        LoadingDialog.cancelLoadingDialog();
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.showToast(HexApplication.getInstance(), msg);
    }

    @Override
    public void showToast(int resId) {
        ToastUtils.showToast(HexApplication.getInstance(), resId);
    }

    /**
     * 查找View
     *
     * @param id id
     * @return View or null
     */
    protected View findViewById(int id) {
        if (getView() != null) {
            return getView().findViewById(id);
        }
        return null;
    }

    //找 Activity中的ID
    protected View findViewInActivityById(int id) {
        return this.mActivity.findViewById(id);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public View setContentView(LayoutInflater inflater, int layoutResID, ViewGroup container) {
        return setContentView(inflater, layoutResID, container, false);
    }

    public View setContentView(LayoutInflater inflater, int layoutResID, ViewGroup container, boolean attachToRoot) {
        this.mInflater = inflater;
        View viewRoot = inflater.inflate(layoutResID, container, attachToRoot);
        InjectedView.init(this, viewRoot);
        viewRoot.setOnClickListener(null);
        return viewRoot;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        initListener();
    }
}
