package com.swolo.lpy.pysx.main;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swolo.lpy.pysx.R;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private List<CustomerListActivity.Customer> data;
    private List<Integer> selectedIndexes = new ArrayList<>();
    private OnItemClickListener listener;

    public CustomerAdapter(List<CustomerListActivity.Customer> data) {
        this.data = data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedIndex(int index) {
        selectedIndexes.clear();
        if (index >= 0) {
            selectedIndexes.add(index);
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int index) {
        if (selectedIndexes.contains(index)) {
            selectedIndexes.remove(Integer.valueOf(index));
        } else {
            selectedIndexes.add(index);
        }
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedIndexes() {
        return new ArrayList<>(selectedIndexes);
    }

    public boolean hasSelection() {
        return !selectedIndexes.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerListActivity.Customer customer = data.get(position);
        Log.d("CustomerAdapter", "onBindViewHolder: position=" + position + ", name=" + customer.name + ", orderCount=" + customer.orderCount + ", unpickedCount=" + customer.unpickedCount);
        
        holder.tvName.setText(customer.name);
        holder.tvOrder.setText("订单: " + customer.orderCount + "个");
        holder.tvUnpicked.setText("未拣货: " + customer.unpickedCount + "个");
        holder.tvUnpicked.setTextColor(customer.unpickedCount > 0 ? Color.parseColor("#198754") : Color.parseColor("#666666"));
        if (customer.unpickedCount > 0) {
            if (selectedIndexes.contains(position)) {
                ImageView ivRadioBg = holder.itemView.findViewById(R.id.iv_radio_bg);
                ivRadioBg.setImageResource(R.drawable.bg_radio_selected);
                holder.ivRadio.setVisibility(View.VISIBLE);
            } else {
                ImageView ivRadioBg = holder.itemView.findViewById(R.id.iv_radio_bg);
                ivRadioBg.setImageResource(R.drawable.bg_radio_unselected);
                holder.ivRadio.setVisibility(View.GONE);
            }
            holder.itemView.setAlpha(1f);
        } else {
            ImageView ivRadioBg = holder.itemView.findViewById(R.id.iv_radio_bg);
            ivRadioBg.setImageResource(R.drawable.bg_radio_unselected);
            holder.ivRadio.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.5f);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        int count = data.size();
        Log.d("CustomerAdapter", "getItemCount: " + count);
        return count;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvOrder, tvUnpicked;
        ImageView ivRadio;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_customer_name);
            tvOrder = itemView.findViewById(R.id.tv_order_count);
            tvUnpicked = itemView.findViewById(R.id.tv_unpicked_count);
            ivRadio = itemView.findViewById(R.id.iv_radio);
            
            // 调试日志
            Log.d("CustomerAdapter", "ViewHolder创建: tvName=" + (tvName != null) + ", tvOrder=" + (tvOrder != null) + ", tvUnpicked=" + (tvUnpicked != null) + ", ivRadio=" + (ivRadio != null));
        }
    }
} 