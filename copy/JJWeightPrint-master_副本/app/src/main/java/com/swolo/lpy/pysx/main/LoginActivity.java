package com.swolo.lpy.pysx.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.CommonResponse;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.main.presenter.LoginPresenterImpl;
import com.swolo.lpy.pysx.main.presenter.MainContract;
import com.swolo.lpy.pysx.ui.BaseActivity;

import org.json.JSONException;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.util.Log;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.swolo.lpy.pysx.main.modal.NxDistributerUserEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerEntity;

import java.util.Map;

public class LoginActivity extends BaseActivity implements MainContract.LoginView {

    private EditText etPhoneNumber;
    private Button btnLogin;
    private MainContract.LoginPresenter loginPresenter;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查本地缓存
        SharedPreferences sp = getSharedPreferences("user_cache", MODE_PRIVATE);
        String userInfoJson = sp.getString("userInfo", null);
        String disInfoJson = sp.getString("disInfo", null);
        SharedPreferences userInfoPrefs = getSharedPreferences("user_info", MODE_PRIVATE);
        int distributerId = userInfoPrefs.getInt("distributer_id", -1);

        if (userInfoJson != null && disInfoJson != null && distributerId != -1) {
            Log.d("LoginActivity", "检测到本地有缓存用户信息，自动跳转到出库页面");
            Intent intent = new Intent(this, StockOutActivity.class);
            intent.putExtra("distributer_id", distributerId);
            startActivity(intent);
            finish();
            return;
        }

        Log.d("LoginActivity", "onCreate: LoginActivity created");

        loginPresenter = new LoginPresenterImpl(this);
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setVisibility(View.GONE);
        }
        ImageButton btnSetting = findViewById(R.id.btn_settings);
        if (btnSetting != null) {
            btnSetting.setVisibility(View.GONE);
        }

        initView();
        initData();
        setView();
        bindAction();
    }

    // 初始化视图
    @Override
    protected void initView() {
        // 获取内容容器
        View contentContainer = findViewById(R.id.content_container);
        if (contentContainer == null) {
            throw new RuntimeException("找不到内容容器");
        }

        // 加载登录布局
        View loginLayout = getLayoutInflater().inflate(R.layout.activity_login, (ViewGroup) contentContainer, false);
        ((ViewGroup) contentContainer).addView(loginLayout);

        // 从登录布局中查找视图
        etPhoneNumber = loginLayout.findViewById(R.id.et_phone_number);
        btnLogin = loginLayout.findViewById(R.id.btn_login);
        Log.d("LoginActivity", "initView: View initialized");
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        // 可选：初始化数据
    }

    @Override
    protected void setView() {
        // 可选：设置界面相关属性
    }

    @Override
    protected void bindAction() {
        // 添加手机号输入监听
        etPhoneNumber.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s.length() == 11) {
                    btnLogin.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    btnLogin.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                Log.d("LoginActivity", "点击登录，手机号: " + phoneNumber);
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                } else {
                    loginPresenter.login(phoneNumber);
                }
            }
        });
    }


    // MainContract.LoginView 实现
    @Override
    public void onLoginSuccess(Object data) {
        Log.d("LoginActivity", "onLoginSuccess: 登录成功, data=" + data);
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

        if (data instanceof Map) {
            Map dataMap = (Map) data;
            Object userInfoObj = dataMap.get("userInfo");
            Object disInfoObj = dataMap.get("disInfo");

            // 1. 解析为实体类
            NxDistributerUserEntity userInfo = null;
            NxDistributerEntity disInfo = null;
            try {
                String userInfoJson = gson.toJson(userInfoObj);
                String disInfoJson = gson.toJson(disInfoObj);
                userInfo = gson.fromJson(userInfoJson, NxDistributerUserEntity.class);
                disInfo = gson.fromJson(disInfoJson, NxDistributerEntity.class);
                Log.d("LoginActivity", "userInfo实体: " + userInfoJson);
                Log.d("LoginActivity", "disInfo实体: " + disInfoJson);

                // 清除所有缓存
                SharedPreferences userCache = getSharedPreferences("user_cache", MODE_PRIVATE);
                SharedPreferences userInfoPrefs = getSharedPreferences("user_info", MODE_PRIVATE);
                SharedPreferences deptCache = getSharedPreferences("department_cache", MODE_PRIVATE);
                SharedPreferences printerCache = getSharedPreferences("printer_cache", MODE_PRIVATE);

                userCache.edit().clear().apply();
                userInfoPrefs.edit().clear().apply();
                deptCache.edit().clear().apply();
                printerCache.edit().clear().apply();
                Log.d("LoginActivity", "已清除所有缓存");

                // 保存分销商ID到SharedPreferences
                if (userInfo != null && userInfo.getNxDiuDistributerId() != null) {
                    userInfoPrefs.edit()
                        .putInt("distributer_id", userInfo.getNxDiuDistributerId())
                        .apply();
                    Log.d("LoginActivity", "保存分销商ID: " + userInfo.getNxDiuDistributerId());
                } else {
                    Log.e("LoginActivity", "用户信息中未找到分销商ID");
                    Toast.makeText(this, "登录失败：未找到分销商信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. 缓存到本地
                userCache.edit()
                    .putString("userInfo", userInfoJson)
                    .putString("disInfo", disInfoJson)
                    .apply();
                Log.d("LoginActivity", "userInfo和disInfo已缓存到本地");

                // 3. 跳转到出库页面
                Intent intent = new Intent(this, StockOutActivity.class);
                intent.putExtra("distributer_id", userInfo.getNxDiuDistributerId());
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Log.e("LoginActivity", "解析实体失败: " + e.getMessage());
                Toast.makeText(this, "登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoginFail(String error) {
        Log.d("LoginActivity", "onLoginFail: 登录失败, error=" + error);
        Toast.makeText(this, "登录失败: " + error, Toast.LENGTH_SHORT).show();
    }
} 