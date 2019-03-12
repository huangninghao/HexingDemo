package cn.hexing.dlt645.model;

/**
 * @author caibinglong
 *         date 2019/3/9.
 *         desc 冻结预付费信息
 */

public class PrePaymentBean {
    public String collectTime;
    public String collectorNo;
    public String meterNo;
    public String meterPosition;
    public String dateTime;
    public String comsumption;  //moneyUsed//使用的电量 comsumption 消费电量
    public String credit;//moneyAdded; // 信用 可透支  credit
    public String surplus;//moneyLeft; //剩下的余额
    public String moneyStatus;//
    public String relayStatus;//继电器状态
    public String meterMode;//电表模式
    public String relayOperationReason;//继电器操作原因
}
