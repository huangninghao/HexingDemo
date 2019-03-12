package cn.hexing.dlms.protocol.comm;

import android.os.AsyncTask;

import cn.hexing.iComm.ICommAction;
import cn.hexing.model.CommPara;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * @author 王昌豹
 * @version 1.0
 * @Title: Tcp通讯类
 * @Description: Tcp Socket
 * @Copyright: Copyright (c) 2016
 * @Company 杭州海兴电力科技
 */
public class CommTcp implements ICommAction {
    private Socket socket = null;
    private String hostIp = "10.10.100.254";
    private int port = 8899;

    public void setSocket(String hostIp, int port) {
        this.hostIp = hostIp;
        this.port = port;
    }

    @Override
    public boolean openDevice(CommPara para) {
        return false;
    }

    @Override
    public boolean closeDevice() {
        return false;
    }

    @Override
    public byte[] receiveBytToCallback(int waitT, int byteLen) {
        return new byte[0];
    }

    @Override
    public byte[] receiveByt(int sleepT, int waitT) {

        byte[] strResult;

        getLogTask task = new getLogTask();
        try {
            strResult = task.execute().get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] receiveByt(int SleepT, int WaitT, int byteLen) {
        return new byte[0];
    }

    @Override
    public boolean sendByt(byte[] sndByte) {
        try {
            if (socket == null) {
                socket = new Socket(hostIp, port);
            }
            OutputStream out = socket.getOutputStream();
            out.write(sndByte);
        } catch (IOException ex) {
            String str = ex.getMessage();
        }

        return true;
    }

    @Override
    public void setBaudRate(int baudRate) {

    }

    @Override
    public void setBaudRate(int baudRate, char parity, int dataBit, int stopBit) {

    }

    public class getLogTask extends AsyncTask<Void, Void, byte[]> {

        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {

        }


        @Override
        protected byte[] doInBackground(Void... param) {
            List<Byte> arrList = new ArrayList<>();
            byte[] bytResult = new byte[0];
            try {

                Socket s = new Socket(hostIp, port);
                s.setSoTimeout(3000);
                InputStream inputStream = s.getInputStream();
                DataInputStream input = new DataInputStream(inputStream);

                byte[] b = new byte[10000];

                while (input.read(b) > 0) {

                    for (int i = 0; i < b.length; i++) {
                        arrList.add(b[i]);
                    }

                }

                Object[] arr = arrList.toArray();
                bytResult = new byte[arr.length];

                for (int i = 0; i < arr.length; i++) {
                    bytResult[i] = (Byte) arr[i];
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return bytResult;
        }


    }

}
