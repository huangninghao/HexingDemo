package com.hexing.hexingdemo.contact;


import com.hexing.hexingdemo.bean.TestBean;
import com.hexing.libhexbase.inter.RxBasePresenter;
import com.hexing.libhexbase.inter.HexBaseView;

import java.util.List;

/*
 * 项目名:    BaseFrame
 * 包名       com.zhon.frame.mvp.login.contact
 * 文件名:    TestContact
 * 创建者:    ZJB
 * 创建时间:  2017/9/7 on 11:13
 * 描述:     TODO  接口
 */
public interface TestContact {

    interface view extends HexBaseView {
        /**
         * 设置数据
         *
         * @param dataList
         */
        void setData(List<TestBean.StoriesBean> dataList);
    }

    interface presenter extends RxBasePresenter {
        /**
         * 获取数据
         */
        void getData();
    }
}
