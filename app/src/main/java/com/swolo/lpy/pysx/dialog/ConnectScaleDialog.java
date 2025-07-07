package com.swolo.lpy.pysx.dialog;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.swolo.lpy.pysx.R;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ConnectScaleDialog extends Dialog {
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;
    private RecyclerView rvDevices;
    private ProgressBar progressBar;
    private Button btnScan;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private DeviceAdapter deviceAdapter;
    private TextView tvStatus;
    private static final long SCAN_PERIOD = 10000; // 10 seconds
    private static final String TAG = "ConnectScaleDialog";
    private Handler handler = new Handler(Looper.getMainLooper());

    // 将 OnItemClickListener 提升为 ConnectScaleDialog 的成员接口
    interface OnItemClickListener {
        void onClick(BluetoothDevice device);
    }

    public interface OnConnectListener {
        void onScaleConnected(String address, String name);
    }
    private OnConnectListener onConnectListener;
    public void setOnConnectListener(OnConnectListener listener) {
        this.onConnectListener = listener;
    }

    public ConnectScaleDialog(Context context) {
        super(context);
        Log.d(TAG, "ConnectScaleDialog 构造函数调用");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_connect_scale);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(lp);
        }
        initViews();
        checkBluetoothPermissions();
    }

    private void initViews() {
        Log.d(TAG, "initViews");
        rvDevices = findViewById(R.id.rv_devices);
        progressBar = findViewById(R.id.progress_bar);
        btnScan = findViewById(R.id.btn_scan);
        tvStatus = findViewById(R.id.tv_status);
        rvDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceAdapter = new DeviceAdapter(deviceList);
        rvDevices.setAdapter(deviceAdapter);
        
        // 检查蓝牙权限后初始化适配器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "缺少蓝牙连接权限，无法初始化蓝牙适配器");
                bluetoothAdapter = null;
            } else {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
        } else {
            // API 30及以下版本，蓝牙权限在安装时自动授予
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        btnScan.setOnClickListener(v -> {
            Log.d(TAG, "btnScan clicked, isScanning=" + isScanning);
            if (!isScanning) {
                if (checkBluetoothPermissions()) {
                    startScan();
                }
            } else {
                stopScan();
            }
        });
        deviceAdapter.setOnItemClickListener(device -> {
            Log.d(TAG, "设备点击: " + (device != null ? device.getName() : "null"));
            stopScan();
            connectToDevice(device);
        });
    }

    private boolean checkBluetoothPermissions() {
        Log.d(TAG, "checkBluetoothPermissions");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "BLUETOOTH_SCAN 或 BLUETOOTH_CONNECT 权限未授予");
                    ActivityCompat.requestPermissions((android.app.Activity) getContext(),
                        new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
                    return false;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "ACCESS_FINE_LOCATION 权限未授予");
                    ActivityCompat.requestPermissions((android.app.Activity) getContext(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_BLUETOOTH_PERMISSIONS);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "checkBluetoothPermissions 异常", e);
            return false;
        }
        return true;
    }

    private void startScan() {
        Log.d(TAG, "startScan");
        if (!checkBluetoothPermissions()) {
            tvStatus.setText("未获得蓝牙权限，无法扫描");
            Toast.makeText(getContext(), "请授予蓝牙相关权限后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        // 权限检查后再初始化
        if (bluetoothAdapter == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "缺少蓝牙连接权限，无法初始化蓝牙适配器");
                    bluetoothAdapter = null;
                } else {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
            } else {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }
        }
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "蓝牙未打开或适配器为null");
            tvStatus.setText("请打开蓝牙");
            return;
        }

        if (isScanning) {
            Log.d(TAG, "已在扫描中，调用 stopScan");
            stopScan();
            return;
        }

        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();
        isScanning = true;
        btnScan.setText("停止扫描");
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("正在扫描...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                Log.d(TAG, "BLE 扫描开始");
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                bluetoothLeScanner.startScan(null, settings, scanCallback);
            } else {
                Log.w(TAG, "bluetoothLeScanner 为 null");
            }
        } else {
            Log.d(TAG, "经典蓝牙扫描开始");
            bluetoothAdapter.startDiscovery();
        }

        handler.postDelayed(this::stopScan, SCAN_PERIOD);
    }

    private void stopScan() {
        Log.d(TAG, "stopScan");
        if (!isScanning) return;
        
        isScanning = false;
        btnScan.setText("开始扫描");
        progressBar.setVisibility(View.GONE);
        tvStatus.setText("扫描完成");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothLeScanner != null) {
                Log.d(TAG, "BLE 扫描停止");
                bluetoothLeScanner.stopScan(scanCallback);
            }
        } else {
            Log.d(TAG, "经典蓝牙扫描停止");
            // stopScan前也要加权限检查
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "缺少蓝牙连接权限，无法取消经典蓝牙扫描");
                    return;
                }
            }
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "connectToDevice: " + (device != null ? device.getName() : "null"));
        try {
            SharedPreferences sp = getContext().getSharedPreferences("scale_cache", Context.MODE_PRIVATE);
            sp.edit().putString("scale_address", device.getAddress()).putString("scale_name", device.getName()).apply();
            Toast.makeText(getContext(), "已保存蓝牙秤: " + device.getName(), Toast.LENGTH_SHORT).show();
            if (onConnectListener != null) {
                Log.e(TAG, "【回调】蓝牙秤连接成功，回调Activity");
                onConnectListener.onScaleConnected(device.getAddress(), device.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "connectToDevice 异常", e);
        }
        dismiss();
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null && device.getName() != null && !deviceList.contains(device)) {
                deviceList.add(device);
                deviceAdapter.notifyDataSetChanged();
            }
        }
    };

    // 设备列表适配器
    private class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.Holder> {
        private List<BluetoothDevice> list;
        private OnItemClickListener listener;
        
        DeviceAdapter(List<BluetoothDevice> list) { 
            this.list = list; 
        }
        
        void setOnItemClickListener(OnItemClickListener l) { 
            this.listener = l; 
        }
        
        @Override 
        public Holder onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_1, p, false);
            return new Holder(view);
        }
        
        @Override 
        public void onBindViewHolder(Holder h, int pos) {
            BluetoothDevice d = list.get(pos);
            ((TextView) h.itemView).setText(d.getName() + "\n" + d.getAddress());
            h.itemView.setOnClickListener(v -> { 
                if (listener != null) listener.onClick(d); 
            });
        }
        
        @Override 
        public int getItemCount() { 
            return list.size(); 
        }
        
        class Holder extends RecyclerView.ViewHolder { 
            Holder(View v) { 
                super(v); 
            } 
        }
    }
} 