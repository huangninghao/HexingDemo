package cn.hexing.dlms;


import android.os.SystemClock;
import android.system.ErrnoException;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.hexing.DeviceControl;
import cn.hexing.HexAction;
import cn.hexing.HexDevice;
import cn.hexing.HexProtocol;
import cn.hexing.HexStringUtil;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.ParaConfig;
import cn.hexing.dlms.protocol.comm.CommOpticalSerialPort;
import cn.hexing.dlms.services.CommServer;
import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;


/**
 * @author caibinglong
 *         date 2018/2/1.
 *         desc desc
 */

public class HexClientAPI {
    private static HexClientAPI instance;
    private String deviceType = HexDevice.KT50;
    private int commMethod = HexDevice.METHOD_OPTICAL;
    private HXFramePara framePara;
    private AbsCommAction iComm = new CommOpticalSerialPort();
    private CommPara cPara = new CommPara();
    private CommServer commServer;
    private HXFramePara.AuthMode authMode;
    private HXFramePara.AuthMethod encryptionMode;
    private int baudRate = 300;
    private int channelBaudRate = 4800;
    private int nBits = 8;
    private int nStop = 1;
    private long sleepSend = 20;// Sleep time after send(ms)
    private long sleepChangeBaudRate = 300;
    private String sVerify = "N";
    private long handWaitTime = 1200;
    private long dataFrameWaitTime = 1500;
    private boolean firstFrame = true;//是否第一帧
    private boolean isHands = true; //是否握手
    private boolean isTermination = false; //是否终止 读取obis
    /**
     * 是否固定通道
     */
    private int fixedChannel = -1;//固定通道0  1,2,3,4,5,6

    private byte enLevel = 0X00;
    private List<IHexListener> listenerList = new ArrayList<>();
    private Action action;
    private String strMeterNo;
    private String strComName;
    private ParaConfig config;
    private DeviceControl deviceControl;

    private boolean debugMode = false;


    /**
     * 执行操作枚举
     * READ 正常读
     * WRITE 正常写
     * ACTION 执行动作
     * ACTION_READ 执行动作且有返回值
     */
    public enum Action {
        READ, WRITE, ACTION, ACTION_READ
    }

    public static HexClientAPI getInstance() {
        if (instance == null) {
            instance = new HexClientAPI();
        }
        return instance;
    }

    public static HexClientAPI getInstance(ParaConfig config) {
        return instance = new HexClientAPI(config);
    }

    public HexClientAPI() {
    }

    public HexClientAPI(ParaConfig config) {
        this.config = config;
        this.strMeterNo = config.strMeterNo;
        this.deviceType = config.deviceType;
        this.commMethod = config.commMethod;
        this.strComName = config.strComName;
        this.baudRate = config.baudRate;
        this.channelBaudRate = config.channelBaudRate;
        this.nBits = config.nBits;
        this.nStop = config.nStop;
        this.enLevel = config.enLevel;
        this.sVerify = config.sVerify;
        this.handWaitTime = config.handWaitTime;
        this.dataFrameWaitTime = config.dataFrameWaitTime;
        this.authMode = config.authMode;
        this.encryptionMode = config.encryptionMode;
        this.firstFrame = config.firstFrame;
        this.isHands = config.isHands;
        this.sleepSend = config.sleepSendTime;
        this.debugMode = config.debugMode;
        this.sleepChangeBaudRate = config.changeBaudRateSleepTime;
        this.fixedChannel = config.fixedChannel;
    }

    /**
     * 设置 通讯设备 及通讯方式
     *
     * @param deviceType 设备类型
     * @param commMethod 通讯方式
     */
    public void setDeviceMethod(String deviceType, int commMethod) {
        this.deviceType = deviceType;
        this.commMethod = commMethod;
    }

    public void setComName(String comName) {
        this.strComName = comName;
    }

    public void setIsFirstFrame(boolean bool) {
        this.firstFrame = bool;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * 设置波特率
     *
     * @param baudRate int
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        if (commServer != null && iComm != null) {
            iComm.setBaudRate(baudRate);
        }
    }

