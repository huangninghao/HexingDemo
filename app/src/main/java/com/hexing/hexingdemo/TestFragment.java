package com.hexing.hexingdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hexing.hexingdemo.presenter.LoginPresenter;
import com.hexing.libhexbase.fragment.HexMVPBaseFragment;

/**
 * @author caibinglong
 *         date 2018/7/10.
 *         desc desc
 */

public class TestFragment extends HexMVPBaseFragment<LoginPresenter> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = setContentView(inflater, R.layout.activity_main, container);
        return root;
    }

    @Override
    protected LoginPresenter createPresenter() {
        return null;
    }

    @Override
    protected void firstLoad() {
        super.firstLoad();
    }
}
