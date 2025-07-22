package com.swolo.lpy.pysx.main.modal;

import java.util.List;
import java.util.TreeSet;

/**
 * 商品类别实体类
 * 对应小程序中的商品类别数据结构
 */
public class NxDistributerFatherGoodsEntity {
    
    /**
     * 商品类别ID
     */
    private Integer nxDistributerFatherGoodsId;
    
    /**
     * 商品类别名称
     */
    private String nxDfgFatherGoodsName;
    
    /**
     * 商品类别图片
     */
    private String nxDfgFatherGoodsImg;
    
    /**
     * 商品类别大图
     */
    private String nxDfgFatherGoodsImgLarge;
    
    /**
     * 商品类别排序
     */
    private Integer nxDfgFatherGoodsSort;
    
    /**
     * 商品类别颜色
     */
    private String nxDfgFatherGoodsColor;
    
    /**
     * 父级商品类别ID
     */
    private Integer nxDfgFathersFatherId;
    
    /**
     * 商品类别层级
     */
    private Integer nxDfgFatherGoodsLevel;

    /**
     * 商品ID
     */
    private Integer nxDfgNxGoodsId;
    
    /**
     * 父级利润总计
     */
    private Double fatherProfitTotal;
    
    /**
     * 父级利润总计字符串
     */
    private String fatherProfitTotalString;
    
    /**
     * 父级利润比例字符串
     */
    private String fatherProfitScaleString;
    
    /**
     * 父级重量总计字符串
     */
    private String fatherWeightTotalString;
    
    /**
     * 父级小计总计字符串
     */
    private String fatherSubtotalTotalString;
    
    /**
     * 分销商ID
     */
    private Integer nxDfgDistributerId;

    /**
     * 是否选中
     */
    private Boolean isSelected = false;

    /**
     * 父级商品类别实体
     */
    private NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity;

    /**
     * 父级商品实体列表
     */
    private List<NxDistributerFatherGoodsEntity> fatherGoodsEntities;

    /**
     * 树形集合
     */
    private TreeSet<NxDistributerFatherGoodsEntity> treeSet;

    /**
     * 分销商商品实体列表
     */
    private List<NxDistributerGoodsEntity> nxDistributerGoodsEntities;

    /**
     * 分销商采购商品实体列表
     */
    private List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities;
    
    /**
     * 部门分销商品实体列表
     */
    private List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities;

    // Getters and Setters
    public Integer getNxDistributerFatherGoodsId() {
        return nxDistributerFatherGoodsId;
    }

    public void setNxDistributerFatherGoodsId(Integer nxDistributerFatherGoodsId) {
        this.nxDistributerFatherGoodsId = nxDistributerFatherGoodsId;
    }

    public String getNxDfgFatherGoodsName() {
        return nxDfgFatherGoodsName;
    }

    public void setNxDfgFatherGoodsName(String nxDfgFatherGoodsName) {
        this.nxDfgFatherGoodsName = nxDfgFatherGoodsName;
    }

    public String getNxDfgFatherGoodsImg() {
        return nxDfgFatherGoodsImg;
    }

    public void setNxDfgFatherGoodsImg(String nxDfgFatherGoodsImg) {
        this.nxDfgFatherGoodsImg = nxDfgFatherGoodsImg;
    }

    public String getNxDfgFatherGoodsImgLarge() {
        return nxDfgFatherGoodsImgLarge;
    }

    public void setNxDfgFatherGoodsImgLarge(String nxDfgFatherGoodsImgLarge) {
        this.nxDfgFatherGoodsImgLarge = nxDfgFatherGoodsImgLarge;
    }

    public Integer getNxDfgFatherGoodsSort() {
        return nxDfgFatherGoodsSort;
    }

    public void setNxDfgFatherGoodsSort(Integer nxDfgFatherGoodsSort) {
        this.nxDfgFatherGoodsSort = nxDfgFatherGoodsSort;
    }

    public String getNxDfgFatherGoodsColor() {
        return nxDfgFatherGoodsColor;
    }

    public void setNxDfgFatherGoodsColor(String nxDfgFatherGoodsColor) {
        this.nxDfgFatherGoodsColor = nxDfgFatherGoodsColor;
    }

    public Integer getNxDfgFathersFatherId() {
        return nxDfgFathersFatherId;
    }

    public void setNxDfgFathersFatherId(Integer nxDfgFathersFatherId) {
        this.nxDfgFathersFatherId = nxDfgFathersFatherId;
    }

