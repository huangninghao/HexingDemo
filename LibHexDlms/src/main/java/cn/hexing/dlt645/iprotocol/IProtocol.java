package cn.hexing.dlt645.iprotocol;


import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.ICommAction;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

public interface IProtocol {

    /**
     * 直接发送 数据帧 不处理解析数据
     */
    TranXADRAssist sendByte(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist);


    /***
     * 断开链路
     * @param commDevice 接口对象
     * @return bool
     */
    boolean discFrame(ICommAction commDevice);
}
