package com.swolo.lpy.pysx.main.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfEntity;
import com.swolo.lpy.pysx.main.modal.NxDistributerGoodsShelfGoodsEntity;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class StockOutPresenterImpl implements MainContract.StockOutPresenter {
    private static final String TAG = "StockOutPresenter";
    private MainContract.StockOutView mView;

    public StockOutPresenterImpl(MainContract.StockOutView mView) {
        this.mView = mView;
    }

    @Override
    public void getStockGoods(Integer disId, Integer goodsType) {
        if (mView != null) {
            mView.showLoading();
        }
        
        Log.d(TAG, "开始获取库存商品数据: disId=" + disId + ", goodsType=" + goodsType);
        
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).pickerGetStockGoodsKf(disId, goodsType),
                new com.google.gson.reflect.TypeToken<List<NxDistributerGoodsShelfEntity>>() {}
            )
            .subscribeOn(rx.schedulers.Schedulers.io())
            .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe(new rx.Subscriber<List<NxDistributerGoodsShelfEntity>>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "数据加载完成");
                    if (mView != null) {
                        mView.hideLoading();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "加载失败: " + e.getMessage());
                    if (mView != null) {
                        mView.hideLoading();
                        mView.getStockGoodsFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(List<NxDistributerGoodsShelfEntity> result) {
                    Log.d(TAG, "接口原始数据: " + new Gson().toJson(result));
                    if (mView != null) {
                        if (result != null && !result.isEmpty()) {
                            Log.d(TAG, "获取到货架数据，数量: " + result.size());
                            // 检查每个商品的订单数据
                            for (NxDistributerGoodsShelfEntity shelf : result) {
                                if (shelf.getNxDisGoodsShelfGoodsEntities() != null) {
                                    for (NxDistributerGoodsShelfGoodsEntity goods : shelf.getNxDisGoodsShelfGoodsEntities()) {
                                        Log.d(TAG, "商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName() + 
                                              ", 订单数量: " + (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
                                              goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : 0));
                                    }
                                }
                            }
                            mView.getStockGoodsSuccess(result);
                        } else {
                            mView.getStockGoodsFail("没有获取到数据");
                        }
                    }
                }
            });
    }

    @Override
    public void giveOrderWeightListForStockAndFinish(List<NxDepartmentOrdersEntity> orderList) {
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).giveOrderWeightListForStockAndFinish(orderList),
                new com.google.gson.reflect.TypeToken<String>() {}
            )
            .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(rx.schedulers.Schedulers.io())
            .subscribe(new rx.Subscriber<String>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    if (mView != null) {
                        mView.onStockOutFinishFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(String s) {
                    if (mView != null) {
                        mView.onStockOutFinishSuccess();
                    }
                }
            });
    }

    @Override
    public void giveOrderWeightListForStockAndFinish(List<NxDepartmentOrdersEntity> orderList, MainContract.SaveCallback callback) {
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).giveOrderWeightListForStockAndFinish(orderList),
                new com.google.gson.reflect.TypeToken<String>() {}
            )
            .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
            .subscribeOn(rx.schedulers.Schedulers.io())
            .subscribe(new rx.Subscriber<String>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    if (callback != null) callback.onSaveFail(e.getMessage());
                }

                @Override
                public void onNext(String s) {
                    if (callback != null) callback.onSaveSuccess();
                }
            });
    }

    @Override
    public void getStockGoodsWithDepIds(Context context, int nxDepId, int gbDepId, int disId, int goodsType) {
        if (mView != null) {
            mView.showLoading();
        }
        
        Log.d(TAG, "开始获取带部门ID的库存商品数据: nxDepId=" + nxDepId + ", gbDepId=" + gbDepId + 
              ", disId=" + disId + ", goodsType=" + goodsType);
        
        // 从缓存中获取所有选中的部门ID
        SharedPreferences sp = context.getSharedPreferences("department_cache", Context.MODE_PRIVATE);
        String nxIdsJson = sp.getString("selectedNxIds", "[]");
        String gbIdsJson = sp.getString("selectedGbIds", "[]");
        
        // 解析JSON字符串获取ID列表
        List<Integer> nxIds = new Gson().fromJson(nxIdsJson, new TypeToken<List<Integer>>(){}.getType());
        List<Integer> gbIds = new Gson().fromJson(gbIdsJson, new TypeToken<List<Integer>>(){}.getType());
        
        // 将ID列表转换为逗号分隔的字符串
        String nxDepIds = nxIds != null && !nxIds.isEmpty() ? 
            TextUtils.join(",", nxIds) : "0";
        String gbDepIds = gbIds != null && !gbIds.isEmpty() ? 
            TextUtils.join(",", gbIds) : "0";
            
        Log.d(TAG, "准备请求数据: nxDepIds=" + nxDepIds + ", gbDepIds=" + gbDepIds);
        
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).pickerGetToStockGoodsWithDepIdsKf(
                    nxDepIds, gbDepIds, disId, goodsType),
                new com.google.gson.reflect.TypeToken<List<NxDistributerGoodsShelfEntity>>() {}
            )
            .subscribeOn(rx.schedulers.Schedulers.io())
            .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe(new rx.Subscriber<List<NxDistributerGoodsShelfEntity>>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "数据加载完成");
                    if (mView != null) {
                        mView.hideLoading();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "加载失败: " + e.getMessage());
                    if (mView != null) {
                        mView.hideLoading();
                        mView.getStockGoodsFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(List<NxDistributerGoodsShelfEntity> result) {
                    Log.d(TAG, "接口原始数据: " + new Gson().toJson(result));
                    if (mView != null) {
                        if (result != null && !result.isEmpty()) {
                            Log.d(TAG, "获取到货架数据，数量: " + result.size());
                            // 检查每个商品的订单数据
                            for (NxDistributerGoodsShelfEntity shelf : result) {
                                if (shelf.getNxDisGoodsShelfGoodsEntities() != null) {
                                    for (NxDistributerGoodsShelfGoodsEntity goods : shelf.getNxDisGoodsShelfGoodsEntities()) {
                                        Log.d(TAG, "商品: " + goods.getNxDistributerGoodsEntity().getNxDgGoodsName() + 
                                              ", 订单数量: " + (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null ? 
                                              goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities().size() : 0));
                                        // 详细记录订单数据
                                        if (goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities() != null) {
                                            for (NxDepartmentOrdersEntity order : goods.getNxDistributerGoodsEntity().getNxDepartmentOrdersEntities()) {
                                                Log.d(TAG, "订单详情: " +
                                                    "部门=" + (order.getNxDepartmentEntity() != null ? 
                                                        order.getNxDepartmentEntity().getNxDepartmentName() : "null") +
                                                    ", 数量=" + order.getNxDoQuantity() +
                                                    ", 单位=" + order.getNxDoStandard());
                                            }
                                        }
                                    }
                                }
                            }
                            mView.getStockGoodsSuccess(result);
                        } else {
                            mView.getStockGoodsFail("没有获取到数据");
                        }
                    }
                }
            });
    }
}