    public Integer getNxDfgFatherGoodsLevel() {
        return nxDfgFatherGoodsLevel;
    }

    public void setNxDfgFatherGoodsLevel(Integer nxDfgFatherGoodsLevel) {
        this.nxDfgFatherGoodsLevel = nxDfgFatherGoodsLevel;
    }

    public Integer getNxDfgNxGoodsId() {
        return nxDfgNxGoodsId;
    }

    public void setNxDfgNxGoodsId(Integer nxDfgNxGoodsId) {
        this.nxDfgNxGoodsId = nxDfgNxGoodsId;
    }

    public Double getFatherProfitTotal() {
        return fatherProfitTotal;
    }

    public void setFatherProfitTotal(Double fatherProfitTotal) {
        this.fatherProfitTotal = fatherProfitTotal;
    }

    public String getFatherProfitTotalString() {
        return fatherProfitTotalString;
    }

    public void setFatherProfitTotalString(String fatherProfitTotalString) {
        this.fatherProfitTotalString = fatherProfitTotalString;
    }

    public String getFatherProfitScaleString() {
        return fatherProfitScaleString;
    }

    public void setFatherProfitScaleString(String fatherProfitScaleString) {
        this.fatherProfitScaleString = fatherProfitScaleString;
    }

    public String getFatherWeightTotalString() {
        return fatherWeightTotalString;
    }

    public void setFatherWeightTotalString(String fatherWeightTotalString) {
        this.fatherWeightTotalString = fatherWeightTotalString;
    }

    public String getFatherSubtotalTotalString() {
        return fatherSubtotalTotalString;
    }

    public void setFatherSubtotalTotalString(String fatherSubtotalTotalString) {
        this.fatherSubtotalTotalString = fatherSubtotalTotalString;
    }

    public Integer getNxDfgDistributerId() {
        return nxDfgDistributerId;
    }

    public void setNxDfgDistributerId(Integer nxDfgDistributerId) {
        this.nxDfgDistributerId = nxDfgDistributerId;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public NxDistributerFatherGoodsEntity getNxDistributerFatherGoodsEntity() {
        return nxDistributerFatherGoodsEntity;
    }

    public void setNxDistributerFatherGoodsEntity(NxDistributerFatherGoodsEntity nxDistributerFatherGoodsEntity) {
        this.nxDistributerFatherGoodsEntity = nxDistributerFatherGoodsEntity;
    }

    public List<NxDistributerFatherGoodsEntity> getFatherGoodsEntities() {
        return fatherGoodsEntities;
    }

    public void setFatherGoodsEntities(List<NxDistributerFatherGoodsEntity> fatherGoodsEntities) {
        this.fatherGoodsEntities = fatherGoodsEntities;
    }

    public TreeSet<NxDistributerFatherGoodsEntity> getTreeSet() {
        return treeSet;
    }

    public void setTreeSet(TreeSet<NxDistributerFatherGoodsEntity> treeSet) {
        this.treeSet = treeSet;
    }

    public List<NxDistributerGoodsEntity> getNxDistributerGoodsEntities() {
        return nxDistributerGoodsEntities;
    }

    public void setNxDistributerGoodsEntities(List<NxDistributerGoodsEntity> nxDistributerGoodsEntities) {
        this.nxDistributerGoodsEntities = nxDistributerGoodsEntities;
    }

    public List<NxDistributerPurchaseGoodsEntity> getNxDistributerPurchaseGoodsEntities() {
        return nxDistributerPurchaseGoodsEntities;
    }

    public void setNxDistributerPurchaseGoodsEntities(List<NxDistributerPurchaseGoodsEntity> nxDistributerPurchaseGoodsEntities) {
        this.nxDistributerPurchaseGoodsEntities = nxDistributerPurchaseGoodsEntities;
    }

    public List<NxDepartmentDisGoodsEntity> getNxDepartmentDisGoodsEntities() {
        return nxDepartmentDisGoodsEntities;
    }

    public void setNxDepartmentDisGoodsEntities(List<NxDepartmentDisGoodsEntity> nxDepartmentDisGoodsEntities) {
        this.nxDepartmentDisGoodsEntities = nxDepartmentDisGoodsEntities;
    }

    @Override
    public String toString() {
        return "NxDistributerFatherGoodsEntity{" +
                "nxDistributerFatherGoodsId=" + nxDistributerFatherGoodsId +
                ", nxDfgFatherGoodsName='" + nxDfgFatherGoodsName + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
} 