package com.hexing.hexingdemo.presenter;


import com.hexing.hexingdemo.view.LoginView;
import com.hexing.libhexbase.activity.BasePresenter;

/**
 * @author caibinglong
 *         date 2018/4/24.
 *         desc desc
 */

public class LoginPresenter extends BasePresenter<LoginView> {

    public LoginPresenter(LoginView view) {
        attachView(view);
    }

    public void login() {
        getView().showToast("");
        //省略处理业务
        getView().hideLoading();
    }


}
