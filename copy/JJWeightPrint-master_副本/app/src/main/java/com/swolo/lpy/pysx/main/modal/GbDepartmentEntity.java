package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GbDepartmentEntity implements Serializable {


   /**
    *  订货部门id
    */
   public Integer gbDepartmentId;
   /**
    *  订货部门名称
    */
   public String gbDepartmentName;
   /**
    *  订货部门上级id
    */
   public Integer gbDepartmentFatherId;
   /**
    *  订货部门类型
    */
   public Integer gbDepartmentType;
   /**
    *  订货部门子部门数量
    */
   public Integer gbDepartmentSubAmount;
   /**
    *  订货部门批发商id
    */
   public Integer gbDepartmentDisId;
   /**
    *
    */
   public String gbDepartmentFilePath;
   /**
    *  是客户吗
    */
   public Integer gbDepartmentIsGroupDep;
   /**
    *
    */
   public String gbDepartmentPrintName;
   /**
    *
    */
   public Integer gbDepartmentShowWeeks;
   /**
    *
    */
   public Integer gbDepartmentSettleType;
   /**
    *  客户简称
    */
   public String gbDepartmentAttrName;
   public String gbDepartmentNamePy;

   public Integer gbDepartmentRouteId;

   public String gbDepartmentSettleFullTime;
   public String gbDepartmentSettleDate;
   public String gbDepartmentSettleWeek;
   public String gbDepartmentSettleMonth;
   public String gbDepartmentSettleYear;
   public String gbDepartmentSettleTimes;

   public String unPayTotal;
   public Integer gbDepartmentDepSettleId;
   public Integer gbDepartmentLevel;

   public String gbDepartmentUnPayTotal;
   public Integer gbDepartmentAddCount;
   public Integer gbDepartmentPurOrderCount;
   public Integer gbDepartmentNeedNotPurOrderCount;


   public GbDepartmentEntity getFatherGbDepartmentEntity() {
      return fatherGbDepartmentEntity;
   }

   public void setFatherGbDepartmentEntity(GbDepartmentEntity fatherGbDepartmentEntity) {
      this.fatherGbDepartmentEntity = fatherGbDepartmentEntity;
   }

   public GbDepartmentEntity fatherGbDepartmentEntity;

   public List<GbDepartmentEntity> gbDepartmentEntityList;


   public List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;


   public Boolean isSelected = false;

   public Double depWasteGoodsTotal = 0.0;
   public String depWasteGoodsTotalString;
   public Double depWasteGoodsWeightTotal = 0.0;
   public String depWasteGoodsWeightTotalString;

   public Double depReturnGoodsWeightTotal = 0.0;
   public String depReturnGoodsWeightTotalString;

   public Double depReturnGoodsTotal = 0.0;
   public String depReturnGoodsTotalString;
   public Double depLossGoodsTotal  = 0.0;
   public String depLossGoodsTotalString;
   public Double depLossGoodsWeightTotal  = 0.0;
   public String depLossGoodsWeightTotalString;

   public Double depRestGoodsTotal  = 0.0;
   public String depRestGoodsTotalString;
   public Double depRestGoodsWeightTotal  = 0.0;
   public String depRestGoodsWeightTotalString;

   public Double depProduceGoodsTotal  = 0.0;
   public String depProduceGoodsPercent;
   public String depProducePercent;
   public String depLossPercent;
   public String depWastePercent;
   public String depLossGoodsPercent;
   public String depWasteGoodsPercent;
   public String depProduceGoodsTotalString;
   public Double depCostGoodsTotal  = 0.0;
   public String depCostGoodsTotalString;
   public Double DepProduceGoodsWeightTotal;
   public String depProduceGoodsWeightTotalString;
   public Double depProfitGoodsTotal;
   public String depProfitGoodsTotalString;

   public Double depWasteGoodsEveryTotal  = 0.0;
   public String depWasteGoodsEveryTotalString;
   public Double depWasteGoodsEveryWeightTotal  = 0.0;
   public String depWasteGoodsEveryWeightTotalString;

   public Double DepLossGoodsEveryTotal  = 0.0;
   public String depLossGoodsEveryTotalString;
   public Double DepLossGoodsEveryWeightTotal  = 0.0;
   public String depLossGoodsEveryWeightTotalString;

   public Double DepProduceGoodsEveryTotal  = 0.0;
   public String depProduceGoodsEveryTotalString;
   public Double DepProduceGoodsEveryWeightTotal  = 0.0;
   public String depProduceGoodsEveryWeightTotalString;

   public String newOrderAmount ;
   public String prepareOrderAmount;
   public String hasWeightOrderAmount;
   public String updateSubtotal;
   public Integer gbDepartmentPrintSet;



   public String depFreshRateString;
   public Double depFreshRate;
   public String depClearTimeString;
   public Double depClearTime;
   public String depCostRateString;
   public Double depCostRate;
   public String depSalesRateString;
   public Double depSalesRate;

   public String depLossRateString;
   public Double depLossRate;

   public String depWasteRateString;
   public Double depWasteRate;


   public Double depCostUseStockTotal = 0.0;
   public Double depCostWasteStockTotal = 0.0;
   public Double depCostLossStockTotal = 0.0;
   public Double depPurchaseTotal = 0.0;
   public Double depPurchaseLowerTotal = 0.0;
   public Double depPurchaseHigherTotal = 0.0;

   public Double depStockSubtotal;
   public String depStockSubtotalString;
   public Double depStockWeightTotal;
   public String depStockWeightTotalString;
   public String depStockAverageSubtotal;
   public Double depStockMany;
   public String depStockManyString;

   public String gbDepartmentLatitude;

   public Integer getGbDepartmentId() {
      return gbDepartmentId;
   }

   public void setGbDepartmentId(Integer gbDepartmentId) {
      this.gbDepartmentId = gbDepartmentId;
   }

   public String getGbDepartmentName() {
      return gbDepartmentName;
   }

   public void setGbDepartmentName(String gbDepartmentName) {
      this.gbDepartmentName = gbDepartmentName;
   }

   public Integer getGbDepartmentFatherId() {
      return gbDepartmentFatherId;
   }

   public void setGbDepartmentFatherId(Integer gbDepartmentFatherId) {
      this.gbDepartmentFatherId = gbDepartmentFatherId;
   }

   public Integer getGbDepartmentType() {
      return gbDepartmentType;
   }

   public void setGbDepartmentType(Integer gbDepartmentType) {
      this.gbDepartmentType = gbDepartmentType;
   }

   public Integer getGbDepartmentSubAmount() {
      return gbDepartmentSubAmount;
   }

   public void setGbDepartmentSubAmount(Integer gbDepartmentSubAmount) {
      this.gbDepartmentSubAmount = gbDepartmentSubAmount;
   }

   public Integer getGbDepartmentDisId() {
      return gbDepartmentDisId;
   }

   public void setGbDepartmentDisId(Integer gbDepartmentDisId) {
      this.gbDepartmentDisId = gbDepartmentDisId;
   }

   public String getGbDepartmentFilePath() {
      return gbDepartmentFilePath;
   }

   public void setGbDepartmentFilePath(String gbDepartmentFilePath) {
      this.gbDepartmentFilePath = gbDepartmentFilePath;
   }

   public Integer getGbDepartmentIsGroupDep() {
      return gbDepartmentIsGroupDep;
   }

   public void setGbDepartmentIsGroupDep(Integer gbDepartmentIsGroupDep) {
      this.gbDepartmentIsGroupDep = gbDepartmentIsGroupDep;
   }

   public String getGbDepartmentPrintName() {
      return gbDepartmentPrintName;
   }

   public void setGbDepartmentPrintName(String gbDepartmentPrintName) {
      this.gbDepartmentPrintName = gbDepartmentPrintName;
   }

   public Integer getGbDepartmentShowWeeks() {
      return gbDepartmentShowWeeks;
   }

   public void setGbDepartmentShowWeeks(Integer gbDepartmentShowWeeks) {
      this.gbDepartmentShowWeeks = gbDepartmentShowWeeks;
   }

   public Integer getGbDepartmentSettleType() {
      return gbDepartmentSettleType;
   }

   public void setGbDepartmentSettleType(Integer gbDepartmentSettleType) {
      this.gbDepartmentSettleType = gbDepartmentSettleType;
   }

   public String getGbDepartmentAttrName() {
      return gbDepartmentAttrName;
   }

   public void setGbDepartmentAttrName(String gbDepartmentAttrName) {
      this.gbDepartmentAttrName = gbDepartmentAttrName;
   }

   public String getGbDepartmentNamePy() {
      return gbDepartmentNamePy;
   }

   public void setGbDepartmentNamePy(String gbDepartmentNamePy) {
      this.gbDepartmentNamePy = gbDepartmentNamePy;
   }

   public Integer getGbDepartmentRouteId() {
      return gbDepartmentRouteId;
   }

   public void setGbDepartmentRouteId(Integer gbDepartmentRouteId) {
      this.gbDepartmentRouteId = gbDepartmentRouteId;
   }

   public String getGbDepartmentSettleFullTime() {
      return gbDepartmentSettleFullTime;
   }

   public void setGbDepartmentSettleFullTime(String gbDepartmentSettleFullTime) {
      this.gbDepartmentSettleFullTime = gbDepartmentSettleFullTime;
   }

   public String getGbDepartmentSettleDate() {
      return gbDepartmentSettleDate;
   }

   public void setGbDepartmentSettleDate(String gbDepartmentSettleDate) {
      this.gbDepartmentSettleDate = gbDepartmentSettleDate;
   }

   public String getGbDepartmentSettleWeek() {
      return gbDepartmentSettleWeek;
   }

   public void setGbDepartmentSettleWeek(String gbDepartmentSettleWeek) {
      this.gbDepartmentSettleWeek = gbDepartmentSettleWeek;
   }

   public String getGbDepartmentSettleMonth() {
      return gbDepartmentSettleMonth;
   }

   public void setGbDepartmentSettleMonth(String gbDepartmentSettleMonth) {
      this.gbDepartmentSettleMonth = gbDepartmentSettleMonth;
   }

   public String getGbDepartmentSettleYear() {
      return gbDepartmentSettleYear;
   }

   public void setGbDepartmentSettleYear(String gbDepartmentSettleYear) {
      this.gbDepartmentSettleYear = gbDepartmentSettleYear;
   }

   public String getGbDepartmentSettleTimes() {
      return gbDepartmentSettleTimes;
   }

   public void setGbDepartmentSettleTimes(String gbDepartmentSettleTimes) {
      this.gbDepartmentSettleTimes = gbDepartmentSettleTimes;
   }

   public String getUnPayTotal() {
      return unPayTotal;
   }

   public void setUnPayTotal(String unPayTotal) {
      this.unPayTotal = unPayTotal;
   }

   public Integer getGbDepartmentDepSettleId() {
      return gbDepartmentDepSettleId;
   }

   public void setGbDepartmentDepSettleId(Integer gbDepartmentDepSettleId) {
      this.gbDepartmentDepSettleId = gbDepartmentDepSettleId;
   }

   public Integer getGbDepartmentLevel() {
      return gbDepartmentLevel;
   }

   public void setGbDepartmentLevel(Integer gbDepartmentLevel) {
      this.gbDepartmentLevel = gbDepartmentLevel;
   }

   public String getGbDepartmentUnPayTotal() {
      return gbDepartmentUnPayTotal;
   }

   public void setGbDepartmentUnPayTotal(String gbDepartmentUnPayTotal) {
      this.gbDepartmentUnPayTotal = gbDepartmentUnPayTotal;
   }

   public Integer getGbDepartmentAddCount() {
      return gbDepartmentAddCount;
   }

   public void setGbDepartmentAddCount(Integer gbDepartmentAddCount) {
      this.gbDepartmentAddCount = gbDepartmentAddCount;
   }

   public Integer getGbDepartmentPurOrderCount() {
      return gbDepartmentPurOrderCount;
   }

   public void setGbDepartmentPurOrderCount(Integer gbDepartmentPurOrderCount) {
      this.gbDepartmentPurOrderCount = gbDepartmentPurOrderCount;
   }

   public Integer getGbDepartmentNeedNotPurOrderCount() {
      return gbDepartmentNeedNotPurOrderCount;
   }

   public void setGbDepartmentNeedNotPurOrderCount(Integer gbDepartmentNeedNotPurOrderCount) {
      this.gbDepartmentNeedNotPurOrderCount = gbDepartmentNeedNotPurOrderCount;
   }




   public List<GbDepartmentEntity> getGbDepartmentEntityList() {
      return gbDepartmentEntityList;
   }

   public void setGbDepartmentEntityList(List<GbDepartmentEntity> gbDepartmentEntityList) {
      this.gbDepartmentEntityList = gbDepartmentEntityList;
   }

   public List<NxDepartmentOrdersEntity> getNxDepartmentOrdersEntities() {
      return nxDepartmentOrdersEntities;
   }

   public void setNxDepartmentOrdersEntities(List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities) {
      this.nxDepartmentOrdersEntities = nxDepartmentOrdersEntities;
   }

   public Boolean getSelected() {
      return isSelected;
   }

   public void setSelected(Boolean selected) {
      isSelected = selected;
   }

   public Double getDepWasteGoodsTotal() {
      return depWasteGoodsTotal;
   }

   public void setDepWasteGoodsTotal(Double depWasteGoodsTotal) {
      this.depWasteGoodsTotal = depWasteGoodsTotal;
   }

   public String getDepWasteGoodsTotalString() {
      return depWasteGoodsTotalString;
   }

   public void setDepWasteGoodsTotalString(String depWasteGoodsTotalString) {
      this.depWasteGoodsTotalString = depWasteGoodsTotalString;
   }

   public Double getDepWasteGoodsWeightTotal() {
      return depWasteGoodsWeightTotal;
   }

   public void setDepWasteGoodsWeightTotal(Double depWasteGoodsWeightTotal) {
      this.depWasteGoodsWeightTotal = depWasteGoodsWeightTotal;
   }

   public String getDepWasteGoodsWeightTotalString() {
      return depWasteGoodsWeightTotalString;
   }

   public void setDepWasteGoodsWeightTotalString(String depWasteGoodsWeightTotalString) {
      this.depWasteGoodsWeightTotalString = depWasteGoodsWeightTotalString;
   }

   public Double getDepReturnGoodsWeightTotal() {
      return depReturnGoodsWeightTotal;
   }

   public void setDepReturnGoodsWeightTotal(Double depReturnGoodsWeightTotal) {
      this.depReturnGoodsWeightTotal = depReturnGoodsWeightTotal;
   }

   public String getDepReturnGoodsWeightTotalString() {
      return depReturnGoodsWeightTotalString;
   }

   public void setDepReturnGoodsWeightTotalString(String depReturnGoodsWeightTotalString) {
      this.depReturnGoodsWeightTotalString = depReturnGoodsWeightTotalString;
   }

   public Double getDepReturnGoodsTotal() {
      return depReturnGoodsTotal;
   }

   public void setDepReturnGoodsTotal(Double depReturnGoodsTotal) {
      this.depReturnGoodsTotal = depReturnGoodsTotal;
   }

   public String getDepReturnGoodsTotalString() {
      return depReturnGoodsTotalString;
   }

   public void setDepReturnGoodsTotalString(String depReturnGoodsTotalString) {
      this.depReturnGoodsTotalString = depReturnGoodsTotalString;
   }

   public Double getDepLossGoodsTotal() {
      return depLossGoodsTotal;
   }

   public void setDepLossGoodsTotal(Double depLossGoodsTotal) {
      this.depLossGoodsTotal = depLossGoodsTotal;
   }

   public String getDepLossGoodsTotalString() {
      return depLossGoodsTotalString;
   }

   public void setDepLossGoodsTotalString(String depLossGoodsTotalString) {
      this.depLossGoodsTotalString = depLossGoodsTotalString;
   }

   public Double getDepLossGoodsWeightTotal() {
      return depLossGoodsWeightTotal;
   }

   public void setDepLossGoodsWeightTotal(Double depLossGoodsWeightTotal) {
      this.depLossGoodsWeightTotal = depLossGoodsWeightTotal;
   }

   public String getDepLossGoodsWeightTotalString() {
      return depLossGoodsWeightTotalString;
   }

   public void setDepLossGoodsWeightTotalString(String depLossGoodsWeightTotalString) {
      this.depLossGoodsWeightTotalString = depLossGoodsWeightTotalString;
   }

   public Double getDepRestGoodsTotal() {
      return depRestGoodsTotal;
   }

   public void setDepRestGoodsTotal(Double depRestGoodsTotal) {
      this.depRestGoodsTotal = depRestGoodsTotal;
   }

   public String getDepRestGoodsTotalString() {
      return depRestGoodsTotalString;
   }

   public void setDepRestGoodsTotalString(String depRestGoodsTotalString) {
      this.depRestGoodsTotalString = depRestGoodsTotalString;
   }

   public Double getDepRestGoodsWeightTotal() {
      return depRestGoodsWeightTotal;
   }

   public void setDepRestGoodsWeightTotal(Double depRestGoodsWeightTotal) {
      this.depRestGoodsWeightTotal = depRestGoodsWeightTotal;
   }

   public String getDepRestGoodsWeightTotalString() {
      return depRestGoodsWeightTotalString;
   }

   public void setDepRestGoodsWeightTotalString(String depRestGoodsWeightTotalString) {
      this.depRestGoodsWeightTotalString = depRestGoodsWeightTotalString;
   }

   public Double getDepProduceGoodsTotal() {
      return depProduceGoodsTotal;
   }

   public void setDepProduceGoodsTotal(Double depProduceGoodsTotal) {
      this.depProduceGoodsTotal = depProduceGoodsTotal;
   }

   public String getDepProduceGoodsPercent() {
      return depProduceGoodsPercent;
   }

   public void setDepProduceGoodsPercent(String depProduceGoodsPercent) {
      this.depProduceGoodsPercent = depProduceGoodsPercent;
   }

   public String getDepProducePercent() {
      return depProducePercent;
   }

   public void setDepProducePercent(String depProducePercent) {
      this.depProducePercent = depProducePercent;
   }

   public String getDepLossPercent() {
      return depLossPercent;
   }

   public void setDepLossPercent(String depLossPercent) {
      this.depLossPercent = depLossPercent;
   }

   public String getDepWastePercent() {
      return depWastePercent;
   }

   public void setDepWastePercent(String depWastePercent) {
      this.depWastePercent = depWastePercent;
   }

   public String getDepLossGoodsPercent() {
      return depLossGoodsPercent;
   }

   public void setDepLossGoodsPercent(String depLossGoodsPercent) {
      this.depLossGoodsPercent = depLossGoodsPercent;
   }

   public String getDepWasteGoodsPercent() {
      return depWasteGoodsPercent;
   }

   public void setDepWasteGoodsPercent(String depWasteGoodsPercent) {
      this.depWasteGoodsPercent = depWasteGoodsPercent;
   }

   public String getDepProduceGoodsTotalString() {
      return depProduceGoodsTotalString;
   }

   public void setDepProduceGoodsTotalString(String depProduceGoodsTotalString) {
      this.depProduceGoodsTotalString = depProduceGoodsTotalString;
   }

   public Double getDepCostGoodsTotal() {
      return depCostGoodsTotal;
   }

   public void setDepCostGoodsTotal(Double depCostGoodsTotal) {
      this.depCostGoodsTotal = depCostGoodsTotal;
   }

   public String getDepCostGoodsTotalString() {
      return depCostGoodsTotalString;
   }

   public void setDepCostGoodsTotalString(String depCostGoodsTotalString) {
      this.depCostGoodsTotalString = depCostGoodsTotalString;
   }

   public Double getDepProduceGoodsWeightTotal() {
      return DepProduceGoodsWeightTotal;
   }

   public void setDepProduceGoodsWeightTotal(Double depProduceGoodsWeightTotal) {
      DepProduceGoodsWeightTotal = depProduceGoodsWeightTotal;
   }

   public String getDepProduceGoodsWeightTotalString() {
      return depProduceGoodsWeightTotalString;
   }

   public void setDepProduceGoodsWeightTotalString(String depProduceGoodsWeightTotalString) {
      this.depProduceGoodsWeightTotalString = depProduceGoodsWeightTotalString;
   }

   public Double getDepProfitGoodsTotal() {
      return depProfitGoodsTotal;
   }

   public void setDepProfitGoodsTotal(Double depProfitGoodsTotal) {
      this.depProfitGoodsTotal = depProfitGoodsTotal;
   }

   public String getDepProfitGoodsTotalString() {
      return depProfitGoodsTotalString;
   }

   public void setDepProfitGoodsTotalString(String depProfitGoodsTotalString) {
      this.depProfitGoodsTotalString = depProfitGoodsTotalString;
   }

   public Double getDepWasteGoodsEveryTotal() {
      return depWasteGoodsEveryTotal;
   }

   public void setDepWasteGoodsEveryTotal(Double depWasteGoodsEveryTotal) {
      this.depWasteGoodsEveryTotal = depWasteGoodsEveryTotal;
   }

   public String getDepWasteGoodsEveryTotalString() {
      return depWasteGoodsEveryTotalString;
   }

   public void setDepWasteGoodsEveryTotalString(String depWasteGoodsEveryTotalString) {
      this.depWasteGoodsEveryTotalString = depWasteGoodsEveryTotalString;
   }

   public Double getDepWasteGoodsEveryWeightTotal() {
      return depWasteGoodsEveryWeightTotal;
   }

   public void setDepWasteGoodsEveryWeightTotal(Double depWasteGoodsEveryWeightTotal) {
      this.depWasteGoodsEveryWeightTotal = depWasteGoodsEveryWeightTotal;
   }

   public String getDepWasteGoodsEveryWeightTotalString() {
      return depWasteGoodsEveryWeightTotalString;
   }

   public void setDepWasteGoodsEveryWeightTotalString(String depWasteGoodsEveryWeightTotalString) {
      this.depWasteGoodsEveryWeightTotalString = depWasteGoodsEveryWeightTotalString;
   }

   public Double getDepLossGoodsEveryTotal() {
      return DepLossGoodsEveryTotal;
   }

   public void setDepLossGoodsEveryTotal(Double depLossGoodsEveryTotal) {
      DepLossGoodsEveryTotal = depLossGoodsEveryTotal;
   }

   public String getDepLossGoodsEveryTotalString() {
      return depLossGoodsEveryTotalString;
   }

   public void setDepLossGoodsEveryTotalString(String depLossGoodsEveryTotalString) {
      this.depLossGoodsEveryTotalString = depLossGoodsEveryTotalString;
   }

   public Double getDepLossGoodsEveryWeightTotal() {
      return DepLossGoodsEveryWeightTotal;
   }

   public void setDepLossGoodsEveryWeightTotal(Double depLossGoodsEveryWeightTotal) {
      DepLossGoodsEveryWeightTotal = depLossGoodsEveryWeightTotal;
   }

   public String getDepLossGoodsEveryWeightTotalString() {
      return depLossGoodsEveryWeightTotalString;
   }

   public void setDepLossGoodsEveryWeightTotalString(String depLossGoodsEveryWeightTotalString) {
      this.depLossGoodsEveryWeightTotalString = depLossGoodsEveryWeightTotalString;
   }

   public Double getDepProduceGoodsEveryTotal() {
      return DepProduceGoodsEveryTotal;
   }

   public void setDepProduceGoodsEveryTotal(Double depProduceGoodsEveryTotal) {
      DepProduceGoodsEveryTotal = depProduceGoodsEveryTotal;
   }

   public String getDepProduceGoodsEveryTotalString() {
      return depProduceGoodsEveryTotalString;
   }

   public void setDepProduceGoodsEveryTotalString(String depProduceGoodsEveryTotalString) {
      this.depProduceGoodsEveryTotalString = depProduceGoodsEveryTotalString;
   }

   public Double getDepProduceGoodsEveryWeightTotal() {
      return DepProduceGoodsEveryWeightTotal;
   }

   public void setDepProduceGoodsEveryWeightTotal(Double depProduceGoodsEveryWeightTotal) {
      DepProduceGoodsEveryWeightTotal = depProduceGoodsEveryWeightTotal;
   }

   public String getDepProduceGoodsEveryWeightTotalString() {
      return depProduceGoodsEveryWeightTotalString;
   }

   public void setDepProduceGoodsEveryWeightTotalString(String depProduceGoodsEveryWeightTotalString) {
      this.depProduceGoodsEveryWeightTotalString = depProduceGoodsEveryWeightTotalString;
   }

   public String getNewOrderAmount() {
      return newOrderAmount;
   }

   public void setNewOrderAmount(String newOrderAmount) {
      this.newOrderAmount = newOrderAmount;
   }

   public String getPrepareOrderAmount() {
      return prepareOrderAmount;
   }

   public void setPrepareOrderAmount(String prepareOrderAmount) {
      this.prepareOrderAmount = prepareOrderAmount;
   }

   public String getHasWeightOrderAmount() {
      return hasWeightOrderAmount;
   }

   public void setHasWeightOrderAmount(String hasWeightOrderAmount) {
      this.hasWeightOrderAmount = hasWeightOrderAmount;
   }

   public String getUpdateSubtotal() {
      return updateSubtotal;
   }

   public void setUpdateSubtotal(String updateSubtotal) {
      this.updateSubtotal = updateSubtotal;
   }

   public Integer getGbDepartmentPrintSet() {
      return gbDepartmentPrintSet;
   }

   public void setGbDepartmentPrintSet(Integer gbDepartmentPrintSet) {
      this.gbDepartmentPrintSet = gbDepartmentPrintSet;
   }

   public String getDepFreshRateString() {
      return depFreshRateString;
   }

   public void setDepFreshRateString(String depFreshRateString) {
      this.depFreshRateString = depFreshRateString;
   }

   public Double getDepFreshRate() {
      return depFreshRate;
   }

   public void setDepFreshRate(Double depFreshRate) {
      this.depFreshRate = depFreshRate;
   }

   public String getDepClearTimeString() {
      return depClearTimeString;
   }

   public void setDepClearTimeString(String depClearTimeString) {
      this.depClearTimeString = depClearTimeString;
   }

   public Double getDepClearTime() {
      return depClearTime;
   }

   public void setDepClearTime(Double depClearTime) {
      this.depClearTime = depClearTime;
   }

   public String getDepCostRateString() {
      return depCostRateString;
   }

   public void setDepCostRateString(String depCostRateString) {
      this.depCostRateString = depCostRateString;
   }

   public Double getDepCostRate() {
      return depCostRate;
   }

   public void setDepCostRate(Double depCostRate) {
      this.depCostRate = depCostRate;
   }

   public String getDepSalesRateString() {
      return depSalesRateString;
   }

   public void setDepSalesRateString(String depSalesRateString) {
      this.depSalesRateString = depSalesRateString;
   }

   public Double getDepSalesRate() {
      return depSalesRate;
   }

   public void setDepSalesRate(Double depSalesRate) {
      this.depSalesRate = depSalesRate;
   }

   public String getDepLossRateString() {
      return depLossRateString;
   }

   public void setDepLossRateString(String depLossRateString) {
      this.depLossRateString = depLossRateString;
   }

   public Double getDepLossRate() {
      return depLossRate;
   }

   public void setDepLossRate(Double depLossRate) {
      this.depLossRate = depLossRate;
   }

   public String getDepWasteRateString() {
      return depWasteRateString;
   }

   public void setDepWasteRateString(String depWasteRateString) {
      this.depWasteRateString = depWasteRateString;
   }

   public Double getDepWasteRate() {
      return depWasteRate;
   }

   public void setDepWasteRate(Double depWasteRate) {
      this.depWasteRate = depWasteRate;
   }

   public Double getDepCostUseStockTotal() {
      return depCostUseStockTotal;
   }

   public void setDepCostUseStockTotal(Double depCostUseStockTotal) {
      this.depCostUseStockTotal = depCostUseStockTotal;
   }

   public Double getDepCostWasteStockTotal() {
      return depCostWasteStockTotal;
   }

   public void setDepCostWasteStockTotal(Double depCostWasteStockTotal) {
      this.depCostWasteStockTotal = depCostWasteStockTotal;
   }

   public Double getDepCostLossStockTotal() {
      return depCostLossStockTotal;
   }

   public void setDepCostLossStockTotal(Double depCostLossStockTotal) {
      this.depCostLossStockTotal = depCostLossStockTotal;
   }

   public Double getDepPurchaseTotal() {
      return depPurchaseTotal;
   }

   public void setDepPurchaseTotal(Double depPurchaseTotal) {
      this.depPurchaseTotal = depPurchaseTotal;
   }

   public Double getDepPurchaseLowerTotal() {
      return depPurchaseLowerTotal;
   }

   public void setDepPurchaseLowerTotal(Double depPurchaseLowerTotal) {
      this.depPurchaseLowerTotal = depPurchaseLowerTotal;
   }

   public Double getDepPurchaseHigherTotal() {
      return depPurchaseHigherTotal;
   }

   public void setDepPurchaseHigherTotal(Double depPurchaseHigherTotal) {
      this.depPurchaseHigherTotal = depPurchaseHigherTotal;
   }

   public Double getDepStockSubtotal() {
      return depStockSubtotal;
   }

   public void setDepStockSubtotal(Double depStockSubtotal) {
      this.depStockSubtotal = depStockSubtotal;
   }

   public String getDepStockSubtotalString() {
      return depStockSubtotalString;
   }

   public void setDepStockSubtotalString(String depStockSubtotalString) {
      this.depStockSubtotalString = depStockSubtotalString;
   }

   public Double getDepStockWeightTotal() {
      return depStockWeightTotal;
   }

   public void setDepStockWeightTotal(Double depStockWeightTotal) {
      this.depStockWeightTotal = depStockWeightTotal;
   }

   public String getDepStockWeightTotalString() {
      return depStockWeightTotalString;
   }

   public void setDepStockWeightTotalString(String depStockWeightTotalString) {
      this.depStockWeightTotalString = depStockWeightTotalString;
   }

   public String getDepStockAverageSubtotal() {
      return depStockAverageSubtotal;
   }

   public void setDepStockAverageSubtotal(String depStockAverageSubtotal) {
      this.depStockAverageSubtotal = depStockAverageSubtotal;
   }

   public Double getDepStockMany() {
      return depStockMany;
   }

   public void setDepStockMany(Double depStockMany) {
      this.depStockMany = depStockMany;
   }

   public String getDepStockManyString() {
      return depStockManyString;
   }

   public void setDepStockManyString(String depStockManyString) {
      this.depStockManyString = depStockManyString;
   }

   public String getGbDepartmentLatitude() {
      return gbDepartmentLatitude;
   }

   public void setGbDepartmentLatitude(String gbDepartmentLatitude) {
      this.gbDepartmentLatitude = gbDepartmentLatitude;
   }

   public String getGbDepartmentLongitude() {
      return gbDepartmentLongitude;
   }

   public void setGbDepartmentLongitude(String gbDepartmentLongitude) {
      this.gbDepartmentLongitude = gbDepartmentLongitude;
   }

   public Map<String, Object> getTotalMap() {
      return totalMap;
   }

   public void setTotalMap(Map<String, Object> totalMap) {
      this.totalMap = totalMap;
   }

   public List<String> getDayData() {
      return dayData;
   }

   public void setDayData(List<String> dayData) {
      this.dayData = dayData;
   }

   public String getFatherGoodsIds() {
      return fatherGoodsIds;
   }

   public void setFatherGoodsIds(String fatherGoodsIds) {
      this.fatherGoodsIds = fatherGoodsIds;
   }

   public Integer getCankaoDepId() {
      return cankaoDepId;
   }

   public void setCankaoDepId(Integer cankaoDepId) {
      this.cankaoDepId = cankaoDepId;
   }

   public String gbDepartmentLongitude;

   public Map<String, Object> totalMap;

   public List<String> dayData;
   public String fatherGoodsIds;
   public Integer cankaoDepId;

}
