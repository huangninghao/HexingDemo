package com.hexing.libhexbase.tools;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author long
 *         消息提示toast
 */
public class ToastUtils {
    public static final int TOAST_DURATION = 3000;
    private static Toast mToast = null;
    public static void showToast(Context context, int resId) {
        showToastTwo(context, context.getString(resId));
    }

    public static void showToast(Context context, String tips) {
        if (null == tips || TextUtils.isEmpty(tips.trim())) {
            return;
        }
        showToastTwo(context, tips);
    }

    public static void showToast(Context context, int resId, int time) {
        String toastString = context.getString(resId);
        if (mToast == null) {
            mToast = Toast.makeText(context, "", time);
        }
        mToast.setText(toastString);
        mToast.show();
    }

    private static void showToast(Context context, String str, int time) {
        if (mToast == null) {
            mToast = Toast.makeText(context, "", time);
        }
        mToast.setText(str);
        mToast.show();
    }

    private static void showToastTwo(Context context, String toastString) {
        if (mToast == null) {
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(toastString);
        mToast.show();
    }

    public static void showToastCenter(Context context, String toastString) {
        if (mToast == null) {
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(toastString);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
    }

    /**
     * 带图片的toast提示
     *
     * @param context         上下文
     * @param imageResourceId imgId
     */
    public static void ImageToast(Context context, int imageResourceId) {
        //创建带图片Toast提示消息   
        if (mToast == null) {
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        //设置Toast提示消息在屏幕上的位于中间  
        mToast.setGravity(Gravity.CENTER, 0, 0);
        //获取Toast提示消息里原有的View   
//      View toastView = mToast.getView();  
        //创建图像ImageView   
        ImageView img = new ImageView(context);
        img.setImageResource(imageResourceId);
        //创建一个对象LineLayout容器   
        LinearLayout ll = new LinearLayout(context);
        //向LinearLayout中添加ImageView和Toast原有的View   
        ll.addView(img);
//      ll.addView(toastView);  
        mToast.setGravity(Gravity.CENTER, 0, 0);
        //将LineLayout容器设置为toast的View   
        mToast.setView(ll);
        //显示消息   
        mToast.show();
    }
}
