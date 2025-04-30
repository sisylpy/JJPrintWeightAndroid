package com.swolo.lpy.pysx.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.adapter.DepartmentAdapter;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.presenter.MainContract;
import com.swolo.lpy.pysx.main.presenter.DepartmentListPresenterImpl;
import com.swolo.lpy.pysx.ui.view.CommonLoadingDialog;

import java.util.ArrayList;
import java.util.List;

public class DepartmentListActivity extends AppCompatActivity implements MainContract.DepartmentListView {
    private static final String TAG = "DepartmentListActivity";
    
    private RecyclerView rvDepartments;
    private TextView tvNoData;
    private DepartmentAdapter adapter;
    private DepartmentListPresenterImpl presenter;
    private Integer disId;
    private Integer goodsType;
    private List<NxDepartmentEntity> selectedNxDeps;
    private List<GbDepartmentEntity> selectedGbDeps;
    private CommonLoadingDialog mLoadingDialog;

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
        
        mLoadingDialog = new CommonLoadingDialog(this);
        
        try {
            // 初始化视图
            initViews();
            
            // 从缓存恢复选中状态
            restoreSelectionFromCache();
            
            // 初始化数据和事件
            initData();
            
        } catch (Exception e) {
            Log.e(TAG, "初始化失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        // 初始化RecyclerView
        rvDepartments = findViewById(R.id.rv_departments);
        if (rvDepartments == null) {
            throw new RuntimeException("找不到部门RecyclerView");
        }
        rvDepartments.setLayoutManager(new LinearLayoutManager(this));
        rvDepartments.setVisibility(View.VISIBLE);
        
        // 初始化返回按钮
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setVisibility(View.VISIBLE); // 确保返回按钮可见
            btnBack.setOnClickListener(v -> {
                saveDepartmentSelection();
                setResult(RESULT_OK);
                finish();
            });
        }
        
        // 初始化NoData TextView
        tvNoData = findViewById(R.id.tv_no_data);
        if (tvNoData == null) {
            throw new RuntimeException("找不到NoData TextView");
        }
    }
    