    public void setStrMeterNo(String meterNo) {
        this.strMeterNo = meterNo;
    }

    /**
     * 设置 数据位 停止位
     *
     * @param nBits int
     * @param nStop int
     */
    public void setBit(int nBits, int nStop) {
        this.nBits = nBits;
        this.nStop = nStop;
    }

    /**
     * 设置加密
     *
     * @param enLevel 0x00 不加密  目前加密是0X03
     */
    public void setEnLevel(byte enLevel) {
        this.enLevel = enLevel;
    }

    public void setHandWaitTime(long waitTime) {
        this.handWaitTime = waitTime;
    }

    public void setDataFrameWaitTime(long waitTime) {
        this.dataFrameWaitTime = waitTime;
    }

    /**
     * 设置校验
     *
     * @param verify Y或N
     */
    public void setVerify(String verify) {
        this.sVerify = verify;
    }

    /**
     * 设置方式
     *
     * @param authMode       授权方式
     * @param encryptionMode 加密方式
     */
    public void setMode(HXFramePara.AuthMode authMode, HXFramePara.AuthMethod encryptionMode) {
        this.authMode = authMode;
        this.encryptionMode = encryptionMode;
    }

    public void initPara() throws Exception {
        // 光电头通讯参数设置
        commServer = new CommServer();
        commServer.setDebugMode(this.debugMode);
        framePara = new HXFramePara();

        if (deviceType.equals(HexDevice.KT50)) {
            switch (commMethod) {
                case HexDevice.METHOD_OPTICAL:
                    cPara.ComName = TextUtils.isEmpty(strComName) ? HexDevice.COMM_NAME_USB : strComName;
                    iComm = new CommOpticalSerialPort();
                    framePara.CommDeviceType = HexDevice.OPTICAL;// RF  Optical
                    break;
                case HexDevice.METHOD_BLUETOOTH:
                    break;
                case HexDevice.METHOD_RF:
                    iComm = new CommOpticalSerialPort();
                    cPara.ComName = TextUtils.isEmpty(strComName) ? HexDevice.COMM_NAME_RF2 : strComName;
                    framePara.CommDeviceType = HexDevice.RF;// RF  Optical
                    this.baudRate = this.baudRate == 300 ? 4800 : this.baudRate;
                    break;
                default:
                    throw new Exception("未设置通讯方式！");
            }
        } else if (deviceType.equals(HexDevice.HT380A)) {
            //380A
            cPara.ComName = HexDevice.COMM_NAME_SAC;
            iComm = new CommOpticalSerialPort();
            framePara.CommDeviceType = HexDevice.OPTICAL;
        }

        cPara.BRate = baudRate;
        cPara.DBit = nBits;
        cPara.Pty = sVerify.charAt(0);
        cPara.Sbit = nStop;

        // DLMS 通讯参数
        framePara.FirstFrame = this.firstFrame;// = true;
        framePara.isHands = this.isHands;
        framePara.Mode = this.authMode == null ? HXFramePara.AuthMode.HLS : this.authMode;
        framePara.enLevel = this.enLevel;
        framePara.SourceAddr = 0x03;
        framePara.strMeterNo = TextUtils.isEmpty(this.strMeterNo) ? "254455455" : this.strMeterNo;
        framePara.setSleepT((int) this.sleepSend);
        framePara.ByteWaitT = 1500;
        framePara.Pwd = "00000000";
        framePara.aesKey = new byte[16];
        framePara.auKey = new byte[16];
        framePara.enKey = new byte[16];
        framePara.setStrsysTitleC("4845430005000001");
        framePara.encryptionMethod = this.encryptionMode == null ? HXFramePara.AuthMethod.AES_GCM_128 : this.encryptionMode;
        framePara.sysTitleS = new byte[8];
        framePara.MaxSendInfo_Value = 255;
        framePara.sleepChangeBaudRate = this.sleepChangeBaudRate;
        framePara.baudRate = this.baudRate;
        framePara.isFixedChannel = this.fixedChannel;
        framePara.channelBaudRate = this.channelBaudRate;
    }

