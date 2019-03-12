package cn.hexing.dlt645;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author caibinglong
 *         date 2018/12/16.
 *         desc desc
 */

public class MeterDataTypes {
    // SwitchOff = 0xEE02,
    // SwitchOff2 = 0xEE03,
    public static final int SwitchOn = 0xEE04;
    public static final int SwitchOff = 0xEE03;
    // SwitchOn2=0xEE01,
    public static final int RelayOff = 0xEE02;
    public static final int RelayOn = 0xEE01;
    public static final int ReadSetupInfo = 0xCB00;  //1-8个表
    public static final int ReadSetupInfo2 = 0xCB0D;//8-16个表
    public static final int SetupMode = 0xCB04;
    public static final int ReadCurrentTotalElectric = 0x9010;
    public static final int ReadRelayStatus = 0xEE0A;
    public static final int ReadMonthFreezeData = 0xCB37;
    public static final int ReadEvent = 0xCB34;
    public static final int CB05 = 0xCB05;
    public static final int ReadGPRS = 0xCB10;
    public static final int ReadEthernet = 0xCB11;
    public static final int PanID = 0xCB02;
    public static final int ReadCollect = 0xC004;
    public static final int EnableSync = 0xCB06;
    public static final int PeriodSync = 0xCB07;
    public static final int ClearCIU = 0xCA0E;
    public static final int DayBlock = 0xCB3010CB;
    public static final int DayBlock2 = 0xCB3111CB;
    public static final int LoadCurve = 0xCB3212CB;
    public static final int LoadCurve2 = 0xCB3313CB;
    public static final int MinuteEvent = 0xCB3414CB;
    public static final int PrePaymentBlock = 0xCB3515CB;
    public static final int PrePaymentBlock2 = 0xCB3616CB;
    public static final int ReadDayBill = 0xF0B0;
    public static final int ReadPre = 0xF0BF;
    public static final int ReadC36F = 0xC36F;//by
    public static final int ReadC370 = 0xC370;
    public static final int ReadC371 = 0xC371;
    public static final int ReadC372 = 0xC372;
    public static final int ReadC373 = 0xC373;
    public static final int ReadC374 = 0xC374;
    public static final int ReadC365 = 0xC365;
    public static final int ReadInstantaneous = 0xC40F;//
    public static final int ReadInstantaneous_M = 0xC400;//multi Phase
    public static final int ReadEthernet_v1 = 0xCB20;

    public static final int CustomerSearchNetWork = 1000;//自定义 搜网使用 搜采集器
    public static final int CustomerConnectCollector = 1500;//自定义 连接采集器
    public static final int CustomerExecuteSearchMeter = 2000; //自定义 搜表
    public static final int CustomerReadMeter = 3000;//自定义 读取表档案

    //采集器相关
    public static final int CustomerCollGPRS = 5000; //自定义 采集器 gprs操作 读/写
    public static final int CustomerCollIEthernet = 5500;// 自定义 采集器 以太网络 ethernet
    public static final int CustomerCollClearMeter = 6000;//自定义 采集器  清表
    public static final int CustomerCollPlanID = 6200;// 自定义 采集器  planId
    public static final int CustomerCOllInstallMode = 6300;// 自定义 采集器  安装模式
    public static final int CustomerCollExit = 6400;// 自定义 采集器  退出 连接
    //表相关

    public static final int CustomerMeterPowerMode = 6500;// 自定义 表 保电
    public static final int CustomerActionMeterRelay = 4000;//自定义 表 继电器操作 拉合闸
    public static final int CustomerMeterDaily = 7000;// 自定义 表 日冻结
    public static final int CustomerMeterDailyPre = 7500;//自定义 表 日冻结 预付费
    public static final int CustomerMeterInstantaneous = 7600;//自定义 表  瞬时量
    public static final int CustomerMeterRelayStatus = 7700; // 自定义 表 继电器状态


    @IntDef({SwitchOn, SwitchOff, RelayOn, RelayOff, ReadSetupInfo, ReadSetupInfo2, SetupMode, ReadGPRS, ReadCollect,
            CustomerSearchNetWork, CustomerExecuteSearchMeter, CustomerReadMeter, CustomerActionMeterRelay, CustomerCollGPRS
            , CustomerCollPlanID, CustomerMeterInstantaneous, CustomerMeterRelayStatus, ReadRelayStatus, ReadInstantaneous
            , ReadInstantaneous_M, PanID, ReadDayBill, ReadEthernet_v1
            , CustomerCOllInstallMode, CustomerCollExit, ReadPre
            , CustomerCollIEthernet, CustomerCollClearMeter, CustomerMeterPowerMode, CustomerMeterDaily, CustomerMeterDailyPre})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReadDataTypes {
    }
}
