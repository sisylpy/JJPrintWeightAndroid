package com.swolo.lpy.pysx.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.modal.DepOutOrderResponse;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;

import java.util.ArrayList;
import java.util.List;

public class DepOutOrderTabFragment extends Fragment {
    private static final String ARG_TYPE = "tab_type";
    private RecyclerView recyclerView;
    private TextView tvNoData, tvBottomTip, tvError;
    private DepOutOrderGroupAdapter adapter;
    private List<DepOutOrderResponse.GreatGrand> groupList;
    private int tabType; // 0: 未拣货, 1: 已拣货

    public static DepOutOrderTabFragment newInstance(int tabType) {
        DepOutOrderTabFragment fragment = new DepOutOrderTabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getInt(ARG_TYPE, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dep_out_order_tab, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvNoData = view.findViewById(R.id.tv_no_data);
        tvBottomTip = view.findViewById(R.id.tv_bottom_tip);
        tvError = view.findViewById(R.id.tv_error);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DepOutOrderGroupAdapter();
        adapter.setShowDelete(tabType == 1); // 1:已拣货Tab显示删除按钮
        adapter.setOnGoodsClickListener((purGoods, order) -> showInputDialog(purGoods, order));
        adapter.setOnGoodsDeleteListener((purGoods, order) -> deleteOrder(purGoods, order));
        recyclerView.setAdapter(adapter);
        
        // 初始化显示
        updateData(groupList);
        return view;
    }

    /**
     * 公共方法：外部可调用此方法刷新当前Tab的数据
     * @param data 分组商品数据
     */
    public void updateData(List<DepOutOrderResponse.GreatGrand> data) {
        android.util.Log.d("DepOutOrderTabFragment", "updateData called: data=" + data + 
              ", size=" + (data != null ? data.size() : 0));
        
        this.groupList = data;
        if (adapter != null) {
            // 转换数据结构
            List<Object> convertedData = convertToAdapterData(data);
            android.util.Log.d("DepOutOrderTabFragment", "convertedData: " + convertedData + 
                  ", size=" + (convertedData != null ? convertedData.size() : 0));
            adapter.setData(convertedData);
        }
        
        // 更新UI状态
        updateUIState(data == null || data.isEmpty() ? UIState.NO_DATA : UIState.SUCCESS);
    }
    
    /**
     * 显示错误状态
     */
    public void showError(String errorMessage) {
        if (tvError != null) {
            tvError.setText("加载失败: " + errorMessage);
        }
        updateUIState(UIState.ERROR);
    }
    
    /**
     * 转换数据结构为适配器可用的格式
     */
    private List<Object> convertToAdapterData(List<DepOutOrderResponse.GreatGrand> data) {
        if (data == null) return null;
        
        List<Object> convertedList = new ArrayList<>();
        
        for (DepOutOrderResponse.GreatGrand responseGreatGrand : data) {
            // 创建适配器期望的GreatGrand对象
            DepOutOrderGroupAdapter.GreatGrand adapterGreatGrand = new DepOutOrderGroupAdapter.GreatGrand();
            adapterGreatGrand.fatherGoodsEntities = new ArrayList<>();
            
            if (responseGreatGrand.fatherGoodsEntities != null) {
                for (DepOutOrderResponse.FatherGoodsEntities responseFather : responseGreatGrand.fatherGoodsEntities) {
                    // 创建适配器期望的FatherGoodsEntities对象
                    DepOutOrderGroupAdapter.FatherGoodsEntities adapterFather = new DepOutOrderGroupAdapter.FatherGoodsEntities();
                    adapterFather.nxDfgFatherGoodsName = responseFather.nxDfgFatherGoodsName;
                    adapterFather.nxDistributerPurchaseGoodsEntities = new ArrayList<>();
                    
                    if (responseFather.nxDistributerPurchaseGoodsEntities != null) {
                        for (DepOutOrderResponse.PurchaseGoodsEntities responsePurchase : responseFather.nxDistributerPurchaseGoodsEntities) {
                            // 创建适配器期望的PurchaseGoodsEntities对象
                            DepOutOrderGroupAdapter.PurchaseGoodsEntities adapterPurchase = new DepOutOrderGroupAdapter.PurchaseGoodsEntities();
                            
                            // 转换商品信息
                            if (responsePurchase.nxDistributerGoodsEntity != null) {
                                DepOutOrderGroupAdapter.DistributerGoodsEntity adapterGoods = new DepOutOrderGroupAdapter.DistributerGoodsEntity();
                                adapterGoods.nxDgGoodsName = responsePurchase.nxDistributerGoodsEntity.nxDgGoodsName;
                                adapterGoods.nxDgGoodsStandardname = responsePurchase.nxDistributerGoodsEntity.nxDgGoodsStandardname;
                                adapterPurchase.nxDistributerGoodsEntity = adapterGoods;
                            }
                            
                            // 转换订单信息
                            adapterPurchase.nxDepartmentOrdersEntities = new ArrayList<>();
                            if (responsePurchase.nxDepartmentOrdersEntities != null) {
                                for (com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity responseOrder : responsePurchase.nxDepartmentOrdersEntities) {
                                    adapterPurchase.nxDepartmentOrdersEntities.add(responseOrder);
                                }
                            }
                            
                            adapterFather.nxDistributerPurchaseGoodsEntities.add(adapterPurchase);
                        }
                    }
                    
                    adapterGreatGrand.fatherGoodsEntities.add(adapterFather);
                }
            }
            
            convertedList.add(adapterGreatGrand);
        }
        
        return convertedList;
    }
    
    /**
     * UI状态枚举
     */
    private enum UIState {
        LOADING, SUCCESS, NO_DATA, ERROR
    }
    
    /**
     * 更新UI状态
     */
    private void updateUIState(UIState state) {
        if (recyclerView == null || tvNoData == null || tvBottomTip == null || tvError == null) {
            return;
        }
        
        switch (state) {
            case LOADING:
                recyclerView.setVisibility(View.GONE);
                tvNoData.setVisibility(View.GONE);
                tvBottomTip.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
                break;
            case SUCCESS:
                recyclerView.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                tvBottomTip.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
                break;
            case NO_DATA:
                recyclerView.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
                tvBottomTip.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
                tvNoData.setText(tabType == 0 ? "没有未拣货订单" : "没有已拣货订单");
                break;
            case ERROR:
                recyclerView.setVisibility(View.GONE);
                tvNoData.setVisibility(View.GONE);
                tvBottomTip.setVisibility(View.GONE);
                tvError.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showInputDialog(DepOutOrderGroupAdapter.PurchaseGoodsEntities purGoods, com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("输入重量");
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(order.nxDoWeight);
        input.setSelection(input.getText().length());
        input.requestFocus();
        builder.setView(input);
        builder.setMessage("单位: " + purGoods.nxDistributerGoodsEntity.nxDgGoodsStandardname);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                input.setError("请输入重量");
                return;
            }
            order.nxDoWeight = value;
            adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteOrder(DepOutOrderGroupAdapter.PurchaseGoodsEntities purGoods, com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order) {
        // 显示删除确认对话框
        new AlertDialog.Builder(getContext())
            .setTitle("确认删除")
            .setMessage("确定要删除这个订单吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                // 调用Activity的删除方法
                if (getActivity() instanceof DepOutOrderActivity) {
                    DepOutOrderActivity activity = (DepOutOrderActivity) getActivity();
                    // 这里需要根据订单类型调用不同的删除接口
                    // 暂时使用一个通用的删除方法，后续可以根据需要区分
                    activity.deleteOrder(order);
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
} 