package com.swolo.lpy.pysx.main;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.gp.UsbListAcitivity;
import com.swolo.lpy.pysx.ui.BaseActivity;
import com.swolo.lpy.pysx.main.gp.Constant;
import com.swolo.lpy.pysx.main.DeviceConnFactoryManager;

import java.util.HashMap;

public class SettingsActivity extends BaseActivity {

    private Button btnPrinterConnect;
    private Button btnLogout;
    private TextView tvPrinterStatus;
    private static final String TAG = "SettingsActivity";
    private static final int CONN_STATE_DISCONN = 0x007;
    private int id = 0;
    private boolean isPrinterConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置ActionBar的返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
//             getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_revert);  // 使用与蓝牙页面相同的返回图标
        }

        initView();

        initData();
        setView();
        bindAction();
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.layout_base;
    }

    @Override
    protected void initView() {
        // 获取内容容器
        View contentContainer = findViewById(R.id.content_container);
        if (contentContainer == null) {
            throw new RuntimeException("找不到内容容器");
        }

        // 加载设置布局
        View settingsLayout = getLayoutInflater().inflate(R.layout.activity_settings, (ViewGroup) contentContainer, false);
        ((ViewGroup) contentContainer).addView(settingsLayout);

        // 从设置布局中查找视图
        btnPrinterConnect = settingsLayout.findViewById(R.id.btn_printer_connect);
        btnLogout = settingsLayout.findViewById(R.id.btn_logout);
        tvPrinterStatus = settingsLayout.findViewById(R.id.tv_printer_status);
        updatePrinterStatus();
    }

    @Override
    protected void initData() {
        // 初始化数据，目前无需特殊处理
    }

    @Override
    protected void setView() {
        // 设置视图属性
        android.widget.ImageButton btnSettings = findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    protected void bindAction() {
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPrinterConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrinterConnectDialog();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除缓存
                SharedPreferences sp = getSharedPreferences("user_cache", MODE_PRIVATE);
                sp.edit().clear().apply();
                // 跳转回登录页
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showPrinterConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择打印机连接方式");
        
        String[] items = {"USB连接", "蓝牙连接"};
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                // USB连接
                Intent intent = new Intent(this, UsbListAcitivity.class);
                startActivityForResult(intent, 1001);
            } else {
                // 蓝牙连接
                Intent intent = new Intent(this, BluetoothDeviceActivity.class);
                startActivityForResult(intent, 1002);
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void updatePrinterStatus() {
        StringBuilder status = new StringBuilder();
        
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
            DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0];
            boolean isConnected = deviceConnFactoryManager.getConnState();
            
            status.append("状态: ").append(isConnected ? "已连接" : "未连接").append("\n");
            
            // 获取连接方式
            String connMethod = "未知";
            if (deviceConnFactoryManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.USB) {
                connMethod = "USB";
            } else if (deviceConnFactoryManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH) {
                connMethod = "蓝牙";
            }
            status.append("方式: ").append(connMethod).append("\n");
            
            if (isConnected) {
                // 根据连接方式显示不同的标识信息
                if (deviceConnFactoryManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.USB) {
                    // 获取USB设备信息
                    SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                    String printerAddress = sp.getString("printer_address", null);
                    if (printerAddress != null) {
                        status.append("USB设备: ").append(printerAddress);
                    }
                } else if (deviceConnFactoryManager.getConnMethod() == DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH) {
                    // 显示蓝牙名称
                    String macAddress = deviceConnFactoryManager.getMacAddress();
                    if (macAddress != null) {
                        // 从缓存中获取蓝牙名称
                        SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                        String printerAddress = sp.getString("printer_address", null);
                        if (printerAddress != null) {
                            status.append("蓝牙名称: ").append(printerAddress);
                        } else {
                            status.append("蓝牙地址: ").append(macAddress);
                        }
                    }
                }
            }
            
            tvPrinterStatus.setText(status.toString());
            tvPrinterStatus.setTextColor(isConnected ? getResources().getColor(android.R.color.holo_green_dark) : 
                                                      getResources().getColor(android.R.color.holo_red_dark));
        } else {
            // 检查是否有保存的打印机信息
            SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            String printerType = sp.getString("printer_type", null);
            String printerAddress = sp.getString("printer_address", null);
            
            if (printerType != null && printerAddress != null) {
                status.append("状态: 未连接\n");
                status.append("方式: ").append("bluetooth".equals(printerType) ? "蓝牙" : "USB").append("\n");
                status.append(printerType.equals("bluetooth") ? "蓝牙名称: " : "USB设备: ").append(printerAddress);
            } else {
                status.append("状态: 未初始化");
            }
            
            tvPrinterStatus.setText(status.toString());
            tvPrinterStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    if (id == deviceId) {
                        switch (state) {
                            case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                                isPrinterConnected = false;
                                showToast("打印机已断开连接");
                                break;
                            case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                                isPrinterConnected = true;
                                showToast("打印机已连接");
                                break;
                        }
                        updatePrinterStatus();
                    }
                    break;
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN:
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                        showToast("打印机已断开连接");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // 保存USB打印机信息到缓存
            SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("printer_type", "usb");
            editor.putString("printer_address", "usb_printer");
            editor.apply();

            Toast.makeText(this, "USB打印机连接成功", Toast.LENGTH_SHORT).show();
            // 立即更新打印机状态
            updatePrinterStatus();
        } else if (requestCode == 1002 && resultCode == RESULT_OK) {
            String macAddress = data.getStringExtra(BluetoothDeviceActivity.EXTRA_DEVICE_ADDRESS);
            if (macAddress != null) {
                // 保存蓝牙打印机信息到缓存
                SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("printer_type", "bluetooth");
                editor.putString("printer_address", macAddress);
                editor.apply();

                // 尝试连接蓝牙打印机
                try {
                    // 关闭之前的连接
                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
                    }

                    // 建立新的蓝牙连接
                    new DeviceConnFactoryManager.Build()
                            .setId(0)
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                            .setMacAddress(macAddress)
                            .setContext(this)
                            .build();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();

                    Toast.makeText(this, "蓝牙打印机连接成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "蓝牙打印机连接失败", e);
                    Toast.makeText(this, "蓝牙打印机连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // 立即更新打印机状态
                updatePrinterStatus();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePrinterStatus();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 