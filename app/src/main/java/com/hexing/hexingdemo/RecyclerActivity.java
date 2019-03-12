package com.hexing.hexingdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.cblong.xrecyclerview.XRecyclerView;
import com.hexing.libhexbase.activity.HexBaseActivity;
import com.hexing.libhexbase.adapter.HexRVBaseAdapter;
import com.hexing.libhexbase.adapter.HexRVBaseViewHolder;
import com.hexing.libhexbase.adapter.RecyclerViewDivider;
import com.hexing.libhexbase.tools.ToastUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author caibinglong
 *         date 2018/5/31.
 *         desc desc
 */

public class RecyclerActivity extends HexBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        XRecyclerView recycler = findViewById(R.id.recycler);

        List<String> data = Arrays.asList("xxx", "yyy", "zzz");
        HexRVBaseAdapter<String> adapter = new HexRVBaseAdapter<String>(this, data, R.layout.item_setting_select) {
            @Override
            protected void convert(HexRVBaseViewHolder holder, String item) {
                holder.setText(R.id.tv_name, item);
            }
        };
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new RecyclerViewDivider(this, RecyclerViewDivider.HORIZONTAL));
        adapter.setOnItemClickListener(new HexRVBaseAdapter.onRecyclerViewItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                ToastUtils.showToast(RecyclerActivity.this, position + "");
            }
        });

        recycler.setAdapter(adapter);
    }
}
