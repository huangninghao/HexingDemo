package cn.hexing.dlt645.comm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.dlt645.zigbee.ZigBeeFrame;

/**
 * @author caibinglong
 *         date 2018/12/16.
 *         desc desc
 */

public class ZigbeeCommandExecutor extends CommOpticalSerialPort {
    public ZigbeeCommandExecutor() {
        super();
    }

    public ReceiveModel Write(String code, byte[] sendData, ReceiveModel model) {
        model.sendData = sendData.clone();
        ZigBeeFrame zigBeeFrame = new ZigBeeFrame();
        setSsSendFrameType(SendFrameTypes.ZigbeeATCommand);
        try {
            zigBeeFrame.SetFrameParameter(FrameParameters.ZigbeeCommandId, HexStringUtil.hexToByte(HexStringUtil.parseAscii(code)));
            //base.Checker.SetFilter(new Type[] { typeof(ZigbeeCommandResponseChecker) });
            // base.Checker.ClearFrameParameters();
            model.sendData = HexStringUtil.hexToByte(zigBeeFrame.GetSendFrame(sendData, SendFrameTypes.ZigbeeATCommand));
            model.isSend = sendByt(model.sendData);
            if (model.isSend) {
                model.recBytes = receiveByt(0, model.maxWaitTime, model.receiveByteLen);
                if (model.recBytes.length < 8) {
                    model.isSuccess = false;
                    return model;
                }
                for (int i = 0; i < model.recBytes.length; i++) {
                    //7E 0004（字节长度） 88（帧类型 固定） 01（帧id 固定） Cmd(2个字节)0000 state(一个字节操作状态) 00成功 02失败  操作数据（n个字节）+ 一个字节校验码
                    if (((model.recBytes[i] & 0xff) == 0x88) &&
                            ((model.recBytes[i + 4] & 0xff) == 0x00)) {
                        model.isSuccess = true;
                        return model;
                    }
                }
            }
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }

        return model;
    }

    public ReceiveModel Read(String code) {
        ReceiveModel model = new ReceiveModel();
        model.maxWaitTime = 5000;
        ssSendFrame = new ZigBeeFrame();
        ssSendFrameType = SendFrameTypes.ZigbeeATCommand;
        try {
            ssSendFrame.SetFrameParameter(FrameParameters.ZigbeeCommandId, HexStringUtil.hexToByte(HexStringUtil.parseAscii(code)));
            // base.Checker.SetFilter(new Type[]{typeof(ZigbeeCommandResponseChecker)});
            // base.Checker.ClearFrameParameters();
            model.sendData = HexStringUtil.hexToByte(ssSendFrame.GetSendFrame(new byte[0], ssSendFrameType));
            model.isSend = sendByt(model.sendData);
            if (model.isSend) {
                model.recBytes = receiveByt(model.sleepTime, model.maxWaitTime, model.receiveByteLen);
            }
        } catch (NotImplementedException e) {
            e.printStackTrace();
            model.errorMsg = "ZigbeeCommandExecutor Read||" + e.getMessage();
        }
        return model;
    }

    /// <summary>
    /// 搜网 / 采集器
    /// </summary>
    /// <returns></returns>
    public List<ReceiveModel> ReadNetWork() {
        List<ReceiveModel> modelList = new ArrayList<>();
        ReceiveModel model = new ReceiveModel();
        ssSendFrame = new ZigBeeFrame();
        ssSendFrameType = SendFrameTypes.ZigbeeNetworkSearchCommand;
        model.maxWaitTime = 9000;
        //base.Checker.SetFilter(new Type[]{typeof(ZigbeeNetworkSearchResponseChecker)});
        //base.Checker.ClearFrameParameters();
        try {
            model.sendData = HexStringUtil.hexToByte(ssSendFrame.GetSendFrame(new byte[0], ssSendFrameType));
            model.isSend = sendByt(model.sendData);
            if (model.isSend) {
                model.recBytes = receiveByt(model.sleepTime, model.maxWaitTime);

                int num = 0;
                byte[] bytes = new byte[0];
                for (int i = 0; i < model.recBytes.length; i++) {
                    if ((model.recBytes[i] & 0xff) == 0x45 && (model.recBytes[i + 1] & 0xff) == 0xC5) {
                        num = model.recBytes[i + 2] & 0xff;
                        int total = num * 21;
                        bytes = Arrays.copyOfRange(model.recBytes, i + 3, (i + 3) + total);
                        break;
                    }
                }

                if (bytes.length > 0 && num > 0) {
                    for (int m = 0; m < num; m++) {
                        byte[] byt = Arrays.copyOfRange(bytes, m * 21, (m + 1) * 21);
                        byte[] collector = Arrays.copyOfRange(byt, 13, 21);
                        byte[] newCollector = new byte[8];
                        int n = 0;
                        for (int k = collector.length - 1; k >= 0; k--) {
                            newCollector[n] = collector[k];
                            n++;
                        }
                        model = new ReceiveModel();
                        model.data = HexStringUtil.bytesToHexString(newCollector);
                        modelList.add(model);
                    }
                }
            }
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }

        return modelList;
    }

}
