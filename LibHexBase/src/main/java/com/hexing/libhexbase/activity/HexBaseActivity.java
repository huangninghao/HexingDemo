package com.hexing.libhexbase.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hexing.libhexbase.application.HexApplication;
import com.hexing.libhexbase.handler.CommonDoHandler;
import com.hexing.libhexbase.handler.CommonHandler;
import com.hexing.libhexbase.inter.InterBaseActivity;
import com.hexing.libhexbase.tools.ToastUtils;
import com.hexing.libhexbase.inter.InterBaseView;
import com.hexing.libhexbase.view.InjectedView;
import com.hexing.libhexbase.view.LoadingDialog;

/**
 * @author long
 *         Created by caibinglong
 *         on 2017/9/25.
 */

public class HexBaseActivity extends AppCompatActivity implements View.OnClickListener, CommonDoHandler,
        InterBaseActivity, InterBaseView {

    protected CommonHandler<HexBaseActivity> uiHandler;
    protected CommonHandler<HexBaseActivity> backgroundHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HexApplication.getInstance().register(this);// 将房间Activity加入activityList
        init();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        InjectedView.init(this);
        initView();
        initData();
        initListener();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void doHandler(Message msg) {
        uiHandler.handleMessage(msg);
    }

    //私有方法区域
    private void init() {
        uiHandler = new CommonHandler<>(this);
        HandlerThread handlerThread = new HandlerThread(getClass().getName());
        handlerThread.start();
        backgroundHandler = new CommonHandler<>(this, handlerThread.getLooper());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HexApplication.getInstance().unregister(this);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void showLoading(String msg) {
        LoadingDialog.showSysLoadingDialog(this, msg);
    }

    @Override
    public void showLoading() {
        LoadingDialog.showSysLoadingDialog(this, "");
    }

    @Override
    public void hideLoading() {
        LoadingDialog.cancelLoadingDialog();
    }

    @Override
    public void showToast(String msg) {
        ToastUtils.showToast(HexApplication.getInstance(), msg);
    }

    @Override
    public void showToast(int resId) {
        ToastUtils.showToast(HexApplication.getInstance(), resId);
    }

    /**
     * 动态申请权限
     */
    public void checkPermissions() {
        PackageManager pm = getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, getPackageName()))
                || (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getPackageName()));
        if (!permission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
    }

    /**
     * 申请权限
     *
     * @param permissions String[]
     * @param requestCode int
     */
    public void checkPermissions(final @NonNull String[] permissions, final @NonNull int requestCode) {
        PackageManager pm = getPackageManager();
        boolean permission = false;
        for (String item : permissions) {
            permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(item, getPackageName()));
            if (!permission) {
                break;
            }
        }

        if (!permission) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }

    }

    /**
     * 从传递的Intent中获取 根据
     * bundle获取值 获取从上一个界面传递的信息
     */
    protected Bundle getBundle() {
        return getIntent().getExtras();
    }

    /**
     * 界面跳转
     *
     * @param cls activity
     */
    public void toActivity(Class<?> cls) {
        toActivity(cls, null);
    }

    public void toActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void toActivityForResult(Class<?> cls, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    public void toActivityWithFinish(Class<?> cls) {
        toActivityWithFinish(cls, null);
    }

    public void toActivityWithFinish(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        this.finish();
    }

    /**
     * http://www.360doc.com/content/12/1225/15/6541311_256191828.shtml
     **/
    public void toActivityClearTop(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 注意本行的FLAG设置
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);//设置NO_ANIMATION在set之后才有效
        startActivity(intent);
    }

    /**
     * Activity 不会无限制重启 会调用 onNewIntent
     *
     * @param cls    activity
     * @param bundle bundle参数
     */
    public void toActivityClearTopWithState(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 注意本行的FLAG设置
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Activity 不会无限制重启 会调用 onNewIntent
     *
     * @param cls    activity
     * @param bundle bundle参数
     */
    public void toActivityClearTopWithState(Class<?> cls, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 注意本行的FLAG设置
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, requestCode);
    }
}
