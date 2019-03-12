package cn.hexing.dlt645.zigbee;

import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/11.
 *         desc desc
 */

public class NetworkSearchResponseChecker extends ZigBeeFrameChecker {
    private int num = -1;

    public NetworkSearchResponseChecker(int headIndex) {
        super(headIndex);
    }

    @Override
    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel model = super.Check(currentIndex, currentByte);
        if (!model.isFinish) {
            int offset = currentIndex - super.GetDataHeadIndex();
            switch (offset) {
                case 0:
                    if (currentByte != 0x45) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong PID Attributes=0x45||curr=" + currentByte;
                        return model;
                    }
                    break;
                case 1:
                    if (currentByte != (byte) 0xC5) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Command Id=0xC5||curr=" + currentByte;
                        return model;
                    }
                    break;
                case 2:
                    num = (int) currentByte;
                    int total = (super.GetDataLength() - 3);
                    if (num * 21 != total) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Network Num=" + (total / 21) + "||" + num;
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
        return super.GetDataHeadIndex() + 3;
    }

    public int GetDataLength() {
        return super.GetDataLength() - 3;
    }

    public byte GetHeadByte() {
        return super.GetHeadByte();
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new NetworkSearchResponseChecker(HeadIndex);
    }

}