    /**
     * 写数据
     *
     * @param obis     obis
     * @param data     数据内容
     * @param dataType 数据类型
     * @deprecated use {@link #action(TranXADRAssist)} instead
     */
    public boolean write(String obis, String data, String dataType) {
        if (openSerial()) {
            framePara.OBISattri = obis;
            framePara.writeDataType = dataType;
            framePara.WriteData = data;
            framePara.setHandWaitTime((int) handWaitTime);
            framePara.setDataFrameWaitTime((int) dataFrameWaitTime);
            framePara.FirstFrame = this.firstFrame;
            TranXADRAssist assist = commServer.write(framePara, iComm);
            closeSerial();
            return assist.aResult;
        }
        return false;
    }

    /**
     * 普通设置 写操作
     *
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist write(TranXADRAssist assist) {
        if (openSerial()) {
            framePara.setHandWaitTime((int) handWaitTime);
            framePara.setDataFrameWaitTime((int) dataFrameWaitTime);
            framePara.FirstFrame = this.firstFrame;
            assist = commServer.write(framePara, iComm, assist);
            closeSerial();
        }
        return assist;
    }

    /**
     * 读取
     *
     * @param obis     obis
     * @param dataType 数据类型
     * @return String
     * @deprecated use {@link #action(TranXADRAssist)} instead
     */
    public String read(String obis, String dataType) {
        if (openSerial()) {
            framePara.OBISattri = obis;
            framePara.writeDataType = dataType;
            framePara.setRecDataType(dataType);
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.FirstFrame = this.firstFrame;
            TranXADRAssist assist = commServer.read(framePara, iComm);
            closeSerial();
            return assist.value;
        }
        return "";
    }

    /**
     * 读取 写 执行
     *
     * @param tranXADRAssist 对象
     */
    public void action(TranXADRAssist tranXADRAssist) {
        List<TranXADRAssist> tranList = new ArrayList<>();
        tranList.add(tranXADRAssist);
        this.action(tranList);
    }

