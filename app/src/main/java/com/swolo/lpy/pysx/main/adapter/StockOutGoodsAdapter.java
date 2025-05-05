package com.swolo.lpy.pysx.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.dialog.StockOutGoodsDialog;

import java.util.ArrayList;
import java.util.List;

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
        this.goodsList = goodsList != null ? goodsList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_goods, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        if (goodsList == null || position >= goodsList.size()) {
            return;
        }

        NxDistributerGoodsShelfGoodsEntity goods = goodsList.get(position);
        if (goods.getNxDistributerGoodsEntity() != null) {
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
            if (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null) {
                for (NxDepartmentOrdersEntity order : goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities()) {
                    View orderView = createOrderView(holder.ordersContainer, order, goods);
                    holder.ordersContainer.addView(orderView);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            // 弹出弹窗
            StockOutGoodsDialog dialog = new StockOutGoodsDialog(v.getContext(), goods);
            dialog.setOnConfirmListener(updatedOrders -> {
                goods.getNxDistributerGoodsEntity().setNxDepartmentOrdersEntities(updatedOrders);
                notifyItemChanged(position);

                // 新增：通过回调把需要保存的订单传递给外部
                if (orderConfirmListener != null) {
                    // 过滤出需要保存的订单
                    List<NxDepartmentOrdersEntity> needSubmit = new ArrayList<>();
                    for (NxDepartmentOrdersEntity order : updatedOrders) {
                        String weight = order.getNxDoWeight();
                        if (weight != null && !weight.isEmpty() && Double.parseDouble(weight) > 0) {
                            needSubmit.add(order);
                        }
                    }
                    if (!needSubmit.isEmpty()) {
                        orderConfirmListener.onOrderConfirm(needSubmit);
                    }
                }
            });
            dialog.show();
        });
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

        public InnerHolder(View itemView) {
            super(itemView);
            goodsBrand = itemView.findViewById(R.id.tv_goods_brand);
            goodsName = itemView.findViewById(R.id.tv_goods_name);
            goodsStandard = itemView.findViewById(R.id.tv_goods_standard);
            ordersContainer = itemView.findViewById(R.id.ll_orders_container);
        }
    }
}

