package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class NxDistributerEntity implements Serializable{


    /**
     *  批发商id
     */
    public Integer nxDistributerId;
    /**
     *  批发商名称
     */
    public String nxDistributerName;
    /**
     *  批发商位置经度
     */
    public String nxDistributerLan;
    /**
     *  批发商位置纬度
     */
    public String nxDistributerLun;

    public Integer getNxDistributerBusinessTypeId() {
        return nxDistributerBusinessTypeId;
    }

    public void setNxDistributerBusinessTypeId(Integer nxDistributerBusinessTypeId) {
        this.nxDistributerBusinessTypeId = nxDistributerBusinessTypeId;
    }

    public String getNxDistributerBuyQuantity() {
        return nxDistributerBuyQuantity;
    }

    public void setNxDistributerBuyQuantity(String nxDistributerBuyQuantity) {
        this.nxDistributerBuyQuantity = nxDistributerBuyQuantity;
    }

    public Integer getNxDistributerType() {
        return nxDistributerType;
    }

    public void setNxDistributerType(Integer nxDistributerType) {
        this.nxDistributerType = nxDistributerType;
    }

    public Integer getNxDistributerShelfQuantity() {
        return nxDistributerShelfQuantity;
    }

    public void setNxDistributerShelfQuantity(Integer nxDistributerShelfQuantity) {
        this.nxDistributerShelfQuantity = nxDistributerShelfQuantity;
    }

    public String getNxDistributerImg() {
        return nxDistributerImg;
    }

    public void setNxDistributerImg(String nxDistributerImg) {
        this.nxDistributerImg = nxDistributerImg;
    }

    public String getNxDistributerAppId() {
        return nxDistributerAppId;
    }

    public void setNxDistributerAppId(String nxDistributerAppId) {
        this.nxDistributerAppId = nxDistributerAppId;
    }

    public String getNxDistributerManager() {
        return nxDistributerManager;
    }

    public void setNxDistributerManager(String nxDistributerManager) {
        this.nxDistributerManager = nxDistributerManager;
    }

    public String getNxDistributerPayUrl() {
        return nxDistributerPayUrl;
    }

    public void setNxDistributerPayUrl(String nxDistributerPayUrl) {
        this.nxDistributerPayUrl = nxDistributerPayUrl;
    }

    public String getNxDistributerPhone() {
        return nxDistributerPhone;
    }

    public void setNxDistributerPhone(String nxDistributerPhone) {
        this.nxDistributerPhone = nxDistributerPhone;
    }

    public String getNxDistributerAddress() {
        return nxDistributerAddress;
    }

    public void setNxDistributerAddress(String nxDistributerAddress) {
        this.nxDistributerAddress = nxDistributerAddress;
    }

    public String getNxDistributerMarketName() {
        return nxDistributerMarketName;
    }

    public void setNxDistributerMarketName(String nxDistributerMarketName) {
        this.nxDistributerMarketName = nxDistributerMarketName;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(double distanceValue) {
        this.distanceValue = distanceValue;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public int getLinshiCount() {
        return linshiCount;
    }

    public void setLinshiCount(int linshiCount) {
        this.linshiCount = linshiCount;
    }

    public Double getPurTimes() {
        return purTimes;
    }

    public void setPurTimes(Double purTimes) {
        this.purTimes = purTimes;
    }

    public String getPurTimesString() {
        return purTimesString;
    }

    public void setPurTimesString(String purTimesString) {
        this.purTimesString = purTimesString;
    }

    public Double getPurGoodsTotal() {
        return purGoodsTotal;
    }

    public void setPurGoodsTotal(Double purGoodsTotal) {
        this.purGoodsTotal = purGoodsTotal;
    }

    public String getPurGoodsTotalString() {
        return purGoodsTotalString;
    }

    public void setPurGoodsTotalString(String purGoodsTotalString) {
        this.purGoodsTotalString = purGoodsTotalString;
    }

    public Double getStockGoodsTotal() {
        return stockGoodsTotal;
    }

    public void setStockGoodsTotal(Double stockGoodsTotal) {
        this.stockGoodsTotal = stockGoodsTotal;
    }

    public String getStockGoodsTotalString() {
        return stockGoodsTotalString;
    }

    public void setStockGoodsTotalString(String stockGoodsTotalString) {
        this.stockGoodsTotalString = stockGoodsTotalString;
    }

    public Double getProduceGoodsTotal() {
        return produceGoodsTotal;
    }

    public void setProduceGoodsTotal(Double produceGoodsTotal) {
        this.produceGoodsTotal = produceGoodsTotal;
    }

    public String getProduceGoodsTotalString() {
        return produceGoodsTotalString;
    }

    public void setProduceGoodsTotalString(String produceGoodsTotalString) {
        this.produceGoodsTotalString = produceGoodsTotalString;
    }

    public Double getWasteGoodsTotal() {
        return wasteGoodsTotal;
    }

    public void setWasteGoodsTotal(Double wasteGoodsTotal) {
        this.wasteGoodsTotal = wasteGoodsTotal;
    }

    public String getWasteGoodsTotalString() {
        return wasteGoodsTotalString;
    }

    public void setWasteGoodsTotalString(String wasteGoodsTotalString) {
        this.wasteGoodsTotalString = wasteGoodsTotalString;
    }

    public Double getLossGoodsTotal() {
        return lossGoodsTotal;
    }

    public void setLossGoodsTotal(Double lossGoodsTotal) {
        this.lossGoodsTotal = lossGoodsTotal;
    }

    public String getLossGoodsTotalString() {
        return lossGoodsTotalString;
    }

    public void setLossGoodsTotalString(String lossGoodsTotalString) {
        this.lossGoodsTotalString = lossGoodsTotalString;
    }

    public Double getReturnGoodsTotal() {
        return returnGoodsTotal;
    }

    public void setReturnGoodsTotal(Double returnGoodsTotal) {
        this.returnGoodsTotal = returnGoodsTotal;
    }

    public String getReturnGoodsTotalString() {
        return returnGoodsTotalString;
    }

    public void setReturnGoodsTotalString(String returnGoodsTotalString) {
        this.returnGoodsTotalString = returnGoodsTotalString;
    }

    public String getNxDistributerShowName() {
        return nxDistributerShowName;
    }

    public void setNxDistributerShowName(String nxDistributerShowName) {
        this.nxDistributerShowName = nxDistributerShowName;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public int getBillCount() {
        return billCount;
    }

    public void setBillCount(int billCount) {
        this.billCount = billCount;
    }

    public String getFreshStars() {
        return freshStars;
    }

    public void setFreshStars(String freshStars) {
        this.freshStars = freshStars;
    }

    public int getStarGreen() {
        return starGreen;
    }

    public void setStarGreen(int starGreen) {
        this.starGreen = starGreen;
    }

    public int getStarGray() {
        return starGray;
    }

    public void setStarGray(int starGray) {
        this.starGray = starGray;
    }

    public int getStarHalf() {
        return starHalf;
    }

    public void setStarHalf(int starHalf) {
        this.starHalf = starHalf;
    }

    public Integer getNxDistributerSysCityId() {
        return nxDistributerSysCityId;
    }

    public void setNxDistributerSysCityId(Integer nxDistributerSysCityId) {
        this.nxDistributerSysCityId = nxDistributerSysCityId;
    }

    public Integer getNxDistributerSysMarketId() {
        return nxDistributerSysMarketId;
    }

    public void setNxDistributerSysMarketId(Integer nxDistributerSysMarketId) {
        this.nxDistributerSysMarketId = nxDistributerSysMarketId;
    }

    public NxDistributerUserEntity getNxDistributerUserEntity() {
        return nxDistributerUserEntity;
    }

    public void setNxDistributerUserEntity(NxDistributerUserEntity nxDistributerUserEntity) {
        this.nxDistributerUserEntity = nxDistributerUserEntity;
    }

    /**
     *  批发商商业类型
     */
    public Integer nxDistributerBusinessTypeId;
    public String nxDistributerBuyQuantity;
    public Integer nxDistributerType;
    public Integer nxDistributerShelfQuantity;

    public String nxDistributerImg;
    public String nxDistributerAppId;

    public String nxDistributerManager;
    public String nxDistributerPayUrl;
    public String nxDistributerPhone;
    public String nxDistributerAddress;
    public String nxDistributerMarketName;
    public String distance;
    public double distanceValue;
    public String duration;
    public Boolean isSelected = false;

    public int linshiCount = 0;

    public Double purTimes = 0.0;
    public String purTimesString;
    public Double purGoodsTotal = 0.0;
    public String purGoodsTotalString;

    public Double stockGoodsTotal = 0.0;
    public String stockGoodsTotalString;
    public Double produceGoodsTotal = 0.0;
    public String produceGoodsTotalString;
    public Double wasteGoodsTotal = 0.0;
    public String wasteGoodsTotalString;

    public Double lossGoodsTotal = 0.0;
    public String lossGoodsTotalString;
    public Double returnGoodsTotal = 0.0;
    public String returnGoodsTotalString;
    public String nxDistributerShowName;

    public String total;
    public int billCount;

    public String freshStars;
    public int starGreen;
    public int starGray;
    public int starHalf;
    public Integer nxDistributerSysCityId;
    public Integer nxDistributerSysMarketId;

    public NxDistributerUserEntity nxDistributerUserEntity;
   
}
