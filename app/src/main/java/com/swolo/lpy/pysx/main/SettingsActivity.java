package com.swolo.lpy.pysx.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.swolo.lpy.pysx.ui.BaseActivity;
import com.swolo.lpy.pysx.main.gp.Constant;
import com.swolo.lpy.pysx.main.DeviceConnFactoryManager;
import java.util.HashMap;

/**
 * 设备设置页面 - 包含蓝牙打印机和蓝牙称的自动连接功能
 * 
 * 主要功能：
 * 1. 蓝牙打印机自动连接
 * 2. 蓝牙称自动连接
 * 3. 设备状态显示
 * 4. 手动连接设备
 * 
 * 使用说明：
 * 1. 页面启动时会自动尝试连接已配置的设备
 * 2. 支持手动连接和断开设备
 * 3. 实时显示设备连接状态
 * 
 * 参考此页面实现其他页面的自动连接功能
 */
public class SettingsActivity extends BaseActivity {

    // ==================== UI 组件 ====================
    private Button btnPrinterConnect;
    private Button btnScaleConnect;
    private Button btnLogout;
    private TextView tvPrinterStatus;
    private TextView tvScaleStatus;
    
    // ==================== 常量定义 ====================
    private static final String TAG = "SettingsActivity";
    private static final int CONN_STATE_DISCONN = 0x007;
    private static final int REQUEST_CODE_SCALE = 1003;
    
    // ==================== 设备状态 ====================
    private int id = 0;
    private boolean isPrinterConnected = false;
    private boolean isScaleConnected = false;
    
