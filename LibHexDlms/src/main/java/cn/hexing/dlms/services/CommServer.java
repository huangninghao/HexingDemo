package cn.hexing.dlms.services;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.AbsCommServer;
import cn.hexing.iComm.ICommAction;
import cn.hexing.dlms.HexDataFormat;
import cn.hexing.HexStringUtil;
import cn.hexing.dlms.IHexListener2;
import cn.hexing.dlms.protocol.bll.dlmsService;
import cn.hexing.dlms.protocol.dlms.HXHdlcDLMS;
import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public class CommServer extends AbsCommServer {
    private IHexListener2 listener;
    private boolean debugMode = false;
    HXHdlcDLMS DLMSProtocol = new HXHdlcDLMS();

    public void addListener(IHexListener2 listener) {
        this.listener = listener;
    }

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

    public String strTakeNum(String str) {
        str = str.trim();
        String str2 = "";
        if (!"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }


    public byte[] GetCommuniteMeterAddr(String MeterAddr) {
        // 当表号中数字超过9位时，只取表号的后9位（自动补0）。
        byte[] btyResult = null;
        String strData = "";
        try {
            MeterAddr = strTakeNum(MeterAddr);
            if (MeterAddr.length() < 9) {
                strData = String
                        .format("%08X", Integer.parseInt(MeterAddr, 10));
                btyResult = HexStringUtil.hexToByte(strData);
            } else {
                strData = MeterAddr.substring(MeterAddr.length() - 9,
                        MeterAddr.length());

            }
            strData = String.format("%08X", Integer.parseInt(strData, 10));
            btyResult = HexStringUtil.hexToByte(strData);
        } catch (Exception ex) {
        }
        return btyResult;
    }


    char HexChar(char c) {
        if ((c >= '0') && (c <= '9'))
            return (char) (c - 0x30);
        else if ((c >= 'A') && (c <= 'F'))
            return (char) (c - 'A' + 10);
        else if ((c >= 'a') && (c <= 'f'))
            return (char) (c - 'a' + 10);
        else
            return 0x10;
    }

    byte[] Str2Hex(String str) {
        int t, t1;
        int rlen = 0, len = str.length();
        final byte[] byteArray = new byte[str.length()];
        // data.SetSize(len/2);
        for (int i = 0; i < len; ) {
            char l, h = str.charAt(i);
            if (h == ' ') {
                i++;
                continue;
            }
            i++;
            if (i >= len)
                break;
            l = str.charAt(i);
            t = HexChar(h);
            t1 = HexChar(l);
            if ((t == 16) || (t1 == 16))
                break;
            else
                t = t * 16 + t1;
            i++;
            byteArray[rlen] = (byte) t;
            rlen++;
        }

        final byte[] byteArray1 = new byte[rlen];
        System.arraycopy(byteArray, 0, byteArray1, 0, rlen);

        return byteArray1;

    }

    @Override
    public synchronized TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice) {
        String strValue = "";
        TranXADRAssist assist = new TranXADRAssist();

        try {
            //转换数据类型
            if (paraModel.getListTranXADRAssist() == null) {
                List<TranXADRAssist> list = new ArrayList<>();
                assist.dataType = HexDataFormat.getDataType(paraModel.getRecDataType());
                list.add(assist);
                paraModel.setListTranXADRAssist(list);
            }
            assist.obis = paraModel.OBISattri;
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            assist.recBytes = recByt;
            if (recByt == null || recByt.length == 0) {
                if (paraModel.ErrTxt != null) {
                    strValue = paraModel.ErrTxt;
                }
            } else {
                if (paraModel.getRecDataType().equals("Struct_Billing")) {
                    strValue = dlmsService.TranBillingCode(recByt, paraModel.getListTranXADRAssist());
                    assist.value = strValue;
                } else {
                    assist = dlmsService.tranXADRCode(recByt, paraModel.getListTranXADRAssist().get(0));
                }
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

        if (debugMode) {
            Log.v("数据解析", "数据接收=" + strValue);
        }
        return assist;
    }

    @Override
    public TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assist.obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }

            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            assist.recBytes = recByt;
            if (recByt == null || recByt.length == 0) {
                if (paraModel.ErrTxt != null) {
                    assist.errMsg = paraModel.ErrTxt;
                }
            } else {
                assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
                assist = dlmsService.tranXADRCode(recByt, assist);
                if (!TextUtils.isEmpty(assist.recStrData)) {
                    assist.aResult = true;
                }
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
        if (debugMode) {
            Log.v("数据解析Read", "数据接收=" + assist.value);
        }
        return assist;
    }

    /**
     * 读取 日费率
     *
     * @param paraModel  HXFramePara
     * @param commDevice AbsCommAction
     * @param assist     TranXADRAssist
     * @return List<TranXADRAssist>
     */
    public List<TranXADRAssist> readDayRate(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        List<TranXADRAssist> assists = new ArrayList<>();
        assists.add(assist);
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assist.obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            assist.recBytes = recByt;
            if (recByt == null || recByt.length == 0) {
                if (paraModel.ErrTxt != null) {
                    assist.errMsg = paraModel.ErrTxt;
                }
            } else {
                assists = dlmsService.getTranRate(recByt, assist);
                assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
                if (!TextUtils.isEmpty(assist.recStrData)) {
                    assist.aResult = true;
                }
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
        if (debugMode) {
            Log.v("数据解析Read", "数据接收=" + assist.value);
        }
        return assists;
    }

    /**
     * 设置 日费率\冻结项
     */
    public TranXADRAssist writeDayRate(HXFramePara paraModel, AbsCommAction commDevice, List<TranXADRAssist> assists) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assists.get(0).obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            paraModel.WriteData = dlmsService.GetXADRCode(assists);
            assists.get(0).aResult = DLMSProtocol.write(paraModel, commDevice);
            assists.get(0).value = paraModel.actionResult;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assists.get(0).errMsg = e.getMessage();
        }
        if (debugMode) {
            Log.v("数据解析Write", "数据接收=" + assists.get(0).aResult + "||type=" + assists.get(0).dataType);
        }
        return assists.get(0);
    }


    /**
     * 设置 冻结项
     */
    public TranXADRAssist writeBlock(HXFramePara paraModel, AbsCommAction commDevice, List<TranXADRAssist> assists) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assists.get(0).obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            paraModel.WriteData = dlmsService.GetXADRCode(assists);
            assists.get(0).aResult = DLMSProtocol.write(paraModel, commDevice);
            assists.get(0).value = paraModel.actionResult;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assists.get(0).errMsg = e.getMessage();
        }
        if (debugMode) {
            Log.v("数据解析Write", "数据接收=" + assists.get(0).aResult + "||type=" + assists.get(0).dataType);
        }
        return assists.get(0);
    }

    /**
     * 正常读数据块
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return list
     * @deprecated use {@link #ReadBlockNew(HXFramePara paraModel, ICommAction commDevice, TranXADRAssist assist)} instead
     */
    public synchronized List<List<TranXADRAssist>> ReadBlock(HXFramePara paraModel, ICommAction commDevice) {
        List<List<TranXADRAssist>> dataList = new ArrayList<>();
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            if (recByt == null || recByt.length == 0) {
                return dataList;
            }
            if (debugMode) {
                Log.v("ReadBlock", HexStringUtil.bytesToHexString(recByt));
            }

            dataList = dlmsService.TranBillingList(recByt, paraModel.getListTranXADRAssist());
        } catch (Exception e) {
            System.out.println("数据接收错误=" + e.getMessage());
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return dataList;
    }

    /**
     * 正常读数据块
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return list
     */
    public synchronized List<TranXADRAssist> ReadBlockNew(HXFramePara paraModel, ICommAction commDevice, TranXADRAssist assist) {
        List<TranXADRAssist> dataList = new ArrayList<>();
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            if (recByt == null || recByt.length == 0) {
                return dataList;
            }
            if (debugMode) {
                Log.v("ReadBlockNew", HexStringUtil.bytesToHexString(recByt));
            }
            if (assist.dataType == HexDataFormat.DAY_RATE) {
                dataList = dlmsService.getTranRate(recByt, assist);
            } else if (assist.dataType == HexDataFormat.DISPLAY_ARRAY) {
                dataList = dlmsService.getDisplay(recByt, assist);
            } else {
                dataList = dlmsService.TranBillingList(recByt, assist);
            }
            dataList.get(0).recBytes = recByt;
        } catch (Exception e) {
            System.out.println("ReadBlockNew数据接收错误=" + e.getMessage());
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return dataList;
    }


    /**
     * 捕获 冻结数据项 obis
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return List<TranXADRAssist>
     * @deprecated use {@link #ReadCaptureNew(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist)} instead
     */
    public synchronized List<TranXADRAssist> ReadCapture(HXFramePara paraModel, AbsCommAction commDevice) {
        List<TranXADRAssist> dataList = new ArrayList<>();
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            if (debugMode) {
                Log.v("ReadCapture", "准备开始捕获对象=" + paraModel.OBISattri);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            if (debugMode) {
                Log.v("ReadCapture", "读取捕获对象结束=" + HexStringUtil.bytesToHexString(recByt));
            }
            if (recByt == null || recByt.length == 0) {
                return dataList;
            }
            dataList = dlmsService.TranBillingCode(recByt);
        } catch (Exception e) {
            if (debugMode) {
                Log.e("ReadCapture", "数据接收错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return dataList;
    }

    /**
     * 捕获 冻结数据项 obis
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return List<TranXADRAssist>
     */
    public synchronized TranXADRAssist ReadCaptureNew(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            if (debugMode) {
                Log.v("ReadCapture", "准备开始捕获对象=" + paraModel.OBISattri);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            if (debugMode) {
                Log.v("ReadCapture", "读取捕获对象结束=" + HexStringUtil.bytesToHexString(recByt));
            }
            if (recByt == null || recByt.length == 0) {
                return assist;
            }
            assist = dlmsService.TranBillingCodeNew(recByt, assist);
        } catch (Exception e) {
            if (debugMode) {
                Log.e("ReadCapture", "数据接收错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return assist;
    }

    /**
     * 捕获负荷 对象
     * @param paraModel HXFramePara
     * @param commDevice AbsCommAction
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    public synchronized TranXADRAssist ReadProfiles(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist){
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            if (debugMode) {
                Log.v("ReadProfiles", "准备开始捕获对象=" + paraModel.OBISattri);
            }
            byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
            if (debugMode) {
                Log.v("ReadProfiles", "负荷读取捕获对象结束=" + HexStringUtil.bytesToHexString(recByt));
            }
            if (recByt == null || recByt.length == 0) {
                return assist;
            }
            assist = dlmsService.TranBillingCodeNew(recByt, assist);
        } catch (Exception e) {
            if (debugMode) {
                Log.e("ReadProfiles", "数据接收错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return assist;
    }

    /**
     * 捕获 负荷 数据项 obis
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return List<TranXADRAssist>
     */
    public synchronized TranXADRAssist readCaptureBurden(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            if (debugMode) {
                Log.v("ReadCapture", "准备开始捕获对象=" + paraModel.OBISattri);
            }
            boolean isSuccess = DLMSProtocol.write(paraModel, commDevice);

            if (isSuccess) {
                byte[] recByt = DLMSProtocol.read(paraModel, commDevice);
                if (debugMode) {
                    Log.v("ReadCapture", "读取捕获对象结束=" + HexStringUtil.bytesToHexString(recByt));
                }
                if (recByt == null || recByt.length == 0) {
                    return assist;
                }
                assist = dlmsService.TranBillingCodeNew(recByt, assist);
            }
        } catch (Exception e) {
            if (debugMode) {
                Log.e("ReadCapture", "数据接收错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
        }
        return assist;
    }


    @Override
    public synchronized TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice) {
        TranXADRAssist assist = new TranXADRAssist();
        try {
            //转换数据类型
            assist.obis = paraModel.OBISattri;
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            assist.dataType = HexDataFormat.getDataType(paraModel.getWriteDataType());
            assist.writeData = paraModel.WriteData;
            paraModel.WriteData = dlmsService.GetXADRCode(assist);
            assist.aResult = DLMSProtocol.write(paraModel, commDevice);
            assist.value = paraModel.actionResult;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
        }
        if (debugMode) {
            Log.v("数据解析Write", "数据接收=" + assist.aResult + "||type=" + assist.dataType);
        }
        return assist;
    }

    @Override
    public TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assist.obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            paraModel.WriteData = dlmsService.GetXADRCode(assist);
            assist.aResult = DLMSProtocol.write(paraModel, commDevice);
            assist.value = paraModel.actionResult;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
        }
        if (debugMode) {
            Log.v("数据解析Write", "数据接收=" + assist.aResult + "||type=" + assist.dataType + "||writeData=" + assist.writeData);
        }
        return assist;
    }

    @Override
    public synchronized TranXADRAssist action(HXFramePara paraModel, AbsCommAction commDevice) {
        TranXADRAssist assist = new TranXADRAssist();
        try {
            //转换数据类型
            assist.obis = paraModel.OBISattri;
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            assist.aResult = DLMSProtocol.action(paraModel, commDevice);
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
        }
        return assist;
    }


    public synchronized TranXADRAssist action(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assist.obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            assist.aResult = DLMSProtocol.action(paraModel, commDevice);
        } catch (Exception e) {
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
        }
        return assist;
    }

    /**
     * 执行 并 解析返回值
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return String
     */
    public synchronized TranXADRAssist actionAndRead(HXFramePara paraModel, ICommAction commDevice) {
        TranXADRAssist tranXADRAssist = paraModel.getListTranXADRAssist().get(0);

        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(paraModel.OBISattri);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            if (debugMode) {
                Log.v("actionAndRead", "开始读取=" + paraModel.OBISattri);
            }
            byte[] recByt = DLMSProtocol.actionAndRead(paraModel, commDevice);

            if (recByt == null || recByt.length == 0) {
                tranXADRAssist.errMsg = paraModel.ErrTxt;
                if (debugMode) {
                    Log.e("actionAndRead", "未收到任何回应数据");
                }
                tranXADRAssist.aResult = false;
                return tranXADRAssist;
            }
            tranXADRAssist = dlmsService.tranXADRCode2(recByt, tranXADRAssist);
            tranXADRAssist.aResult = true;
        } catch (Exception e) {
            if (debugMode) {
                Log.e("actionAndRead", "异常错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            tranXADRAssist.errMsg = e.getMessage();
            tranXADRAssist.aResult = false;
        }
        return tranXADRAssist;
    }

    /**
     * 执行 并 解析返回值
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return String
     */
    public synchronized TranXADRAssist actionAndRead(HXFramePara paraModel, ICommAction commDevice, TranXADRAssist assist) {

        try {
            //转换数据类型
            paraModel.OBISattri = dlmsService.fnChangeOBIS(assist.obis);
            paraModel.sysTitleC = Str2Hex(paraModel.StrsysTitleC);
            if (paraModel.CommDeviceType.equals("Optical")) {
                paraModel.DestAddr = new byte[]{0x03};
            } else if (paraModel.CommDeviceType.equals("RF")) {
                paraModel.DestAddr = GetCommuniteMeterAddr(paraModel.strMeterNo);
            }
            if (debugMode) {
                Log.v("actionAndRead", "开始读取=" + paraModel.OBISattri);
            }
            byte[] recByt = DLMSProtocol.actionAndRead(paraModel, commDevice);

            if (recByt == null || recByt.length == 0) {
                assist.errMsg = paraModel.ErrTxt;
                if (debugMode) {
                    Log.e("actionAndRead", "未收到任何回应数据");
                }
                assist.aResult = false;
                return assist;
            }
            assist = dlmsService.tranXADRCode2(recByt, assist);
            assist.aResult = true;
        } catch (Exception e) {
            if (debugMode) {
                Log.e("actionAndRead", "异常错误=" + e.getMessage());
            }
            if (listener != null) {
                listener.onFailure(e.getMessage());
            }
            assist.errMsg = e.getMessage();
            assist.aResult = false;
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
        return DLMSProtocol.sendByte(paraModel, commDevice, assist);
    }

    /**
     * 直接发送 byte[] 数据 assist.WriteData
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @param assist     TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist sendUpgrade(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        return DLMSProtocol.sendUpgrade(paraModel, commDevice, assist);
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
