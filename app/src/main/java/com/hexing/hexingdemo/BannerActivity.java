package com.hexing.hexingdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cbl.banner.view.BannerViewPager;
import com.hexing.libhexbase.activity.HeaderBaseActivity;

/**
 * @author caibinglong
 *         date 2019/1/19.
 *         desc desc
 */

public class BannerActivity extends HeaderBaseActivity {
    private BannerViewPager viewPager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        viewPager = findViewById(R.id.bannerViewPager);


    }
}