    /**
     * 正常读
     *
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist read(TranXADRAssist assist) {
        if (openSerial()) {
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.FirstFrame = this.firstFrame;
            assist = commServer.read(framePara, iComm, assist);
            closeSerial();
        } else {
            assist.aResult = false;
        }
        return assist;
    }

    /**
     * 读取 写 执行
     *
     * @param assistList 对象集合
     */
    public synchronized void action(List<TranXADRAssist> assistList) {//
        if (openSerial()) {
            //List<TranXADRAssist> assistList = tranXADRAssist;
            framePara.setListTranXADRAssist(assistList);
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.FirstFrame = this.firstFrame;
            int pos = 0;
            while (assistList.size() > 0 && !isTermination && !TextUtils.isEmpty(assistList.get(0).obis)) {
                TranXADRAssist assist = assistList.get(0).clone();
                assist.processWriteData = assist.writeData;
                assist.originalWriteData = assist.writeData;
                if (!TextUtils.isEmpty(assist.startTime) && !TextUtils.isEmpty(assist.endTime)) {
                    //时间拼接
                    assist.processWriteData = writeTimeData(assist.startTime, assist.endTime);
                }
                framePara.OBISattri = assist.obis;
                framePara.WriteData = assist.processWriteData;
                framePara.writeDataType = assist.writeType;
                framePara.setRecDataType(assist.recType);

                if (assist.dataType > 0 && (assist.structList == null || assist.structList.size() == 0)) {
                    TranXADRAssist.StructBean bean = new TranXADRAssist.StructBean();

                    bean.value = assist.processWriteData;
                    bean.dataType = assist.definiteDataType == -1 ? assist.dataType : assist.definiteDataType;
                    bean.scale = assist.scale;
                    bean.name = assist.name;
                    bean.unit = assist.unit;
                    assist.structList = new ArrayList<>();
                    assist.structList.add(bean);
                }
                switch (assist.actionType) {
                    case HexAction.ACTION_READ:
                        assist = commServer.read(framePara, iComm, assist);
                        break;
                    case HexAction.ACTION_WRITE:
                        if (assist.dataType == HexDataFormat.FREEZE_CAPTURE
                                || assist.dataType == HexDataFormat.DISPLAY_ARRAY) {
                            assist = commServer.writeBlock(framePara, iComm, assistList);
                            isTermination = true;
                        } else if (assist.dataType == HexDataFormat.DAY_RATE) {
                            assist = commServer.writeDayRate(framePara, iComm, assistList);
                            if (assist.aResult) {
                                //费率写成功  设置激活时间
                                framePara.FirstFrame = false;
                                framePara.isHands = false;
                                assist.dataType = HexDataFormat.DATE_TIME;
                                if (TextUtils.isEmpty(assist.obisTwo)) {
                                    int index = assist.obis.lastIndexOf("#");
                                    assist.obis = assist.obis.substring(0, index + 1) + "10";
                                } else {
                                    assist.obis = assist.obisTwo;
                                }
                                assist = commServer.write(framePara, iComm, assist);
                                isTermination = true;
                            }
                        } else {
                            assist = commServer.write(framePara, iComm, assist);
                        }
                        break;
                    case HexAction.ACTION_EXECUTE:
                        assist = commServer.action(framePara, iComm);
                        break;
                    case HexAction.ACTION_EXECUTE_READ:
                        assist = commServer.actionAndRead(framePara, iComm);
                        break;
                    case HexAction.ACTION_READ_BLOCK:
                        if (assist.dataType == HexDataFormat.DAY_RATE) {
                            //日费率
                            List<TranXADRAssist> list = commServer.readDayRate(framePara, iComm, assistList.get(0));
                            listener.onSuccess(list);
                        } else if (assist.dataType == HexDataFormat.DISPLAY_ARRAY) {
                            List<TranXADRAssist> list = commServer.ReadBlockNew(framePara, iComm, assistList.get(0));
                            listener.onSuccess(list);
                        } else {
                            if (assistList.get(0).structList != null && assistList.get(0).structList.size() > 0) {
                                List<TranXADRAssist> dataList = commServer.ReadBlockNew(framePara, iComm, assist);
                                listener.onSuccess(dataList);
                            } else {
                                framePara.setRecDataType("Struct_Billing");
                                List<List<TranXADRAssist>> dataList = commServer.ReadBlock(framePara, iComm);
                                listener.onSuccessBlock(dataList);
                            }
                        }
                        break;
                    default:
                        System.out.println("action type is null");
                        break;
                }
                if (assist.aResult) {
                    framePara.FirstFrame = false;
                    framePara.isHands = false;
                } else {
                    discFrame();
                    framePara.FirstFrame = this.firstFrame;
                    framePara.isHands = this.isHands;
                }
                if (assist.actionType != HexAction.ACTION_READ_BLOCK) {
                    //读数据块单独 回调
                    listener.onSuccess(assist, pos);
                }
                assistList.remove(0);
                pos++;
                SystemClock.sleep(100);
            }
            closeSerial();
        }
    }

    /**
     * 读取日费率
     *
     * @param assist TranXADRAssist
     * @return List<TranXADRAssist>
     */
    public synchronized List<TranXADRAssist> readDayRate(TranXADRAssist assist) {
        List<TranXADRAssist> assists = new ArrayList<>();
        if (openSerial()) {
            framePara.OBISattri = assist.obis;
            framePara.WriteData = assist.writeData;
            framePara.FirstFrame = this.firstFrame;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.setListTranXADRAssist(assists);
            assists = commServer.readDayRate(framePara, iComm, assist);
            listener.onSuccess(assists);
            closeSerial();
        }
        return assists;
    }

    /**
     * 读取数据块 如冻结数据
     *
     * @param obis obis
     */
    public synchronized List<List<TranXADRAssist>> readBlock(String obis, String writeData, List<TranXADRAssist> tranXADRAssists) {
        List<List<TranXADRAssist>> dataList = new ArrayList<>();
        if (openSerial()) {
            framePara.OBISattri = obis;
            String dataType = "Struct_Billing";
            framePara.WriteData = writeData;
            framePara.FirstFrame = this.firstFrame;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.setListTranXADRAssist(tranXADRAssists);
            framePara.setRecDataType(dataType);
            framePara.setWriteDataType(dataType);
            dataList = commServer.ReadBlock(framePara, iComm);
            listener.onSuccessBlock(dataList);
            closeSerial();
        }
        return dataList;
    }

