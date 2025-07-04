package com.swolo.lpy.pysx.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.bean.BluetoothParameter;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TITLE = 0;
    private static final int TYPE_DEVICE = 1;

    private List<BluetoothParameter> pairedDevices;
    private List<BluetoothParameter> newDevices;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BluetoothParameter device);
    }

    public BluetoothDeviceAdapter(List<BluetoothParameter> pairedDevices, List<BluetoothParameter> newDevices, Context context) {
        this.pairedDevices = pairedDevices;
        this.newDevices = newDevices;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == pairedDevices.size() + 1) {
            return TYPE_TITLE;
        }
        return TYPE_DEVICE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TITLE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_title, parent, false);
            return new TitleViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_device, parent, false);
            return new DeviceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleHolder = (TitleViewHolder) holder;
            titleHolder.tvTitle.setText(position == 0 ? "已配对设备" : "可用设备");
        } else if (holder instanceof DeviceViewHolder) {
            DeviceViewHolder deviceHolder = (DeviceViewHolder) holder;
            BluetoothParameter device;
            if (position <= pairedDevices.size()) {
                device = pairedDevices.get(position - 1);
            } else {
                device = newDevices.get(position - pairedDevices.size() - 2);
            }
            
            deviceHolder.tvName.setText(device.getBluetoothName());
            deviceHolder.tvAddress.setText(device.getBluetoothMac());
            
            deviceHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(device);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 2 + pairedDevices.size() + newDevices.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        TitleViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvAddress;

        DeviceViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }
} 