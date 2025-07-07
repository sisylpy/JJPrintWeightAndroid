package com.swolo.lpy.pysx.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.swolo.lpy.pysx.R;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.swolo.lpy.pysx.main.presenter.DepOutOrderPresenter;
import com.swolo.lpy.pysx.main.modal.DepOutOrderResponse;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class DepOutOrderActivity extends AppCompatActivity implements DepOutOrderPresenter.DepOutOrderView {
    private static final String TAG = "DepOutOrderActivity";
    private static final List<String> TAB_TITLES = Arrays.asList("未拣货", "已拣货");
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DepOutOrderTabFragment[] fragments = new DepOutOrderTabFragment[2];
    private DepOutOrderPresenter presenter;
    
    // 从Intent获取的参数
    private String depFatherId;
    private String gbDepFatherId;
    private String resFatherId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dep_out_order);
        setTitle("出库商品");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 获取Intent参数
        Intent intent = getIntent();
        depFatherId = intent.getStringExtra("depFatherId");
        gbDepFatherId = intent.getStringExtra("gbDepFatherId");
        resFatherId = intent.getStringExtra("resFatherId");
        
        Log.d(TAG, "接收到的参数: depFatherId=" + depFatherId + 
              ", gbDepFatherId=" + gbDepFatherId + ", resFatherId=" + resFatherId);
        
        // 初始化Presenter
        presenter = new DepOutOrderPresenter(this);
        
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // 初始化Fragment
        fragments[0] = DepOutOrderTabFragment.newInstance(0);
        fragments[1] = DepOutOrderTabFragment.newInstance(1);

        // 设置ViewPager2适配器
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() { return fragments.length; }
            @Override
            public Fragment createFragment(int position) { return fragments[position]; }
        });

        // TabLayout与ViewPager2联动
        for (String title : TAB_TITLES) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                // Tab切换时重新加载数据
                loadDataForTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) tab.select();
            }
        });

        // 加载初始数据（默认加载未拣货）
        loadDataForTab(0);
    }
    
    /**
     * 根据Tab位置加载对应数据
     */
    private void loadDataForTab(int tabPosition) {
        if (presenter == null) {
            Log.e(TAG, "Presenter为null，无法加载数据");
            return;
        }
        
        Log.d(TAG, "加载Tab数据: position=" + tabPosition);
        
        if (tabPosition == 0) {
            // 未拣货
            presenter.getNotOutCataGoods(depFatherId, gbDepFatherId, resFatherId);
        } else if (tabPosition == 1) {
            // 已拣货
            presenter.getHaveOutCataGoods(depFatherId, gbDepFatherId, resFatherId);
        }
    }

    // ========== DepOutOrderPresenter.DepOutOrderView 接口实现 ==========
    
    @Override
    public void showLoading() {
        // TODO: 显示加载对话框
        Log.d(TAG, "显示加载中...");
    }

    @Override
    public void hideLoading() {
        // TODO: 隐藏加载对话框
        Log.d(TAG, "隐藏加载中...");
    }

    @Override
    public void onGetHaveOutDataSuccess(DepOutOrderResponse response) {
        Log.d(TAG, "已拣货数据加载成功: " + response);
        if (fragments[1] != null) {
            fragments[1].updateData(response.arr);
        }
        // 更新Tab数量显示
        updateTabBadge(1, response.haveCount);
    }

    @Override
    public void onGetHaveOutDataFail(String error) {
        Log.e(TAG, "已拣货数据加载失败: " + error);
        Toast.makeText(this, "已拣货数据加载失败: " + error, Toast.LENGTH_SHORT).show();
        // 显示错误状态
        if (fragments[1] != null) {
            fragments[1].showError(error);
        }
    }

    @Override
    public void onGetNotOutDataSuccess(DepOutOrderResponse response) {
        Log.d(TAG, "未拣货数据加载成功: " + response);
        if (fragments[0] != null) {
            fragments[0].updateData(response.arr);
        }
        // 更新Tab数量显示
        updateTabBadge(0, response.notCount);
    }

    @Override
    public void onGetNotOutDataFail(String error) {
        Log.e(TAG, "未拣货数据加载失败: " + error);
        Toast.makeText(this, "未拣货数据加载失败: " + error, Toast.LENGTH_SHORT).show();
        // 显示错误状态
        if (fragments[0] != null) {
            fragments[0].showError(error);
        }
    }

    @Override
    public void onDeleteOrderSuccess() {
        Log.d(TAG, "删除订单成功");
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        // 删除成功后刷新当前Tab的数据
        int currentTab = viewPager.getCurrentItem();
        loadDataForTab(currentTab);
    }

    @Override
    public void onDeleteOrderFail(String error) {
        Log.e(TAG, "删除订单失败: " + error);
        Toast.makeText(this, "删除失败: " + error, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 更新Tab的数量显示
     */
    private void updateTabBadge(int tabPosition, int count) {
        if (tabPosition < TAB_TITLES.size()) {
            TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
            if (tab != null) {
                String title = TAB_TITLES.get(tabPosition);
                if (count > 0) {
                    tab.setText(title + "(" + count + ")");
                } else {
                    tab.setText(title);
                }
            }
        }
    }
    
    /**
     * 删除订单
     * 根据订单类型调用不同的删除接口
     */
    public void deleteOrder(com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity order) {
        if (presenter == null) {
            Log.e(TAG, "Presenter为null，无法删除订单");
            return;
        }
        
        String orderId = String.valueOf(order.nxDepartmentOrdersId);
        if (orderId == null || orderId.isEmpty()) {
            Log.e(TAG, "订单ID为空，无法删除");
            Toast.makeText(this, "订单ID为空，无法删除", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "删除订单: orderId=" + orderId + ", gbDepFatherId=" + gbDepFatherId);
        
        // 根据部门类型调用不同的删除接口
        if ("-1".equals(gbDepFatherId)) {
            // 内销部门，调用内销订单删除接口
            // 直接传递整个订单对象，和小程序行为一致
            presenter.deleteOutOrder(order);
        } else {
            // 国标部门，调用国标订单删除接口
            presenter.deleteGbOrder(orderId);
        }
    }
} 