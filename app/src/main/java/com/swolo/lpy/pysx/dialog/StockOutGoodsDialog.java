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
import android.widget.TextView;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;

import java.util.List;

public class StockOutGoodsDialog extends Dialog {
    private Context mContext;
    private NxDistributerGoodsShelfGoodsEntity goodsEntity;
    private OnConfirmListener confirmListener;
    private RecyclerView ordersRecyclerView;
    private StockOutOrdersAdapter ordersAdapter;

    public StockOutGoodsDialog(Context context, NxDistributerGoodsShelfGoodsEntity goods) {
        super(context, R.style.Theme_dialog);
        this.mContext = context;
        this.goodsEntity = goods;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_stock_out_goods);

        // 设置弹窗圆角背景和半透明遮罩
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.5f; // 0.5为半透明，1为全黑
            // 设置弹窗宽度为屏幕宽度的90%
            lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            // 设置弹窗位置向上偏移
            lp.y = -100; // 向上偏移100像素
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_stock_out_goods, null);
        this.setCanceledOnTouchOutside(true);
        setContentView(view);

        // 设置商品标题
        TextView titleView = view.findViewById(R.id.tv_goods_title);
        if (goodsEntity != null && goodsEntity.getNxDistributerGoodsEntity() != null) {
            StringBuilder title = new StringBuilder();
            // 添加品牌
            if (goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand() != null) {
                title.append(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsBrand()).append(" ");
            }
            // 添加商品名称
            title.append(goodsEntity.getNxDistributerGoodsEntity().getNxDgGoodsName());
            // 添加规格
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
        
        // 获取订单列表
        List<NxDepartmentOrdersEntity> ordersList = goodsEntity.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities();
        ordersAdapter = new StockOutOrdersAdapter(ordersList, goodsEntity);
        ordersRecyclerView.setAdapter(ordersAdapter);

        // 设置按钮点击事件
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
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
                        // 设置焦点在文本末尾
                        firstInput.setSelection(firstInput.getText().length());
                        firstInput.requestFocus();
                        // 显示键盘
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(firstInput, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }
        });
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }

    public interface OnConfirmListener {
        void onConfirm(List<NxDepartmentOrdersEntity> orders);
    }
} 