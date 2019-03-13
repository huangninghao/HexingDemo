package cn.hexing.dlt645.model;


/**
 * @author caibinglong
 *         date 2019/2/27.
 *         desc desc
 */

public class InstantaneousBean {
    public String collectorNo;
    public String collectorTime;
    public String meterNo;
    public String meterPosition;

    public String power;

    public String reactivePower;
    public String powerFactor;
    public String frequency;

    public String current1;
    public String current2;
    public String current3;

    public String voltage1 = "";
    public String voltage2 = "";
    public String voltage3 = "";

    public boolean isSingle = true;//true单相表

}
