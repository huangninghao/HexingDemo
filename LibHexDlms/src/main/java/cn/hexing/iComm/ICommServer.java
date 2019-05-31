package cn.hexing.iComm;

import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public interface ICommServer {

    /***
     * 打开通讯模块
     * @param  para CommPara 配置参数
     * @param commDevice 接口对象
     * @return 接口对象
     */
    ICommAction openDevice(CommPara para, AbsCommAction commDevice);

    /***
     * 关闭通讯模块
     * @return bool
     */
    boolean close(AbsCommAction commDevice);

    /**
     * 读取电表
     *
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return string
     */
    TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice);

    /**
     * 读取电表
     *
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return string
     */
    TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist);

    /**
     * 设置电表
     *
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice);

    /**
     * 设置电表
     *
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist);

    /**
     * 执行电表
     *
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    TranXADRAssist action(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist);
    TranXADRAssist action(HXFramePara paraModel, AbsCommAction commDevice);

}
