package com.swolo.lpy.pysx.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.fragment.DepOutOrderSimpleTabFragment;
import com.swolo.lpy.pysx.main.presenter.DepOutOrderPresenter;
import com.swolo.lpy.pysx.main.modal.DepOutOrderResponse;
import android.widget.ImageView;

public class DepOutOrderSimpleActivity extends AppCompatActivity implements DepOutOrderPresenter.DepOutOrderView {
    private static final String TAG = "DepOutOrderSimpleActivity";
    private static final String[] TAB_TITLES = {"未拣货", "已拣货"};
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DepOutOrderSimpleTabFragment[] fragments = new DepOutOrderSimpleTabFragment[2];
    private DepOutOrderPresenter presenter;
    
    // 从Intent获取的参数
    private String depFatherId;
    private String gbDepFatherId;
    private String resFatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dep_out_order);
        // 顶部导航栏返回按钮点击事件
        ImageView btnBack = findViewById(R.id.iv_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
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
        Log.d(TAG, "初始化Fragment");
        fragments[0] = DepOutOrderSimpleTabFragment.newInstance(0);
        fragments[1] = DepOutOrderSimpleTabFragment.newInstance(1);
        
        // 为第二个Fragment设置删除监听器
        fragments[1].setOnDeleteOrderListener((orderId, orderObject) -> {
            Log.d(TAG, "收到删除订单请求: orderId=" + orderId + ", orderObject=" + orderObject);
            // 调用Presenter删除订单
            if (orderObject != null) {
                presenter.deleteOutOrder(orderObject);
            } else {
                presenter.deleteOrder(orderId);
            }
        });
        
        Log.d(TAG, "Fragment初始化完成: fragments[0]=" + fragments[0] + ", fragments[1]=" + fragments[1]);

        // 设置ViewPager2适配器
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() { return fragments.length; }
            @Override
            public Fragment createFragment(int position) { return fragments[position]; }
        });

        // TabLayout与ViewPager2联动
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(TAB_TITLES[position])).attach();
        
        // 监听Tab切换，加载对应数据
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "Tab切换到: " + position);
                loadDataForTab(position);
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
        Log.d(TAG, "显示加载中...");
    }

    @Override
    public void hideLoading() {
        Log.d(TAG, "隐藏加载中...");
    }

    @Override
    public void onGetHaveOutDataSuccess(DepOutOrderResponse response) {
        Log.d(TAG, "已拣货数据加载成功: " + response);
        if (fragments[1] != null) {
            fragments[1].updateData(response.arr);
        }
    }

    @Override
    public void onGetHaveOutDataFail(String error) {
        Log.e(TAG, "已拣货数据加载失败: " + error);
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
    }

    @Override
    public void onGetNotOutDataFail(String error) {
        Log.e(TAG, "未拣货数据加载失败: " + error);
        if (fragments[0] != null) {
            fragments[0].showError(error);
        }
    }

    @Override
    public void onDeleteOrderSuccess() {
        Log.d(TAG, "删除订单成功");
        // 删除成功后刷新当前Tab的数据
        int currentTab = viewPager.getCurrentItem();
        loadDataForTab(currentTab);
    }

    @Override
    public void onDeleteOrderFail(String error) {
        Log.e(TAG, "删除订单失败: " + error);
    }
} 