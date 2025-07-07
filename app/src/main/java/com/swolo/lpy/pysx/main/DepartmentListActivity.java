package com.swolo.lpy.pysx.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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

public class DepartmentListActivity extends AppCompatActivity implements MainContract.DepartmentListView {
    private static final String TAG = "DepartmentListActivity";
    private RecyclerView rvDepartments;
    private TextView tvNoData;
    private DepartmentAdapter adapter;
    private DepartmentListPresenterImpl presenter;
    private Integer disId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_list);
        Log.d(TAG, "onCreate");
        
        // 设置ActionBar的返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        try {
            // 初始化视图
            initViews();
            initData();
            
        } catch (Exception e) {
            Log.e(TAG, "初始化失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        Log.d(TAG, "开始初始化视图");
        
        // 初始化RecyclerView
        rvDepartments = findViewById(R.id.rv_departments);
        if (rvDepartments == null) {
            Log.e(TAG, "找不到rv_departments RecyclerView");
            throw new RuntimeException("找不到rv_departments RecyclerView");
        }
        Log.d(TAG, "成功找到rv_departments RecyclerView");
        rvDepartments.setLayoutManager(new LinearLayoutManager(this));
        
        // 初始化NoData TextView
        tvNoData = findViewById(R.id.tv_no_data);
        if (tvNoData == null) {
            Log.e(TAG, "找不到tv_no_data TextView");
            throw new RuntimeException("找不到tv_no_data TextView");
        }
        Log.d(TAG, "成功找到tv_no_data TextView");
        
        // 初始化标题
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("部门列表");
        }
        Log.d(TAG, "视图初始化完成");
    }
    
    private void initData() {
        if (getIntent() != null) {
            disId = getIntent().getIntExtra("disId", 0);
        }
        adapter = new DepartmentAdapter();
        rvDepartments.setAdapter(adapter);
        adapter.setOnItemClickListener(new DepartmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object entity) {
            if (entity instanceof NxDepartmentEntity) {
                    // 内销部门：depFatherId传实际ID，gbDepFatherId传-1
                    NxDepartmentEntity nxDep = (NxDepartmentEntity) entity;
                    String depFatherId = String.valueOf(nxDep.getNxDepartmentId());
                    String gbDepFatherId = "-1";
                    String resFatherId = "-1";
                    String depName = nxDep.getNxDepartmentName();
                    
                    Log.d(TAG, "点击内销部门: " + depName + ", depFatherId=" + depFatherId);
                    
                    Intent intent = new Intent(DepartmentListActivity.this, DepOutOrderActivity.class);
                    intent.putExtra("depFatherId", depFatherId);
                    intent.putExtra("gbDepFatherId", gbDepFatherId);
                    intent.putExtra("resFatherId", resFatherId);
                    intent.putExtra("depName", depName);
                    startActivity(intent);
            } else if (entity instanceof GbDepartmentEntity) {
                    // 国标部门：depFatherId传-1，gbDepFatherId传实际ID
                    GbDepartmentEntity gbDep = (GbDepartmentEntity) entity;
                    String depFatherId = "-1";
                    String gbDepFatherId = String.valueOf(gbDep.getGbDepartmentFatherId());
                    String resFatherId = "-1";
                    String depName = gbDep.getGbDepartmentName();
                    
                    Log.d(TAG, "点击国标部门: " + depName + ", gbDepFatherId=" + gbDepFatherId);
                    
                    Intent intent = new Intent(DepartmentListActivity.this, DepOutOrderActivity.class);
                    intent.putExtra("depFatherId", depFatherId);
                    intent.putExtra("gbDepFatherId", gbDepFatherId);
                    intent.putExtra("resFatherId", resFatherId);
                    intent.putExtra("depName", depName);
                    startActivity(intent);
                }
            }
        });
        presenter = new DepartmentListPresenterImpl(this);
        presenter.getDepartmentList(disId, null);
    }

    @Override
    public void getDepartmentListSuccess(List<NxDepartmentEntity> nxDeps, List<GbDepartmentEntity> gbDeps) {
        List<Object> allDepartments = new ArrayList<>();
        if (nxDeps != null) allDepartments.addAll(nxDeps);
        if (gbDeps != null) allDepartments.addAll(gbDeps);
        if (allDepartments.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvDepartments.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvDepartments.setVisibility(View.VISIBLE);
            adapter.setData(allDepartments);
        }
    }

    @Override
    public void getDepartmentListFail(String error) {
        tvNoData.setVisibility(View.VISIBLE);
        rvDepartments.setVisibility(View.GONE);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}