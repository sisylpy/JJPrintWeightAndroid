package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class NxDistributerGoodsShelfEntity implements Serializable{

    public Integer nxCommunityFatherGoodsId;

    public static final long serialVersionUID = 1L;

    /**
     *  货架id
     */
    public Integer nxDistributerGoodsShelfId;
    /**
     *  货架名称
     */
    public String nxDistributerGoodsShelfName;

    public Integer getNxCommunityFatherGoodsId() {
        return nxCommunityFatherGoodsId;
    }

    public void setNxCommunityFatherGoodsId(Integer nxCommunityFatherGoodsId) {
        this.nxCommunityFatherGoodsId = nxCommunityFatherGoodsId;
    }

    public Integer getNxDistributerGoodsShelfId() {
        return nxDistributerGoodsShelfId;
    }

    public void setNxDistributerGoodsShelfId(Integer nxDistributerGoodsShelfId) {
        this.nxDistributerGoodsShelfId = nxDistributerGoodsShelfId;
    }

    public String getNxDistributerGoodsShelfName() {
        return nxDistributerGoodsShelfName;
    }

    public void setNxDistributerGoodsShelfName(String nxDistributerGoodsShelfName) {
        this.nxDistributerGoodsShelfName = nxDistributerGoodsShelfName;
    }

    public Integer getNxDistributerGoodsShelfSort() {
        return nxDistributerGoodsShelfSort;
    }

    public void setNxDistributerGoodsShelfSort(Integer nxDistributerGoodsShelfSort) {
        this.nxDistributerGoodsShelfSort = nxDistributerGoodsShelfSort;
    }

    public Integer getNxDistributerGoodsShelfDisId() {
        return nxDistributerGoodsShelfDisId;
    }

    public void setNxDistributerGoodsShelfDisId(Integer nxDistributerGoodsShelfDisId) {
        this.nxDistributerGoodsShelfDisId = nxDistributerGoodsShelfDisId;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public List<NxDistributerGoodsShelfGoodsEntity> getNxDisGoodsShelfGoodsEntities() {
        return nxDisGoodsShelfGoodsEntities;
    }

    public void setNxDisGoodsShelfGoodsEntities(List<NxDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities) {
        this.nxDisGoodsShelfGoodsEntities = nxDisGoodsShelfGoodsEntities;
    }

    /**
     *  货架排序
     */
    public Integer nxDistributerGoodsShelfSort;
    /**
     *  批发商id
     */
    public Integer nxDistributerGoodsShelfDisId;
    public Boolean isSelected = false;

    public List<NxDistributerGoodsShelfGoodsEntity> nxDisGoodsShelfGoodsEntities;

}
