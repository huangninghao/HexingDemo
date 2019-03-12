package cn.hexing.dlt645.check;


import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public interface IFrameChecker {
    ReceiveModel Check(int currentIndex, byte currentByte);

    int GetHeadIndex();

    int GetDataHeadIndex();

    int GetDataLength();

    void SetFrameCheckerParameter(int paraType, byte[] parameter);

    byte[] GetReceivedFrameParameter(int paraType);

    byte GetHeadByte();

    IFrameChecker CreateInstance(int HeadIndex);
}
