package com.hexing.libhexbase.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * http://www.cnblogs.com/kobe8/p/4343478.html
 *
 * @author cbl
 */
public class HexViewPagerViewAdapter extends PagerAdapter {
    private Context mContext;
    private List<View> views;
    private LayoutInflater inflater;

    /**
     * @param mContext 上下文
     * @param views    view
     */
    public HexViewPagerViewAdapter(Context mContext, List<View> views) {
        super();
        this.mContext = mContext;
        this.views = views;
        inflater = LayoutInflater.from(mContext);
    }

    public void addView(View view) {
        this.views.add(view);
        this.notifyDataSetChanged();
    }

    public void setViews(List<View> views) {
        this.views = views;
        this.notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        view.addView(views.get(position), 0);
        return views.get(position);
//        View v = this.views.get(position);
//        ViewGroup parent = (ViewGroup) v.getParent();
//        //Log.i("ViewPaperAdapter", parent.toString());
//        if (parent != null) {
//            parent.removeAllViews();
//        }
//        view.addView(views.get(position));
//        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
