package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/12/22 0022.
 */

public class NxDistributerGoodsEntity implements Serializable{

    public Integer nxCommunityFatherGoodsId;

    public static final long serialVersionUID = 1L;

    /**
     *  社区商品id
     */
    public Integer nxDistributerGoodsId;
    /**
     *  批发商id
     */
    public Integer nxDgDistributerId;
    /**
     *  商品状态
     */
    public Integer nxDgGoodsStatus;
    /**
     *  是否称重
     */
    public Integer nxDgGoodsIsWeight;
    /**
     *  批发商商品父类id
     */
    public Integer nxDgDfgGoodsFatherId;
    /**
     *  购买热度
     */
    public Integer nxDgNxGoodsId;
    /**
     *  采购数量
     */
    public Integer nxDgNxFatherId;
    public String nxDgNxFatherImg;
    public String nxDgNxFatherName;

    public Integer nxDgNxGrandId;
    public String nxDgNxGrandName;
    public Integer nxDgNxGreatGrandId;
    public String nxDgNxGreatGrandName;
    public String nxDgGoodsFileLarge;

    public List<NxDepartmentOrdersEntity> getNxDepartmentOrdersEntities() {
        return nxDepartmentOrdersEntities;
    }

    public void setNxDepartmentOrdersEntities(List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities) {
        this.nxDepartmentOrdersEntities = nxDepartmentOrdersEntities;
    }

    public List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
    /**
     *
     */
    public String nxDgGoodsFile;
    /**
     *  商品名称
     */
    public String nxDgGoodsName;

    public Integer getNxCommunityFatherGoodsId() {
        return nxCommunityFatherGoodsId;
    }

    public void setNxCommunityFatherGoodsId(Integer nxCommunityFatherGoodsId) {
        this.nxCommunityFatherGoodsId = nxCommunityFatherGoodsId;
    }

    public Integer getNxDistributerGoodsId() {
        return nxDistributerGoodsId;
    }

    public void setNxDistributerGoodsId(Integer nxDistributerGoodsId) {
        this.nxDistributerGoodsId = nxDistributerGoodsId;
    }

    public Integer getNxDgDistributerId() {
        return nxDgDistributerId;
    }

    public void setNxDgDistributerId(Integer nxDgDistributerId) {
        this.nxDgDistributerId = nxDgDistributerId;
    }

    public Integer getNxDgGoodsStatus() {
        return nxDgGoodsStatus;
    }

    public void setNxDgGoodsStatus(Integer nxDgGoodsStatus) {
        this.nxDgGoodsStatus = nxDgGoodsStatus;
    }

    public Integer getNxDgGoodsIsWeight() {
        return nxDgGoodsIsWeight;
    }

    public void setNxDgGoodsIsWeight(Integer nxDgGoodsIsWeight) {
        this.nxDgGoodsIsWeight = nxDgGoodsIsWeight;
    }

    public Integer getNxDgDfgGoodsFatherId() {
        return nxDgDfgGoodsFatherId;
    }

    public void setNxDgDfgGoodsFatherId(Integer nxDgDfgGoodsFatherId) {
        this.nxDgDfgGoodsFatherId = nxDgDfgGoodsFatherId;
    }

    public Integer getNxDgNxGoodsId() {
        return nxDgNxGoodsId;
    }

    public void setNxDgNxGoodsId(Integer nxDgNxGoodsId) {
        this.nxDgNxGoodsId = nxDgNxGoodsId;
    }

    public Integer getNxDgNxFatherId() {
        return nxDgNxFatherId;
    }

    public void setNxDgNxFatherId(Integer nxDgNxFatherId) {
        this.nxDgNxFatherId = nxDgNxFatherId;
    }

    public String getNxDgNxFatherImg() {
        return nxDgNxFatherImg;
    }

    public void setNxDgNxFatherImg(String nxDgNxFatherImg) {
        this.nxDgNxFatherImg = nxDgNxFatherImg;
    }

    public String getNxDgNxFatherName() {
        return nxDgNxFatherName;
    }

    public void setNxDgNxFatherName(String nxDgNxFatherName) {
        this.nxDgNxFatherName = nxDgNxFatherName;
    }

