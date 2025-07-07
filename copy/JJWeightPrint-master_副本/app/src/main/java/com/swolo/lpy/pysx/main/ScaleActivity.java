package com.swolo.lpy.pysx.main;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.adapter.BluetoothDeviceAdapter;
import com.swolo.lpy.pysx.main.bean.BluetoothParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ScaleActivity extends AppCompatActivity {
    private static final String TAG = "ScaleActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final long SCAN_PERIOD = 20000; // 20秒扫描时间

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<BluetoothParameter> pairedDevices = new ArrayList<>();
    private List<BluetoothParameter> newDevices = new ArrayList<>();
    private BluetoothDeviceAdapter deviceAdapter;
    private RecyclerView recyclerView;
    private Button btnScan;
    private TextView tvStatus;
    private TextView tvWeight;
    private Button btnZero;
    private Button btnGetWeight;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private double tareWeight = 0;
    private boolean autoTareDone = false;

    // 添加广播接收器用于传统蓝牙扫描
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    BluetoothParameter parameter = new BluetoothParameter();
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                                    != PackageManager.PERMISSION_GRANTED) {
                                Log.e(TAG, "缺少蓝牙连接权限");
                                Toast.makeText(context, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                                ActivityCompat.requestPermissions(ScaleActivity.this, 
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                                    REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                                return;
                            }
                        }
                        parameter.setBluetoothName(device.getName() != null ? device.getName() : "未知设备");
                        parameter.setBluetoothMac(device.getAddress());
                    } catch (SecurityException e) {
                        Log.e(TAG, "获取设备信息失败", e);
                        Toast.makeText(context, "获取设备信息失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    parameter.setBluetoothDevice(device);
                    newDevices.add(parameter);
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // 将BluetoothGattCallback移到内部类，并添加API级别检查
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class GattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange - status: " + status + ", newState: " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "设备已连接，开始发现服务");
                runOnUiThread(() -> {
                    tvStatus.setText("已连接");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(ScaleActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法discoverServices");
                            ActivityCompat.requestPermissions(ScaleActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                            return;
                        }
                    }
                    boolean success = gatt.discoverServices();
                    Log.d(TAG, "开始发现服务: " + (success ? "成功" : "失败"));
                });
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "设备已断开连接");
                runOnUiThread(() -> {
                    tvStatus.setText("已断开");
                    closeGatt();
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered - status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "服务发现成功，开始查找特征");
                boolean foundWrite = false;
                boolean foundNotify = false;
                
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d(TAG, "发现服务: " + service.getUuid());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        int properties = characteristic.getProperties();
                        Log.d(TAG, "发现特征: " + characteristic.getUuid() + 
                            ", 属性: " + properties);
                        
                        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                            writeCharacteristic = characteristic;
                            foundWrite = true;
                            Log.d(TAG, "找到写入特征: " + characteristic.getUuid());
                        }
                        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            notifyCharacteristic = characteristic;
                            foundNotify = true;
                            Log.d(TAG, "找到通知特征: " + characteristic.getUuid());
                            
                            // 设置通知
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (ActivityCompat.checkSelfPermission(ScaleActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    Log.e(TAG, "缺少BLUETOOTH_CONNECT权限，无法设置通知");
                                    ActivityCompat.requestPermissions(ScaleActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                                    return;
                                }
                            }
                            boolean success = gatt.setCharacteristicNotification(characteristic, true);
                            Log.d(TAG, "设置通知: " + (success ? "成功" : "失败"));
                            
                            // 设置通知描述符
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                success = gatt.writeDescriptor(descriptor);
                                Log.d(TAG, "设置通知描述符: " + (success ? "成功" : "失败"));
                            } else {
                                Log.e(TAG, "未找到通知描述符");
                            }
                        }
                    }
                }
                
                if (!foundWrite || !foundNotify) {
                    Log.e(TAG, "未找到必要的特征 - 写入: " + foundWrite + ", 通知: " + foundNotify);
                    runOnUiThread(() -> {
                        Toast.makeText(ScaleActivity.this, 
                            "未找到必要的特征，请检查设备", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.d(TAG, "所有必要的特征都已找到并设置");
                    runOnUiThread(() -> {
                        Toast.makeText(ScaleActivity.this, 
                            "设备已准备就绪", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Log.e(TAG, "服务发现失败: " + status);
                runOnUiThread(() -> {
                    Toast.makeText(ScaleActivity.this, 
                        "服务发现失败: " + status, Toast.LENGTH_SHORT).show();
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged - 特征: " + characteristic.getUuid());
            if (characteristic == notifyCharacteristic) {
                byte[] data = characteristic.getValue();
                StringBuilder hexData = new StringBuilder();
                for (byte b : data) {
                    hexData.append(String.format("%02X ", b));
                }
                Log.d(TAG, "收到数据: " + hexData.toString());
                
                if (data.length == 7 && data[0] == 0x05) {
                    final int status = data[1];
                    Log.d(TAG, "状态字节: 0x" + String.format("%02X", status));
                    
                    // 修改重量解析方式，使用位运算确保正确处理负数
                    final int rawWeight = ((data[3] & 0xFF) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
                    Log.d(TAG, "原始重量: " + rawWeight + "g");
                    
                    // 只在第一次收到数据且重量为0时进行自动去皮
                    if (!autoTareDone && rawWeight == 0) {
                        tareWeight = rawWeight;
                        autoTareDone = true;
                        Log.d(TAG, "自动去皮完成，皮重: " + tareWeight + "g");
                    }

                    final double realWeight = Math.round((rawWeight - tareWeight) * 10) / 10.0;
                    Log.d(TAG, "实际重量: " + realWeight + "g");

                    final StringBuilder statusTextBuilder = new StringBuilder();
                    if ((status & 0x20) != 0) statusTextBuilder.append("超载 ");
                    if ((status & 0x40) != 0) statusTextBuilder.append("稳定 ");
                    else statusTextBuilder.append("不稳定 ");
                    if ((status & 0x10) != 0) statusTextBuilder.append("负重 ");
                    else statusTextBuilder.append("正重 ");

                    final String finalStatusText = statusTextBuilder.toString().trim();
                    final boolean isStable = (status & 0x40) != 0;
                    Log.d(TAG, "状态: " + finalStatusText + ", 是否稳定: " + isStable);

                    runOnUiThread(() -> {
                        tvWeight.setText(String.format("%.1fg", realWeight));
                        tvStatus.setText(finalStatusText);
                        
                        if (isStable) {
                            Log.d(TAG, "[重量稳定] 准备发送广播: weight=" + realWeight);
                            // 发送本地广播
                            Intent intent = new Intent("ACTION_SCALE_WEIGHT");
                            intent.putExtra("weight", realWeight);
                            LocalBroadcastManager.getInstance(ScaleActivity.this).sendBroadcast(intent);
                            Log.d(TAG, "[重量稳定] 已发送广播 ACTION_SCALE_WEIGHT, weight=" + realWeight);
                            
                            // 返回重量数据
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("weight", realWeight);
                            setResult(RESULT_OK, resultIntent);
                        }
                    });
                } else {
                    Log.d(TAG, "数据格式不正确: 长度=" + data.length + 
                        (data.length > 0 ? ", 首字节=0x" + String.format("%02X", data[0]) : ""));
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorWrite - status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "描述符写入成功");
            } else {
                Log.e(TAG, "描述符写入失败: " + status);
            }
        }
    }

    private BluetoothGattCallback gattCallback;

    private void initGattCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            gattCallback = new GattCallback();
        } else {
            gattCallback = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate开始执行");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale);
        Log.d(TAG, "setContentView执行完成");

        // 初始化视图
        Log.d(TAG, "开始初始化视图");
        initView();
        Log.d(TAG, "视图初始化完成");
        
        // 初始化蓝牙
        Log.d(TAG, "开始初始化蓝牙");
        initBluetooth();
        Log.d(TAG, "蓝牙初始化完成");
        
        // 初始化GATT回调
        Log.d(TAG, "开始初始化GATT回调");
        initGattCallback();
        Log.d(TAG, "GATT回调初始化完成");
    }

    private void initView() {
        Log.d(TAG, "开始初始化视图组件");
        try {
            tvStatus = findViewById(R.id.tv_status);
            tvWeight = findViewById(R.id.tv_weight);
            btnScan = findViewById(R.id.btn_scan);
            btnZero = findViewById(R.id.btn_zero);
            btnGetWeight = findViewById(R.id.btn_get_weight);
            recyclerView = findViewById(R.id.rv_devices);
            Log.d(TAG, "基本视图组件初始化完成");

            // 设置RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            deviceAdapter = new BluetoothDeviceAdapter(pairedDevices, newDevices, this);
            recyclerView.setAdapter(deviceAdapter);
            Log.d(TAG, "RecyclerView设置完成");

            btnScan.setOnClickListener(v -> {
                Log.d(TAG, "扫描按钮点击");
                if (scanning) {
                    Log.d(TAG, "停止扫描");
                    stopScan();
                } else {
                    Log.d(TAG, "开始扫描");
                    startScan();
                }
            });

            btnZero.setOnClickListener(v -> {
                Log.d(TAG, "清零按钮点击");
                zero();
            });

            btnGetWeight.setOnClickListener(v -> {
                Log.d(TAG, "获取重量按钮点击");
                getWeight();
            });

            deviceAdapter.setOnItemClickListener(parameter -> {
                Log.d(TAG, "设备列表项点击");
                stopScan();
                if (parameter != null && parameter.getBluetoothDevice() != null) {
                    Log.d(TAG, "准备连接设备: " + parameter.getBluetoothDevice().getAddress());
                    connectToDevice(parameter.getBluetoothDevice());
                } else {
                    Log.e(TAG, "设备信息无效");
                    Toast.makeText(this, "设备信息无效", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d(TAG, "所有视图组件初始化完成");
        } catch (Exception e) {
            Log.e(TAG, "初始化视图失败", e);
            Toast.makeText(this, "初始化视图失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initBluetooth() {
        Log.d(TAG, "开始初始化蓝牙");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "使用新版本蓝牙API");
                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager != null) {
                    bluetoothAdapter = bluetoothManager.getAdapter();
                    Log.d(TAG, "获取到蓝牙适配器");
                } else {
                    Log.e(TAG, "无法获取蓝牙服务");
                }
            } else {
                Log.d(TAG, "使用旧版本蓝牙API");
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

            if (bluetoothAdapter == null) {
                Log.e(TAG, "设备不支持蓝牙");
                Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 检查蓝牙权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.d(TAG, "检查Android 12及以上版本的蓝牙权限");
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限");
                    Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                        REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                    return;
                }
            }

            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "蓝牙未开启，请求开启");
                try {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } catch (SecurityException e) {
                    Log.e(TAG, "无法启用蓝牙", e);
                    Toast.makeText(this, "无法启用蓝牙: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Log.d(TAG, "蓝牙已开启，检查权限并开始扫描");
                checkPermissionsAndStartScan();
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化蓝牙失败", e);
            Toast.makeText(this, "初始化蓝牙失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkPermissionsAndStartScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }
        startScan();
    }

    private void startScan() {
        if (scanning) return;

        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    },
                    REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

        newDevices.clear();
        deviceAdapter.notifyDataSetChanged();
        btnScan.setText("停止扫描");
        scanning = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(scanCallback);
            } else {
                Toast.makeText(this, "无法启动蓝牙扫描", Toast.LENGTH_SHORT).show();
                scanning = false;
                btnScan.setText("开始扫描");
            }
        } else {
            // 对于Android 5.0以下的版本，使用传统蓝牙扫描
            if (bluetoothAdapter != null) {
                bluetoothAdapter.startDiscovery();
            }
        }

        handler.postDelayed(this::stopScan, SCAN_PERIOD);
    }

    private void stopScan() {
        if (!scanning) return;

        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        btnScan.setText("开始扫描");
        scanning = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        } else {
            // 对于Android 5.0以下的版本，停止传统蓝牙扫描
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }

    // 将ScanCallback移到内部类
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(ScaleActivity.this, Manifest.permission.BLUETOOTH_CONNECT) 
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "缺少蓝牙连接权限");
                        Toast.makeText(ScaleActivity.this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(ScaleActivity.this, 
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                            REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                        return;
                    }
                }

                String deviceName = device.getName();
                if (deviceName != null) {
                    BluetoothParameter parameter = new BluetoothParameter();
                    parameter.setBluetoothName(deviceName);
                    parameter.setBluetoothMac(device.getAddress());
                    parameter.setBluetoothDevice(device);
                    
                    if (!newDevices.contains(parameter)) {
                        newDevices.add(parameter);
                        deviceAdapter.notifyDataSetChanged();
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "获取设备信息失败", e);
                Toast.makeText(ScaleActivity.this, "获取设备信息失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void updateStatus(String status) {
        runOnUiThread(() -> {
            tvStatus.setText(status);
        });
    }

    private void connectToDevice(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "设备为空");
            return;
        }

        Log.d(TAG, "准备连接设备: " + device.getAddress());
        
        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "缺少蓝牙连接权限");
                Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                    REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                return;
            }
        }

        Log.d(TAG, "开始连接设备: " + device.getAddress());
        
        // 停止扫描
        stopScan();
        
        // 使用BLE连接设备
        Log.d(TAG, "使用BLE连接设备");
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        
        if (bluetoothGatt != null) {
            Log.d(TAG, "GATT连接已创建");
            
            // 保存设备信息到SharedPreferences
            Log.d(TAG, "保存设备信息到缓存");
            SharedPreferences prefs = getSharedPreferences("scale_cache", MODE_PRIVATE);
            prefs.edit()
                .putString("scale_address", device.getAddress())
                .putString("scale_name", device.getName())
                .apply();
            Log.d(TAG, "设备信息已保存");
            
            // 设置返回结果
            Log.d(TAG, "设置返回结果");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("device_address", device.getAddress());
            resultIntent.putExtra("device_name", device.getName());
            setResult(RESULT_OK, resultIntent);
            
            // 更新UI状态
            updateStatus("已连接");
        } else {
            Log.e(TAG, "GATT连接创建失败");
            updateStatus("连接失败");
        }
    }

    private void onWeightReceived(double weight) {
        runOnUiThread(() -> {
            tvWeight.setText(String.format("%.1fg", weight));
            Log.d(TAG, "[onWeightReceived] 当前重量: " + weight);
            // 发送本地广播
            Intent intent = new Intent("ACTION_SCALE_WEIGHT");
            intent.putExtra("weight", weight);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d(TAG, "[onWeightReceived] 已发送广播 ACTION_SCALE_WEIGHT, weight=" + weight);
            // 返回重量数据（如有需要）
            Intent resultIntent = new Intent();
            resultIntent.putExtra("weight", weight);
            setResult(RESULT_OK, resultIntent);
        });
    }

    private void getWeight() {
        Log.d(TAG, "开始获取重量");
        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "缺少蓝牙连接权限");
                return;
            }
        }

        if (writeCharacteristic != null && bluetoothGatt != null) {
            byte[] cmd = new byte[]{0x05, (byte)0xa9, 0x00, 0x00, 0x00, (byte)0xae};
            Log.d(TAG, "发送获取重量命令: " + bytesToHex(cmd));
            writeCharacteristic.setValue(cmd);
            boolean success = bluetoothGatt.writeCharacteristic(writeCharacteristic);
            Log.d(TAG, "发送命令" + (success ? "成功" : "失败"));
        } else {
            Log.e(TAG, "设备未连接或特征为空");
            Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void zero() {
        Log.d(TAG, "开始清零");
        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "缺少蓝牙连接权限");
                return;
            }
        }

        if (writeCharacteristic != null && bluetoothGatt != null) {
            byte[] cmd = new byte[]{0x05, (byte)0x86, 0x00, 0x00, 0x00, (byte)0x8B};
            Log.d(TAG, "发送清零命令: " + bytesToHex(cmd));
            writeCharacteristic.setValue(cmd);
            boolean success = bluetoothGatt.writeCharacteristic(writeCharacteristic);
            Log.d(TAG, "发送命令" + (success ? "成功" : "失败"));
        } else {
            Log.e(TAG, "设备未连接或特征为空");
            Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    private void closeGatt() {
        // 检查蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeGatt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                checkPermissionsAndStartScan();
            } else {
                Toast.makeText(this, "需要启用蓝牙才能使用此功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndStartScan();
            } else {
                Toast.makeText(this, "需要蓝牙权限才能使用此功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 注销广播接收器
        unregisterReceiver(bluetoothReceiver);
    }
} 