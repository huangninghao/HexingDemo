package com.hexing.libhexbase.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * @author caibinglong
 *         date 2018/5/30.
 *         desc desc
 */

public class HexRVBaseViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> views;
    private View mItemView;
    private Context mContext;

    public HexRVBaseViewHolder(View itemView, Context Context) {
        super(itemView);
        this.mContext = Context;
        views = new SparseArray<>();
        mItemView = itemView;
    }

    /**
     * 获取ItemView
     *
     * @return View
     */
    public View getItemView() {
        return mItemView;
    }

    public View getView(int resId) {
        return retrieveView(resId);
    }

    public TextView getTextView(int resId) {
        return retrieveView(resId);
    }

    public ImageView getImageView(int resId) {
        return retrieveView(resId);
    }

    public Button getButton(int resId) {
        return retrieveView(resId);
    }

    @SuppressWarnings("unchecked")
    protected <V extends View> V retrieveView(int viewId) {
        View view = views.get(viewId);
        if (view == null) {
            view = mItemView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (V) view;
    }

    public void setText(int resId, CharSequence text) {
        getTextView(resId).setText(text);
    }

    public void setText(int viewId, int strId) {
        getTextView(viewId).setText(strId);
    }

    public void setImageUrl(int viewId, int resId) {
        getImageView(viewId).setImageResource(resId);
    }

    public void setImageUrl(int viewId, String img) {
        ImageView imageView = retrieveView(viewId);
        if (img.contains("http") || img.contains("https")) {
            Glide.with(this.mContext).load(img).into(imageView);
        } else {
            int resId = this.mContext.getResources().getIdentifier(img, "mipmap", this.mContext.getPackageName());
            //imageView.setImageResource(resId);
            Glide.with(this.mContext).load(resId).into(imageView);
        }
    }
}
