package cn.hexing.iec21.services;


import android.util.Log;


import cn.hexing.HexStringUtil;
import cn.hexing.IHexListener2;
import cn.hexing.dlms.protocol.bll.dlmsService;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.AbsCommServer;
import cn.hexing.iec21.iprotocol.HexHandLC;
import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public class CommServer extends AbsCommServer {
    private IHexListener2 listener;
    private boolean debugMode = false;
    HexHandLC protocol = new HexHandLC();

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public AbsCommAction openDevice(CommPara para, AbsCommAction commDevice) {
        AbsCommAction DevOpen = null;
        try {
            boolean blOpen = commDevice.openDevice(para);
            if (blOpen) {
                DevOpen = commDevice;
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return DevOpen;
    }

    @Override
    public boolean close(AbsCommAction commDevice) {
        boolean blClose = false;
        try {
            blClose = commDevice.closeDevice();
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return blClose;
    }

    @Override
    public TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice) {
        String strValue = "";
        TranXADRAssist assist = paraModel.getListTranXADRAssist().get(0);

        try {
            //转换数据类型
            paraModel.OBISattri = assist.obis;
            //paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = HexStringUtil.hexToByte(paraModel.strMeterNo);
            }
            byte[] recByt = protocol.read(paraModel, commDevice);
            assist.recBytes = recByt;
            if (recByt == null || recByt.length == 0) {
                if (paraModel.ErrTxt != null) {
                    strValue = paraModel.ErrTxt;
                }
            } else {
                strValue = AnalysisService.tranXADRCode(recByt, assist);
                assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
            }
        } catch (Exception e) {
            if (debugMode) {
                Log.e("Read异常", "错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
        }
        assist.value = strValue;
        if (debugMode) {
            Log.v("数据解析", "数据接收=" + strValue);
        }
        return assist;
    }

    @Override
    public TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        String strValue = "";
        try {
            paraModel.WriteData = assist.writeData;
            //转换数据类型
            paraModel.OBISattri = assist.obis;
            paraModel.isConBaudRate = assist.autoBaudRate;
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = HexStringUtil.hexToByte(paraModel.strMeterNo);
            }
            byte[] recByt = protocol.read(paraModel, commDevice);
            assist.recBytes = recByt;
            if (recByt == null || recByt.length == 0) {
                if (paraModel.ErrTxt != null) {
                    strValue = paraModel.ErrTxt;
                }
            } else {
                strValue = AnalysisService.tranXADRCode(recByt, assist);
                assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
                assist.aResult = true;
            }
        } catch (Exception e) {
            if (debugMode) {
                Log.e("Read异常", "错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
            assist.aResult = false;
        }
        assist.value = strValue;
        if (debugMode) {
            Log.v("数据解析", "数据接收=" + strValue);
        }
        return assist;
    }

    @Override
    public TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = assist.obis;
            paraModel.isConBaudRate = assist.autoBaudRate;
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = HexStringUtil.hexToByte(paraModel.strMeterNo);
            }
            paraModel.WriteData = AnalysisService.GetXADRCode(assist);
            assist.aResult = protocol.write(paraModel, commDevice);
        } catch (Exception e) {
            if (debugMode) {
                Log.e("Read异常", "错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
            assist.aResult = false;
        }
        if (debugMode) {
            Log.v("数据解析", "数据接收=" + paraModel.ErrTxt);
        }
        return assist;
    }

    @Override
    public TranXADRAssist action(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = assist.obis;
            paraModel.isConBaudRate = assist.autoBaudRate;
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = HexStringUtil.hexToByte(paraModel.strMeterNo);
            }
            paraModel.WriteData = AnalysisService.GetXADRCode(assist);
            assist.aResult = protocol.action(paraModel, commDevice);
        } catch (Exception e) {
            if (debugMode) {
                Log.e("Read异常", "错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
            assist.aResult = false;
        }
        if (debugMode) {
            Log.v("数据解析", "数据接收=" + paraModel.ErrTxt);
        }
        return assist;
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
        return protocol.sendByte(paraModel, commDevice, assist);
    }


    /***
     * 断开通讯
     *
     * @param commDevice ICommAction
     * @return bool
     */
    public boolean DiscFrame(HXFramePara para, AbsCommAction commDevice) {
        return protocol.discFrame(para, commDevice);
    }

}
