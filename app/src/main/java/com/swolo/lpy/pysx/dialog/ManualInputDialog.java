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
import android.content.SharedPreferences;
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

    // ManualInputDialog类成员变量
    private Integer currentGoodsShelfId;

    public ManualInputDialog(@NonNull Context context, NxDistributerGoodsShelfGoodsEntity goods, List<NxDepartmentOrdersEntity> orders) {
        super(context);
        this.mContext = context;
        this.goodsEntity = goods;
        this.orderList = orders;
        this.currentGoodsShelfId = goods != null ? goods.getNxDgsgShelfId() : null;
        android.util.Log.d("ManualInputDialog", "[构造] goodsEntity: " + goods);
        if (goods != null) {
            android.util.Log.d("ManualInputDialog", "[构造] nxDgsgShelfId: " + goods.getNxDgsgShelfId()
                + ", nxDgsgDisGoodsId: " + goods.getNxDgsgDisGoodsId()
                + ", 商品名: " + (goods.getNxDistributerGoodsEntity() != null ? goods.getNxDistributerGoodsEntity().getNxDgGoodsName() : "null"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 【修改】根据用户设置选择布局文件
        int layoutResId = getLayoutBasedOnOrientation();
        setContentView(layoutResId);
        
        // 【修改】应用用户的屏幕方向设置
        applyScreenOrientation();
        tvGoodsName = findViewById(R.id.tv_goods_name);
        
        // 【新增】调试数字键盘高度
        android.util.Log.d("ManualInputDialog", "[调试] 开始检查数字键盘高度");
        View keyboardContainer = findViewById(R.id.keyboard_container);
        View keyboardGrid = findViewById(R.id.keyboard_grid);
        View buttonsContainer = findViewById(R.id.buttons_container);
        View btnClear = findViewById(R.id.btn_clear);
        View btnConfirm = findViewById(R.id.btn_confirm);
        
        if (keyboardContainer != null) {
            android.util.Log.d("ManualInputDialog", "[调试] keyboard_container高度: " + keyboardContainer.getLayoutParams().height);
        }
        if (keyboardGrid != null) {
            android.util.Log.d("ManualInputDialog", "[调试] keyboard_grid高度: " + keyboardGrid.getLayoutParams().height);
        }
        if (buttonsContainer != null) {
            android.util.Log.d("ManualInputDialog", "[调试] buttons_container高度: " + buttonsContainer.getLayoutParams().height);
        }
        if (btnClear != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_clear高度: " + btnClear.getLayoutParams().height);
        }
        if (btnConfirm != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_confirm高度: " + btnConfirm.getLayoutParams().height);
        }
        
        // 【新增】延迟检查实际高度
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (keyboardContainer != null) {
                    android.util.Log.d("ManualInputDialog", "[调试] keyboard_container实际高度: " + keyboardContainer.getHeight());
                }
                if (keyboardGrid != null) {
                    android.util.Log.d("ManualInputDialog", "[调试] keyboard_grid实际高度: " + keyboardGrid.getHeight());
                }
                if (buttonsContainer != null) {
                    android.util.Log.d("ManualInputDialog", "[调试] buttons_container实际高度: " + buttonsContainer.getHeight());
                }
                if (btnClear != null) {
                    android.util.Log.d("ManualInputDialog", "[调试] btn_clear实际高度: " + btnClear.getHeight());
                }
                        if (btnConfirm != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_confirm实际高度: " + btnConfirm.getHeight());
        }
        
        // 【新增】检查数字键盘按钮高度
        Button btn1 = findViewById(R.id.btn_1);
        Button btn2 = findViewById(R.id.btn_2);
        Button btn3 = findViewById(R.id.btn_3);
        Button btn4 = findViewById(R.id.btn_4);
        Button btn5 = findViewById(R.id.btn_5);
        Button btn6 = findViewById(R.id.btn_6);
        Button btn7 = findViewById(R.id.btn_7);
        Button btn8 = findViewById(R.id.btn_8);
        Button btn9 = findViewById(R.id.btn_9);
        Button btn0 = findViewById(R.id.btn_0);
        
        if (btn1 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_1实际宽度: " + btn1.getWidth() + ", 实际高度: " + btn1.getHeight());
        }
        if (btn2 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_2实际宽度: " + btn2.getWidth() + ", 实际高度: " + btn2.getHeight());
        }
        if (btn3 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_3实际宽度: " + btn3.getWidth() + ", 实际高度: " + btn3.getHeight());
        }
        if (btn4 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_4实际宽度: " + btn4.getWidth() + ", 实际高度: " + btn4.getHeight());
        }
        if (btn5 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_5实际宽度: " + btn5.getWidth() + ", 实际高度: " + btn5.getHeight());
        }
        if (btn6 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_6实际宽度: " + btn6.getWidth() + ", 实际高度: " + btn6.getHeight());
        }
        if (btn7 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_7实际宽度: " + btn7.getWidth() + ", 实际高度: " + btn7.getHeight());
        }
        if (btn8 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_8实际宽度: " + btn8.getWidth() + ", 实际高度: " + btn8.getHeight());
        }
        if (btn9 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_9实际宽度: " + btn9.getWidth() + ", 实际高度: " + btn9.getHeight());
        }
        if (btn0 != null) {
            android.util.Log.d("ManualInputDialog", "[调试] btn_0实际宽度: " + btn0.getWidth() + ", 实际高度: " + btn0.getHeight());
        }
            }
        }, 1000);

        // 【新增】订单列表RecyclerView（参考蓝牙称弹窗）【2024-07-09】
        rvOrders = findViewById(R.id.rv_orders);
        android.util.Log.d("ManualInputDialog", "[订单列表] rvOrders: " + (rvOrders != null ? "找到" : "未找到"));
        android.util.Log.d("ManualInputDialog", "[订单列表] orderList: " + (orderList != null ? "不为空" : "为空"));
        android.util.Log.d("ManualInputDialog", "[订单列表] orderList大小: " + (orderList != null ? orderList.size() : 0));
        
        // 【新增】弹窗显示时，将所有订单的称重重量设置为空【2024-07-10】
        if (orderList != null && !orderList.isEmpty()) {
            for (NxDepartmentOrdersEntity order : orderList) {
                order.setNxDoWeight("");
            }
            android.util.Log.d("ManualInputDialog", "[订单列表] 所有订单重量已重置为空");
        }
        
        if (rvOrders != null && orderList != null && !orderList.isEmpty()) {
            rvOrders.setLayoutManager(new LinearLayoutManager(mContext));
            android.util.Log.d("ManualInputDialog", "[订单列表] LayoutManager设置完成");
            
            // 使用StockOutOrdersAdapter，和蓝牙称弹窗一致
            ordersAdapter = new StockOutOrdersAdapter((Activity) mContext, orderList, goodsEntity, rvOrders);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ordersAdapter != null) {
                        NxDepartmentOrdersEntity selectedOrder = ordersAdapter.getSelectedOrder();
                        if (selectedOrder != null) {
                            android.util.Log.d("ManualInputDialog", "[订单列表] 初始选中订单: orderId=" + selectedOrder.getNxDepartmentOrdersId());
                        }
                    }
                }
            }, 100);
            
            // 【新增】输出所有订单详情【2024-07-09】
            android.util.Log.d("ManualInputDialog", "[订单列表] 所有订单详情:");
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
        Button btnClearMain = findViewById(R.id.btn_clear);
        if (btnClearMain != null) {
            btnClearMain.setOnClickListener(v -> removeLastChar());
            
            // 添加删除按钮的详细调试信息
            btnClearMain.post(new Runnable() {
                @Override
                public void run() {
                    int btnWidth = btnClearMain.getWidth();
                    int btnHeight = btnClearMain.getHeight();
                    int btnX = btnClearMain.getLeft();
                    int btnY = btnClearMain.getTop();
                    android.util.Log.d("ManualInputDialog", "[删除按钮调试] 宽度: " + btnWidth + ", 高度: " + btnHeight + ", X: " + btnX + ", Y: " + btnY);
                    
                    // 检查父容器
                    View parent = (View) btnClearMain.getParent();
                    if (parent != null) {
                        android.util.Log.d("ManualInputDialog", "[删除按钮调试] 父容器: " + parent.getClass().getSimpleName() + 
                            ", 宽度: " + parent.getWidth() + ", 高度: " + parent.getHeight());
                    }
                }
            });
        }

        // 确定按钮
        Button btnConfirmMain = findViewById(R.id.btn_confirm);
        android.util.Log.d("ManualInputDialog", "[按钮绑定] btnConfirm: " + (btnConfirmMain != null ? "找到" : "未找到"));
        if (btnConfirmMain != null) {
            btnConfirmMain.setOnClickListener(v -> {
                android.util.Log.d("ManualInputDialog", "[按钮点击] 确定按钮被点击");
                onConfirmClick();
            });
            
            // 添加保存按钮的详细调试信息
            btnConfirmMain.post(new Runnable() {
                @Override
                public void run() {
                    int btnWidth = btnConfirmMain.getWidth();
                    int btnHeight = btnConfirmMain.getHeight();
                    int btnX = btnConfirmMain.getLeft();
                    int btnY = btnConfirmMain.getTop();
                    android.util.Log.d("ManualInputDialog", "[保存按钮调试] 宽度: " + btnWidth + ", 高度: " + btnHeight + ", X: " + btnX + ", Y: " + btnY);
                    
                    // 检查父容器
                    View parent = (View) btnConfirmMain.getParent();
                    if (parent != null) {
                        android.util.Log.d("ManualInputDialog", "[保存按钮调试] 父容器: " + parent.getClass().getSimpleName() + 
                            ", 宽度: " + parent.getWidth() + ", 高度: " + parent.getHeight());
                    }
                }
            });
        }

        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                android.util.Log.d("ManualInputDialog", "[返回按钮] 用户点击返回，关闭弹窗");
                dismiss();
            });
            android.util.Log.d("ManualInputDialog", "[返回按钮] 返回按钮绑定成功");
        } else {
            android.util.Log.d("ManualInputDialog", "[返回按钮] 返回按钮不存在（横屏布局）");
        }



        // 商品信息初始化
        if (goodsEntity != null) {
            tvGoodsName.setText(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName() + "(" + goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname() + ")");
        }
        // 【新增】设置弹窗为全屏，去掉所有边距和背景
        if (getWindow() != null) {
            getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
            getWindow().setDimAmount(0f); // 去掉蒙版
            getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 去掉背景
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
                               android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // 允许内容超出状态栏
        }
        // 【新增】详细布局日志，输出所有关键组件的尺寸和位置
        final View leftPanel = findViewById(R.id.left_panel);
        final View rightPanel = findViewById(R.id.right_panel);
        
        // 添加空值检查，避免崩溃
        if (leftPanel != null && rightPanel != null) {
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
                View keyboardContainer = findViewById(R.id.keyboard_container);
                View buttonsContainer = findViewById(R.id.buttons_container);
                
                if (keyboardGrid != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] keyboardGrid宽度: " + keyboardGrid.getWidth() + ", 高度: " + keyboardGrid.getHeight());
                    android.util.Log.d("ManualInputDialog", "[布局日志] keyboardGrid位置: x=" + keyboardGrid.getX() + ", y=" + keyboardGrid.getY());
                }
                
                if (keyboardContainer != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] keyboardContainer宽度: " + keyboardContainer.getWidth() + ", 高度: " + keyboardContainer.getHeight());
                    android.util.Log.d("ManualInputDialog", "[布局日志] keyboardContainer位置: x=" + keyboardContainer.getX() + ", y=" + keyboardContainer.getY());
                }
                
                if (buttonsContainer != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] buttonsContainer宽度: " + buttonsContainer.getWidth() + ", 高度: " + buttonsContainer.getHeight());
                    android.util.Log.d("ManualInputDialog", "[布局日志] buttonsContainer位置: x=" + buttonsContainer.getX() + ", y=" + buttonsContainer.getY());
                }
                
                // 【新增】检查2个功能按钮
                Button btnClearDebug = findViewById(R.id.btn_clear);
                Button btnConfirmDebug = findViewById(R.id.btn_confirm);
                android.util.Log.d("ManualInputDialog", "[布局日志] btnClear: " + (btnClearDebug != null ? "找到" : "未找到") + ", 宽度: " + (btnClearDebug != null ? btnClearDebug.getWidth() : "N/A") + ", 高度: " + (btnClearDebug != null ? btnClearDebug.getHeight() : "N/A"));
                android.util.Log.d("ManualInputDialog", "[布局日志] btnConfirm: " + (btnConfirmDebug != null ? "找到" : "未找到") + ", 宽度: " + (btnConfirmDebug != null ? btnConfirmDebug.getWidth() : "N/A") + ", 高度: " + (btnConfirmDebug != null ? btnConfirmDebug.getHeight() : "N/A"));
                
                if (btnClearDebug != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] btnClear位置: x=" + btnClearDebug.getX() + ", y=" + btnClearDebug.getY());
                }
                if (btnConfirmDebug != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] btnConfirm位置: x=" + btnConfirmDebug.getX() + ", y=" + btnConfirmDebug.getY());
                }
                
                // 【新增】检查数字键盘按钮
                Button btn3 = findViewById(R.id.btn_3);
                Button btn6 = findViewById(R.id.btn_6);
                Button btn9 = findViewById(R.id.btn_9);
                Button btnDot = findViewById(R.id.btn_dot);
                
                if (btn3 != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] btn3位置: x=" + btn3.getX() + ", y=" + btn3.getY() + ", 宽度: " + btn3.getWidth());
                }
                if (btn6 != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] btn6位置: x=" + btn6.getX() + ", y=" + btn6.getY() + ", 宽度: " + btn6.getWidth());
                }
                if (btn9 != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] btn9位置: x=" + btn9.getX() + ", y=" + btn9.getY() + ", 宽度: " + btn9.getWidth());
                }
                if (btnDot != null) {
                    android.util.Log.d("ManualInputDialog", "[布局日志] btnDot位置: x=" + btnDot.getX() + ", y=" + btnDot.getY() + ", 宽度: " + btnDot.getWidth());
                }
            }
        });
        } else {
            android.util.Log.d("ManualInputDialog", "[布局日志] leftPanel或rightPanel为空，跳过布局日志");
        }
    }



    @Override
    public void dismiss() {
        // 【修改】弹窗关闭时应用用户的屏幕方向设置
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            // 恢复为自动模式，让系统根据用户设置决定方向
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            android.util.Log.d("ManualInputDialog", "[弹窗关闭] 恢复为自动屏幕方向");
        }
        
        android.util.Log.d("ManualInputDialog", "[弹窗关闭] ManualInputDialog dismiss");
        super.dismiss();
    }

    // 【新增】数字键盘输入方法（针对当前选中订单）【2024-07-09】
    private void appendToInput(String value) {
        android.util.Log.d("ManualInputDialog", "[数字键盘] 开始输入: " + value);
        android.util.Log.d("ManualInputDialog", "[数字键盘] ordersAdapter: " + (ordersAdapter != null ? "不为空" : "为空"));
        
        if (ordersAdapter != null) {
            NxDepartmentOrdersEntity selectedOrder = ordersAdapter.getSelectedOrder();
            android.util.Log.d("ManualInputDialog", "[数字键盘] 当前选中订单: " + (selectedOrder != null ? "orderId=" + selectedOrder.getNxDepartmentOrdersId() : "null"));
            
            if (selectedOrder != null) {
                String currentWeight = selectedOrder.getNxDoWeight();
                android.util.Log.d("ManualInputDialog", "[数字键盘] 当前重量: " + currentWeight);
                
                // 如果重量为空或null，初始化为空字符串
                if (currentWeight == null) {
                    currentWeight = "";
                }
                
                // 检查是否已经有小数点
                if (value.equals(".") && currentWeight.contains(".")) {
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 已有小数点，忽略输入");
                    return;
                }
                
                // 检查长度限制（最多5位数字）
                if (currentWeight.length() >= 5 && !value.equals(".")) {
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 已达到最大长度限制");
                    return;
                }
                
                // 拼接新值
                String newWeight = currentWeight + value;
                android.util.Log.d("ManualInputDialog", "[数字键盘] 新重量: " + newWeight);
                
                // 更新订单重量
                selectedOrder.setNxDoWeight(newWeight);
                
                // 通知适配器更新UI - 使用更精确的更新方式
                int selectedPosition = ordersAdapter.getSelectedPosition();
                if (selectedPosition >= 0) {
                    ordersAdapter.notifyItemChanged(selectedPosition);
                }
                
                android.util.Log.d("ManualInputDialog", "[数字键盘] 重量更新完成");
            } else {
                android.util.Log.d("ManualInputDialog", "[数字键盘] 没有选中的订单");
            }
        } else {
            android.util.Log.d("ManualInputDialog", "[数字键盘] ordersAdapter为空");
        }
    }

    // 【新增】删除最后一个字符【2024-07-09】
    private void removeLastChar() {
        android.util.Log.d("ManualInputDialog", "[数字键盘] 开始删除最后一个字符");
        
        if (ordersAdapter != null) {
            NxDepartmentOrdersEntity selectedOrder = ordersAdapter.getSelectedOrder();
            
            if (selectedOrder != null) {
                String currentWeight = selectedOrder.getNxDoWeight();
                android.util.Log.d("ManualInputDialog", "[数字键盘] 当前重量: " + currentWeight);
                
                if (currentWeight != null && !currentWeight.isEmpty()) {
                    // 删除最后一个字符
                    String newWeight = currentWeight.substring(0, currentWeight.length() - 1);
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 删除后重量: " + newWeight);
                    
                    // 更新订单重量
                    selectedOrder.setNxDoWeight(newWeight);
                    
                    // 通知适配器更新UI - 使用更精确的更新方式
                    int selectedPosition = ordersAdapter.getSelectedPosition();
                    if (selectedPosition >= 0) {
                        ordersAdapter.notifyItemChanged(selectedPosition);
                    }
                    
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 删除完成");
                } else {
                    android.util.Log.d("ManualInputDialog", "[数字键盘] 当前重量为空，无需删除");
                }
            } else {
                android.util.Log.d("ManualInputDialog", "[数字键盘] 没有选中的订单");
            }
        } else {
            android.util.Log.d("ManualInputDialog", "[数字键盘] ordersAdapter为空");
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
            List<NxDepartmentOrdersEntity> ordersToSubmit = new ArrayList<>();
            ordersToSubmit.add(selectedOrder);
            
            android.util.Log.d("ManualInputDialog", "[保存] 准备提交订单数量: " + ordersToSubmit.size());
            
            // 调用确认监听器
            if (confirmListener != null) {
                android.util.Log.d("ManualInputDialog", "[保存] 调用confirmListener.onConfirm");
                confirmListener.onConfirm(ordersToSubmit);
            } else {
                android.util.Log.d("ManualInputDialog", "[保存] confirmListener为空");
            }
            
            // 【修改】参考StockOutGoodsDialog逻辑：处理订单保存后的逻辑
            handleOrderSavedSuccessfully(selectedOrder);
            
            android.util.Log.d("ManualInputDialog", "[保存] ========== onConfirmClick结束 ==========");
        } else {
            android.util.Log.d("ManualInputDialog", "[保存] 没有订单数据");
            android.widget.Toast.makeText(mContext, "没有订单数据", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }

    /**
     * 处理订单保存成功后的逻辑
     */
    private void handleOrderSavedSuccessfully(NxDepartmentOrdersEntity savedOrder) {
        android.util.Log.d("ManualInputDialog", "[订单保存成功] ========== 开始handleOrderSavedSuccessfully方法 ==========");
        android.util.Log.d("ManualInputDialog", "[订单保存成功] 处理已保存订单: orderId=" + savedOrder.getNxDepartmentOrdersId());
        
        if (mContext instanceof android.app.Activity) {
            ((android.app.Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 从订单列表中移除已保存的订单
                    if (ordersAdapter != null && orderList != null) {
                        android.util.Log.d("ManualInputDialog", "[订单保存成功] 当前订单列表大小: " + orderList.size());
                        
                        // 移除已保存的订单
                        java.util.Iterator<NxDepartmentOrdersEntity> iterator = orderList.iterator();
                        while (iterator.hasNext()) {
                            NxDepartmentOrdersEntity order = iterator.next();
                            if (order.getNxDepartmentOrdersId().equals(savedOrder.getNxDepartmentOrdersId())) {
                                iterator.remove();
                                break;
                            }
                        }
                        android.util.Log.d("ManualInputDialog", "[订单保存成功] 移除订单后列表大小: " + orderList.size());
                        
                        // 更新适配器
                        ordersAdapter.updateOrders(orderList);
                        
                        // 显示保存成功提示
                        android.widget.Toast.makeText(mContext, "订单保存成功", android.widget.Toast.LENGTH_SHORT).show();
                        
                        // 检查是否还有订单
                        if (orderList.isEmpty()) {
                            android.util.Log.d("ManualInputDialog", "[订单保存成功] 没有剩余订单，关闭弹窗");
                            dismiss();
                            
                            // 延迟刷新页面数据，避免窗口泄漏
                            new android.os.Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    refreshPageData();
                                }
                            }, 500); // 延迟500ms
                        } else {
                            android.util.Log.d("ManualInputDialog", "[订单保存成功] 还有 " + orderList.size() + " 个订单，继续显示弹窗");
                            // 自动选择第一个订单
                            if (ordersAdapter != null) {
                                ordersAdapter.setSelectedPosition(0);
                            }
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

    /**
     * 【新增】根据用户设置选择布局文件
     */
    private int getLayoutBasedOnOrientation() {
        android.util.Log.d("ManualInputDialog", "[布局选择] 开始选择布局文件");
        
        try {
            // 读取横竖屏设置
            SharedPreferences prefs = mContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            int screenOrientation = prefs.getInt("screen_orientation", 0); // 默认竖屏
            
            android.util.Log.d("ManualInputDialog", "[布局选择] 读取到横竖屏设置: " + screenOrientation);
            
            // 新的索引映射：0=竖屏，1=横屏
            if (screenOrientation >= 2) {
                screenOrientation = 0; // 如果索引超出范围，默认使用竖屏
                android.util.Log.w("ManualInputDialog", "[布局选择] 设置值超出范围，重置为竖屏");
            }
            
            switch (screenOrientation) {
                case 0: // 竖屏
                    android.util.Log.d("ManualInputDialog", "[布局选择] 竖屏模式，使用竖屏布局");
                    return R.layout.dialog_manual_input_portrait;
                case 1: // 横屏
                    android.util.Log.d("ManualInputDialog", "[布局选择] 横屏模式，使用横屏布局");
                    return R.layout.dialog_manual_input;
                default:
                    android.util.Log.w("ManualInputDialog", "[布局选择] 未知设置，使用竖屏布局");
                    return R.layout.dialog_manual_input_portrait;
            }
        } catch (Exception e) {
            android.util.Log.e("ManualInputDialog", "[布局选择] 选择布局文件失败", e);
            return R.layout.dialog_manual_input_portrait; // 默认使用竖屏布局
        }
    }

    /**
     * 【新增】应用横竖屏设置
     */
    private void applyScreenOrientation() {
        android.util.Log.d("ManualInputDialog", "[弹窗] applyScreenOrientation: 开始应用横竖屏设置");
        
        try {
            // 读取横竖屏设置
            SharedPreferences prefs = mContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            int screenOrientation = prefs.getInt("screen_orientation", 0); // 默认竖屏
            
            android.util.Log.d("ManualInputDialog", "[弹窗] applyScreenOrientation: 读取到横竖屏设置: " + screenOrientation);
            
            // 新的索引映射：0=竖屏，1=横屏
            if (screenOrientation >= 2) {
                screenOrientation = 0; // 如果索引超出范围，默认使用竖屏
                android.util.Log.w("ManualInputDialog", "[弹窗] applyScreenOrientation: 设置值超出范围，重置为竖屏");
            }
            
            // 获取Activity
            Activity activity = null;
            if (mContext instanceof Activity) {
                activity = (Activity) mContext;
            }
            
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                switch (screenOrientation) {
                    case 0: // 竖屏
                        android.util.Log.d("ManualInputDialog", "[弹窗] applyScreenOrientation: 设置为竖屏模式");
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case 1: // 横屏
                        android.util.Log.d("ManualInputDialog", "[弹窗] applyScreenOrientation: 设置为横屏模式");
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    default:
                        android.util.Log.w("ManualInputDialog", "[弹窗] applyScreenOrientation: 未知的横竖屏设置: " + screenOrientation + "，使用竖屏");
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                }
            } else {
                android.util.Log.e("ManualInputDialog", "[弹窗] applyScreenOrientation: 无法获取Activity实例或Activity已销毁");
            }
        } catch (Exception e) {
            android.util.Log.e("ManualInputDialog", "[弹窗] applyScreenOrientation: 应用横竖屏设置失败", e);
        }
        
        android.util.Log.d("ManualInputDialog", "[弹窗] applyScreenOrientation: 横竖屏设置应用完成");
    }

    public interface OnConfirmListener {
        void onConfirm(List<NxDepartmentOrdersEntity> orders);
    }
} 