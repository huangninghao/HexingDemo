package com.hexing.hexingdemo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.hexing.hexingdemo.adapter.TestAdapter;
import com.hexing.hexingdemo.bean.TestBean;
import com.hexing.hexingdemo.contact.TestContact;
import com.hexing.hexingdemo.presenter.TestPresenter;
import com.hexing.libhexbase.activity.RxMvpBaseActivity;

import java.util.ArrayList;
import java.util.List;


/*
 * 项目名:    BaseFrame
 * 文件名:    TestActivity
 * 创建者:    long
 * 创建时间:  2017/9/7 on 11:21
 * 描述:     TODO 测试Activity
 */
public class TestActivity extends RxMvpBaseActivity<TestContact.presenter> implements TestContact.view {

    private List<TestBean.StoriesBean> list = new ArrayList<>();//数据
    private TestAdapter adapter;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        init();
        mvpPresenter.getData();
    }

    /**
     * 初始化界面
     */
    private void init() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mvpPresenter.getData();
            }
        });
        adapter = new TestAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 初始化presenter
     *
     * @return 对应的presenter
     */
    @Override
    public TestContact.presenter createPresenter() {
        return new TestPresenter(this);
    }

    /**
     * 设置数据
     * 刷新界面
     *
     * @param dataList 数据源
     */
    @Override
    public void setData(List<TestBean.StoriesBean> dataList) {
        list.addAll(dataList);
        adapter.notifyDataSetChanged();
    }
}
