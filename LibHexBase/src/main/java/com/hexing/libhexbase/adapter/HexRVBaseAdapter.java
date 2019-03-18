package com.hexing.libhexbase.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

/**
 * @author caibinglong
 *         date 2018/5/30.
 *         desc desc
 */

public abstract class HexRVBaseAdapter<T> extends RecyclerView.Adapter<HexRVBaseViewHolder> implements View.OnClickListener {
    public static final String TAG = "HexRVBaseAdapter";
    protected List<T> mData;
    protected final LayoutInflater mLayoutInflater;
    private int mLayoutId;
    private onRecyclerViewItemClickListener mOnItemClickListener;

    public HexRVBaseAdapter(Context context, List<T> data, @LayoutRes int layoutId) {
        mData = data;
        this.mLayoutId = layoutId;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    /**
     * add one cell
     *
     * @param cell T
     */
    public void add(T cell) {
        mData.add(cell);
        int index = mData.indexOf(cell);
        notifyItemChanged(index);
    }

    public void add(int index, T cell) {
        mData.add(index, cell);
        notifyItemChanged(index);
    }

    /**
     * remove a cell
     *
     * @param cell
     */
    public void remove(T cell) {
        int indexOfCell = mData.indexOf(cell);
        remove(indexOfCell);
    }

    public void remove(int index) {
        mData.remove(index);
        notifyItemRemoved(index);
    }

    /**
     * @param start 开始
     * @param count 结束标志
     */
    public void remove(int start, int count) {
        if ((start + count) > mData.size()) {
            return;
        }
        int size = getItemCount();
        for (int i = start; i < size; i++) {
            mData.remove(i);
        }
        notifyItemRangeRemoved(start, count);
    }


    /**
     * add a cell list
     *
     * @param cells List<T>
     */
    public void addAll(List<T> cells) {
        if (cells == null || cells.size() == 0) {
            return;
        }
        mData.addAll(cells);
        notifyItemRangeChanged(mData.size() - cells.size(), mData.size());
    }

    public void addAll(int index, List<T> cells) {
        if (cells == null || cells.size() == 0) {
            return;
        }
        mData.addAll(index, cells);
        notifyItemRangeChanged(index, index + cells.size());
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(HexRVBaseViewHolder viewHolder, final int position) {
        viewHolder.itemView.setTag(position);
        convert(viewHolder, mData.get(position));
    }

    @Override
    public HexRVBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        view.setOnClickListener(this);
        return new HexRVBaseViewHolder(view, parent.getContext());
    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClickListener(view, (int) view.getTag());
        }
    }

    /**
     * 如果子类需要在onBindViewHolder 回调的时候做的操作可以在这个方法里做
     *
     * @param holder HexRVBaseViewHolder
     * @param item   T
     */
    protected abstract void convert(HexRVBaseViewHolder holder, T item);


    public void setOnItemClickListener(onRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(View view, int position);
    }
}

