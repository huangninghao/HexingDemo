package com.android.SerialPort;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    public FileDescriptor mFd;
    public FileInputStream mFileInputStream;
    public FileOutputStream mFileOutputStream;
    private int dev_num = 0;

    public SerialPort() {
    }

    public FileDescriptor open(String path, int baudrate, int nBits, char nEvent, int nStop) {
        mFd = open(path, baudrate, nBits, nEvent, nStop, 0);
        if (mFd == null) {
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        return mFd;
    }


    public void Close() {
        close();
        SetPowerState(0x18);
    }

    public InputStream getInputStream(FileDescriptor mfd) {
        return mFileInputStream;
    }

    public void SetPowerState(int Powerstate) {
        SetIoState(dev_num, Powerstate);

    }

    public OutputStream getOutputStream(FileDescriptor mfd) {
        return mFileOutputStream;
    }

    private native FileDescriptor open(String path, int baudrate, int nBits, char nVerify, int nStop, int flags);

    public native void close();

    private native int SetIoState(int dev_num, int controlcode);

    static {
        System.loadLibrary("serialport");
    }

}

