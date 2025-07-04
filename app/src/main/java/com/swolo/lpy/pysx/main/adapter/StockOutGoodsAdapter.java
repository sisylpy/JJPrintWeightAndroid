package com.swolo.lpy.pysx.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.util.Log;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.dialog.StockOutGoodsDialog;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class StockOutGoodsAdapter extends RecyclerView.Adapter<StockOutGoodsAdapter.InnerHolder> {
    private List<NxDistributerGoodsShelfGoodsEntity> goodsList = new ArrayList<>();
    private OnItemClickListener mListener;

    // 新增：订单确认回调接口
    public interface OnOrderConfirmListener {
        void onOrderConfirm(List<NxDepartmentOrdersEntity> orderList);
    }

    private OnOrderConfirmListener orderConfirmListener;

    public void setOnOrderConfirmListener(OnOrderConfirmListener listener) {
        this.orderConfirmListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(NxDistributerGoodsShelfGoodsEntity entity);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setGoodsList(List<NxDistributerGoodsShelfGoodsEntity> goodsList) {
        Log.d("StockOutGoodsAdapter", "设置商品列表，数量: " + (goodsList != null ? goodsList.size() : 0));
        if (goodsList != null) {
            for (NxDistributerGoodsShelfGoodsEntity goods : goodsList) {
                Log.d("StockOutGoodsAdapter", "商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName() + 
                      ", 订单数量: " + (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
                      goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : 0));
            }
        }
        this.goodsList = goodsList != null ? goodsList : new ArrayList<>();
        notifyDataSetChanged();
    }

    private int currentSelectedPosition = -1;

    public void updateCurrentItemWeight(double weight) {
        if (currentSelectedPosition >= 0 && currentSelectedPosition < goodsList.size()) {
            NxDistributerGoodsShelfGoodsEntity item = goodsList.get(currentSelectedPosition);
            item.setNxDoWeight(weight);
            notifyItemChanged(currentSelectedPosition);
        }
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_goods, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        NxDistributerGoodsShelfGoodsEntity goods = goodsList.get(position);
        Log.d("StockOutGoodsAdapter", "绑定商品视图，位置: " + position + 
              ", 商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName() + 
              ", 订单数量: " + (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
              goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : 0));
        
        // 新增详细订单日志
        List<NxDepartmentOrdersEntity> ordersDebug = goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        if (ordersDebug == null || ordersDebug.isEmpty()) {
            Log.d("StockOutGoodsAdapter", "【调试】该商品没有订单数据");
        } else {
            for (int i = 0; i < ordersDebug.size(); i++) {
                NxDepartmentOrdersEntity order = ordersDebug.get(i);
                String depName = "";
                if (order.getNxDepartmentEntity() != null) {
                    depName = order.getNxDepartmentEntity().getNxDepartmentName();
                } else if (order.getGbDepartmentEntity() != null) {
                    depName = order.getGbDepartmentEntity().getGbDepartmentName();
                }
                Log.d("StockOutGoodsAdapter", String.format("【调试】订单%d: 部门=%s, 订货=%s%s, 出库=%s, 备注=%s", i,
                        depName,
                        String.valueOf(order.getNxDoQuantity()),
                        String.valueOf(order.getNxDoStandard()),
                        String.valueOf(order.getNxDoWeight()),
                        String.valueOf(order.getNxDoRemark())));
            }
        }

        // 设置品牌
        String brand = goods.getNxDistributerGoodsEntity().getNxDgGoodsBrand();
        if (brand != null && !brand.equals("null") && !brand.isEmpty()) {
            holder.goodsBrand.setVisibility(View.VISIBLE);
            holder.goodsBrand.setText(brand);
        } else {
            holder.goodsBrand.setVisibility(View.GONE);
        }

        // 设置商品名称
        holder.goodsName.setText(goods.getNxDistributerGoodsEntity().getNxDgGoodsName());

        // 设置规格信息
        String standardWeight = goods.getNxDistributerGoodsEntity().getNxDgGoodsStandardWeight();
        String standardName = goods.getNxDistributerGoodsEntity().getNxDgGoodsStandardname();
        if (standardWeight != null && !standardWeight.equals("null") && !standardWeight.isEmpty()) {
            if (!standardName.equals("斤")) {
                holder.goodsStandard.setText(String.format("(%s/%s)", standardName, standardWeight));
            }
        } else if (standardName != null && !standardName.equals("斤")) {
            holder.goodsStandard.setText(String.format("(%s)", standardName));
        }

        // 清除之前的订单视图
        holder.ordersContainer.removeAllViews();

        // 添加订单信息
        List<NxDepartmentOrdersEntity> orders = goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        if (orders != null && !orders.isEmpty()) {
            Log.d("StockOutGoodsAdapter", "添加订单信息，订单数量: " + orders.size());
            for (NxDepartmentOrdersEntity order : orders) {
                Log.d("StockOutGoodsAdapter", "订单详情: " +
                    "部门=" + (order.getNxDepartmentEntity() != null ? 
                        order.getNxDepartmentEntity().getNxDepartmentName() : "null") +
                    ", 数量=" + order.getNxDoQuantity() +
                    ", 单位=" + order.getNxDoStandard());
                View orderView = createOrderView(holder.ordersContainer, order, goods);
                holder.ordersContainer.addView(orderView);
            }
        } else {
            Log.d("StockOutGoodsAdapter", "没有订单数据");
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            Log.d("StockOutGoodsAdapter", "点击商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName() + 
                  ", 订单数量: " + (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
                  goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : 0));
            if (mListener != null) {
                mListener.onItemClick(goods);
            }
        });

        // 设置蓝牙秤按钮点击事件
        holder.btnScale.setOnClickListener(v -> {
            if (scaleButtonClickListener != null) {
                scaleButtonClickListener.onScaleButtonClick(goods);
            }
        });

        // 设置选中状态
        holder.itemView.setSelected(position == currentSelectedPosition);
    }

    private View createOrderView(ViewGroup parent, NxDepartmentOrdersEntity order, NxDistributerGoodsShelfGoodsEntity goods) {
        View orderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_order_display, parent, false);
        TextView departmentName = orderView.findViewById(R.id.tv_department_name);
        TextView orderQuantity = orderView.findViewById(R.id.tv_order_quantity);
        TextView remark = orderView.findViewById(R.id.tv_remark);

        // 设置部门名称
        if (order != null) {
            if (order.getNxDepartmentEntity() != null) {
                NxDepartmentEntity department = order.getNxDepartmentEntity();
                if (department.getFatherDepartmentEntity() != null) {
                    departmentName.setText(String.format("(%s)%s.%s",
                            department.getFatherDepartmentEntity().getNxDepartmentPickName(),
                            department.getFatherDepartmentEntity().getNxDepartmentName(),
                            department.getNxDepartmentName()));
                } else {
                    departmentName.setText(String.format("(%s)%s",
                            department.getNxDepartmentPickName(),
                            department.getNxDepartmentName()));
                }
            } else if (order.getGbDepartmentEntity() != null) {
                GbDepartmentEntity department = order.getGbDepartmentEntity();
                if (department.getFatherGbDepartmentEntity() != null && 
                    department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                        departmentName.setText(String.format("(%s)%s.%s",
                            department.getFatherGbDepartmentEntity().getGbDepartmentAttrName(),
                            department.getFatherGbDepartmentEntity().getGbDepartmentName(),
                            department.getGbDepartmentName()));
                } else {
                    departmentName.setText(String.format("(%s)%s",
                            department.getGbDepartmentAttrName(),
                            department.getGbDepartmentName()));
                }
            }
        } else {
            departmentName.setText("");
        }

        // 设置订单数量和出库数量
        String orderText = String.format("订货: %s%s",
                order.getNxDoQuantity(),
                order.getNxDoStandard());

        // 添加出库数量显示
        if (order.getNxDoWeight() != null && !order.getNxDoWeight().isEmpty()) {
            orderText += String.format("  出库: %s%s",
                    order.getNxDoWeight(),
                    goods.getNxDistributerGoodsEntity().getNxDgGoodsStandardname());
        }

        orderQuantity.setText(orderText);

        // 设置备注
        if (order.getNxDoRemark() != null && !order.getNxDoRemark().isEmpty()) {
            remark.setVisibility(View.VISIBLE);
            remark.setText(order.getNxDoRemark());
        } else {
            remark.setVisibility(View.GONE);
        }

        return orderView;
    }

    @Override
    public int getItemCount() {
        return goodsList != null ? goodsList.size() : 0;
    }

    public static class InnerHolder extends RecyclerView.ViewHolder {
        TextView goodsBrand;
        TextView goodsName;
        TextView goodsStandard;
        LinearLayout ordersContainer;
        ImageButton btnScale;

        public InnerHolder(View itemView) {
            super(itemView);
            goodsBrand = itemView.findViewById(R.id.tv_goods_brand);
            goodsName = itemView.findViewById(R.id.tv_goods_name);
            goodsStandard = itemView.findViewById(R.id.tv_goods_standard);
            ordersContainer = itemView.findViewById(R.id.ll_orders_container);
            btnScale = itemView.findViewById(R.id.btn_scale);
        }
    }

    // 新增：蓝牙秤按钮点击回调接口
    public interface OnScaleButtonClickListener {
        void onScaleButtonClick(NxDistributerGoodsShelfGoodsEntity entity);
    }

    private OnScaleButtonClickListener scaleButtonClickListener;

    public void setOnScaleButtonClickListener(OnScaleButtonClickListener listener) {
        this.scaleButtonClickListener = listener;
    }
}

