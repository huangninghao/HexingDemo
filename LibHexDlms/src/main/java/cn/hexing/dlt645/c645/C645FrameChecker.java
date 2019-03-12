package cn.hexing.dlt645.c645;


import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class C645FrameChecker implements IFrameChecker{

    private final byte headByte = 0x68;
    private int headIndex;
    private int dataAreaLength = -1;
    private byte CS = 0x00;

    private byte[] C645Address;
    private byte[] received645Address = new byte[6];
    private byte[] controlCodeBytes;
    private byte[] receivedControlCodeBytes = new byte[1];


    public C645FrameChecker() {
    }

    public C645FrameChecker(int headIndex) {
        this.headIndex = headIndex;
        CS += headByte;
    }

    public void Set645Address(byte[] address) {
        if (address.length != 6) {
            System.out.println("Wrong 645 Address Size 6||" + address.length);
            return;
        }
        this.C645Address = address.clone();
    }

    @Override
    public ReceiveModel Check(int currentIndex, byte currentByte) {
        int offset = currentIndex - headIndex;
        ReceiveModel model = new ReceiveModel();
        if (offset < 0) {
            model.isFinish = true;
            model.isSuccess = false;
            model.errorMsg = "Note:Wrong Current Index,Less than headIndex ||headIndex:" + headIndex + "||CurrentIndex:" + currentIndex;
            return model;
        }
        model.isFinish = false;
        model.isSuccess = false;
        if (offset == 0) {
            return model;
        }
        switch (offset) {
            case 1:
                if (C645Address != null) {
                    if (currentByte != C645Address[0]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Address index 0 =" + C645Address[0] + "||" + currentByte;
                        return model;
                    }
                } else {
                    received645Address[0] = currentByte;
                }
                CS += currentByte;
                break;
            case 2:
                if (C645Address != null) {
                    if (currentByte != C645Address[1]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Address index 1 =" + C645Address[1] + "||" + currentByte;
                        return model;
                    }
                } else {
                    received645Address[1] = currentByte;
                }
                CS += currentByte;
                break;
            case 3:
                if (C645Address != null) {
                    if (currentByte != C645Address[2]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Address index 2 =" + C645Address[2] + "||" + currentByte;
                        return model;
                    }
                } else {
                    received645Address[2] = currentByte;
                }
                CS += currentByte;
                break;
            case 4:
                if (C645Address != null) {
                    if (currentByte != C645Address[3]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Address index 3 =" + C645Address[3] + "||" + currentByte;
                        return model;
                    }
                } else {
                    received645Address[3] = currentByte;
                }
                CS += currentByte;
                break;
            case 5:
                if (C645Address != null) {
                    if (currentByte != C645Address[4]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Address index 4 =" + C645Address[4] + "||=" + currentByte;
                        return model;
                    }
                } else {
                    received645Address[4] = currentByte;
                }
                CS += currentByte;
                break;
            case 6:
                if (C645Address != null) {
                    if (currentByte != C645Address[5]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Address index 5  =" + C645Address[5] + "||=" + currentByte;
                        return model;
                    }
                } else {
                    received645Address[5] = currentByte;
                }
                CS += currentByte;
                break;
            case 7:
                if (currentByte != 0x68) {
                    model.isFinish = true;
                    model.errorMsg = "Note:Wrong 645 second 68 flag 0x68||" + currentByte;
                    return model;
                }
                CS += currentByte;
                break;
            case 8:
                if (controlCodeBytes != null) {
                    if (currentByte != controlCodeBytes[0]) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 Control Code =" + controlCodeBytes[0] + "||" + currentByte;
                        return model;
                    }
                } else {
                    receivedControlCodeBytes[0] = currentByte;
                }
                CS += currentByte;
                break;
            case 9:
                dataAreaLength = (int) currentByte;
                CS += currentByte;
                break;
            default:
                if (dataAreaLength != -1 && offset == dataAreaLength + 10) {
                    if (CS != currentByte) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 CS =" + CS + "||" + currentByte;
                        return model;
                    }
                } else if (dataAreaLength != -1 && offset == dataAreaLength + 11) {
                    if (0x16 != currentByte) {
                        model.isFinish = true;
                        model.errorMsg = "Note:Wrong 645 end =" + 0x16 + "||" + currentByte;
                        return model;
                    } else {
                        model.isFinish = true;
                        model.isSuccess = true;
                        return model;
                    }
                } else {
                    CS += currentByte;
                }
                break;
        }
        return model;
    }
//
    @Override
    public int GetHeadIndex() {
        return headIndex;
    }

    @Override
    public int GetDataHeadIndex() {
        return headIndex + 10;
    }

    @Override
    public int GetDataLength() {
        return dataAreaLength;
    }
//
    @Override
    public void SetFrameCheckerParameter(int paraType, byte[] parameter) {
        switch (paraType) {
            case FrameParameters.C645Address:
                this.C645Address = parameter.clone();
                break;
            case FrameParameters.C645ControlCode:
                this.controlCodeBytes = parameter.clone();
                break;
        }
    }

    @Override
    public byte[] GetReceivedFrameParameter(int paraType) {
        switch (paraType) {
            case FrameParameters.C645Address:
                return this.received645Address.clone();
            case FrameParameters.C645ControlCode:
                return this.receivedControlCodeBytes.clone();
        }
        return new byte[0];
    }
//
    @Override
    public byte GetHeadByte() {
        return headByte;
    }

    @Override
    public IFrameChecker CreateInstance(int HeadIndex) {
        return new C645FrameChecker(HeadIndex);
    }

}
