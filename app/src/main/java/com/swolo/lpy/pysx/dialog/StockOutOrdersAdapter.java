package com.swolo.lpy.pysx.dialog;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.ScaleActivity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class StockOutOrdersAdapter extends RecyclerView.Adapter<StockOutOrdersAdapter.InnerHolder> {
    private static final int REQUEST_SCALE_ACTIVITY = 1003;
    private List<NxDepartmentOrdersEntity> ordersList;
    private NxDistributerGoodsShelfGoodsEntity goods;
    private Activity activity;
    private int currentPosition = -1;
    private int selectedPosition = 0; // 当前选中的订单位置，默认选中第一个

    public StockOutOrdersAdapter(Activity activity, List<NxDepartmentOrdersEntity> ordersList, NxDistributerGoodsShelfGoodsEntity goods) {
        this.activity = activity;
        this.ordersList = ordersList;
        this.goods = goods;
        this.selectedPosition = 0; // 默认选中第一个订单
        Log.d("StockOutOrdersAdapter", "[适配器] 构造函数: ordersList hash=" + (ordersList != null ? ordersList.hashCode() : 0) + ", size=" + (ordersList != null ? ordersList.size() : 0));
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_out_order, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        NxDepartmentOrdersEntity order = ordersList.get(position);
        Log.d("StockOutOrdersAdapter", "[适配器] ========== onBindViewHolder开始 ==========");
        Log.d("StockOutOrdersAdapter", "[适配器] onBindViewHolder: position=" + position + ", orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null") + ", weight=" + (order != null ? order.getNxDoWeight() : "null"));
        Log.d("StockOutOrdersAdapter", "[适配器] 当前状态: selectedPosition=" + selectedPosition + ", currentPosition=" + currentPosition);
        
        // 设置部门名称 - 只显示部门名称，不显示简称
        String departmentName = "";
        if (order.getNxDepartmentEntity() != null) {
            NxDepartmentEntity department = order.getNxDepartmentEntity();
            if (department.getFatherDepartmentEntity() != null) {
                departmentName = String.format("%s.%s",
                    department.getFatherDepartmentEntity().getNxDepartmentName(),
                    department.getNxDepartmentName());
            } else {
                departmentName = department.getNxDepartmentName();
            }
            Log.d("StockOutOrdersAdapter", "内销部门名称: " + departmentName);
        } else if (order.getGbDepartmentEntity() != null) {
            GbDepartmentEntity department = order.getGbDepartmentEntity();
            if (department.getFatherGbDepartmentEntity() != null && 
                department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                departmentName = String.format("%s.%s",
                    department.getFatherGbDepartmentEntity().getGbDepartmentName(),
                    department.getGbDepartmentName());
            } else {
                departmentName = department.getGbDepartmentName();
            }
            Log.d("StockOutOrdersAdapter", "国标部门名称: " + departmentName);
        }
        holder.departmentName.setText(departmentName);

        // 设置订货数量 - 只显示数量和单位，不显示"订单"
        String orderQuantity = String.format("%s%s", 
            order.getNxDoQuantity(), 
            order.getNxDoStandard());
        Log.d("StockOutOrdersAdapter", "订货数量: " + orderQuantity);
        holder.orderQuantity.setText(orderQuantity);

        // 设置备注
        if (order.getNxDoRemark() != null && !order.getNxDoRemark().isEmpty()) {
            String remark = "备注: " + order.getNxDoRemark();
            Log.d("StockOutOrdersAdapter", "备注: " + remark);
            holder.remark.setVisibility(View.VISIBLE);
            holder.remark.setText(remark);
        } else {
            Log.d("StockOutOrdersAdapter", "无备注");
            holder.remark.setVisibility(View.GONE);
        }

        // 先移除旧的TextWatcher，防止setText时触发afterTextChanged
        if (holder.outQuantity.getTag() instanceof TextWatcher) {
            holder.outQuantity.removeTextChangedListener((TextWatcher) holder.outQuantity.getTag());
        }
        
        // 设置出库数量 - 修复初始重量显示问题
        String weightStr = "";
        if (order != null && order.getNxDoWeight() != null && !order.getNxDoWeight().isEmpty()) {
            weightStr = order.getNxDoWeight();
        } else {
            // 如果订单没有重量数据，设置为空而不是0
            weightStr = "";
        }
        Log.d("StockOutOrdersAdapter", "[适配器] setText前: position=" + position + ", setText=" + weightStr + ", 订单原重量=" + (order != null ? order.getNxDoWeight() : "null"));
        holder.outQuantity.setText(weightStr);
        Log.d("StockOutOrdersAdapter", "[适配器] setText后: position=" + position + ", EditText内容=" + holder.outQuantity.getText().toString());
        
        // 新建TextWatcher
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;
                
                String weight = s.toString();
                Log.d("StockOutOrdersAdapter", "[适配器] afterTextChanged: position=" + currentPosition + ", s=" + weight);
                // 只有和原内容不一样才写回去，防止setText死循环和内容被覆盖
                if (currentPosition >= 0 && currentPosition < ordersList.size()) {
                    NxDepartmentOrdersEntity currentOrder = ordersList.get(currentPosition);
                    if (currentOrder != null && !weight.equals(currentOrder.getNxDoWeight())) {
                        Log.d("StockOutOrdersAdapter", "[适配器] afterTextChanged: 赋值新重量, position=" + currentPosition + ", newWeight=" + weight);
                        currentOrder.setNxDoWeight(weight);
                    }
                }
            }
        };
        holder.outQuantity.addTextChangedListener(watcher);
        holder.outQuantity.setTag(watcher);
        
        // 设置选中状态显示
        if (position == selectedPosition) {
            // 选中的订单：淡绿色背景，黑色文字，数量绿色，输入框绿色边框
            holder.itemView.setBackgroundResource(R.drawable.order_item_selected_bg);
            holder.departmentName.setTextColor(Color.parseColor("#222222"));
            holder.orderQuantity.setTextColor(Color.parseColor("#3fc1ab"));
            holder.outQuantity.setTextColor(Color.parseColor("#222222"));
            holder.unit.setTextColor(Color.parseColor("#3fc1ab"));
            if (holder.remark.getVisibility() == View.VISIBLE) {
                holder.remark.setTextColor(Color.parseColor("#222222"));
            }
            holder.selectedIndicator.setVisibility(View.GONE); // 不再显示蓝色指示条
        } else {
            // 未选中的订单：白色背景，黑色文字，数量灰色，输入框灰色边框
            holder.itemView.setBackgroundResource(R.drawable.order_item_bg);
            holder.departmentName.setTextColor(Color.parseColor("#222222"));
            holder.orderQuantity.setTextColor(Color.parseColor("#d6d6d6"));
            holder.outQuantity.setTextColor(Color.parseColor("#222222"));
            holder.unit.setTextColor(Color.parseColor("#d6d6d6"));
            if (holder.remark.getVisibility() == View.VISIBLE) {
                holder.remark.setTextColor(Color.parseColor("#222222"));
            }
            holder.selectedIndicator.setVisibility(View.GONE);
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    Log.d("StockOutOrdersAdapter", "[适配器] 订单" + clickedPosition + "被点击");
                    setSelectedPosition(clickedPosition);
                }
            }
        });
        
        // 设置输入框焦点事件
        holder.outQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int focusedPosition = holder.getAdapterPosition();
                    if (focusedPosition != RecyclerView.NO_POSITION) {
                        Log.d("StockOutOrdersAdapter", "[适配器] 订单" + focusedPosition + "获得焦点");
                        setSelectedPosition(focusedPosition);
        }
                }
            }
        });
        
        Log.d("StockOutOrdersAdapter", "[适配器] ========== onBindViewHolder结束，位置: " + position + " ==========");
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public List<NxDepartmentOrdersEntity> getUpdatedOrders() {
        return ordersList;
    }
    
    public List<NxDepartmentOrdersEntity> getOrders() {
        return ordersList;
    }

    /**
     * 更新订单列表
     */
    public void updateOrders(List<NxDepartmentOrdersEntity> newOrders) {
        Log.d("StockOutOrdersAdapter", "[适配器] ========== updateOrders开始 ==========");
        Log.d("StockOutOrdersAdapter", "[适配器] 原订单列表大小: " + (ordersList != null ? ordersList.size() : 0));
        Log.d("StockOutOrdersAdapter", "[适配器] 新订单列表大小: " + (newOrders != null ? newOrders.size() : 0));
        
        this.ordersList = newOrders;
        
        // 调整选中位置，确保不越界
        if (selectedPosition >= ordersList.size()) {
            selectedPosition = ordersList.size() > 0 ? 0 : -1;
            currentPosition = selectedPosition;
            Log.d("StockOutOrdersAdapter", "[适配器] 选中位置越界，调整为: " + selectedPosition);
        }
        
        // 刷新UI
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Log.d("StockOutOrdersAdapter", "[适配器] 主线程中刷新UI");
                notifyDataSetChanged();
                Log.d("StockOutOrdersAdapter", "[适配器] UI刷新完成");
            });
        } else {
            Log.d("StockOutOrdersAdapter", "[适配器] activity为空，直接刷新UI");
            notifyDataSetChanged();
        }
        
        Log.d("StockOutOrdersAdapter", "[适配器] ========== updateOrders结束 ==========");
    }

    public void updateWeight(double weight) {
        Log.d("StockOutOrdersAdapter", "updateWeight: weight=" + weight + ", selectedPosition=" + selectedPosition);
        if (selectedPosition >= 0 && selectedPosition < ordersList.size()) {
            NxDepartmentOrdersEntity order = ordersList.get(selectedPosition);
            double weightInJin = weight / 500.0;  // 将克转换为斤
            order.setNxDoWeight(String.format("%.2f", weightInJin)); // 去掉"斤"单位，只保留数字
            Log.d("StockOutOrdersAdapter", "updateWeight: 赋值给订单, position=" + selectedPosition + ", weight=" + weightInJin);
            notifyItemChanged(selectedPosition);
        } else {
            Log.d("StockOutOrdersAdapter", "updateWeight: selectedPosition无效, selectedPosition=" + selectedPosition);
        }
    }

    public void updateWeightAtPosition(int position, double weight) {
        Log.d("StockOutOrdersAdapter", "[适配器] ========== updateWeightAtPosition开始 ==========");
        Log.d("StockOutOrdersAdapter", "[适配器] 参数检查: position=" + position + ", weight=" + weight + ", ordersList.size=" + (ordersList != null ? ordersList.size() : 0));
        
        if (position >= 0 && position < ordersList.size()) {
            Log.d("StockOutOrdersAdapter", "[适配器] 位置有效，开始更新订单");
            NxDepartmentOrdersEntity order = ordersList.get(position);
            Log.d("StockOutOrdersAdapter", "[适配器] 获取到订单: orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null"));
            
            if (order != null) {
                String oldWeight = order.getNxDoWeight();
                Log.d("StockOutOrdersAdapter", "[适配器] 订单原重量: " + oldWeight);
                
                double weightInJin = weight / 500.0;  // 将克转换为斤
                String newWeight = String.format("%.2f", weightInJin); // 去掉"斤"单位，只保留数字
                Log.d("StockOutOrdersAdapter", "[适配器] 重量转换: " + weight + "g -> " + weightInJin + " -> " + newWeight);
                
                Log.d("StockOutOrdersAdapter", "[适配器] 开始设置订单新重量");
                order.setNxDoWeight(newWeight);
                Log.d("StockOutOrdersAdapter", "[适配器] 订单重量设置完成: " + newWeight);
                
                // 使用notifyItemChanged而不是notifyDataSetChanged，避免重新绑定所有视图
                if (activity != null) {
                    Log.d("StockOutOrdersAdapter", "[适配器] activity不为空，在主线程中刷新UI");
                    activity.runOnUiThread(() -> {
                        Log.d("StockOutOrdersAdapter", "[适配器] 主线程中调用notifyItemChanged");
                        notifyItemChanged(position);
                        Log.d("StockOutOrdersAdapter", "[适配器] UI刷新完成，position=" + position);
                    });
                } else {
                    Log.d("StockOutOrdersAdapter", "[适配器] activity为空，直接刷新UI");
                    notifyItemChanged(position);
                    Log.d("StockOutOrdersAdapter", "[适配器] UI刷新完成，position=" + position);
                }
            } else {
                Log.e("StockOutOrdersAdapter", "[适配器] 订单为空，无法更新重量");
            }
        } else {
            Log.e("StockOutOrdersAdapter", "[适配器] 位置无效: position=" + position + ", listSize=" + (ordersList != null ? ordersList.size() : 0));
        }
        Log.d("StockOutOrdersAdapter", "[适配器] ========== updateWeightAtPosition结束 ==========");
    }

    /**
     * 设置选中的订单位置
     */
    public void setSelectedPosition(int position) {
        Log.d("StockOutOrdersAdapter", "[适配器] ========== setSelectedPosition开始 ==========");
        Log.d("StockOutOrdersAdapter", "[适配器] 参数检查: position=" + position + ", ordersList.size=" + (ordersList != null ? ordersList.size() : 0));
        
        if (position >= 0 && position < ordersList.size()) {
            int oldSelectedPosition = selectedPosition;
            selectedPosition = position;
            currentPosition = position; // 同时更新当前位置，用于蓝牙秤重量更新
            
            Log.d("StockOutOrdersAdapter", "[适配器] 选中位置变更: " + oldSelectedPosition + " -> " + selectedPosition);
            Log.d("StockOutOrdersAdapter", "[适配器] currentPosition也更新为: " + currentPosition);
            
            // 清空新选中订单的重量
            if (oldSelectedPosition != selectedPosition) {
                NxDepartmentOrdersEntity newSelectedOrder = ordersList.get(selectedPosition);
                if (newSelectedOrder != null) {
                    String oldWeight = newSelectedOrder.getNxDoWeight();
                    Log.d("StockOutOrdersAdapter", "[适配器] 清空新选中订单重量: position=" + selectedPosition + ", 原重量=" + oldWeight);
                    newSelectedOrder.setNxDoWeight("");
                    Log.d("StockOutOrdersAdapter", "[适配器] 重量已清空");
                }
            }
            
            // 刷新UI显示
            if (activity != null) {
                Log.d("StockOutOrdersAdapter", "[适配器] activity不为空，在主线程中刷新UI");
                activity.runOnUiThread(() -> {
                    Log.d("StockOutOrdersAdapter", "[适配器] 主线程中刷新UI开始");
                    // 刷新之前选中的项目
                    if (oldSelectedPosition >= 0 && oldSelectedPosition < ordersList.size()) {
                        Log.d("StockOutOrdersAdapter", "[适配器] 刷新之前选中的项目: " + oldSelectedPosition);
                        notifyItemChanged(oldSelectedPosition);
                    }
                    // 刷新新选中的项目
                    Log.d("StockOutOrdersAdapter", "[适配器] 刷新新选中的项目: " + selectedPosition);
                    notifyItemChanged(selectedPosition);
                    Log.d("StockOutOrdersAdapter", "[适配器] 主线程中刷新UI完成");
                });
            } else {
                Log.d("StockOutOrdersAdapter", "[适配器] activity为空，直接刷新UI");
                if (oldSelectedPosition >= 0 && oldSelectedPosition < ordersList.size()) {
                    notifyItemChanged(oldSelectedPosition);
                }
                notifyItemChanged(selectedPosition);
            }
        } else {
            Log.e("StockOutOrdersAdapter", "[适配器] 位置无效: position=" + position + ", listSize=" + (ordersList != null ? ordersList.size() : 0));
        }
        Log.d("StockOutOrdersAdapter", "[适配器] ========== setSelectedPosition结束 ==========");
    }

    /**
     * 获取当前选中的订单
     */
    public NxDepartmentOrdersEntity getSelectedOrder() {
        if (selectedPosition >= 0 && selectedPosition < ordersList.size()) {
            return ordersList.get(selectedPosition);
        }
        return null;
    }

    /**
     * 获取当前选中的位置
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private TextView departmentName;
        private TextView orderQuantity;
        private EditText outQuantity;
        private TextView unit;
        private TextView remark;
        private ImageButton btnScale;
        private LinearLayout selectedIndicator;

        public InnerHolder(View view) {
            super(view);
            departmentName = view.findViewById(R.id.tv_department_name);
            orderQuantity = view.findViewById(R.id.tv_order_quantity);
            outQuantity = view.findViewById(R.id.et_out_quantity);
            unit = view.findViewById(R.id.tv_unit);
            remark = view.findViewById(R.id.tv_remark);
            btnScale = view.findViewById(R.id.btn_scale);
            selectedIndicator = view.findViewById(R.id.selected_indicator);
        }
    }
} 