package com.swolo.lpy.pysx.main.modal;

import java.util.List;

/**
 * 按商品类别获取出库数据的响应类
 * 对应小程序中的 stokerGetToStockGoodsWithDepIds 接口返回数据
 */
public class StockGoodsWithCategoriesResponse {
    
    /**
     * 商品类别数组（左侧菜单）
     */
    private List<NxDistributerFatherGoodsEntity> grandArr;
    
    /**
     * 等待部门NX
     */
    private List<NxDepartmentEntity> waitDepNx;
    
    /**
     * 等待部门GB
     */
    private List<GbDepartmentEntity> waitDepGb;
    
    /**
     * 部门订单等待
     */
    private Integer depOrdersWait;
    
    /**
     * ID部门订单等待
     */
    private Integer idDepOrdersWait;
    
    /**
     * 库存计数OK
     */
    private Integer stockCountOk;
    
    /**
     * 库存计数
     */
    private Integer stockCount;
    
    // Getters and Setters
    public List<NxDistributerFatherGoodsEntity> getGrandArr() {
        return grandArr;
    }

    public void setGrandArr(List<NxDistributerFatherGoodsEntity> grandArr) {
        this.grandArr = grandArr;
    }

    public List<NxDepartmentEntity> getWaitDepNx() {
        return waitDepNx;
    }

    public void setWaitDepNx(List<NxDepartmentEntity> waitDepNx) {
        this.waitDepNx = waitDepNx;
    }

    public List<GbDepartmentEntity> getWaitDepGb() {
        return waitDepGb;
    }

    public void setWaitDepGb(List<GbDepartmentEntity> waitDepGb) {
        this.waitDepGb = waitDepGb;
    }

    public Integer getDepOrdersWait() {
        return depOrdersWait;
    }

    public void setDepOrdersWait(Integer depOrdersWait) {
        this.depOrdersWait = depOrdersWait;
    }

    public Integer getIdDepOrdersWait() {
        return idDepOrdersWait;
    }

    public void setIdDepOrdersWait(Integer idDepOrdersWait) {
        this.idDepOrdersWait = idDepOrdersWait;
    }

    public Integer getStockCountOk() {
        return stockCountOk;
    }

    public void setStockCountOk(Integer stockCountOk) {
        this.stockCountOk = stockCountOk;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    @Override
    public String toString() {
        return "StockGoodsWithCategoriesResponse{" +
                "grandArr=" + (grandArr != null ? grandArr.size() : 0) + "个类别" +
                ", waitDepNx=" + (waitDepNx != null ? waitDepNx.size() : 0) + "个部门" +
                ", waitDepGb=" + (waitDepGb != null ? waitDepGb.size() : 0) + "个部门" +
                ", depOrdersWait=" + depOrdersWait +
                ", idDepOrdersWait=" + idDepOrdersWait +
                ", stockCountOk=" + stockCountOk +
                ", stockCount=" + stockCount +
                '}';
    }
} 