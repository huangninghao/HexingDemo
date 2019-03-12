package cn.hexing.dlt645.zigbee;

import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/11.
 *         desc desc
 */

public class StateReportChecker extends ZigBeeFrameChecker {
    public StateReportChecker(int headIndex) {
        super(headIndex);
    }

    @Override
    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel model = super.Check(currentIndex, currentByte);
        if (!model.isFinish) {
            int offset = currentIndex - super.GetHeadIndex() - 3;
            switch (offset) {
                case -2:
                    if (currentByte != 0x00) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Length High Digit=" + 0x00 + "||curr=" + currentByte;
                        return model;
                    }
                    break;
                case -1:
                    if (currentByte != 0x02) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Length Low Digit=" + 0x02 + "||curr=" + currentByte;
                        return model;
                    }
                    break;
                case 0:
                    if (currentByte != (byte) 0x8A) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Type =" + 0x8A + "||curr" + currentByte;
                        return model;
                    }
                    break;
                default:
                    break;
            }
        }

        return model;
    }

    public int GetHeadIndex() {
        return super.GetHeadIndex();
    }

    public int GetDataHeadIndex() {
        return super.GetDataHeadIndex() + 1;
    }

    public int GetDataLength() {
        return 1;
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new StateReportChecker(HeadIndex);
    }

}
