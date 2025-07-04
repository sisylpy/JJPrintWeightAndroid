package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class NxDistributerGoodsShelfGoodsEntity implements Serializable{

    public Integer nxCommunityFatherGoodsId;


    public static final long serialVersionUID = 1L;

    /**
     *  货架商品id
     */
    public Integer nxDistributerGoodsShelfGoodsId;
    /**
     *  批发商商品id
     */
    public Integer nxDgsgDisGoodsId;

    public Integer getNxCommunityFatherGoodsId() {
        return nxCommunityFatherGoodsId;
    }

    public void setNxCommunityFatherGoodsId(Integer nxCommunityFatherGoodsId) {
        this.nxCommunityFatherGoodsId = nxCommunityFatherGoodsId;
    }

    public Integer getNxDistributerGoodsShelfGoodsId() {
        return nxDistributerGoodsShelfGoodsId;
    }

    public void setNxDistributerGoodsShelfGoodsId(Integer nxDistributerGoodsShelfGoodsId) {
        this.nxDistributerGoodsShelfGoodsId = nxDistributerGoodsShelfGoodsId;
    }

    public Integer getNxDgsgDisGoodsId() {
        return nxDgsgDisGoodsId;
    }

    public void setNxDgsgDisGoodsId(Integer nxDgsgDisGoodsId) {
        this.nxDgsgDisGoodsId = nxDgsgDisGoodsId;
    }

    public Integer getNxDgsgShelfId() {
        return nxDgsgShelfId;
    }

    public void setNxDgsgShelfId(Integer nxDgsgShelfId) {
        this.nxDgsgShelfId = nxDgsgShelfId;
    }

    public Integer getNxDgsgSort() {
        return nxDgsgSort;
    }

    public void setNxDgsgSort(Integer nxDgsgSort) {
        this.nxDgsgSort = nxDgsgSort;
    }

    public NxDistributerGoodsEntity getNxDistributerGoodsEntity() {
        return nxDistributerGoodsEntity;
    }

    public void setNxDistributerGoodsEntity(NxDistributerGoodsEntity nxDistributerGoodsEntity) {
        this.nxDistributerGoodsEntity = nxDistributerGoodsEntity;
    }

    /**
     *  货架id
     */
    public Integer nxDgsgShelfId;
    public Integer nxDgsgSort;

    public NxDistributerGoodsEntity nxDistributerGoodsEntity;

    private String nxDoWeight;

    public String getNxDoWeight() {
        return nxDoWeight;
    }

    public void setNxDoWeight(double weight) {
        this.nxDoWeight = String.valueOf(weight);
    }

    private List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;

    public List<NxDepartmentOrdersEntity> getNxDepartmentOrdersEntities() {
        return nxDepartmentOrdersEntities;
    }

    public void setNxDepartmentOrdersEntities(List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities) {
        this.nxDepartmentOrdersEntities = nxDepartmentOrdersEntities;
    }
}
