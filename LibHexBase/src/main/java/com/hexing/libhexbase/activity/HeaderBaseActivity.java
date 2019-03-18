package com.hexing.libhexbase.activity;

import android.view.LayoutInflater;
import android.view.View;

import com.hexing.libhexbase.view.HeaderLayout;

/**
 * 带有头部的baseActivity
 */
public class HeaderBaseActivity extends HexBaseActivity {

    protected HeaderLayout headerLayout;
    public void setContentView(int layoutResID, int viewId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View topView = inflater.inflate(layoutResID, null);
        headerLayout = (HeaderLayout) topView.findViewById(viewId);
        super.setContentView(topView);
    }
}
