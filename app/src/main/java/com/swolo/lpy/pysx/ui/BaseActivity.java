package com.swolo.lpy.pysx.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.ui.view.CommonLoadingDialog;
import com.swolo.lpy.pysx.util.ActivityUtil;
import com.swolo.lpy.pysx.main.SettingsActivity;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected TextView tvTitle;
    protected ImageView ivAvatar;
    protected ImageButton btnMore;
    protected ImageButton btnCircle;
    private CommonLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_base);
        
        // 动态加载子类布局到内容区
        int contentRes = getContentViewRes();
        if (contentRes != 0) {
            ViewGroup container = findViewById(R.id.content_container);
            if (container != null) {
                getLayoutInflater().inflate(contentRes, container, true);
            }
        }

        ivAvatar = findViewById(R.id.iv_avatar);
        tvTitle = findViewById(R.id.tv_title);
        btnMore = findViewById(R.id.btn_more);
        btnCircle = findViewById(R.id.btn_circle);

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
