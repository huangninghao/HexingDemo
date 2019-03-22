package cn.hexing.iec21;


import android.os.SystemClock;
import android.text.TextUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.hexing.DeviceControl;
import cn.hexing.HexAction;
import cn.hexing.HexDevice;
import cn.hexing.HexHandType;
import cn.hexing.HexStringUtil;
import cn.hexing.IHexListener;
import cn.hexing.MeterType;
import cn.hexing.ParaConfig;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iec21.comm.CommOpticalSerialPort;
import cn.hexing.iec21.services.CommServer;
import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;


/**
 * @author caibinglong
 *         date 2018/2/1.
 *         desc 21 协议api
 */

public class HexClient21API {
    private static HexClient21API instance;
    private String deviceType = HexDevice.KT50;
    private int commMethod = HexDevice.METHOD_OPTICAL;
    private HXFramePara framePara;
    private AbsCommAction iComm = new CommOpticalSerialPort();
    private CommPara cPara = new CommPara();
    private CommServer commServer;
    private HXFramePara.AuthMode authMode;
    private HXFramePara.AuthMethod encryptionMode;
    private int baudRate = 300;
    private int nBits = 8;
    private int nStop = 1;
    private long sleepSend = 20;// Sleep time after send(ms)
    private String sVerify = "N"; // E 偶校验 N 无校验
    private long handWaitTime = 1200;
    private long dataFrameWaitTime = 1500;
    private boolean firstFrame = true;//是否第一帧
    private boolean isHands = true; //是否握手
    //8位数据位 是否转换 7位数据位发送
    private boolean isBitConversion;
    //收到数据 是否 & 7F 计算 适用于 数据位7 偶校验
    //收到数据有偏差需要每个byte & 0x7F 得到正确数据
    private boolean recDataConversion = false;
    private long sleepChangeBaudRate = 300;
    private byte enLevel = 0X00;
    private List<IHexListener> listenerList = new ArrayList<>();
    private String strMeterNo;
    private String strComName;
    private DeviceControl deviceControl;
    private boolean debugMode = false;
    private boolean isBaudRateTest = false;
    private int handType = HexHandType.HEXING;
    private int meterType = MeterType.IRAQ_SINGLE;
    private String strMeterPwd;

    public static HexClient21API getInstance() {
        if (instance == null) {
            instance = new HexClient21API();
        }
        return instance;
    }

    public static HexClient21API getInstance(ParaConfig config) {
        return instance = new HexClient21API(config);
    }

    public HexClient21API() {
    }

    public HexClient21API(ParaConfig config) {
        this.strMeterNo = config.strMeterNo;
        this.deviceType = config.deviceType;
        this.commMethod = config.commMethod;
        this.strComName = config.strComName;
        this.baudRate = config.baudRate;
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
        this.strMeterPwd = config.strMeterPwd;
        this.sleepSend = config.sleepSendTime;
        this.isBitConversion = config.isBitConversion;
        this.recDataConversion = config.recDataConversion;
        this.debugMode = config.debugMode;
        this.isBaudRateTest = config.isBaudRateTest;
        this.sleepChangeBaudRate = config.changeBaudRateSleepTime;
        this.handType = config.handType;
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

    /**
     * 设置波特率
     *
     * @param baudRate int
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
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

    /**
     * 初始化参数设置
     *
     * @throws Exception
     */
    private void initPara() throws Exception {
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
                    //this.baudRate = this.baudRate;
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
        }

        cPara.BRate = baudRate;
        cPara.DBit = nBits;
        cPara.Pty = sVerify.charAt(0);
        cPara.Sbit = nStop;
        //7位数据位 偶校验  需要移位处理
        framePara.isBitConversion = this.isBitConversion;
        framePara.recDataConversion = this.recDataConversion;
        // 通讯参数
        framePara.FirstFrame = this.firstFrame;// = true;
        framePara.isHands = this.isHands;
        framePara.Mode = this.authMode == null ? HXFramePara.AuthMode.HLS : this.authMode;
        framePara.enLevel = this.enLevel;
        framePara.SourceAddr = 0x03;
        framePara.sleepChangeBaudRate = this.sleepChangeBaudRate;
        if (commMethod == HexDevice.METHOD_RF) {
            framePara.strMeterNo = TextUtils.isEmpty(this.strMeterNo) ? "254455455" : this.strMeterNo;
        } else if (commMethod == HexDevice.METHOD_OPTICAL) {
            framePara.strMeterNo = "";
        }
        framePara.setSleepT((int) this.sleepSend);
        framePara.ByteWaitT = 1500;
        framePara.Pwd = this.strMeterPwd;
        framePara.aesKey = new byte[16];
        framePara.auKey = new byte[16];
        framePara.enKey = new byte[16];
        framePara.setStrsysTitleC("4845430005000001");
        framePara.encryptionMethod = this.encryptionMode == null ? HXFramePara.AuthMethod.AES_GCM_128 : this.encryptionMode;
        framePara.sysTitleS = new byte[8];
        framePara.MaxSendInfo_Value = 255;
        framePara.baudRateTest = this.isBaudRateTest;
        framePara.handType = this.handType;
        framePara.meterType = this.meterType;
    }