    /**
     * 读取数据块 如冻结数据
     *
     * @param obis obis
     */
    public synchronized List<TranXADRAssist> readBlockNew(String obis, String writeData, TranXADRAssist assist) throws Exception {
        List<TranXADRAssist> dataList = new ArrayList<>();
        if (assist == null || assist.structList == null || assist.structList.size() == 0) {
            throw new Exception("未配置待解析冻结项");
        }
        if (openSerial()) {
            framePara.OBISattri = obis;
            String dataType = "Struct_Billing";
            framePara.WriteData = writeData;
            framePara.FirstFrame = this.firstFrame;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.setRecDataType(dataType);
            framePara.setWriteDataType(dataType);
            dataList = commServer.ReadBlockNew(framePara, iComm, assist);
            listener.onSuccess(dataList);
            closeSerial();
        }
        return dataList;
    }

    /**
     * 动态 捕获 对象
     *
     * @param tranXADRAssist TranXADRAssist
     * @deprecated use {@link #readCaptureObjectNew(TranXADRAssist)} instead
     */
    public synchronized List<TranXADRAssist> readCaptureObject(TranXADRAssist tranXADRAssist) {
        List<TranXADRAssist> dataList = new ArrayList<>();
        if (openSerial()) {
            framePara.OBISattri = tranXADRAssist.obis;
            String dataType = "Struct_Capture";
            framePara.WriteData = tranXADRAssist.writeData;
            framePara.FirstFrame = this.firstFrame;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.writeDataType = dataType;
            framePara.setRecDataType(dataType);
            dataList = commServer.ReadCapture(framePara, iComm);
            listener.onSuccess(dataList);
            closeSerial();
        }
        return dataList;
    }

    /**
     * 动态 捕获 对象 2.0
     *
     * @param tranXADRAssist TranXADRAssist
     */
    public synchronized TranXADRAssist readCaptureObjectNew(TranXADRAssist tranXADRAssist) {
        if (openSerial()) {
            framePara.OBISattri = tranXADRAssist.obis;
            String dataType = "Struct_Capture";
            framePara.WriteData = tranXADRAssist.writeData;
            framePara.FirstFrame = this.firstFrame;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.writeDataType = dataType;
            framePara.setRecDataType(dataType);
            tranXADRAssist = commServer.ReadCaptureNew(framePara, iComm, tranXADRAssist);
            listener.onSuccess(tranXADRAssist, 0);
            closeSerial();
        }
        return tranXADRAssist;
    }

    /**
     * 执行
     *
     * @param obis      obis
     * @param writeData 发送数据
     * @param dataType  数据类型
     * @return bool
     */
    public synchronized boolean actionBlock(String obis, String writeData, String dataType) {
        framePara.OBISattri = obis;
        framePara.WriteData = writeData;
        framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
        framePara.setHandWaitTime((int) this.handWaitTime);
        framePara.FirstFrame = this.firstFrame;
        framePara.writeDataType = dataType;
        return commServer.action(framePara, iComm).aResult;
    }

    /**
     * 简单的执行操作 前提是串口已经打开 并且不是第一帧
     *
     * @param obis      obis
     * @param writeData 写入数据
     * @param dataType  数据类型
     * @return object
     * @deprecated use {@link #action(TranXADRAssist)} instead
     */
    public synchronized Object execute(String obis, String writeData, String dataType) {
        Object result;
        framePara.OBISattri = obis;
        framePara.FirstFrame = this.firstFrame;
        framePara.WriteData = writeData;
        framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
        framePara.setHandWaitTime((int) this.handWaitTime);
        framePara.setRecDataType(dataType);
        framePara.setRecDataType(dataType);
        switch (this.action) {
            case READ:
                result = commServer.read(framePara, iComm);
                break;
            case WRITE:
                result = commServer.write(framePara, iComm);
                break;
            case ACTION:
                result = commServer.action(framePara, iComm);
                break;
            case ACTION_READ:
                result = commServer.actionAndRead(framePara, iComm);
                break;
            default:
                result = "No Action Error";
                break;
        }
        return result;
    }

