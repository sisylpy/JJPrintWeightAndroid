package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class NxDepartmentEntity implements Serializable{

    public Integer nxCommunityFatherGoodsId;

    public static final long serialVersionUID = 1L;

    /**
     *  订货部门id
     */
    public Integer nxDepartmentId;
    /**
     *  订货部门名称
     */
    public String nxDepartmentName;
    /**
     *  订货部门上级id
     */
    public Integer nxDepartmentFatherId;
    /**
     *  订货部门类型
     */
    public String nxDepartmentType;
    /**
     *  订货部门子部门数量
     */
    public Integer nxDepartmentSubAmount;

    public String nxDepartmentFilePath;

    public Boolean isSelected;


    public Integer nxDepUserId;

    public Integer nxDepartmentDisId;

    public Integer nxDepartmentIsGroupDep;

    public String  nxDepartmentPrintName;

    public Integer nxDepartmentShowWeeks;

    public Integer nxDepartmentSettleType;

    public String nxDepartmentAttrName;
    public Integer nxDepartmentPromotionGoodsId;

    public Integer nxDepartmentDisRouteId;

    public Integer nxDepartmentDriverId;
    public Integer nxDepartmentOweBoxNumber;
    public Integer nxDepartmentDeliveryBoxNumber;
    public Integer nxDepartmentWorkingStatus;
    public String nxDepartmentUnPayTotal;
    public Integer nxDepartmentAddCount;
    public Integer nxDepartmentPurOrderCount;
    public Integer nxDepartmentNeedNotPurOrderCount;
    public Integer nxDepartmentOrderTotal;
    public String nxDepartmentPayTotal;
    public String nxDepartmentJoinDate;
    public String nxDepartmentProfitTotal;

    public Integer getNxCommunityFatherGoodsId() {
        return nxCommunityFatherGoodsId;
    }

    public void setNxCommunityFatherGoodsId(Integer nxCommunityFatherGoodsId) {
        this.nxCommunityFatherGoodsId = nxCommunityFatherGoodsId;
    }

    public Integer getNxDepartmentId() {
        return nxDepartmentId;
    }

    public void setNxDepartmentId(Integer nxDepartmentId) {
        this.nxDepartmentId = nxDepartmentId;
    }

    public String getNxDepartmentName() {
        return nxDepartmentName;
    }

    public void setNxDepartmentName(String nxDepartmentName) {
        this.nxDepartmentName = nxDepartmentName;
    }

    public Integer getNxDepartmentFatherId() {
        return nxDepartmentFatherId;
    }

    public void setNxDepartmentFatherId(Integer nxDepartmentFatherId) {
        this.nxDepartmentFatherId = nxDepartmentFatherId;
    }

    public String getNxDepartmentType() {
        return nxDepartmentType;
    }

    public void setNxDepartmentType(String nxDepartmentType) {
        this.nxDepartmentType = nxDepartmentType;
    }

    public Integer getNxDepartmentSubAmount() {
        return nxDepartmentSubAmount;
    }

    public void setNxDepartmentSubAmount(Integer nxDepartmentSubAmount) {
        this.nxDepartmentSubAmount = nxDepartmentSubAmount;
    }

    public String getNxDepartmentFilePath() {
        return nxDepartmentFilePath;
    }

    public void setNxDepartmentFilePath(String nxDepartmentFilePath) {
        this.nxDepartmentFilePath = nxDepartmentFilePath;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public Integer getNxDepUserId() {
        return nxDepUserId;
    }

    public void setNxDepUserId(Integer nxDepUserId) {
        this.nxDepUserId = nxDepUserId;
    }

    public Integer getNxDepartmentDisId() {
        return nxDepartmentDisId;
    }

    public void setNxDepartmentDisId(Integer nxDepartmentDisId) {
        this.nxDepartmentDisId = nxDepartmentDisId;
    }

    public Integer getNxDepartmentIsGroupDep() {
        return nxDepartmentIsGroupDep;
    }

    public void setNxDepartmentIsGroupDep(Integer nxDepartmentIsGroupDep) {
        this.nxDepartmentIsGroupDep = nxDepartmentIsGroupDep;
    }

    public String getNxDepartmentPrintName() {
        return nxDepartmentPrintName;
    }

    public void setNxDepartmentPrintName(String nxDepartmentPrintName) {
        this.nxDepartmentPrintName = nxDepartmentPrintName;
    }

    public Integer getNxDepartmentShowWeeks() {
        return nxDepartmentShowWeeks;
    }

    public void setNxDepartmentShowWeeks(Integer nxDepartmentShowWeeks) {
        this.nxDepartmentShowWeeks = nxDepartmentShowWeeks;
    }

    public Integer getNxDepartmentSettleType() {
        return nxDepartmentSettleType;
    }

    public void setNxDepartmentSettleType(Integer nxDepartmentSettleType) {
        this.nxDepartmentSettleType = nxDepartmentSettleType;
    }

    public String getNxDepartmentAttrName() {
        return nxDepartmentAttrName;
    }

    public void setNxDepartmentAttrName(String nxDepartmentAttrName) {
        this.nxDepartmentAttrName = nxDepartmentAttrName;
    }

    public Integer getNxDepartmentPromotionGoodsId() {
        return nxDepartmentPromotionGoodsId;
    }

    public void setNxDepartmentPromotionGoodsId(Integer nxDepartmentPromotionGoodsId) {
        this.nxDepartmentPromotionGoodsId = nxDepartmentPromotionGoodsId;
    }

    public Integer getNxDepartmentDisRouteId() {
        return nxDepartmentDisRouteId;
    }

    public void setNxDepartmentDisRouteId(Integer nxDepartmentDisRouteId) {
        this.nxDepartmentDisRouteId = nxDepartmentDisRouteId;
    }

    public Integer getNxDepartmentDriverId() {
        return nxDepartmentDriverId;
    }

    public void setNxDepartmentDriverId(Integer nxDepartmentDriverId) {
        this.nxDepartmentDriverId = nxDepartmentDriverId;
    }

    public Integer getNxDepartmentOweBoxNumber() {
        return nxDepartmentOweBoxNumber;
    }

    public void setNxDepartmentOweBoxNumber(Integer nxDepartmentOweBoxNumber) {
        this.nxDepartmentOweBoxNumber = nxDepartmentOweBoxNumber;
    }

    public Integer getNxDepartmentDeliveryBoxNumber() {
        return nxDepartmentDeliveryBoxNumber;
    }

    public void setNxDepartmentDeliveryBoxNumber(Integer nxDepartmentDeliveryBoxNumber) {
        this.nxDepartmentDeliveryBoxNumber = nxDepartmentDeliveryBoxNumber;
    }

    public Integer getNxDepartmentWorkingStatus() {
        return nxDepartmentWorkingStatus;
    }

    public void setNxDepartmentWorkingStatus(Integer nxDepartmentWorkingStatus) {
        this.nxDepartmentWorkingStatus = nxDepartmentWorkingStatus;
    }

    public String getNxDepartmentUnPayTotal() {
        return nxDepartmentUnPayTotal;
    }

    public void setNxDepartmentUnPayTotal(String nxDepartmentUnPayTotal) {
        this.nxDepartmentUnPayTotal = nxDepartmentUnPayTotal;
    }

    public Integer getNxDepartmentAddCount() {
        return nxDepartmentAddCount;
    }

    public void setNxDepartmentAddCount(Integer nxDepartmentAddCount) {
        this.nxDepartmentAddCount = nxDepartmentAddCount;
    }

    public Integer getNxDepartmentPurOrderCount() {
        return nxDepartmentPurOrderCount;
    }

    public void setNxDepartmentPurOrderCount(Integer nxDepartmentPurOrderCount) {
        this.nxDepartmentPurOrderCount = nxDepartmentPurOrderCount;
    }

    public Integer getNxDepartmentNeedNotPurOrderCount() {
        return nxDepartmentNeedNotPurOrderCount;
    }

    public void setNxDepartmentNeedNotPurOrderCount(Integer nxDepartmentNeedNotPurOrderCount) {
        this.nxDepartmentNeedNotPurOrderCount = nxDepartmentNeedNotPurOrderCount;
    }

    public Integer getNxDepartmentOrderTotal() {
        return nxDepartmentOrderTotal;
    }

    public void setNxDepartmentOrderTotal(Integer nxDepartmentOrderTotal) {
        this.nxDepartmentOrderTotal = nxDepartmentOrderTotal;
    }

    public String getNxDepartmentPayTotal() {
        return nxDepartmentPayTotal;
    }

    public void setNxDepartmentPayTotal(String nxDepartmentPayTotal) {
        this.nxDepartmentPayTotal = nxDepartmentPayTotal;
    }

    public String getNxDepartmentJoinDate() {
        return nxDepartmentJoinDate;
    }

    public void setNxDepartmentJoinDate(String nxDepartmentJoinDate) {
        this.nxDepartmentJoinDate = nxDepartmentJoinDate;
    }

    public String getNxDepartmentProfitTotal() {
        return nxDepartmentProfitTotal;
    }

    public void setNxDepartmentProfitTotal(String nxDepartmentProfitTotal) {
        this.nxDepartmentProfitTotal = nxDepartmentProfitTotal;
    }

    public String getNxDepartmentPinyin() {
        return nxDepartmentPinyin;
    }

    public void setNxDepartmentPinyin(String nxDepartmentPinyin) {
        this.nxDepartmentPinyin = nxDepartmentPinyin;
    }

    public String getNxDepartmentAppId() {
        return nxDepartmentAppId;
    }

    public void setNxDepartmentAppId(String nxDepartmentAppId) {
        this.nxDepartmentAppId = nxDepartmentAppId;
    }

    public NxDepartmentEntity getFatherDepartmentEntity() {
        return fatherDepartmentEntity;
    }

    public void setFatherDepartmentEntity(NxDepartmentEntity fatherDepartmentEntity) {
        this.fatherDepartmentEntity = fatherDepartmentEntity;
    }

    public List<NxDepartmentEntity> getNxDepartmentEntities() {
        return nxDepartmentEntities;
    }

    public void setNxDepartmentEntities(List<NxDepartmentEntity> nxDepartmentEntities) {
        this.nxDepartmentEntities = nxDepartmentEntities;
    }

    public List<NxDepartmentEntity> getNxSubDepartments() {
        return nxSubDepartments;
    }

    public void setNxSubDepartments(List<NxDepartmentEntity> nxSubDepartments) {
        this.nxSubDepartments = nxSubDepartments;
    }

    public List<NxDepartmentOrdersEntity> getNxDepartmentOrdersEntities() {
        return nxDepartmentOrdersEntities;
    }

    public void setNxDepartmentOrdersEntities(List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities) {
        this.nxDepartmentOrdersEntities = nxDepartmentOrdersEntities;
    }

    public String nxDepartmentPinyin;
    public String nxDepartmentAppId;


    public NxDepartmentEntity fatherDepartmentEntity;



    public List<NxDepartmentEntity> nxDepartmentEntities;
    public List<NxDepartmentEntity> nxSubDepartments;

    public List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;

   
}
