package com.swolo.lpy.pysx.main.modal;

import java.util.List;

/**
 * 客户出库页面数据响应模型
 * 对应小程序 stockerGetToStockGoodsWithDepIdsKf 接口返回数据
 */
public class StockGoodsWithDepIdsResponse {
    
    /**
     * 货架列表
     */
    private List<NxDistributerGoodsShelfEntity> shelfArr;
    
    public List<NxDistributerGoodsShelfEntity> getShelfArr() {
        return shelfArr;
    }
    
    public void setShelfArr(List<NxDistributerGoodsShelfEntity> shelfArr) {
        this.shelfArr = shelfArr;
    }
} 