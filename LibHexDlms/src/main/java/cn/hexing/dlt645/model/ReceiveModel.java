package cn.hexing.dlt645.model;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.dlt645.MeterDataTypes;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class ReceiveModel {
    public boolean isFinish = false;
    public boolean isSuccess = false;//校验结果
    public boolean isSend = false; //发送结果
    public byte[] recBytes = new byte[0]; //接收数据
    public byte controlCode = 0x00; //控制码
    public byte expectControlCode = 0x00;//期望值
    public byte exeControlCode = 0x00;//数据块标识

    public String errorMsg = "";
    public int errorCode = -1; //错误码
    public int maxWaitTime = 3000;//最大等待时间
    public int receiveByteLen = 0;//接收字节数 停止接收
    public List<Integer> checkFilter = new ArrayList<>();//帧过滤
    public int sleepTime = 0;//睡眠时间
    public byte[] sendData = new byte[0];//待发送数据帧
    public StringBuilder sendBuilfer = new StringBuilder(0);
    public String data;
    private @MeterDataTypes.ReadDataTypes
    int readType = -1;  //读取类型

    public void setReadType(@MeterDataTypes.ReadDataTypes int readType) {
        this.readType = readType;
    }

    public int getReadType() {
        return this.readType;
    }
}
