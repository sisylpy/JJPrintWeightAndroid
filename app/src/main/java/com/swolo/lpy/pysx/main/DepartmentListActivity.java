package com.swolo.lpy.pysx.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.adapter.DepartmentAdapter;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.presenter.DepartmentListPresenterImpl;
import com.swolo.lpy.pysx.main.presenter.MainContract;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.view.ViewGroup;

/**
 * 部门列表页面 - 美化版本
 * 
 * 【功能特性】
 * 1. 现代化UI设计，包含顶部栏、搜索框
 * 2. 实时搜索功能，支持部门名称模糊匹配
 * 3. 优雅的加载状态和错误处理
 * 4. 流畅的动画效果和交互反馈
 * 5. 完善的空状态提示
 * 
 * 【交互体验】
 * - 搜索框实时过滤部门列表
 * - 点击部门项有视觉反馈
 * - 加载时显示进度提示
 * - 错误时显示友好提示
 */
public class DepartmentListActivity extends AppCompatActivity implements MainContract.DepartmentListView {
    private static final String TAG = "DepartmentListActivity";
    
    // UI组件
    private RecyclerView rvDepartments;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private EditText etSearch;
    
    // 数据和适配器
    private DepartmentAdapter adapter;
    private DepartmentListPresenterImpl presenter;
    private Integer disId;
    
