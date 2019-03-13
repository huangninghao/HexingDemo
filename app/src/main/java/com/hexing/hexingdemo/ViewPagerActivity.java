package com.hexing.hexingdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.cblong.xrecyclerview.XRecyclerView;
import com.hexing.hexingdemo.model.ObjectNodeModel;
import com.hexing.libhexbase.activity.HexBaseActivity;
import com.hexing.libhexbase.adapter.HexRVBaseAdapter;
import com.hexing.libhexbase.adapter.HexRVBaseViewHolder;
import com.hexing.libhexbase.adapter.HexViewPagerViewAdapter;
import com.hexing.libhexbase.tools.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caibinglong
 *         date 2018/3/27.
 *         desc desc
 */

public class ViewPagerActivity extends HexBaseActivity {
    private HexViewPagerViewAdapter adapter;
    private TextView tvAdd;
    private ViewPager viewPager;
    private HexRVBaseAdapter<ObjectNodeModel> commonAdapter;
    private List<ObjectNodeModel> objectNodeModelList = new ArrayList<>();

    private List<View> views = new ArrayList<>();
    int i = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);

        tvAdd = findViewById(R.id.tvAdd);
        viewPager = findViewById(R.id.viewPager);

        adapter = new HexViewPagerViewAdapter(this, views);

        adapter.setViews(views);

        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                objectNodeModelList.clear();
                ObjectNodeModel model = new ObjectNodeModel();
                model.obis = "11111" + i;
                model.classid = TimeUtil.getNowTime();
                objectNodeModelList.add(model);

                View view = getLayoutInflater().inflate(R.layout.item_viewpager, null);
                XRecyclerView recyclerView = view.findViewById(R.id.recycleview);
                commonAdapter = new HexRVBaseAdapter<ObjectNodeModel>(ViewPagerActivity.this, objectNodeModelList, R.layout.item_test) {
                    @Override
                    protected void convert(HexRVBaseViewHolder holder, ObjectNodeModel item) {
                        holder.setText(R.id.text, item.obis);
                        holder.setText(R.id.text1, item.classid);
                    }
                };
                LinearLayoutManager manager = new LinearLayoutManager(App.getInstance());
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(commonAdapter);
                adapter.addView(view);
            }
        });

        viewPager.setAdapter(adapter);

    }
}
