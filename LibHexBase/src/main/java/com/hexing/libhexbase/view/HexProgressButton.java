package com.hexing.libhexbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexing.libhexbase.R;
import com.hexing.libhexbase.log.HexLog;
import com.hexing.libhexbase.tools.DisplayTool;

/**
 * @author caibinglong
 *         date 2018/8/12.
 *         desc 带进度条的按钮
 */

public class HexProgressButton extends LinearLayout implements Animatable {
    private HexProgressDrawable drawable;
    private TextView button;

    public interface onAnimFinish {
        void onFinish();
    }

    private onAnimFinish listener;

    public HexProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HexProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        RelativeLayout header = (RelativeLayout) mInflater.inflate(R.layout.button_progress, null, false);
        button = header.findViewById(R.id.button);

        if (attrs != null) {
            TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.HexProgressButton);

            int count = attrArray.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attrName = attrArray.getIndex(i);
                if (attrName == R.styleable.HexProgressButton_text) {
                    String text = attrArray.getString(R.styleable.HexProgressButton_text);
                    if (!TextUtils.isEmpty(text)) {
                        button.setText(text);
                    }
                    HexLog.e("Progress", "text = " + text);

                }
                if (attrName == R.styleable.HexProgressButton_textSize) {
                    float textSize = attrArray.getLayoutDimension(R.styleable.HexProgressButton_textSize, 12);

                    button.setTextSize((float) DisplayTool.px2sp_(context, textSize));
                    HexLog.e("Progress", "textSize = " + textSize + "||"
                            + DisplayTool.sp2px(context, textSize) + "||"
                            + DisplayTool.px2sp(context, textSize) + "||"
                            + DisplayTool.dip2px(context, textSize) + "||"
                            + DisplayTool.px2dp(context, textSize) + "||"
                            + DisplayTool.px2sp_(context, textSize));
                }
                if (attrName == R.styleable.HexProgressButton_textColor) {
                    int color = attrArray.getColor(R.styleable.HexProgressButton_textColor, ContextCompat.getColor(context, R.color.white));
                    button.setTextColor(color);
                    HexLog.e("Progress", "textColor = " + color);
                }

            }
            attrArray.recycle();
        }
        drawable = new HexProgressDrawable(button.getTextSize(), button);
        drawable.setColorDefault(button.getCurrentTextColor());
        drawable.setAnimatable(this);
        addView(header);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    public void startRotate() {
        button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        button.setCompoundDrawablePadding(15);
        drawable.startRotate();
    }

    public void animFinish() {
        drawable.animFinish();
    }

    public void animError() {
        drawable.animError();
    }

    public void removeDrawable() {
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        drawable.stopRotate();
    }

    @Override
    public void start() {
        startRotate();
    }

    @Override
    public void stop() {

        if (listener != null) {
            listener.onFinish();
        }
    }

    public void setOnAnimFinishListener(onAnimFinish listener) {
        this.listener = listener;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}

