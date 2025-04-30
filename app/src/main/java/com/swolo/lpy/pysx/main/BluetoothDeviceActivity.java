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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BluetoothDeviceActivity extends AppCompatActivity {
    private static final String TAG = BluetoothDeviceActivity.class.getSimpleName();
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;
    private static final int REQUEST_ENABLE_BT = 1;
    
    private ListView lvDevices;
    private BluetoothDeviceAdapter adapter;
    private List<BluetoothParameter> pairedDevices = new ArrayList<>();
    private List<BluetoothParameter> newDevices = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    public static final String EXTRA_DEVICE_ADDRESS = "address";
    private Button btnSearch;
    private boolean mIsSearching = false;

    private final BroadcastReceiver mFindBlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null) return;
                
                BluetoothParameter parameter = new BluetoothParameter();
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                
                // 检查权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothDeviceActivity.this, 
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                
                String deviceName = device.getName();
                parameter.setBluetoothName(deviceName != null ? deviceName : "未知设备");
                parameter.setBluetoothMac(device.getAddress());
                parameter.setBluetoothStrength(String.valueOf(rssi));

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    for (BluetoothParameter p : newDevices) {
                        if (p.getBluetoothMac().equals(parameter.getBluetoothMac())) {
                            return;
                        }
                    }
                    newDevices.add(parameter);
                    Collections.sort(newDevices, new Signal());
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mIsSearching = false;
                btnSearch.setText("搜索设备");
                btnSearch.setVisibility(View.VISIBLE);
                Toast.makeText(BluetoothDeviceActivity.this, "搜索完成", Toast.LENGTH_SHORT).show();
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
            if (position == 0 || position == pairedDevices.size() + 1) {
                return;
            }
            
            String mac = null;
            if (position <= pairedDevices.size()) {
                mac = pairedDevices.get(position - 1).getBluetoothMac();
            } else {
                mac = newDevices.get(position - pairedDevices.size() - 2).getBluetoothMac();
            }
            
            if (mBluetoothAdapter == null) {
                return;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            
            mBluetoothAdapter.cancelDiscovery();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, mac);
            setResult(Activity.RESULT_OK, intent);
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
        registerReceiver(mFindBlueToothReceiver, filter);
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            };
            
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, 
                    permissionsToRequest.toArray(new String[0]), 
                    REQUEST_BLUETOOTH_PERMISSIONS);
            } else {
                // 权限已授予，检查蓝牙是否开启
                checkBluetoothEnabled();
            }
        } else {
            // Android 11 及以下版本不需要这些权限
            checkBluetoothEnabled();
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
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                checkBluetoothEnabled();
            } else {
                Toast.makeText(this, "需要蓝牙权限才能使用此功能", Toast.LENGTH_SHORT).show();
                finish();
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
        
        mBluetoothAdapter.cancelDiscovery();
    }

    private void startSearch() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙适配器未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "需要蓝牙扫描权限", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        cancelDiscovery();
        
        newDevices.clear();
        adapter.notifyDataSetChanged();
        mIsSearching = true;
        btnSearch.setText("停止搜索");
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
} 