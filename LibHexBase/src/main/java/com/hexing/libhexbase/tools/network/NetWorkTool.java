package com.hexing.libhexbase.tools.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class NetWorkTool {

    private final static String TAG = NetWorkTool.class.getSimpleName();

    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_NOT_CONNECTED = -1;
    public static final int TYPE_OF_NO_PASS = 1;//WIFICIPHER_NOPASS
    public static final int TYPE_OF_WEP = 2;//WIFICIPHER_WEP
    public static final int TYPE_OF_WPA = 3;//WIFICIPHER_WPA
    public static final String ACTION_NETWORK = "android.net.conn.CONNECTIVITY_CHANGE";

    private final static String NULL = "NULL";

    /**
     * 打开网络设置界面
     */
    public static void openWifiSetting(Context context) {
        context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
    }

    /**
     * wifi开关操作
     *
     * @param context 上下文
     */
    public static void ctlWifi(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            if (mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(false);
            } else {
                mWifiManager.setWifiEnabled(true);
            }
        }
    }

    /**
     * wifi开关操作
     *
     * @param context 上下文
     * @param tag     true:open ; false:close
     */
    public static void ctlWifi(Context context, boolean tag) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            if (mWifiManager.isWifiEnabled()) {
                if (!tag) {
                    mWifiManager.setWifiEnabled(false);
                }
            } else {
                if (tag) {
                    mWifiManager.setWifiEnabled(true);
                }
            }
        }
    }

    /**
     * 检查当前Wifi网卡状态
     *
     * @param context 上下文
     * @return int
     */
    public static int checkWifiState(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            // 0 "网卡正在关闭"  1"网卡已经关闭" 2"网卡正在打开" 3"网卡已经打开"
            return mWifiManager.getWifiState();
        }
        Log.i(TAG, "没有获取到状态");

        return WifiManager.WIFI_STATE_UNKNOWN;
    }

    /**
     * 扫描周边网络
     */
    public static List<ScanResult> scanWifi(Context context) {
        List<ScanResult> listResult = new ArrayList<>();
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            boolean isCan = mWifiManager.startScan();
            if (isCan) {
                listResult = mWifiManager.getScanResults();
                if (listResult != null) {
                    Log.i(TAG, "当前区域存在无线网络，请查看扫描结果");
                } else {
                    Log.i(TAG, "当前区域没有无线网络");
                }
            } else {
                Log.i(TAG, "扫描失败");
            }
        }
        return listResult;
    }

    /**
     * 得到扫描结果
     */
    public static String getScanResult(Context context) {
        // 每次点击扫描之前清空上一次的扫描结果
        StringBuffer mStringBuffer = new StringBuffer();
        ScanResult mScanResult = null;
        // 开始扫描网络
        List<ScanResult> listResult = scanWifi(context);
        if (listResult != null) {
            for (int i = 0; i < listResult.size(); i++) {
                mScanResult = listResult.get(i);
                mStringBuffer = mStringBuffer.append("NO.").append(i + 1)
                        .append(" :").append(mScanResult.SSID).append("->")
                        .append(mScanResult.BSSID).append("->")
                        .append(mScanResult.capabilities).append("->")
                        .append(mScanResult.frequency).append("->")
                        .append(mScanResult.level).append("->")
                        .append(mScanResult.describeContents()).append("\n\n");
            }
        }
        Log.i(TAG, mStringBuffer.toString());
        return mStringBuffer.toString();
    }

    /**
     * 断开当前连接的网络
     */
    public static void disconnectWifi(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            int netId = getNetworkId(context);
            mWifiManager.disableNetwork(netId);
            mWifiManager.disconnect();
        }
    }

    /**
     * 验证网络是否连接
     *
     * @param ctx 上下文
     * @return bool
     */
    public static boolean isConnectNet(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
        return !(networkinfo == null || !networkinfo.isAvailable());
    }

    //获取活动网络类型
    public static int getActiveNetWorkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                if (activeNetwork.isAvailable() && activeNetwork.isConnected()) {
                    return TYPE_WIFI;
                }
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (activeNetwork.isAvailable() && activeNetwork.isConnected()) {
                    return TYPE_MOBILE;
                }
            }
        }
        return TYPE_NOT_CONNECTED;
    }


    /**
     * 判断mobile是否连接
     *
     * @param context 上下文
     * @return bool
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                    if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 得到连接的ID
     */
    public static int getNetworkId(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            return (wifiInfo == null) ? 0 : wifiInfo.getNetworkId();
        }
        return -1;
    }

    /**
     * 得到IP地址
     */
    public static int getIPAddress(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            return (wifiInfo == null) ? 0 : wifiInfo.getIpAddress();
        }
        return 0;
    }

    /**
     * 指定配置好的网络进行连接
     *
     * @param context 上下文
     * @param index   pos
     * @return bool
     */
    public static boolean connectConfiguration(Context context, int index) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
            if (index >= list.size()) {
                return false;
            }
            mWifiManager.disconnect();
            mWifiManager.enableNetwork(list.get(index).networkId, true);
            return mWifiManager.reconnect();
        }
        return false;
    }

    /**
     * 连接指定ssId
     *
     * @param context     上下文
     * @param networkSSID ssId
     * @return bool
     */
    public static boolean connect(Context context, String networkSSID) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    mWifiManager.disconnect();
                    mWifiManager.enableNetwork(i.networkId, true);
                    return mWifiManager.reconnect();
                }
            }
        }
        return false;
    }

    // 得到MAC地址
    public static String getMacAddress(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return UUID.randomUUID().toString();
        } else {
            WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (mWifiManager != null) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                return (wifiInfo == null) ? NULL : wifiInfo.getMacAddress();
            }
            return NULL;
        }
    }

    /**
     * 得到接入点的BSSID
     *
     * @param context 上下文
     * @return String
     */
    public static String getBSSID(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            return (wifiInfo == null) ? NULL : wifiInfo.getBSSID();
        }
        return NULL;
    }


    /**
     * 得到WifiInfo的所有信息包
     *
     * @param context 上下文
     * @return String
     */
    public static String getWifiInfo(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            return (wifiInfo == null) ? NULL : wifiInfo.toString();
        }
        return NULL;
    }


    /**
     * 获取网络信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                    return mNetworkInfo;
                }
            }
        }
        return null;
    }

    /**
     * 创建 wifi配置信息
     *
     * @param context  上下文
     * @param SSID     ssID
     * @param password 密码
     * @param Type     加密类型
     * @return WifiConfiguration
     */
    public static WifiConfiguration createWifiInfo(Context context, String SSID, String password, int Type) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiConfiguration config = new WifiConfiguration();
        if (mWifiManager == null) {
            return config;
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = isExists(context, SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        if (Type == TYPE_OF_NO_PASS) {// WIFICIPHER_NOPASS
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == TYPE_OF_WEP) {// WIFICIPHER_WEP
//			config.hiddenSSID = true;
//			config.flag = WifiConfiguration.Status.DISABLED;
//			config.priority = 40;
//			config.wepKeys[0] = "\"" + Password + "\"";
//			config.wepTxKeyIndex = 0;
//			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//		    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
//		    config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//		    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//		    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//		    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//		    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//		    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//		    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            //if your password is in hex, you do not need to surround it by quotes
            config.hiddenSSID = true;
            if (Pattern.matches("[0-9a-fA-F]+", password) && password.length() % 2 == 0) {
                config.wepKeys[0] = password;
                System.out.println("qiang ma dan");
            } else {
                config.wepKeys[0] = "\"" + password + "\"";
            }
            config.wepTxKeyIndex = 0;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        }
        if (Type == TYPE_OF_WPA) { // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        mWifiManager.addNetwork(config);
        return config;
    }

    /**
     * 验证是否存在 ssId
     *
     * @param context 上下文
     * @param SSID    ssID
     * @return WifiConfiguration
     */
    private static WifiConfiguration isExists(Context context, String SSID) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }


    /**
     * 数据流量开关
     *
     * @param ctx     上下文
     * @param enabled bool
     */
    public static void toggleMobileData(Context ctx, boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null) {
            return;
        }
        try {
            // 取得ConnectivityManager类
            Class<?> conMgrClass = Class.forName(conMgr.getClass().getName());
            // 取得ConnectivityManager类中的对象mService
            Field iConMgrField = conMgrClass.getDeclaredField("mService");
            // 设置mService可访问
            iConMgrField.setAccessible(true);
            // 取得mService的实例化类IConnectivityManager
            Object iConMgr = iConMgrField.get(conMgr);
            // 取得IConnectivityManager类
            Class<?> iConMgrClass = Class.forName(iConMgr.getClass().getName());
            // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            Method setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            // 设置setMobileDataEnabled方法可访问
            setMobileDataEnabledMethod.setAccessible(true);
            // 调用setMobileDataEnabled方法
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    /**
     * @return bool
     * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     */
    public static boolean ping() {
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuilder = new StringBuilder();
            String content;
            while ((content = in.readLine()) != null) {
                stringBuilder.append(content);
            }
            System.out.println(TAG + "||------ping-----result content : " + stringBuilder.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                return true;
            }
        } catch (IOException e) {
            System.out.println(TAG + "||" + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(TAG + "||" + e.getMessage());
        }

        return false;
    }
}
