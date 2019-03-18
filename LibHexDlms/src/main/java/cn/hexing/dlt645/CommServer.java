package cn.hexing.dlt645;


import java.util.ArrayList;
import java.util.List;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.c645.C645Frame;
import cn.hexing.dlt645.c645.C645ZigbeeFrame;
import cn.hexing.dlt645.check.FrameCheckerFilterTypes;
import cn.hexing.dlt645.zigbee.ZigBeeFrame;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.AbsCommServer;
import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public class CommServer extends AbsCommServer {
    private boolean debugMode = false;

    private HexHdlcFrame DLMSProtocol = new HexHdlcFrame();
    private ZigBeeFrame zigBeeFrame = new ZigBeeFrame();
    private C645Frame c645Frame = new C645Frame();
    private C645ZigbeeFrame c645ZigbeeFrame = new C645ZigbeeFrame();
    public final static byte[] BroadCastZigbeeAddress = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    public final static byte[] BroadCast645Address = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};
    public final static byte[] BroadCastZigbeeShortAddress = new byte[]{(byte) 0xFF, (byte) 0xFE};

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public AbsCommAction openDevice(CommPara cpara, AbsCommAction commDevice) {
        AbsCommAction DevOpen = null;
        try {
            boolean blOpen = commDevice.openDevice(cpara);
            if (blOpen) {
                DevOpen = commDevice;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return DevOpen;
    }

    @Override
    public boolean close(AbsCommAction commDevice) {
        boolean blClose = false;
        try {
            blClose = commDevice.closeDevice();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return blClose;
    }

    @Override
    public TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        assist.processWriteData = OrganizationAnalysis.ZigBeeGetXADRCode(assist);

        assist = DLMSProtocol.sendByte(paraModel, commDevice, assist);
        return assist;
    }

    @Override
    public TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {

        assist.processWriteData = OrganizationAnalysis.ZigBeeGetXADRCode(assist);
        assist = DLMSProtocol.sendByte(paraModel, commDevice, assist);
        assist.aResult = false;
        return assist;
    }

    public List<TranXADRAssist> searchCollector(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        paraModel.sendType = SendFrameTypes.ZigbeeNetworkSearchCommand;
        paraModel.filterReceiveType = FrameCheckerFilterTypes.ZigbeeNetworkSearchResponse;
        paraModel.setDataFrameWaitTime(9000);
        try {
            zigBeeFrame.SetFrameParameter(FrameParameters.ZigbeeNetWork, HexStringUtil.hexToByte(assist.writeData));
            assist.processWriteData = zigBeeFrame.GetSendFrame(new byte[0], paraModel.sendType);
            return DLMSProtocol.readCollector(paraModel, commDevice, assist);

        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 直接发送 byte[] 数据 assist.WriteData
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @param assist     TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist sendByte(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        return DLMSProtocol.sendByte(paraModel, commDevice, assist);
    }

    /***
     * 断开通讯
     *
     * @param commDevice ICommAction
     * @return bool
     */
    public boolean DiscFrame(AbsCommAction commDevice) {
        return DLMSProtocol.discFrame(commDevice);
    }
}
