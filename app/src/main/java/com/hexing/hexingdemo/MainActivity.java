package com.hexing.hexingdemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import com.cbl.timepicker.TimePickerDialog;
import com.cbl.timepicker.data.Type;
import com.cbl.timepicker.listener.OnDateSetListener;
import com.hexing.hexingdemo.bean.UserInfoBean;
import com.hexing.hexingdemo.model.DataModel;
import com.hexing.hexingdemo.model.ObisBean;
import com.hexing.hexingdemo.model.ObjectNodeModel;
import com.hexing.hexingdemo.presenter.LoginPresenter;
import com.hexing.hexingdemo.queue.PrintTask;
import com.hexing.hexingdemo.queue.Priority;
import com.hexing.hexingdemo.queue.TaskQueue;
import com.hexing.hexingdemo.view.LoginView;
import com.hexing.libhexbase.activity.HexMVPBaseActivity;
import com.hexing.libhexbase.log.HexLog;
import com.hexing.libhexbase.tools.CBLSystemUtil;
import com.hexing.libhexbase.tools.XmlHelperUtil;
import com.hexing.libhexbase.tools.file.FileUtil;
import com.hexing.libhexbase.view.HexProgressButton;
import com.othershe.dutil.DUtil;
import com.othershe.dutil.callback.DownloadCallback;
import com.othershe.dutil.download.DownloadManger;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cn.hexing.HexAction;
import cn.hexing.HexDevice;
import cn.hexing.ParaConfig;
import cn.hexing.dlms.HexClientAPI;
import cn.hexing.dlms.HexDataFormat;

import cn.hexing.HexStringUtil;
import cn.hexing.dlms.IHexListener;
import cn.hexing.dlms.protocol.bll.dlmsService;
import cn.hexing.dlt645.HexClient645API;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.model.TranXADRAssist;


public class MainActivity extends HexMVPBaseActivity<LoginPresenter> implements View.OnClickListener, LoginView, OnDateSetListener {

    private TextView tvTest;
    private TextView tvSend;
    private TextView tvRestart;
    private TextView tvRF;
    // private SearchView searchView;
    private TextView tvLocation;
    private EditText etLocation;
    private TextView tvDown;
    private TextView tvRecycler;
    private TextView tvTime;
    private TextView tvQueue;
    private TextView tvRx;
    private HexProgressButton btn;
    HexProgressButton btn2;
    private TextView mapBox;
    /**
     * 正向有功总电能
     */
    public final static String ACTIVE_ENERGY = "3#1.0.1.8.0.255#2";
    public final static String READ_VERSION = "1#0.0.96.1.146.255#2";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTest = (TextView) findViewById(R.id.tvTest);
        tvSend = (TextView) findViewById(R.id.tvSend);
        tvRestart = (TextView) findViewById(R.id.tvRestart);
        tvRF = (TextView) findViewById(R.id.tvRF);
        //searchView = (SearchView) findViewById(R.id.searchView);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDown = (TextView) findViewById(R.id.tvDown);
        tvRx = findViewById(R.id.tvRx);
        tvQueue = findViewById(R.id.tvQueue);
        tvRecycler = findViewById(R.id.tvRecycler);
        tvTime = findViewById(R.id.tvTime);
        btn = findViewById(R.id.button);
        btn2 = findViewById(R.id.button2);
        mapBox = findViewById(R.id.mapBox);
        tvTest.setOnClickListener(this);
        tvSend.setOnClickListener(this);
        tvRestart.setOnClickListener(this);
        tvRF.setOnClickListener(this);
        tvLocation.setOnClickListener(this);
        tvDown.setOnClickListener(this);
        tvRecycler.setOnClickListener(this);
        tvTime.setOnClickListener(this);
        tvQueue.setOnClickListener(this);
        tvRx.setOnClickListener(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn.startRotate();
                //btn.animFinish();
//                btn.animError();
//                 btn.removeDrawable();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn2.startRotate();
            }
        });
        mapBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toActivity(MapBoxActivity.class);
            }
        });
        //business = new Hex645Business();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        Log.e("量纲", dlmsService.parseScale(18777887773444l, 1));
        Log.e("量纲", dlmsService.parseScale(18777887772333l, -1));
        Log.e("量纲", dlmsService.parseScale(18777444474455l, -2));

        Log.e("量纲", dlmsService.parseScale(1344, 3));
        Log.e("量纲", dlmsService.parseScale(18777887773444l, -2));
        //Log.e("量纲",dlmsService.getDecimalValue(18777887773444l,-5));

        //  HexStringUtil
        HexLog.e("协议", HexStringUtil.upgradeEncrypt(":000000EF11"));
        HexLog.e("协议", HexStringUtil.upgradeEncrypt(":020000040000FA"));
        HexLog.e("协议", HexStringUtil.upgradeEncrypt(":2000000008070010E90B0000A50A0000A70A0000000000000000000000000000000000006D"));

        //List<TranXADRAssist> list = new ArrayList<>();
        TranXADRAssist assist = new TranXADRAssist();
        assist.structList = new ArrayList<>();

        assist.dataType = HexDataFormat.DISPLAY_ARRAY;

        TranXADRAssist.StructBean structBean = new TranXADRAssist.StructBean();

        structBean = new TranXADRAssist.StructBean();
        structBean.dataType = HexDataFormat.OCTET_STRING;
        assist.structList.add(structBean);

        structBean = new TranXADRAssist.StructBean();
        structBean.dataType = HexDataFormat.UNSIGNED;
        structBean.writeData = "01";
        assist.structList.add(structBean);