    public Integer getNxDgNxGrandId() {
        return nxDgNxGrandId;
    }

    public void setNxDgNxGrandId(Integer nxDgNxGrandId) {
        this.nxDgNxGrandId = nxDgNxGrandId;
    }

    public String getNxDgNxGrandName() {
        return nxDgNxGrandName;
    }

    public void setNxDgNxGrandName(String nxDgNxGrandName) {
        this.nxDgNxGrandName = nxDgNxGrandName;
    }

    public Integer getNxDgNxGreatGrandId() {
        return nxDgNxGreatGrandId;
    }

    public void setNxDgNxGreatGrandId(Integer nxDgNxGreatGrandId) {
        this.nxDgNxGreatGrandId = nxDgNxGreatGrandId;
    }

    public String getNxDgNxGreatGrandName() {
        return nxDgNxGreatGrandName;
    }

    public void setNxDgNxGreatGrandName(String nxDgNxGreatGrandName) {
        this.nxDgNxGreatGrandName = nxDgNxGreatGrandName;
    }

    public String getNxDgGoodsFileLarge() {
        return nxDgGoodsFileLarge;
    }

    public void setNxDgGoodsFileLarge(String nxDgGoodsFileLarge) {
        this.nxDgGoodsFileLarge = nxDgGoodsFileLarge;
    }

    public String getNxDgGoodsFile() {
        return nxDgGoodsFile;
    }

    public void setNxDgGoodsFile(String nxDgGoodsFile) {
        this.nxDgGoodsFile = nxDgGoodsFile;
    }

    public String getNxDgGoodsName() {
        return nxDgGoodsName;
    }

    public void setNxDgGoodsName(String nxDgGoodsName) {
        this.nxDgGoodsName = nxDgGoodsName;
    }

    public String getNxDgGoodsDetail() {
        return nxDgGoodsDetail;
    }

    public void setNxDgGoodsDetail(String nxDgGoodsDetail) {
        this.nxDgGoodsDetail = nxDgGoodsDetail;
    }

    public String getNxDgGoodsBrand() {
        return nxDgGoodsBrand;
    }

    public void setNxDgGoodsBrand(String nxDgGoodsBrand) {
        this.nxDgGoodsBrand = nxDgGoodsBrand;
    }

    public String getNxDgGoodsPlace() {
        return nxDgGoodsPlace;
    }

    public void setNxDgGoodsPlace(String nxDgGoodsPlace) {
        this.nxDgGoodsPlace = nxDgGoodsPlace;
    }

    public String getNxDgGoodsStandardname() {
        return nxDgGoodsStandardname;
    }

    public void setNxDgGoodsStandardname(String nxDgGoodsStandardname) {
        this.nxDgGoodsStandardname = nxDgGoodsStandardname;
    }

    public String getNxDgGoodsStandardWeight() {
        return nxDgGoodsStandardWeight;
    }

    public void setNxDgGoodsStandardWeight(String nxDgGoodsStandardWeight) {
        this.nxDgGoodsStandardWeight = nxDgGoodsStandardWeight;
    }

    public String getNxDgGoodsPinyin() {
        return nxDgGoodsPinyin;
    }

    public void setNxDgGoodsPinyin(String nxDgGoodsPinyin) {
        this.nxDgGoodsPinyin = nxDgGoodsPinyin;
    }

    public String getNxDgGoodsPy() {
        return nxDgGoodsPy;
    }

    public void setNxDgGoodsPy(String nxDgGoodsPy) {
        this.nxDgGoodsPy = nxDgGoodsPy;
    }

    public Integer getNxDgPullOff() {
        return nxDgPullOff;
    }

    public void setNxDgPullOff(Integer nxDgPullOff) {
        this.nxDgPullOff = nxDgPullOff;
    }

    public String getNxDgNxGoodsFatherColor() {
        return nxDgNxGoodsFatherColor;
    }

    public void setNxDgNxGoodsFatherColor(String nxDgNxGoodsFatherColor) {
        this.nxDgNxGoodsFatherColor = nxDgNxGoodsFatherColor;
    }

    public String getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(String sellAmount) {
        this.sellAmount = sellAmount;
    }

