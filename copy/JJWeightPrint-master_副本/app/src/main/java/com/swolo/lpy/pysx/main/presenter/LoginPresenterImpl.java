package com.swolo.lpy.pysx.main.presenter;

import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.HttpManager;
import java.util.Map;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.util.Log;

public class LoginPresenterImpl implements MainContract.LoginPresenter {
    private MainContract.LoginView mView;

    public LoginPresenterImpl(MainContract.LoginView view) {
        this.mView = view;
        Log.d("LoginPresenterImpl", "LoginPresenterImpl created");
    }

    @Override
    public void login(String phone) {
        Log.d("LoginPresenterImpl", "login: 开始登录, phone=" + phone);
        HttpManager.getInstance()
                .request(
                        HttpManager.getInstance().getApi(GoodsApi.class).disAndroidLoginWork(phone),
                        new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d("LoginPresenterImpl", "login: 登录请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("LoginPresenterImpl", "login: 登录请求出错, error=" + e.getMessage());
                        if (mView != null) mView.onLoginFail(e.getMessage());
                    }

                    @Override
                    public void onNext(Map<String, Object> data) {
                        Log.d("LoginPresenterImpl", "login: 登录返回, data=" + data);
                        if (mView != null) mView.onLoginSuccess(data);
                    }
                });
    }
}