//
//        assist.structList.get(0).beanItems = new ArrayList<>();
//
        byte[] bytes = new byte[]{1, 3,
                2, 2, 9, 6, 0, 0, 10, 0, 64, -1, 18, 0, 2,
                2, 2, 9, 6, 0, 0, 10, 0, 64, -1, 18, 0, 2,
                2, 2, 9, 6, 0, 0, 10, 0, 64, -1, 18, 0, 2};
        String a = Integer.toBinaryString(0);
        String b = Integer.toBinaryString(3);
        String c = Integer.toBinaryString(10);

        a = HexStringUtil.padRight(a, 16, '0');
        b = HexStringUtil.padRight(b, 16, '0');
        c = HexStringUtil.padRight(c, 16, '0');

        System.out.println("binary=" + a);
        System.out.println("binary=" + b);
        System.out.println("binary=" + c);

        StringBuilder buffer = new StringBuilder(a);
        buffer.reverse();

        System.out.println("binary a=" + buffer);

        buffer = new StringBuilder(b);
        buffer.reverse();

        System.out.println("binary b=" + buffer);

        buffer = new StringBuilder(c);
        buffer.reverse();
        System.out.println("binary c=" + buffer);

        ParaConfig.Builder config = new ParaConfig.Builder();
        config.setComName(HexDevice.COMM_NAME_ZIGBEE);

        String str = "7E 00 07 8B 01 00 00 00 00 00 73 7E 00 18 90 FF FF FF FF FF FF FF FF 00 00 01 68 AA AA AA AA AA AA 68 81 00 4D 16 C6".replace(" ", "");

        String temByte = "46 50 36 30 30 36 37 56 30 34 52 30 30 35 4D 30 30 31 28 31 38 30 37 32 30 31 34 29 00 00";
        temByte = temByte.replace(" ", "");
        byte[] bytes1 = HexStringUtil.hexToByte(temByte);
        String asciiStr = byteToString(bytes1, 0, bytes1.length);
        System.out.println("ascii=" + asciiStr);

    }

    private void showSystemParameter() {
        String TAG = "系统参数：";
        Log.e(TAG, "手机厂商：" + CBLSystemUtil.getDeviceBrand());
        Log.e(TAG, "手机型号：" + CBLSystemUtil.getSystemModel());
        Log.e(TAG, "手机当前系统语言：" + CBLSystemUtil.getSystemLanguage());
        Log.e(TAG, "Android系统版本号：" + CBLSystemUtil.getSystemVersion());
        Log.e(TAG, "手机IMEI：" + CBLSystemUtil.getIMEI(getApplicationContext()));
        Log.e(TAG, "手机Serial no：" + CBLSystemUtil.getSerialNumber());
    }

    @Override
    protected LoginPresenter createPresenter() {
        return new LoginPresenter(this);
    }

    @Override
    public void show(String data) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //business.unBindService();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvTest:
                // business.enableBluetooth();
                // business.scanLeDevice("00000000001", true);

                //Socket socket = new Socket();
                //socket.setSoTimeout(1000);
                //   HexClientAPI.getInstance().setStrMeterNo("17090901");
                //  HexClientAPI.getInstance().setDeviceMethod(HexDevice.KT50, HexDevice.METHOD_RF);
                //HexClientAPI.getInstance().sendTest("7E0004080143486B");
                //HexClientAPI.getInstance().sendTest("7E000508014348016A");
                //HexClientAPI.getInstance().sendTest("7Ea0230104c955039329e8818014050207d0060207d00704000000010804000000013af27e".toUpperCase());
