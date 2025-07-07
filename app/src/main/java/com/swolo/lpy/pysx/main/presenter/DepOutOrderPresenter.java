package com.swolo.lpy.pysx.main.presenter;

import android.util.Log;
import com.swolo.lpy.pysx.api.GoodsApi;
import com.swolo.lpy.pysx.http.HttpManager;
import com.swolo.lpy.pysx.main.modal.DepOutOrderResponse;
import com.google.gson.reflect.TypeToken;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 部门出库订单页面Presenter
 * 处理 stockerGetHaveOutCataGoods 和 stokerHaveNotOutCataGoods 接口调用
 */
public class DepOutOrderPresenter {
    private static final String TAG = "DepOutOrderPresenter";
    private DepOutOrderView mView;

    public interface DepOutOrderView {
        void showLoading();
        void hideLoading();
        void onGetHaveOutDataSuccess(DepOutOrderResponse response);
        void onGetHaveOutDataFail(String error);
        void onGetNotOutDataSuccess(DepOutOrderResponse response);
        void onGetNotOutDataFail(String error);
        void onDeleteOrderSuccess();
        void onDeleteOrderFail(String error);
    }

    public DepOutOrderPresenter(DepOutOrderView view) {
        this.mView = view;
    }

    /**
     * 获取已拣货订单数据
     */
    public void getHaveOutCataGoods(String depFatherId, String gbDepFatherId, String resFatherId) {
        if (mView != null) {
            mView.showLoading();
        }
        
        Log.d(TAG, "开始获取已拣货订单数据: depFatherId=" + depFatherId + 
              ", gbDepFatherId=" + gbDepFatherId + ", resFatherId=" + resFatherId);
        
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).stockerGetHaveOutCataGoods(
                    depFatherId, gbDepFatherId, resFatherId),
                new TypeToken<DepOutOrderResponse>() {}
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<DepOutOrderResponse>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "已拣货数据加载完成");
                    if (mView != null) {
                        mView.hideLoading();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "已拣货数据加载失败: " + e.getMessage());
                    if (mView != null) {
                        mView.hideLoading();
                        mView.onGetHaveOutDataFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(DepOutOrderResponse response) {
                    Log.d(TAG, "已拣货接口返回数据: " + response);
                    if (mView != null) {
                        if (response != null && response.arr != null) {
                            Log.d(TAG, "获取到已拣货数据，分组数量: " + response.arr.size() + 
                                  ", 未拣货: " + response.notCount + ", 已拣货: " + response.haveCount);
                            mView.onGetHaveOutDataSuccess(response);
                        } else {
                            mView.onGetHaveOutDataFail("没有获取到已拣货数据");
                        }
                    }
                }
            });
    }

    /**
     * 获取未拣货订单数据
     */
    public void getNotOutCataGoods(String depFatherId, String gbDepFatherId, String resFatherId) {
        if (mView != null) {
            mView.showLoading();
        }
        
        Log.d(TAG, "开始获取未拣货订单数据: depFatherId=" + depFatherId + 
              ", gbDepFatherId=" + gbDepFatherId + ", resFatherId=" + resFatherId);
        
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).stokerHaveNotOutCataGoods(
                    depFatherId, gbDepFatherId, resFatherId),
                new TypeToken<DepOutOrderResponse>() {}
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<DepOutOrderResponse>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "未拣货数据加载完成");
                    if (mView != null) {
                        mView.hideLoading();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "未拣货数据加载失败: " + e.getMessage());
                    if (mView != null) {
                        mView.hideLoading();
                        mView.onGetNotOutDataFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(DepOutOrderResponse response) {
                    Log.d(TAG, "未拣货接口返回数据: " + response);
                    if (mView != null) {
                        if (response != null && response.arr != null) {
                            Log.d(TAG, "获取到未拣货数据，分组数量: " + response.arr.size() + 
                                  ", 未拣货: " + response.notCount + ", 已拣货: " + response.haveCount);
                            mView.onGetNotOutDataSuccess(response);
                        } else {
                            mView.onGetNotOutDataFail("没有获取到未拣货数据");
                        }
                    }
                }
            });
    }

    /**
     * 删除内销订单
     */
    public void deleteOutOrder(Object order) {
        if (mView != null) {
            mView.showLoading();
        }
        
        Log.d(TAG, "开始删除内销订单: order=" + new com.google.gson.Gson().toJson(order));
        
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).cancelOutOrder(order),
                new TypeToken<String>() {}
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "删除内销订单完成");
                    if (mView != null) {
                        mView.hideLoading();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "删除内销订单失败: " + e.getMessage());
                    if (mView != null) {
                        mView.hideLoading();
                        mView.onDeleteOrderFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(String result) {
                    Log.d(TAG, "删除内销订单成功: " + result);
                    if (mView != null) {
                        mView.onDeleteOrderSuccess();
                    }
                }
            });
    }

    /**
     * 删除国标订单
     */
    public void deleteGbOrder(String id) {
        if (mView != null) {
            mView.showLoading();
        }
        
        Log.d(TAG, "开始删除国标订单: id=" + id);
        
        HttpManager.getInstance()
            .request(
                HttpManager.getInstance().getApi(GoodsApi.class).cancleGbOrderSx(id),
                new TypeToken<String>() {}
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG, "删除国标订单完成");
                    if (mView != null) {
                        mView.hideLoading();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "删除国标订单失败: " + e.getMessage());
                    if (mView != null) {
                        mView.hideLoading();
                        mView.onDeleteOrderFail(e.getMessage());
                    }
                }

                @Override
                public void onNext(String result) {
                    Log.d(TAG, "删除国标订单成功: " + result);
                    if (mView != null) {
                        mView.onDeleteOrderSuccess();
                    }
                }
            });
    }
} 