package com.hexing.libhexbase.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hexing.libhexbase.R;


/**
 * @author long
 *         自定义title
 */
public class HeaderLayout extends LinearLayout {
    private LayoutInflater mInflater;
    private RelativeLayout header;
    private TextView titleView;
    private LinearLayout leftContainer, rightContainer;
    private Button backBtn;
    private TextView submit;
    private View rImageViewLayout, lImageViewLayout;
    private TextView rightButton;

    public HeaderLayout(Context context) {
        super(context);
        init();
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
        header = (RelativeLayout) mInflater.inflate(R.layout.base_common_header, null, false);
        titleView = (TextView) header.findViewById(R.id.tvTitle);
        leftContainer = (LinearLayout) header.findViewById(R.id.leftContainer);
        rightContainer = (LinearLayout) header.findViewById(R.id.rightContainer);
        backBtn = (Button) header.findViewById(R.id.btnBack);
        submit = (TextView) header.findViewById(R.id.submit);
        addView(header);
    }

    public void showTitle(int titleId) {
        titleView.setText(titleId);
    }

    public void showTitle(String s) {
        titleView.setText(s);
    }

    public void showTitle(String s, int textSize, int textColor) {
        titleView.setText(s);
        titleView.setTextColor(textColor);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
    }

    public void showLeftBackButton(OnClickListener listener) {
        showLeftBackButton(R.string.empty_header_str, R.mipmap.icon_back, listener);
    }

    public void showLeftBackButton() {
        showLeftBackButton(null);
    }

    public void showLeftBackButton(int backTextId, OnClickListener listener) {
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_back, 0, 0, 0);
        backBtn.setText(backTextId);
        if (listener == null) {
            listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) getContext()).finish();
                }
            };
        }
        backBtn.setOnClickListener(listener);
    }

    public void showLeftBackButton(int backTextId, int resId, OnClickListener listener) {
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setText(backTextId);
        backBtn.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        if (listener == null) {
            listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) getContext()).finish();
                }
            };
        }
        backBtn.setOnClickListener(listener);
    }

    public void showLeftBackButton(int backTextId, OnClickListener listener, boolean isback) {
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setText(backTextId);
        if (listener == null) {
            listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) getContext()).finish();
                }
            };
        }
        backBtn.setOnClickListener(listener);
    }

    public void showRightSubmitButton(int backTextId, OnClickListener listener) {
        submit.setVisibility(View.VISIBLE);
        submit.setText(backTextId);
        if (listener == null) {
            listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) getContext()).finish();
                }
            };
        }
        submit.setOnClickListener(listener);
    }

    public void showLeftImageButton(@DrawableRes int rightResId, OnClickListener listener) {
        lImageViewLayout = mInflater.inflate(R.layout.base_common_header_right_image_btn, null, false);
        ImageButton leftButton = (ImageButton) lImageViewLayout.findViewById(R.id.imageBtn);
        leftButton.setImageResource(rightResId);
        leftButton.setOnClickListener(listener);
        leftContainer.addView(lImageViewLayout);
    }

    public void showLeftImageButton(@DrawableRes int rightResId, OnClickListener listener, boolean isShow) {
        lImageViewLayout = mInflater.inflate(R.layout.base_common_header_right_image_btn, null, false);
        ImageButton leftButton = (ImageButton) lImageViewLayout.findViewById(R.id.imageBtn);
        leftButton.setImageResource(rightResId);
        leftButton.setOnClickListener(listener);
        leftContainer.addView(lImageViewLayout);
    }

    public void changeLeftImage(@DrawableRes int resId) {
        if (lImageViewLayout != null && leftContainer != null) {
            ImageButton leftButton = (ImageButton) lImageViewLayout.findViewById(R.id.imageBtn);
            leftButton.setImageResource(resId);
        }
    }

    public void changeRightImage(@DrawableRes int resId) {
        if (rImageViewLayout != null && rightContainer != null) {
            ImageButton rightButton = (ImageButton) rImageViewLayout.findViewById(R.id.imageBtn);
            rightButton.setImageResource(resId);
        }
    }

    public void showRightImageButton(int rightResId, OnClickListener listener) {
        rImageViewLayout = mInflater.inflate(R.layout.base_common_header_right_image_btn, null, false);
        ImageButton rightButton = (ImageButton) rImageViewLayout.findViewById(R.id.imageBtn);
        rightButton.setImageResource(rightResId);
        rightButton.setOnClickListener(listener);
        rightContainer.addView(rImageViewLayout);
    }

    public void showRightImageButton(int rightResId, OnClickListener listener, boolean isShow) {
        rImageViewLayout = mInflater.inflate(R.layout.base_common_header_right_image_btn, null, false);
        ImageButton rightButton = (ImageButton) rImageViewLayout.findViewById(R.id.imageBtn);
        rightButton.setImageResource(rightResId);
        rightButton.setOnClickListener(listener);
        rightContainer.addView(rImageViewLayout);
    }

    public void removeRightView() {
        rightContainer.removeView(rImageViewLayout);
    }

    public void removeLeftView() {
        leftContainer.removeView(lImageViewLayout);
    }

    public void removeAllView() {
        this.removeLeftView();
        this.removeRightView();
    }

    public void showRightTextButton(int color, int rightResId, OnClickListener listener) {
        lImageViewLayout = mInflater.inflate(R.layout.base_common_header_right_btn, null, false);
        rightButton = (TextView) lImageViewLayout.findViewById(R.id.textBtn);
        rightButton.setText(rightResId);
        rightButton.setTextColor(color);
        rightButton.setOnClickListener(listener);
        rightContainer.addView(lImageViewLayout);
    }

    public void setRightTextButton(int color) {
        rightButton.setTextColor(color);
    }

    public void setLeftTextButton(int color, int rightResId) {
        backBtn.setText(rightResId);
        backBtn.setTextColor(color);
    }

    public void hideLeftBackButton() {
        backBtn.setVisibility(GONE);
    }

    public void setHeaderBackground(int color) {
        header.setBackgroundColor(color);
    }

}
