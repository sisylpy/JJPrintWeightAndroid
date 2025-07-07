package com.swolo.lpy.pysx.main.modal;

/**
 * 出库确认请求数据模型
 * 对应小程序 giveOrderWeightListForStockAndFinish 接口请求参数
 */
public class OrderWeightRequest {
    
    /**
     * 订单ID
     */
    private int nxDepartmentOrdersId;
    
    /**
     * 重量
     */
    private Double nxDoWeight;
    
    /**
     * 拣货员ID
     */
    private int nxDoPickUserId;
    
    /**
     * 是否选中
     */
    private boolean hasChoice;
    
    public int getNxDepartmentOrdersId() {
        return nxDepartmentOrdersId;
    }
    
    public void setNxDepartmentOrdersId(int nxDepartmentOrdersId) {
        this.nxDepartmentOrdersId = nxDepartmentOrdersId;
    }
    
    public Double getNxDoWeight() {
        return nxDoWeight;
    }
    
    public void setNxDoWeight(Double nxDoWeight) {
        this.nxDoWeight = nxDoWeight;
    }
    
    public int getNxDoPickUserId() {
        return nxDoPickUserId;
    }
    
    public void setNxDoPickUserId(int nxDoPickUserId) {
        this.nxDoPickUserId = nxDoPickUserId;
    }
    
    public boolean isHasChoice() {
        return hasChoice;
    }
    
    public void setHasChoice(boolean hasChoice) {
        this.hasChoice = hasChoice;
    }
} 