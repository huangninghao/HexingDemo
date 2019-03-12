package cn.hexing.iComm;

import cn.hexing.model.CommPara;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

/**
 * @author caibinglong
 *         date 2018/4/20.
 *         desc desc
 */

public abstract class AbsCommServer implements ICommServer {
    @Override
    public ICommAction openDevice(CommPara para, AbsCommAction commDevice) {
        return null;
    }

    @Override
    public boolean close(AbsCommAction commDevice) {
        return false;
    }

    @Override
    public TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice) {
        return null;
    }

    @Override
    public TranXADRAssist read(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        return null;
    }

    @Override
    public TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        return null;
    }

    @Override
    public TranXADRAssist write(HXFramePara paraModel, AbsCommAction commDevice) {
        return null;
    }

    @Override
    public TranXADRAssist action(HXFramePara paraModel, AbsCommAction commDevice) {
        return null;
    }

}
