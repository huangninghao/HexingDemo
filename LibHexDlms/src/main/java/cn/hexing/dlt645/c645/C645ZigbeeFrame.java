package cn.hexing.dlt645.c645;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.iprotocol.IFrame;
import cn.hexing.dlt645.zigbee.ZigBeeFrame;

/**
 * @author caibinglong
 *         date 2018/12/14.
 *         desc desc
 */

public class C645ZigbeeFrame extends ZigBeeFrame implements IFrame {
    public C645Frame subFrame = new C645Frame();

    @Override
    public void SetFrameParameter(int faraType, byte[] parameter) {
        try {
            super.SetFrameParameter(faraType, parameter);
        } catch (NotImplementedException ex) {
            subFrame.SetFrameParameter(faraType, parameter);
        }
    }

    @Override
    public String GetSendFrame(byte[] data, @SendFrameTypes.SFrameTypes int sendType) throws NotImplementedException {
        String sendData = "";
        switch (sendType) {
            case SendFrameTypes.C645ZigbeeTransmit:
                try {
                    sendData = subFrame.GetSendFrame(data, SendFrameTypes.C645Transmit);
                    return super.GetSendFrame(HexStringUtil.hexToByte(sendData), SendFrameTypes.ZigbeeTransmit);
                } catch (NotImplementedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new NotImplementedException();
        }
        return sendData;
    }

}