    // ==================== 蓝牙称相关 ====================
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCallback gattCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: SettingsActivity 启动");
        super.onCreate(savedInstanceState);
        // 不要再手动调用 initView/initData/setView/bindAction
    }

    @Override
    protected int getContentViewRes() {
        Log.d(TAG, "getContentViewRes: 返回activity_settings布局");
        return R.layout.layout_base;
    }

    @Override
    protected void initView() {
        Log.d(TAG, "initView: 开始初始化视图");
        try {
            View contentContainer = findViewById(R.id.content_container);
            if (contentContainer == null) {
                Log.e(TAG, "initView: 找不到内容容器 content_container");
                throw new RuntimeException("找不到内容容器");
            }
            Log.d(TAG, "initView: 找到内容容器");
            
            View settingsLayout = getLayoutInflater().inflate(R.layout.activity_settings, (ViewGroup) contentContainer, false);
            Log.d(TAG, "initView: 成功inflate activity_settings布局");
            ((ViewGroup) contentContainer).addView(settingsLayout);
            Log.d(TAG, "initView: 成功添加设置布局到容器");
            
            btnPrinterConnect = settingsLayout.findViewById(R.id.btn_printer_connect);
            Log.d(TAG, "initView: 查找btn_printer_connect按钮: " + (btnPrinterConnect != null ? "找到" : "未找到"));
            
            btnScaleConnect = settingsLayout.findViewById(R.id.btn_scale_connect);
            Log.d(TAG, "initView: 查找btn_scale_connect按钮: " + (btnScaleConnect != null ? "找到" : "未找到"));
            
            btnLogout = settingsLayout.findViewById(R.id.btn_logout);
            Log.d(TAG, "initView: 查找btn_logout按钮: " + (btnLogout != null ? "找到" : "未找到"));
            
            tvPrinterStatus = settingsLayout.findViewById(R.id.tv_printer_status);
            Log.d(TAG, "initView: 查找tv_printer_status: " + (tvPrinterStatus != null ? "找到" : "未找到"));
            
            tvScaleStatus = settingsLayout.findViewById(R.id.tv_scale_status);
            Log.d(TAG, "initView: 查找tv_scale_status: " + (tvScaleStatus != null ? "找到" : "未找到"));
            
            Log.d(TAG, "initView: 视图初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "initView: 初始化视图异常", e);
        }
    }

    @Override
    protected void initData() {
        Log.d(TAG, "initData: 初始化数据");
    }

    @Override
    protected void setView() {
        Log.d(TAG, "setView: 设置视图属性");
        android.widget.ImageButton btnSettings = findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setVisibility(android.view.View.GONE);
        }
        
        // 设置页面标题
        setTitle("打印机设置");
        Log.d(TAG, "setView: 设置标题为'打印机设置'");
    }

    @Override
    protected void bindAction() {
        Log.d(TAG, "bindAction: 开始绑定事件");
        try {
            Log.d(TAG, "bindAction: 查找返回按钮");
            View btnBack = findViewById(R.id.iv_avatar);
            Log.d(TAG, "bindAction: 返回按钮: " + (btnBack != null ? "找到" : "未找到"));
            
            if (btnBack != null) {
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 点击返回按钮");
                        finish();
                    }
                });
                Log.d(TAG, "bindAction: 返回按钮事件绑定成功");
            }
            
            Log.d(TAG, "bindAction: 检查btnPrinterConnect: " + (btnPrinterConnect != null ? "不为空" : "为空"));
            if (btnPrinterConnect != null) {
                btnPrinterConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 点击连接打印机按钮");
                        Log.d(TAG, "bindAction: btnPrinterConnect不为空: " + (btnPrinterConnect != null));
                        try {
                            showPrinterConnectDialog();
                            Log.d(TAG, "bindAction: showPrinterConnectDialog调用完成");
                        } catch (Exception e) {
                            Log.e(TAG, "bindAction: 显示打印机连接对话框异常", e);
                            Toast.makeText(SettingsActivity.this, "显示对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(TAG, "bindAction: 打印机按钮事件绑定成功");
            } else {
                Log.e(TAG, "bindAction: btnPrinterConnect为空，无法绑定事件");
            }
            
            Log.d(TAG, "bindAction: 检查btnScaleConnect: " + (btnScaleConnect != null ? "不为空" : "为空"));
            if (btnScaleConnect != null) {
                btnScaleConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 点击连接蓝牙称按钮");
                        // 使用 BluetoothDeviceActivity 选择蓝牙称设备
                        Intent intent = new Intent(SettingsActivity.this, BluetoothDeviceActivity.class);
                        intent.putExtra("device_type", "scale"); // 标识是连接称
                        startActivityForResult(intent, REQUEST_CODE_SCALE);
                    }
                });
                Log.d(TAG, "bindAction: 蓝牙称按钮事件绑定成功");
            } else {
                Log.e(TAG, "bindAction: btnScaleConnect为空，无法绑定事件");
            }
            
            Log.d(TAG, "bindAction: 检查btnLogout: " + (btnLogout != null ? "不为空" : "为空"));
            if (btnLogout != null) {
                btnLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 点击退出登录按钮");
                        SharedPreferences sp = getSharedPreferences("user_cache", MODE_PRIVATE);
                        sp.edit().clear().apply();
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
                Log.d(TAG, "bindAction: 退出登录按钮事件绑定成功");
            } else {
                Log.e(TAG, "bindAction: btnLogout为空，无法绑定事件");
            }
            
            Log.d(TAG, "bindAction: 事件绑定完成");
        } catch (Exception e) {
            Log.e(TAG, "bindAction: 绑定事件异常", e);
        }
    }

    private void showPrinterConnectDialog() {
        Log.d(TAG, "showPrinterConnectDialog: 开始显示打印机连接对话框");
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("选择打印机连接方式");
            
            String[] items = {"USB连接", "蓝牙连接"};
            Log.d(TAG, "showPrinterConnectDialog: 创建选项数组: " + items.length + "个选项");
            
            builder.setItems(items, (dialog, which) -> {
                Log.d(TAG, "showPrinterConnectDialog: 用户选择了第" + which + "个选项");
                if (which == 0) {
                    Log.d(TAG, "showPrinterConnectDialog: 选择USB连接");
                    showUsbDeviceDialog();
                } else {
                    Log.d(TAG, "showPrinterConnectDialog: 选择蓝牙连接");
                    Intent intent = new Intent(this, BluetoothDeviceActivity.class);
                    startActivityForResult(intent, 1002);
                }
            });
            
            builder.setNegativeButton("取消", (dialog, which) -> {
                Log.d(TAG, "showPrinterConnectDialog: 用户点击取消");
            });
            
            AlertDialog dialog = builder.create();
            Log.d(TAG, "showPrinterConnectDialog: 对话框创建成功");
            dialog.show();
            Log.d(TAG, "showPrinterConnectDialog: 对话框显示成功");
        } catch (Exception e) {
            Log.e(TAG, "showPrinterConnectDialog: 显示对话框异常", e);
            Toast.makeText(this, "显示对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showUsbDeviceDialog() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        
        if (deviceList.isEmpty()) {
            Toast.makeText(this, "未找到USB设备", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 过滤出支持的USB打印机设备
        java.util.List<UsbDevice> supportedDevices = new java.util.ArrayList<>();
        for (UsbDevice device : deviceList.values()) {
            if (isSupportedUsbPrinter(device)) {
                supportedDevices.add(device);
            }
        }
        
        if (supportedDevices.isEmpty()) {
            Toast.makeText(this, "未找到支持的USB打印机", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建设备选择对话框
        String[] deviceNames = new String[supportedDevices.size()];
        for (int i = 0; i < supportedDevices.size(); i++) {
            UsbDevice device = supportedDevices.get(i);
            deviceNames[i] = "USB打印机 " + (i + 1) + " (VID:" + device.getVendorId() + ", PID:" + device.getProductId() + ")";
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择USB打印机");
        builder.setItems(deviceNames, (dialog, which) -> {
            UsbDevice selectedDevice = supportedDevices.get(which);
            connectUsbPrinter(selectedDevice);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private boolean isSupportedUsbPrinter(UsbDevice device) {
        int vid = device.getVendorId();
        int pid = device.getProductId();
        
        // 支持的USB打印机VID/PID列表
        return (vid == 34918 && pid == 256) || (vid == 1137 && pid == 85)
                || (vid == 6790 && pid == 30084)
                || (vid == 26728 && pid == 256) || (vid == 26728 && pid == 512)
                || (vid == 26728 && pid == 768) || (vid == 26728 && pid == 1024)
                || (vid == 26728 && pid == 1280) || (vid == 26728 && pid == 1536);
    }

    private void connectUsbPrinter(UsbDevice usbDevice) {
        try {
            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
            }

            // 建立新的USB连接
            new DeviceConnFactoryManager.Build()
                    .setId(0)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                    .setUsbDevice(usbDevice)
                    .setContext(this)
                    .build();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();

            // 保存USB打印机信息到缓存
            SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("printer_type", "usb");
            editor.putString("printer_address", usbDevice.getDeviceName());
            editor.apply();

            Toast.makeText(this, "USB打印机连接成功", Toast.LENGTH_SHORT).show();
            updatePrinterStatus();
            
        } catch (Exception e) {
            Log.e(TAG, "USB打印机连接失败", e);
            Toast.makeText(this, "USB打印机连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePrinterStatus() {
        SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
        String type = sp.getString("printer_type", null);
        String address = sp.getString("printer_address", null);
        
        if (type == null || address == null) {
            tvPrinterStatus.setText("未配置打印机");
            tvPrinterStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // 使用SettingsActivity中的连接状态，而不是DeviceManager中的
        String status;
        if (isPrinterConnected) {
            status = String.format("打印机: %s (%s) - 已连接 ✓", type, address);
            tvPrinterStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            status = String.format("打印机: %s (%s) - 已配置", type, address);
            tvPrinterStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
        
        tvPrinterStatus.setText(status);
    }

    private void updateScaleStatus() {
        SharedPreferences sp = getSharedPreferences("scale_cache", MODE_PRIVATE);
        String address = sp.getString("scale_address", null);
        String name = sp.getString("scale_name", null);
        
        if (address == null) {
            tvScaleStatus.setText("未配置称");
            tvScaleStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // 使用SettingsActivity中的连接状态，而不是DeviceManager中的
        String status;
        if (isScaleConnected) {
            status = String.format("称: %s (%s) - 已连接 ✓", 
                name != null ? name : "未知", address);
            tvScaleStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            status = String.format("称: %s (%s) - 已配置", 
                name != null ? name : "未知", address);
            tvScaleStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
        
        tvScaleStatus.setText(status);
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
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            String macAddress = data.getStringExtra(BluetoothDeviceActivity.EXTRA_DEVICE_ADDRESS);
            if (macAddress != null) {
                // 保存蓝牙打印机信息到缓存
                SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("printer_type", "bluetooth");
                editor.putString("printer_address", macAddress);
                editor.apply();

                // 尝试连接蓝牙打印机（保持原有功能）
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
        } else if (requestCode == REQUEST_CODE_SCALE && resultCode == RESULT_OK) {
            // 蓝牙称连接成功
            double weight = data.getDoubleExtra("weight", 0.0);
            String macAddress = data.getStringExtra("device_address"); // 修正：使用 device_address 而不是 mac_address
            String deviceName = data.getStringExtra("device_name");
            
            Log.d(TAG, "收到蓝牙称连接结果: weight=" + weight + ", macAddress=" + macAddress + ", deviceName=" + deviceName);
            
            if (macAddress != null) {
                // 保存蓝牙称信息到缓存
                SharedPreferences sp = getSharedPreferences("scale_cache", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("scale_address", macAddress);
                editor.putString("scale_name", deviceName != null ? deviceName : "蓝牙称");
                editor.apply();
                
                Log.d(TAG, "蓝牙称信息已保存到scale_cache: address=" + macAddress + ", name=" + deviceName);

                Toast.makeText(this, "蓝牙称连接成功", Toast.LENGTH_SHORT).show();
                updateScaleStatus();
            } else {
                Log.e(TAG, "蓝牙称连接成功但地址为空");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 启用打印机自动连接
        new Thread(() -> autoConnectPrinter()).start();
        
        // 延迟更新状态，给连接一些时间
        new Handler().postDelayed(() -> {
            Log.d(TAG, "延迟更新设备状态");
            updatePrinterStatus();
            updateScaleStatus();
        }, 3000); // 延迟3秒检查状态，给更多时间让连接完成
    }
    
    /**
     * ==================== 自动连接核心方法 ====================
     * 
     * 自动连接已配置的设备
     * 
     * 使用说明：
     * 1. 在页面启动时调用此方法
     * 2. 会自动检查并连接已配置的打印机
     * 3. 包含完整的权限检查和错误处理
     * 
     * 参考此方法实现其他页面的自动连接功能
     */
    private void autoConnectDevices() {
        Log.d(TAG, "开始自动连接已配置的设备");
        
        // 检查并清除旧的配置格式
        checkAndClearOldConfig();
        
        // 自动连接打印机
        SharedPreferences printerSp = getSharedPreferences("printer_cache", MODE_PRIVATE);
        String printerType = printerSp.getString("printer_type", null);
        String printerAddress = printerSp.getString("printer_address", null);
        
        if (printerType != null && printerAddress != null) {
            Log.d(TAG, "发现打印机配置，尝试自动连接");
            autoConnectPrinter();
        }
        
        // 删除蓝牙称的自动连接
        // 自动连接称功能已移除
    }
    
    /**
     * 检查并清除旧的配置格式
     */
    private void checkAndClearOldConfig() {
        try {
            SharedPreferences printerSp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            String printerType = printerSp.getString("printer_type", null);
            if ("蓝牙打印机".equals(printerType)) {
                Log.d(TAG, "发现旧的配置格式，清除配置");
                printerSp.edit().clear().apply();
                Log.d(TAG, "旧配置已清除");
            }
        } catch (Exception e) {
            Log.e(TAG, "检查旧配置时出错", e);
        }
    }
    
    /**
     * 自动连接打印机
     */
    private void autoConnectPrinter() {
        try {
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "缺少蓝牙连接权限，跳过自动连接");
                    return;
                }
            }

            SharedPreferences sp = getSharedPreferences("printer_cache", MODE_PRIVATE);
            String printerAddress = sp.getString("printer_address", null);
            String printerType = sp.getString("printer_type", null);

            Log.d(TAG, "获取到的打印机配置 - 类型: '" + printerType + "', 地址: '" + printerAddress + "'");

            if (printerAddress == null || printerAddress.isEmpty()) {
                Log.w(TAG, "打印机地址为空，跳过自动连接");
                return;
            }

            Log.d(TAG, "自动连接打印机: " + printerType + " - " + printerAddress);

            // 根据类型建立连接
            if ("bluetooth".equals(printerType)) {
                // 蓝牙打印机 - 参考StockOutActivity的成功实现
                Log.d(TAG, "开始调用autoConnectBluetoothPrinter方法");
                autoConnectBluetoothPrinter(printerAddress);
            } else if ("usb".equals(printerType)) {
                // USB打印机 - 需要用户选择设备
                Log.d(TAG, "USB打印机需要用户手动选择，跳过自动连接");
                return;
            }

        } catch (Exception e) {
            Log.e(TAG, "打印机自动连接失败", e);
        }
    }

    /**
     * 自动连接蓝牙打印机 - 参考StockOutActivity的成功实现
     */
    private void autoConnectBluetoothPrinter(String address) {
        Log.d(TAG, "开始连接蓝牙打印机: " + address);
        try {
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限");
                    return;
                }
            }

            // 检查蓝牙是否开启
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Log.e(TAG, "设备不支持蓝牙");
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "蓝牙未开启");
                return;
            }

            // 获取蓝牙设备
            BluetoothDevice device;
            try {
                // 权限检查
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法获取蓝牙设备");
                        return;
                    }
                }
                device = bluetoothAdapter.getRemoteDevice(address);
                Log.d(TAG, "获取到蓝牙设备: " + device.getName());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "无效的蓝牙地址: " + address);
                return;
            }

            if (device == null) {
                Log.e(TAG, "未找到蓝牙打印机设备");
                return;
            }

            // 检查设备是否已配对
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "设备未配对，尝试配对");
                try {
                    // 检查API版本，createBond需要API 19+
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        device.createBond();
                        // 等待配对完成
                        Thread.sleep(2000);
                    } else {
                        Log.w(TAG, "当前API版本不支持createBond，跳过配对");
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "设备配对失败", e);
                    return;
                }
            }

            // 关闭之前的连接
            if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
            }

            // 建立新的蓝牙连接
            Log.d(TAG, "开始建立蓝牙连接");
            DeviceConnFactoryManager manager = new DeviceConnFactoryManager.Build()
                    .setId(0)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                    .setMacAddress(address)
                    .setContext(this)
                    .build();

            // 检查build是否成功
            if (manager == null) {
                Log.e(TAG, "DeviceConnFactoryManager build 失败，无法连接打印机");
                return;
            }

            // 打开端口
            manager.openPort();
            
            // 检查连接状态
            boolean connected = manager.getConnState();
            Log.d(TAG, "蓝牙连接结果: " + (connected ? "成功" : "失败"));

            if (connected) {
                Log.d(TAG, "蓝牙打印机连接成功");
                isPrinterConnected = true;
                
                // 立即更新UI状态
                runOnUiThread(() -> {
                    updatePrinterStatus();
                });
            } else {
                Log.e(TAG, "蓝牙打印机连接失败");
                isPrinterConnected = false;
                
                // 立即更新UI状态
                runOnUiThread(() -> {
                    updatePrinterStatus();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "连接蓝牙打印机失败", e);
            isPrinterConnected = false;
        }
    }
    
    /**
     * 自动连接称 - 已删除
     * 根据用户要求，删除了蓝牙称的自动连接功能
     */
    
    /**
     * 连接蓝牙称 - 参考CustomerStockOutActivity的成功实现
     */
    private void connectToScale(String address) {
        Log.d(TAG, "开始连接蓝牙称: " + address);
        try {
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限，无法连接称");
                    return;
                }
            }
            
            if (bluetoothAdapter == null) {
                Log.e(TAG, "蓝牙适配器为空，无法连接称");
                return;
            }
            
            // 获取蓝牙设备
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            if (device != null) {
                Log.d(TAG, "获取到称设备: " + device.getName());
                
                // 检查设备是否已配对
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "称设备未配对，尝试配对");
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            device.createBond();
                            // 等待配对完成
                            Thread.sleep(2000);
                        } else {
                            Log.w(TAG, "当前API版本不支持createBond，跳过配对");
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "称设备配对失败", e);
                        return;
                    }
                }
                
                // 初始化GATT回调
                gattCallback = new GattCallback();
                
                // 连接设备
                // 权限检查
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法连接GATT");
                        return;
                    }
                }
                bluetoothGatt = device.connectGatt(this, false, gattCallback);
                if (bluetoothGatt != null) {
                    Log.d(TAG, "称GATT连接创建成功");
                } else {
                    Log.e(TAG, "称GATT连接创建失败");
                }
            } else {
                Log.e(TAG, "无法获取称远程设备");
            }
        } catch (Exception e) {
            Log.e(TAG, "连接称异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 关闭GATT连接
     */
    private void closeGatt() {
        Log.d(TAG, "开始关闭称GATT连接");
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
            Log.d(TAG, "称GATT连接已关闭");
        }
        writeCharacteristic = null;
        notifyCharacteristic = null;
        gattCallback = null;
        isScaleConnected = false;
    }
    
    /**
     * GATT回调类
     */
    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "称连接状态变化: status=" + status + ", newState=" + newState);
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "称连接成功，开始发现服务");
                    isScaleConnected = true;
                    // 权限检查
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法发现服务");
                            return;
                        }
                    }
                    gatt.discoverServices();
                    
                    // 更新UI状态
                    runOnUiThread(() -> {
                        updateScaleStatus();
                    });
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d(TAG, "称连接断开");
                    isScaleConnected = false;
                    
                    // 更新UI状态
                    runOnUiThread(() -> {
                        updateScaleStatus();
                    });
                }
            } else {
                Log.e(TAG, "称连接失败: " + status);
                isScaleConnected = false;
                
                // 更新UI状态
                runOnUiThread(() -> {
                    updateScaleStatus();
                });
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "称服务发现完成: status=" + status);
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "称服务发现成功，开始查找特征");
                
                // 调试：打印所有可用的服务
                Log.d(TAG, "=== 蓝牙称提供的所有服务 ===");
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d(TAG, "服务UUID: " + service.getUuid());
                    
                    // 打印每个服务的特征
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.d(TAG, "  特征UUID: " + characteristic.getUuid() + 
                              ", 属性: " + characteristic.getProperties());
                    }
                }
                Log.d(TAG, "=== 服务列表结束 ===");
                
                // 查找服务和特征
                BluetoothGattService service = gatt.getService(java.util.UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                if (service != null) {
                    Log.d(TAG, "找到称服务");
                    
                    // 查找写特征
                    writeCharacteristic = service.getCharacteristic(java.util.UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                    if (writeCharacteristic != null) {
                        Log.d(TAG, "找到称写特征");
                    }
                    
                    // 查找通知特征
                    notifyCharacteristic = service.getCharacteristic(java.util.UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                    if (notifyCharacteristic != null) {
                        Log.d(TAG, "找到称通知特征，启用通知");
                        // 权限检查
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                                    != PackageManager.PERMISSION_GRANTED) {
                                Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法设置通知");
                                return;
                            }
                        }
                        // 启用通知
                        gatt.setCharacteristicNotification(notifyCharacteristic, true);
                        
                        // 写入描述符
                        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(
                            java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            // 权限检查
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法写入描述符");
                                    return;
                                }
                            }
                            gatt.writeDescriptor(descriptor);
                            Log.d(TAG, "称通知描述符写入成功");
                        }
                    }
                } else {
                    Log.e(TAG, "未找到称服务");
                }
            } else {
                Log.e(TAG, "称服务发现失败: " + status);
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // 设置页面不需要处理称的实时数据，只记录连接状态
            // Log.d(TAG, "称数据变化: " + characteristic.getUuid());
            // 这里可以处理称的数据，但设置页面不需要显示重量
        }
        
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "称描述符写入: status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "称描述符写入成功，连接完成");
            } else {
                Log.e(TAG, "称描述符写入失败: " + status);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 关闭称连接
        closeGatt();
    }
} 