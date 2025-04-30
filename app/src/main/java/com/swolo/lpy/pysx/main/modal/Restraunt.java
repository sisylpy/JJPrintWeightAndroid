package com.swolo.lpy.pysx.main.modal;

import java.io.Serializable;

public class Restraunt implements Serializable{

    /**
     *  订货部门id
     */
    public Integer nxRestrauntId;
    /**
     *  订货部门名称
     */
    public String nxRestrauntName;
    /**
     *  订货部门上级id
     */
    public Integer nxRestrauntFatherId;
    /**
     *  订货部门类型
     */
    public String nxRestrauntType;
    /**
     *  订货部门子部门数量
     */
    public Integer nxRestrauntSubAmount;
    /**
     *  订货部门批发商id
     */
    public Integer nxRestrauntComId;
    /**
     *
     */
    public String nxRestrauntFilePath;
    /**
     *  是客户吗
     */
    public Integer nxRestrauntIsGroupDep;
    /**
     *
     */
    public String nxRestrauntPrintName;
    /**
     *
     */
    public Integer nxRestrauntShowWeeks;
    /**
     *
     */
    public Integer nxRestrauntSettleType;

    /**
     *  客户简称
     */
    public String nxRestrauntAttrName;
    public String nxRestrauntLat;
    public String nxRestrauntLng;
    public String nxRestrauntAddress;
    public String nxRestrauntNavigationAddress;
    public String nxRestrauntNumber;
    public Integer nxRestrauntServiceLevel;
    public Integer nxRestrauntDriverId;
    public Integer nxRestrauntOweBoxNumber;
    public Integer nxRestrauntDeliveryBoxNumber;
    public Integer nxRestrauntWorkingStatus;
    public String nxRestrauntMixTime;
    public String nxRestrauntMaxTime;
    public String nxRestrauntDeliveryCost;
    public String nxRestrauntDeliveryLimit;

}
