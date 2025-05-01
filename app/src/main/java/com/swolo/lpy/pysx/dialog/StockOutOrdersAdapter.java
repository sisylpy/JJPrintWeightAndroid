package com.swolo.lpy.pysx.dialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;

import java.util.List;

public class StockOutOrdersAdapter extends RecyclerView.Adapter<StockOutOrdersAdapter.InnerHolder> {
    private List<NxDepartmentOrdersEntity> ordersList;
    private NxDistributerGoodsShelfGoodsEntity goods;

    public StockOutOrdersAdapter(List<NxDepartmentOrdersEntity> ordersList, NxDistributerGoodsShelfGoodsEntity goods) {
        this.ordersList = ordersList;
        this.goods = goods;
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_order, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        NxDepartmentOrdersEntity order = ordersList.get(position);
        
        // 设置部门名称
        if (order != null && order.getNxDepartmentEntity() != null) {
            NxDepartmentEntity department = order.getNxDepartmentEntity();
            if (department.getFatherDepartmentEntity() != null) {
                holder.departmentName.setText(String.format("(%s)%s.%s",
                        department.getFatherDepartmentEntity().getNxDepartmentAttrName(),
                        department.getFatherDepartmentEntity().getNxDepartmentName(),
                        department.getNxDepartmentName()));
            } else {
                holder.departmentName.setText(String.format("(%s)%s",
                        department.getNxDepartmentAttrName(),
                        department.getNxDepartmentName()));
            }
        } else {
            holder.departmentName.setText("");
        }

        // 设置订单数量
        holder.orderQuantity.setText(String.format("订货: %s%s", 
            order.getNxDoQuantity(), 
            order.getNxDoStandard()));

        // 设置备注
        if (order.getNxDoRemark() != null && !order.getNxDoRemark().isEmpty()) {
            holder.remark.setVisibility(View.VISIBLE);
            holder.remark.setText(order.getNxDoRemark());
        } else {
            holder.remark.setVisibility(View.GONE);
        }

        // 设置出库数量
        if (order.getNxDoWeight() != null) {
            holder.outQuantity.setText(order.getNxDoWeight());
        }

        // 设置单位
        holder.unit.setText(goods.getNxDistributerGoodsEntity().getNxDgGoodsStandardname());

        // 监听出库数量输入
        holder.outQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String input = s.toString().trim();
                    if (!input.isEmpty()) {
                        // 检查输入是否是有效的数字格式
                        if (input.matches("^[0-9]*(\\.[0-9]*)?$")) {
                            double value = Double.parseDouble(input);
                            order.setNxDoWeight(String.valueOf(value));
                            holder.outQuantity.setError(null);
                        } else {
                            holder.outQuantity.setError("请输入有效数字");
                            order.setNxDoWeight(null);
                        }
                    } else {
                        order.setNxDoWeight(null);
                        holder.outQuantity.setError(null);
                    }
                } catch (NumberFormatException e) {
                    order.setNxDoWeight(null);
                    holder.outQuantity.setError("数字格式错误");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public List<NxDepartmentOrdersEntity> getUpdatedOrders() {
        return ordersList;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private TextView departmentName;
        private TextView orderQuantity;
        private EditText outQuantity;
        private TextView unit;
        private TextView remark;

        public InnerHolder(View view) {
            super(view);
            departmentName = view.findViewById(R.id.tv_department_name);
            orderQuantity = view.findViewById(R.id.tv_order_quantity);
            outQuantity = view.findViewById(R.id.et_out_quantity);
            unit = view.findViewById(R.id.tv_unit);
            remark = view.findViewById(R.id.tv_remark);
        }
    }
} 