package com.hexing.hexingdemo.presenter;


import android.widget.Toast;

import com.hexing.hexingdemo.contact.TestContact;
import com.hexing.libhexbase.inter.RxBasePresenterImpl;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.HexAction;
import cn.hexing.HexDevice;
import cn.hexing.ParaConfig;
import cn.hexing.dlms.HexDataFormat;
import cn.hexing.iec21.HexClient21API;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

import static cn.hexing.HexDevice.COMM_NAME_RF2;


/*
 * 项目名:    BaseFrame
 * 文件名:    TestPresenter
 * 创建时间:  2017/9/7 on 11:17
 * 描述:     TODO
 */
public class TestPresenter extends RxBasePresenterImpl<TestContact.view> implements TestContact.presenter {

    public TestPresenter(TestContact.view view) {
        super(view);
    }

    HexClient21API hexClient21API;

    private HexClient21API buid21() {

        ParaConfig.Builder o = new ParaConfig.Builder();
        return o.setComName(COMM_NAME_RF2)
                .setDevice(HexDevice.KT50)
                .setIsHands(false)
                .setBaudRate(4800)
                .setAuthMode(HXFramePara.AuthMode.NONE)
                .setDataFrameWaitTime(1500)
                .setDebugMode(true)
                .setStrMeterNo("000000000123")
                .setStrMeterPwd("00000000")
                .setRecDataConversion(true)
                .setIsBitConversion(true)
                .setSleepSendTime(200)
                .setCommMethod(HexDevice.METHOD_RF)
                .setVerify("N").build21();


    }

    public List<TranXADRAssist> getlist() {

        List<TranXADRAssist> list = new ArrayList<>();
        TranXADRAssist tranXADRAssist = new TranXADRAssist();
        tranXADRAssist.name = "ddd";
        tranXADRAssist.obis = "C040";
        tranXADRAssist.visible = true;
        tranXADRAssist.writeData ="0970";
        tranXADRAssist.actionType = HexAction.ACTION_READ;
        list.add(tranXADRAssist);
        return list;

    }

    /**
     * 获取数据
     */
    @Override
    public void getData() {

        hexClient21API = buid21();
        hexClient21API.addListener(new cn.hexing.IHexListener() {


            @Override
            public void onSuccess(TranXADRAssist data, int pos) {
                super.onSuccess(data, pos);
                if (data.aResult)
                    getView().showToast("succeed"+data.value);
                else {
                    getView().showToast("failed");
                }

            }

            @Override
            public void onFailure(String msg) {
                super.onFailure(msg);

            }
        });
        hexClient21API.action(getlist());
//        Api.getInstance().test()
//                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(new Consumer<Disposable>() {
//                    @Override
//                    public void accept(@NonNull Disposable disposable) throws Exception {
//                        addDisposable(disposable);
//                        mvpView.get().showLoadingDialog("");
//                    }
//                })
//                .map(new Function<TestBean, List<TestBean.StoriesBean>>() {
//                    @Override
//                    public List<TestBean.StoriesBean> apply(@NonNull TestBean testBean) throws Exception {
//                        return testBean.getStories();
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<List<TestBean.StoriesBean>>() {
//                    @Override
//                    public void accept(@NonNull List<TestBean.StoriesBean> storiesBeen) {
//                        mvpView.get().hideLoadingDialog();
//                        mvpView.get().setData(storiesBeen);
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(@NonNull Throwable throwable) throws  Exception{
//                        mvpView.get().hideLoadingDialog();
//                        RxExceptionHelper.handleException(throwable);
//                    }
//                });
    }
}
