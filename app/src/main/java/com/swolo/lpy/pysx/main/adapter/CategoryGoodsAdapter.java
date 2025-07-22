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

        // 设置订单信息
        List<NxDepartmentOrdersEntity> orders = goods.getNxDepartmentOrdersEntities();
        if (orders != null && !orders.isEmpty()) {
            holder.llOrders.setVisibility(View.VISIBLE);
            holder.llOrders.removeAllViews();
            
            for (NxDepartmentOrdersEntity order : orders) {
                View orderView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_order_info, holder.llOrders, false);
                
                TextView tvDepartment = orderView.findViewById(R.id.tv_department);
                TextView tvQuantity = orderView.findViewById(R.id.tv_quantity);
                TextView tvWeight = orderView.findViewById(R.id.tv_weight);
                TextView tvRemark = orderView.findViewById(R.id.tv_remark);
                
                // 设置部门信息
                String departmentName = "";
                if (order.getNxDepartmentEntity() != null) {
                    NxDepartmentEntity nxDep = order.getNxDepartmentEntity();
                    if (nxDep.getFatherDepartmentEntity() != null) {
                        departmentName = nxDep.getFatherDepartmentEntity().getNxDepartmentAttrName() + "." + nxDep.getNxDepartmentName();
                    } else {
                        departmentName = nxDep.getNxDepartmentName();
                    }
                } else if (order.getGbDepartmentEntity() != null) {
                    GbDepartmentEntity gbDep = order.getGbDepartmentEntity();
                    if (gbDep.getFatherGbDepartmentEntity() != null) {
                        departmentName = gbDep.getFatherGbDepartmentEntity().getGbDepartmentName() + "." + gbDep.getGbDepartmentName();
                    } else {
                        departmentName = gbDep.getGbDepartmentName();
                    }
                }
                tvDepartment.setText(departmentName);
                
                // 设置订货信息
                String quantity = "订: " + (order.getNxDoQuantity() != null ? order.getNxDoQuantity() : 0) + 
                               (order.getNxDoStandard() != null ? order.getNxDoStandard() : "");
                tvQuantity.setText(quantity);
                
                // 设置出库信息
                if (order.getNxDoWeight() != null) {
                    String weight = "出: " + order.getNxDoWeight() + 
                                  (goods.getNxDgGoodsStandardname() != null ? goods.getNxDgGoodsStandardname() : "");
                    tvWeight.setText(weight);
                } else {
                    tvWeight.setText("出: ");
                    tvWeight.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
                }
                
                // 设置备注信息
                if (order.getNxDoRemark() != null && !order.getNxDoRemark().isEmpty()) {
                    tvRemark.setVisibility(View.VISIBLE);
                    tvRemark.setText("备注: " + order.getNxDoRemark());
                } else {
                    tvRemark.setVisibility(View.GONE);
                }
                
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