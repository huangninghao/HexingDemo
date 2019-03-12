package cn.hexing.iec21.iprotocol;


import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.ICommAction;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public interface IProtocol {

    /***
     * 读取
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return byte[]
     */
    byte[] read(HXFramePara paraModel, AbsCommAction commDevice);


    /***
     * 设置
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    boolean write(HXFramePara paraModel, AbsCommAction commDevice);

    /***
     * 执行
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    boolean action(HXFramePara paraModel, AbsCommAction commDevice);

    /***
     * 断开链路
     * @param commDevice 接口对象
     * @return bool
     */
    boolean discFrame(AbsCommAction commDevice);

    /**
     * 直接发送 数据帧 不处理解析数据
     */
    TranXADRAssist sendByte(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist);
}
