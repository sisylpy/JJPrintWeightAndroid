package com.swolo.lpy.pysx.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.Manifest;
import android.app.PendingIntent;
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
import android.app.PendingIntent;

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
    private Button btnPrinterDelete;  // 新增：删除打印机按钮
    private Button btnScaleDelete;    // 新增：删除蓝牙称按钮
    private TextView tvPrinterStatus;
    private TextView tvScaleStatus;
    private TextView tvPaperSizeLabel;
    private TextView tvScreenOrientationLabel; // 【新增】屏幕方向设置标签
    
    // ==================== 常量定义 ====================
    private static final String TAG = "SettingsActivity";
    private static final int CONN_STATE_DISCONN = 0x007;
    private static final int REQUEST_CODE_SCALE = 1003;
    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_PAPER_SIZE = "paper_size";
    private static final String KEY_SCREEN_ORIENTATION = "screen_orientation";
    private static final String[] PAPER_SIZE_OPTIONS = {
        "4×3cm（横）",
        "6×4cm（横）",
        "4×6cm（竖）",
        "5×8cm（竖）"
    };
    // 【新增】屏幕方向选项
    private static final String[] SCREEN_ORIENTATION_OPTIONS = {
        "竖屏",
        "横屏"
    };
    // 纸张尺寸映射（单位：厘米），与CustomerStockOutActivity保持一致
    private static final int[][] PAPER_SIZE_CM = {
        {4, 3}, // 4×3cm 横
        {6, 4}, // 6×4cm 横
        {4, 6}, // 4×6cm 竖
        {5, 8}  // 5×8cm 竖
    };
    
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
        
        // 检查并请求必要权限
        checkAndRequestPermissions();
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
            
            btnPrinterDelete = settingsLayout.findViewById(R.id.btn_printer_delete); // 新增：查找删除打印机按钮
            Log.d(TAG, "initView: 查找btn_printer_delete按钮: " + (btnPrinterDelete != null ? "找到" : "未找到"));
            
            btnScaleDelete = settingsLayout.findViewById(R.id.btn_scale_delete); // 新增：查找删除蓝牙称按钮
            Log.d(TAG, "initView: 查找btn_scale_delete按钮: " + (btnScaleDelete != null ? "找到" : "未找到"));
            
            tvPrinterStatus = settingsLayout.findViewById(R.id.tv_printer_status);
            Log.d(TAG, "initView: 查找tv_printer_status: " + (tvPrinterStatus != null ? "找到" : "未找到"));
            
            tvScaleStatus = settingsLayout.findViewById(R.id.tv_scale_status);
            Log.d(TAG, "initView: 查找tv_scale_status: " + (tvScaleStatus != null ? "找到" : "未找到"));
            
            tvPaperSizeLabel = settingsLayout.findViewById(R.id.tv_paper_size_label);
            Log.d(TAG, "initView: 查找tv_paper_size_label: " + (tvPaperSizeLabel != null ? "找到" : "未找到"));
            
            tvScreenOrientationLabel = settingsLayout.findViewById(R.id.tv_screen_orientation_label);
            Log.d(TAG, "initView: 查找tv_screen_orientation_label: " + (tvScreenOrientationLabel != null ? "找到" : "未找到"));
            
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
        // 打印纸尺寸回显
        updatePaperSizeLabel();
        // 【新增】屏幕方向设置回显
        updateScreenOrientationLabel();
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
                            showPrinterConnectDialog();
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

            if (tvPaperSizeLabel != null) {
                tvPaperSizeLabel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPaperSizeDialog();
                    }
                });
            }
            
            // 【新增】屏幕方向设置点击事件
            if (tvScreenOrientationLabel != null) {
                tvScreenOrientationLabel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 用户点击了屏幕方向设置 - 开始");
                        
                        // 直接在这里创建对话框
                        Log.d(TAG, "bindAction: 准备创建对话框");
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                            builder.setTitle("选择屏幕方向");
                            builder.setItems(SCREEN_ORIENTATION_OPTIONS, (dialog, which) -> {
                                Log.d(TAG, "bindAction: 用户选择了: " + SCREEN_ORIENTATION_OPTIONS[which]);
                                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                prefs.edit().putInt(KEY_SCREEN_ORIENTATION, which).apply();
                                Toast.makeText(SettingsActivity.this, "已选择: " + SCREEN_ORIENTATION_OPTIONS[which], Toast.LENGTH_SHORT).show();
                                updateScreenOrientationLabel();
                            });
                            builder.setNegativeButton("取消", null);
                            builder.show();
                            Log.d(TAG, "bindAction: 对话框显示完成");
                        } catch (Exception e) {
                            Log.e(TAG, "bindAction: 创建对话框异常", e);
                            Toast.makeText(SettingsActivity.this, "显示对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        
                        Log.d(TAG, "bindAction: 用户点击了屏幕方向设置 - 结束");
                    }
                });
                Log.d(TAG, "bindAction: 屏幕方向设置点击事件绑定成功");
            } else {
                Log.e(TAG, "bindAction: tvScreenOrientationLabel为空，无法绑定点击事件");
            }
            
            // 新增：删除打印机按钮事件绑定
            Log.d(TAG, "bindAction: 检查btnPrinterDelete: " + (btnPrinterDelete != null ? "不为空" : "为空"));
            if (btnPrinterDelete != null) {
                btnPrinterDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 点击删除打印机按钮");
                        showDeletePrinterDialog();
                    }
                });
                Log.d(TAG, "bindAction: 删除打印机按钮事件绑定成功");
            } else {
                Log.e(TAG, "bindAction: btnPrinterDelete为空，无法绑定事件");
            }
            
            // 新增：删除蓝牙称按钮事件绑定
            Log.d(TAG, "bindAction: 检查btnScaleDelete: " + (btnScaleDelete != null ? "不为空" : "为空"));
            if (btnScaleDelete != null) {
                btnScaleDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "bindAction: 点击删除蓝牙称按钮");
                        showDeleteScaleDialog();
                    }
                });
                Log.d(TAG, "bindAction: 删除蓝牙称按钮事件绑定成功");
            } else {
                Log.e(TAG, "bindAction: btnScaleDelete为空，无法绑定事件");
            }
            

            
            Log.d(TAG, "bindAction: 事件绑定完成");
        } catch (Exception e) {
            Log.e(TAG, "bindAction: 绑定事件异常", e);
        }
    }

        @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 页面恢复");
                        updatePrinterStatus();
        updateScaleStatus();
                    }

        @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 页面销毁");
        // 断开蓝牙称连接
        if (bluetoothGatt != null) {
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                }
            } else {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            }
            bluetoothGatt = null;
        }
    }

    /**
     * 显示打印机连接对话框
     */
    private void showPrinterConnectDialog() {
        Log.d(TAG, "showPrinterConnectDialog: 显示打印机连接对话框");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择打印机连接方式");
        
        String[] options = {"USB打印机", "蓝牙打印机"};
        builder.setItems(options, (dialog, which) -> {
            Log.d(TAG, "showPrinterConnectDialog: 用户选择了第" + which + "个选项");
            if (which == 0) {
                // USB打印机
                connectUsbPrinter();
            } else if (which == 1) {
                // 蓝牙打印机
                connectBluetoothPrinter();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 连接USB打印机
     */
    private void connectUsbPrinter() {
        Log.d(TAG, "connectUsbPrinter: 开始连接USB打印机");
        
        // 检查USB权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.USB_PERMISSION") != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "connectUsbPrinter: 缺少USB权限");
                Toast.makeText(this, "缺少USB权限", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "connectUsbPrinter: 缺少蓝牙连接权限");
                Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        // 获取USB管理器
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Log.e(TAG, "connectUsbPrinter: 无法获取USB管理器");
            Toast.makeText(this, "无法获取USB管理器", Toast.LENGTH_SHORT).show();
                return;
            }

        // 查找USB打印机设备
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        UsbDevice printerDevice = null;
        
        for (UsbDevice device : deviceList.values()) {
            Log.d(TAG, "connectUsbPrinter: 发现USB设备: " + device.getDeviceName() + ", VID=" + device.getVendorId() + ", PID=" + device.getProductId());
            // 这里可以根据具体的打印机VID/PID进行过滤
            if (device.getVendorId() == 0x0483 || device.getVendorId() == 0x0484) { // 示例VID
                printerDevice = device;
                break;
            }
        }
        
        if (printerDevice == null) {
            Log.e(TAG, "connectUsbPrinter: 未找到USB打印机设备");
            Toast.makeText(this, "未找到USB打印机设备", Toast.LENGTH_SHORT).show();
                    return;
        }
        
        Log.d(TAG, "connectUsbPrinter: 找到USB打印机设备: " + printerDevice.getDeviceName());
        
        // 检查USB权限
        if (!usbManager.hasPermission(printerDevice)) {
            Log.d(TAG, "connectUsbPrinter: 请求USB权限");
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent("USB_PERMISSION"), 0);
            usbManager.requestPermission(printerDevice, permissionIntent);
                return;
            }

        // 连接USB打印机
        connectToUsbPrinter(printerDevice);
    }

    /**
     * 连接USB打印机设备
     */
    private void connectToUsbPrinter(UsbDevice device) {
        Log.d(TAG, "connectToUsbPrinter: 开始连接USB打印机: " + device.getDeviceName());
        
        try {
            // 使用DeviceConnFactoryManager连接USB打印机
            new DeviceConnFactoryManager.Build()
                    .setId(0)
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                .setUsbDevice(device)
                    .setContext(this)
                    .build();

            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();
            
            // 检查连接状态
            boolean isConnected = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState();
            Log.d(TAG, "connectToUsbPrinter: USB打印机连接状态: " + (isConnected ? "成功" : "失败"));
            
                    if (isConnected) {
            // 保存打印机配置到 printer_cache
            SharedPreferences printerPrefs = getSharedPreferences("printer_cache", MODE_PRIVATE);
            printerPrefs.edit()
                .putString("printer_type", "usb")
                .putString("printer_name", device.getDeviceName())
                .putInt("printer_vid", device.getVendorId())
                .putInt("printer_pid", device.getProductId())
                .apply();
            
            Log.d(TAG, "connectToUsbPrinter: USB打印机连接成功");
            Toast.makeText(this, "USB打印机连接成功", Toast.LENGTH_SHORT).show();
            } else {
            Log.e(TAG, "connectToUsbPrinter: USB打印机连接失败");
            Toast.makeText(this, "USB打印机连接失败", Toast.LENGTH_SHORT).show();
        }
                
                    updatePrinterStatus();
            
        } catch (Exception e) {
            Log.e(TAG, "connectToUsbPrinter: USB打印机连接失败", e);
            Toast.makeText(this, "USB打印机连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 连接蓝牙打印机
     */
    private void connectBluetoothPrinter() {
        Log.d(TAG, "connectBluetoothPrinter: 开始连接蓝牙打印机");
        
            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "connectBluetoothPrinter: 缺少蓝牙连接权限");
                Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
        // 检查蓝牙扫描权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "connectBluetoothPrinter: 缺少蓝牙扫描权限");
                Toast.makeText(this, "缺少蓝牙扫描权限", Toast.LENGTH_SHORT).show();
                        return;
            }
        }
        
        // 启动蓝牙设备选择页面
        Intent intent = new Intent(this, BluetoothDeviceActivity.class);
        intent.putExtra("device_type", "printer"); // 标识是连接打印机
        startActivityForResult(intent, 1002);
    }

    /**
     * 显示删除打印机确认对话框
     */
    private void showDeletePrinterDialog() {
        Log.d(TAG, "showDeletePrinterDialog: 显示删除打印机确认对话框");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除打印机配置");
        builder.setMessage("确定要删除已保存的打印机配置吗？");
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            Log.d(TAG, "showDeletePrinterDialog: 用户确认删除打印机配置");
            deletePrinterConfig();
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 删除打印机配置
     */
    private void deletePrinterConfig() {
        Log.d(TAG, "deletePrinterConfig: 开始删除打印机配置");
        
        SharedPreferences printerPrefs = getSharedPreferences("printer_cache", MODE_PRIVATE);
        printerPrefs.edit()
            .remove("printer_type")
            .remove("printer_name")
            .remove("printer_address")
            .remove("printer_vid")
            .remove("printer_pid")
            .apply();
        
        Log.d(TAG, "deletePrinterConfig: 打印机配置删除成功");
            Toast.makeText(this, "打印机配置已删除", Toast.LENGTH_SHORT).show();
            
        updatePrinterStatus();
    }
    
    /**
     * 显示删除蓝牙称确认对话框
     */
    private void showDeleteScaleDialog() {
        Log.d(TAG, "showDeleteScaleDialog: 显示删除蓝牙称确认对话框");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除蓝牙称配置");
        builder.setMessage("确定要删除已保存的蓝牙称配置吗？");
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            Log.d(TAG, "showDeleteScaleDialog: 用户确认删除蓝牙称配置");
            deleteScaleConfig();
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 删除蓝牙称配置
     */
    private void deleteScaleConfig() {
        Log.d(TAG, "deleteScaleConfig: 开始删除蓝牙称配置");
        
        SharedPreferences scalePrefs = getSharedPreferences("scale_cache", MODE_PRIVATE);
        scalePrefs.edit()
            .remove("scale_address")
            .remove("scale_name")
            .apply();
        
        Log.d(TAG, "deleteScaleConfig: 蓝牙称配置删除成功");
        Toast.makeText(this, "蓝牙称配置已删除", Toast.LENGTH_SHORT).show();
        
            updateScaleStatus();
    }

    /**
     * 更新打印机状态显示
     */
    private void updatePrinterStatus() {
        Log.d(TAG, "updatePrinterStatus: 更新打印机状态显示");
        
        // 从 printer_cache 读取打印机配置
        SharedPreferences printerPrefs = getSharedPreferences("printer_cache", MODE_PRIVATE);
        String printerType = printerPrefs.getString("printer_type", "");
        String printerName = printerPrefs.getString("printer_name", "");
        String printerAddress = printerPrefs.getString("printer_address", "");
        
        if (tvPrinterStatus != null) {
            if (!printerType.isEmpty() && (!printerName.isEmpty() || !printerAddress.isEmpty())) {
                String displayName = !printerName.isEmpty() ? printerName : printerAddress;
                String status = "已连接: " + displayName + " (" + printerType + ")";
                tvPrinterStatus.setText(status);
                Log.d(TAG, "updatePrinterStatus: 打印机状态: " + status);
            } else {
                tvPrinterStatus.setText("状态: 未连接");
                Log.d(TAG, "updatePrinterStatus: 打印机状态: 未连接");
            }
        }
    }

    /**
     * 更新蓝牙称状态显示
     */
    private void updateScaleStatus() {
        Log.d(TAG, "updateScaleStatus: 更新蓝牙称状态显示");
        
        // 从 scale_cache 读取蓝牙称配置
        SharedPreferences scalePrefs = getSharedPreferences("scale_cache", MODE_PRIVATE);
        String scaleAddress = scalePrefs.getString("scale_address", "");
        String scaleName = scalePrefs.getString("scale_name", "");
        
        if (tvScaleStatus != null) {
            if (!scaleAddress.isEmpty() && !scaleName.isEmpty()) {
                String status = "已配置: " + scaleName + " (" + scaleAddress + ")";
                tvScaleStatus.setText(status);
                Log.d(TAG, "updateScaleStatus: 蓝牙称状态: " + status);
            } else {
                tvScaleStatus.setText("状态: 未配置");
                Log.d(TAG, "updateScaleStatus: 蓝牙称状态: 未配置");
            }
        }
    }

    /**
     * 显示打印纸尺寸设置对话框
     */
    private void showPaperSizeDialog() {
        Log.d(TAG, "showPaperSizeDialog: 开始显示打印纸尺寸设置对话框");
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int checkedItem = prefs.getInt(KEY_PAPER_SIZE, 0);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择打印纸尺寸");
        builder.setSingleChoiceItems(PAPER_SIZE_OPTIONS, checkedItem, (dialog, which) -> {
            Log.d(TAG, "showPaperSizeDialog: 用户选择了第" + which + "个选项");
                prefs.edit().putInt(KEY_PAPER_SIZE, which).apply();
                updatePaperSizeLabel();
                dialog.dismiss();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void updatePaperSizeLabel() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int checkedItem = prefs.getInt(KEY_PAPER_SIZE, 0);
        if (tvPaperSizeLabel != null) {
            tvPaperSizeLabel.setText("打印纸尺寸：" + PAPER_SIZE_OPTIONS[checkedItem]);
        }
    }
    
        // 【新增】显示屏幕方向设置对话框
    private void showScreenOrientationDialog() {
        Log.d(TAG, "showScreenOrientationDialog: 方法开始");
        
        try {
            Log.d(TAG, "showScreenOrientationDialog: 开始显示屏幕方向设置对话框");
            
            // 最简单的测试
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("测试");
            builder.setMessage("这是一个测试对话框");
            builder.setPositiveButton("确定", null);
            builder.show();
            
            Log.d(TAG, "showScreenOrientationDialog: 测试对话框显示完成");
            
        } catch (Exception e) {
            Log.e(TAG, "showScreenOrientationDialog: 显示对话框异常", e);
            Toast.makeText(this, "显示对话框失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        
        Log.d(TAG, "showScreenOrientationDialog: 方法结束");
    }
    
    // 【新增】更新屏幕方向设置标签
    private void updateScreenOrientationLabel() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int checkedItem = prefs.getInt(KEY_SCREEN_ORIENTATION, 0);
        Log.d(TAG, "updateScreenOrientationLabel: 读取到设置值: " + checkedItem);
        
        // 新的索引映射：0=竖屏，1=横屏
        if (checkedItem >= SCREEN_ORIENTATION_OPTIONS.length) {
            checkedItem = 0; // 如果索引超出范围，默认使用竖屏
            Log.w(TAG, "updateScreenOrientationLabel: 设置值超出范围，重置为竖屏");
        }
        
        if (tvScreenOrientationLabel != null) {
            String displayText = "人工输入弹窗屏幕方向：" + SCREEN_ORIENTATION_OPTIONS[checkedItem];
            tvScreenOrientationLabel.setText(displayText);
            Log.d(TAG, "updateScreenOrientationLabel: 设置显示文本: " + displayText);
        } else {
            Log.e(TAG, "updateScreenOrientationLabel: tvScreenOrientationLabel为空");
        }
    }

    /**
     * 检查并请求必要权限
     */
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            };
            
            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                ActivityCompat.requestPermissions(this, permissions, 1001);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 1001) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Log.d(TAG, "onRequestPermissionsResult: 所有权限已授予");
            } else {
                Log.w(TAG, "onRequestPermissionsResult: 部分权限被拒绝");
                Toast.makeText(this, "需要蓝牙权限才能正常使用设备连接功能", Toast.LENGTH_LONG).show();
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
} 