    /**
     * 简单的执行操作 前提是串口已经打开 并且不是第一帧
     *
     * @return TranXADRAssist
     * @deprecated use {@link #action(TranXADRAssist)} instead
     */
    public synchronized TranXADRAssist execute(TranXADRAssist assist) {
        framePara.FirstFrame = this.firstFrame;
        framePara.WriteData = assist.writeData;
        framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
        framePara.setHandWaitTime((int) this.handWaitTime);
        switch (assist.actionType) {
            case HexAction.ACTION_READ:
                assist = commServer.read(framePara, iComm, assist);
                break;
            case HexAction.ACTION_WRITE:
                assist = commServer.write(framePara, iComm, assist);
                break;
            case HexAction.ACTION_EXECUTE:
                assist = commServer.action(framePara, iComm, assist);
                break;
            case HexAction.ACTION_EXECUTE_READ:
                assist = commServer.actionAndRead(framePara, iComm, assist);
                break;
            default:
                assist.errMsg = "No Action Error";
                assist.aResult = false;
                break;
        }
        return assist;
    }

    /**
     * 执行
     *
     * @param obis      obis
     * @param writeData 发送数据
     * @param dataType  数据类型
     * @return bool
     * @deprecated use {@link #action(TranXADRAssist)} instead
     */
    public synchronized boolean action(String obis, String writeData, String dataType) {
        if (openSerial()) {
            framePara.OBISattri = obis;
            framePara.WriteData = writeData;
            framePara.FirstFrame = this.firstFrame;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.writeDataType = dataType;
            TranXADRAssist assist = commServer.action(framePara, iComm);
            closeSerial();
            return assist.aResult;
        }
        return false;
    }

    /**
     * 发送 16 进制数据
     *
     * @param assist 16进制数据
     * @return TranXADRAssist
     */
    public TranXADRAssist sendByte(TranXADRAssist assist) {
        boolean isSuccess = false;
        if (assist.isOpenSerial && openSerial()) {
            assist.errMsg = "openSerial is fail";
            isSuccess = true;
        } else if (!assist.isOpenSerial) {
            isSuccess = true;
        }
        if (framePara == null || iComm == null) {
            assist.errMsg = "iComm or framePara is null ";
            isSuccess = false;
        }
        if (TextUtils.isEmpty(assist.writeData)) {
            assist.errMsg = "data is empty";
            isSuccess = false;
        }
        if (isSuccess) {
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setSleepT((int) this.handWaitTime);
            framePara.setSleepT((int) this.sleepSend);
            assist = commServer.sendByte(framePara, iComm, assist);
            if (assist.isCloseSerial) {
                closeSerial();
            }
        }
        return assist;
    }

