package cn.hexing.dlms;

import java.util.List;

import cn.hexing.model.TranXADRAssist;

/**
 * @author caibinglong
 *         date 2018/2/1.
 *         desc desc
 */

public abstract class IHexListener implements IHexListener2 {
    @Override
    public void onFailure(String msg) {

    }

    @Override
    public void onSuccess(Object object, int dataFormat) {

    }

    @Override
    public void onSuccess(byte[] bytes) {

    }


    @Override
    public void onSuccess(List<TranXADRAssist> dataList) {

    }

    @Override
    public void onSuccessBlock(List<List<TranXADRAssist>> blockList) {

    }


    @Override
    public void onSuccess(Object object) {

    }

    @Override
    public void onSuccess(TranXADRAssist data, int pos) {

    }

    @Override
    public void onSuccess(TranXADRAssist data) {

    }

    @Override
    public void onFinish() {

    }
}
