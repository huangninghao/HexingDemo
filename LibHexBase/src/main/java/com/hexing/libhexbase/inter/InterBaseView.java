package com.hexing.libhexbase.inter;

import android.support.annotation.StringRes;

/**
 * @author caibinglong
 *         date 2018/8/26.
 *         desc view 接口
 */

public interface InterBaseView {
    void showLoading(String msg);

    void showLoading();

    void hideLoading();

    void showToast(String msg);

    void showToast(@StringRes int resId);

}