    public String getSellSubtotal() {
        return sellSubtotal;
    }

    public void setSellSubtotal(String sellSubtotal) {
        this.sellSubtotal = sellSubtotal;
    }

    public Integer getNxDgPurchaseAuto() {
        return nxDgPurchaseAuto;
    }

    public void setNxDgPurchaseAuto(Integer nxDgPurchaseAuto) {
        this.nxDgPurchaseAuto = nxDgPurchaseAuto;
    }

    public Integer getNxDgGoodsSort() {
        return nxDgGoodsSort;
    }

    public void setNxDgGoodsSort(Integer nxDgGoodsSort) {
        this.nxDgGoodsSort = nxDgGoodsSort;
    }

    public String getNxDgBuyingPrice() {
        return nxDgBuyingPrice;
    }

    public void setNxDgBuyingPrice(String nxDgBuyingPrice) {
        this.nxDgBuyingPrice = nxDgBuyingPrice;
    }

    public String getNxDgPriceProfitOne() {
        return nxDgPriceProfitOne;
    }

    public void setNxDgPriceProfitOne(String nxDgPriceProfitOne) {
        this.nxDgPriceProfitOne = nxDgPriceProfitOne;
    }

    public String getNxDgPriceProfitTwo() {
        return nxDgPriceProfitTwo;
    }

    public void setNxDgPriceProfitTwo(String nxDgPriceProfitTwo) {
        this.nxDgPriceProfitTwo = nxDgPriceProfitTwo;
    }

    public String getNxDgPriceProfitThree() {
        return nxDgPriceProfitThree;
    }

    public void setNxDgPriceProfitThree(String nxDgPriceProfitThree) {
        this.nxDgPriceProfitThree = nxDgPriceProfitThree;
    }

    public Integer getNxDgSupplierId() {
        return nxDgSupplierId;
    }

    public void setNxDgSupplierId(Integer nxDgSupplierId) {
        this.nxDgSupplierId = nxDgSupplierId;
    }

    public String getNxDgBuyingPriceUpdate() {
        return nxDgBuyingPriceUpdate;
    }

    public void setNxDgBuyingPriceUpdate(String nxDgBuyingPriceUpdate) {
        this.nxDgBuyingPriceUpdate = nxDgBuyingPriceUpdate;
    }

    public String getNxDgWillPrice() {
        return nxDgWillPrice;
    }

    public void setNxDgWillPrice(String nxDgWillPrice) {
        this.nxDgWillPrice = nxDgWillPrice;
    }

    public String getNxDgWillPriceOne() {
        return nxDgWillPriceOne;
    }

    public void setNxDgWillPriceOne(String nxDgWillPriceOne) {
        this.nxDgWillPriceOne = nxDgWillPriceOne;
    }

    public String getNxDgWillPriceOneWeight() {
        return nxDgWillPriceOneWeight;
    }

    public void setNxDgWillPriceOneWeight(String nxDgWillPriceOneWeight) {
        this.nxDgWillPriceOneWeight = nxDgWillPriceOneWeight;
    }

    public String getNxDgWillPriceTwo() {
        return nxDgWillPriceTwo;
    }

    public void setNxDgWillPriceTwo(String nxDgWillPriceTwo) {
        this.nxDgWillPriceTwo = nxDgWillPriceTwo;
    }

    public String getNxDgWillPriceTwoWeight() {
        return nxDgWillPriceTwoWeight;
    }

    public void setNxDgWillPriceTwoWeight(String nxDgWillPriceTwoWeight) {
        this.nxDgWillPriceTwoWeight = nxDgWillPriceTwoWeight;
    }

    public String getNxDgWillPriceThree() {
        return nxDgWillPriceThree;
    }

    public void setNxDgWillPriceThree(String nxDgWillPriceThree) {
        this.nxDgWillPriceThree = nxDgWillPriceThree;
    }

    public String getNxDgWillPriceThreeWeight() {
        return nxDgWillPriceThreeWeight;
    }

    public void setNxDgWillPriceThreeWeight(String nxDgWillPriceThreeWeight) {
        this.nxDgWillPriceThreeWeight = nxDgWillPriceThreeWeight;
    }

