package cn.hexing.dlt645;


import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cn.hexing.DeviceControl;
import cn.hexing.HexAction;
import cn.hexing.HexDevice;
import cn.hexing.HexStringUtil;
import cn.hexing.ParaConfig;
import cn.hexing.dlms.HexDataFormat;
import cn.hexing.dlms.IHexListener;
import cn.hexing.dlms.RS232Controller;
import cn.hexing.dlt645.check.FrameCheckerFilterTypes;
import cn.hexing.dlt645.comm.CommOpticalSerialPort;
import cn.hexing.dlt645.model.DayBlockBean;
import cn.hexing.dlt645.model.EthernetBean;
import cn.hexing.dlt645.model.GprsBean;
import cn.hexing.dlt645.model.InstantaneousBean;
import cn.hexing.dlt645.model.MeterRelayBean;
import cn.hexing.dlt645.model.MeterSetupBean;
import cn.hexing.dlt645.model.PrePaymentBean;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;


/**
 * @author caibinglong
 *         date 2018/2/1.
 *         desc desc
 */

public class HexClient645API {
    private static HexClient645API instance;
    private String deviceType = HexDevice.KT50;
    private int commMethod = HexDevice.METHOD_ZIGBEE;
    private HXFramePara framePara;
    private AbsCommAction iComm;
    private CommPara cPara = new CommPara();
    private CommServer commServer;
    private int baudRate = 9600;
    private int nBits = 8;
    private int nStop = 1;
    private long sleepSend = 20;// Sleep time after send(ms)
    private String sVerify = "N";
    private long handWaitTime = 1200;
    private long dataFrameWaitTime = 3000;
    private boolean isTermination = false; //是否终止 读取obis
    private List<IHexListener> listenerList = new ArrayList<>();
    private String strComName;
    private ParaConfig config;
    private boolean debugMode = false;
    private DeviceControl deviceControl;
    private final static String TAG = HexClient645API.class.getSimpleName();
    private RS232Controller rs232Controller;

    public static HexClient645API getInstance() {
        if (instance == null) {
            instance = new HexClient645API();
        }
        return instance;
    }

    public static boolean getDebugMode() {
        if (instance == null) {
            return false;
        }
        return instance.debugMode;
    }

    public static HexClient645API getInstance(ParaConfig config) {
        return instance = new HexClient645API(config);
    }

    public HexClient645API() {
    }

