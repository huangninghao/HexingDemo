package cn.hexing.dlt645.model;

/**
 * @author caibinglong
 *         date 2019/3/6.
 *         desc 以太网 数据模型
 */

public class EthernetBean {
    public String stationIP;
    public String gateWay;//网关
    public String masks;//掩码 stationIP
    public String port;
    public String cascade;//级联 stationIP
}
