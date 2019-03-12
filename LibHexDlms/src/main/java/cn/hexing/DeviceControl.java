package cn.hexing;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author caibinglong
 *         date 2018/9/27.
 *         desc 设备控制 上下电
 */

public class DeviceControl {
    private BufferedWriter CtrlFile;
    private Context mContext;

    public DeviceControl(String path) throws IOException {
        File DeviceName = new File(path);
        CtrlFile = new BufferedWriter(new FileWriter(DeviceName, false)); // open
        // file
    }

    public void PowerOnDevice(String power_on) throws IOException // poweron
    // barcode
    // device
    {
        CtrlFile.write(power_on);
        CtrlFile.flush();
    }

    public void PowerOffDevice(String power_off) throws IOException // poweroff
    // barcode
    // device
    {
        CtrlFile.write(power_off);
        CtrlFile.flush();
    }

    public void TriggerOnDevice() throws IOException // make barcode begin to
    // scan
    {
        CtrlFile.write("trig");
        CtrlFile.flush();
    }

    public void TriggerOffDevice() throws IOException // make barcode stop scan
    {
        CtrlFile.write("trigoff");
        CtrlFile.flush();
    }

    public void DeviceClose() throws IOException // close file
    {
        CtrlFile.close();
    }

    /**
     * 上电
     *
     * @return bool
     */
    public boolean MTGpioOn() {
        try {
            CtrlFile.write("-wdout64 1");
            CtrlFile.flush();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 下电
     *
     * @return bool
     */
    public boolean MTGpioOff() {
        try {
            CtrlFile.write("-wdout64 0");
            CtrlFile.flush();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }
}
