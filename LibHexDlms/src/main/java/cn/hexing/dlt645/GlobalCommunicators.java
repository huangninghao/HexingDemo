package cn.hexing.dlt645;

import cn.hexing.dlt645.comm.C645Meter;
import cn.hexing.dlt645.comm.C645ZigbeeCollector;
import cn.hexing.dlt645.comm.C645ZigbeeMeter;
import cn.hexing.dlt645.comm.CHX645Meter;
import cn.hexing.dlt645.comm.ZigbeeCommandExecutor;
import cn.hexing.iComm.ICommunicator;

/**
 * @author caibinglong
 *         date 2018/12/16.
 *         desc desc
 */

public class GlobalCommunicators {
    public static ICommunicator c645ZigbeeCollector = new C645ZigbeeCollector();
    public static ICommunicator c645ZigbeeMeter = new C645ZigbeeMeter();
    public static ICommunicator c645Meter = new C645Meter();
    public static ICommunicator cHX645Meter = new CHX645Meter();
    public static ZigbeeCommandExecutor zigbeeCommandExecutor = new ZigbeeCommandExecutor();

    public static byte[] MeterPasswordBytes = new byte[]{0x00, 0x00, 0x00, 0x00};
    public static byte[] CollectorPasswordBytes = new byte[]{0x00, 0x00, 0x00, 0x00};

    public static String longAddress = "0000000000000000";
    public static String shortAddress = "0000";
    public static String c645Address = "AAAAAAAAAAAA";


    public static void Update() {
        c645ZigbeeCollector = new C645ZigbeeCollector(longAddress, shortAddress, c645Address);
        c645ZigbeeMeter = new C645ZigbeeMeter(longAddress, shortAddress, c645Address);
        c645Meter = new C645Meter(c645Address);
        cHX645Meter = new CHX645Meter(c645Address);
        zigbeeCommandExecutor = new ZigbeeCommandExecutor();
    }
}
