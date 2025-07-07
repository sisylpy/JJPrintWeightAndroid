package com.swolo.lpy.pysx.main.modal;

import java.util.List;

/**
 * 部门出库订单接口响应数据模型
 * 对应小程序 stockerGetHaveOutCataGoods 和 stokerHaveNotOutCataGoods 接口返回数据
 */
public class DepOutOrderResponse {
    
    /**
     * 订单分组数据数组
     * 对应小程序 cataArr
     */
    public List<GreatGrand> arr;
    
    /**
     * 未拣货数量
     * 对应小程序 notCount
     */
    public int notCount;
    
    /**
     * 已拣货数量
     * 对应小程序 haveCount
     */
    public int haveCount;
    
    /**
     * 最外层分组 - 对应小程序 greatGrand
     */
    public static class GreatGrand {
        public List<FatherGoodsEntities> fatherGoodsEntities;
    }
    
    /**
     * 父级商品分类 - 对应小程序 grand
     */
    public static class FatherGoodsEntities {
        public String nxDfgFatherGoodsName;
        public List<PurchaseGoodsEntities> nxDistributerPurchaseGoodsEntities;
    }
    
    /**
     * 采购商品 - 对应小程序 pur
     */
    public static class PurchaseGoodsEntities {
        public NxDistributerGoodsEntity nxDistributerGoodsEntity;
        public List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
    }
    
    /**
     * 部门订单 - 对应小程序 order
     */
    public static class DepartmentOrdersEntities {
        public String nxDepartmentOrdersId; // 订单ID，用于删除
        public String indexStr;
        public String nxDoQuantity;
        public String nxDoStandard;
        public String nxDoWeight;
        public String nxDoRemark;
    }
} 