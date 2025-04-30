package com.swolo.lpy.pysx.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.ui.view.CommonLoadingDialog;
import com.swolo.lpy.pysx.util.ActivityUtil;
import com.swolo.lpy.pysx.main.SettingsActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected TextView tvTitle;
    protected ImageButton btnBack;
    protected ImageButton btnSettings;
    private CommonLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_base);
        
        // 初始化基础视图
        btnBack = findViewById(R.id.btn_back);
//        tvTitle = findViewById(R.id.tv_title);
//        btnSettings = findViewById(R.id.btn_settings);
        
        // 设置返回按钮点击事件
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        initView();
        initData();
        setView();
        bindAction();
        
        mLoadingDialog = new CommonLoadingDialog(this);
    }

    protected abstract int getContentViewRes();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void setView();

    protected abstract void bindAction();

    protected void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showLoading() {
        if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void stopLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
