package com.hexing.hexingdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hexing.libhexbase.activity.HexBaseActivity;

/**
 * @author caibinglong
 *         date 2018/3/27.
 *         desc desc
 */

public class ViewPagerActivity extends HexBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
    }
}
