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

    public StockOutOrdersAdapter(Activity activity, List<NxDepartmentOrdersEntity> ordersList, NxDistributerGoodsShelfGoodsEntity goods) {
        this.activity = activity;
        this.ordersList = ordersList;
        this.goods = goods;
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
        Log.d("StockOutOrdersAdapter", "[适配器] onBindViewHolder: position=" + position + ", orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null") + ", weight=" + (order != null ? order.getNxDoWeight() : "null"));
        
        // 设置部门名称
        String departmentName = "";
            if (order.getNxDepartmentEntity() != null) {
                NxDepartmentEntity department = order.getNxDepartmentEntity();
                if (department.getFatherDepartmentEntity() != null) {
                departmentName = String.format("(%s)%s.%s",
                            department.getFatherDepartmentEntity().getNxDepartmentPickName(),
                            department.getFatherDepartmentEntity().getNxDepartmentName(),
                    department.getNxDepartmentName());
                } else {
                departmentName = String.format("(%s)%s",
                            department.getNxDepartmentPickName(),
                    department.getNxDepartmentName());
                }
            Log.d("StockOutOrdersAdapter", "内销部门名称: " + departmentName);
            } else if (order.getGbDepartmentEntity() != null) {
                GbDepartmentEntity department = order.getGbDepartmentEntity();
                if (department.getFatherGbDepartmentEntity() != null && 
                    department.getFatherGbDepartmentEntity().getGbDepartmentSubAmount() > 1) {
                departmentName = String.format("(%s)%s.%s",
                            department.getFatherGbDepartmentEntity().getGbDepartmentAttrName(),
                            department.getFatherGbDepartmentEntity().getGbDepartmentName(),
                    department.getGbDepartmentName());
                } else {
                departmentName = String.format("(%s)%s",
                            department.getGbDepartmentAttrName(),
                    department.getGbDepartmentName());
            }
            Log.d("StockOutOrdersAdapter", "国标部门名称: " + departmentName);
        }
        holder.departmentName.setText(departmentName);

        // 设置订货数量
        String orderQuantity = String.format("订货: %s%s", 
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
        // 设置出库数量
        String weightStr = order != null && order.getNxDoWeight() != null ? order.getNxDoWeight() : "";
        Log.d("StockOutOrdersAdapter", "[适配器] setText前: position=" + position + ", setText=" + weightStr);
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
        
        // --- 新增：第一条订单输入框高亮、选中、蓝色、光标 ---
        if (position == 0) {
            holder.outQuantity.setTextColor(Color.BLUE);
            holder.outQuantity.requestFocus();
            holder.outQuantity.setSelection(holder.outQuantity.getText().length());
        } else {
            holder.outQuantity.setTextColor(Color.BLACK);
        }
        
        Log.d("StockOutOrdersAdapter", "视图绑定完成，位置: " + position);
    }

    @Override
    public int getItemCount() {
        return ordersList != null ? ordersList.size() : 0;
    }

    public List<NxDepartmentOrdersEntity> getUpdatedOrders() {
        return ordersList;
    }

    public void updateWeight(double weight) {
        Log.d("StockOutOrdersAdapter", "updateWeight: weight=" + weight + ", currentPosition=" + currentPosition);
        if (currentPosition >= 0 && currentPosition < ordersList.size()) {
            NxDepartmentOrdersEntity order = ordersList.get(currentPosition);
            double weightInJin = weight / 500.0;  // 将克转换为斤
            order.setNxDoWeight(String.format("%.2f", weightInJin) + "斤");
            Log.d("StockOutOrdersAdapter", "updateWeight: 赋值给订单, position=" + currentPosition + ", weight=" + weightInJin + "斤");
            notifyItemChanged(currentPosition);
        } else {
            Log.d("StockOutOrdersAdapter", "updateWeight: currentPosition无效, currentPosition=" + currentPosition);
        }
    }

    public void updateWeightAtPosition(int position, double weight) {
        Log.d("StockOutOrdersAdapter", "[适配器] updateWeightAtPosition: position=" + position + ", weight=" + weight);
        if (position >= 0 && position < ordersList.size()) {
            NxDepartmentOrdersEntity order = ordersList.get(position);
            double weightInJin = weight / 500.0;  // 将克转换为斤
            Log.d("StockOutOrdersAdapter", "[适配器] 更新订单重量: orderId=" + (order != null ? order.getNxDepartmentOrdersId() : "null") + ", oldWeight=" + (order != null ? order.getNxDoWeight() : "null") + ", newWeight=" + weightInJin + "斤");
            order.setNxDoWeight(String.format("%.2f", weightInJin) + "斤");
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    notifyDataSetChanged();
                    Log.d("StockOutOrdersAdapter", "[适配器] 重量更新完成，已主线程通知UI刷新(全量), position=" + position);
                });
            } else {
                notifyDataSetChanged();
                Log.d("StockOutOrdersAdapter", "[适配器] 重量更新完成，已通知UI刷新(全量), position=" + position);
            }
        } else {
            Log.e("StockOutOrdersAdapter", "[适配器] 位置无效: position=" + position + ", listSize=" + ordersList.size());
        }
    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        private TextView departmentName;
        private TextView orderQuantity;
        private EditText outQuantity;
        private TextView unit;
        private TextView remark;
        private ImageButton btnScale;

        public InnerHolder(View view) {
            super(view);
            departmentName = view.findViewById(R.id.tv_department_name);
            orderQuantity = view.findViewById(R.id.tv_order_quantity);
            outQuantity = view.findViewById(R.id.et_out_quantity);
            unit = view.findViewById(R.id.tv_unit);
            remark = view.findViewById(R.id.tv_remark);
            btnScale = view.findViewById(R.id.btn_scale);
        }
    }
} 