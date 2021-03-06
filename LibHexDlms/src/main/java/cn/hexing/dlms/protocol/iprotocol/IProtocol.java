package cn.hexing.dlms.protocol.iprotocol;


import cn.hexing.iComm.ICommAction;
import cn.hexing.dlms.protocol.model.HexActionBean;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public interface IProtocol {

    /***
     * 读取
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return byte[]
     */
    byte[] read(HXFramePara paraModel, ICommAction commDevice);

    /**
     * 直接发送 数据帧 不处理解析数据
     */
    TranXADRAssist sendByte(HXFramePara paraModel, ICommAction commDevice, TranXADRAssist assist);

    /***
     * 设置
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    boolean write(HXFramePara paraModel, ICommAction commDevice);

    /***
     * 执行
     * @param paraModel  HXFramePara 配置参数
     * @param commDevice 接口对象
     * @return bool
     */
    boolean action(HXFramePara paraModel, ICommAction commDevice);

    /**
     * 执行结果 返回对象
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return HexActionBean
     */
    HexActionBean actionWrite(HXFramePara paraModel, ICommAction commDevice);

    byte[] actionAndRead(HXFramePara paraModel, ICommAction commDevice);

    /***
     * 断开链路
     * @param commDevice 接口对象
     * @return bool
     */
    boolean discFrame(ICommAction commDevice);
}
