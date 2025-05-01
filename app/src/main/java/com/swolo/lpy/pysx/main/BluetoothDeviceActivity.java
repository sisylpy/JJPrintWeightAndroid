package com.swolo.lpy.pysx.main;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

//import androidx.appcompat.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.adapter.BluetoothDeviceAdapter;
import com.swolo.lpy.pysx.main.bean.BluetoothParameter;
import com.printer.command.LabelCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class BluetoothDeviceActivity extends AppCompatActivity {
    private static final String TAG = BluetoothDeviceActivity.class.getSimpleName();
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1002;
    
    private ListView lvDevices;
    private BluetoothDeviceAdapter adapter;
    private List<BluetoothParameter> pairedDevices = new ArrayList<>();
    private List<BluetoothParameter> newDevices = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    public static final String EXTRA_DEVICE_ADDRESS = "address";
    private Button btnSearch;
    private boolean mIsSearching = false;
    private String mSelectedMacAddress;

    private final BroadcastReceiver mFindBlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "收到蓝牙广播: " + action);
            
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "蓝牙搜索开始");
                mIsSearching = true;
                btnSearch.setText("停止搜索");
                newDevices.clear();
                adapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) return;
                
                // 检查权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothDeviceActivity.this, 
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                
                Log.d(TAG, "发现设备: " + deviceName + " (" + deviceAddress + "), RSSI: " + rssi + 
                          ", BondState: " + device.getBondState());
                
                // 检查设备是否未配对
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    BluetoothParameter parameter = new BluetoothParameter();
                    parameter.setBluetoothName(deviceName != null ? deviceName : "未知设备");
                    parameter.setBluetoothMac(deviceAddress);
                    parameter.setBluetoothStrength(String.valueOf(rssi));
                    
                    // 检查是否已存在
                    boolean exists = false;
                    for (BluetoothParameter p : newDevices) {
                        if (p.getBluetoothMac().equals(deviceAddress)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (!exists) {
                        Log.d(TAG, "添加新设备到列表: " + deviceName + " (" + deviceAddress + ")");
                        newDevices.add(parameter);
                        Collections.sort(newDevices, new Signal());
                        adapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "蓝牙搜索完成");
                mIsSearching = false;
                btnSearch.setText("搜索设备");
                if (newDevices.isEmpty()) {
                    Toast.makeText(BluetoothDeviceActivity.this, "未发现新设备", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BluetoothDeviceActivity.this, "搜索完成，发现 " + newDevices.size() + " 个新设备", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    static class Signal implements Comparator<BluetoothParameter> {
        public int compare(BluetoothParameter p1, BluetoothParameter p2) {
            return p1.getBluetoothStrength().compareTo(p2.getBluetoothStrength());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initView();
        initBluetooth();
        initBroadcast();
    }

    private void initView() {
        lvDevices = findViewById(R.id.lv_devices);
        btnSearch = findViewById(R.id.btn_search);
        
        adapter = new BluetoothDeviceAdapter(pairedDevices, newDevices, this);
        lvDevices.setAdapter(adapter);
        
        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, "点击了设备列表项，位置: " + position);
            if (position == 0 || position == pairedDevices.size() + 1) {
                Log.d(TAG, "点击了标题项，忽略");
                return;
            }
            
            String mac = null;
            if (position <= pairedDevices.size()) {
                mac = pairedDevices.get(position - 1).getBluetoothMac();
                Log.d(TAG, "选择了已配对设备，MAC: " + mac);
            } else {
                mac = newDevices.get(position - pairedDevices.size() - 2).getBluetoothMac();
                Log.d(TAG, "选择了新设备，MAC: " + mac);
            }
            
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "蓝牙适配器为空");
                return;
            }
            
            // 检查权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限");
                    Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                        REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                    return;
                }
            }
            
            Log.d(TAG, "取消蓝牙搜索");
            mBluetoothAdapter.cancelDiscovery();
            
            // 保存选中的MAC地址
            mSelectedMacAddress = mac;
            
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, mac);
            setResult(Activity.RESULT_OK, intent);
            
            Log.d(TAG, "开始打印测试小票");
            printTestReceipt();
            
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            if (mIsSearching) {
                stopSearch();
            } else {
                startSearch();
            }
        });
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 检查并请求权限
        checkAndRequestPermissions();
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);  // 添加搜索开始的广播
        registerReceiver(mFindBlueToothReceiver, filter);
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,  // 添加位置权限
                Manifest.permission.ACCESS_COARSE_LOCATION // 添加位置权限
            };
            
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                Log.d(TAG, "请求权限: " + permissionsToRequest);
                ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    REQUEST_BLUETOOTH_PERMISSIONS);
            } else {
                // 权限已授予，检查蓝牙是否开启
                checkBluetoothEnabled();
            }
        } else {
            // Android 11 及以下版本需要位置权限
            String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
            
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                Log.d(TAG, "请求位置权限: " + permissionsToRequest);
                ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    REQUEST_BLUETOOTH_PERMISSIONS);
            } else {
                checkBluetoothEnabled();
            }
        }
    }

    private void checkBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要蓝牙连接权限", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            loadPairedDevices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "收到权限请求结果，requestCode: " + requestCode);
        
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Log.d(TAG, "所有蓝牙权限已授予");
                checkBluetoothEnabled();
            } else {
                Log.e(TAG, "部分或全部蓝牙权限被拒绝");
                Toast.makeText(this, "需要蓝牙权限才能使用此功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "蓝牙连接权限已授予");
                // 重新尝试打印
                printTestReceipt();
            } else {
                Log.e(TAG, "蓝牙连接权限被拒绝");
                Toast.makeText(this, "需要蓝牙连接权限才能打印", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPairedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        this.pairedDevices.clear();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BluetoothParameter parameter = new BluetoothParameter();
                String deviceName = device.getName();
                parameter.setBluetoothName(deviceName != null ? deviceName : "未知设备");
                parameter.setBluetoothMac(device.getAddress());
                parameter.setBluetoothStrength("已配对");
                this.pairedDevices.add(parameter);
            }
            adapter.notifyDataSetChanged();
        }
        startSearch();
    }

    private void cancelDiscovery() {
        if (mBluetoothAdapter == null) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        
        if (mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "取消蓝牙搜索");
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private void startSearch() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙适配器未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查蓝牙是否开启
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "请先开启蓝牙", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        
        // 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要蓝牙扫描权限", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // 检查位置权限（Android 12以下需要）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要位置权限才能搜索蓝牙设备", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // 如果正在搜索，先停止
        if (mIsSearching) {
            stopSearch();
            return;
        }
        
        Log.d(TAG, "开始搜索蓝牙设备");
        cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
    }

    private void stopSearch() {
        if (mBluetoothAdapter == null) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        
        Log.d(TAG, "停止搜索蓝牙设备");
        mIsSearching = false;
        btnSearch.setText("搜索设备");
        cancelDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                loadPairedDevices();
            } else {
                Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelDiscovery();
        unregisterReceiver(mFindBlueToothReceiver);
    }

    private void printTestReceipt() {
        Log.d(TAG, "开始打印测试小票");
        try {
            // 检查权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "缺少蓝牙连接权限");
                    Toast.makeText(this, "缺少蓝牙连接权限", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 
                        REQUEST_BLUETOOTH_CONNECT_PERMISSION);
                    return;
                }
            }

            // 检查MAC地址
            if (mSelectedMacAddress == null) {
                Log.e(TAG, "未获取到蓝牙设备地址");
                Toast.makeText(this, "未获取到蓝牙设备地址", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "初始化打印机管理器，MAC地址: " + mSelectedMacAddress);
            try {
                // 关闭之前的连接
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0] != null) {
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].closePort(0);
                }

                // 建立新的蓝牙连接
                new DeviceConnFactoryManager.Build()
                        .setId(0)
                        .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                        .setMacAddress(mSelectedMacAddress)
                        .setContext(this)
                        .build();
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].openPort();
                Log.d(TAG, "打印机管理器初始化成功");
            } catch (Exception e) {
                Log.e(TAG, "打印机管理器初始化失败", e);
                Toast.makeText(this, "打印机连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查打印机状态
            if (!DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].getConnState()) {
                Log.e(TAG, "打印机未连接");
                Toast.makeText(this, "打印机未连接", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "创建LabelCommand对象");
            LabelCommand tsc = new LabelCommand();
            tsc.addSize(40, 30); // 设置标签大小
            tsc.addGap(2); // 设置标签间隙
            tsc.addCls(); // 清除缓冲区
            tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL); // 设置打印方向
            tsc.addReference(0, 0); // 设置参考点
            tsc.addHome(); // 设置打印位置
            tsc.addDensity(LabelCommand.DENSITY.DNESITY4); // 设置打印浓度

            // 添加文本
            tsc.addText(50, 50, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "测试小票");
            tsc.addText(50, 100, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "蓝牙连接测试");
            tsc.addText(50, 150, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "时间: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            tsc.addText(50, 200, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "设备: " + mBluetoothAdapter.getName());
            tsc.addText(50, 250, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1, "测试打印成功！");

            tsc.addPrint(1, 1); // 设置打印份数
            tsc.addSound(2, 100); // 设置蜂鸣器
            tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255); // 设置钱箱
            
            Log.d(TAG, "获取打印命令数据");
            Vector<Byte> datas = tsc.getCommand();
            
            try {
                Log.d(TAG, "准备发送打印数据");
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[0].sendDataImmediately(datas);
                Log.d(TAG, "数据发送成功");
                Toast.makeText(this, "测试小票打印成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "发送数据失败", e);
                Toast.makeText(this, "发送数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "打印测试小票失败", e);
            Toast.makeText(this, "打印测试小票失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 