    private void initData() {
        // 获取Intent参数
        if (getIntent() != null) {
            disId = getIntent().getIntExtra("disId", 0);
            goodsType = getIntent().getIntExtra("goodsType", -1);
        }
        
        // 初始化适配器
        adapter = new DepartmentAdapter();
        rvDepartments.setAdapter(adapter);
        
        // 设置点击监听器
        adapter.setOnItemClickListener(entity -> {
            if (entity instanceof NxDepartmentEntity) {
                // 选择内销部门时，取消所有国标部门的选择
                List<?> departments = adapter.getData();
                if (departments != null) {
                    for (Object dep : departments) {
                        if (dep instanceof GbDepartmentEntity) {
                            GbDepartmentEntity gbDep = (GbDepartmentEntity) dep;
                            gbDep.setSelected(false);
                        }
                    }
                }
                
                NxDepartmentEntity nxDep = (NxDepartmentEntity) entity;
                Boolean currentSelected = nxDep.getSelected();
                nxDep.setSelected(currentSelected == null ? true : !currentSelected);
            } else if (entity instanceof GbDepartmentEntity) {
                // 选择国标部门时，取消所有内销部门的选择
                List<?> departments = adapter.getData();
                if (departments != null) {
                    for (Object dep : departments) {
                        if (dep instanceof NxDepartmentEntity) {
                            NxDepartmentEntity nxDep = (NxDepartmentEntity) dep;
                            nxDep.setSelected(false);
                        }
                    }
                }
                
                GbDepartmentEntity gbDep = (GbDepartmentEntity) entity;
                Boolean currentSelected = gbDep.getSelected();
                gbDep.setSelected(currentSelected == null ? true : !currentSelected);
            }
            adapter.notifyDataSetChanged();
        });
        
        // 初始化Presenter并加载数据
        presenter = new DepartmentListPresenterImpl(this);
        showLoading();
        presenter.getDepartmentList(disId, goodsType);
    }
    
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showLoading() {
        if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void stopLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void getDepartmentListSuccess(List<NxDepartmentEntity> nxDeps, List<GbDepartmentEntity> gbDeps) {
        stopLoading();
        
        List<Object> allDepartments = new ArrayList<>();
        if (nxDeps != null) {
            allDepartments.addAll(nxDeps);
        }
        if (gbDeps != null) {
            allDepartments.addAll(gbDeps);
        }

        if (allDepartments.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            rvDepartments.setVisibility(View.GONE);
        } else {
            tvNoData.setVisibility(View.GONE);
            rvDepartments.setVisibility(View.VISIBLE);
            adapter.setData(allDepartments);
        }
        
        // 数据加载完成后恢复选中状态
        if (selectedNxDeps != null && !selectedNxDeps.isEmpty() && nxDeps != null) {
            for (NxDepartmentEntity dep : nxDeps) {
                for (NxDepartmentEntity selected : selectedNxDeps) {
                    if (dep.getNxDepartmentId().equals(selected.getNxDepartmentId())) {
                        dep.setSelected(true);
                    }
                }
            }
        }

        if (selectedGbDeps != null && !selectedGbDeps.isEmpty() && gbDeps != null) {
            for (GbDepartmentEntity dep : gbDeps) {
                for (GbDepartmentEntity selected : selectedGbDeps) {
                    if (dep.getGbDepartmentId().equals(selected.getGbDepartmentId())) {
                        dep.setSelected(true);
                    }
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }

    @Override
    public void getDepartmentListFail(String error) {
        stopLoading();
        tvNoData.setVisibility(View.VISIBLE);
        rvDepartments.setVisibility(View.GONE);
        showToast(error);
    }

    private void saveSelectionToCache() {
        SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        List<Integer> nxIds = new ArrayList<>();
        List<String> nxNames = new ArrayList<>();
        List<Integer> gbIds = new ArrayList<>();
        List<String> gbNames = new ArrayList<>();
        
        if (adapter != null && adapter.getData() != null) {
            for (Object dep : adapter.getData()) {
                if (dep instanceof NxDepartmentEntity) {
                    NxDepartmentEntity nxDep = (NxDepartmentEntity) dep;
                    if (nxDep.getSelected() != null && nxDep.getSelected()) {
                        nxIds.add(nxDep.getNxDepartmentId());
                        nxNames.add(nxDep.getNxDepartmentName());
                    }
                } else if (dep instanceof GbDepartmentEntity) {
                    GbDepartmentEntity gbDep = (GbDepartmentEntity) dep;
                    if (gbDep.getSelected() != null && gbDep.getSelected()) {
                        gbIds.add(gbDep.getGbDepartmentId());
                        gbNames.add(gbDep.getGbDepartmentName());
                    }
                }
            }
        }

        editor.putString("selectedNxIds", new Gson().toJson(nxIds));
        editor.putString("selectedNxNames", new Gson().toJson(nxNames));
        editor.putString("selectedGbIds", new Gson().toJson(gbIds));
        editor.putString("selectedGbNames", new Gson().toJson(gbNames));
        editor.commit();
    }

    private void restoreSelectionFromCache() {
        SharedPreferences sp = getSharedPreferences("department_cache", MODE_PRIVATE);
        
        String nxIdsJson = sp.getString("selectedNxIds", "[]");
        String nxNamesJson = sp.getString("selectedNxNames", "[]");
        String gbIdsJson = sp.getString("selectedGbIds", "[]");
        String gbNamesJson = sp.getString("selectedGbNames", "[]");

        List<Integer> nxIds = new Gson().fromJson(nxIdsJson, new TypeToken<List<Integer>>(){}.getType());
        List<String> nxNames = new Gson().fromJson(nxNamesJson, new TypeToken<List<String>>(){}.getType());
        List<Integer> gbIds = new Gson().fromJson(gbIdsJson, new TypeToken<List<Integer>>(){}.getType());
        List<String> gbNames = new Gson().fromJson(gbNamesJson, new TypeToken<List<String>>(){}.getType());

        selectedNxDeps = new ArrayList<>();
        for (int i = 0; i < nxIds.size(); i++) {
            NxDepartmentEntity dep = new NxDepartmentEntity();
            dep.setNxDepartmentId(nxIds.get(i));
            if (i < nxNames.size()) {
                dep.setNxDepartmentName(nxNames.get(i));
            }
            selectedNxDeps.add(dep);
        }

        selectedGbDeps = new ArrayList<>();
        for (int i = 0; i < gbIds.size(); i++) {
            GbDepartmentEntity dep = new GbDepartmentEntity();
            dep.setGbDepartmentId(gbIds.get(i));
            if (i < gbNames.size()) {
                dep.setGbDepartmentName(gbNames.get(i));
            }
            selectedGbDeps.add(dep);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSelectionToCache();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveDepartmentSelection();
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveDepartmentSelection();
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void saveDepartmentSelection() {
        saveSelectionToCache();
        setResult(RESULT_OK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter = null;
        }
        if (adapter != null) {
            adapter.setOnItemClickListener(null);
            adapter = null;
        }
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }
}