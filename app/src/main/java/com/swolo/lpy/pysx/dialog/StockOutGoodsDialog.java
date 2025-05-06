package com.swolo.lpy.pysx.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;

import java.util.List;

import android.view.ViewGroup;

public class StockOutGoodsDialog extends Dialog {
    private Context mContext;
    private NxDistributerGoodsShelfGoodsEntity goodsEntity;
    private OnConfirmListener confirmListener;
    private RecyclerView ordersRecyclerView;
    private StockOutOrdersAdapter ordersAdapter;
    private TextView scrollHint;

    public StockOutGoodsDialog(Context context, NxDistributerGoodsShelfGoodsEntity goods) {
        super(context, R.style.Theme_dialog);
        this.mContext = context;
        this.goodsEntity = goods;
    }

    // @Override
    // protected void onCreate(Bundle savedInstanceState) {
    //     super.onCreate(savedInstanceState);
    //     setContentView(R.layout.dialog_stock_out_goods);

    //     // 设置弹窗圆角背景和半透明遮罩
    //     Window window = getWindow();
    //     if (window != null) {
    //         window.setBackgroundDrawableResource(android.R.color.transparent);
    //         WindowManager.LayoutParams lp = window.getAttributes();
    //         lp.dimAmount = 0.6f;
    //         // 设置弹窗宽度为屏幕宽度的90%
    //         lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
    //         // 设置弹窗高度为自适应内容，但不超过屏幕高度的80%
    //         lp.height = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.8);
    //         // 设置弹窗位置居中
    //         lp.gravity = android.view.Gravity.CENTER;
    //         window.setAttributes(lp);
    //         window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    //         window.setWindowAnimations(R.style.DialogAnimation);
    //     }

    //     LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //     View view = inflater.inflate(R.layout.dialog_stock_out_goods, null);
    //     this.setCanceledOnTouchOutside(true);
    //     setContentView(view);

    //     // 设置商品标题
    //     TextView titleView = view.findViewById(R.id.tv_goods_title);
    //     if (goodsEntity != null && goodsEntity.getNxDistributerGoodsEntity() != null) {
    //         StringBuilder title = new StringBuilder();
    //         if (goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand() != null) {
    //             title.append(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand()).append(" ");
    //         }
    //         title.append(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName());
    //         String standardWeight = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardWeight();
    //         String standardName = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname();
    //         if (standardWeight != null && !standardWeight.equals("null")) {
    //             title.append("(").append(standardWeight).append("/").append(standardName).append(")");
    //         } else {
    //             title.append("(").append(standardName).append(")");
    //         }
    //         titleView.setText(title.toString());
    //     }

    //     // 设置订单列表
    //     ordersRecyclerView = view.findViewById(R.id.rv_orders);
    //     ordersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    //     // 添加分割线
    //     ordersRecyclerView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(mContext, android.support.v7.widget.DividerItemDecoration.VERTICAL));
        
    //     // 获取滚动提示文本视图
    //     scrollHint = view.findViewById(R.id.tv_scroll_hint);
        
    //     // 添加滚动监听
    //     ordersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
    //         @Override
    //         public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    //             super.onScrollStateChanged(recyclerView, newState);
    //             // 当停止滚动时，隐藏键盘
    //             if (newState == RecyclerView.SCROLL_STATE_IDLE) {
    //                 InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //                 imm.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
    //             }
    //         }

    //         @Override
    //         public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    //             super.onScrolled(recyclerView, dx, dy);
    //             // 控制滚动提示的显示和隐藏
    //             if (scrollHint != null) {
    //                 LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    //                 if (layoutManager != null) {
    //                     int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
    //                     scrollHint.setVisibility(firstVisibleItem > 0 ? View.VISIBLE : View.GONE);
    //                 }
    //             }
    //         }
    //     });
        
    //     // 获取订单列表
    //     List<NxDepartmentOrdersEntity> ordersList = goodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
    //     ordersAdapter = new StockOutOrdersAdapter(ordersList, goodsEntity);
    //     ordersRecyclerView.setAdapter(ordersAdapter);

    //     // 设置按钮点击事件
    //     view.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
    //         InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //         imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    //         dismiss();
    //     });
        
    //     view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
    //         InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //         imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    //         if (confirmListener != null) {
    //             confirmListener.onConfirm(ordersAdapter.getUpdatedOrders());
    //         }
    //         dismiss();
    //     });

