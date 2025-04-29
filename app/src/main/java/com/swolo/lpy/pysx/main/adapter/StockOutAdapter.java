package com.swolo.lpy.pysx.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;

import java.util.ArrayList;
import java.util.List;

public class StockOutAdapter extends RecyclerView.Adapter<StockOutAdapter.ViewHolder> {
    private Context context;
    private List<NxDepartmentOrdersEntity> dataList;
    private OnItemClickListener listener;

    public StockOutAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    public void setData(List<NxDepartmentOrdersEntity> data) {
        this.dataList = data;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock_out, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NxDepartmentOrdersEntity item = dataList.get(position);
        holder.tvGoodsName.setText(item.getNxDistributerGoodsEntity().nxDgGoodsName);
        holder.tvQuantity.setText(item.getNxDoQuantity() != null ? item.getNxDoQuantity().toString() : "0");
        holder.tvWeight.setText(item.getNxDoWeight() + item.getNxDoStandard());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(NxDepartmentOrdersEntity item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoodsName;
        TextView tvQuantity;
        TextView tvWeight;

        ViewHolder(View itemView) {
            super(itemView);
            tvGoodsName = itemView.findViewById(R.id.tv_goods_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvWeight = itemView.findViewById(R.id.tv_weight);
        }
    }
} 