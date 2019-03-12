package cn.hexing.dlt645.zigbee;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class ZigBeeFrameChecker implements IFrameChecker {

    private final byte headByte = 0x7E;
    private int headIndex;
    private int dataAreaLength = -1;

    private byte CS = 0x00;

    public ZigBeeFrameChecker(int headIndex) {
        this.headIndex = headIndex;
    }

    public ReceiveModel Check(int currentIndex, byte currentByte) {
        int offset = currentIndex - headIndex;
        ReceiveModel receiveModel = new ReceiveModel();
        if (offset < 0) {
            receiveModel.isFinish = true;
            receiveModel.isSuccess = false;
            receiveModel.errorMsg = "Wrong Current Index,Less than headIndex||headIndex:" + headIndex + "CurrentIndex:" + currentIndex;
            return receiveModel;
        }
        receiveModel.isFinish = false;
        receiveModel.isSuccess = false;
        if (offset == 0) {
            return receiveModel;
        }
        switch (offset) {
            case 1:
                dataAreaLength = (int) currentByte;
                break;
            case 2:
                dataAreaLength = dataAreaLength * 256 + (int) currentByte;
                break;
            default:
                if (dataAreaLength >= 0 && offset == dataAreaLength + 3) {
                    CS = (byte) (0xFF - CS);
                    if (CS == currentByte) {
                        receiveModel.isSuccess = true;
                    } else {
                        receiveModel.errorMsg = "CS Wrong:" + "0x" + HexStringUtil.byteToString(CS) + "->" + "0x" + HexStringUtil.byteToString(currentByte);
                        receiveModel.isSuccess = false;
                    }
                    receiveModel.isFinish = true;
                } else {
                    CS += currentByte;
                }
                break;
        }
        return receiveModel;
    }

    public int GetHeadIndex() {
        return headIndex;
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new ZigBeeFrameChecker(HeadIndex);
    }

    public byte GetHeadByte() {
        return headByte;
    }

    public int GetDataHeadIndex() {
        return headIndex + 3;
    }

    public int GetDataLength() {
        return dataAreaLength;
    }

    @Override
    public void SetFrameCheckerParameter(int paraType, byte[] parameter) {

    }

    @Override
    public byte[] GetReceivedFrameParameter(int paraType) {
        return new byte[0];
    }
}
