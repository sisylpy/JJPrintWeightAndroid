package com.swolo.lpy.pysx.main.adapter;

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

public class DepOutOrderSimpleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_GROUP = 0;
    public static final int TYPE_GOODS = 1;
    public static final int TYPE_ORDER = 2;

    private List<Item> data = new ArrayList<>();
    private OnDeleteClickListener deleteClickListener;
    private boolean showDeleteButtons = false; // 是否显示删除按钮

    public void setData(List<Item> items) {
        android.util.Log.d("DepOutOrderSimpleAdapter", "setData开始: items.size=" + (items != null ? items.size() : 0));
        data.clear();
        if (items != null) data.addAll(items);
        android.util.Log.d("DepOutOrderSimpleAdapter", "setData: data.size=" + data.size());
        notifyDataSetChanged();
        android.util.Log.d("DepOutOrderSimpleAdapter", "setData完成");
    }
    
    public void setShowDeleteButtons(boolean show) {
        this.showDeleteButtons = show;
        notifyDataSetChanged();
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }
    
    public List<Item> getData() {
        return data;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_GROUP) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
            return new GroupHolder(v);
        } else if (viewType == TYPE_GOODS) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goods, parent, false);
            return new GoodsHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new OrderHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = data.get(position);
        android.util.Log.d("DepOutOrderSimpleAdapter", "onBindViewHolder: position=" + position + 
              ", type=" + item.type + ", text=" + item.text);
        if (item.type == TYPE_GROUP) {
            ((GroupHolder) holder).tvGroup.setText(item.text);
        } else if (item.type == TYPE_ORDER) {
            OrderHolder orderHolder = (OrderHolder) holder;
            orderHolder.tvIndex.setText(item.orderIndex + ".");
            orderHolder.tvGoodsName.setText(item.goodsName);
            String info = "订:" + item.orderQuantity + item.orderUnit + "  出货:" + item.outWeight + item.outUnit;
            orderHolder.tvOrderInfo.setText(info);
            if (showDeleteButtons && item.orderId != null) {
                orderHolder.ivDelete.setVisibility(View.VISIBLE);
                orderHolder.ivDelete.setOnClickListener(v -> {
                    if (deleteClickListener != null) {
                        deleteClickListener.onDeleteClick(item.orderId, item.orderObject);
                    }
                });
            } else {
                orderHolder.ivDelete.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvGroup;
        GroupHolder(View v) {
            super(v);
            tvGroup = v.findViewById(R.id.tv_group);
        }
    }
    static class GoodsHolder extends RecyclerView.ViewHolder {
        TextView tvGoods;
        ImageView ivDelete;
        GoodsHolder(View v) {
            super(v);
            tvGoods = v.findViewById(R.id.tv_goods);
            ivDelete = v.findViewById(R.id.iv_delete);
        }
    }
    static class OrderHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        TextView tvGoodsName;
        TextView tvOrderInfo;
        ImageView ivDelete;
        OrderHolder(View v) {
            super(v);
            tvIndex = v.findViewById(R.id.tv_index);
            tvGoodsName = v.findViewById(R.id.tv_goods_name);
            tvOrderInfo = v.findViewById(R.id.tv_order_info);
            ivDelete = v.findViewById(R.id.iv_delete);
        }
    }

    public static class Item {
        public int type;
        public String text; // 分组用
        public String orderId; // 订单ID
        public Object orderObject; // 完整订单对象
        // 订单项专用
        public int orderIndex;
        public String goodsName;
        public String orderQuantity;
        public String orderUnit;
        public String outWeight;
        public String outUnit;
        public Item(int type, String text) {
            this.type = type;
            this.text = text;
        }
        public Item(int type, int orderIndex, String goodsName, String orderQuantity, String orderUnit, String outWeight, String outUnit, String orderId, Object orderObject) {
            this.type = type;
            this.orderIndex = orderIndex;
            this.goodsName = goodsName;
            this.orderQuantity = orderQuantity;
            this.orderUnit = orderUnit;
            this.outWeight = outWeight;
            this.outUnit = outUnit;
            this.orderId = orderId;
            this.orderObject = orderObject;
        }
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(String orderId, Object orderObject);
    }
} 