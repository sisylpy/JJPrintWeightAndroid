package com.swolo.lpy.pysx.api;


import android.content.Intent;

import com.swolo.lpy.pysx.http.CommonResponse;
import com.swolo.lpy.pysx.main.modal.OrderWeightRequest;
import com.swolo.lpy.pysx.main.modal.StockGoodsWithDepIdsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;
import rx.Observable;

import java.util.List;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public interface GoodsApi {



    @FormUrlEncoded
    @POST("api/nxrestrauntorders/weighingGetOrderGoodsType")
    Observable<CommonResponse> weighingGetOrderGoodsType(@Field("comId") String comId,
                                              @Field("type") String type);

    @FormUrlEncoded
    @POST("api/nxrestrauntorders/weighingGetGoods")
    Observable<CommonResponse> weighingGetGoods(@Field("fatherId") String fatherId,
                                                @Field("type")String type);


    @FormUrlEncoded
    @POST("api/nxdepartmentorders/pickerGetStockGoodsKf")
    Observable<CommonResponse> pickerGetStockGoodsKf(
            @Field("disId") Integer disId,
            @Field("goodsType") Integer goodsType
    );

    @POST("api/nxdepartmentorders/giveOrderWeightListForStockAndFinish")
    Observable<CommonResponse> giveOrderWeightListForStockAndFinish(@Body java.util.List<com.swolo.lpy.pysx.main.modal.NxDepartmentOrdersEntity> orderList);



    @GET("api/nxdistributeruser/disAndroidLoginWork/{phone}")
    Observable<CommonResponse> disAndroidLoginWork(@Path("phone") String phone);

    @FormUrlEncoded
    @POST("api/nxdepartmentorders/disGetWaitStockGoodsDeps")
    Observable<CommonResponse> disGetWaitStockGoodsDeps(
        @Field("disId") Integer disId,
        @Field("goodsType") Integer goodsType
    );

    @FormUrlEncoded
    @POST("api/nxdepartmentorders/pickerGetToStockGoodsWithDepIdsKf")
    Observable<CommonResponse> pickerGetToStockGoodsWithDepIdsKf(
            @Field("nxDepIds") String nxDepIds,
            @Field("gbDepIds") String gbDepIds,
            @Field("disId") Integer disId,
            @Field("goodsType") Integer goodsType
    );

    /**
     * 客户出库页面 - 获取货架和商品数据
     * 对应小程序 stockerGetToStockGoodsWithDepIdsKf 接口
     */
    @FormUrlEncoded
    @POST("api/nxdepartmentorders/stockerGetToStockGoodsWithDepIdsKf")
    Observable<CommonResponse> stockerGetToStockGoodsWithDepIdsKf(
            @Field("nxDepIds") String nxDepIds,
            @Field("gbDepIds") String gbDepIds,
            @Field("nxDisId") Integer nxDisId
    );

    /**
     * 客户出库页面 - 确认出库
     * 对应小程序 giveOrderWeightListForStockAndFinish 接口
     */
    @POST("api/nxdepartmentorders/giveOrderWeightListForStockAndFinish")
    Observable<CommonResponse> confirmStockOut(@Body List<OrderWeightRequest> orders);

    /**
     * 部门列表页面 - 获取已完成出库的部门列表
     */
    @FormUrlEncoded
    @POST("api/nxdepartmentorders/stockerGetFinishStockGoodsDeps")
    Observable<CommonResponse> stockerGetFinishStockGoodsDeps(
        @Field("disId") Integer disId
    );

    /**
     * 部门出库订单页面 - 获取已拣货订单数据
     * 对应小程序 stockerGetHaveOutCataGoods 接口
     */
    @FormUrlEncoded
    @POST("api/nxdepartmentorders/stockerGetHaveOutCataGoods")
    Observable<CommonResponse> stockerGetHaveOutCataGoods(
        @Field("depFatherId") String depFatherId,
        @Field("gbDepFatherId") String gbDepFatherId,
        @Field("resFatherId") String resFatherId
    );

    /**
     * 部门出库订单页面 - 获取未拣货订单数据
     * 对应小程序 stokerHaveNotOutCataGoods 接口
     */
    @FormUrlEncoded
    @POST("api/nxdepartmentorders/stokerHaveNotOutCataGoods")
    Observable<CommonResponse> stokerHaveNotOutCataGoods(
        @Field("depFatherId") String depFatherId,
        @Field("gbDepFatherId") String gbDepFatherId,
        @Field("resFatherId") String resFatherId
    );

    /**
     * 部门出库订单页面 - 删除内销订单
     * 对应小程序 cancelOutOrder 接口
     */
    @POST("api/nxdepartmentorders/cancelOutOrder")
    Observable<CommonResponse> cancelOutOrder(@Body Object order);

    /**
     * 部门出库订单页面 - 删除国标订单
     * 对应小程序 cancleGbOrderSx 接口
     */
    @FormUrlEncoded
    @POST("api/nxdepartmentorders/cancleGbOrderSx")
    Observable<CommonResponse> cancleGbOrderSx(@Field("id") String id);

}
