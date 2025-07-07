package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class NxDepartmentOrdersEntity implements Serializable{

    public Integer nxCommunityFatherGoodsId;

    public NxDepartmentDisGoodsEntity getNxDepartmentDisGoodsEntity() {
        return nxDepartmentDisGoodsEntity;
    }

    public void setNxDepartmentDisGoodsEntity(NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity) {
        this.nxDepartmentDisGoodsEntity = nxDepartmentDisGoodsEntity;
    }



    private NxDepartmentDisGoodsEntity nxDepartmentDisGoodsEntity;
    /**
     *  部门订单id
     */
    public Integer nxDepartmentOrdersId;
    /**
     *  部门订单nx商品id
     */
    public Integer nxDoNxGoodsId;
    /**
     *  部门订单商品父id
     */
    public Integer nxDoNxGoodsFatherId;
    /**
     *  部门订单社区商品id
     */
    public Integer nxDoDisGoodsId;
    public Integer nxDoDisGoodsFatherId;

    public  Integer nxDoDepDisGoodsId;
    public String  nxDoDepDisGoodsPrice;

    /**
     *  部门订单申请数量
     */
    public String nxDoQuantity;
    /**
     *  部门订单申请规格
     */


    public String nxDoStandard;
    /**
     *  部门订单申请备注
     */
    public String nxDoRemark;
    /**
     *  部门订单重量
     */
    public String nxDoWeight;

    /**
     *  部门订单商品单价
     */
    public String nxDoPrice;
    /**
     *  部门订单申请商品小计
     */
    public String nxDoSubtotal;
    /**
     *  部门订单部门id
     */
    public Integer nxDoDepartmentId;

    public Integer nxDoDepartmentFatherId;
    public Integer nxDoDisGoodsGrandId;
    /**
     *  部门订单批发商id
     */
    public Integer nxDoDistributerId;
    /**
     *  部门订单账单id
     */
    public Integer nxDoBillId;
    /**
     *  部门订单申请商品状态
     */
    public Integer nxDoStatus;
    /**
     *  部门订单订货用户id
     */
    public Integer nxDoOrderUserId;
    /**
     *  部门订单商品称重用户id
     */
    public Integer nxDoPickUserId;
    /**
     *  部门订单商品输入单价用户id
     */
    public Integer nxDoAccountUserId;
    /**
     *  部门商品采购员id
     */
    public Integer nxDoPurchaseUserId;
    /**
     *  部门订单商品进货状态
     */
    public Integer nxDoPurchaseStatus;
    /**
     *  部门订单申请时间
     */
    public String nxDoApplyDate;
    public String nxDoApplyFullTime;
    public String nxDoGoodsName;

    /**
     *  部门订单送达时间
     */
    public String nxDoArriveOnlyDate;
    public String nxDoArriveDate;
    public Integer nxDoArriveWeeksYear;

    /**
     * 采购商品id
     */
    public Integer nxDoPurchaseGoodsId;


    public String nxDoOperationTime;

    public String nxDoArriveWhatDay;

    public Integer nxDoIsAgent;

    public String nxDoApplyOnlyTime;
    public String nxDoCostPrice;
    public String nxDoCostSubtotal;
    public String nxDoPrintStandard;
    public String nxDoExpectPrice;

    public String nxDoReturnWeight;
    public String nxDoReturnSubtotal;
    public Integer nxDoReturnBillId;
    public Integer nxDoReturnStatus;

    public Integer nxDoGbDistributerId;
    public Integer nxDoGbDepartmentId;
    public Integer nxDoGbDepartmentFatherId;
    public Integer nxDoWeightId;
    public Integer nxDoNxCommunityId;
    public Integer nxDoNxCommRestrauntId;
    public Integer nxDoNxCommRestrauntFatherId;
    public Integer nxDoGbDepartmentOrderId;
    public Integer nxDoNxRestrauntOrderId;
    public Integer nxDoGoodsType;
    public Integer nxDoTodayOrder;
    public Integer nxDoGbDepDisGoodId;
    public String nxDoPriceDifferent;
    public String nxDoProfitSubtotal;
    public String nxDoProfitScale;
    public String nxDoCostPriceUpdate;
    public String nxDoCostPriceLevel;

    public NxDistributerGoodsEntity nxDistributerGoodsEntity;

    public NxDepartmentEntity nxDepartmentEntity;

    public List<NxDistributerGoodsEntity> nxDistributerGoodsEntityList;

    private GbDepartmentEntity gbDepartmentEntity;

    public GbDepartmentEntity getGbDepartmentEntity() {
        return gbDepartmentEntity;
    }

    public void setGbDepartmentEntity(GbDepartmentEntity gbDepartmentEntity) {
        this.gbDepartmentEntity = gbDepartmentEntity;
    }

    public Boolean onFocus;

    public Boolean hasChoice =  false;

    public Boolean isNotice = false ;

    public Boolean showDate = true;

    public Boolean  isWeeks = true;

    public Boolean purSelected = true;

    public String indexStr;

    public Integer getNxCommunityFatherGoodsId() {
        return nxCommunityFatherGoodsId;
    }

    public void setNxCommunityFatherGoodsId(Integer nxCommunityFatherGoodsId) {
        this.nxCommunityFatherGoodsId = nxCommunityFatherGoodsId;
    }

    public Integer getNxDepartmentOrdersId() {
        return nxDepartmentOrdersId;
    }

    public void setNxDepartmentOrdersId(Integer nxDepartmentOrdersId) {
        this.nxDepartmentOrdersId = nxDepartmentOrdersId;
    }

    public Integer getNxDoNxGoodsId() {
        return nxDoNxGoodsId;
    }

    public void setNxDoNxGoodsId(Integer nxDoNxGoodsId) {
        this.nxDoNxGoodsId = nxDoNxGoodsId;
    }

    public Integer getNxDoNxGoodsFatherId() {
        return nxDoNxGoodsFatherId;
    }

    public void setNxDoNxGoodsFatherId(Integer nxDoNxGoodsFatherId) {
        this.nxDoNxGoodsFatherId = nxDoNxGoodsFatherId;
    }

    public Integer getNxDoDisGoodsId() {
        return nxDoDisGoodsId;
    }

    public void setNxDoDisGoodsId(Integer nxDoDisGoodsId) {
        this.nxDoDisGoodsId = nxDoDisGoodsId;
    }

    public Integer getNxDoDisGoodsFatherId() {
        return nxDoDisGoodsFatherId;
    }

    public void setNxDoDisGoodsFatherId(Integer nxDoDisGoodsFatherId) {
        this.nxDoDisGoodsFatherId = nxDoDisGoodsFatherId;
    }

    public Integer getNxDoDepDisGoodsId() {
        return nxDoDepDisGoodsId;
    }

    public void setNxDoDepDisGoodsId(Integer nxDoDepDisGoodsId) {
        this.nxDoDepDisGoodsId = nxDoDepDisGoodsId;
    }

    public String getNxDoDepDisGoodsPrice() {
        return nxDoDepDisGoodsPrice;
    }

    public void setNxDoDepDisGoodsPrice(String nxDoDepDisGoodsPrice) {
        this.nxDoDepDisGoodsPrice = nxDoDepDisGoodsPrice;
    }

    public String getNxDoQuantity() {
        return nxDoQuantity;
    }

    public void setNxDoQuantity(String nxDoQuantity) {
        this.nxDoQuantity = nxDoQuantity;
    }

    public String getNxDoStandard() {
        return nxDoStandard;
    }

    public void setNxDoStandard(String nxDoStandard) {
        this.nxDoStandard = nxDoStandard;
    }

    public String getNxDoRemark() {
        return nxDoRemark;
    }

    public void setNxDoRemark(String nxDoRemark) {
        this.nxDoRemark = nxDoRemark;
    }

    public String getNxDoWeight() {
        return nxDoWeight;
    }

    public void setNxDoWeight(String nxDoWeight) {
        this.nxDoWeight = nxDoWeight;
    }

    public String getNxDoPrice() {
        return nxDoPrice;
    }

    public void setNxDoPrice(String nxDoPrice) {
        this.nxDoPrice = nxDoPrice;
    }

    public String getNxDoSubtotal() {
        return nxDoSubtotal;
    }

    public void setNxDoSubtotal(String nxDoSubtotal) {
        this.nxDoSubtotal = nxDoSubtotal;
    }

    public Integer getNxDoDepartmentId() {
        return nxDoDepartmentId;
    }

    public void setNxDoDepartmentId(Integer nxDoDepartmentId) {
        this.nxDoDepartmentId = nxDoDepartmentId;
    }

    public Integer getNxDoDepartmentFatherId() {
        return nxDoDepartmentFatherId;
    }

    public void setNxDoDepartmentFatherId(Integer nxDoDepartmentFatherId) {
        this.nxDoDepartmentFatherId = nxDoDepartmentFatherId;
    }

    public Integer getNxDoDisGoodsGrandId() {
        return nxDoDisGoodsGrandId;
    }

    public void setNxDoDisGoodsGrandId(Integer nxDoDisGoodsGrandId) {
        this.nxDoDisGoodsGrandId = nxDoDisGoodsGrandId;
    }

    public Integer getNxDoDistributerId() {
        return nxDoDistributerId;
    }

    public void setNxDoDistributerId(Integer nxDoDistributerId) {
        this.nxDoDistributerId = nxDoDistributerId;
    }

    public Integer getNxDoBillId() {
        return nxDoBillId;
    }

    public void setNxDoBillId(Integer nxDoBillId) {
        this.nxDoBillId = nxDoBillId;
    }

    public Integer getNxDoStatus() {
        return nxDoStatus;
    }

    public void setNxDoStatus(Integer nxDoStatus) {
        this.nxDoStatus = nxDoStatus;
    }

    public Integer getNxDoOrderUserId() {
        return nxDoOrderUserId;
    }

    public void setNxDoOrderUserId(Integer nxDoOrderUserId) {
        this.nxDoOrderUserId = nxDoOrderUserId;
    }

    public Integer getNxDoPickUserId() {
        return nxDoPickUserId;
    }

    public void setNxDoPickUserId(Integer nxDoPickUserId) {
        this.nxDoPickUserId = nxDoPickUserId;
    }

    public Integer getNxDoAccountUserId() {
        return nxDoAccountUserId;
    }

    public void setNxDoAccountUserId(Integer nxDoAccountUserId) {
        this.nxDoAccountUserId = nxDoAccountUserId;
    }

    public Integer getNxDoPurchaseUserId() {
        return nxDoPurchaseUserId;
    }

    public void setNxDoPurchaseUserId(Integer nxDoPurchaseUserId) {
        this.nxDoPurchaseUserId = nxDoPurchaseUserId;
    }

    public Integer getNxDoPurchaseStatus() {
        return nxDoPurchaseStatus;
    }

    public void setNxDoPurchaseStatus(Integer nxDoPurchaseStatus) {
        this.nxDoPurchaseStatus = nxDoPurchaseStatus;
    }

    public String getNxDoApplyDate() {
        return nxDoApplyDate;
    }

    public void setNxDoApplyDate(String nxDoApplyDate) {
        this.nxDoApplyDate = nxDoApplyDate;
    }

    public String getNxDoApplyFullTime() {
        return nxDoApplyFullTime;
    }

    public void setNxDoApplyFullTime(String nxDoApplyFullTime) {
        this.nxDoApplyFullTime = nxDoApplyFullTime;
    }

    public String getNxDoGoodsName() {
        return nxDoGoodsName;
    }

    public void setNxDoGoodsName(String nxDoGoodsName) {
        this.nxDoGoodsName = nxDoGoodsName;
    }

    public String getNxDoArriveOnlyDate() {
        return nxDoArriveOnlyDate;
    }

    public void setNxDoArriveOnlyDate(String nxDoArriveOnlyDate) {
        this.nxDoArriveOnlyDate = nxDoArriveOnlyDate;
    }

    public String getNxDoArriveDate() {
        return nxDoArriveDate;
    }

    public void setNxDoArriveDate(String nxDoArriveDate) {
        this.nxDoArriveDate = nxDoArriveDate;
    }

    public Integer getNxDoArriveWeeksYear() {
        return nxDoArriveWeeksYear;
    }

    public void setNxDoArriveWeeksYear(Integer nxDoArriveWeeksYear) {
        this.nxDoArriveWeeksYear = nxDoArriveWeeksYear;
    }

    public Integer getNxDoPurchaseGoodsId() {
        return nxDoPurchaseGoodsId;
    }

    public void setNxDoPurchaseGoodsId(Integer nxDoPurchaseGoodsId) {
        this.nxDoPurchaseGoodsId = nxDoPurchaseGoodsId;
    }

    public String getNxDoOperationTime() {
        return nxDoOperationTime;
    }

    public void setNxDoOperationTime(String nxDoOperationTime) {
        this.nxDoOperationTime = nxDoOperationTime;
    }

    public String getNxDoArriveWhatDay() {
        return nxDoArriveWhatDay;
    }

    public void setNxDoArriveWhatDay(String nxDoArriveWhatDay) {
        this.nxDoArriveWhatDay = nxDoArriveWhatDay;
    }

    public Integer getNxDoIsAgent() {
        return nxDoIsAgent;
    }

    public void setNxDoIsAgent(Integer nxDoIsAgent) {
        this.nxDoIsAgent = nxDoIsAgent;
    }

    public String getNxDoApplyOnlyTime() {
        return nxDoApplyOnlyTime;
    }

    public void setNxDoApplyOnlyTime(String nxDoApplyOnlyTime) {
        this.nxDoApplyOnlyTime = nxDoApplyOnlyTime;
    }

    public String getNxDoCostPrice() {
        return nxDoCostPrice;
    }

    public void setNxDoCostPrice(String nxDoCostPrice) {
        this.nxDoCostPrice = nxDoCostPrice;
    }

    public String getNxDoCostSubtotal() {
        return nxDoCostSubtotal;
    }

    public void setNxDoCostSubtotal(String nxDoCostSubtotal) {
        this.nxDoCostSubtotal = nxDoCostSubtotal;
    }

    public String getNxDoPrintStandard() {
        return nxDoPrintStandard;
    }

    public void setNxDoPrintStandard(String nxDoPrintStandard) {
        this.nxDoPrintStandard = nxDoPrintStandard;
    }

    public String getNxDoExpectPrice() {
        return nxDoExpectPrice;
    }

    public void setNxDoExpectPrice(String nxDoExpectPrice) {
        this.nxDoExpectPrice = nxDoExpectPrice;
    }

    public String getNxDoReturnWeight() {
        return nxDoReturnWeight;
    }

    public void setNxDoReturnWeight(String nxDoReturnWeight) {
        this.nxDoReturnWeight = nxDoReturnWeight;
    }

    public String getNxDoReturnSubtotal() {
        return nxDoReturnSubtotal;
    }

    public void setNxDoReturnSubtotal(String nxDoReturnSubtotal) {
        this.nxDoReturnSubtotal = nxDoReturnSubtotal;
    }

    public Integer getNxDoReturnBillId() {
        return nxDoReturnBillId;
    }

    public void setNxDoReturnBillId(Integer nxDoReturnBillId) {
        this.nxDoReturnBillId = nxDoReturnBillId;
    }

    public Integer getNxDoReturnStatus() {
        return nxDoReturnStatus;
    }

    public void setNxDoReturnStatus(Integer nxDoReturnStatus) {
        this.nxDoReturnStatus = nxDoReturnStatus;
    }

    public Integer getNxDoGbDistributerId() {
        return nxDoGbDistributerId;
    }

    public void setNxDoGbDistributerId(Integer nxDoGbDistributerId) {
        this.nxDoGbDistributerId = nxDoGbDistributerId;
    }

    public Integer getNxDoGbDepartmentId() {
        return nxDoGbDepartmentId;
    }

    public void setNxDoGbDepartmentId(Integer nxDoGbDepartmentId) {
        this.nxDoGbDepartmentId = nxDoGbDepartmentId;
    }

    public Integer getNxDoGbDepartmentFatherId() {
        return nxDoGbDepartmentFatherId;
    }

    public void setNxDoGbDepartmentFatherId(Integer nxDoGbDepartmentFatherId) {
        this.nxDoGbDepartmentFatherId = nxDoGbDepartmentFatherId;
    }

    public Integer getNxDoWeightId() {
        return nxDoWeightId;
    }

    public void setNxDoWeightId(Integer nxDoWeightId) {
        this.nxDoWeightId = nxDoWeightId;
    }

    public Integer getNxDoNxCommunityId() {
        return nxDoNxCommunityId;
    }

    public void setNxDoNxCommunityId(Integer nxDoNxCommunityId) {
        this.nxDoNxCommunityId = nxDoNxCommunityId;
    }

    public Integer getNxDoNxCommRestrauntId() {
        return nxDoNxCommRestrauntId;
    }

    public void setNxDoNxCommRestrauntId(Integer nxDoNxCommRestrauntId) {
        this.nxDoNxCommRestrauntId = nxDoNxCommRestrauntId;
    }

    public Integer getNxDoNxCommRestrauntFatherId() {
        return nxDoNxCommRestrauntFatherId;
    }

    public void setNxDoNxCommRestrauntFatherId(Integer nxDoNxCommRestrauntFatherId) {
        this.nxDoNxCommRestrauntFatherId = nxDoNxCommRestrauntFatherId;
    }

    public Integer getNxDoGbDepartmentOrderId() {
        return nxDoGbDepartmentOrderId;
    }

    public void setNxDoGbDepartmentOrderId(Integer nxDoGbDepartmentOrderId) {
        this.nxDoGbDepartmentOrderId = nxDoGbDepartmentOrderId;
    }

    public Integer getNxDoNxRestrauntOrderId() {
        return nxDoNxRestrauntOrderId;
    }

    public void setNxDoNxRestrauntOrderId(Integer nxDoNxRestrauntOrderId) {
        this.nxDoNxRestrauntOrderId = nxDoNxRestrauntOrderId;
    }

    public Integer getNxDoGoodsType() {
        return nxDoGoodsType;
    }

    public void setNxDoGoodsType(Integer nxDoGoodsType) {
        this.nxDoGoodsType = nxDoGoodsType;
    }

    public Integer getNxDoTodayOrder() {
        return nxDoTodayOrder;
    }

    public void setNxDoTodayOrder(Integer nxDoTodayOrder) {
        this.nxDoTodayOrder = nxDoTodayOrder;
    }

    public Integer getNxDoGbDepDisGoodId() {
        return nxDoGbDepDisGoodId;
    }

    public void setNxDoGbDepDisGoodId(Integer nxDoGbDepDisGoodId) {
        this.nxDoGbDepDisGoodId = nxDoGbDepDisGoodId;
    }

    public String getNxDoPriceDifferent() {
        return nxDoPriceDifferent;
    }

    public void setNxDoPriceDifferent(String nxDoPriceDifferent) {
        this.nxDoPriceDifferent = nxDoPriceDifferent;
    }

    public String getNxDoProfitSubtotal() {
        return nxDoProfitSubtotal;
    }

    public void setNxDoProfitSubtotal(String nxDoProfitSubtotal) {
        this.nxDoProfitSubtotal = nxDoProfitSubtotal;
    }

    public String getNxDoProfitScale() {
        return nxDoProfitScale;
    }

    public void setNxDoProfitScale(String nxDoProfitScale) {
        this.nxDoProfitScale = nxDoProfitScale;
    }

    public String getNxDoCostPriceUpdate() {
        return nxDoCostPriceUpdate;
    }

    public void setNxDoCostPriceUpdate(String nxDoCostPriceUpdate) {
        this.nxDoCostPriceUpdate = nxDoCostPriceUpdate;
    }

    public String getNxDoCostPriceLevel() {
        return nxDoCostPriceLevel;
    }

    public void setNxDoCostPriceLevel(String nxDoCostPriceLevel) {
        this.nxDoCostPriceLevel = nxDoCostPriceLevel;
    }

    public NxDistributerGoodsEntity getNxDistributerGoodsEntity() {
        return nxDistributerGoodsEntity;
    }

    public void setNxDistributerGoodsEntity(NxDistributerGoodsEntity nxDistributerGoodsEntity) {
        this.nxDistributerGoodsEntity = nxDistributerGoodsEntity;
    }

    public NxDepartmentEntity getNxDepartmentEntity() {
        return nxDepartmentEntity;
    }

    public void setNxDepartmentEntity(NxDepartmentEntity nxDepartmentEntity) {
        this.nxDepartmentEntity = nxDepartmentEntity;
    }

    public List<NxDistributerGoodsEntity> getNxDistributerGoodsEntityList() {
        return nxDistributerGoodsEntityList;
    }

    public void setNxDistributerGoodsEntityList(List<NxDistributerGoodsEntity> nxDistributerGoodsEntityList) {
        this.nxDistributerGoodsEntityList = nxDistributerGoodsEntityList;
    }

    public Boolean getOnFocus() {
        return onFocus;
    }

    public void setOnFocus(Boolean onFocus) {
        this.onFocus = onFocus;
    }

    public Boolean getHasChoice() {
        return hasChoice;
    }

    public void setHasChoice(Boolean hasChoice) {
        this.hasChoice = hasChoice;
    }

    public Boolean getNotice() {
        return isNotice;
    }

    public void setNotice(Boolean notice) {
        isNotice = notice;
    }

    public Boolean getShowDate() {
        return showDate;
    }

    public void setShowDate(Boolean showDate) {
        this.showDate = showDate;
    }

    public Boolean getWeeks() {
        return isWeeks;
    }

    public void setWeeks(Boolean weeks) {
        isWeeks = weeks;
    }

    public Boolean getPurSelected() {
        return purSelected;
    }

    public void setPurSelected(Boolean purSelected) {
        this.purSelected = purSelected;
    }



}
