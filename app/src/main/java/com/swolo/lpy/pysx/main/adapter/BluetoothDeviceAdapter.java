package com.swolo.lpy.pysx.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.bean.BluetoothParameter;

import java.util.List;

public class BluetoothDeviceAdapter extends BaseAdapter {
    private List<BluetoothParameter> pairedDevices;
    private List<BluetoothParameter> newDevices;
    private Context mContext;
    private static final int TITLE = 0;
    private static final int CONTENT = 1;

    public BluetoothDeviceAdapter(List<BluetoothParameter> pairedDevices, List<BluetoothParameter> newDevices, Context context) {
        this.pairedDevices = pairedDevices;
        this.newDevices = newDevices;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return pairedDevices.size() + newDevices.size() + 2;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0 || position == pairedDevices.size() + 1) {
            // 标题项
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.text_item, parent, false);
            }
            TextView tvTitle = convertView.findViewById(R.id.text);
            if (position == 0) {
                tvTitle.setText("已配对设备");
            } else {
                tvTitle.setText("未配对设备");
            }
        } else {
            // 设备项
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_list_item, parent, false);
            }
            BluetoothParameter device;
            if (position <= pairedDevices.size()) {
                device = pairedDevices.get(position - 1);
            } else {
                device = newDevices.get(position - pairedDevices.size() - 2);
            }

            TextView tvName = convertView.findViewById(R.id.b_name);
            TextView tvMac = convertView.findViewById(R.id.b_mac);
            TextView tvStrength = convertView.findViewById(R.id.b_info);

            tvName.setText(device.getBluetoothName());
            tvMac.setText(device.getBluetoothMac());
            tvStrength.setText("信号强度: " + device.getBluetoothStrength());
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == pairedDevices.size() + 1) {
            return TITLE;
        }
        return CONTENT;
    }
} 