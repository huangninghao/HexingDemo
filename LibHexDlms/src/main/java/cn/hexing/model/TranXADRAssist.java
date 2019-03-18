package cn.hexing.model;

import android.support.annotation.IntDef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.hexing.dlt645.MeterDataTypes;
import cn.hexing.dlt645.model.DayBlockBean;
import cn.hexing.dlt645.model.EthernetBean;
import cn.hexing.dlt645.model.GprsBean;
import cn.hexing.dlt645.model.InstantaneousBean;
import cn.hexing.dlt645.model.MeterRelayBean;
import cn.hexing.dlt645.model.MeterSetupBean;
import cn.hexing.dlt645.model.PrePaymentBean;


/**
 * @author caibinglong
 *         date 2018/2/2.
 *         desc desc
 */

public class TranXADRAssist implements Cloneable, Serializable {
    public String obis = "";//obis 地址
    public String obisTwo = ""; //第2个obis
    public String writeData = "";//写入数据参数 最终下发
    public String originalWriteData = "";//原始待写入数据
    public String processWriteData = "";//处理之后的数据
    public String writeType = "";//写入数据类型
    public byte[] recBytes; //固件返回byte[]
    public String recStrData;//固件返回 string
    public int byteLen = -1; //收到设定字节 结束串口接收
    public int actionType = 0;// read write action
    public boolean aResult = false;//执行结果
    public String name = "";//名称
    public double scale = 0;//量刚
    public String unit = "";//单位
    public String value = "";//返回数据
    public boolean visible = false;//是否显示
    public String errMsg = "";//错误信息
    public String recType; //接收固件返回待解析数据类型
    public String markNo = "";//唯一标识

    public int dataType = -1;//数据类型
    public String comments = ""; //bit
    public String coding = ""; //当dataType = Octs  有几种模式 输出格式 Hex 、 Ascs 等

    public void setCodingType(@CodingType.DataTypes int codingType) {
        this.codingType = codingType;
    }

    public int getCodingType() {
        return codingType;
    }

    private int codingType = -1; //替换 coding string
    public String format = "";//
    public int size = 0; //字节长度
    public int definiteDataType = -1; //具体的数据类型
    public boolean needReadScale = false;//是否需要读取量刚
    public String startTime; //开始时间
    public String endTime;//结束时间 一般用于冻结项 参数
    public List<StructBean> structList;
    public boolean isCloseSerial = false; // false 不关闭 串口
    public boolean isOpenSerial = false; // true 开串口
    public boolean isFirstFrame = true;//是否第一数据帧
    public boolean isHands = true;//是否需要握手
    public boolean autoBaudRate = true;//是否需要切换波特率
    public boolean needMoveData = false;//发送数据 是否需要 移位
    public boolean needMoveAnalysis = false;// 数据解析是否需要处理  &7F
    public String protocol;
    public boolean baudRateTest = false;// 握手 波特率测试
    public C645Bean c645Bean;

    public static class StructBean implements Cloneable, Serializable {
        public String name = "";//名称
        public double scale = 0;//量刚
        public String obis = "";
        public String unit = "";//单位
        public String value = "";//数据
        public int dataType = -1;//数据类型
        public String comments = ""; //

        public String coding = ""; //当dataType = Octs  有几种模式 输出格式 Hex 、 Ascs 等

        public void setCodingType(@CodingType.DataTypes int codingType) {
            this.codingType = codingType;
        }

        public int getCodingType() {
            return codingType;
        }

        private int codingType = -1; //替换 coding string
        public String format = "";//分割字符串，| 或者其他
        public int size = 0; //字节长度
        public String writeData = "";
        public boolean visible = true;//是否显示
        public List<BeanItem> beanItems;

        @Override
        public StructBean clone() {
            try {
                return (StructBean) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static class BeanItem implements Cloneable, Serializable {
            public String name = "";//名称
            public double scale = 0;//量刚
            public String unit = "";//单位
            public String value = "";//数据
            public int dataType = -1;//数据类型
            public String format = "";//分割字符串，| 或者其他
            public int size = 0; //字节长度
            public String writeData = "";
            public boolean visible = true;//是否显示

            @Override
            protected BeanItem clone() throws CloneNotSupportedException {
                try {
                    return (BeanItem) super.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }

    //配置 通讯参数
    public static class C645Bean implements Cloneable, Serializable {

        public int maxWaitTime = 3000; //一般用于645 zigBee  ms
        public int waitReceiveTime = 0;//long  ms
        public int sleepSend = 0;//ms
        private int meterDataType645Id = -1;//645命令 id 控制码
        public String collectorNumber = ""; //采集器
        public String writeData;
        public String startTime;
        public String endTime;
        public GprsBean gprsBean; //gprs
        public EthernetBean ethernetBean;
        public String value;
        public InstantaneousBean insBean; //瞬时量
        public MeterRelayBean relayBean; //继电器
        public List<DayBlockBean> dayBlockBean = new ArrayList<>();// 日冻结
        public List<PrePaymentBean> prePaymentBeanList = new ArrayList<>();//日冻结 预付费
        public boolean relayAction = false; //true connect false disconnect
        public List<String> meterNumberList = new ArrayList<>();//表
        public List<MeterSetupBean> meterSetupBeanList = new ArrayList<>();//表安装信息

        public int getMeterDataType645Id() {
            return meterDataType645Id;
        }

        public void setMeterDataType645Id(@MeterDataTypes.ReadDataTypes int meterDataType645Id) {
            this.meterDataType645Id = meterDataType645Id;
        }


        @Override
        public C645Bean clone() {
            try {
                return (C645Bean) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public TranXADRAssist clone() {
        try {
            TranXADRAssist assist = (TranXADRAssist) super.clone();
            List<StructBean> newBeans = new ArrayList<>();
            if (assist.structList != null && assist.structList.size() > 0) {
                for (StructBean bean : assist.structList) {
                    newBeans.add(bean.clone());
                }
                assist.structList = newBeans;
            }
            if (assist.c645Bean != null) {
                assist.c645Bean = assist.c645Bean.clone();
            }
            return assist;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class CodingType {
        public static final int ASCS = 1;
        public static final int HEX = 2;
        public static final int OCTS = 3;

        @IntDef({ASCS, HEX, OCTS})
        public @interface DataTypes {
        }
    }
}