    /**
     * 发送 升级包内容
     * 注：开关 串口 需要主动调用
     *
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist sendUpgrade(TranXADRAssist assist) {
        boolean isSuccess = true;
        if (TextUtils.isEmpty(assist.writeData)) {
            assist.errMsg = "data is empty";
            isSuccess = false;
        } else if (framePara == null || iComm == null) {
            assist.errMsg = "Not init build para";
            isSuccess = false;
        }
        if (isSuccess) {
            assist.originalWriteData = assist.writeData;
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.setSleepT((int) this.sleepSend);
            assist.processWriteData = assist.originalWriteData;
            if (!TextUtils.isEmpty(assist.protocol)) {
                if (assist.protocol.equals(HexProtocol.PRO_21)) {
                    assist.processWriteData = HexStringUtil.upgradeEncrypt(assist.originalWriteData);
                    if (assist.needMoveData) {
                        assist.processWriteData = HexStringUtil.getDisplacement(assist.processWriteData);
                    }
                } else if (assist.protocol.equals(HexProtocol.PRO_DLMS)) {
                    assist.processWriteData = assist.originalWriteData;
                }
            }
            SystemClock.sleep(framePara.getSleepT());
            assist = commServer.sendUpgrade(framePara, iComm, assist);
            if (!TextUtils.isEmpty(assist.protocol)) {
                if (assist.protocol.equals(HexProtocol.PRO_21)) {
                    if (assist.needMoveAnalysis) {
                        //收到数据需要 &7F处理
                        assist.recStrData = HexStringUtil.moveRecData(assist.recStrData);
                    }
                    assist.aResult = HexStringUtil.decryptUpgrade(assist.recStrData);
                } else if (assist.protocol.equals(HexProtocol.PRO_DLMS)) {

                }
            }
        }
        return assist;
    }

    /**
     * 开启串口
     */
    public synchronized boolean openSerial() {
        boolean isOpenSuc = false;
        try {
            this.initPara();
            int pos = 0;
            while (!isOpenSuc && pos < 5) {
                //cPara.ComName = ("/dev/ttyUSB" + pos);
                iComm = new CommOpticalSerialPort();
                iComm = commServer.openDevice(cPara, iComm);
                if (iComm != null) {
                    isOpenSuc = true;
                    isTermination = false;
                    if (deviceType.equals(HexDevice.HT380A)) {
                        powerHT380AOn();
                    } else {
                        powerOn();
                    }
                    break;
                } else {
                    pos++;
                    cPara.ComName = cPara.ComName.substring(0, cPara.ComName.length() - 1) + pos;
                }

            }
            if (!isOpenSuc) {
                listener.onFailure("open Serial port fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
        }
        return isOpenSuc;
    }

    /**
     * 关闭串口
     */
    public synchronized void closeSerial() {
        isTermination = true;
        try {
            if (commServer != null && iComm != null) {
                commServer.DiscFrame(iComm);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listener.onFailure(ex.getMessage());
        }
        try {
            if (commServer != null && iComm != null) {
                commServer.close(iComm);
                iComm = null;
                commServer = null;
                if (deviceType.equals(HexDevice.HT380A)) {
                    powerHT380AOff();
                } else {
                    powerOff();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
        }
    }

    /**
     * 380A上电操作
     */
    public void powerHT380AOn() {
        try {
            RS232Controller.getInstance().Rs232_PowerOn();
            SystemClock.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 380A下电操作
     */
    public void powerHT380AOff() {
        try {
            RS232Controller.getInstance().Rs232_PowerOff();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上电操作
     */
    public void powerOn() {
        try {
            if (deviceControl == null) {
                deviceControl = new DeviceControl("/sys/class/misc/mtgpio/pin");
            }
            deviceControl.PowerOnDevice("-wdout94 1");
            SystemClock.sleep(1500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下电操作
     */
    public void powerOff() {
        if (deviceControl != null) {
            try {
                deviceControl.PowerOffDevice("-wdout94 0");
                deviceControl = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 时间拼接 用于冻结数据查询
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return String
     */
    public String writeTimeData(String startTime, String endTime) {
        String startYear, startMonth, startDay, endYear, endMonth, endDay;
        startTime = startTime.replace("-", "").replace(" ", "");
        endTime = endTime.replace("-", "").replace(" ", "");
        for (int i = startTime.length(); i < HexDataFormat.DEFAULT_DAY_NO_SEPRATOR_FORMAT.length(); i++) {
            startTime += "0";
        }
        for (int i = endTime.length(); i < HexDataFormat.DEFAULT_DAY_NO_SEPRATOR_FORMAT.length(); i++) {
            endTime += "0";
        }
        startYear = startTime.substring(0, 4);
        startMonth = startTime.substring(4, 6);
        startDay = startTime.substring(6, 8);

        endYear = endTime.substring(0, 4);
        endMonth = endTime.substring(4, 6);
        endDay = endTime.substring(6, 8);
        if (endDay.equals("00")) {
            Calendar cld = Calendar.getInstance();
            cld.set(Calendar.YEAR, Integer.parseInt(endYear));
            cld.set(Calendar.MONTH, Integer.parseInt(endMonth));
            endDay = String.valueOf(cld.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        String last = "FF800000";
        StringBuilder writeData = new StringBuilder();
        //结构体 4个元素
        writeData.append("01010204");

        // 第一个数据为结构体 4个元素
        writeData.append("0204");
        //Unsigned16 2个字节 class id 08  data_time(时间费率class id)
        writeData.append("120008");
        //octet-string 6个字节 obis data_time(时间费率obis)
        writeData.append("09060000010000FF");
        // integer8 一个字节 02 属性
        writeData.append("0F02");
        writeData.append("120000");
        //第2个元素 //octet-string 12个字节
        writeData.append("090C");
        writeData.append(String.format("%04x", Integer.parseInt(startYear) & 0xffff));
        writeData.append(String.format("%02x", Integer.parseInt(startMonth) & 0xff));
        writeData.append(String.format("%02x", Integer.parseInt(startDay) & 0xff));
        writeData.append("00");
        writeData.append("00");
        writeData.append("00");
        writeData.append("00");
        writeData.append(last);

        //第3个元素 //octet-string 12个字节
        writeData.append("090C");
        writeData.append(String.format("%04x", Integer.parseInt(endYear) & 0xffff));
        writeData.append(String.format("%02x", Integer.parseInt(endMonth) & 0xff));
        //日
        writeData.append(String.format("%02x", Integer.parseInt(endDay) & 0xff));
        //星期
        writeData.append("00");
        //时
        writeData.append(HexStringUtil.toHex(23));
        //分
        writeData.append(HexStringUtil.toHex(59));
        //秒
        writeData.append(HexStringUtil.toHex(59));
        writeData.append(last);
        //selected values 数据类型 数组 01 00获取全部
        writeData.append("0100");
        return writeData.toString();
    }

    /**
     * 测试通讯  直接发原始帧
     *
     * @param data 帧内容
     */
    public void sendTest(String data) {
        if (openSerial()) {
            boolean isSend = iComm.sendByt(HexStringUtil.hexToByte(data.toUpperCase()));
            if (isSend) {
                SystemClock.sleep(2000);
                byte[] bytes = iComm.receiveByt(5000, 3000);
                System.out.print("receiver=" + HexStringUtil.bytesToHexString(bytes));
            }
            closeSerial();
        }
    }

    /**
     * 断开链路层
     */
    public void discFrame() {
        try {
            if (commServer != null && iComm != null) {
                commServer.DiscFrame(iComm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(IHexListener listener) {
        this.listenerList.clear();
        this.listenerList.add(listener);
    }

    /**
     * 监听事件
     */
    private IHexListener listener = new IHexListener() {
        @Override
        public void onFailure(String msg) {
            for (IHexListener item : listenerList) {
                item.onFailure(msg);
            }
        }

        @Override
        public void onSuccess(Object object, int dataFormat) {
            for (IHexListener item : listenerList) {
                item.onSuccess(object, dataFormat);
            }
        }

        @Override
        public void onSuccess(byte[] bytes) {
            for (IHexListener item : listenerList) {
                item.onSuccess(bytes);
            }
        }

        @Override
        public void onSuccess(List<TranXADRAssist> dataList) {
            for (IHexListener item : listenerList) {
                item.onSuccess(dataList);
            }
        }

        @Override
        public void onSuccessBlock(List<List<TranXADRAssist>> blockList) {
            for (IHexListener item : listenerList) {
                item.onSuccessBlock(blockList);
            }
        }


        @Override
        public void onFinish() {
            for (IHexListener item : listenerList) {
                item.onFinish();
            }
        }

        @Override
        public void onSuccess(Object object) {
            for (IHexListener item : listenerList) {
                item.onSuccess(object);
            }
        }

        @Override
        public void onSuccess(TranXADRAssist data) {
            for (IHexListener item : listenerList) {
                item.onSuccess(data);
            }
        }

        @Override
        public void onSuccess(TranXADRAssist data, int pos) {
            for (IHexListener item : listenerList) {
                item.onSuccess(data, pos);
            }
        }
    };

}
