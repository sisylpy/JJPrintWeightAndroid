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

public class BluetoothDeviceListAdapter extends BaseAdapter {
    private static final int TYPE_TITLE = 0;
    private static final int TYPE_DEVICE = 1;
    private static final int TYPE_COUNT = 2;

    private List<BluetoothParameter> pairedDevices;
    private List<BluetoothParameter> newDevices;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BluetoothParameter device);
    }

    public BluetoothDeviceListAdapter(List<BluetoothParameter> pairedDevices, List<BluetoothParameter> newDevices, Context context) {
        this.pairedDevices = pairedDevices;
        this.newDevices = newDevices;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == pairedDevices.size() + 1) {
            return TYPE_TITLE;
        }
        return TYPE_DEVICE;
    }

    @Override
    public int getCount() {
        return 2 + pairedDevices.size() + newDevices.size();
    }

    @Override
    public BluetoothParameter getItem(int position) {
        if (position == 0 || position == pairedDevices.size() + 1) {
            return null;
        }
        if (position <= pairedDevices.size()) {
            return pairedDevices.get(position - 1);
        }
        return newDevices.get(position - pairedDevices.size() - 2);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_DEVICE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            if (type == TYPE_TITLE) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_title, parent, false);
                holder.tvTitle = convertView.findViewById(R.id.tv_title);
            } else {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device, parent, false);
                holder.tvName = convertView.findViewById(R.id.tv_name);
                holder.tvAddress = convertView.findViewById(R.id.tv_address);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == TYPE_TITLE) {
            holder.tvTitle.setText(position == 0 ? "已配对设备" : "可用设备");
        } else {
            BluetoothParameter device = getItem(position);
            holder.tvName.setText(device.getBluetoothName());
            holder.tvAddress.setText(device.getBluetoothMac());
            
            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(device);
                }
            });
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvName;
        TextView tvAddress;
    }
} 