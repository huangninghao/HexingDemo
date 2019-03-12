package cn.hexing.dlt645;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.check.FrameCheckerFilterTypes;
import cn.hexing.dlt645.iprotocol.IProtocol;
import cn.hexing.dlt645.zigbee.CommandResponseChecker;
import cn.hexing.dlt645.zigbee.NetworkSearchResponseChecker;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.ICommAction;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class HexHdlcFrame implements IProtocol {
    private CommandResponseChecker commandResponseChecker;
    NetworkSearchResponseChecker networkSearchResponseChecker;

    public HexHdlcFrame() {
        //commandResponseChecker = new CommandResponseChecker();
        // networkSearchResponseChecker = new NetworkSearchResponseChecker();
    }

    @Override
    public TranXADRAssist sendByte(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {

        boolean isSend = commDevice.sendByt(HexStringUtil.hexToByte(assist.processWriteData));
        if (isSend) {
            byte[] receiveByt = commDevice.receiveByte(paraModel.dataFrameWaitTime, paraModel.filterReceiveType, 0);
            switch (paraModel.filterReceiveType) {
                case FrameCheckerFilterTypes.ZigbeeCommandResponse:

                    break;
                case FrameCheckerFilterTypes.ZigbeeNetworkSearchResponse:

                    break;

            }
            assist.aResult = true;
            assist.recBytes = receiveByt;
        }
        return assist;
    }

    /**
     * 读采集器
     *
     * @param paraModel  HXFramePara
     * @param commDevice AbsCommAction
     * @param assist     TranXADRAssist
     * @return list
     */
    public List<TranXADRAssist> readCollector(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        List<TranXADRAssist> assists = new ArrayList<>();
        boolean isSend = commDevice.sendByt(HexStringUtil.hexToByte(assist.processWriteData));
        if (isSend) {
            byte[] receiveByt = commDevice.receiveByte(paraModel.dataFrameWaitTime, paraModel.filterReceiveType, 0);
            switch (paraModel.filterReceiveType) {
                case FrameCheckerFilterTypes.ZigbeeCommandResponse:

                    break;
                case FrameCheckerFilterTypes.ZigbeeNetworkSearchResponse:
                    int num = receiveByt.length / 21;
                    byte[] receivedItem;
                    for (int i = 0; i < num; i++) {
                        receivedItem = Arrays.copyOfRange(receiveByt, i * 21, (i + 1) * 21);
                        byte[] panIdList = Arrays.copyOfRange(receivedItem, 17, 21);
                        Collections.reverse(Arrays.asList(panIdList));
                        TranXADRAssist item = assist.clone();
                        item.value = HexStringUtil.bytesToHexString(panIdList);
                        assists.add(item);
                    }
                    break;

            }
            assist.recBytes = receiveByt;

        }
        return assists;
    }

    @Override
    public boolean discFrame(ICommAction commDevice) {
        return true;
    }
}
