package com.hexing.libhexbase.view;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @author caibinglong
 *         date 2018/6/1.
 *         desc 动画
 */

public class AnimatorView {
    /**
     * 从下往上动画显示
     * @param view     view
     * @param isShow   bool
     * @param duration 持续时间
     */
    public static void translationAnimRun(final View view, final boolean isShow, final long duration) {
        ObjectAnimator animator;
        final int height = view.getHeight();
        if (height != 0) {
            if (isShow) {
                animator = ObjectAnimator.ofFloat(view, "translationY",
                        view.getHeight(), 0).setDuration(duration);
            } else {
                animator = ObjectAnimator.ofFloat(view, "translationY", 0,
                        view.getHeight()).setDuration(duration);
            }
            animator.start();
        } else {
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    int preHeight = view.getHeight();
                    if (preHeight != 0) {
                        translationAnimRun(view, isShow, duration);
                    }
                    return true;
                }
            });
        }
    }

}
