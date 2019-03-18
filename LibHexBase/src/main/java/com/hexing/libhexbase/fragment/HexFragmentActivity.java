package com.hexing.libhexbase.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hexing.libhexbase.activity.HexBaseActivity;

/**
 * Fragment基础Activity
 * 提供FragmentActivity快捷功能
 * Created by long on 16/3/25.
 */
public class HexFragmentActivity extends HexBaseActivity {

    public FragmentManager mFManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFManager = getSupportFragmentManager();
    }

    /**
     * 添加Fragment
     *
     * @param isAddBackStack 是否添加回退栈
     */
    public FragmentTransaction addFragment(int contentId, Fragment fragment, String tag, boolean isAddBackStack) {
        FragmentTransaction transaction = mFManager.beginTransaction();
        transaction.add(contentId, fragment, tag);
        if (isAddBackStack) {
            transaction.addToBackStack(tag);
        }
        return transaction;
    }

    /**
     * 覆盖Fragment
     *
     * @param isAddBackStack 是否添加回退栈
     */
    public FragmentTransaction replaceFragment(int contentId, Fragment fragment, String tag, boolean isAddBackStack) {
        FragmentTransaction transaction = mFManager.beginTransaction();
        transaction.replace(contentId, fragment, tag);
        if (isAddBackStack) {
            transaction.addToBackStack(tag);
        }
        return transaction;
    }

}