    // 数据管理
    private List<NxDepartmentEntity> allNxDeps = new ArrayList<>();
    private List<GbDepartmentEntity> allGbDeps = new ArrayList<>();
    private List<Object> filteredDepartments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_base);
        // 设置顶部栏标题
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) tvTitle.setText("出库部门");
        // 隐藏右侧所有按钮
        int[] rightBtnIds = {R.id.btn_more, R.id.btn_circle, R.id.btn_settings, R.id.btn_right};
        for (int id : rightBtnIds) {
            View btn = findViewById(id);
            if (btn != null) btn.setVisibility(View.GONE);
        }
        // inflate 内容区
        View contentContainer = findViewById(R.id.content_container);
        View content = getLayoutInflater().inflate(R.layout.department_content, (ViewGroup) contentContainer, false);
        ((ViewGroup) contentContainer).addView(content);
        Log.d(TAG, "onCreate - 部门列表页面启动");
        
        try {
            // 初始化视图
            initViews();
            // 初始化数据和事件
            initData();
            // 绑定事件
            bindEvents();
            
        } catch (Exception e) {
            Log.e(TAG, "初始化失败", e);
            showError("页面初始化失败: " + e.getMessage());
            finish();
        }
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews() {
        Log.d(TAG, "开始初始化视图组件");
        
        // 返回按钮
        ImageView btnBack = findViewById(R.id.iv_avatar);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finishWithResult());
        }
        
        // 初始化搜索框
        etSearch = findViewById(R.id.et_search);
        
        // 初始化RecyclerView
        rvDepartments = findViewById(R.id.rv_departments);
        if (rvDepartments == null) {
            throw new RuntimeException("找不到部门列表RecyclerView");
        }
        rvDepartments.setLayoutManager(new LinearLayoutManager(this));
        
        // 添加分割线装饰器
        androidx.recyclerview.widget.DividerItemDecoration dividerItemDecoration = 
            new androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_bright));
        rvDepartments.addItemDecoration(dividerItemDecoration);
        
        // 初始化空状态组件
        llNoData = findViewById(R.id.ll_no_data);
        tvNoData = findViewById(R.id.tv_no_data);
        
        if (llNoData == null || tvNoData == null) {
            throw new RuntimeException("找不到空状态组件");
        }
        
        Log.d(TAG, "视图组件初始化完成");
    }
    
    /**
     * 初始化数据和适配器
     */
    private void initData() {
        Log.d(TAG, "开始初始化数据");
        
        // 获取Intent参数
        if (getIntent() != null) {
            disId = getIntent().getIntExtra("disId", 0);
            Log.d(TAG, "获取到分销商ID: " + disId);
        }
        
        // 初始化适配器
        adapter = new DepartmentAdapter();
        rvDepartments.setAdapter(adapter);
        
        // 设置部门点击事件
        adapter.setOnItemClickListener(new DepartmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object entity) {
                handleDepartmentClick(entity);
            }
        });
        
        // 初始化Presenter并加载数据
        presenter = new DepartmentListPresenterImpl(this);
        showLoading();
        presenter.getDepartmentList(disId, null);
        
        Log.d(TAG, "数据初始化完成");
    }
    
    /**
     * 绑定事件监听器
     */
    private void bindEvents() {
        Log.d(TAG, "开始绑定事件");
        
        // 注意：已移除返回按钮相关代码
        
        // 搜索框文本变化监听
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String searchText = s.toString().trim();
                    Log.d(TAG, "搜索文本变化: " + searchText);
                    filterDepartments(searchText);
                }
            });
        }
        
        Log.d(TAG, "事件绑定完成");
    }
    
    /**
     * 处理部门点击事件
     */
    private void handleDepartmentClick(Object entity) {
        try {
            if (entity instanceof NxDepartmentEntity) {
                // 内销部门处理
                NxDepartmentEntity nxDep = (NxDepartmentEntity) entity;
                String depName = nxDep.getNxDepartmentName();
                Integer depId = nxDep.getNxDepartmentId();
                Log.d(TAG, "点击内销部门: " + depName + ", ID: " + depId);
                showToast("已选择: " + depName);
                // 跳转到新极简部门出库页面
                Intent intent = new Intent(this, DepOutOrderSimpleActivity.class);
                intent.putExtra("depName", depName);
                intent.putExtra("depFatherId", String.valueOf(depId));
                intent.putExtra("gbDepFatherId", "-1");
                intent.putExtra("resFatherId", "-1");
                startActivity(intent);
            } else if (entity instanceof GbDepartmentEntity) {
                // 国标部门处理
                GbDepartmentEntity gbDep = (GbDepartmentEntity) entity;
                String depName = gbDep.getGbDepartmentName();
                Integer depId = gbDep.getGbDepartmentId();
                Log.d(TAG, "点击国标部门: " + depName + ", ID: " + depId);
                showToast("已选择: " + depName);
                // 跳转到新极简部门出库页面
                Intent intent = new Intent(this, DepOutOrderSimpleActivity.class);
                intent.putExtra("depName", depName);
                intent.putExtra("depFatherId", "-1");
                intent.putExtra("gbDepFatherId", String.valueOf(depId));
                intent.putExtra("resFatherId", "-1");
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "处理部门点击事件失败", e);
            showError("处理部门选择失败: " + e.getMessage());
        }
    }
    
    /**
     * 过滤部门列表
     */
    private void filterDepartments(String searchText) {
        Log.d(TAG, "开始过滤部门，搜索文本: " + searchText);
        
        filteredDepartments.clear();
        
        if (searchText.isEmpty()) {
            // 搜索框为空，显示所有部门
            if (allNxDeps != null) filteredDepartments.addAll(allNxDeps);
            if (allGbDeps != null) filteredDepartments.addAll(allGbDeps);
        } else {
            // 根据搜索文本过滤
            String lowerSearchText = searchText.toLowerCase();
            
            // 过滤内销部门
            if (allNxDeps != null) {
                for (NxDepartmentEntity dep : allNxDeps) {
                    if (dep.getNxDepartmentName() != null && 
                        dep.getNxDepartmentName().toLowerCase().contains(lowerSearchText)) {
                        filteredDepartments.add(dep);
                    }
                }
            }
            
            // 过滤国标部门
            if (allGbDeps != null) {
                for (GbDepartmentEntity dep : allGbDeps) {
                    if (dep.getGbDepartmentName() != null && 
                        dep.getGbDepartmentName().toLowerCase().contains(lowerSearchText)) {
                        filteredDepartments.add(dep);
                    }
                }
            }
        }
        
        // 更新UI
        updateDepartmentList();
        Log.d(TAG, "过滤完成，结果数量: " + filteredDepartments.size());
    }
    
    /**
     * 更新部门列表显示
     */
    private void updateDepartmentList() {
        if (filteredDepartments.isEmpty()) {
            // 显示空状态
            rvDepartments.setVisibility(View.GONE);
            llNoData.setVisibility(View.VISIBLE);
            
            // 根据是否有搜索文本来显示不同的提示
            String searchText = etSearch != null ? etSearch.getText().toString().trim() : "";
            if (!searchText.isEmpty()) {
                tvNoData.setText("未找到匹配的部门");
            } else {
                tvNoData.setText("暂无部门数据");
            }
        } else {
            // 显示部门列表
            rvDepartments.setVisibility(View.VISIBLE);
            llNoData.setVisibility(View.GONE);
            adapter.setData(filteredDepartments);
        }
    }
    
    /**
     * 显示加载状态
     */
    private void showLoading() {
        Log.d(TAG, "显示加载状态");
        rvDepartments.setVisibility(View.GONE);
        llNoData.setVisibility(View.VISIBLE);
        tvNoData.setText("正在加载部门数据...");
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        Log.e(TAG, "显示错误: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 显示提示信息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 完成页面并返回结果
     */
    private void finishWithResult() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void getDepartmentListSuccess(List<NxDepartmentEntity> nxDeps, List<GbDepartmentEntity> gbDeps) {
        Log.d(TAG, "获取部门列表成功");
        
        // 保存所有数据
        allNxDeps.clear();
        allGbDeps.clear();
        if (nxDeps != null) allNxDeps.addAll(nxDeps);
        if (gbDeps != null) allGbDeps.addAll(gbDeps);
        
        Log.d(TAG, "内销部门数量: " + allNxDeps.size() + ", 国标部门数量: " + allGbDeps.size());
        
        // 初始化过滤列表
        filteredDepartments.clear();
        filteredDepartments.addAll(allNxDeps);
        filteredDepartments.addAll(allGbDeps);
        
        // 更新UI
        updateDepartmentList();
        
        // 显示成功提示
        if (filteredDepartments.size() > 0) {
            showToast("加载完成，共 " + filteredDepartments.size() + " 个部门");
        }
    }

    @Override
    public void getDepartmentListFail(String error) {
        Log.e(TAG, "获取部门列表失败: " + error);
        
        // 显示错误状态
        rvDepartments.setVisibility(View.GONE);
        llNoData.setVisibility(View.VISIBLE);
        tvNoData.setText("加载失败: " + error);
        
        // 显示错误提示
        showError("获取部门列表失败: " + error);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishWithResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "页面销毁");
        
        // 清理资源
        if (presenter != null) {
            presenter = null;
        }
        if (adapter != null) {
            adapter.setOnItemClickListener(null);
            adapter = null;
        }
    }
}