    public String getNxDgBuyingPriceOne() {
        return nxDgBuyingPriceOne;
    }

    public void setNxDgBuyingPriceOne(String nxDgBuyingPriceOne) {
        this.nxDgBuyingPriceOne = nxDgBuyingPriceOne;
    }

    public String getNxDgBuyingPriceOneUpdate() {
        return nxDgBuyingPriceOneUpdate;
    }

    public void setNxDgBuyingPriceOneUpdate(String nxDgBuyingPriceOneUpdate) {
        this.nxDgBuyingPriceOneUpdate = nxDgBuyingPriceOneUpdate;
    }

    public String getNxDgBuyingPriceTwo() {
        return nxDgBuyingPriceTwo;
    }

    public void setNxDgBuyingPriceTwo(String nxDgBuyingPriceTwo) {
        this.nxDgBuyingPriceTwo = nxDgBuyingPriceTwo;
    }

    public String getNxDgBuyingPriceTwoUpdate() {
        return nxDgBuyingPriceTwoUpdate;
    }

    public void setNxDgBuyingPriceTwoUpdate(String nxDgBuyingPriceTwoUpdate) {
        this.nxDgBuyingPriceTwoUpdate = nxDgBuyingPriceTwoUpdate;
    }

    public String getNxDgWillPriceUpdate() {
        return nxDgWillPriceUpdate;
    }

    public void setNxDgWillPriceUpdate(String nxDgWillPriceUpdate) {
        this.nxDgWillPriceUpdate = nxDgWillPriceUpdate;
    }

    public String getNxDgBuyingPriceThree() {
        return nxDgBuyingPriceThree;
    }

    public void setNxDgBuyingPriceThree(String nxDgBuyingPriceThree) {
        this.nxDgBuyingPriceThree = nxDgBuyingPriceThree;
    }

    public String getNxDgPriceFirstDay() {
        return nxDgPriceFirstDay;
    }

    public void setNxDgPriceFirstDay(String nxDgPriceFirstDay) {
        this.nxDgPriceFirstDay = nxDgPriceFirstDay;
    }

    public String getNxDgPriceSecondDay() {
        return nxDgPriceSecondDay;
    }

    public void setNxDgPriceSecondDay(String nxDgPriceSecondDay) {
        this.nxDgPriceSecondDay = nxDgPriceSecondDay;
    }

    public String getNxDgPriceThirdDay() {
        return nxDgPriceThirdDay;
    }

    public void setNxDgPriceThirdDay(String nxDgPriceThirdDay) {
        this.nxDgPriceThirdDay = nxDgPriceThirdDay;
    }

    public String getNxDgBuyingPriceThreeUpdate() {
        return nxDgBuyingPriceThreeUpdate;
    }

    public void setNxDgBuyingPriceThreeUpdate(String nxDgBuyingPriceThreeUpdate) {
        this.nxDgBuyingPriceThreeUpdate = nxDgBuyingPriceThreeUpdate;
    }

    public Integer getNxDgBuyingPriceIsGrade() {
        return nxDgBuyingPriceIsGrade;
    }

    public void setNxDgBuyingPriceIsGrade(Integer nxDgBuyingPriceIsGrade) {
        this.nxDgBuyingPriceIsGrade = nxDgBuyingPriceIsGrade;
    }

    public Integer getNxDgDfgGoodsGrandId() {
        return nxDgDfgGoodsGrandId;
    }

    public void setNxDgDfgGoodsGrandId(Integer nxDgDfgGoodsGrandId) {
        this.nxDgDfgGoodsGrandId = nxDgDfgGoodsGrandId;
    }

    public Integer getNxDgIsOldestSon() {
        return nxDgIsOldestSon;
    }

    public void setNxDgIsOldestSon(Integer nxDgIsOldestSon) {
        this.nxDgIsOldestSon = nxDgIsOldestSon;
    }

    public String getOrderContent() {
        return orderContent;
    }

    public void setOrderContent(String orderContent) {
        this.orderContent = orderContent;
    }

    public String getNxDgOutTotalWeight() {
        return nxDgOutTotalWeight;
    }

