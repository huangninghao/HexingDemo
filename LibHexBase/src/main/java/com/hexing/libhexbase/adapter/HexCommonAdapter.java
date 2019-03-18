package com.hexing.libhexbase.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * BaseAdapter的封装类
 */
public abstract class HexCommonAdapter<T> extends BaseAdapter {
    private Context mContext;
    private List<T> mData;
    private LayoutInflater mInflater;
    private int mLayoutId;

    public HexCommonAdapter(Context context, List<T> dataList, @LayoutRes int layoutId) {
        this.mContext = context;
        this.mData = dataList;
        this.mLayoutId = layoutId;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        } else {
            return mData.size();
        }
    }

    public void setData(List<T> listData) {
        this.mData = listData == null ? new ArrayList<T>() : listData;
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HexViewHolder holder = HexViewHolder.get(mContext, convertView, parent, mLayoutId, position);
        convert(holder, getItem(position));
        return holder.getConvertView();
    }

    /**
     * 这里处理控件
     *
     * @param holder viewHolder
     * @param item   item
     */
    public abstract void convert(HexViewHolder holder, T item);

}