    //     // 设置第一个输入框自动获取焦点并显示键盘
    //     view.post(() -> {
    //         if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
    //             View firstItem = ordersRecyclerView.getChildAt(0);
    //             if (firstItem != null) {
    //                 EditText firstInput = firstItem.findViewById(R.id.et_out_quantity);
    //                 if (firstInput != null) {
    //                     firstInput.setSelection(firstInput.getText().length());
    //                     firstInput.requestFocus();
    //                     InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    //                     imm.showSoftInput(firstInput, InputMethodManager.SHOW_IMPLICIT);
    //                 }
    //             }
    //         }
    //     });
    // }


    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_stock_out_goods);

    // 设置弹窗圆角背景和半透明遮罩
    Window window = getWindow();
    if (window != null) {
        window.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0.6f;
        // 设置弹窗宽度为屏幕宽度的90%
        lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        // 设置弹窗高度为自适应内容，但不超过屏幕高度的80%
        // lp.height = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.8);
       // 设置弹窗高度为自适应内容
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 设置弹窗位置居中
        lp.gravity = android.view.Gravity.CENTER;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setWindowAnimations(R.style.DialogAnimation);
    }

    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.dialog_stock_out_goods, null);
    this.setCanceledOnTouchOutside(true);
    setContentView(view);

    // 设置商品标题
    TextView titleView = view.findViewById(R.id.tv_goods_title);
    if (goodsEntity != null && goodsEntity.getNxDistributerGoodsEntity() != null) {
        StringBuilder title = new StringBuilder();
        if (goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand() != null) {
            title.append(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand()).append(" ");
        }
        title.append(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName());
        String standardWeight = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardWeight();
        String standardName = goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsStandardname();
        if (standardWeight != null && !standardWeight.equals("null")) {
            title.append("(").append(standardWeight).append("/").append(standardName).append(")");
        } else {
            title.append("(").append(standardName).append(")");
        }
        titleView.setText(title.toString());
    }

    // 设置订单列表
    ordersRecyclerView = view.findViewById(R.id.rv_orders);
    ordersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    // 添加分割线
    ordersRecyclerView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(mContext, android.support.v7.widget.DividerItemDecoration.VERTICAL));
    
    // 获取滚动提示文本视图
    scrollHint = view.findViewById(R.id.tv_scroll_hint);
    
    // 添加滚动监听
    ordersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // 当停止滚动时，隐藏键盘
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            // 控制滚动提示的显示和隐藏
            if (scrollHint != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    scrollHint.setVisibility(firstVisibleItem > 0 ? View.VISIBLE : View.GONE);
                }
            }
        }
    });

        // 获取订单列表
        List<NxDepartmentOrdersEntity> ordersList = goodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        ordersAdapter = new StockOutOrdersAdapter(ordersList, goodsEntity);
        ordersRecyclerView.setAdapter(ordersAdapter);

// 根据订单数量调整列表高度
        if (ordersList != null) {
            View listContainer = view.findViewById(R.id.list_container);
            if (listContainer != null) {
                // 移除layout_weight的影响
                ViewGroup.LayoutParams params = listContainer.getLayoutParams();
                if (params instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) params).weight = 0;
                }

                int height;
                if (ordersList.size() <= 3) {
                    // 订单数量少时使用较小的高度
                    height = (int) (200 * getContext().getResources().getDisplayMetrics().density);
                    android.util.Log.d("StockOutDialog", "订单数量<=3，设置高度为: " + height + "px");
                } else {
                    // 订单数量多时使用最大高度
                    height = (int) (400 * getContext().getResources().getDisplayMetrics().density);
                    android.util.Log.d("StockOutDialog", "订单数量>3，设置高度为: " + height + "px");
                }

                // 设置固定高度
                params.height = height;
                listContainer.setLayoutParams(params);

                // 设置RecyclerView的高度
                ViewGroup.LayoutParams rvParams = ordersRecyclerView.getLayoutParams();
                rvParams.height = height;
                ordersRecyclerView.setLayoutParams(rvParams);

                // 禁用RecyclerView的自动调整大小
                ordersRecyclerView.setHasFixedSize(true);

                android.util.Log.d("StockOutDialog", "设置后的容器高度: " + listContainer.getLayoutParams().height);
                android.util.Log.d("StockOutDialog", "设置后的RecyclerView高度: " + ordersRecyclerView.getLayoutParams().height);
            }
        }
    // 设置按钮点击事件
    view.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        dismiss();
    });
    
    view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (confirmListener != null) {
            confirmListener.onConfirm(ordersAdapter.getUpdatedOrders());
        }
        dismiss();
    });

    // 设置第一个输入框自动获取焦点并显示键盘
    view.post(() -> {
        if (ordersAdapter != null && ordersAdapter.getItemCount() > 0) {
            View firstItem = ordersRecyclerView.getChildAt(0);
            if (firstItem != null) {
                EditText firstInput = firstItem.findViewById(R.id.et_out_quantity);
                if (firstInput != null) {
                    firstInput.setSelection(firstInput.getText().length());
                    firstInput.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(firstInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }
    });
}

    @Override
    public void dismiss() {
        // 确保在关闭对话框时隐藏键盘
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        super.dismiss();
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }

    public interface OnConfirmListener {
        void onConfirm(List<NxDepartmentOrdersEntity> orders);
    }
}