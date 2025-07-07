package com.swolo.lpy.pysx.main.bean;

import android.bluetooth.BluetoothDevice;

public class BluetoothParameter {
    private String bluetoothName;
    private String bluetoothMac;
    private BluetoothDevice bluetoothDevice;

    public String getBluetoothName() {
        return bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothMac() {
        return bluetoothMac;
    }

    public void setBluetoothMac(String bluetoothMac) {
        this.bluetoothMac = bluetoothMac;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BluetoothParameter that = (BluetoothParameter) o;
        return bluetoothMac != null && bluetoothMac.equals(that.bluetoothMac);
    }

    @Override
    public int hashCode() {
        return bluetoothMac != null ? bluetoothMac.hashCode() : 0;
    }
} 