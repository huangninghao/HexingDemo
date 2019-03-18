package com.hexing.libhexbase.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hexing.libhexbase.R;

/**
 * 用于BaseAdapter封装类的ViewHolder
 *
 * @author caibinglong
 */
public class HexViewHolder {
    private SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;
    private Context mContext;

    public HexViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
        this.mViews = new SparseArray<>();
        this.mPosition = position;
        this.mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        this.mConvertView.setTag(this);
        this.mContext = context;
    }

    public static HexViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
        if (null == convertView) {
            return new HexViewHolder(context, parent, layoutId, position);
        } else {
            HexViewHolder holder = (HexViewHolder) convertView.getTag();
            holder.mPosition = position;
            return holder;
        }
    }

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 通过ViewId获取控件
     *
     * @param viewId view id
     * @return VIEW
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (null == view) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 给ID为viewId的TextView设置文字text，并返回this
     *
     * @param viewId view id
     * @param text   文本
     * @return holder
     */
    public HexViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        if (TextUtils.isEmpty(text)) {
            tv.setText("");
            return this;
        }
        tv.setText(text);
        return this;
    }

    public HexViewHolder setText(int viewId, CharSequence text) {
        TextView tv = getView(viewId);
        if (TextUtils.isEmpty(text.toString())) {
            tv.setText("");
            return this;
        }
        tv.setText(text);
        return this;
    }

    /**
     * 给ID为viewId的ImageView设置 Resource，并返回this
     *
     * @param viewId   view id
     * @param resource 资源
     * @return holder
     */
    public HexViewHolder setImageResource(int viewId, int resource) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resource);
        return this;
    }

    public HexViewHolder setImageResource(int viewId, String img) {
        ImageView imageView = getView(viewId);
        if (img.contains("http") || img.contains("https")) {
        } else {
            int resId = this.mContext.getResources().getIdentifier(img, "mipmap", this.mContext.getPackageName());
            //imageView.setImageResource(resId);
            Glide.with(this.mContext).load(resId).into(imageView);
        }
        return this;
    }
}
