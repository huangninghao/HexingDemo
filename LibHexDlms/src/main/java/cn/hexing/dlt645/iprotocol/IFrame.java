package cn.hexing.dlt645.iprotocol;

import cn.hexing.dlt645.NotImplementedException;

/**
 * @author caibinglong
 *         date 2018/12/14.
 *         desc desc
 */

public interface IFrame {
    void SetFrameParameter(int frameType, byte[] parameter) throws NotImplementedException;
    String GetSendFrame(byte[] data, int sendType) throws NotImplementedException;
}
