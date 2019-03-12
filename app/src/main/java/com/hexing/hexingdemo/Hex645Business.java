package com.hexing.hexingdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.dragon.bluetooth.BleService;

import java.util.Arrays;

/**
 * 645协议 业务处理类
 *
 * @author caibinglong
 *         date 2018/3/8.
 *         desc desc
 */

public class Hex645Business {
    private Context mContext;
    private BleService mBleService;
    private boolean mIsBind;
    private iHex645Business listener;
    public final static int CONNECTED = 1;
    public final static int DISCONNECT = 0;
    public final static int CONNECTING = 2;

    public void initBlueService(Context mContext) {
        this.mContext = mContext;
        Intent serviceIntent = new Intent(this.mContext, BleService.class);
        mContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unBindService() {
        if (this.mContext != null && mIsBind) {
            mContext.unbindService(serviceConnection);
        }
    }

    public void addListener(iHex645Business listener) {
        this.listener = listener;
    }

    /**
     * 扫描蓝牙
     *
     * @param blueName      蓝牙名称
     * @param isAutoConnect 是否自动链接
     */
    public void scanLeDevice(String blueName, boolean isAutoConnect) {
        if (mBleService != null) {
            mBleService.scanLeDevice(blueName, isAutoConnect);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();
            mIsBind = true;
            if (mBleService != null) {
                if (listener != null) {
                    listener.bindServerState(true);
                }
                setListener();
            } else {
                //服务绑定失败
                listener.bindServerState(false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
            mIsBind = false;
            if (listener != null) {
                listener.bindServerState(false);
            }
        }
    };

    /**
     * 开启蓝牙
     *
     * @return bool
     */
    public boolean enableBluetooth() {
        if (mBleService != null) {
            if (mBleService.initialize() && mBleService.enableBluetooth(true)) {
                return true;
            }
        }
        return false;
    }

    private void setListener() {
        //Ble扫描回调
        mBleService.setOnLeScanListener(new BleService.OnLeScanListener() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //每当扫描到一个Ble设备时就会返回，（扫描结果重复的库中已处理）
                Log.e("", device.getName() + "||" + device.getAddress());

            }
        });
        //Ble连接回调
        mBleService.setOnConnectListener(new BleService.OnConnectionStateChangeListener() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //Ble连接已断开
                    Log.e("", "Ble连接已断开");
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    //Ble正在连接
                    Log.e("", "Ble正在连接");
                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e("", "Ble已连接");
                    //Ble已连接

                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    //Ble正在断开连接
                    Log.e("", "Ble正在断开连接");
                    if (listener != null) {
                        listener.onConnectState(0);
                    }
                }
            }
        });
        //Ble服务发现回调
        mBleService.setOnServicesDiscoveredListener(new BleService.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.e("", "发现服务" + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 得到服务对象
                    mBleService.setIsHexBlueDevice(true);
                    mBleService.verifyConnect(HexBluetoothConstant.VERIFY_READ_WRITE_SERVICE, HexBluetoothConstant.VERIFY_READ_WRITE_CHARACTERISTIC);
                    mBleService.setCharacteristicNotification(HexBluetoothConstant.METER_NOTIFY_SERVICE, HexBluetoothConstant.METER_NOTIFY_CHARACTERISTIC, true);
                } else {
                    if (listener != null) {
                        listener.onConnectState(0);
                    }
                }

            }
        });
        //Ble数据回调
        mBleService.setOnDataAvailableListener(new BleService.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                //处理特性读取返回的数据
                byte[] bytes = characteristic.getValue();
                Log.e("", "读取返回=" + Arrays.toString(bytes));

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                //处理通知返回的数据
                byte[] bytes = characteristic.getValue();
                StringBuilder stringBuilder = new StringBuilder();
                for (byte byteChar : bytes) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.e("", "通知返回数据=" + stringBuilder.toString());
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.e("", "onDescriptorRead通知返回数据=");

            }

            @Override
            public void onVerifyFinish() {
                if (listener != null) {
                    listener.onConnectState(1);
                }
            }
        });
    }

    /**
     * 发送数据
     *
     * @param data 645协议帧
     * @return bool
     */
    public boolean write(String data) {
        if (mBleService != null) {
            return mBleService.writeCharacteristic(HexBluetoothConstant.METER_WRITE_SERVICE, HexBluetoothConstant.METER_WRITE_CHARACTERISTIC, StringUtil.hexStringToByte(data));
        }
        return false;
    }

    public interface iHex645Business {
        void bindServerState(boolean bool);

        void onConnectState(int state);
    }

}
