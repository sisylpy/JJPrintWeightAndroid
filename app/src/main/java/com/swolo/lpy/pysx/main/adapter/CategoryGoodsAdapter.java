package com.swolo.lpy.pysx.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CategoryGoodsAdapter extends RecyclerView.Adapter<CategoryGoodsAdapter.InnerHolder> {
    private static final String TAG = "CategoryGoodsAdapter";
    private List<NxDistributerGoodsEntity> goodsList = new ArrayList<>();
    private OnItemClickListener mListener;
    private int selectedPosition = 0; // 当前选中的商品位置，默认选中第一个

    public interface OnItemClickListener {
        void onItemClick(NxDistributerGoodsEntity entity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        Log.d(TAG, "设置选中位置: " + position + ", 当前数据大小: " + goodsList.size());
        if (position >= 0 && position < goodsList.size()) {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public void setGoodsList(List<NxDistributerGoodsEntity> goodsList) {
        Log.d(TAG, "设置商品列表，数量: " + (goodsList != null ? goodsList.size() : 0));
        if (goodsList != null) {
            for (NxDistributerGoodsEntity goods : goodsList) {
                Log.d(TAG, "商品: " + goods.getNxDgGoodsName() + 
                      ", 订单数量: " + (goods.getNxDepartmentOrdersEntities() != null ? 
                      goods.getNxDepartmentOrdersEntities().size() : 0));
            }
        }
        this.goodsList = goodsList != null ? goodsList : new ArrayList<>();
        
        // 调整选中位置，确保不越界
        if (selectedPosition >= goodsList.size()) {
            selectedPosition = goodsList.size() > 0 ? 0 : -1;
        }
        
        notifyDataSetChanged();
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_goods, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        NxDistributerGoodsEntity goods = goodsList.get(position);
        Log.d(TAG, "绑定商品视图，位置: " + position + 
              ", 商品: " + goods.getNxDgGoodsName() + 
              ", 订单数量: " + (goods.getNxDepartmentOrdersEntities() != null ? 
              goods.getNxDepartmentOrdersEntities().size() : 0));
        
        // 设置品牌
        String brand = goods.getNxDgGoodsBrand();
        if (brand != null && !brand.isEmpty() && !"null".equals(brand)) {
            holder.tvBrand.setVisibility(View.VISIBLE);
            holder.tvBrand.setText(brand);
        } else {
            holder.tvBrand.setVisibility(View.GONE);
        }

        // 设置商品名称
        String goodsName = goods.getNxDgGoodsName();
        holder.tvGoodsName.setText(goodsName != null ? goodsName : "");

        // 设置规格信息
        String standardName = goods.getNxDgGoodsStandardname();
        String standardWeight = goods.getNxDgGoodsStandardWeight();
        if (standardName != null && !standardName.isEmpty() && !"null".equals(standardName)) {
            if (standardWeight != null && !standardWeight.isEmpty() && !"null".equals(standardWeight)) {
                holder.tvStandard.setText("(" + standardName + "/" + standardWeight + ")");
            } else {
                holder.tvStandard.setText("(" + standardName + ")");
            }
        } else {
            holder.tvStandard.setVisibility(View.GONE);
        }

        // 设置订单信息（使用与货架模式相同的简洁显示方式）
        List<NxDepartmentOrdersEntity> orders = goods.getNxDepartmentOrdersEntities();
        if (orders != null && !orders.isEmpty()) {
            holder.llOrders.setVisibility(View.VISIBLE);
            holder.llOrders.removeAllViews();
            
            for (NxDepartmentOrdersEntity order : orders) {
                View orderView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_stock_out_order_display, holder.llOrders, false);
                
                TextView departmentName = orderView.findViewById(R.id.tv_department_name);
                TextView orderQuantity = orderView.findViewById(R.id.tv_order_quantity);
                TextView remark = orderView.findViewById(R.id.tv_remark);

                // 设置客户名，只显示部门名称，不显示简称（与货架模式一致）
                String customerName = "";
                if (order != null) {
                    if (order.getNxDepartmentEntity() != null) {
                        NxDepartmentEntity department = order.getNxDepartmentEntity();
                        if (department.getFatherDepartmentEntity() != null) {
                            customerName = department.getFatherDepartmentEntity().getNxDepartmentName();
                        } else {
                            customerName = department.getNxDepartmentName();
                        }
                    } else if (order.getGbDepartmentEntity() != null) {
                        GbDepartmentEntity department = order.getGbDepartmentEntity();
                        if (department.getFatherGbDepartmentEntity() != null && 
                            department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                            customerName = department.getFatherGbDepartmentEntity().getGbDepartmentName();
                        } else {
                            customerName = department.getGbDepartmentName();
                        }
                    }
                }
                departmentName.setText(customerName);

                // 设置数量和单位，全部为绿色，不显示"订"字（与货架模式一致）
                String quantity = String.valueOf(order.getNxDoQuantity()) + (order.getNxDoStandard() == null ? "" : order.getNxDoStandard());
                android.text.SpannableString spannable = new android.text.SpannableString(quantity);
                spannable.setSpan(new android.text.style.ForegroundColorSpan(0xFF20B384), 0, quantity.length(), android.text.SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                orderQuantity.setText(spannable);

                // 隐藏备注（与货架模式一致）
                remark.setVisibility(View.GONE);
                
                holder.llOrders.addView(orderView);
            }
        } else {
            holder.llOrders.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(goods);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goodsList != null ? goodsList.size() : 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private TextView tvBrand;
        private TextView tvGoodsName;
        private TextView tvStandard;
        private LinearLayout llOrders;

        public InnerHolder(View itemView) {
            super(itemView);
            tvBrand = itemView.findViewById(R.id.tv_goods_brand);
            tvGoodsName = itemView.findViewById(R.id.tv_goods_name);
            tvStandard = itemView.findViewById(R.id.tv_goods_standard);
            llOrders = itemView.findViewById(R.id.ll_orders_container);
        }
    }
} 