    public void setNxDgOutTotalWeight(String nxDgOutTotalWeight) {
        this.nxDgOutTotalWeight = nxDgOutTotalWeight;
    }

    public Integer getOrderSize() {
        return orderSize;
    }

    public void setOrderSize(Integer orderSize) {
        this.orderSize = orderSize;
    }

    public Integer getNxDgGoodsSonsSort() {
        return nxDgGoodsSonsSort;
    }

    public void setNxDgGoodsSonsSort(Integer nxDgGoodsSonsSort) {
        this.nxDgGoodsSonsSort = nxDgGoodsSonsSort;
    }

    public Integer getNxDgGoodsIsHidden() {
        return nxDgGoodsIsHidden;
    }

    public void setNxDgGoodsIsHidden(Integer nxDgGoodsIsHidden) {
        this.nxDgGoodsIsHidden = nxDgGoodsIsHidden;
    }

    public String getPerPrice() {
        return perPrice;
    }

    public void setPerPrice(String perPrice) {
        this.perPrice = perPrice;
    }

    public Integer getIsDownload() {
        return isDownload;
    }

    public void setIsDownload(Integer isDownload) {
        this.isDownload = isDownload;
    }

    public NxDistributerGoodsShelfGoodsEntity getShelfGoodsEntity() {
        return shelfGoodsEntity;
    }

    public void setShelfGoodsEntity(NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity) {
        this.shelfGoodsEntity = shelfGoodsEntity;
    }

    /**
     *  商品详细
     */
    public String nxDgGoodsDetail;
    public String nxDgGoodsBrand;
    public String nxDgGoodsPlace;
    /**
     *  商品规格
     */
    public String nxDgGoodsStandardname;

    public String nxDgGoodsStandardWeight;
    /**
     *  社区商品拼音
     */
    public String nxDgGoodsPinyin;
    /**
     *  社区商品拼音简拼
     */
    public String nxDgGoodsPy;


    public Integer nxDgPullOff;

    public String nxDgNxGoodsFatherColor;

    public String sellAmount;
    public String sellSubtotal;
    public Integer nxDgPurchaseAuto;
    public Integer nxDgGoodsSort;

    public String nxDgBuyingPrice;
    public String nxDgPriceProfitOne;
    public String nxDgPriceProfitTwo;
    public String nxDgPriceProfitThree;
    public Integer nxDgSupplierId;
    public String nxDgBuyingPriceUpdate;

    public String nxDgWillPrice;
    public String nxDgWillPriceOne;
    public String nxDgWillPriceOneWeight;
    public String nxDgWillPriceTwo;
    public String nxDgWillPriceTwoWeight;
    public String nxDgWillPriceThree;
    public String nxDgWillPriceThreeWeight;

    public String nxDgBuyingPriceOne;
    public String nxDgBuyingPriceOneUpdate;
    public String nxDgBuyingPriceTwo;
    public String nxDgBuyingPriceTwoUpdate;
    public String nxDgWillPriceUpdate;

    public String nxDgBuyingPriceThree;
    public String nxDgPriceFirstDay;
    public String nxDgPriceSecondDay;
    public String nxDgPriceThirdDay;
    public String nxDgBuyingPriceThreeUpdate;
    public Integer nxDgBuyingPriceIsGrade;
    public Integer nxDgDfgGoodsGrandId;
    public Integer nxDgIsOldestSon;
    public String  orderContent;
    public String  nxDgOutTotalWeight;
    public Integer orderSize;
    public Integer nxDgGoodsSonsSort;
    public Integer nxDgGoodsIsHidden;
    public String perPrice;

    public Integer isDownload;

//    public List<NxDepartmentOrdersEntity> nxDepartmentOrdersEntities;
//    public List<NxDepartmentOrdersEntity> histfyOrdersEntities;
//    public List<NxDepartmentOrdersEntity> neetNotPurOrders;
//    public NxDepartmentOrdersEntity nxDepartmentOrdersEntity;
//    public List<NxRestrauntOrdersEntity> nxRestrauntOrdersEntities;

   
    public NxDistributerGoodsShelfGoodsEntity shelfGoodsEntity;

   
}