    /**
     * 读取 写 执行
     *
     * @param tranXADRAssist 对象集合
     */
    public synchronized void action(List<TranXADRAssist> tranXADRAssist) {
        if (openSerial()) {
            framePara.setDataFrameWaitTime((int) this.dataFrameWaitTime);
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.FirstFrame = this.firstFrame;
            framePara.setSleepT((int) this.sleepSend);
            int pos = 0;

            while (tranXADRAssist.size() > 0) {
                TranXADRAssist assist = tranXADRAssist.get(0);
                assist.processWriteData = assist.writeData;
                assist.originalWriteData = assist.writeData;

                if (!TextUtils.isEmpty(assist.obis)) {
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
                            assist = commServer.write(framePara, iComm, assist);
                            break;
                        case HexAction.ACTION_EXECUTE:
                            assist = commServer.action(framePara, iComm);
                            break;
                    }
                    if (assist.aResult) {
                        framePara.FirstFrame = false;
                        framePara.isHands = false;
                    }
                } else {
                    assist.aResult = false;
                    assist.value = "-1";
                    assist.errMsg = "obis is empty";
                }
                listener.onSuccess(assist, pos);
                tranXADRAssist.remove(0);
                pos++;
                SystemClock.sleep(framePara.getSleepT());
            }
            closeSerial();
        }
    }

    /**
     * 简单直接读取数据
     *
     * @param assist TranXADRAssist
     * @return String
     */
    public synchronized String read(TranXADRAssist assist) {
        if (openSerial()) {
            assist = commServer.read(framePara, iComm);
            closeSerial();
            return assist.value;
        }
        return assist.value;
    }

    /**
     * 发送 16 进制数据
     *
     * @param assist 16进制数据 存储在 TranXADRAssist.writeData 对象
     * @return TranXADRAssist 返回接收的 16进制数据 TranXADRAssist.value
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
            framePara.setHandWaitTime((int) this.handWaitTime);
            framePara.setSleepT((int) this.sleepSend);
            framePara.sleepChangeBaudRate = this.sleepChangeBaudRate;
            assist = commServer.sendByte(framePara, iComm, assist);
            if (assist.isCloseSerial) {
                closeSerial();
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
                //cPara.ComName = strComName.substring(0, strComName.length() - 1) + pos;
                iComm = new CommOpticalSerialPort();
                iComm = commServer.openDevice(cPara, iComm);
                if (iComm != null) {
                    isOpenSuc = true;
                    pos = 0;
                    powerOn();
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
     * 关闭串口
     */
    public synchronized void closeSerial() {
        try {
            if (commServer != null && iComm != null) {
                commServer.DiscFrame(framePara, iComm);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            listener.onFailure(ex.getMessage());
        }
        try {
            if (commServer != null && iComm != null) {
                commServer.close(iComm);
                powerOff();
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
        }
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
        public void onFinish() {
            for (IHexListener item : listenerList) {
                item.onFinish();
            }
        }

        @Override
        public void onSuccess(Object object) {

        }

        @Override
        public void onSuccess(TranXADRAssist data, int pos) {
            for (IHexListener item : listenerList) {
                item.onSuccess(data, pos);
            }
        }
    };


}
