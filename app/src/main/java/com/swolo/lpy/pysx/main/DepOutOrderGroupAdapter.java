package com.swolo.lpy.pysx.main;

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

public class DepOutOrderGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Item> flatList = new ArrayList<>();
    private boolean showDelete = false;
    private OnGoodsClickListener goodsClickListener;
    private OnGoodsDeleteListener goodsDeleteListener;

    public void setData(List<Object> groupList) {
        flatList.clear();
        if (groupList != null) {
            for (Object greatGrand : groupList) {
                if (greatGrand instanceof GreatGrand) {
                    GreatGrand gg = (GreatGrand) greatGrand;
                    for (FatherGoodsEntities grand : gg.fatherGoodsEntities) {
                        flatList.add(new Item(Item.TYPE_GROUP, grand.nxDfgFatherGoodsName));
                        for (PurchaseGoodsEntities purGoods : grand.nxDistributerPurchaseGoodsEntities) {
                            for (com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order : purGoods.nxDepartmentOrdersEntities) {
                                flatList.add(new Item(Item.TYPE_GOODS, purGoods, order));
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setShowDelete(boolean show) { this.showDelete = show; }
    public void setOnGoodsClickListener(OnGoodsClickListener l) { this.goodsClickListener = l; }
    public void setOnGoodsDeleteListener(OnGoodsDeleteListener l) { this.goodsDeleteListener = l; }

    @Override
    public int getItemViewType(int position) {
        return flatList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Item.TYPE_GROUP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dep_out_order_group, parent, false);
            return new GroupHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dep_out_order_goods, parent, false);
            return new GoodsHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = flatList.get(position);
        if (item.type == Item.TYPE_GROUP) {
            ((GroupHolder) holder).groupName.setText(item.groupName);
        } else if (item.type == Item.TYPE_GOODS) {
            GoodsHolder goodsHolder = (GoodsHolder) holder;
            goodsHolder.bind(item.purGoods, item.order, showDelete);
            goodsHolder.itemView.setOnClickListener(v -> {
                if (goodsClickListener != null) goodsClickListener.onGoodsClick(item.purGoods, item.order);
            });
            goodsHolder.ivDelete.setOnClickListener(v -> {
                if (goodsDeleteListener != null) goodsDeleteListener.onGoodsDelete(item.purGoods, item.order);
            });
        }
    }

    @Override
    public int getItemCount() {
        return flatList.size();
    }

    static class GroupHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        GroupHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.tv_group_name);
        }
    }

    static class GoodsHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvName, tvOrder;
        ImageView ivDelete;
        GoodsHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_index);
            tvName = itemView.findViewById(R.id.tv_name);
            tvOrder = itemView.findViewById(R.id.tv_order);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
        void bind(PurchaseGoodsEntities purGoods, com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order, boolean showDelete) {
            tvIndex.setText(order.indexStr);
            tvName.setText(purGoods.nxDistributerGoodsEntity.nxDgGoodsName);
            tvOrder.setText("订:" + order.nxDoQuantity + order.nxDoStandard);
            ivDelete.setVisibility(showDelete ? View.VISIBLE : View.GONE);
        }
    }

    static class Item {
        static final int TYPE_GROUP = 0;
        static final int TYPE_GOODS = 1;
        int type;
        String groupName;
        PurchaseGoodsEntities purGoods;
        com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order;
        Item(int type, String groupName) {
            this.type = type; this.groupName = groupName;
        }
        Item(int type, PurchaseGoodsEntities purGoods, com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order) {
            this.type = type; this.purGoods = purGoods; this.order = order;
        }
    }

    // 数据结构定义（可根据实际数据源调整）
    public static class GreatGrand {
        public List<FatherGoodsEntities> fatherGoodsEntities;
    }
    public static class FatherGoodsEntities {
        public String nxDfgFatherGoodsName;
        public List<PurchaseGoodsEntities> nxDistributerPurchaseGoodsEntities;
    }
    public static class PurchaseGoodsEntities {
        public DistributerGoodsEntity nxDistributerGoodsEntity;
        public List<com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
    }
    public static class DistributerGoodsEntity {
        public String nxDgGoodsName;
        public String nxDgGoodsStandardname;
    }

    public interface OnGoodsClickListener {
        void onGoodsClick(PurchaseGoodsEntities purGoods, com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order);
    }
    public interface OnGoodsDeleteListener {
        void onGoodsDelete(PurchaseGoodsEntities purGoods, com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order);
    }
} 