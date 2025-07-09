package com.swolo.lpy.pysx.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import java.util.List;
import java.util.ArrayList;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import com.swolo.lpy.pysx.dialog.StockOutOrdersAdapter;

public class ManualInputDialog extends Dialog {
    private Context mContext;
    private NxDistributerGoodsShelfGoodsEntity goodsEntity;
    private List<NxDepartmentOrdersEntity> orderList;
    private int selectedOrderIndex = 0;
    private OnConfirmListener confirmListener;
    private RecyclerView rvOrders;
    private TextView tvGoodsName;
    private StockOutOrdersAdapter ordersAdapter;

    public ManualInputDialog(@NonNull Context context, NxDistributerGoodsShelfGoodsEntity goods, List<NxDepartmentOrdersEntity> orders) {
        super(context);
        this.mContext = context;
        this.goodsEntity = goods;
        this.orderList = orders;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_manual_input);
        // 【新增】弹窗显示时切换为横屏
        if (mContext instanceof Activity) {
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        tvGoodsName = findViewById(R.id.tv_goods_name);

        // 【新增】订单列表RecyclerView（参考蓝牙称弹窗）【2024-07-09】
        rvOrders = findViewById(R.id.rv_orders);
        android.util.Log.d("ManualInputDialog", "[订单列表] rvOrders: " + (rvOrders != null ? "找到" : "未找到"));
        android.util.Log.d("ManualInputDialog", "[订单列表] orderList: " + (orderList != null ? "不为空" : "为空"));
        android.util.Log.d("ManualInputDialog", "[订单列表] orderList大小: " + (orderList != null ? orderList.size() : 0));
        
        if (rvOrders != null && orderList != null && !orderList.isEmpty()) {
            rvOrders.setLayoutManager(new LinearLayoutManager(mContext));
            android.util.Log.d("ManualInputDialog", "[订单列表] LayoutManager设置完成");
            
            // 使用StockOutOrdersAdapter，和蓝牙称弹窗一致
            ordersAdapter = new StockOutOrdersAdapter((Activity) mContext, orderList, goodsEntity);
            android.util.Log.d("ManualInputDialog", "[订单列表] StockOutOrdersAdapter创建完成");
            

            
            rvOrders.setAdapter(ordersAdapter);
            android.util.Log.d("ManualInputDialog", "[订单列表] Adapter设置完成");
            
            // 【新增】监听订单点击，重置重量为空【2024-07-09】
            // 通过RecyclerView的点击事件来监听，不影响StockOutOrdersAdapter
            rvOrders.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    return false; // 不拦截，让子视图处理
                }

                @Override
                public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                    // 不处理触摸事件
                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                    // 不处理
                }
            });
            
            // 使用Handler延迟检查选中位置变化
            final Handler handler = new Handler();
            final Runnable checkSelectionRunnable = new Runnable() {
                private int lastSelectedPosition = 0;
                
                @Override
                public void run() {
                    int currentSelectedPosition = ordersAdapter.getSelectedPosition();
                    if (currentSelectedPosition != lastSelectedPosition) {
                        android.util.Log.d("ManualInputDialog", "[订单切换] 检测到选中位置变化: " + lastSelectedPosition + " -> " + currentSelectedPosition);
                        // 重置所有订单重量为空
                        for (NxDepartmentOrdersEntity order : orderList) {
                            order.setNxDoWeight("");
                        }
                        // 通知适配器更新显示
                        ordersAdapter.notifyDataSetChanged();
                        android.util.Log.d("ManualInputDialog", "[订单切换] 所有订单重量已重置为空");
                        lastSelectedPosition = currentSelectedPosition;
                    }
                    // 继续检查
                    handler.postDelayed(this, 100);
                }
            };
            handler.post(checkSelectionRunnable);
            
            rvOrders.setVisibility(View.VISIBLE);
            android.util.Log.d("ManualInputDialog", "[订单列表] RecyclerView设置为可见");
            
            android.util.Log.d("ManualInputDialog", "[订单列表] 初始化完成，订单数量: " + orderList.size());
            
            // 【新增】详细输出每个订单信息
            for (int i = 0; i < orderList.size(); i++) {
                NxDepartmentOrdersEntity order = orderList.get(i);
                android.util.Log.d("ManualInputDialog", "[订单列表] 订单" + i + ": ID=" + order.getNxDepartmentOrdersId() + 
                    ", 数量=" + order.getNxDoQuantity() + order.getNxDoStandard() + 
                    ", 重量=" + order.getNxDoWeight() + 
                    ", 备注=" + order.getNxDoRemark());
            }
        } else {
            android.util.Log.d("ManualInputDialog", "[订单列表] 没有订单数据");
            if (rvOrders != null) {
                rvOrders.setVisibility(View.GONE);
                android.util.Log.d("ManualInputDialog", "[订单列表] RecyclerView设置为不可见");
            }
        }

        // 【新增】数字键盘按钮事件绑定（手动输入核心功能）【2024-07-09】
        int[] numBtnIds = {R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_0, R.id.btn_dot};
        String[] numBtnValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "."};
        for (int i = 0; i < numBtnIds.length; i++) {
            Button btn = findViewById(numBtnIds[i]);
            String value = numBtnValues[i];
            btn.setOnClickListener(v -> appendToInput(value));
        }

        // 清除按钮
        Button btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> removeLastChar());

        // 确定按钮
        Button btnConfirm = findViewById(R.id.btn_confirm);
        android.util.Log.d("ManualInputDialog", "[按钮绑定] btnConfirm: " + (btnConfirm != null ? "找到" : "未找到"));
        btnConfirm.setOnClickListener(v -> {
            android.util.Log.d("ManualInputDialog", "[按钮点击] 确定按钮被点击");
            onConfirmClick();
        });

        // 取消按钮
        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dismiss());

        // 商品信息初始化
        if (goodsEntity != null) {
            tvGoodsName.setText(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName() + "(" + goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname() + ")");
        }
        // 【新增】设置弹窗为全屏
        if (getWindow() != null) {
            getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }
        // 【新增】布局日志，输出左右区域宽度和占比
        final View leftPanel = findViewById(R.id.left_panel);
        final View rightPanel = findViewById(R.id.right_panel);
        leftPanel.post(new Runnable() {
            @Override
            public void run() {
                int leftWidth = leftPanel.getWidth();
                int rightWidth = rightPanel.getWidth();
                int totalWidth = ((View) leftPanel.getParent()).getWidth();
                android.util.Log.d("ManualInputDialog", "[布局日志] leftPanel宽度: " + leftWidth + ", rightPanel宽度: " + rightWidth + ", 总宽度: " + totalWidth);
                if (totalWidth > 0) {
                    float leftPercent = (leftWidth * 100f) / totalWidth;
                    float rightPercent = (rightWidth * 100f) / totalWidth;
                    android.util.Log.d("ManualInputDialog", "[布局日志] leftPanel占比: " + leftPercent + "% , rightPanel占比: " + rightPercent + "%");
                }
                
                // 【新增】检查右侧面板内部布局
                View keyboardGrid = findViewById(R.id.keyboard_grid);
                if (keyboardGrid != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] keyboardGrid宽度: " + keyboardGrid.getWidth() + ", 高度: " + keyboardGrid.getHeight());
                }
                
                // 【新增】检查3个功能按钮
                Button btnClear = findViewById(R.id.btn_clear);
                Button btnConfirm = findViewById(R.id.btn_confirm);
                Button btnCancel = findViewById(R.id.btn_cancel);
                android.util.Log.d("ManualInputDialog", "[布局日志] btnClear: " + (btnClear != null ? "找到" : "未找到") + ", 宽度: " + (btnClear != null ? btnClear.getWidth() : "N/A"));
                android.util.Log.d("ManualInputDialog", "[布局日志] btnConfirm: " + (btnConfirm != null ? "找到" : "未找到") + ", 宽度: " + (btnConfirm != null ? btnConfirm.getWidth() : "N/A"));
                android.util.Log.d("ManualInputDialog", "[布局日志] btnCancel: " + (btnCancel != null ? "找到" : "未找到") + ", 宽度: " + (btnCancel != null ? btnCancel.getWidth() : "N/A"));
            }
        });
    }

    @Override
    public void dismiss() {
        // 【修改】弹窗关闭时保持横屏，不恢复竖屏【2024-07-09】
        // 注释掉恢复竖屏的代码，保持横屏状态
        // if (mContext instanceof Activity) {
        //     ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        // }
        super.dismiss();
    }





    // 【新增】数字键盘输入方法（针对当前选中订单）【2024-07-09】
    private void appendToInput(String value) {
        android.util.Log.d("ManualInputDialog", "[数字键盘] 开始输入: " + value);
        android.util.Log.d("ManualInputDialog", "[数字键盘] ordersAdapter: " + (ordersAdapter != null ? "不为空" : "为空"));
        
        if (ordersAdapter != null) {
            int selectedPosition = ordersAdapter.getSelectedPosition();
            android.util.Log.d("ManualInputDialog", "[数字键盘] 当前选中位置: " + selectedPosition);
            
            if (selectedPosition >= 0 && selectedPosition < orderList.size()) {
                NxDepartmentOrdersEntity currentOrder = orderList.get(selectedPosition);
                String currentWeight = currentOrder.getNxDoWeight() != null ? currentOrder.getNxDoWeight() : "";
                android.util.Log.d("ManualInputDialog", "[数字键盘] 当前重量: " + currentWeight);
                
                // 校验：只允许一个小数点
                if (value.equals(".")) {
                    if (currentWeight.contains(".")) {
                        android.util.Log.d("ManualInputDialog", "[数字键盘] 已存在小数点，忽略输入");
                        return;
                    }
                    if (currentWeight.isEmpty()) currentWeight = "0"; // 小数点前自动补0
                }
                // 校验：首位0后不能再输入0
                if (value.equals("0")) {
                    if (currentWeight.equals("0")) {
                        android.util.Log.d("ManualInputDialog", "[数字键盘] 首位0后不能再输入0，忽略");
                        return;
                    }
                }
                // 校验：首位不能是小数点
                if (currentWeight.isEmpty() && value.equals(".")) {
                    currentWeight = "0";
                }
                
                String newWeight;
                // 【新增】连续输入拼接逻辑【2024-07-09】
                if (currentWeight.isEmpty() || currentWeight.equals("0") || currentWeight.equals("0.00") || 
                    currentWeight.startsWith("0.")) {
                    // 如果当前重量为空、0或0.xx，直接替换
                    newWeight = value;
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 初始输入，直接替换为: " + newWeight);
                } else {
                    // 否则拼接到当前重量后面
                    newWeight = currentWeight + value;
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 连续输入，拼接为: " + newWeight);
                }
                currentOrder.setNxDoWeight(newWeight);
                android.util.Log.d("ManualInputDialog", "[数字键盘] 新重量: " + newWeight);
                
                // 更新适配器显示 - 直接通知适配器刷新，不使用updateWeightAtPosition避免单位转换
                ordersAdapter.notifyItemChanged(selectedPosition);
                android.util.Log.d("ManualInputDialog", "[数字键盘] 适配器更新成功");
                
                android.util.Log.d("ManualInputDialog", "[数字键盘] 输入完成: " + value + ", 订单" + selectedPosition + "重量: " + newWeight);
            } else {
                android.util.Log.d("ManualInputDialog", "[数字键盘] 选中位置无效: " + selectedPosition + ", 订单列表大小: " + orderList.size());
            }
        } else {
            android.util.Log.d("ManualInputDialog", "[数字键盘] ordersAdapter为空，无法输入");
        }
    }

    private void removeLastChar() {
        if (ordersAdapter != null) {
            int selectedPosition = ordersAdapter.getSelectedPosition();
            if (selectedPosition >= 0 && selectedPosition < orderList.size()) {
                NxDepartmentOrdersEntity currentOrder = orderList.get(selectedPosition);
                String currentWeight = currentOrder.getNxDoWeight() != null ? currentOrder.getNxDoWeight() : "";
                
                if (!currentWeight.isEmpty()) {
                    String newWeight = currentWeight.substring(0, currentWeight.length() - 1);
                    currentOrder.setNxDoWeight(newWeight);
                    
                    // 更新适配器显示 - 直接通知适配器刷新，不使用updateWeightAtPosition避免单位转换
                    ordersAdapter.notifyItemChanged(selectedPosition);
                    
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 清除一位, 订单" + selectedPosition + "重量: " + newWeight);
                }
            }
        }
    }

    private void onConfirmClick() {
        android.util.Log.d("ManualInputDialog", "[保存] ========== onConfirmClick开始 ==========");
        android.util.Log.d("ManualInputDialog", "[保存] ordersAdapter: " + (ordersAdapter != null ? "不为空" : "为空"));
        android.util.Log.d("ManualInputDialog", "[保存] orderList: " + (orderList != null ? "不为空" : "为空"));
        android.util.Log.d("ManualInputDialog", "[保存] orderList大小: " + (orderList != null ? orderList.size() : 0));
        
        // 【修改】参考蓝牙称弹窗逻辑：只提交选中的订单【2024-07-09】
        if (ordersAdapter != null && orderList != null && !orderList.isEmpty()) {
            // 获取当前选中的订单
            NxDepartmentOrdersEntity selectedOrder = ordersAdapter.getSelectedOrder();
            android.util.Log.d("ManualInputDialog", "[保存] 获取到选中订单: " + (selectedOrder != null ? "orderId=" + selectedOrder.getNxDepartmentOrdersId() : "null"));
            
            if (selectedOrder == null) {
                android.util.Log.d("ManualInputDialog", "[保存] 没有选中的订单，显示提示");
                android.widget.Toast.makeText(mContext, "请选择一个订单", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 验证选中订单的重量数据
            String weight = selectedOrder.getNxDoWeight();
            android.util.Log.d("ManualInputDialog", "[保存] 选中订单重量: " + weight);
            if (weight == null || weight.isEmpty() || weight.equals("0.00") || weight.equals("0")) {
                android.util.Log.d("ManualInputDialog", "[保存] 选中订单重量无效，显示提示");
                android.widget.Toast.makeText(mContext, "请为选中的订单输入重量", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 创建只包含选中订单的列表
            List<NxDepartmentOrdersEntity> selectedOrders = new ArrayList<>();
            selectedOrders.add(selectedOrder);
            
            android.util.Log.d("ManualInputDialog", "[保存] 准备处理选中订单: orderId=" + selectedOrder.getNxDepartmentOrdersId() + ", weight=" + weight);
            
            // 回调处理保存
            if (confirmListener != null) {
                android.util.Log.d("ManualInputDialog", "[保存] confirmListener不为空，调用回调");
                confirmListener.onConfirm(selectedOrders);
                
                // 【新增】保存成功后处理订单移除，参考蓝牙称弹窗逻辑【2024-07-09】
                handleOrderSavedSuccessfully(selectedOrder);
            } else {
                android.util.Log.d("ManualInputDialog", "[保存] confirmListener为空，无法回调");
            }
        } else {
            android.util.Log.d("ManualInputDialog", "[保存] 没有可处理的订单，显示提示");
            android.widget.Toast.makeText(mContext, "没有可处理的订单", android.widget.Toast.LENGTH_SHORT).show();
        }
        android.util.Log.d("ManualInputDialog", "[保存] ========== onConfirmClick结束 ==========");
    }

    /**
     * 处理订单保存成功后的逻辑，参考蓝牙称弹窗
     */
    private void handleOrderSavedSuccessfully(NxDepartmentOrdersEntity savedOrder) {
        android.util.Log.d("ManualInputDialog", "[订单保存成功] ========== 开始handleOrderSavedSuccessfully方法 ==========");
        android.util.Log.d("ManualInputDialog", "[订单保存成功] 处理已保存订单: orderId=" + savedOrder.getNxDepartmentOrdersId());
        
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(() -> {
                // 从订单列表中移除已保存的订单
                if (ordersAdapter != null) {
                    List<NxDepartmentOrdersEntity> currentOrders = ordersAdapter.getOrders();
                    android.util.Log.d("ManualInputDialog", "[订单保存成功] 当前订单列表大小: " + currentOrders.size());
                    
                    // 移除已保存的订单
                    currentOrders.removeIf(order -> order.getNxDepartmentOrdersId().equals(savedOrder.getNxDepartmentOrdersId()));
                    android.util.Log.d("ManualInputDialog", "[订单保存成功] 移除订单后列表大小: " + currentOrders.size());
                    
                    // 更新适配器
                    ordersAdapter.updateOrders(currentOrders);
                    
                    // 显示保存成功提示
                    android.widget.Toast.makeText(mContext, "订单保存成功", android.widget.Toast.LENGTH_SHORT).show();
                    
                    // 刷新页面数据
                    refreshPageData();
                    
                    // 检查是否还有订单
                    if (currentOrders.isEmpty()) {
                        android.util.Log.d("ManualInputDialog", "[订单保存成功] 没有剩余订单，关闭弹窗");
                        dismiss();
                    } else {
                        android.util.Log.d("ManualInputDialog", "[订单保存成功] 还有 " + currentOrders.size() + " 个订单，继续显示弹窗");
                        // 自动选择第一个订单
                        if (ordersAdapter != null) {
                            ordersAdapter.setSelectedPosition(0);
                        }
                    }
                }
            });
        }
        android.util.Log.d("ManualInputDialog", "[订单保存成功] ========== handleOrderSavedSuccessfully方法完成 ==========");
    }

    /**
     * 刷新页面数据
     */
    private void refreshPageData() {
        android.util.Log.d("ManualInputDialog", "[刷新页面] ========== 开始refreshPageData方法 ==========");
        
        if (mContext instanceof com.swolo.lpy.pysx.main.CustomerStockOutActivity) {
            android.util.Log.d("ManualInputDialog", "[刷新页面] 刷新CustomerStockOutActivity数据");
            com.swolo.lpy.pysx.main.CustomerStockOutActivity activity = (com.swolo.lpy.pysx.main.CustomerStockOutActivity) mContext;
            activity.refreshData();
        } else if (mContext instanceof com.swolo.lpy.pysx.main.StockOutActivity) {
            android.util.Log.d("ManualInputDialog", "[刷新页面] 刷新StockOutActivity数据");
            com.swolo.lpy.pysx.main.StockOutActivity activity = (com.swolo.lpy.pysx.main.StockOutActivity) mContext;
            // 假设StockOutActivity有refreshData方法，如果没有需要添加
            // activity.refreshData();
        }
        
        android.util.Log.d("ManualInputDialog", "[刷新页面] ========== refreshPageData方法完成 ==========");
    }

    public interface OnConfirmListener {
        void onConfirm(List<NxDepartmentOrdersEntity> orders);
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }

    // 内部订单Adapter（简化版，仅用于切换选中状态）
    private static class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
        private List<NxDepartmentOrdersEntity> orders;
        private int selectedIndex;
        private OnOrderClickListener listener;
        public OrdersAdapter(List<NxDepartmentOrdersEntity> orders, int selectedIndex, OnOrderClickListener listener) {
            this.orders = orders;
            this.selectedIndex = selectedIndex;
            this.listener = listener;
        }
        @Override
        public OrderViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new OrderViewHolder(view);
        }
        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            holder.textView.setText("订单" + (position + 1));
            holder.textView.setBackgroundColor(position == selectedIndex ? 0xFFB2DFDB : 0x00000000);
            holder.textView.setOnClickListener(v -> {
                if (listener != null) listener.onOrderClick(position);
            });
        }
        @Override
        public int getItemCount() {
            return orders != null ? orders.size() : 0;
        }
        public void setSelectedIndex(int index) {
            this.selectedIndex = index;
            notifyDataSetChanged();
        }
        static class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            public OrderViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
        interface OnOrderClickListener {
            void onOrderClick(int position);
        }
    }
} 