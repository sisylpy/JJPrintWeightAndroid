package com.swolo.lpy.pysx.main.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.adapter.DepOutOrderSimpleAdapter;
import com.swolo.lpy.pysx.main.modal.DepOutOrderResponse;
import java.util.ArrayList;
import java.util.List;

public class DepOutOrderSimpleTabFragment extends Fragment {
    private static final String ARG_TYPE = "tab_type";
    private int tabType;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private DepOutOrderSimpleAdapter adapter;
    private OnDeleteOrderListener deleteListener;
    private View emptyContainer;
    private View errorContainer;
    private View loadingContainer;

    public static DepOutOrderSimpleTabFragment newInstance(int tabType) {
        DepOutOrderSimpleTabFragment f = new DepOutOrderSimpleTabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, tabType);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d("DepOutOrderSimpleTabFragment", "onCreateView开始: tabType=" + tabType);
        View view = inflater.inflate(R.layout.fragment_dep_out_order_tab, container, false);
        android.util.Log.d("DepOutOrderSimpleTabFragment", "onCreateView: view=" + view);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_no_data);
        
        // 获取新的布局组件
        emptyContainer = view.findViewById(R.id.empty_container);
        errorContainer = view.findViewById(R.id.error_container);
        loadingContainer = view.findViewById(R.id.loading_container);
        android.util.Log.d("DepOutOrderSimpleTabFragment", "findViewById结果: recyclerView=" + recyclerView + ", tvEmpty=" + tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 添加分割线装饰器
        recyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(getContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, @NonNull androidx.recyclerview.widget.RecyclerView parent, @NonNull androidx.recyclerview.widget.RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position >= 0 && adapter != null && adapter.getItemCount() > position) {
                    DepOutOrderSimpleAdapter.Item item = adapter.getData().get(position);
                    // 只为商品和订单项添加分割线，分组项不添加
                    if (item.type == DepOutOrderSimpleAdapter.TYPE_GOODS || item.type == DepOutOrderSimpleAdapter.TYPE_ORDER) {
                        outRect.bottom = 1;
                    }
                }
            }
        });
        
        adapter = new DepOutOrderSimpleAdapter();
        
        // 先获取tabType
        if (getArguments() != null) tabType = getArguments().getInt(ARG_TYPE, 0);
        android.util.Log.d("DepOutOrderSimpleTabFragment", "onCreateView: tabType=" + tabType);
        
        // 为第二个Tab（已拣货）启用删除功能
        android.util.Log.d("DepOutOrderSimpleTabFragment", "初始化适配器: tabType=" + tabType);
        if (tabType == 1) {
            android.util.Log.d("DepOutOrderSimpleTabFragment", "启用删除功能");
            adapter.setShowDeleteButtons(true);
            adapter.setOnDeleteClickListener((orderId, orderObject) -> {
                android.util.Log.d("DepOutOrderSimpleTabFragment", "删除按钮被点击: orderId=" + orderId);
                if (deleteListener != null) {
                    deleteListener.onDeleteOrder(orderId, orderObject);
                }
            });
        } else {
            android.util.Log.d("DepOutOrderSimpleTabFragment", "不启用删除功能");
        }
        
        recyclerView.setAdapter(adapter);
        loadData();
        android.util.Log.d("DepOutOrderSimpleTabFragment", "onCreateView完成");
        return view;
    }

    private void loadData() {
        android.util.Log.d("DepOutOrderSimpleTabFragment", "loadData开始: tabType=" + tabType);
        // 显示加载状态
        showLoading();
        android.util.Log.d("DepOutOrderSimpleTabFragment", "loadData完成");
        
        // 添加RecyclerView状态检查
        if (recyclerView != null) {
            android.util.Log.d("DepOutOrderSimpleTabFragment", "RecyclerView状态: width=" + recyclerView.getWidth() + 
                  ", height=" + recyclerView.getHeight() + ", visibility=" + recyclerView.getVisibility());
        }
    }
    
    private void showLoading() {
        recyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.VISIBLE);
    }
    
    private void showEmpty(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        errorContainer.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.GONE);
        tvEmpty.setText(message);
    }
    

    
    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.GONE);
    }
    
    /**
     * 更新数据 - 由Activity调用
     */
    public void updateData(List<DepOutOrderResponse.GreatGrand> data) {
        android.util.Log.d("DepOutOrderSimpleTabFragment", "updateData: data=" + data + 
              ", size=" + (data != null ? data.size() : 0));
        
        if (data == null || data.isEmpty()) {
            showEmpty(tabType == 0 ? "没有未拣货订单" : "没有已拣货订单");
            adapter.setData(new ArrayList<>());
            return;
        }
        
        // 转换数据格式
        List<DepOutOrderSimpleAdapter.Item> items = new ArrayList<>();
        for (DepOutOrderResponse.GreatGrand greatGrand : data) {
            if (greatGrand.fatherGoodsEntities != null) {
                for (DepOutOrderResponse.FatherGoodsEntities father : greatGrand.fatherGoodsEntities) {
                    // 添加分组
                    items.add(new DepOutOrderSimpleAdapter.Item(DepOutOrderSimpleAdapter.TYPE_GROUP, 
                        father.nxDfgFatherGoodsName));
                    if (father.nxDistributerPurchaseGoodsEntities != null) {
                        for (DepOutOrderResponse.PurchaseGoodsEntities purchase : father.nxDistributerPurchaseGoodsEntities) {
                            String goodsName = purchase.nxDistributerGoodsEntity != null ? 
                                purchase.nxDistributerGoodsEntity.nxDgGoodsName : "未知商品";
                            if (purchase.nxDepartmentOrdersEntities != null) {
                                for (int i = 0; i < purchase.nxDepartmentOrdersEntities.size(); i++) {
                                    com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order = 
                                        purchase.nxDepartmentOrdersEntities.get(i);
                                    // 订货量和单位
                                    String orderQuantity = order.nxDoQuantity != null ? order.nxDoQuantity : "";
                                    String orderUnit = order.nxDoStandard != null ? order.nxDoStandard : "";
                                    // 出货量和单位
                                    String outWeight = order.nxDoWeight != null ? order.nxDoWeight : "";
                                    String outUnit = order.nxDoStandard != null ? order.nxDoStandard : "";
                                    int orderIndex = i + 1;
                                    if (tabType == 1 && order.nxDepartmentOrdersId != null) {
                                        items.add(new DepOutOrderSimpleAdapter.Item(
                                            DepOutOrderSimpleAdapter.TYPE_ORDER,
                                            orderIndex,
                                            goodsName,
                                            orderQuantity,
                                            orderUnit,
                                            outWeight,
                                            outUnit,
                                            order.nxDepartmentOrdersId.toString(),
                                            order
                                        ));
                                    } else {
                                        items.add(new DepOutOrderSimpleAdapter.Item(
                                            DepOutOrderSimpleAdapter.TYPE_ORDER,
                                            orderIndex,
                                            goodsName,
                                            orderQuantity,
                                            orderUnit,
                                            outWeight,
                                            outUnit,
                                            null,
                                            order
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        android.util.Log.d("DepOutOrderSimpleTabFragment", "updateData: items.size=" + items.size());
        adapter.setData(items);
        if (items.isEmpty()) {
            showEmpty(tabType == 0 ? "没有未拣货订单" : "没有已拣货订单");
        } else {
            showContent();
        }
    }
    
    /**
     * 显示错误 - 公共方法供Activity调用
     */
    public void showError(String error) {
        android.util.Log.d("DepOutOrderSimpleTabFragment", "showError: " + error);
        showErrorInternal("加载失败: " + error);
        adapter.setData(new ArrayList<>());
    }
    
    /**
     * 显示错误 - 内部方法
     */
    private void showErrorInternal(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
        errorContainer.setVisibility(View.VISIBLE);
        loadingContainer.setVisibility(View.GONE);
        TextView tvError = errorContainer.findViewById(R.id.tv_error);
        tvError.setText(message);
    }
    
    public void setOnDeleteOrderListener(OnDeleteOrderListener listener) {
        this.deleteListener = listener;
    }
    
    public interface OnDeleteOrderListener {
        void onDeleteOrder(String orderId, Object orderObject);
    }
} 