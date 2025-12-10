package com.swolo.lpy.pysx.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.api.OrdersApi;
import com.swolo.lpy.pysx.http.CommonResponse;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.WaitStockGoodsDepsResponse;
import com.google.gson.reflect.TypeToken;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.swolo.lpy.pysx.main.modal.NxDistributerUserEntity;

public class CustomerListActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvCustomers;
    private Button btnNext;
    private CustomerAdapter adapter;
    private List<Customer> customerList = new ArrayList<>();
    private List<Integer> selectedIndexes = new ArrayList<>();
    private List<NxDepartmentEntity> nxDepList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        initView();
        // 加载顶部栏头像
        try {
            SharedPreferences sp = getSharedPreferences("user_cache", MODE_PRIVATE);
            String userInfoJson = sp.getString("userInfo", null);
            if (userInfoJson != null) {
                NxDistributerUserEntity userInfo = new Gson().fromJson(userInfoJson, NxDistributerUserEntity.class);
                String avatarUrl = "https://grainservice.club:8443/nongxinle/" + userInfo.getNxDiuWxAvartraUrl();
                ImageView avatarView = findViewById(R.id.iv_avatar);
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .transform(new CircleCrop())
                        .into(avatarView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initData();
        setView();
        bindAction();
        
        // 初始显示加载动画
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private void initView() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        rvCustomers = findViewById(R.id.rv_customers);
        btnNext = findViewById(R.id.btn_next);
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        
        // 设置下拉刷新
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, 
            android.R.color.holo_green_light, 
            android.R.color.holo_orange_light, 
            android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("CustomerList", "下拉刷新触发");
                // 重新加载数据
                initData();
            }
        });
    }

    private void initData() {
        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        int disId = sp.getInt("distributer_id", -1);
        if (disId == -1) {
            Toast.makeText(this, "未获取到分销商ID，请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(OrdersApi.class).stockerGetWaitStockGoodsDeps(disId),
                new TypeToken<WaitStockGoodsDepsResponse>() {}
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<WaitStockGoodsDepsResponse>() {
                @Override
                public void onCompleted() {
                    // 数据加载完成，停止刷新动画
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e("CustomerList", "加载客户失败: " + e.getMessage(), e);
                    Toast.makeText(CustomerListActivity.this, "加载客户失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // 出错时也要停止刷新动画
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    // 清空数据列表（避免显示过期数据）
                    customerList.clear();
                    nxDepList.clear();
                    // 通知Adapter刷新UI
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        Log.d("CustomerList", "加载失败，已清空客户列表并刷新UI");
                    }
                    // 出错时重置选中状态
                    selectedIndexes.clear();
                    btnNext.setVisibility(View.GONE);
                }

                @Override
                public void onNext(WaitStockGoodsDepsResponse resp) {
                    Log.d("CustomerList", "接口返回 resp=" + new Gson().toJson(resp));
                    if (resp != null && resp.nxDep != null && !resp.nxDep.isEmpty()) {
                        Log.d("CustomerList", "nxDep size=" + resp.nxDep.size());
                        for (NxDepartmentEntity dep : resp.nxDep) {
                            Log.d("CustomerList", "dep: id=" + dep.nxDepartmentId + ", name=" + dep.nxDepartmentName + ", addCount=" + dep.nxDepartmentAddCount + ", purOrderCount=" + dep.nxDepartmentPurOrderCount);
                        }
                        nxDepList.clear();
                        nxDepList.addAll(resp.nxDep);
                        List<Customer> customers = new ArrayList<>();
                        for (NxDepartmentEntity dep : nxDepList) {
                            customers.add(new Customer(
                                dep.nxDepartmentName,
                                dep.nxDepartmentAddCount != null ? dep.nxDepartmentAddCount : 0,
                                dep.nxDepartmentPurOrderCount != null ? dep.nxDepartmentPurOrderCount : 0
                            ));
                        }
                        customerList.clear();
                        customerList.addAll(customers);
                        Log.d("CustomerList", "customerList.size=" + customerList.size());
                        if (adapter == null) {
                            adapter = new CustomerAdapter(customerList);
                            rvCustomers.setAdapter(adapter);
                            Log.d("CustomerList", "初始化Adapter，客户数=" + customerList.size());
                            adapter.setOnItemClickListener(new CustomerAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    if (customerList.get(position).unpickedCount > 0) {
                                        adapter.toggleSelection(position);
                                        selectedIndexes = adapter.getSelectedIndexes();
                                        btnNext.setVisibility(adapter.hasSelection() ? View.VISIBLE : View.GONE);
                                    } else {
                                        Toast.makeText(CustomerListActivity.this, "未拣货为0，不能选择", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            adapter.notifyDataSetChanged();
                            Log.d("CustomerList", "刷新Adapter，客户数=" + customerList.size());
                        }
                        
                        // 刷新完成后重置选中状态
                        selectedIndexes.clear();
                        btnNext.setVisibility(View.GONE);
                    } else {
                        Log.d("CustomerList", "没有客户数据");
                        // 清空数据列表
                        customerList.clear();
                        nxDepList.clear();
                        // 通知Adapter刷新UI
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            Log.d("CustomerList", "已清空客户列表并刷新UI");
                        }
                        // 重置选中状态
                        selectedIndexes.clear();
                        btnNext.setVisibility(View.GONE);
                        Toast.makeText(CustomerListActivity.this, "没有客户数据", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void setView() {
        btnNext.setVisibility(View.GONE);
    }

    private void bindAction() {
        // 头像按钮点击事件 - 打开设置页面
        findViewById(R.id.iv_avatar).setOnClickListener(v -> {
            Log.d("CustomerList", "点击头像按钮，打开设置页面");
            Intent intent = new Intent(CustomerListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        // 圆形按钮点击事件 - 跳转到部门列表页面
        findViewById(R.id.btn_circle).setOnClickListener(v -> {
            Log.d("CustomerList", "点击圆形按钮，跳转到部门列表页面");
            Intent intent = new Intent(CustomerListActivity.this, DepartmentListActivity.class);
            // 传递分销商ID
            SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
            int disId = sp.getInt("distributer_id", -1);
            intent.putExtra("disId", disId);
            intent.putExtra("goodsType", 1); // 1表示出库
            startActivityForResult(intent, 1001);
        });
        
        btnNext.setOnClickListener(v -> {
            if (!selectedIndexes.isEmpty()) {
                Log.d("CustomerList", "跳转到出库页面，选中客户数量: " + selectedIndexes.size());
                
                // 保存选中的客户信息到缓存
                saveSelectedCustomersToCache();
                
                // 跳转到出库页面（使用 startActivityForResult，以便返回时刷新）
                Intent intent = new Intent(CustomerListActivity.this, CustomerStockOutActivity.class);
                // 传递分销商ID
                SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
                int disId = sp.getInt("distributer_id", -1);
                intent.putExtra("distributer_id", disId);
                startActivityForResult(intent, 1002); // requestCode=1002 表示出库页面
                Log.d("CustomerList", "使用 startActivityForResult 跳转，requestCode=1002");
            }
        });
    }

    private void saveSelectedCustomersToCache() {
        SharedPreferences sp = getSharedPreferences("idsChangeStock", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        
        // 保存选中的内销部门ID
        List<String> nxDepIds = new ArrayList<>();
        List<String> nxDepNames = new ArrayList<>();
        
        for (Integer index : selectedIndexes) {
            if (index < nxDepList.size()) {
                NxDepartmentEntity dep = nxDepList.get(index);
                nxDepIds.add(String.valueOf(dep.nxDepartmentId));
                nxDepNames.add(dep.nxDepartmentName);
                Log.d("CustomerList", "保存选中客户: " + dep.nxDepartmentName + " (ID: " + dep.nxDepartmentId + ")");
            }
        }
        
        editor.putString("outNxDepIds", String.join(",", nxDepIds));
        editor.putString("outNxDepNames", String.join(",", nxDepNames));
        editor.putString("outGbDepIds", ""); // 暂时不处理国标部门
        editor.putString("outGbDepNames", "");
        editor.apply();
        
        Log.d("CustomerList", "缓存保存完成: nxDepIds=" + String.join(",", nxDepIds) + ", nxDepNames=" + String.join(",", nxDepNames));
    }

    // 客户数据模型
    public static class Customer {
        public String name;
        public int orderCount;
        public int unpickedCount;
        public Customer(String name, int orderCount, int unpickedCount) {
            this.name = name;
            this.orderCount = orderCount;
            this.unpickedCount = unpickedCount;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // 从部门列表页面返回，重新加载数据
            Log.d("CustomerList", "从部门列表页面返回，重新加载数据");
            initData();
        } else if (requestCode == 1002) {
            // 从出库页面返回，刷新客户列表并清除选中状态
            Log.d("CustomerList", "从出库页面返回，刷新数据并清除选中状态");
            
            // 清除选中状态
            selectedIndexes.clear();
            if (adapter != null) {
                adapter.clearSelection();
                Log.d("CustomerList", "已清除适配器选中状态");
            }
            
            // 隐藏"下一步"按钮
            btnNext.setVisibility(View.GONE);
            Log.d("CustomerList", "已隐藏下一步按钮");
            
            // 重新加载数据
            Log.d("CustomerList", "开始重新加载客户数据");
            initData();
        }
    }
} 