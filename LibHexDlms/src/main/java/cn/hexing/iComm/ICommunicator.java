package cn.hexing.iComm;

import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.MeterDataTypes;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/16.
 *         desc desc
 */

public interface ICommunicator {

    ReceiveModel Read(ReceiveModel model, String dateTimeHexString);//从采集器中读

    ReceiveModel ReadDay(ReceiveModel model, String dateTimeHexString);//从采集器中读

    ReceiveModel ContinueRead(@MeterDataTypes.ReadDataTypes int type, String dateTimeHexString); // BaseCommunicatorContinueChecker checker

    ReceiveModel Read(ReceiveModel model);//从表中读取信息

    ReceiveModel Write(@MeterDataTypes.ReadDataTypes int type, byte[] passwordBytes, byte[] writeData, ReceiveModel model);

    void SetParameters(@FrameParameters.FParameters int type, byte[] value);

    void GoodBye();
}
