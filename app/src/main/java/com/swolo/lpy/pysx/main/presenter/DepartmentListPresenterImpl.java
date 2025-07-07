package com.swolo.lpy.pysx.main.presenter;

import android.util.Log;
import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.http.CommonResponse;
import com.swolo.lpy.pysx.main.modal.NxDepartmentEntity;
import com.swolo.lpy.pysx.main.modal.GbDepartmentEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DepartmentListPresenterImpl implements MainContract.DepartmentListPresenter {
    private static final String TAG = "DepartmentListPresenter";
    private MainContract.DepartmentListView mView;
    private Gson gson;

    public DepartmentListPresenterImpl(MainContract.DepartmentListView mView) {
        this.mView = mView;
        this.gson = new Gson();
    }

    @Override
    public void getDepartmentList(Integer disId, Integer goodsType) {
        Log.d(TAG, "开始获取部门列表: disId=" + disId);
        
        HttpManager.getInstance()
            .getApi(GoodsApi.class)
            .stockerGetFinishStockGoodsDeps(disId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response != null && response.data != null) {
                    try {
                        // 打印原始响应数据
                        Log.d(TAG, "原始响应数据: " + response.toString());
                        Log.d(TAG, "响应code: " + response.code);
                        Log.d(TAG, "响应msg: " + response.msg);
                        Log.d(TAG, "响应data: " + response.data.toString());
                        
                        // 转换为JSON字符串并打印
                        String jsonStr = gson.toJson(response.data);
                        Log.d(TAG, "转换后的JSON数据: " + jsonStr);
                        
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        Log.d(TAG, "JSON对象包含的key: " + jsonObject.keys());
                        
                        // 解析内销部门列表
                        List<NxDepartmentEntity> nxDeps = new ArrayList<>();
                        if (jsonObject.has("nxDep")) {
                            JSONArray nxArray = jsonObject.getJSONArray("nxDep");
                            Log.d(TAG, "内销部门JSON数组: " + nxArray.toString());
                            nxDeps = gson.fromJson(nxArray.toString(), 
                                new TypeToken<List<NxDepartmentEntity>>(){}.getType());
                            Log.d(TAG, "成功解析内销部门数量: " + nxDeps.size());
                        } else {
                            Log.d(TAG, "响应中没有nxDep字段");
                        }
                        
                        // 解析国标部门列表
                        List<GbDepartmentEntity> gbDeps = new ArrayList<>();
                        if (jsonObject.has("gbDep")) {
                            JSONArray gbArray = jsonObject.getJSONArray("gbDep");
                            Log.d(TAG, "国标部门JSON数组: " + gbArray.toString());
                            gbDeps = gson.fromJson(gbArray.toString(), 
                                new TypeToken<List<GbDepartmentEntity>>(){}.getType());
                            Log.d(TAG, "成功解析国标部门数量: " + gbDeps.size());
                        } else {
                            Log.d(TAG, "响应中没有gbDep字段");
                        }
                        
                        Log.d(TAG, "解析完成: nxDeps=" + nxDeps.size() + ", gbDeps=" + gbDeps.size());
                        mView.getDepartmentListSuccess(nxDeps, gbDeps);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "解析部门数据失败", e);
                        mView.getDepartmentListFail("解析部门数据失败: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "获取部门列表失败: 响应数据为空");
                    mView.getDepartmentListFail("获取部门列表失败: 响应数据为空");
                }
            }, throwable -> {
                Log.e(TAG, "获取部门列表失败", throwable);
                mView.getDepartmentListFail("获取部门列表失败: " + throwable.getMessage());
            });
    }
} 