//                TranXADRAssist tranXADRAssist = new TranXADRAssist();
//                tranXADRAssist.obis = ACTIVE_ENERGY;
//                tranXADRAssist.actionType = HexAction.ACTION_READ;
//                tranXADRAssist.writeData = "U16";
//                tranXADRAssist.recType = "U16";
//                HexClientAPI.getInstance().action(tranXADRAssist);

                //String crc = CRCUtil.getCRC16("00050801434801");
                //ToastUtils.showToast(this, "上电发送数据完成");
                // ViewGroup customView = (ViewGroup) View.inflate(this, R.layout.item_test, null);
                //StyledDialog.buildCustomAsAdStyle(customView, Gravity.CENTER).show();


                //System.out.println("jni=" + SayTest.sayHello());
                //HexClient645API.readGPRS("", new ReceiveModel());
                InputStream stream = FileUtil.openAssetsFile(App.getInstance(), "ProfileObject.xml");
                List<ObjectNodeModel> model = XmlHelperUtil.getInstance().getList(ObjectNodeModel.class, stream, "ObjectNode");

                stream = FileUtil.openAssetsFile(App.getInstance(), "Obis.xml");
                List<ObisBean> modelList = XmlHelperUtil.getInstance().getList(ObisBean.class, stream, "OBIS");
                System.out.println();
                break;
            case R.id.tvSend:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String data = "6801000000000068110433333333";
                        String cs = StringUtil.checkSum("6801000000000068110433333333");
                        String last = "16";
                        data = data + cs + last;
                        //data = "6801000000000068201C983333334444444469666C6963666B6A6A65636567656469676B66645C16";
                        data = "6801000000000068110433333333B216";
                        // boolean isSuccess = business.write(data);
                        //Log.e("", "数据发送=" + isSuccess);
                    }
                }).start();
                break;
            case R.id.tvRestart:
                //system.ping();
                try {
                    Log.v("", "root Runtime->reboot");
                    Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot "});
                    proc.waitFor();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case R.id.tvRF:
                //HexClientAPI.getInstance().setDeviceMethod(HexDevice.KT50, HexDevice.METHOD_RF);
                //HexClientAPI.getInstance().read(ACTIVE_ENERGY, "U32");
                List<UserInfoBean> userInfoBeanList = new ArrayList<>();

                UserInfoBean bean = new UserInfoBean();
                bean.name = "test";
                bean.password = "123";
                userInfoBeanList.add(bean);

                bean = new UserInfoBean();
                bean.name = "test22";
                bean.password = "123456";
                userInfoBeanList.add(bean);
                //StringCache.putJavaBeanList("UserTest", userInfoBeanList);
                break;
            case R.id.tvLocation:
                ParaConfig.Builder builder = new ParaConfig.Builder();
                HexClientAPI api = builder.setDevice(HexDevice.KT50).
                        setCommMethod(4)
                        .setHandWaitTime(1500)
                        .setDataFrameWaitTime(3000).build();

                TranXADRAssist tran = new TranXADRAssist();
                tran.obis = READ_VERSION;
                tran.actionType = HexAction.ACTION_READ;
                tran.writeType = "Ascs";
                tran.recType = "Ascs";

                try {
                    api.action(tran);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                api.addListener(new IHexListener() {
                    @Override
                    public void onSuccess(TranXADRAssist data, int pos) {
                        super.onSuccess(data, pos);
                        HexLog.d("通讯测试", data.value);
                    }

                });
                break;
            case R.id.tvDown:
                //downApk();

                //StyledDialog.buildMdLoading().show();
//                StyledDialog.buildMdInput("登录", "请输入密码",
//                        "", new DialogListener() {
//                            @Override
//                            public void onFirst() {
//
//                            }
//
//                            @Override
//                            public void onSecond() {
//
//                            }
//
//                            @Override
//                            public boolean onInputValid(CharSequence input1, EditText editText1) {
//                                showToast("input1--input2:" + input1 + "--" + "is not accepted!");
//                                return false;
//                            }
//
//                            @Override
//                            public void onGetInput(CharSequence input1) {
//                                super.onGetInput(input1);
//                                showToast("input1:" + input1);
//                            }
//                        })
//                        .setInput2HideAsPassword(true)
//                        .setCancelable(true, true)
//                        .show();
                break;
            case R.id.tvRecycler:
                toActivity(RecyclerActivity.class);
                break;
            case R.id.tvTime:
                long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L;
//                TimePickerDialog mDialogAll = new TimePickerDialog.Builder()
//                        .setCallBack(this)
//                        .setCancelStringId("Cancel")
//                        .setSureStringId("Sure")
//                        .setTitleStringId("TimePicker")
//                        .setYearText("Year")
//                        .setMonthText("Month")
//                        .setDayText("Day")
//                        .setHourText("Hour")
//                        .setMinuteText("Minute")
//                        .setCyclic(false)
//                        .setMinMillSeconds(System.currentTimeMillis())
//                        .setMaxMillSeconds(System.currentTimeMillis() + tenYears)
//                        .setCurrentMillSeconds(System.currentTimeMillis())
//                        .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
//                        .setType(Type.ALL)
//                        .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
//                        .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
//                        .setWheelItemTextSize(12)
//                        .build();
                TimePickerDialog mDialogYearMonthDay = new TimePickerDialog.Builder()
                        .setType(Type.YEAR_MONTH)
                        .setYearText("Year")
                        .setMonthText("Month")
                        .setDayText("Day")
                        .setTitleStringId("TimePicker")
                        .setCallBack(this)
                        .build();
                mDialogYearMonthDay.show(getSupportFragmentManager(), Type.YEAR_MONTH_DAY.toString());
                break;
            case R.id.tvQueue:
                // 这里暂时只开一个窗口。
                TaskQueue taskQueue = new TaskQueue(1);
                taskQueue.start();
                for (int i = 0; i < 10; i++) {
                    PrintTask task = new PrintTask(i);
                    if (i == 8)
                        task.setPriority(Priority.Immediately);
                    taskQueue.add(task);
                }
                break;
            case R.id.tvRx:
                toActivity(TestActivity.class);
                break;
            default:
                mvpPresenter.login();
                //HexOkHexHttpManager.getInstance().download();

                break;

        }
    }


    private void downApk() {
        String mTempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download";
        final String APK_URL = "http://sqdd.myapp.com/myapp/qqteam/tim/down/tim.apk";
        DownloadManger downloadManger = DUtil.init(this.getApplicationContext())
                .url(APK_URL)
                .path(mTempPath)
                .name("ussd2.0" + ".apk")
                .childTaskCount(1)
                .build()
                .start(new DownloadCallback() {

                    @Override
                    public void onStart(long currentSize, long totalSize, float progress) {
                        Log.e("开始下载", "已经下载size=" + currentSize + "||totalSize =" + totalSize);
                    }

                    @Override
                    public void onProgress(long currentSize, long totalSize, float progress) {
                        Log.e("正在下载", "已经下载size=" + currentSize + "||totalSize =" + totalSize);
                    }

                    @Override
                    public void onPause() {
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onFinish(File file) {
                        Uri uri = Uri.fromFile(file);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        startActivity(intent);
                    }

                    @Override
                    public void onWait() {

                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {

    }

    public static String byteToString(byte[] bBytes, int iStartIndex, int iLen) {
        StringBuilder sb = new StringBuilder();
        int i = iStartIndex;
        char cChar = (char) 0;
        byte bCnt = (byte) 0;
        for (i = iStartIndex; i < iStartIndex + iLen; i++) {
            if ((bBytes[i] >= (byte) 0x20) && (bBytes[i] <= (byte) 0x7F)) {// 可显示ASCII
                cChar = (char) bBytes[i];
                sb.append(cChar);
            } else {// 不可显示的ASCII
                if ((bBytes[i] & 0xff) >= 0x80) {
                    bCnt++;
                    if (bCnt > 2) {
                        bCnt = 0;
                        String str = null;
                        try {
                            str = new String(bBytes, i - 2, 3, "UTF8");
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        sb.append(str);
                    }
                } else {
                    sb.append(String.format("%02x", bBytes[i]));
                }
            }
        }
        //
        return sb.toString();
    }

}