    public HexClient645API(ParaConfig config) {
        this.config = config;
        this.deviceType = config.deviceType;
        this.commMethod = config.commMethod;
        this.strComName = config.strComName;
        this.baudRate = config.baudRate;
        this.nBits = config.nBits;
        this.nStop = config.nStop;
        this.sVerify = config.sVerify;
        this.handWaitTime = config.handWaitTime;
        this.dataFrameWaitTime = config.dataFrameWaitTime;
        this.sleepSend = config.sleepSendTime;
        this.debugMode = config.debugMode;
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

    public void setHandWaitTime(long waitTime) {
        this.handWaitTime = waitTime;
    }

    public void setDataFrameWaitTime(long waitTime) {
        this.dataFrameWaitTime = waitTime;
    }

    public void initPara() throws Exception {
        // 光电头通讯参数设置
        iComm = new CommOpticalSerialPort();
        commServer = new CommServer();
        commServer.setDebugMode(this.debugMode);
        framePara = new HXFramePara();

        if (deviceType.equals(HexDevice.KT50)) {
            switch (commMethod) {
                case HexDevice.METHOD_ZIGBEE:
                    iComm = new CommOpticalSerialPort();
                    cPara.ComName = TextUtils.isEmpty(strComName) ? HexDevice.COMM_NAME_ZIGBEE : strComName;
                    framePara.CommDeviceType = HexDevice.ZIGBEE;
                    this.baudRate = 9600;
                    break;
                default:
                    throw new NotImplementedException("通讯 " + commMethod + " 未定义");
            }
        } else if (deviceType.equals(HexDevice.HT380A)) {
            //380A
            cPara.ComName = HexDevice.COMM_NAME_SAC;
            iComm = new CommOpticalSerialPort();
            framePara.CommDeviceType = HexDevice.OPTICAL;
            rs232Controller = new RS232Controller();
        }

        cPara.BRate = baudRate;
        cPara.DBit = nBits;
        cPara.Pty = sVerify.charAt(0);
        cPara.Sbit = nStop;
    }

    /**
     * 645 对外api
     *
     * @param tranXADRAssist TranXADRAssist
     */
    public void action(TranXADRAssist tranXADRAssist) throws NotImplementedException {
        if (tranXADRAssist.c645Bean == null) {
            throw new NotImplementedException("645 config is null");
        }
        if (openSerial()) {
            List<TranXADRAssist> assists;
            TranXADRAssist assist = tranXADRAssist.clone();
            TranXADRAssist xadrAssist;
            ReceiveModel model = new ReceiveModel();
            model.maxWaitTime = assist.c645Bean.maxWaitTime;
            model.sleepTime = assist.c645Bean.waitReceiveTime;
            int pos = 0;
            switch (assist.c645Bean.getMeterDataType645Id()) {
                case MeterDataTypes.CustomerSearchNetWork:
                    assists = new ArrayList<>();
                    //maxWaitTime 2000
                    GlobalCommunicators.zigbeeCommandExecutor.Write("SR", new byte[0], model);
                    if (debugMode) {
                        System.out.println("SR action over");
                    }
                    List<ReceiveModel> models = GlobalCommunicators.zigbeeCommandExecutor.ReadNetWork();
                    for (ReceiveModel item : models) {
                        xadrAssist = assist.clone();
                        xadrAssist.recBytes = item.recBytes;
                        xadrAssist.value = item.data;
                        assists.add(xadrAssist);
                    }
                    listener.onSuccess(assists);
                    break;
                case MeterDataTypes.CustomerConnectCollector:
                    //连接采集器
                    //waitTime = 2000;
                    if (TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
                        throw new NotImplementedException("CustomerConnectCollector collector number is null");
                    }
                    assist = connectCollectorNew(assist.c645Bean.collectorNumber, model);
                    if (assist.aResult) {
                        listener.onSuccess(assist, 0);
                    } else {
                        listener.onFailure(assist.errMsg);
                    }
                    break;
                case MeterDataTypes.CustomerMeterRelayStatus:
                    //表 操作  继电器状态
                    executeReadRelay(assist, model);
                    //listener.onSuccess(assists);
                    break;
                case MeterDataTypes.CustomerActionMeterRelay:
                    String meterNo;
                    while (assist.c645Bean.meterNumberList.size() > 0) {
                        meterNo = assist.c645Bean.meterNumberList.get(0);
                        if (TextUtils.isEmpty(meterNo)) {
                            throw new NotImplementedException("CustomerActionMeterRelay meter number is null");
                        }
                        byte[] bytes = HexStringUtil.hexToByte(HexStringUtil.padLeft(meterNo, 12, '0'));
                        byte[] newByte = HexStringUtil.reverse(bytes);
                        GlobalCommunicators.c645Address = HexStringUtil.bytesToHexString(newByte);
                        GlobalCommunicators.Update();
                        model = GlobalCommunicators.c645ZigbeeMeter.Write(assist.c645Bean.relayAction ? MeterDataTypes.SwitchOn : MeterDataTypes.SwitchOff,
                                GlobalCommunicators.MeterPasswordBytes, new byte[0], model);
                        assist.recBytes = model.recBytes;
                        assist.aResult = model.isSuccess;
                        assist.c645Bean.meterNumberList.remove(0);
                        listener.onSuccess(assist, pos);
                        pos++;
                    }
                    break;
                case MeterDataTypes.CustomerReadMeter:
                    //maxWaitTime = 10*1000;
                    if (TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
                        throw new NotImplementedException("CustomerReadMeter collector number is null");
                    }
                    assist = readMeterSetupInfoNew(assist, model);
                    if (assist.aResult) {
                        listener.onSuccess(assist, 0);
                    } else {
                        listener.onFailure(assist.errMsg);
                    }
                    break;
                case MeterDataTypes.CustomerExecuteSearchMeter:
                    //maxWaitTime = 2000;
                    if (TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
                        throw new NotImplementedException("CustomerExecuteSearchMeter collector number is null");
                    }
                    GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
                    GlobalCommunicators.Update();
                    System.out.println(TAG + "Run Meter Search Start  ...");
                    model = GlobalCommunicators.c645ZigbeeCollector.Write(MeterDataTypes.SetupMode, GlobalCommunicators.CollectorPasswordBytes, new byte[]{0x02}, model);
                    System.out.println(TAG + "Run Meter Search end  ..." + model.isSend);
                    assist.recBytes = model.recBytes;
                    assist.aResult = model.isSuccess;
                    listener.onSuccess(assist, 0);
                    break;
                case MeterDataTypes.CustomerCollGPRS:
                    //采集器 gprs操作
                    if (TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
                        throw new NotImplementedException("CustomerCollGPRS collector number is null");
                    }
                    System.out.println(TAG + "collector gprs  ...");
                    if (assist.actionType == HexAction.ACTION_READ) {
                        //读gprs
                        assist = readGPRS(assist, model);
                    } else if (assist.actionType == HexAction.ACTION_WRITE) {
                        //写gprs
                        assist = writeGPRS(assist, model);
                    }
                    listener.onSuccess(assist, 0);
                    break;
                case MeterDataTypes.CustomerCollIEthernet:
                    if (TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
                        throw new NotImplementedException("CustomerCollIEthernet collector number is null");
                    }
                    //采集器 网络设置
                    if (assist.actionType == HexAction.ACTION_READ) {
                        //读
                        assist = readEthernet(assist);
                    } else if (assist.actionType == HexAction.ACTION_WRITE) {
                        //写
                        try {
                            assist = writeEthernetParameters(assist, model);
                        } catch (Exception e) {
                            assist.errMsg = e.getMessage();
                            e.printStackTrace();
                        }
                    }
                    listener.onSuccess(assist, 0);
                    break;

                case MeterDataTypes.CustomerCollClearMeter:
                    // 采集器  清表 操作
                    if (TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
                        throw new NotImplementedException("collector number is null");
                    }
                    assist = executeCleatMeter(assist.c645Bean.collectorNumber, model);
                    listener.onSuccess(assist, 0);
                    break;

                case MeterDataTypes.CustomerMeterDaily:
                    assist = readDayBlock(assist, model);
                    //listener.onSuccess(assist, 0);
                    // 表 冻结
                    break;
                case MeterDataTypes.CustomerMeterDailyPre:
                    assist = readDayBlockPrePayment(assist, model);
                    //listener.onSuccess(assist, 0);
                    //表 冻结 预付费
                    break;

                case MeterDataTypes.CustomerMeterInstantaneous:
                    //表  瞬时量
                    assist = readInstantaneous(assist, model);
                    //listener.onSuccess(assist, 0);
                    break;

                case MeterDataTypes.CustomerMeterPowerMode:
                    //表  保电模式
                    System.out.println("Enter Power-On Mode Start ...");
                    int i = 0;
                    for (String meter : assist.c645Bean.meterNumberList) {
                        if (debugMode) {
                            System.out.println("Read Enter Power-On Mode||" + assist.c645Bean.collectorNumber + "--" + meter);
                        }
                        byte[] bytes = HexStringUtil.hexToByte(HexStringUtil.padLeft(meter, 12, '0'));
                        byte[] newByte = HexStringUtil.reverse(bytes);
                        GlobalCommunicators.c645Address = HexStringUtil.bytesToHexString(newByte);
                        GlobalCommunicators.Update();
                        model = GlobalCommunicators.c645ZigbeeMeter.Write(assist.c645Bean.relayAction ? MeterDataTypes.RelayOn : MeterDataTypes.RelayOff,
                                GlobalCommunicators.MeterPasswordBytes, new byte[]{}, model);

                        if (!model.isSuccess) {
                            model = GlobalCommunicators.c645ZigbeeMeter.Write(assist.c645Bean.relayAction ? MeterDataTypes.RelayOn : MeterDataTypes.RelayOff,
                                    GlobalCommunicators.MeterPasswordBytes, new byte[]{}, model);
                        }
                        assist.aResult = model.isSuccess;
                        assist.recBytes = model.recBytes;
                        assist.errMsg = model.errorMsg;
                        listener.onSuccess(assist, i);
                        i++;
                    }
                    System.out.println("All finished");
                    break;
                case MeterDataTypes.CustomerCollPlanID:
                    if (assist.actionType == HexAction.ACTION_READ) {
                        //读
                        assist = readCLTPanID(assist, model);
                    } else if (assist.actionType == HexAction.ACTION_WRITE) {
                        //写
                        assist = writeCLTPanID(assist, model);
                    }
                    listener.onSuccess(assist, 0);
                    break;
                case MeterDataTypes.CustomerCollExit:
                    assist = executeDisconnect(assist.c645Bean.collectorNumber, model);
                    listener.onSuccess(assist, 0);
                    break;
                case MeterDataTypes.CustomerCOllInstallMode:
                    assist = executeInstall(assist, model);
                    listener.onSuccess(assist, 0);
                    break;
                default:
                    throw new NotImplementedException("MeterDataTypes " + assist.c645Bean.getMeterDataType645Id() + " Undefined");

            }
            closeSerial();
        }
    }

    /**
     * 读取 表安装信息
     *
     * @param assist TranXADRAssist
     */
    private TranXADRAssist readMeterSetupInfoNew(TranXADRAssist assist, ReceiveModel model) {
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        System.out.println("Read MeterInfo ReadSetupInfo1" + " Start  ...");
        model.setReadType(MeterDataTypes.ReadSetupInfo); //= -8999;//MeterDataTypes.ReadSetupInfo;
        model.checkFilter.add(FrameCheckerFilterTypes.C645ZigbeeReceivedData);
        model = GlobalCommunicators.c645ZigbeeCollector.Read(model);
        assist.recBytes = model.recBytes;
        MeterSetupBean meterSetupBean;
        assist.value = assist.c645Bean.collectorNumber;
        assist.aResult = model.isSuccess;
        assist.errMsg = model.errorMsg;
        if (model.isSuccess) {
            byte[] meterList1 = model.recBytes.clone();
            if (debugMode) {
                System.out.println("ReadSetupInfo2 start");
            }
            model.setReadType(MeterDataTypes.ReadSetupInfo2);
            model = GlobalCommunicators.c645ZigbeeCollector.Read(model);
            byte[] meterList2 = new byte[0];
            if (model.isSuccess) {
                meterList2 = model.recBytes.clone();
            } else {
                assist.errMsg = "8-16 meter read fail||" + model.errorMsg;
            }
            byte[] newMeter = new byte[meterList1.length + meterList2.length];
            System.arraycopy(meterList1, 0, newMeter, 0, meterList1.length);
            System.arraycopy(meterList2, 0, newMeter, meterList1.length, meterList2.length);
            assist.c645Bean.meterSetupBeanList.clear();
            assist.recStrData = HexStringUtil.bytesToHexString(newMeter);

            byte[] item = new byte[17];
            if (newMeter.length >= 17) {
                int num = newMeter.length / 17;
                for (int i = 0; i < num; i++) {
                    System.arraycopy(newMeter, i * 17, item, 0, 17);
                    byte[] meterNo = HexStringUtil.getBytes(item, 0, 6);
                    meterNo = HexStringUtil.reverse(meterNo); //反序
                    meterSetupBean = new MeterSetupBean();
                    meterSetupBean.meterNo = HexStringUtil.bytesToHexString(meterNo);
                    meterSetupBean.position = String.valueOf((item[6] & 0xff) +1);
                    assist.c645Bean.meterSetupBeanList.add(meterSetupBean);
                }
            }
        }
        return assist;
    }

    /**
     * 连接采集器
     *
     * @param collectorNo String
     * @return TranXADRAssist
     */
    private synchronized TranXADRAssist connectCollectorNew(String collectorNo, ReceiveModel model) {
        TranXADRAssist assist = new TranXADRAssist();
        GlobalCommunicators.c645Address = "AAAAAAAAAAAA";
        GlobalCommunicators.Update();
        model.receiveByteLen = 15;
        model = GlobalCommunicators.zigbeeCommandExecutor.Write("LH", new byte[0], model);
        System.out.println("LH send finish'");
        if (!model.isSuccess) {
            assist.errMsg = "LH write fail";
            assist.aResult = false;
            return assist;
        }
        model.receiveByteLen = 9;
        model = GlobalCommunicators.zigbeeCommandExecutor.Write("NR", new byte[0], model);
        System.out.println("NR send finish'");
        if (!model.isSuccess) {
            assist.errMsg = "NR write fail";
            assist.aResult = false;
            return assist;
        }
        model.receiveByteLen = 15;
        model = GlobalCommunicators.zigbeeCommandExecutor.Write("SD", new byte[]{0x01}, model);
        System.out.println("SD send finish'");
        if (!model.isSuccess) {
            assist.errMsg = "SD write fail";
            assist.aResult = false;
            return assist;
        }
        model.receiveByteLen = 15;
        model = GlobalCommunicators.zigbeeCommandExecutor.Write("WR", new byte[0], model);
        System.out.println("WR send finish'");
        if (!model.isSuccess) {
            assist.errMsg = "WR write fail";
            assist.aResult = false;
            return assist;
        }
        model.receiveByteLen = 9;
        model = GlobalCommunicators.zigbeeCommandExecutor.Write("ID", HexStringUtil.hexToByte(HexStringUtil.padRight(collectorNo, 16, '0')), model);
        System.out.println("ID send finish'");
        if (!model.isSuccess) {
            assist.errMsg = "ID write fail";
            assist.aResult = false;
            return assist;
        }
        int i = 0;
        while (i < 3) {
            model = GlobalCommunicators.zigbeeCommandExecutor.Write("AI", new byte[0], model);
            System.out.println("AI send finish'");
            if (model.isSuccess && ((model.recBytes[model.recBytes.length - 1] & 0xff) == 0xEC)) {
                break;
            }
            SystemClock.sleep(2000);
            i++;
        }
        if (!model.isSuccess) {
            assist.errMsg = "AI write fail";
            assist.aResult = false;
            return assist;
        }
        assist.recBytes = model.recBytes;
        assist.errMsg = model.errorMsg;
        assist.aResult = true;
        return assist;
    }

    //清表
    private TranXADRAssist executeCleatMeter(String collectorNo, ReceiveModel model) {
        TranXADRAssist tranXADRAssist = new TranXADRAssist();
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(collectorNo, 12, 'F');
        GlobalCommunicators.Update();
        model.setReadType(MeterDataTypes.SetupMode);
        model = GlobalCommunicators.c645ZigbeeCollector.Write(MeterDataTypes.SetupMode,
                GlobalCommunicators.CollectorPasswordBytes, new byte[]{0x04}, model);
        if (model.isSuccess) {
            tranXADRAssist.recBytes = model.recBytes;
            tranXADRAssist.aResult = true;
        } else {
            tranXADRAssist.errMsg = model.errorMsg;
            tranXADRAssist.aResult = false;
        }
        return tranXADRAssist;
    }

    /**
     * 读取电表 继电器 状态 读取
     *
     * @param assist 采集器号
     * @param model  ReceiveModel
     * @return List<TranXADRAssist>
     */
    private List<TranXADRAssist> executeReadRelay(TranXADRAssist assist, ReceiveModel model) {
        List<TranXADRAssist> tranXADRAssist = new ArrayList<>();
        System.out.println("Read Meter Relay Status Start  ...");
        int i = 0;
        MeterRelayBean item;
        for (String meter : assist.c645Bean.meterNumberList) {
            if (debugMode) {
                System.out.println(meter + ":" + "Position" + ":" + String.valueOf(i));
            }
            byte[] bytes = HexStringUtil.hexToByte(HexStringUtil.padLeft(meter, 12, '0'));
            byte[] newByte = HexStringUtil.reverse(bytes);
            GlobalCommunicators.c645Address = HexStringUtil.bytesToHexString(newByte);
            GlobalCommunicators.Update();
            model.setReadType(MeterDataTypes.ReadRelayStatus);
            model = GlobalCommunicators.c645ZigbeeMeter.Read(model);
            assist.errMsg = model.errorMsg;
            assist.aResult = model.isSuccess;
            assist.recBytes = model.recBytes;
            if (model.isSuccess) {
                item = new MeterRelayBean();
                item.collectorTime = HexStringUtil.getNowTime();
                item.collectorNo = assist.c645Bean.collectorNumber;
                item.meterNo = meter;
                item.meterPosition = String.valueOf(i);

                byte temp = model.recBytes[2];
                item.relayStatus = HexStringUtil.byteToString(temp);
                item.relayStatus = OperationReasons.getRelayStatus(item.relayStatus);

                temp = model.recBytes[3];
                item.meterMode = HexStringUtil.byteToString(temp);
                item.meterMode = MeterModes.GetMeterModeText(item.meterMode);

                temp = model.recBytes[4];
                item.relayReason = HexStringUtil.byteToString(temp);
//                //解析数据
                item.relayReason = OperationReasons.GetRelayOperationReason(item.relayReason);
                assist.c645Bean.relayBean = item;
            }
            listener.onSuccess(assist, i);
            i++;
        }

        System.out.println("ALL finished");
        return tranXADRAssist;
    }

    /// <summary>
    /// 0x01 退出安装模式  0x00 进入安装模式
    /// </summary>
    /// <returns></returns>
    private TranXADRAssist executeInstall(TranXADRAssist assist, ReceiveModel model) {
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        //0x01 退出安装模式   0x00 进入安装模式
        model.setReadType(MeterDataTypes.SetupMode);
        model = GlobalCommunicators.c645ZigbeeCollector.Write(MeterDataTypes.SetupMode,
                GlobalCommunicators.CollectorPasswordBytes, assist.c645Bean.relayAction ? new byte[]{0x01} : new byte[]{0x00}, model);
        assist.aResult = model.isSuccess;
        assist.errMsg = model.errorMsg;
        assist.recBytes = model.recBytes;
        return assist;
    }

    /**
     * 断开连接 采集器
     *
     * @param collectorNo 采集器号
     * @param model       数据接收
     * @return TranXADRAssist
     */
    private TranXADRAssist executeDisconnect(String collectorNo, ReceiveModel model) {
        TranXADRAssist assist = new TranXADRAssist();
        GlobalCommunicators.c645Address = "AAAAAAAAAAAA";
        GlobalCommunicators.Update();
        System.out.println("Disconnect Start : " + collectorNo + " ...");
        model = GlobalCommunicators.zigbeeCommandExecutor.Write("LN", new byte[0], model);
        assist.errMsg = model.errorMsg;
        assist.aResult = model.isSuccess;
        assist.recBytes = model.recBytes;
        assist.recStrData = HexStringUtil.bytesToHexString(model.recBytes);
        return assist;
    }

    /**
     * 读取 采集器 gprs
     *
     * @param assist 采集器号
     * @param model  ReceiveModel
     * @return TranXADRAssist
     */
    private TranXADRAssist readGPRS(TranXADRAssist assist, ReceiveModel model) {
        assist.c645Bean.gprsBean = new GprsBean();

        System.out.println(TAG + "Read ReadGPRSParameters||" + assist.c645Bean.collectorNumber);
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();

        model.setReadType(MeterDataTypes.ReadGPRS);
        model = GlobalCommunicators.c645ZigbeeCollector.Read(model);
        assist.recBytes = model.recBytes;
        assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
        assist.errMsg = model.errorMsg;
        assist.aResult = model.isSuccess;
        if (model.isSuccess && model.recBytes.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            byte[] srcData = model.recBytes;

            //apn
            int apnLen = srcData[0] & 0xff;
            byte[] temp = new byte[apnLen];
            System.arraycopy(srcData, 1, temp, 0, apnLen);
            assist.c645Bean.gprsBean.apn = HexStringUtil.convertHexToString(HexStringUtil.bytesToHexString(temp));

            byte[] newData = new byte[srcData.length - apnLen - 1];
            System.arraycopy(srcData, apnLen + 1, newData, 0, newData.length);
            srcData = Arrays.copyOf(newData, newData.length);

            // stationIP 4个字节
            int ipLen = 4;
            temp = new byte[ipLen];
            System.arraycopy(srcData, 0, temp, 0, ipLen);

            String[] arr = new String[4];
            for (int i = 0; i < temp.length; i++) {
                arr[i] = String.valueOf(temp[i] & 0xff);
            }
            assist.c645Bean.gprsBean.ip = TextUtils.join(".", arr);

            newData = new byte[srcData.length - ipLen];
            System.arraycopy(srcData, ipLen, newData, 0, newData.length);
            srcData = Arrays.copyOf(newData, newData.length);

            //port 倒序
            int portLen = 2;
            stringBuilder.append(String.format("%02X", (srcData[1] & 0xff)));
            stringBuilder.append(String.format("%02X", (srcData[0] & 0xff)));
            assist.c645Bean.gprsBean.port = Integer.parseInt(stringBuilder.toString(), 16);

            newData = new byte[srcData.length - portLen];
            System.arraycopy(srcData, portLen, newData, 0, newData.length);
            srcData = Arrays.copyOf(newData, newData.length);

            // pdp userName
            int pdpUNameLen = srcData[0] & 0xff;
            temp = new byte[pdpUNameLen];
            System.arraycopy(srcData, 1, temp, 0, temp.length);
            assist.c645Bean.gprsBean.PDPUserName = HexStringUtil.convertHexToString(HexStringUtil.bytesToHexString(temp));

            newData = new byte[srcData.length - pdpUNameLen - 1];
            System.arraycopy(srcData, pdpUNameLen + 1, newData, 0, newData.length);
            srcData = Arrays.copyOf(newData, newData.length);

            //pdp userPassword
            int pdpUPassLen = srcData[0] & 0xff;
            temp = new byte[pdpUPassLen];
            System.arraycopy(srcData, 1, temp, 0, temp.length);
            assist.c645Bean.gprsBean.PDPUserPassword = HexStringUtil.convertHexToString(HexStringUtil.bytesToHexString(temp));

            newData = new byte[srcData.length - pdpUPassLen - 1];
            System.arraycopy(srcData, pdpUPassLen + 1, newData, 0, newData.length);
            srcData = Arrays.copyOf(newData, newData.length);

            // sms number
            int smsNumLen = srcData[0] & 0xff;
            temp = new byte[smsNumLen];
            System.arraycopy(srcData, 1, temp, 0, temp.length);
            assist.c645Bean.gprsBean.SMSNumber = HexStringUtil.convertHexToString(HexStringUtil.bytesToHexString(temp));
        }
        return assist;
    }

    /**
     * gprs 写  采集器
     *
     * @return TranXADRAssist
     */
    private TranXADRAssist writeGPRS(TranXADRAssist assist, ReceiveModel model) {
        if (assist.c645Bean == null || TextUtils.isEmpty(assist.c645Bean.collectorNumber)) {
            System.out.println(TAG + "write gprs parameter is null");
            return assist;
        }
        if (debugMode) {
            System.out.println(TAG + "WriteGPRSParameters||" + assist.c645Bean.collectorNumber);
        }
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        if (assist.c645Bean.gprsBean == null) {
            assist.errMsg = "GPRS Parameters is null";
            assist.aResult = false;
            return assist;
        }
        StringBuilder stringBuilder = new StringBuilder();
        GprsBean gprsBean = assist.c645Bean.gprsBean;
        String[] ips = gprsBean.ip.split("\\.");

        stringBuilder.append(String.format("%02X", gprsBean.apn.length()));
        stringBuilder.append(HexStringUtil.parseAscii(gprsBean.apn));

        stringBuilder.append(String.format("%02X", Integer.parseInt(ips[0])));
        stringBuilder.append(String.format("%02X", Integer.parseInt(ips[1])));
        stringBuilder.append(String.format("%02X", Integer.parseInt(ips[2])));
        stringBuilder.append(String.format("%02X", Integer.parseInt(ips[3])));

        String temp = String.format("%04X", gprsBean.port);
        stringBuilder.append(temp.substring(2, 4));
        stringBuilder.append(temp.substring(0, 2));

        stringBuilder.append(String.format("%02X", gprsBean.PDPUserName.length()));
        stringBuilder.append(HexStringUtil.parseAscii(gprsBean.PDPUserName));

        stringBuilder.append(String.format("%02X", gprsBean.PDPUserPassword.length()));
        stringBuilder.append(HexStringUtil.parseAscii(gprsBean.PDPUserPassword));

        stringBuilder.append(String.format("%02X", gprsBean.SMSNumber.length()));
        stringBuilder.append(HexStringUtil.parseAscii(gprsBean.SMSNumber));
        model.setReadType(MeterDataTypes.ReadGPRS);
        model = GlobalCommunicators.c645ZigbeeCollector.Write(MeterDataTypes.ReadGPRS, GlobalCommunicators.CollectorPasswordBytes, HexStringUtil.hexToByte(stringBuilder.toString()), model);
        assist.aResult = model.isSuccess;
        assist.errMsg = model.errorMsg;
        assist.recBytes = model.recBytes;
        assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);

        return assist;
    }

