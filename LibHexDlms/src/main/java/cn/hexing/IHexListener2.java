package cn.hexing;


import java.util.List;

import cn.hexing.model.TranXADRAssist;

/**
 * @author caibinglong
 *         date 2018/2/1.
 *         desc desc
 */

public interface IHexListener2 {
    void onSuccess(Object object, int dataFormat);

    void onFailure(String msg);

    void onSuccess(byte[] bytes);

    void onSuccess(List<TranXADRAssist> dataList);

    void onSuccessBlock(List<List<TranXADRAssist>> blockList);

    void onFinish();

    void onSuccess(Object object);

    void onSuccess(TranXADRAssist data, int pos);
}