    /**
     * 瞬时量 读取
     *
     * @param assist TranXADRAssist 调用参数模型
     * @param model  接收数据模型
     * @return TranXADRAssist
     */
    private TranXADRAssist readInstantaneous(TranXADRAssist assist, ReceiveModel model) {
        String Power = "";
        String Voltage1 = "";
        String Voltage2 = "";
        String Voltage3 = "";
        String Current1 = "";
        String Current2 = "";
        String Current3 = "";
        String Frequency = "";
        String PowerFactor = "";
        String ReactivePower = "";
        InstantaneousBean insitem = new InstantaneousBean();
        StringBuilder builder;
        System.out.println(TAG + "Read Instantaneous Start  ...");
        int i = 0;
        for (String meter : assist.c645Bean.meterNumberList) {
            System.out.println(TAG + "Read Instantaneous Value||" + assist.c645Bean.collectorNumber + "--" + meter);
            boolean singlePhase = false;
            byte[] bytes = HexStringUtil.hexToByte(HexStringUtil.padLeft(meter, 12, '0'));
            byte[] newByte = HexStringUtil.reverse(bytes);
            GlobalCommunicators.c645Address = HexStringUtil.bytesToHexString(newByte);
            GlobalCommunicators.Update();

            model.setReadType(MeterDataTypes.ReadInstantaneous);
            model = GlobalCommunicators.c645ZigbeeMeter.Read(model, "");
            if (!model.isSuccess) {
                model.setReadType(MeterDataTypes.ReadInstantaneous_M);
                model = GlobalCommunicators.c645ZigbeeMeter.Read(model, "");
                if (model.isSuccess) {
                    singlePhase = true;
                }
            }
            assist.recBytes = model.recBytes;
            assist.errMsg = model.errorMsg;
            assist.aResult = model.isSuccess;
            if (model.isSuccess && model.recBytes.length > 0) {
                byte[] receiveData = model.recBytes;
                if (receiveData.length <= 5) {
                    System.out.println("NO DATA");
                } else {
                    if (singlePhase) {
                        //单相表
                        Power = String.format("%02X%02X%02X", receiveData[4] & 0xff, receiveData[3] & 0xff, receiveData[2] & 0xff);
                        Voltage1 = String.format("%02X%02X", receiveData[6] & 0xff, receiveData[5] & 0xff);
                        Current1 = String.format("%02X%02X%02X", receiveData[9] & 0xff, receiveData[8] & 0xff, receiveData[7] & 0xff);
                        Frequency = String.format("%02X%02X", receiveData[11] & 0xff, receiveData[10] & 0xff);
                        PowerFactor = String.format("%02X%02X", receiveData[13] & 0xff, receiveData[12] & 0xff);
                        ReactivePower = String.format("%02X%02X%02X", receiveData[16] & 0xff, receiveData[15] & 0xff, receiveData[14] & 0xff);

                    } else {
                        //三相电表
                        Power = String.format("%02X%02X%02X", receiveData[4] & 0xff, receiveData[3] & 0xff, receiveData[2] & 0xff);
                        Voltage1 = String.format("%02X%02X", receiveData[6] & 0xff, receiveData[5] & 0xff);
                        Voltage2 = String.format("%02X%02X", receiveData[8] & 0xff, receiveData[7] & 0xff);
                        Voltage3 = String.format("%02X%02X", receiveData[10] & 0xff, receiveData[9] & 0xff);
                        Current1 = String.format("%02X%02X%02X", receiveData[13] & 0xff, receiveData[12] & 0xff, receiveData[11] & 0xff);
                        Current2 = String.format("%02X%02X%02X", receiveData[16] & 0xff, receiveData[15] & 0xff, receiveData[14] & 0xff);
                        Current3 = String.format("%02X%02X%02X", receiveData[19] & 0xff, receiveData[18] & 0xff, receiveData[17] & 0xff);
                        Frequency = String.format("%02X%02X", receiveData[21] & 0xff, receiveData[20] & 0xff);
                        PowerFactor = String.format("%02X%02X", receiveData[23] & 0xff, receiveData[22] & 0xff);
                        ReactivePower = String.format("%02X%02X%02X", receiveData[26] & 0xff, receiveData[25] & 0xff, receiveData[24] & 0xff);
                    }

                    if (!TextUtils.isEmpty(ReactivePower)) {
                        builder = new StringBuilder(ReactivePower);
                        ReactivePower = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(PowerFactor)) {
                        builder = new StringBuilder(PowerFactor);
                        PowerFactor = builder.insert(1, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Frequency)) {
                        builder = new StringBuilder(Frequency);
                        Frequency = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Current1)) {
                        builder = new StringBuilder(Current1);
                        Current1 = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Current2)) {
                        builder = new StringBuilder(Current2);
                        Current2 = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Current3)) {
                        builder = new StringBuilder(Current3);
                        Current3 = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Voltage1)) {
                        builder = new StringBuilder(Voltage1);
                        Voltage1 = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Voltage2)) {
                        builder = new StringBuilder(Voltage2);
                        Voltage2 = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Voltage3)) {
                        builder = new StringBuilder(Voltage3);
                        Voltage3 = builder.insert(3, ".").toString();
                    }

                    if (!TextUtils.isEmpty(Power)) {
                        builder = new StringBuilder(Power);
                        Power = builder.insert(3, ".").toString();
                    }

                    insitem = new InstantaneousBean();
                    insitem.collectorNo = assist.c645Bean.collectorNumber;
                    insitem.collectorTime = HexStringUtil.getNowTime();
                    insitem.meterNo = meter;
                    insitem.meterPosition = String.valueOf(i);
                    insitem.reactivePower = ReactivePower;

                    insitem.powerFactor = PowerFactor;

                    insitem.frequency = Frequency + " Hz";
                    insitem.current1 = Current1 + " A";
                    insitem.current2 = Current2 + " A";
                    insitem.current3 = Current3 + " A";

                    insitem.voltage1 = Voltage1 + " V";
                    insitem.voltage2 = Voltage2 + " V";
                    insitem.voltage3 = Voltage3 + " V";

                    insitem.power = Power;
                    insitem.isSingle = singlePhase;
                }
            }
            assist.c645Bean.insBean = insitem;
            listener.onSuccess(assist, i);
            i++;
        }

        System.out.println(TAG + "Instantaneous All finished");

        return assist;
    }

    /**
     * 读PAN ID
     *
     * @param assist TranXADRAssist
     * @param model  ReceiveModel
     * @return TranXADRAssist
     */
    private TranXADRAssist readCLTPanID(TranXADRAssist assist, ReceiveModel model) {
        if (debugMode) {
            System.out.println(TAG + "Read ReadCLTPanID=" + assist.c645Bean.collectorNumber);
        }
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();

        model.setReadType(MeterDataTypes.PanID);
        model = GlobalCommunicators.c645ZigbeeCollector.Read(model);
        byte[] data = HexStringUtil.reverse(model.recBytes);
        assist.c645Bean.value = HexStringUtil.bytesToHexString(data);
        assist.aResult = model.isSuccess;
        assist.errMsg = model.errorMsg;
        return assist;
    }

    /**
     * Pan ID write
     *
     * @param assist TranXADRAssist
     * @param model  ReceiveModel
     * @return TranXADRAssist
     */
    private TranXADRAssist writeCLTPanID(TranXADRAssist assist, ReceiveModel model) {
        if (debugMode) {
            System.out.println(TAG + "Write WriteCLTPanID||" + assist.c645Bean.collectorNumber);
        }
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        byte[] sendBytes = HexStringUtil.reverse(HexStringUtil.hexToByte(assist.c645Bean.writeData));
        model.setReadType(MeterDataTypes.PanID);
        model = GlobalCommunicators.c645ZigbeeCollector.Write(MeterDataTypes.PanID, GlobalCommunicators.CollectorPasswordBytes, sendBytes, model);
        assist.aResult = model.isSuccess;
        assist.errMsg = model.errorMsg;
        assist.recBytes = model.recBytes;
        return assist;
    }

    /**
     * 以太网 设置
     *
     * @param assist TranXADRAssist
     * @param model  ReceiveModel
     * @return TranXADRAssist
     */
    public TranXADRAssist writeEthernetParameters(TranXADRAssist assist, ReceiveModel model) throws Exception {
        if (debugMode) {
            System.out.println(TAG + "WriteEthernetParameters||" + assist.c645Bean.collectorNumber);
        }
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        byte[] byteList = new byte[18];
        String[] ips = assist.c645Bean.ethernetBean.stationIP.split("\\.");

        String[] gateway = assist.c645Bean.ethernetBean.gateWay.split("\\.");

        String[] masks = assist.c645Bean.ethernetBean.masks.split("\\.");

        String[] connectIps = assist.c645Bean.ethernetBean.cascade.split("\\.");
        if (ips.length != 4) {
            throw new Exception("Input Wrong IP Address :" + assist.c645Bean.ethernetBean.stationIP);
        }
        int index = 0;
        for (String item : ips) {

            byteList[index++] = (byte) Integer.parseInt(item);
        }

        for (String item : gateway) {
            byteList[index++] = (byte) Integer.parseInt(item);
        }

        for (String item : masks) {
            byteList[index++] = (byte) Integer.parseInt(item);
        }

        byte[] portBytes = HexStringUtil.GetIntegerBytes(Integer.parseInt(assist.c645Bean.ethernetBean.port), 2);
        if (portBytes.length > 2 || portBytes.length <= 0) {
            throw new Exception("port length error!:" + assist.c645Bean.ethernetBean.port);
        }
        byteList[index++] = portBytes[0];
        byteList[index++] = portBytes[1];


        for (String item : connectIps) {
            byteList[index++] = (byte) Integer.parseInt(item);
        }
        model.setReadType(MeterDataTypes.ReadEthernet_v1);
        model = GlobalCommunicators.c645ZigbeeCollector.Write(MeterDataTypes.ReadEthernet_v1, GlobalCommunicators.CollectorPasswordBytes, byteList, model);
        assist.aResult = model.isSuccess;
        assist.recBytes = model.recBytes;
        assist.errMsg = model.errorMsg;
        return assist;

    }

    /**
     * 读取 以太网
     *
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    private TranXADRAssist readEthernet(TranXADRAssist assist) {
        if (debugMode) {
            System.out.println(TAG + "ReadEthernetParameters||" + assist.c645Bean.collectorNumber);
        }
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        ReceiveModel model = new ReceiveModel();
        model.setReadType(MeterDataTypes.ReadEthernet_v1);
        model = GlobalCommunicators.c645ZigbeeCollector.Read(model);//ReadEthernet_v1
        assist.errMsg = model.errorMsg;
        assist.recBytes = model.recBytes;
        assist.aResult = model.isSuccess;
        if (!model.isSuccess) {
            return assist;
        }
        assist.c645Bean.ethernetBean = new EthernetBean();
        byte[] data = assist.recBytes;
        byte[] temp = new byte[data.length - 2];
        System.arraycopy(data, 2, temp, 0, temp.length);
        data = Arrays.copyOf(temp, temp.length);

        byte[] stationIP = new byte[4];
        System.arraycopy(data, 0, stationIP, 0, stationIP.length);


        String[] arr = new String[4];
        for (int i = 0; i < stationIP.length; i++) {
            arr[i] = String.valueOf(stationIP[i] & 0xff);
        }
        assist.c645Bean.ethernetBean.stationIP = TextUtils.join(".", arr);

        byte[] gateway = new byte[4];
        System.arraycopy(data, 4, gateway, 0, gateway.length);
        for (int i = 0; i < gateway.length; i++) {
            arr[i] = String.valueOf(gateway[i] & 0xff);
        }
        assist.c645Bean.ethernetBean.gateWay = TextUtils.join(".", arr);

        byte[] master = new byte[4];
        System.arraycopy(data, 8, master, 0, master.length);
        for (int i = 0; i < master.length; i++) {
            arr[i] = String.valueOf(master[i] & 0xff);
        }
        assist.c645Bean.ethernetBean.masks = TextUtils.join(".", arr);

        byte[] stationPort = new byte[2];
        System.arraycopy(data, 12, stationPort, 0, stationPort.length);
        assist.c645Bean.ethernetBean.port = String.format(Locale.ENGLISH, "%04d", (stationPort[1] & 0xff) * 256
                + (stationPort[0] & 0xff));

        byte[] connectIP = new byte[4];
        System.arraycopy(data, 14, connectIP, 0, connectIP.length);
        for (int i = 0; i < connectIP.length; i++) {
            arr[i] = String.valueOf(connectIP[i] & 0xff);
        }
        assist.c645Bean.ethernetBean.cascade = TextUtils.join(".", arr);
        return assist;
    }

    /**
     * 保电模式
     * assist.c645Bean.relayAction true 进入保电模式  否则退出
     *
     * @param assist TranXADRAssist
     * @return TranXADRAssist
     */
    private TranXADRAssist ExecuteRelay(TranXADRAssist assist) {
        System.out.println("Enter Power-On Mode Start ...");
        for (String meterNo : assist.c645Bean.meterNumberList) {
            System.out.println("Read Enter Power-On Mode||" + assist.c645Bean.collectorNumber + "--" + meterNo);
            GlobalCommunicators.c645Address = HexStringUtil.padLeft(meterNo, 12, '0');
            GlobalCommunicators.Update();
            ReceiveModel model = new ReceiveModel();
            model = GlobalCommunicators.c645ZigbeeMeter.Write(assist.c645Bean.relayAction ? MeterDataTypes.RelayOn : MeterDataTypes.RelayOff,
                    GlobalCommunicators.MeterPasswordBytes, new byte[]{}, model);

            if (!model.isSuccess) {
                model = GlobalCommunicators.c645ZigbeeMeter.Write(assist.c645Bean.relayAction ? MeterDataTypes.RelayOn : MeterDataTypes.RelayOff,
                        GlobalCommunicators.MeterPasswordBytes, new byte[]{}, model);
            }
            if (model.isSuccess) {
                System.out.println(meterNo + ":" + "Position" + ":" + ":" + "finished!");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("All finished");
        return assist;

    }

    /**
     * 冻结
     *
     * @param assist TranXADRAssist
     * @param model  ReceiveModel
     * @return TranXADRAssist
     */
    private TranXADRAssist readDayBlock(TranXADRAssist assist, ReceiveModel model) {

        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        assist.c645Bean.dayBlockBean.clear();
        model.setReadType(MeterDataTypes.ReadDayBill);
        int i = 0;
        for (String meter : assist.c645Bean.meterNumberList) {
            if (debugMode) {
                System.out.println(TAG + "Read daily frozen energy||" + assist.c645Bean.collectorNumber + "||meter=" + meter);
            }
            model.setReadType(MeterDataTypes.ReadDayBill);

            byte[] bytes = HexStringUtil.hexToByte(HexStringUtil.padLeft(meter, 12, '0'));
            byte[] newByte = HexStringUtil.reverse(bytes);
            String meterAddress = HexStringUtil.bytesToHexString(newByte);
            String startTime = assist.c645Bean.startTime.replace("-", "").replace(" ", "").replace(":", "");
            String endTime = assist.c645Bean.endTime.replace("-", "").replace(" ", "").replace(":", "");
            //2019-01-23 12:00
            startTime = startTime.substring(2, 8) + "0000";
            endTime = endTime.substring(2, 8) + "0000";

            //时间拼接待完成
            model = GlobalCommunicators.c645ZigbeeCollector.ReadDay(model, meterAddress + startTime + endTime);
            assist.aResult = model.isSuccess;
            assist.errMsg = model.errorMsg;
            if (model.isSuccess) {
                assist.c645Bean.dayBlockBean.clear();

                byte[] data = model.recBytes;
                byte[] temp;

                byte[] itemBlock;

                if (data.length > 5 && data.length < 101) {
                    itemBlock = HexStringUtil.removeBytes(data, 0, 4);
                    while (itemBlock.length >= 32) {
                        DayBlockBean dayItem = new DayBlockBean();
                        dayItem.collectorTime = HexStringUtil.getNowTime();
                        dayItem.collectorNo = assist.c645Bean.collectorNumber;
                        dayItem.meterNo = meter;
                        dayItem.meterPosition = String.valueOf(i);

                        dayItem.dateTime = HexStringUtil.byteToString(itemBlock[7]) + HexStringUtil.byteToString(itemBlock[8]) + HexStringUtil.byteToString(itemBlock[9]);
                        temp = HexStringUtil.getBytes(itemBlock, 12, 4);
                        dayItem.forwardActiveData = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(dayItem.forwardActiveData)) {
                            dayItem.forwardActiveData = new StringBuilder(dayItem.forwardActiveData).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(dayItem.forwardActiveData, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            dayItem.forwardActiveData = parseScale(longVal, -2);
                        }
                        assist.c645Bean.dayBlockBean.add(dayItem);

                        itemBlock = HexStringUtil.removeBytes(itemBlock, 0, 31);
                    }
                }
                while (data.length >= 101) {
                    itemBlock = HexStringUtil.removeBytes(data, 0, 4);
                    while (itemBlock.length >= 32) {
                        DayBlockBean dayItem = new DayBlockBean();
                        dayItem.collectorTime = HexStringUtil.getNowTime();
                        dayItem.collectorNo = assist.c645Bean.collectorNumber;
                        dayItem.meterNo = meter;
                        dayItem.meterPosition = String.valueOf(i);
                        //2017-06-29 fzj
                        int aa = 0;
                        String strNo = meter.substring(meter.length() - 2);
                        if (!HexStringUtil.byteToString(data[0]).equals(strNo)) {
                            for (int k = 0; k < itemBlock.length; k++) {
                                if (HexStringUtil.byteToString(itemBlock[k]).equals(strNo)) {
                                    itemBlock = HexStringUtil.removeBytes(itemBlock, 0, k - 1);
                                    aa = 1;
                                    break;
                                }
                            }
                        }

                        dayItem.dateTime = HexStringUtil.byteToString(itemBlock[7]) + HexStringUtil.byteToString(itemBlock[8]) + HexStringUtil.byteToString(itemBlock[9]);
                        temp = HexStringUtil.getBytes(itemBlock, 12, 4);
                        dayItem.forwardActiveData = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));
                        if (HexStringUtil.judgeContainsStr(dayItem.forwardActiveData)) {
                            dayItem.forwardActiveData = new StringBuilder(dayItem.forwardActiveData).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(dayItem.forwardActiveData, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            dayItem.forwardActiveData = parseScale(longVal, -2);
                        }
                        if (aa == 0) {
                            itemBlock = HexStringUtil.removeBytes(itemBlock, 0, 31);
                        } else if (aa == 1) {
                            itemBlock = HexStringUtil.removeBytes(itemBlock, 0, 26);
                            aa = 0;
                        }

                        assist.c645Bean.dayBlockBean.add(dayItem);
                    }

                    data = HexStringUtil.removeBytes(data, 0, 100);
                }

            }
            listener.onSuccess(assist, i);
            i++;
        }
        return assist;
    }

    /**
     * 冻结-预付费信息
     *
     * @param assist TranXADRAssist 参数
     * @param model  ReceiveModel
     * @return TranXADRAssist
     */
    public TranXADRAssist readDayBlockPrePayment(TranXADRAssist assist, ReceiveModel model) {
        GlobalCommunicators.c645Address = HexStringUtil.padLeft(assist.c645Bean.collectorNumber, 12, 'F');
        GlobalCommunicators.Update();
        model.setReadType(MeterDataTypes.ReadPre);
        int i = 0;
        for (String meter : assist.c645Bean.meterNumberList) {

            assist.c645Bean.prePaymentBeanList.clear();
            model.setReadType(MeterDataTypes.ReadPre);
            byte[] bytes = HexStringUtil.hexToByte(HexStringUtil.padLeft(meter, 12, '0'));
            byte[] newByte = HexStringUtil.reverse(bytes);
            String meterAddress = HexStringUtil.bytesToHexString(newByte);
            String startTime = assist.c645Bean.startTime.replace("-", "").replace(" ", "").replace(":", "");
            String endTime = assist.c645Bean.endTime.replace("-", "").replace(" ", "").replace(":", "");
            //2019-01-23 12:00
            startTime = startTime.substring(2, 8) + "0000";
            endTime = endTime.substring(2, 8) + "0000";

            model = GlobalCommunicators.c645ZigbeeCollector.ReadDay(model, meterAddress + startTime + endTime);
            assist.errMsg = model.errorMsg;
            assist.aResult = model.isSuccess;
            if (model.isSuccess) {
                byte[] data = model.recBytes;
                byte[] temp;
                byte[] itemBlock;

                while (data.length >= 122) {
                    itemBlock = HexStringUtil.removeBytes(data, 0, 4);
                    while (itemBlock.length >= 28) {
                        if ((itemBlock[0] & 0xff) == 0xBF || (itemBlock[1] & 0xff) == 0xF0 ||
                                (itemBlock[2] & 0xff) == 0x01 || (itemBlock[3] & 0xff) == 0x00
                                || (itemBlock[4] & 0xff) == 0x0F) {
                            itemBlock = HexStringUtil.removeBytes(itemBlock, 0, 4);
                        }
                        PrePaymentBean daypre = new PrePaymentBean();
                        daypre.collectTime = HexStringUtil.getNowTime();
                        daypre.collectorNo = assist.c645Bean.collectorNumber;
                        daypre.meterNo = meter;
                        daypre.meterPosition = String.valueOf(i);
                        daypre.dateTime = HexStringUtil.byteToString(itemBlock[7]) + HexStringUtil.byteToString(itemBlock[8]) + HexStringUtil.byteToString(itemBlock[9]);

                        temp = HexStringUtil.getBytes(itemBlock, 12, 4);
                        daypre.comsumption = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(daypre.comsumption)) {
                            daypre.comsumption = new StringBuilder(daypre.comsumption).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(daypre.comsumption, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            daypre.comsumption = parseScale(longVal, -2);
                        }

                        temp = HexStringUtil.getBytes(itemBlock, 16, 4);
                        daypre.credit = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(daypre.credit)) {
                            daypre.credit = new StringBuilder(daypre.credit).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(daypre.credit, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            daypre.credit = parseScale(longVal, -2);
                        }

                        temp = HexStringUtil.getBytes(itemBlock, 20, 4);
                        daypre.surplus = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(daypre.surplus)) {
                            daypre.surplus = new StringBuilder(daypre.surplus).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(daypre.surplus, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            daypre.surplus = parseScale(longVal, -2);
                        }

                        temp = HexStringUtil.getBytes(itemBlock, 24, 1);
                        daypre.moneyStatus = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        temp = HexStringUtil.getBytes(itemBlock, 25, 1);
                        daypre.relayStatus = OperationReasons.getRelayStatus(HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp)));

                        temp = HexStringUtil.getBytes(itemBlock, 26, 1);
                        daypre.meterMode = MeterModes.GetMeterModeText(HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp)));

                        temp = HexStringUtil.getBytes(itemBlock, 27, 1);
                        daypre.relayOperationReason = OperationReasons.GetRelayOperationReason(HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp)));


                        assist.c645Bean.prePaymentBeanList.add(daypre);

                        itemBlock = HexStringUtil.removeBytes(data, 0, 27);
                    }

                    data = HexStringUtil.removeBytes(data, 0, 121);
                }

                if (data.length > 5) {
                    itemBlock = HexStringUtil.removeBytes(data, 0, 4);
                    while (itemBlock.length >= 28) {
                        if ((itemBlock[0] & 0xff) == 0xBF || (itemBlock[1] & 0xff) == 0xF0 || (itemBlock[2] & 0xff) == 0x01
                                || (itemBlock[3] & 0xff) == 0x00 || (itemBlock[4] & 0xff) == 0x0F) {
                            itemBlock = HexStringUtil.removeBytes(itemBlock, 0, 4);
                        }
                        PrePaymentBean daypre = new PrePaymentBean();
                        daypre.collectTime = HexStringUtil.getNowTime();
                        daypre.collectorNo = assist.c645Bean.collectorNumber;
                        daypre.meterNo = meter;
                        daypre.meterPosition = String.valueOf(i);
                        daypre.dateTime = HexStringUtil.byteToString(itemBlock[7]) + HexStringUtil.byteToString(itemBlock[8]) + HexStringUtil.byteToString(itemBlock[9]);

                        temp = HexStringUtil.getBytes(itemBlock, 12, 4);
                        daypre.comsumption = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(daypre.comsumption)) {
                            daypre.comsumption = new StringBuilder(daypre.comsumption).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(daypre.comsumption, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            daypre.comsumption = parseScale(longVal, -2);
                        }

                        temp = HexStringUtil.getBytes(itemBlock, 16, 4);
                        daypre.credit = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(daypre.credit)) {
                            daypre.credit = new StringBuilder(daypre.credit).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(daypre.credit, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            daypre.credit = parseScale(longVal, -2);
                        }

                        temp = HexStringUtil.getBytes(itemBlock, 20, 4);
                        daypre.surplus = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        if (HexStringUtil.judgeContainsStr(daypre.surplus)) {
                            daypre.surplus = new StringBuilder(daypre.surplus).insert(6, ".").toString();
                        } else {
                            long longVal = parseInteger(new BigInteger(daypre.surplus, 10), HexDataFormat.DOUBLE_LONG_UNSIGNED);
                            daypre.surplus = parseScale(longVal, -2);
                        }

                        temp = HexStringUtil.getBytes(itemBlock, 24, 1);
                        daypre.moneyStatus = HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp));

                        temp = HexStringUtil.getBytes(itemBlock, 25, 1);
                        daypre.relayStatus = OperationReasons.getRelayStatus(HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp)));

                        temp = HexStringUtil.getBytes(itemBlock, 26, 1);
                        daypre.meterMode = MeterModes.GetMeterModeText(HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp)));

                        temp = HexStringUtil.getBytes(itemBlock, 27, 1);
                        daypre.relayOperationReason = OperationReasons.GetRelayOperationReason(HexStringUtil.bytesToHexString(HexStringUtil.reverse(temp)));

                        assist.c645Bean.prePaymentBeanList.add(daypre);

                        itemBlock = HexStringUtil.removeBytes(itemBlock, 0, 27);
                    }
                }
            }
            listener.onSuccess(assist, i);
            i++;
        }
        return assist;
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
            assist = commServer.read(framePara, iComm, assist);
            closeSerial();
        } else {
            assist.aResult = false;
        }
        return assist;
    }

    /**
     * 大数据 转换 int 有符合
     *
     * @param value    16进制数据
     * @param dataType 数据类型
     * @return int
     */
    public static long parseInteger(String value, int dataType) {
        try {

            return parseInteger(new BigInteger(value, 16), dataType);

        } catch (Exception ex) {
            Log.d("api=parseInteger", ex.getMessage());
        }
        return -1;
    }

    /**
     * 转换 Integer
     *
     * @param integer  BigInteger
     * @param dataType 数据类型
     * @return Integer
     */
    public static long parseInteger(BigInteger integer, int dataType) {

        //4字节 有 负数
        if (dataType == HexDataFormat.DOUBLE_LONG) {
            BigInteger c = new BigInteger("100000000", 16);
            int data = integer.compareTo(c);
            if (data > 0) {
                return data;
            }
        }
        //有负数
        else if (dataType == HexDataFormat.LONG64) {
            BigInteger c = new BigInteger("10000000000000000", 16);
            int data = integer.compareTo(c);
            if (data > 0) {
                return data;
            }
        } else if (dataType == HexDataFormat.DOUBLE_LONG_UNSIGNED) {
            return integer.longValue();
        } else if (dataType == HexDataFormat.LONG64_UNSIGNED) {
            BigInteger c = new BigInteger("10000000000000000", 16);
            return c.longValue();
        }
        return integer.intValue();
    }

    /**
     * 量纲转换
     *
     * @param val   long
     * @param scale 量纲
     * @return String
     */
    private String parseScale(long val, double scale) {
        String result;
        if (scale < 0) {
            scale = Math.abs(scale);
            String str = String.valueOf(val);
            String temp = "";
            if (str.startsWith("-")) {
                //有负数的情况
                temp = "-";
                str = str.substring(1);
            }
            if (str.length() <= scale) {
                str = HexStringUtil.padRight(str, (int) scale + 1, '0');
                str = temp + str;
            }
            result = new StringBuffer(str).insert(str.length() - (int) scale, ".").toString();
        } else {
            result = String.valueOf((int) (val * Math.pow(10, scale)));
        }
        return result;
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
            framePara.SleepT =(int) this.sleepSend;
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
            while (pos < 5) {
                iComm = new CommOpticalSerialPort();
                iComm = commServer.openDevice(cPara, iComm);
                if (iComm != null) {
                    isTermination = false;
                    isOpenSuc = true;
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
//                if (deviceType.equals(HexDevice.HT380A)) {
//                    powerHT380AOff();
//                } else {
//                    powerOff();
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
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
     * 380A上电操作
     */
    public void powerHT380AOn() {
        try {
            if (rs232Controller == null) {
                rs232Controller = new RS232Controller();
            }
            rs232Controller.Rs232_PowerOn();
            SystemClock.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 380A下电操作
     */
    public void powerHT380AOff() {
        if (rs232Controller != null) {
            try {
                rs232Controller.Rs232_PowerOff();
                rs232Controller = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        public void onSuccess(TranXADRAssist data, int pos) {
            for (IHexListener item : listenerList) {
                item.onSuccess(data, pos);
            }
        }
    };

}
